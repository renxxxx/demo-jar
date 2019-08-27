
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CacheMap<K, V> extends AbstractMap<K, V> {

	public static void main(String[] args) {
		CacheMap<Object, Object> cacheMap = new CacheMap<Object, Object>();
		String key = "qweqwe" + "17777889636";

		cacheMap.set(key, "1123");
		cacheMap.expire(key, 60 * 1000);

		key = "qweqwe" + "17777889636";
		System.out.println(cacheMap.get(key));
		cacheMap.remove(key);
		System.out.println(cacheMap.get(key));

	}

	private static CacheMap<Object, Object> defaultInstance;

	public static synchronized final CacheMap<Object, Object> getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new CacheMap<Object, Object>();
		}
		return defaultInstance;
	}

	private class CacheEntry implements Entry<K, V> {
		long insertTime;
		long updateTime;
		long expireSeconds;
		V value;
		K key;

		CacheEntry(K key, V value) {
			super();
			this.value = value;
			this.key = key;
			this.insertTime = System.currentTimeMillis();
			this.updateTime = System.currentTimeMillis();
			this.expireSeconds = -1;
		}

		CacheEntry(K key, V value, long expireSeconds) {
			super();
			this.value = value;
			this.key = key;
			this.insertTime = System.currentTimeMillis();
			this.updateTime = System.currentTimeMillis();
			this.expireSeconds = expireSeconds;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			return this.value = value;
		}
	}

	private class ClearThread extends Thread {
		ClearThread() {
			setName("clear cache thread");
		}

		public void run() {
			while (true) {
				try {
					long now = System.currentTimeMillis();
					Object[] keys = map.keySet().toArray();
					for (Object key : keys) {
						CacheEntry entry = map.get(key);
						if (entry.expireSeconds == -1)
							continue;
						if (now - entry.insertTime >= entry.expireSeconds * 1000) {
							synchronized (map) {
								map.remove(key);
							}
						}
					}
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Map<K, CacheEntry> map = new HashMap<K, CacheEntry>();

	public CacheMap() {
		new ClearThread().start();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entrySet = new HashSet<Map.Entry<K, V>>();
		Set<Entry<K, CacheEntry>> wrapEntrySet = map.entrySet();
		for (Entry<K, CacheEntry> entry : wrapEntrySet) {
			entrySet.add(entry.getValue());
		}
		return entrySet;
	}

	@Override
	public V get(Object key) {
		CacheEntry entry = map.get(key);
		return entry == null ? null : entry.value;
	}

	public int expire(Object key, long expireSeconds) {
		CacheEntry entry = map.get(key);
		if (entry == null)
			return 0;
		entry.expireSeconds = expireSeconds;
		return 1;
	}

	public V set(K key, V value) {
		CacheEntry entry = new CacheEntry(key, value);
		synchronized (map) {
			map.put(key, entry);
		}
		return value;
	}

	public int incr(K key) {
		CacheEntry cachedEntry = map.get(key);
		if (cachedEntry == null
				|| (cachedEntry.getValue() instanceof String
						&& !StringUtils.isNumeric(cachedEntry.getValue().toString()))
				|| !(cachedEntry.getValue() instanceof Integer))
			return 0;
		synchronized (key.toString()) {
			Integer result = Integer.parseInt(cachedEntry.getValue().toString()) + 1;
			if (cachedEntry.getValue() instanceof String) {
				cachedEntry.value = (V) (result + "");
			} else {
				cachedEntry.value = (V) result;
			}
			return 0;
		}
	}

	public int setnx(K key, V value) {
		CacheEntry cachedEntry = map.get(key);
		synchronized (key.toString()) {
			if (cachedEntry != null)
				return 0;
			else {
				CacheEntry entry = new CacheEntry(key, value);
				map.put(key, entry);
				return 1;
			}
		}
	}

	public V set(K key, V value, long expireSeconds) {
		CacheEntry entry = new CacheEntry(key, value, expireSeconds);
		synchronized (map) {
			map.put(key, entry);
		}
		return value;
	}
}
