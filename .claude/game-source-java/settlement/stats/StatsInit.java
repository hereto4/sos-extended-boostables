package settlement.stats;

import init.paths.PATH;
import init.paths.PATHS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.stat.DataStat;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import util.data.DataO;
import util.data.INT_O.INT_OE;

public final class StatsInit {

	public final PATH pd = PATHS.INIT().getFolder("stats");
	public final PATH pt = PATHS.TEXT().getFolder("stats");

	public final KeyMap<SAVABLE> savers = new KeyMap<SAVABLE>();
	
	public final LinkedList<StatUpdatableI> updatable = new LinkedList<>();
	
	public final LinkedList<StatDisposable> disposable = new LinkedList<>();
	public final LinkedList<StatCollection> holders = new LinkedList<>();
	public final LinkedList<StatUpdatable> upers = new LinkedList<>();
	public final ArrayListGrower<Addable> addable = new ArrayListGrower<>();
	
	public final LinkedList<StatInitable> onArrival = new LinkedList<>();
	public final LinkedList<STAT> onArrivalStats = new LinkedList<STAT>();
	public final LinkedList<ACTION_O<Induvidual>> onArrivalActions = new LinkedList<ACTION_O<Induvidual>>();
	public final LinkedList<StatInitable> onConstruct = new LinkedList<>();
	public final LinkedList<INT_OE<Induvidual>> copier = new LinkedList<>();
	
	
	public final LinkedList<STAT> stats = new LinkedList<STAT>();
	
	public final LinkedList<DataStat> datas = new LinkedList<DataStat>();
	
	public final KeyMap<StatCollection> collMap = new KeyMap<>();
	public final KeyMap<STAT> statMap = new KeyMap<>();
	
	

	
	public StatCollection coll;

	
	public final DataO<Induvidual> count = new DataO<Induvidual>("STATS") {
		@Override
		protected long[] data(Induvidual t) {
			return t.data;
		}
	
	};
	
	
	public final Json dText = new Json(PATHS.TEXT().getFolder("stats").get("NAMES"));

	StatsInit() {
		
	}
	
	
	public void init(String key, StatCollection collection) {
		coll = collection;
		collMap.put(key, collection);
	}
	
	
	public interface Addable {
		default void addH(Induvidual i) {
			if (i.added())
				addPrivate(i);
		}
		default void removeH(Induvidual i) {
			if (i.added())
				removePrivate(i);
		}
		
		void addPrivate(Induvidual i);
		void removePrivate(Induvidual i);
	}
	
//	public interface Pushable {
//		void pushday();
//	}
	
	public interface StatUpdatableI {
		void update16(Humanoid h, int updateR, boolean day, int updateI);
		
	}
	
	public interface StatInitable {
		void init(Induvidual h);
	}
	
	public interface StatDisposable {
		void dispose(Humanoid h);
	}
	
	public interface StatUpdatable {
		void update(double ds);
	}
	
}
