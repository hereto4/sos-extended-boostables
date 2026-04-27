package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

/**
 * I got pissed that java doesn't have a inheritance structure that allows for
 * unmodifiable collections, so I made my own arraylist. this list is fixed in size.
 * the order of the elemets put in will not be the order of the content. It is not threadable.
 * A neat thing is that you can remove/add while iterating. It is serilizable.
 * @author mail__000
 *
 * @param <E>
 */
public class ArrayListShort implements Serializable, SAVABLE{

	
	private static final long serialVersionUID = 1L;
	private final short[] es;
	private final int size;
	private int last = 0;
	
	/**
	 * 
	 * @param size the fixed size of the list. Connot be changed.
	 */
	public ArrayListShort(int size){
	
		this.size = size;
		es = new short[size];
	}
	
	public int get(int index) {
		if (index < this.last)
			return es[index];
		return -1;
	}
	
	/**
	 * 
	 * @param e
	 * @return index if successful, -1 if not (set full)
	 */
	public int add(int e){
		if (!hasRoom())
			throw new RuntimeException("I'm full!");
		es[last] = (short) e;
		last++;
		return last -1;
	}
	
	public void set(int e, int index){
		if (index >= last)
			throw new RuntimeException();
		es[index] = (short) e;
	}

	/**
	 * removes and messes up the order.
	 * @param i
	 * @return if success
	 */
	public int remove(int index){
		
		if (index >= last)
			throw new RuntimeException();
		
		int res = es[index];
		
		if (index == last-1){
			es[last-1] = -1;
			last --;
		}else {
			es[index] = es[last-1];
			es[last-1] = -1;
			last --;
		}
		return res;
	}
	
	public void removeShort(short s) {
		for (int i = 0; i < size(); i++) {
			if (get(i) == s) {
				remove(i);
				return;
			}
		}
		throw new RuntimeException();
	}
	
	public int remainingSlots() {
		return size-last;
	}

	public boolean hasRoom() {
		return remainingSlots() > 0;
	}
	
	@Override
	public void clear(){
		last = 0;
	}
	
	public int size() {
		return last;
	}
	
	public int max(){
		return size;
	}

	public boolean isEmpty() {
		return last <= 0;
	}

	@Override
	public void save(FilePutter file) {
		file.ss(es);
		file.i(last);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ss(es);
		last = file.i();
	}

	public void reverse() {
		int s = size()/2;
		for (int i = 0; i < s; i++) {
			short o = es[i];
			es[i] = es[size()-1-i];
			es[size()-1-i] = o;
		}
	}

	public boolean contains(short s) {
		for (int i = 0; i < size(); i++) {
			if (get(i) == s) {
				return true;
			}
		}
		return false;
	}
	
}
