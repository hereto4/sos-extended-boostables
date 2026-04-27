package snake2d.util.misc;

public class Usable<T> {

	private final T t;
	private Object user;
	
	public Usable(T t){
		this.t = t;
	}
	
	public T use(Object user) {
		if (this.user != null)
			throw new RuntimeException("In use by: " + this.user);
		this.user = user;
		return t;
	}
	
	public void done() {
		user = null;
	}
	
}
