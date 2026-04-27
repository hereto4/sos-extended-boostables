package snake2d.util.sets;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class ArrayListIntegerResize implements SAVABLE{

	private int[] es;
	private final int maxSize;
	private final int minSize;
	private int last = 0;
	
	/**
	 * 
	 * @param maxSize the fixed size of the list. Connot be changed.
	 */
	public ArrayListIntegerResize(int minSize, int maxSize){
		this.maxSize = maxSize;
		this.minSize = minSize;
		es = new int[minSize];
	}
	
	private void increase(){
		if (last == es.length-1 && es.length != maxSize){
			int size = es.length*2;
			if (size > maxSize)
				size = maxSize;
			int[] esNew = new int[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	private void decrease(){
		if (es.length != minSize && last < es.length/2){
			int size = es.length/2;
			if (size < minSize)
				size = minSize;
			int[] esNew = new int[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	public int get(int index) {
		if (index < this.last)
			return es[index];
		throw new NoSuchElementException("no element at index: " + index);
	}


	public int add(int e){
		increase();
		if (!hasRoom())
			return -1;
		es[last] = e;
		last++;
		return last -1;
	}

	/**
	 * removes and messes up the order.
	 * @param i
	 * @return if success
	 */
	public boolean remove(int i){
		
		if (i >= last)
			return false;
		
		if (i == last-1){
			last --;
			decrease();
			return true;
		}
		
		es[i] = es[last-1];
		last --;
		decrease();
		return true;
	}
	
	public int remainingSlots() {
		return maxSize-last;
	}

	public boolean hasRoom() {
		return remainingSlots() > 0;
	}
	
	/**
	 * Start anew!
	 */
	@Override
	public void clear(){
		if (es.length != minSize)
			es = new int[minSize];
		last = 0;
	}
	
	public int size() {
		return last;
	}
	
	public int max(){
		return maxSize;
	}
	
	public boolean isEmpty() {
		return last == 0;
	}
	
	
	public void trim(){
		int[] no = new int[last];
		for (int i = 0; i < last; i++)
			no[i] = es[i];
		es = no;
	}

	public int getLast() {
		return get(last-1);
	}
	
	public void sort(){
		if (last <= 1)
			return;
		
		Arrays.sort(es, 0, last);
	}
	
	public void swap(int indexA, int indexB) {
		if (indexA < 0 || indexB >= size() || indexB < 0 || indexB >= size())
			throw new RuntimeException();
		int a = es[indexA];
		es[indexA] = es[indexB];
		es[indexB] = a;
	}

	@Override
	public void save(FilePutter file) {
		file.i(last);
		for (int i = 0; i < size(); i++)
			file.i(get(i));
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		clear();
		int m = file.i();
		for (int i = 0; i < m; i++)
			add(file.i());
		
	}
	

	
}
