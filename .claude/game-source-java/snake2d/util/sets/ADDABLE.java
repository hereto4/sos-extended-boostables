package snake2d.util.sets;

public interface ADDABLE <E> {

	int add(E e);
	default void add(Iterable<E> es) {
		for (E e : es)
			add(e);
	}
	default void add(E[] es) {
		for (E e : es)
			add(e);
	}
	
	default E addReturn(E element){
		add(element);
		return element;
	}
	
	int tryAdd(E e);
	
	boolean hasRoom();
	
}
