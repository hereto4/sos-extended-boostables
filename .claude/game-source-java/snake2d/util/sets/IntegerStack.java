package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class IntegerStack implements Serializable{

	private static final long serialVersionUID = 1L;
	private final int[] ints;
	private int current = -1;
	
	public IntegerStack(int maxSize){
		ints = new int[maxSize];
	}
	
	public final void save(FilePutter fp){
		fp.is(ints);
		fp.writeInt(current);
	}
	
	public final void load(FileGetter fp) throws IOException {
		fp.is(ints);
		current = fp.i();
	}

	public IntegerStack fill() {
		if (current != -1)
			throw new RuntimeException("has elements");
		for (int i = ints.length-1; i >= 0; i--)
			push(i);
		return this;
	}
	
	public int pop(){
		if (current >= 0)
			return ints[current--];
		throw new RuntimeException("I'm empty!");
	}
	
	public boolean push(int i){
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
	
	public IntegerStack clear() {
		current = -1;
		return this;
	}
	
	public int size() {
		return current + 1;
	}
	
	public int get(int index) {
		return ints[index];
	}

}
