package us.ligusan.base.tools.collections.nativ.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import us.ligusan.base.tools.collections.nativ.api.IntMap;
import us.ligusan.base.tools.collections.nativ.api.IntSet;

public class SortedIntMap<V> implements IntMap<V>
{
    private int[] keys;
    private ArrayList<V> values;
    
    
    @Override
    public int size()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean containsKey(int key)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(V value)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public V get(int key)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V put(int key, V value)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V remove(int key)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(IntMap<? extends V> mapToAdd)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IntSet keySet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<V> values()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<IntEntry<V>> entrySet()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
