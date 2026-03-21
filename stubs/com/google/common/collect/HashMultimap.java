package com.google.common.collect;
import java.util.*;
@SuppressWarnings("unchecked")
public class HashMultimap<K,V> implements Multimap<K,V> {
    private Map<K, Collection<V>> map = new HashMap<>();
    public static <K,V> HashMultimap<K,V> create() { return new HashMultimap<>(); }
    public boolean containsKey(Object key) { return map.containsKey(key); }
    public boolean containsValue(Object value) {
        for (Collection<V> c : map.values()) if (c.contains(value)) return true;
        return false;
    }
    public boolean put(K key, Object value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add((V)value);
        return true;
    }
    public Collection<Map.Entry<K,V>> entries() {
        Collection<Map.Entry<K,V>> result = new HashSet<>();
        for (Map.Entry<K, Collection<V>> e : map.entrySet())
            for (V v : e.getValue()) result.add(new AbstractMap.SimpleEntry<>(e.getKey(), v));
        return result;
    }
}
