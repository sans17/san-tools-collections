package us.ligusan.base.tools.collections.nativ.api;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public interface IntMap<V>
{
    public static interface IntEntry<V> {
        int getKey();
        V getValue();
        void setValue(V value);
    }
    
    int size();
    boolean isEmpty();
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
    
    default V getOrDefault(final int pKey, final V pValue) {
        V lValue = get(pKey);
        return lValue != null || containsKey(pKey) ? lValue : pValue;
    }
    
    default V putIfAbsent(final int pKey, final V pValue) {
        V lValue = get(pKey);
        return lValue != null || containsKey(pKey) ? lValue : put(pKey, pValue);
    }
    
    default boolean remove(final int pKey, final V pValue) {
        V lValue = get(pKey);
        if((lValue != null || containsKey(pKey)) && Objects.equals(lValue, pValue)) {
            remove(pKey);
            return true;
        }
        return false;
    }
    
    default boolean replace(final int pKey, final V pExpectedValue, final V pNewValue) {
        V lValue = get(pKey);
        if((lValue != null || containsKey(pKey)) && Objects.equals(lValue, pExpectedValue)) {
            put(pKey, pNewValue);
            return true;
        }
        return false;
    }
    replace(K, V)
    computeIfAbsent(K, Function<? super K, ? extends V>)
    computeIfPresent(K, BiFunction<? super K, ? super V, ? extends V>)
    compute(K, BiFunction<? super K, ? super V, ? extends V>)
    merge(K, V, BiFunction<? super V, ? super V, ? extends V>)
}
