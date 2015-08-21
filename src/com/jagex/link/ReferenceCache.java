package com.jagex.link;

/**
 * A least-recently used cache of references, backed by a {@link HashTable} and a {@link Queue}.
 */
public final class ReferenceCache {

	/**
	 * The capacity of this cache.
	 */
	private int capacity;
	
	/**
	 * The empty cacheable.
	 */
	private Cacheable empty = new Cacheable();
	
	/**
	 * The queue of references, used for LRU behaviour.
	 */
	private Queue references = new Queue();
	
	/**
	 * The HashTable backing this cache.
	 */
	private HashTable table = new HashTable(1024);
	
	/**
	 * The amount of unused slots in this cache.
	 */
	private int unused;

	/**
	 * Creates the ReferenceCache.
	 *
	 * @param capacity The capacity of this cache.
	 */
	public ReferenceCache(int capacity) {
		this.capacity = capacity;
		unused = capacity;
	}

	/**
	 * Clears the contents of this ReferenceCache.
	 */
	public void clear() {
		do {
			Cacheable front = references.pop();

			if (front != null) {
				front.unlink();
				front.unlinkCacheable();
			} else {
				unused = capacity;
				break;
			}
		} while (true);
	}

	/**
	 * Gets the {@link Cacheable} with the specified key.
	 * 
	 * @param key The key.
	 * @return The Cacheable.
	 */
	public Cacheable get(long key) {
		Cacheable node = (Cacheable) table.get(key);
		if (node != null) {
			references.push(node);
		}

		return node;
	}

	public void put(long key, Cacheable node) {
		if (unused == 0) {
			Cacheable front = references.pop();
			front.unlink();
			front.unlinkCacheable();

			if (front == empty) {
				front = references.pop();
				front.unlink();
				front.unlinkCacheable();
			}
		} else {
			unused--;
		}

		table.put(key, node);
		references.push(node);
	}

}