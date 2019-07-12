package us.ligusan.base.tools.collections.nativ.api;

public interface IntConsumer
{
    void accept(int data);

    default IntConsumer andThen(final IntConsumer after)
    {
        return (data) -> {
            accept(data);
            after.accept(data);
        };
    }
}
