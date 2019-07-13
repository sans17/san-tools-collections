package us.ligusan.base.tools.collections.nativ.api;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

public interface IntMap<V>
{
    public static interface IntEntry<V>
    {
        int getKey();

        V getValue();

        void setValue(V value);
    }
    public static interface IntBiConsumer<V>
    {
        void accept(int key, V value);

        default IntBiConsumer<V> andThen(final IntBiConsumer<? super V> pAfter)
        {
            return (key, value) -> {
                accept(key, value);
                pAfter.accept(key, value);
            };
        }
    }
    public static interface IntBiFunction<V>
    {
        V apply(int key, V value);

        default IntBiFunction<V> andThen(final IntBiFunction<V> pAfter)
        {
            return (key, value) -> pAfter.apply(key, apply(key, value));
        }
    }

    int size();

    default boolean isEmpty()
    {
        return size() <= 0;
    }

    boolean containsKey(int key);
    boolean containsValue(V value);

    V get(int key);
    V put(int key, V value);
    V remove(int key);

    void putAll(IntMap<? extends V> mapToAdd);

    void clear();

    IntSet keySet();
    Collection<V> values();
    Set<IntEntry<V>> entrySet();

    default V getOrDefault(final int pKey, final V pDefaultValue)
    {
        V lCurrentValue = get(pKey);
        return lCurrentValue != null || containsKey(pKey) ? lCurrentValue : pDefaultValue;
    }

    default void forEach(final IntBiConsumer<? super V> pAction)
    {
        for(IntEntry<V> lEntry : entrySet())
            pAction.accept(lEntry.getKey(), lEntry.getValue());
    }

    default void replaceAll(final IntBiFunction<V> pFunction)
    {
        for(IntEntry<V> lEntry : entrySet())
            lEntry.setValue(pFunction.apply(lEntry.getKey(), lEntry.getValue()));
    }

    default V putIfAbsent(final int pKey, final V pNewValue)
    {
        V lCurrentValue = get(pKey);
        return lCurrentValue != null || containsKey(pKey) ? lCurrentValue : put(pKey, pNewValue);
    }

    default boolean remove(final int pKey, final V pExpectedValue)
    {
        V lCurrentValue = get(pKey);
        if((lCurrentValue != null || containsKey(pKey)) && Objects.equals(lCurrentValue, pExpectedValue))
        {
            remove(pKey);
            return true;
        }
        return false;
    }

    default boolean replace(final int pKey, final V pExpectedValue, final V pNewValue)
    {
        V lCurrentValue = get(pKey);
        if((lCurrentValue != null || containsKey(pKey)) && Objects.equals(lCurrentValue, pExpectedValue))
        {
            put(pKey, pNewValue);
            return true;
        }
        return false;
    }

    default V replace(final int pKey, final V pNewValue)
    {
        return containsKey(pKey) ? put(pKey, pNewValue) : null;
    }

    default V computeIfAbsent(final int pKey, final IntFunction<? extends V> pMappingFunction)
    {
        V ret = get(pKey);
        if(ret == null && !containsKey(pKey)) put(pKey, ret = pMappingFunction.apply(pKey));
        return ret;
    }

    default V computeIfPresent(final int pKey, final IntBiFunction<V> pRemappingFunction)
    {
        V lCurrentValue = get(pKey);
        if(lCurrentValue != null || containsKey(pKey))
        {
            V ret = pRemappingFunction.apply(pKey, lCurrentValue);
            put(pKey, ret);
            return ret;
        }
        return null;
    }

    default V compute(final int pKey, final IntBiFunction<V> pRemappingFunction)
    {
        V ret = pRemappingFunction.apply(pKey, get(pKey));
        put(pKey, ret);
        return ret;
    }

    default V merge(final int pKey, final V pValue, final BiFunction<? super V, ? super V, ? extends V> pRemappingFunction)
    {
        V ret = pRemappingFunction.apply(get(pKey), pValue);
        put(pKey, ret);
        return ret;
    }
}
