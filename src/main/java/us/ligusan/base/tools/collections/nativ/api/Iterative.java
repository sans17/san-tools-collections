package us.ligusan.base.tools.collections.nativ.api;

public interface Iterative
{
    default boolean hasNext()
    {
        return false;
    }

    default void remove()
    {
        throw new UnsupportedOperationException();
    }
}
