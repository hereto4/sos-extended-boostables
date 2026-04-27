package snake2d.util.sets;

import java.io.Serializable;

public class Stack<T> implements Serializable{

	private static final long serialVersionUID = 1L;
	private final Object[] ints;
	private int current = -1;
	
	public Stack(int maxSize){
		ints = new Object[maxSize];
	}

	@SuppressWarnings("unchecked")
	public T pop(){
		if (current >= 0)
			return (T) ints[current--];
		throw new RuntimeException("I'm empty!");
	}
	
	public boolean push(T i){
		if (!isFull()){
			ints[++current] = i;
			return true;
		}
		return false;
	}
	
	public boolean isFull(){
		return current >= ints.length -1;
	}
	
	public boolean isEmpty(){
		return current == -1;
	}
	
	public Stack<T> clear() {
		current = -1;
		return this;
	}
	
	public int size() {
		return current + 1;
	}

}
