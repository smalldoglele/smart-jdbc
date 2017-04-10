package org.smart.jdbc;

import java.util.Map;

public interface ILoader<K, V> {
    
    void initMap();
    
    V load(K key);
    
    Map<K, V> loadMap();
}
