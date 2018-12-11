package us.ligusan.base.tools.collections;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.apache.commons.collections4.IteratorUtils;

/**
 * Always full, blocking queue. Waiting on {@link #poll(long, TimeUnit)}
 * 
 * @author Alexander Prishchepov
 */
public class FullBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
{
    @Override
    public E poll()
    {
        return null;
    }
    @Override
    public E peek()
    {
        return null;
    }
    @Override
    public boolean offer(final E pE)
    {
        // san - Dec 9, 2018 3:47:02 PM : this queue is always full
        return false;
    }
    @Override
    public void put(final E pE) throws InterruptedException
    {
        // san - Dec 9, 2018 1:53:15 PM : there is nowhere to insert, so we just wait
        LockSupport.park();
    }
    @Override
    public boolean offer(final E pE, final long pTimeout, final TimeUnit pUnit) throws InterruptedException
    {
        // san - Dec 9, 2018 3:46:13 PM : this queue is always full
        return false;
    }
    @Override
    public E take() throws InterruptedException
    {
        // san - Dec 9, 2018 1:54:20 PM : there is nothing to take, so we just wait
        LockSupport.park();

        return null;
    }
    @Override
    public E poll(final long pTimeout, final TimeUnit pUnit) throws InterruptedException
    {
        // san - Dec 10, 2018 10:27:27 PM : will wait
        LockSupport.parkNanos(pUnit.toNanos(pTimeout));

        return null;
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
        return 0;
    }
    @Override
    public int drainTo(final Collection<? super E> pC, final int pMaxElements)
    {
        return 0;
    }
    @Override
    public Iterator<E> iterator()
    {
        return IteratorUtils.emptyIterator();
    }
    @Override
    public int size()
    {
        return 0;
    }
}
