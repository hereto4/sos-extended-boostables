package snake2d.util.sets;

import java.io.Serializable;
import java.util.Iterator;

public class LinkedList<E> implements LISTE<E>, Serializable{

	private static final long serialVersionUID = 1L;
	private Node<E> first = null;
	private Node<E> last = null;
	private int size = -1;
	private final Itr iter = new Itr();
	
	public LinkedList(E e) {
		add(e);
	}
	
	public LinkedList(E[] e) {
		add(e);
	}
	
	public LinkedList() {

	}
	
	public <T extends E> LinkedList(Iterable<T> other){
		for (E e : other)
			add(e);
	}
	
	@Override
	public Iterator<E> iterator() {
		iter.init();
		return iter;
	}

	@Override
	public E get(int index) {
		
		if (index > size)
			return null;
		
		Node<E> n = first;
		
		while(index > 0 && n != null) {
			n = n.next;
			index--;
		}
		return n.element;
		
	}

	@Override
	public boolean hasRoom() {
		return true;
	}
	
	public LinkedList<E> clear(){
		first = null;
		size = -1;
		return this;
	}
	
	public E removeFirst(){
		E e = null;
		if (first != null){
			e = first.element;
			first = first.next;
			if (first == null)
				last = null;
			size--;
		}
		return e;
	}
	
	public E getFirst() {
		if (first != null)
			return first.element;
		return null;
	}
	
	@Override
	public boolean contains(int i) {
		return (i > size);
	}

	@Override
	public boolean contains(E object) {
		for (E e : this){
			if (e.equals(object))
				return true;
		}
		return false;
	}

	public void addFirst(E element){
		if (first == null){
			first = new Node<E>();
			first.element = element;
			last = first;
		}else{
			Node<E> n = new Node<E>();
			n.element = element;
			n.next = first;
			first = n;
		}
		size++;
	}
	
	@Override
	public int add(E element){
		if (first == null){
			first = new Node<E>();
			first.element = element;
			last = first;
		}else{
			
			
			Node<E> current = new Node<E>();
			current.element = element;
			last.next = current;
			last = current;
			
		}
		size++;
		return size;
	}
	
	@Override
	public int tryAdd(E e) {
		return add(e);
	}

	public boolean remove(E element){
		
		if (size < 0)
			return false;
		
		
		if (first.element == element){
			first = first.next;
			size --;
			if (first == null)
				last = null;
			return true;
		}
		
		Node<E> current = first;
		while (current.next != null){
			if (current.next.element == element){
				current.next = current.next.next;
				size --;
				if (current.next == null)
					last = current;
				return true;
			}
			current = current.next;
		}
		
		return false;
		
	}
	
	
	
	@Override
	public int size() {
		return size +1;
	}

	private class Itr implements Iterator<E>, Serializable{

		private static final long serialVersionUID = 1L;
		private Node<E> current;
		
		private void init(){
			current = first;
		}
		
		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public E next() {
			E e = current.element;
			current = current.next;
			return e;
		}
		
		
	}
	
	private static class Node<E> implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private E element;
		private Node<E> next;
		
	}

	@Override
	public boolean isEmpty() {
		return size == -1;
	}
	
	public void shiftLeft() {
		if (first != null && first.next != null) {
			Node<E> n = first;
			first = n.next;
			last.next = n;
			n.next = null;
			last = n;
		}
	}
	
	public void shiftRight() {
		if (last != null && first.next != null) {
			Node<E> current = first;
			while (current.next != last){
				current = current.next;
			}
			current.next = null;
			Node<E> n = last;
			last = current;
			n.next = first;
			first = n;
		}
	}
	
}
