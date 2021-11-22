package us.ligusan.base.tools.collections.nativ.api;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.stream.IntStream;

public interface IntCollection
{
    int size();

    default boolean isEmpty()
    {
        return size() <= 0;
    }

    default boolean contains(final int pIntToCheck)
    {
        for(PrimitiveIterator.OfInt lIterator = iterator(); lIterator.hasNext();)
            if(lIterator.nextInt() == pIntToCheck) return true;
        return false;
    }

    PrimitiveIterator.OfInt iterator();

    int[] toArray();

    default boolean add(final int pIntToAdd) {
        throw new UnsupportedOperationException();
    }

    boolean remove(int intToRemove);

    default boolean containsAll(final IntCollection pCollectionToCheck)
    {
        for(PrimitiveIterator.OfInt lIterator = iterator(); lIterator.hasNext();)
            if(!pCollectionToCheck.contains(lIterator.nextInt())) return false;
        return true;
    }

    default boolean addAll(final IntCollection pCollectionToAdd)
    {
        boolean ret = false;
        for(PrimitiveIterator.OfInt lIterator = pCollectionToAdd.iterator(); lIterator.hasNext();)
            if(add(lIterator.nextInt())) ret = true;
        return ret;
    }

    boolean removeAll(IntCollection collectionToRemove);

    default boolean removeIf(final IntPredicate pFilter)
    {
        boolean ret = false;
        for(PrimitiveIterator.OfInt lIterator = iterator(); lIterator.hasNext();)
            if(pFilter.test(lIterator.nextInt()))
            {
                lIterator.remove();
                ret = true;
            }
        return ret;
    }

    boolean retainAll(IntCollection collectionToRetain);

    void clear();

    Spliterator.OfInt spliterator();

    IntStream stream();
    IntStream parallelStream();
}
