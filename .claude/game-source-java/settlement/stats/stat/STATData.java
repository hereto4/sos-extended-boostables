package settlement.stats.stat;

import game.battle.div.Div;
import init.race.Race;
import init.type.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import util.data.DataO;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public class STATData extends STAT implements Addable {

	public final SettStatistics stats;
	private final INT_OE<Induvidual> indu;

	public STATData(String key, String dkey, StatsInit init, INT_OE<Induvidual> data) {
		this(key, dkey, init, data, null);
	}
	
	public STATData(String key, StatsInit init, DataO<Induvidual>.DataAbs data) {
		this(key, data.key, init, data, null);			
	}
	
	public STATData(String key, StatsInit init, DataO<Induvidual>.DataAbs data, StatInfo info) {
		this(key, data.key, init, data, info);			
	}
//	
//	public STATData(String key, StatsInit init, int dataBits, StatInfo info) {
//		this(key, init, init.count.create(init.coll.key+"_"+ key, (1<<dataBits)-1), info);
//		if (key == null)
//			throw new RuntimeException();
//	}
	
	public STATData(String key, String dkey, StatsInit init, INT_OE<Induvidual> data, StatInfo info) {
		super(key, init, info);
		
		stats = new SettStatistics(dkey, init, info) {

			@Override
			protected int popDivider(HCLASS c, Race r, int daysback) {
				return (int) pdivider(c, r, daysback);
			}

			@Override
			public int dataDivider() {
				return data.max(null);
			}
		};

		indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return data.get(t);
			}

			@Override
			public int min(Induvidual t) {
				return data.min(t);
			}

			@Override
			public int max(Induvidual t) {
				return data.max(t);
			}

			@Override
			public double getD(Induvidual t) {
				return data.getD(t);
			}
			
			@Override
			public void set(Induvidual t, int i) {
				removeH(t);
				data.set(t, i);
				addH(t);
			}

		};

		init.addable.add(this);
		
	}

	@Override
	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	@Override
	public int dataDivider() {
		return indu.max(null);
	}

	@Override
	public void addPrivate(Induvidual i) {
		stats.inc(i, indu.get(i));
	}

	@Override
	public void removePrivate(Induvidual i) {
		stats.inc(i, -indu.get(i));
	}

	@Override
	public INT_OE<Induvidual> indu() {
		return indu;
	}

	@Override
	public HISTORY_INT_OBJECT<Race> data() {
		return data(null);
	}

	@Override
	public HISTORY_INT_OBJECT<Race> data(HCLASS c) {
		return stats.data(c);
	}

	@Override
	public INT_O<Div> div() {
		return stats.div();
	}

	@Override
	public boolean hasIndu() {
		return key != null && key.length() > 0;
	}

}