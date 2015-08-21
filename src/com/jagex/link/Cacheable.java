package com.jagex.link;

public class Cacheable extends Linkable {

	// Class30_Sub2
	private Cacheable nextCacheable;
	private Cacheable previousCacheable;

	public Cacheable getNextCacheable() {
		return nextCacheable;
	}

	public Cacheable getPreviousCacheable() {
		return previousCacheable;
	}

	public void setNextCacheable(Cacheable nextCacheable) {
		this.nextCacheable = nextCacheable;
	}

	public void setPreviousCacheable(Cacheable previousCacheable) {
		this.previousCacheable = previousCacheable;
	}

	public void unlinkCacheable() {
		if (previousCacheable == null) {
			return;
		}

		previousCacheable.nextCacheable = nextCacheable;
		nextCacheable.previousCacheable = previousCacheable;
		nextCacheable = null;
		previousCacheable = null;
	}

}