package us.ligusan.base.tools.collections.nativ.impl;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;

import us.ligusan.base.tools.collections.nativ.api.IntMap;
import us.ligusan.base.tools.collections.nativ.api.IntSet;

public class SortedIntMap<V> implements IntMap<V>
{
    private int[] keys;
    private ArrayList<V> values;
    
    public SortedIntMap()
    {
      values = new ArrayList<>();
    }
    
    @Override
    public int size()
    {
        return values.size();
    }

    protected int getIndex(final int pKey) {
        int lSize = size();
        // AlexP - Jun 25, 2019 4:38:25 PM : when current size is 0, insertion point is 0
        return lSize > 0 ? Arrays.binarySearch(keys, 0, lSize, pKey) : -1;
    }
    
    @Override
    public boolean containsKey(final int pKey)
    {
        return getIndex(pKey) >= 0;
    }

    @Override
    public boolean containsValue(final V pValue)
    {
        return values.contains(pValue);
    }

    @Override
    public V get(final int pKey)
    {
        int lIndex = getIndex(pKey);
        return lIndex < 0 ? null : values.get(lIndex);
    }

    protected int sizeIncrease(final int pCurrentSize) {
        // AlexP - Jun 25, 2019 5:30:02 PM : increase by 10? in ArrayList they use: oldCapacity + (oldCapacity >> 1)
        return pCurrentSize + 10;
    }
    
    @Override
    public V put(final int pKey, final V pValue)
    {
        int lIndex = getIndex(pKey);
        // AlexP - Jun 25, 2019 4:19:18 PM : insertion
        if(lIndex < 0)
        {
            // AlexP - Jun 25, 2019 5:55:33 PM : insertion index
            lIndex = -++lIndex;

            // AlexP - Jun 26, 2019 11:35:18 AM : where we copy to (might be the same as where we copy from)
            int[] lNewKeys = keys;
            int lLength = keys == null ? 0 : keys.length;
            int lSize = size();
            // AlexP - Jun 26, 2019 9:21:32 AM : need to increase keys size
            if(lLength <= lSize)
            {
                // AlexP - Jun 26, 2019 9:32:11 AM : assigning new keys
                lNewKeys = new int[sizeIncrease(lLength)];

                // AlexP - Jun 26, 2019 9:27:48 AM : #1 copy left side
                if(lIndex > 0) System.arraycopy(keys, 0, lNewKeys, 0, lIndex);
            }
            // AlexP - Jun 26, 2019 9:28:13 AM : #2 copy right side (in case it is the same array)
            if(lIndex < lSize) System.arraycopy(keys, lIndex, lNewKeys, lIndex + 1, lSize - lIndex);
            // AlexP - Jun 26, 2019 9:29:21 AM : #3 setting new key
            lNewKeys[lIndex] = pKey;

            // AlexP - Jul 18, 2019 11:40:17 AM : reset keys
            keys = lNewKeys;
            // AlexP - Jun 26, 2019 9:20:28 AM : adding value
            values.add(lIndex, pValue);

            // AlexP - Jul 18, 2019 11:44:44 AM : no old value - return null
            return null;
        }
        // AlexP - Jun 25, 2019 5:41:53 PM : update
        else return values.set(lIndex, pValue);
    }

    protected V removeByIndex(final int pIndex) {
        int lNewSize = size() - 1;
        // AlexP - Jun 25, 2019 4:16:23 PM : need to copy only if not last element
        if (pIndex < lNewSize)
          System.arraycopy(keys, pIndex + 1, keys, pIndex, lNewSize - pIndex);
        // AlexP - Jul 18, 2019 12:35:28 PM : remove and return
        return values.remove(pIndex);
    }
    
    @Override
    public V remove(final int pKey)
    {
        int lIndex = getIndex(pKey);
        // AlexP - Jul 18, 2019 1:49:57 PM : not found return null
        return lIndex < 0 ? null : removeByIndex(lIndex);
    }

    @Override
    public void clear()
    {
        values.clear();
    }

    @Override
    public IntSet keySet()
    {
        return new IntSet()
            {
                @Override
                public int[] toArray()
                {
                    int lSize = size();
                    int[] ret = new int[lSize];
                    if(lSize > 0) System.arraycopy(keys, 0, ret, 0, lSize);
                    return ret;
                }
                
                @Override
                public IntStream stream()
                {
                    return size() > 0 ? IntStream.of(keys) : IntStream.empty();
                }
                
                @Override
                public Spliterator.OfInt spliterator()
                {
                    return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.SIZED | Spliterator.NONNULL);
                }
                
                @Override
                public int size()
                {
                    return values.size();
                }
                
                @Override
                public boolean remove(final int pIntToRemove)
                {
                    int lIndex = getIndex(pIntToRemove);
                    boolean ret = lIndex >= 0;
                    if(ret) removeByIndex(lIndex);
                    return ret;
                }
                
                @Override
                public IntStream parallelStream()
                {
                    return stream().parallel();
                }
                
                /**
                 * Not fail fast implementation.
                 */
                @Override
                public OfInt iterator()
                {
                    return new OfInt()
                        {
                            private int index;

                            @Override
                            public boolean hasNext()
                            {
                                return index < size();
                            }

                            @Override
                            public int nextInt()
                            {
                                return keys[index++];
                            }

                            @Override
                            public void remove()
                            {
                                removeByIndex(index - 1);
                            }
                        };
                }
                
                @Override
                public void clear()
                {
                    SortedIntMap.this.clear();
                }
            };
    }

    @Override
    public Collection<V> values()
    {
        return new Collection<V>()
            {
                @Override
                public int size()
                {
                    return values.size();
                }

                @Override
                public boolean isEmpty()
                {
                    return values.isEmpty();
                }

                @Override
                public boolean contains(final Object pObjectToCheck)
                {
                    return values.contains(pObjectToCheck);
                }

                /**
                 * Not fail fast implementation.
                 */
                @Override
                public Iterator<V> iterator()
                {
                    return new Iterator<V>() {
                        private int index;

						@Override
						public boolean hasNext() {
                            return index < size();
						}

						@Override
						public V next() {
							return values.get(index++);
						}

						@Override
						public void remove() {
                            removeByIndex(index - 1);
						}
					};
                }

                @Override
                public Object[] toArray()
                {
                    return values.toArray();
                }

                @Override
                public <T> T[] toArray(final T[] pArray)
                {
                    return values.toArray(pArray);
                }

                @Override
                public boolean add(final V pValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean remove(final Object pObjectToRemove)
                {
                    int lIndex = values.indexOf(pObjectToRemove);
                    boolean ret = lIndex >= 0;
                    if(ret) removeByIndex(lIndex);
                    return ret;
                }

                @Override
                public boolean containsAll(final Collection<?> pCollectionToCheck)
                {
                    return values.containsAll(pCollectionToCheck);
                }

                @Override
                public boolean addAll(final Collection<? extends V> pCollectionToAdd)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(final Collection<?> pCollectionToRemove)
                {
                    boolean ret = false;
                    for(Object lObject : pCollectionToRemove)
                        if(remove(lObject)) ret = true;
                    return ret;
                }

                @Override
                public boolean retainAll(final Collection<?> pCollectionToRetain)
                {
                    boolean ret = false;
                    // AlexP - Jul 18, 2019 3:34:12 PM : going from the end in hope to minimize copying
                    for(int i = size() - 1; i >= 0; i--)
                        if(!pCollectionToRetain.contains(values.get(i)))
                        {
                            removeByIndex(i);
                            ret = true;
                        }
                    return ret;
                }

                @Override
                public void clear()
                {
                    values.clear();
                }
            };
    }

    @Override
    public Set<IntEntry<V>> entrySet()
    {
        return new AbstractSet<IntEntry<V>>()
            {
                @Override
                public int size()
                {
                    return values.size();
                }

                @Override
                public boolean isEmpty()
                {
                    return values.isEmpty();
                }

                @Override
                public boolean contains(final Object pObjectToCheck)
                {
                    if(pObjectToCheck instanceof IntEntry)
                    {
                        IntEntry<?> lIntEntry = (IntEntry)pObjectToCheck;
                        return Objects.equals(get(lIntEntry.getKey()), lIntEntry.getValue());
                    }
                    return false;
                }

                /**
                 * Not fail fast implementation.
                 */
                @Override
                public Iterator<IntEntry<V>> iterator()
                {
                    return new Iterator<IntEntry<V>>()
                        {
                            private int index;

                            @Override
                            public boolean hasNext()
                            {
                                return index < size();
                            }

                            @Override
                            public IntEntry<V> next()
                            {
                                int lIndex = index++;

                                return new IntEntry<V>()
                                    {
                                        @Override
                                        public int getKey()
                                        {
                                            return keys[lIndex];
                                        }

                                        @Override
                                        public V getValue()
                                        {
                                            return values.get(lIndex);
                                        }

                                        @Override
                                        public void setValue(final V pValue)
                                        {
                                            values.set(lIndex, pValue);
                                        }
                                    };
                            }

                            @Override
                            public void remove()
                            {
                                removeByIndex(index - 1);
                            }
                        };
                }

                @Override
                public boolean remove(final Object pObjectToRemove)
                {
                    if(pObjectToRemove instanceof IntEntry)
                    {
                        IntEntry<V> lIntEntry = (IntEntry)pObjectToRemove;
                        return SortedIntMap.this.remove(lIntEntry.getKey(), lIntEntry.getValue());
                    }
                    return false;
                }

                @Override
                public void clear()
                {
                    values.clear();
                }
            };
    }
}
