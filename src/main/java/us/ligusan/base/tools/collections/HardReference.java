package us.ligusan.base.tools.collections;

/**
 * Hard reference to an object. Not thread safe.
 * 
 * @author Alexander Prishchepov
 */
public class HardReference<T>
{
    private T referent;

    public HardReference(final T pReferent)
    {
        referent = pReferent;
    }

    public T get()
    {
        return referent;
    }
}
