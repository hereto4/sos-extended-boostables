package init.value;

import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.keymap.MAPPED;
import util.keymap.RMAP;

public abstract class COMPARATOR implements MAPPED{

	private static LinkedList<COMPARATOR> all = new LinkedList<>();
	
	public static final COMPARATOR LESS = new COMPARATOR("LESS", "<") {

		@Override
		public boolean passes(double a, double b) {
			return a < b;
		}

		@Override
		public double progress(double a, double b) {
			return b/a;
		}


		
	};
	public static final COMPARATOR GREATER = new COMPARATOR("GREATER", ">") {

		@Override
		public boolean passes(double a, double b) {
			return a > b;
		}

		@Override
		public double progress(double a, double b) {
			return a/b;
		}
		
	};
	public static final COMPARATOR GREATERE = new COMPARATOR("GREATERE", ">=") {

		@Override
		public boolean passes(double a, double b) {
			return a >= b;
		}
		
		@Override
		public double progress(double a, double b) {
			return a/b;
		}
		
	};
	public static final COMPARATOR EQUAL = new COMPARATOR("EQUAL", "=") {

		@Override
		public boolean passes(double a, double b) {
			return a == b;
		}
		
		@Override
		public double progress(double a, double b) {
			return 1.0-Math.abs(a-b);
		}
		
	};
	public static final COMPARATOR NEQUAL = new COMPARATOR("NEQUAL", "!=") {

		@Override
		public boolean passes(double a, double b) {
			return a != b;
		}
		
		@Override
		public double progress(double a, double b) {
			return a == b ? 0 : 1;
		}
		
	};
	
	public static final RMAP<COMPARATOR> map = new RMAP<COMPARATOR>("COMPARATOR", all);
	
	public final String KEY;
	public final String rep;
	private final int index;
	
	private COMPARATOR(String key, String rep) {
		this.rep = rep;
		this.KEY = key;
		this.index = all.add(this);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public static LIST<COMPARATOR> ALL(){
		return all;
	}
	
	public abstract boolean passes(double a, double b);

	public abstract double progress(double a, double b);
	
	@Override
	public String key() {
		return KEY;
	}
	
}
