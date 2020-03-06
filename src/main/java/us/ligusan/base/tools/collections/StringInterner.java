package us.ligusan.base.tools.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Weak strings cache. Alternative to synchronized WeakHashMap.
 * 
 * @author Alexander Prishchepov
 */
public class StringInterner
{
    private AtomicReferenceArray<Entry> table;
    private ReferenceQueue<String> queue;
    private Thread expungeThread;
    private volatile boolean stopped;

    public StringInterner(final int pInitialCapacity)
    {
        int lCapacity = 1;
        // aprishchepov - Mar 6, 2020 5:02:34 PM : max size is 1 << 30, we don't resize the collection, makes things much easier
        while(lCapacity < pInitialCapacity && lCapacity < 1 << 30)
            lCapacity <<= 1;

        // aprishchepov - Feb 1, 2020 7:58:54 PM : maximum size, so we never resize it
        table = new AtomicReferenceArray<>(lCapacity);
        queue = new ReferenceQueue<>();

        expungeThread = new Thread(() -> {
            for(int lSleep = 0; !isStopped();)
            {
                if(lSleep > 0) try
                {
                    Thread.sleep(lSleep);
                }
                catch(InterruptedException e)
                {
                    Thread.interrupted();
                }

                boolean lProcessed = false;
                for(Entry lEntryToRemove = null; !isStopped() && (lEntryToRemove = (Entry)getQueue().poll()) != null; lProcessed = true)
                    removeEntry(lEntryToRemove);

                lSleep = lProcessed ? Math.max(0, lSleep - 1) : lSleep + 1;
            }
        }, getClass().getSimpleName());
        expungeThread.setDaemon(true);
        expungeThread.start();
    }

    protected AtomicReferenceArray<Entry> getTable()
    {
        return table;
    }
    protected void setTable(final AtomicReferenceArray<Entry> pTable)
    {
        table = pTable;
    }
    protected ReferenceQueue<String> getQueue()
    {
        return queue;
    }
    protected void setQueue(final ReferenceQueue<String> pQueue)
    {
        queue = pQueue;
    }
    protected Thread getExpungeThread()
    {
        return expungeThread;
    }
    protected void setExpungeThread(Thread pExpungeThread)
    {
        expungeThread = pExpungeThread;
    }
    protected boolean isStopped()
    {
        return stopped;
    }
    protected void setStopped(boolean pStopped)
    {
        stopped = pStopped;
    }
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("table", table).append("queue", queue).append("expungeThread", expungeThread).append("stopped", stopped).toString();
    }

    /**
     * Returns index for hash code h.
     */
    protected int indexFor(int h)
    {
        return h & (getTable().length() - 1);
    }

    protected void removeEntry(final Entry pEntryToRemove)
    {
        AtomicReferenceArray<Entry> lTable = getTable();
        int lIndex = indexFor(pEntryToRemove.getHash());

        for(Entry lPreviousEntry = null, lCurrentEntry = lTable.get(lIndex), lNextEntry = null; !isStopped() && lCurrentEntry != null; lCurrentEntry = lNextEntry)
        {
            lNextEntry = lCurrentEntry.getNext().get();

            // aprishchepov - Feb 2, 2020 6:09:19 PM : removing empty entry - previous does not change
            if(lCurrentEntry == pEntryToRemove || lCurrentEntry.get() == null)
            {
                // aprishchepov - Feb 2, 2020 6:01:19 PM : in the middle of the list
                if(lPreviousEntry != null)
                    // aprishchepov - Feb 2, 2020 12:02:00 PM : successful or not, we removed old entry
                    lPreviousEntry.getNext().compareAndSet(lCurrentEntry, lNextEntry);
                // aprishchepov - Feb 2, 2020 6:21:49 PM : root of the list
                else
                    // aprishchepov - Feb 2, 2020 7:07:19 PM : root was not removed
                    if(!lTable.compareAndSet(lIndex, lCurrentEntry, lNextEntry))
                        // aprishchepov - Feb 2, 2020 6:01:52 PM : our entry might been pushed down the list - let's go from the start
                        if(lCurrentEntry == pEntryToRemove)
                {
                    lNextEntry = lTable.get(lIndex);
                    continue;
                }
                        // aprishchepov - Feb 2, 2020 7:11:11 PM : it was not our entry anyway - move on
                        else lPreviousEntry = lCurrentEntry;

                // aprishchepov - Feb 2, 2020 6:23:40 PM : our entry is removed
                if(lCurrentEntry == pEntryToRemove) break;
            }
            else lPreviousEntry = lCurrentEntry;
        }
    }

    public String get(final String pKey)
    {
        if(pKey == null) return null;

        /*
         * Retrieve object hash code and applies a supplemental hash function to the
         * result hash, which defends against poor quality hash functions. This is
         * critical because HashMap uses power-of-two length hash tables, that
         * otherwise encounter collisions for hashCodes that do not differ
         * in lower bits.
         */
        int lHash = pKey.hashCode();
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        lHash ^= (lHash >>> 20) ^ (lHash >>> 12);
        lHash = lHash ^ (lHash >>> 7) ^ (lHash >>> 4);

        AtomicReferenceArray<Entry> lTable = getTable();
        int lIndex = indexFor(lHash);

        // aprishchepov - Feb 3, 2020 12:16:33 PM : fast get first - no locking
        Entry lFirstEntryToGet = lTable.get(lIndex);
        for(Entry lCurrentEntry = lFirstEntryToGet; lCurrentEntry != null; lCurrentEntry = lCurrentEntry.getNext().get())
        {
            if(lHash == lCurrentEntry.getHash())
            {
                String lCurrentKey = lCurrentEntry.get();
                if(pKey.equals(lCurrentKey))
                {
                    for(int lWait = 0;; lWait++)
                        try
                        {
                            if(lCurrentEntry.getSearchLatch().await(lWait, TimeUnit.MILLISECONDS)) break;
                        }
                        catch(InterruptedException e)
                        {
                            Thread.interrupted();
                        }

                    // aprishchepov - Feb 4, 2020 10:50:21 AM : value would be either null or real value
                    return StringUtils.defaultString(lCurrentEntry.getValue(), lCurrentKey);
                }
            }
        }

        // aprishchepov - Feb 2, 2020 5:11:30 PM : my guess, we do new string so that we do not pollute our weak hash with strings from constant pool
        String lNewKey = new String(pKey);
        Entry lFirstEntry = lTable.get(lIndex);
        Entry lNewEntry = new Entry(lNewKey = new String(pKey), getQueue(), lHash, lFirstEntry);
        // aprishchepov - Feb 3, 2020 3:16:50 PM : write into beginning
        for(; !lTable.compareAndSet(lIndex, lFirstEntry, lNewEntry); lNewEntry.getNext().set(lFirstEntry = lTable.get(lIndex)));
        CountDownLatch lNewSearchLatch = lNewEntry.getSearchLatch();

        // aprishchepov - Feb 10, 2020 12:00:03 PM : just in case very first entry was not removed - it will shorten our search
        for(Entry lPreviousEntry = lNewEntry, lCurrentEntry = lFirstEntry, lNextEntry = null; lCurrentEntry != null && lCurrentEntry != lFirstEntryToGet; lCurrentEntry = lNextEntry)
        {
            String lCurrentKey = lCurrentEntry.get();
            if(lHash == lCurrentEntry.getHash() && pKey.equals(lCurrentKey))
            {
                for(int lWait = 0;; lWait++)
                    try
                    {
                        if(lCurrentEntry.getSearchLatch().await(lWait, TimeUnit.MILLISECONDS)) break;
                    }
                    catch(InterruptedException e)
                    {
                        Thread.interrupted();
                    }

                // aprishchepov - Feb 4, 2020 10:50:21 AM : found real value real value
                String ret = StringUtils.defaultString(lCurrentEntry.getValue(), lCurrentKey);
                lNewEntry.setValue(ret);
                lNewSearchLatch.countDown();

                // aprishchepov - Feb 10, 2020 12:39:18 PM : someone already inserted the same value
                removeEntry(lNewEntry);

                return ret;
            }

            lNextEntry = lCurrentEntry.getNext().get();

            // aprishchepov - Feb 2, 2020 11:16:33 AM : removing empty entry - previous does not change
            if(lCurrentKey == null)
                // aprishchepov - Feb 2, 2020 12:02:00 PM : successful or not, we removed entry
                lPreviousEntry.getNext().compareAndSet(lCurrentEntry, lNextEntry);
            else lPreviousEntry = lCurrentEntry;
        }

        // aprishchepov - Feb 10, 2020 11:59:26 AM : did not find anything - new entry is the real one
        lNewSearchLatch.countDown();

        return lNewKey;
    }

    /**
     * The entries in this hash table extend WeakReference, using its main ref
     * field as the key.
     */
    private static class Entry extends WeakReference<String>
    {
        private int hash;
        private AtomicReference<Entry> next;
        private CountDownLatch searchLatch;
        private String value;

        public Entry(final String pKey, final ReferenceQueue<String> pQueue, final int pHash, final Entry pNext)
        {
            super(pKey, pQueue);

            hash = pHash;
            next = new AtomicReference<Entry>(pNext);
            searchLatch = new CountDownLatch(1);
        }

        protected int getHash()
        {
            return hash;
        }
        protected void setHash(final int pHash)
        {
            hash = pHash;
        }
        protected AtomicReference<Entry> getNext()
        {
            return next;
        }
        protected void setNext(final AtomicReference<Entry> pNext)
        {
            next = pNext;
        }
        protected CountDownLatch getSearchLatch()
        {
            return searchLatch;
        }
        protected void setSearchLatch(CountDownLatch pSearchLatch)
        {
            searchLatch = pSearchLatch;
        }
        protected String getValue()
        {
            return value;
        }
        protected void setValue(String pValue)
        {
            value = pValue;
        }
        public String toString()
        {
            return new ToStringBuilder(this).appendSuper(super.toString()).append("hash", hash).append("next", next).append("searchLatch", searchLatch).append("value", value).toString();
        }
    }

    public void shutdown()
    {
        setStopped(true);

        getExpungeThread().interrupt();
    }
}
