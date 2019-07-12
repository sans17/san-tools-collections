package us.ligusan.base.tools.collections.nativ.api;

public interface IntIterable
{
        IntIterator iterator();

        default void forEach(final IntConsumer action) {
            Objects.requireNonNull(action);
            for (T t : this) {
                action.accept(t);
            }
        }

        /**
         * Creates a {@link Spliterator} over the elements described by this
         * {@code Iterable}.
         *
         * @implSpec
         * The default implementation creates an
         * <em><a href="Spliterator.html#binding">early-binding</a></em>
         * spliterator from the iterable's {@code Iterator}.  The spliterator
         * inherits the <em>fail-fast</em> properties of the iterable's iterator.
         *
         * @implNote
         * The default implementation should usually be overridden.  The
         * spliterator returned by the default implementation has poor splitting
         * capabilities, is unsized, and does not report any spliterator
         * characteristics. Implementing classes can nearly always provide a
         * better implementation.
         *
         * @return a {@code Spliterator} over the elements described by this
         * {@code Iterable}.
         * @since 1.8
         */
        default Spliterator<T> spliterator() {
            return Spliterators.spliteratorUnknownSize(iterator(), 0);
        }
    }

}
