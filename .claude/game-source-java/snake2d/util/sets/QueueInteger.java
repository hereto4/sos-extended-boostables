package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public final class QueueInteger implements Serializable, SAVABLE {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int[] queue;
	private int front;
	private int rear = -1;
	private int currentSize = 0;

	public QueueInteger(int size) {
		this.queue = new int[size+1];
		clear();
	}

	public void push(int i) {
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

	public int peek() {
		if (!hasNext())
			throw new RuntimeException();

		int tmp = queue[front];
		return tmp;
	}
	
	public int poll() {
		if (!hasNext())
			throw new RuntimeException();

		int tmp = queue[front];
		currentSize--;
		front++;
		if (front == queue.length - 1) {
			front = 0;
		}
		return tmp;
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
	
	public boolean contains(int i) {
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

	@Override
	public void clear() {
		front = 0;
		rear = -1;
		currentSize = 0;
	}


	@Override
	public void save(FilePutter file) {

		file.is(queue);
		file.i(front);
		file.i(rear);
		file.i(currentSize);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.is(queue);
		front = file.i();
		rear = file.i();
		currentSize = file.i();
	}

}
