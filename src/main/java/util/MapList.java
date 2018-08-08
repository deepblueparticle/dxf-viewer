/**
 * 
 */
package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Key に対して Valueの配列を持つマップの実装
 * 
 * @author FUJIWARA Masayasu
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class MapList<K, V> {
	Map<K, List<V>> map = new HashMap<K, List<V>>();

	public void clear() {
		this.map.clear();
	}

	public boolean containsKey(K key) {
		return this.map.containsKey(key);
	}

	public Set<Map.Entry<K, List<V>>> entrySet() {
		return this.map.entrySet();
	}

	public List<V> get(K key) {
		return this.map.get(key);
	}

	public Collection<V> put(K key, List<V> values) {
		return this.map.put(key, values);
	}

	public void put(K key, V value) {
		List<V> list = this.map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			this.map.put(key, list);
		}
		list.add(value);
	}

	public void putAll(K key, Collection<? extends V> values) {
		List<V> list = this.map.get(key);
		if (list == null) {
			list = new ArrayList<V>(values);
			this.map.put(key, list);
		} else {
			list.addAll(values);
		}
	}

	public void putAll(MapList<K, V> maplist) {
		this.map.putAll(maplist.map);
	}

	public void remove(K key) {
		this.map.remove(key);
	}

	public Collection<List<V>> values() {
		return this.map.values();
	}
}
