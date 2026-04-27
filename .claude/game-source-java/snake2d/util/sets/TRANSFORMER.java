package snake2d.util.sets;

public interface TRANSFORMER<F, T> {

	public T transform(F f);
	
	public default ArrayList<T> toArrayList(@SuppressWarnings("unchecked") F...fs) {
		ArrayList<T> res = new ArrayList<>(fs.length);
		for (F f : fs)
			res.add(transform(f));
		return res;
	}
	
}
