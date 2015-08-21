package com.jagex.link;

public final class Deque {

	private Linkable current;
	private Linkable tail = new Linkable();

	public Deque() {
		tail.setNext(tail);
		tail.setPrevious(tail);
	}

	public void clear() {
		if (tail.getNext() == tail) {
			return;
		}

		do {
			Linkable linkable = tail.getNext();
			if (linkable == tail) {
				return;
			}

			linkable.unlink();
		} while (true);
	}

	public Linkable getFront() {
		Linkable linkable = tail.getNext();
		if (linkable == tail) {
			current = null;
			return null;
		}

		current = linkable.getNext();
		return linkable;
	}

	public Linkable getNext() {
		Linkable linkable = current;
		if (linkable == tail) {
			current = null;
			return null;
		}

		current = linkable.getNext();
		return linkable;
	}

	public Linkable getPrevious() {
		Linkable linkable = current;
		if (linkable == tail) {
			current = null;
			return null;
		}

		current = linkable.getPrevious();
		return linkable;
	}

	public Linkable getTail() {
		Linkable linkable = tail.getPrevious();
		if (linkable == tail) {
			current = null;
			return null;
		}

		current = linkable.getPrevious();
		return linkable;
	}

	public Linkable popFront() {
		Linkable linkable = tail.getNext();
		if (linkable == tail) {
			return null;
		}

		linkable.unlink();
		return linkable;
	}

	public void pushBack(Linkable linkable) {
		if (linkable.getPrevious() != null) {
			linkable.unlink();
		}

		linkable.setPrevious(tail.getPrevious());
		linkable.setNext(tail);
		linkable.getPrevious().setNext(linkable);
		linkable.getNext().setPrevious(linkable);
	}

	public void pushFront(Linkable linkable) {
		if (linkable.getPrevious() != null) {
			linkable.unlink();
		}

		linkable.setPrevious(tail);
		linkable.setNext(tail.getNext());
		linkable.getPrevious().setNext(linkable);
		linkable.getNext().setPrevious(linkable);
	}

}