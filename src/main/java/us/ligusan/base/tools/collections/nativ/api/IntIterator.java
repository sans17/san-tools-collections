package us.ligusan.base.tools.collections.nativ.api;

public interface IntIterator extends Iterative
{
    int next();

    default void forEachRemaining(final IntConsumer pAction)
    {
        while(hasNext())
            pAction.accept(next());
    }
}
