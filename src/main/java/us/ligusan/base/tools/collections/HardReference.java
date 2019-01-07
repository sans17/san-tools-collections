package us.ligusan.base.tools.collections;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("referent", referent).toString();
    }

    public T get()
    {
        return referent;
    }
}
