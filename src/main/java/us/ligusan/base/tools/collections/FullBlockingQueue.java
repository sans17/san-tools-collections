package us.ligusan.base.tools.collections;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.IteratorUtils;

/**
 * Always empty, always full, blocking queue. Only waited operations are respected.
 * 
 * @author Alexander Prishchepov
 */
public class FullBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
{
    // san - Jan 6, 2019 4:11:55 PM : need to have same objects and reference them uniquely
    private final BlockingQueue<HardReference<E>> waitingElementsQueue;

    public FullBlockingQueue()
    {
        // san - Jan 6, 2019 2:51:36 PM : we need unbounded queue
        waitingElementsQueue = new LinkedBlockingQueue<HardReference<E>>();
    }

    protected E dereference(final HardReference<E> pReference)
    {
        // san - Jan 6, 2019 1:48:52 PM : nobody's offering
        if(pReference == null) return null;

        // san - Jan 5, 2019 5:26:48 PM : we polled something
        synchronized(pReference)
        {
            // san - Jan 6, 2019 1:42:16 PM : only one thread should be waiting on it
            pReference.notify();
        }

        return pReference.get();
    }

    @Override
    public E poll()
    {
        return dereference(waitingElementsQueue.poll());
    }
    @Override
    public E peek()
    {
        // san - Jan 4, 2019 9:03:14 PM : this queue is always empty
        return null;
    }

    protected boolean offer(final E pE, final Long pNanos) throws InterruptedException
    {
        long lNanos = pNanos == null ? TimeUnit.SECONDS.toNanos(1) : pNanos;
        long lNanoTimeEnd = System.nanoTime() + lNanos;

        boolean ret = false;

        HardReference<E> lWaitingElementReference = new HardReference<>(pE);
        try
        {
            // san - Jan 5, 2019 1:08:52 PM : add element into waiting queue
            if(pNanos == null) waitingElementsQueue.put(lWaitingElementReference);
            else
            // san - Jan 6, 2019 8:28:20 PM : return false if ran out of time inserting
            if(!waitingElementsQueue.offer(lWaitingElementReference, lNanoTimeEnd - System.nanoTime(), TimeUnit.NANOSECONDS)) return false;

            // san - Jan 6, 2019 7:02:49 PM : wait in 1sec intervals if pNanos is null
            while(waitingElementsQueue.contains(lWaitingElementReference) && (pNanos == null || (lNanos = lNanoTimeEnd - System.nanoTime()) > 0))
            {
                long lMillis = TimeUnit.NANOSECONDS.toMillis(lNanos);
                synchronized(lWaitingElementReference)
                {
                    lWaitingElementReference.wait(lMillis, (int)(lNanos - TimeUnit.MILLISECONDS.toNanos(lMillis)));
                }
            }
        }
        finally
        {
            // san - Jan 5, 2019 1:11:00 PM : remove from queue in case we got interrupted
            // san - Jan 5, 2019 1:22:33 PM : if reference is not there, it was polled out
            ret = !waitingElementsQueue.remove(lWaitingElementReference);
        }

        return ret;
    }

    @Override
    public boolean offer(final E pE)
    {
        try
        {
            // san - Jan 6, 2019 7:21:09 PM : let's try to insert something, 
            return offer(pE, 0L);
        }
        catch(InterruptedException e)
        {
            Thread.interrupted();

            // san - Jan 6, 2019 7:19:19 PM : if we were interrupted, nothing happened 
            return false;
        }
    }
    @Override
    public void put(final E pE) throws InterruptedException
    {
        // san - Jan 6, 2019 8:01:50 PM : offering until interrupted
        offer(pE, null);
    }
    @Override
    public boolean offer(final E pE, final long pTimeout, final TimeUnit pUnit) throws InterruptedException
    {
        // san - Jan 6, 2019 8:02:12 PM : timed offering
        return offer(pE, pUnit.toNanos(pTimeout));
    }
    @Override
    public E take() throws InterruptedException
    {
        return dereference(waitingElementsQueue.take());
    }
    @Override
    public E poll(final long pTimeout, final TimeUnit pUnit) throws InterruptedException
    {
        return dereference(waitingElementsQueue.poll(pTimeout, pUnit));
    }
    @Override
    public int remainingCapacity()
    {
        // san - Dec 9, 2018 3:46:39 PM : this queue is always full
        return 0;
    }
    @Override
    public int drainTo(final Collection<? super E> pC)
    {
        // san - Dec 9, 2018 3:46:39 PM : this queue is always full
        return 0;
    }
    @Override
    public int drainTo(final Collection<? super E> pC, final int pMaxElements)
    {
        // san - Dec 9, 2018 3:46:39 PM : this queue is always full
        return 0;
    }
    @Override
    public Iterator<E> iterator()
    {
        // san - Dec 9, 2018 3:46:39 PM : this queue is always full
        return IteratorUtils.emptyIterator();
    }
    @Override
    public int size()
    {
        // san - Dec 9, 2018 3:46:39 PM : this queue is always full
        return 0;
    }
}
