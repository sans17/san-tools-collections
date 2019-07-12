package us.ligusan.base.tools.collections.nativ.api;

import java.util.PrimitiveIterator;

public interface IntSet extends IntCollection
{
    @Override
    default boolean removeAll(final IntCollection pCollectionToRemove)
    {
        boolean ret = false;
        // aprishchepov - Jul 12, 2019 4:56:15 PM : set contains only one - single removal should work
        for(PrimitiveIterator.OfInt lIterator = pCollectionToRemove.iterator(); lIterator.hasNext();)
            if(remove(lIterator.nextInt())) ret = true;
        return ret;
    }

    @Override
    default boolean retainAll(final IntCollection pCollectionToRetain)
    {
        boolean ret = false;
        // aprishchepov - Jul 12, 2019 4:55:22 PM : going over this collection checking if other contains the value
        for(PrimitiveIterator.OfInt lIterator = iterator(); lIterator.hasNext();)
            if(pCollectionToRetain.contains(lIterator.nextInt()))
            {
                lIterator.remove();
                ret = true;
            }
        return ret;
    }
}
