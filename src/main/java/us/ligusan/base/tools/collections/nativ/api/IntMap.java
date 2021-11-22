package us.ligusan.base.tools.collections.nativ.api;

import java.util.Collection;
import java.util.Map;
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
    public static interface IntTriFunction<V>
    {
        V apply(int key, V valueOne, V valueTwo);
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

    default void putAll(final IntMap<? extends V> pMapToAdd)
    {
        for(IntEntry<? extends V> lEntry : pMapToAdd.entrySet())
            put(lEntry.getKey(), lEntry.getValue());
    }

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

    /**
     * Differences with {@link Map#computeIfAbsent(Object, java.util.function.Function)} method: <ol><li>null value considered as present</li><li>if mapping function returns null, null is inserted into map</li></ol>
     * 
     * @param pKey key with which the specified value is to be associated
     * @param pMappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key
     */
    default V computeIfAbsent(final int pKey, final IntFunction<? extends V> pMappingFunction)
    {
        V ret = get(pKey);
        if(ret == null && !containsKey(pKey)) put(pKey, ret = pMappingFunction.apply(pKey));
        return ret;
    }

    /**
     * Differences with {@link Map#computeIfPresent(Object, BiFunction)} method: <ol><li>null value considered as present</li><li>if remapping function returns null, null is inserted into map</li></ol>
     * 
     * @param pKey key with which the specified value is to be associated
     * @param pRemappingFunction the function to compute a value
     * @return the new value associated with the specified key
     */
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

    /**
     * Difference with {@link Map#compute(Object, BiFunction)} method: this one does not remove the key
     * 
     * @param pKey key with which the specified value is to be associated
     * @param pRemappingFunction the function to compute a value
     * @return the new value associated with the specified key
     */
    default V compute(final int pKey, final IntBiFunction<V> pRemappingFunction)
    {
        V ret = pRemappingFunction.apply(pKey, get(pKey));
        put(pKey, ret);
        return ret;
    }

    default V merge(final int pKey, final V pValue, final IntTriFunction<V> pRemappingFunction)
    {
        V ret = pRemappingFunction.apply(pKey, get(pKey), pValue);
        put(pKey, ret);
        return ret;
    }
}
