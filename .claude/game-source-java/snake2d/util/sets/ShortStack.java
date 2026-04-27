package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class ShortStack implements Serializable{

	private static final long serialVersionUID = 1L;
	private final short[] ints;
	private int current = -1;
	
	public ShortStack(int maxSize){
		ints = new short[maxSize];
	}
	
	public final void save(FilePutter fp){
		fp.ss(ints);
		fp.writeInt(current);
	}
	
	public final void load(FileGetter fp) throws IOException {
		fp.ss(ints);
		current = fp.i();
	}

	public ShortStack fill() {
		if (current != -1)
			throw new RuntimeException("has elements");
		for (int i = ints.length-1; i >= 0; i--)
			push((short)i);
		return this;
	}
	
	public short pop(){
		if (current >= 0)
			return ints[current--];
		throw new RuntimeException("I'm empty!");
	}
	
	public boolean push(short i){
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
	
	public ShortStack clear() {
		current = -1;
		return this;
	}
	
	public int size() {
		return current + 1;
	}
	
	public int get(int index) {
		return ints[index];
	}
	
	public int capacity() {
		return ints.length;
	}

}
