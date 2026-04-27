package snake2d.util.sets;

import java.util.Iterator;

public final class Queue<T> implements Iterable<T>{

	private final Object[] queue;
	private int front;
	private int rear = -1;
	private int currentSize = 0;

	public Queue(int size) {
		this.queue = new Object[size+1];
		clear();
	}

	public void push(T i) {
		if (!hasRoom())
			throw new RuntimeException();
		rear++;
		if (rear == queue.length - 1) {
			rear = 0;
		}
		queue[rear] = i;
		currentSize++;
	}

	public boolean hasNext() {
		return currentSize > 0 && front >= 0;
	}

	@SuppressWarnings("unchecked")
	public T peek() {
		if (!hasNext())
			throw new RuntimeException();

		return (T) queue[front];
	}
	
	@SuppressWarnings("unchecked")
	public T poll() {
		if (!hasNext())
			throw new RuntimeException();

		Object tmp = queue[front];
		currentSize--;
		front++;
		if (front == queue.length - 1) {
			front = 0;
		}
		return (T) tmp;
	}

	public boolean hasRoom() {
		return currentSize < queue.length-1;
	}

	public boolean isFull() {
		return remaining() == 0;
	}

	public int remaining() {
		return capacity() - size();
	}

	public int capacity() {
		return queue.length-1;
	}

	public int size() {
		return currentSize;
	}
	
	public boolean contains(T i) {
		int f = front;
		int c = currentSize;
		while(c > 0) {
			if (queue[f] == i)
				return true;
			c--;
			f++;
			if (f == queue.length - 1) {
				f = 0;
			}
		}
		return false;
	}

	public void clear() {
		front = 0;
		rear = -1;
		currentSize = 0;
	}

	@Override
	public Iterator<T> iterator() {
		iter.ii = 0;
		iter.f = front;
		return iter;
	}
	
	private final Iter iter = new Iter();
	
	private class Iter implements Iterator<T> {

		int ii = 0;
		int f = 0;
		
		@Override
		public boolean hasNext() {
			return ii < currentSize && f >= 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {

			Object tmp = queue[f];
			ii++;
			f++;
			if (f == queue.length - 1) {
				f = 0;
			}
			return (T) tmp;
		}
		
		
	}

}
