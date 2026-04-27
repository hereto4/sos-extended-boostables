package game.boosting;


import java.io.IOException;

import init.INIT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.keymap.RMAP;

public final class BOOSTING extends INIT.InitResource{

	public static final String KEY = "BOOST";
	
	private static final Map map = new Map();
	public static final LinkedList<ACTION> waiting = new LinkedList<>();
	private static final LinkedList<ACTION> connecters = new LinkedList<>();
//	private static final ArrayListGrower<Boostable> all = new ArrayListGrower<>();
	static boolean hasErrored = false;
//	private static BTypes types;	
	
//	static {
//		new GameDisposable() {
//			
//			@Override
//			protected void dispose() {
//				clear();
//			}
//		};
//	}
	
	static void clear() {
		
		map.clear();
		waiting.clear();
		connecters.clear();
		hasErrored = false;
//		all.clear();
//		types =  new BTypes();
	}

	public BOOSTING(INIT init) throws IOException {
		super(init);
		clear();
		BOOSTABLES.init();
		BoostableCat.init();
	}
	
	@Override
	protected void finishSetup() throws IOException {
		for (ACTION a : waiting)
			a.exe();
		for (ACTION a : connecters) {
			a.exe();
		}
		
//		ArrayList<Boostable> all = new ArrayList<>(BOOSTING.all.size());
//		all.add(BOOSTING.all);
//		
//		all.sort(new Comparator<Boostable>() {
//			
//			@Override
//			public int compare(Boostable o1, Boostable o2) {
//				int c = (""+o1.cat.name).compareTo(""+o2.cat.name);
//				if (c == 0) {
//					return (""+o1.name).compareTo(""+o2.name);
//				}
//				return c;
//			}
//		});
//		
//		BOOSTING.all.clear();
//		BOOSTING.all.add(all);

		waiting.clear();
		connecters.clear();
		super.finishSetup();
	}
	
	public static LIST<Boostable> ALL(){
		return map.all();
	}
	
//	public static BTypes TYPES() {
//		return types;
//	}
	
	public static void connecter(ACTION a) {
		connecters.add(a);
	}
	
	public static String available() {
		String s = "";
		for (String ss : map.map().keysSorted()) {
			s += ss;
			s += "  - " + map.map().get(ss).name; 
			s += System.lineSeparator();
		}
		return s;
	}
	
	public static Boostable push(String key, double baseValue, CharSequence name, CharSequence desc, SPRITE icon, BoostableCat cat) {
		return push(key, baseValue, name, desc, icon, cat, -10000000);
	}
	
	public static Boostable push(String key, double baseValue, CharSequence name, CharSequence desc, SPRITE icon, BoostableCat cat, double minValue) {
		if (key.charAt(0) == '_')
			key = key.substring(1);
		key = cat.prefix + key;
		
		Boostable b = new Boostable(map.all().size(), key, baseValue, name, desc, icon, cat, minValue);
		
		map.add(b);
		
		return b;
	}
	
	public static RMAP<Boostable> MAP(){
		return map;
	}
	



	static class Entry {
		
		public final ArrayListGrower<Boostable> all = new ArrayListGrower<>();
		public final boolean isMaster;
		
		Entry(Boostable b, boolean isMaster){
			all.add(b);
			this.isMaster = isMaster;
		}
		
	}
	


	
}
