package com.jagex.link;

public final class Queue {

	private Cacheable current;
	private Cacheable head;

	public Queue() {
		head = new Cacheable();
		head.setNextCacheable(head);
		head.setPreviousCacheable(head);
	}

	public Cacheable getHead() {
		return head;
	}

	public Cacheable getNext() {
		Cacheable current = this.current;
		if (current == head) {
			this.current = null;
			return null;
		}

		this.current = current.getNextCacheable();
		return current;
	}

	public Cacheable peek() {
		Cacheable next = head.getNextCacheable();
		if (next == head) {
			current = null;
			return null;
		}

		current = next.getNextCacheable();
		return next;
	}

	public Cacheable pop() {
		Cacheable next = head.getNextCacheable();
		if (next == head) {
			return null;
		}

		next.unlinkCacheable();
		return next;
	}

	public void push(Cacheable node) {
		if (node.getPreviousCacheable() != null) {
			node.unlinkCacheable();
		}

		node.setPreviousCacheable(head.getPreviousCacheable());
		node.setNextCacheable(head);
		node.getPreviousCacheable().setNextCacheable(node);
		node.getNextCacheable().setPreviousCacheable(node);
	}

	public void setHead(Cacheable head) {
		this.head = head;
	}

	public int size() {
		int count = 0;
		for (Cacheable next = head.getNextCacheable(); next != head; next = next.getNextCacheable()) {
			count++;
		}

		return count;
	}

}