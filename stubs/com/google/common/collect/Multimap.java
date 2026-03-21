package com.google.common.collect;
import java.util.*;
public interface Multimap<K,V> {
    boolean containsKey(Object key);
    boolean containsValue(Object value);
    boolean put(K key, Object value);
    Collection<Map.Entry<K,V>> entries();
}
