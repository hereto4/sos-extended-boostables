package util.gui.table;

import snake2d.util.sets.Tree;
import util.data.GETTER;
import util.gui.misc.GText;

public abstract class GTableSorter<T> {
	
	private final Object[] pop;
	private final Tree<T> tree;
	
	private int popI;
	private int sortI;
	
	protected GTSort<T> sort;
	protected GTSort<T> sort2;
	protected GTFilter<T> filter;
	
	
	public GTableSorter(int max){
		pop = new Object[max];
		tree = new Tree<T>(max) {

			@Override
			protected boolean isGreaterThan(T current, T cmp) {
				if (sort == null) {
					if (sort2 != null)
						return sort2.cmp(current, cmp) <= 0;
					return true;
				}
				int c = sort.cmp(current, cmp);
				if (c == 0 && sort2 != null) {
					return sort2.cmp(current, cmp) <= 0;
				}
				return c <= 0;
			}
		};
	}
	
	public void sort() {
		sortI --;
		if (sortI > 0)
			return;
		sortI = 64;
		popI = 0;
		tree.clear();
		for (int i = 0; i < pop.length; i++) {
			T t = getUnsorted(i);
			if (t != null) {
				if (filter == null || filter.passes(t))
					tree.add(t);
			}
		}
		while (tree.hasMore()) {
			pop[popI++] = tree.pollGreatest();
		}
	}
	
	public void sortForced() {
		sortI = 0;
		sort();
	}
	
	protected abstract T getUnsorted(int index);
	
	public int size() {
		return popI;
	}
	
	@SuppressWarnings("unchecked")
	public T get(int index) {
		if (index >= 0 && index < popI) {
			return (T) pop[index];
		}
		return null;
	}
	
	public T get(GETTER<Integer> getter) {
		return get(getter.get());
	}
	
	public int getIndex(T t) {
		for (int i = 0; i < popI; i++)
			if (t == pop[i])
				return i;
		return 0;
	}
	
	public void setFilter(GTFilter<T> filter){
		this.filter = filter;
		sortI = 0;
		sort();
	}
	
	public void setSort(GTSort<T> sort) {
		this.sort = sort;
		sortI = 0;
		sort();
	}
	
	public GTSort<T> currentSort(){
		return sort;
	}
	
	public GTFilter<T> currentFilter(){
		return filter;
	}
	
	public static abstract class GTFilter<T> {
		
		public final CharSequence name;
		
		public GTFilter(CharSequence name){
			this.name = name;
		}
		
		public abstract boolean passes(T h);
	}
	
	public static abstract class GTSort<T> {
		public final CharSequence name;
		
		public GTSort(CharSequence name){
			this.name = name;
		}
		
		public abstract int cmp(T current, T cmp);
		
		public abstract void format(T h, GText text);
	}
}
