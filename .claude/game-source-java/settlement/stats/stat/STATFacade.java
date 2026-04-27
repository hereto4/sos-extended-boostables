package settlement.stats.stat;

import game.battle.div.Div;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import snake2d.util.sets.ArrayList;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public abstract class STATFacade extends STAT {

	private final ArrayList<HISTORY_INT_OBJECT<Race>> datas = new ArrayList<HISTORY_INT_OBJECT<Race>>(
			HCLASSES.ALL().size() + 1);
	private final INT_O<Div> div;
	private final INT_OE<Induvidual> indu;

	public STATFacade(String key, StatsInit init) {
		this(key, init, null, null);
	}
	
	public STATFacade(String key, StatsInit init, StatInfo info) {
		this(key, init, null, info);
	}
	
	public STATFacade(String key, StatsInit init, INT_OE<Induvidual> indu) {
		this(key, init, indu, null);
	}

	public STATFacade(String key, StatsInit init, INT_OE<Induvidual> indu, StatInfo info) {
		super(key, init, info);
		for (HCLASS c : HCLASSES.ALL()) {

			datas.add(new HISTORY_INT_OBJECT<Race>() {

				@Override
				public int min(Race t) {
					return 0;
				}

				@Override
				public int max(Race t) {
					return dataDivider() * pdivider(c, t, 0);
				}

				@Override
				public double getD(Race t, int fromZero) {

					return getDD(c, t, fromZero);
				}

				@Override
				public TIMECYCLE time() {
					return TIME.days();
				}

				@Override
				public int historyRecords() {
					return STATS.DAYS_SAVED;
				}

				@Override
				public int get(Race t, int fromZero) {
					double d = dataDivider() * pdivider(c, t, fromZero);
					return (int) (getDD(c, t, fromZero) * d);
				}

			});
		}
		datas.add(new HISTORY_INT_OBJECT<Race>() {

			@Override
			public int min(Race t) {
				return 0;
			}

			@Override
			public int max(Race t) {
				return dataDivider() * pdivider(null, t, 0);
			}

			@Override
			public double getD(Race t, int fromZero) {
				double am = 0;
				for (int hi = 0; hi < HCLASSES.ALL().size(); hi++) {
					HCLASS cl = HCLASSES.ALL().get(hi);
					if (cl.player) {
						am += STATS.POP().POP.data(cl).get(null, fromZero)*getDD(cl, null, fromZero);
					}
				}
				double pop = STATS.POP().POP.data().get(null, fromZero);
				if (pop == 0)
					return am > 0 ? 1 : 0;
				return am/pop;
			}

			@Override
			public TIMECYCLE time() {
				return TIME.days();
			}

			@Override
			public int historyRecords() {
				return STATS.DAYS_SAVED;
			}

			@Override
			public int get(Race t, int fromZero) {
				double d = dataDivider() * pdivider(null, t, fromZero);
				return (int) (getD(t, fromZero) * d);
			}

		});

		div = new INT_O<Div>() {

			@Override
			public int get(Div t) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int min(Div t) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int max(Div t) {
				// TODO Auto-generated method stub
				return 0;
			}

		};
		if (indu == null)
			indu = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return (int) (64 * getDD(t.clas(), t.race(), 0));
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 64;
				}

				@Override
				public void set(Induvidual t, int i) {

				}

			};
		this.indu = indu;
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
		if (c == null)
			return datas.get(datas.size() - 1);
		return datas.get(c.index());
	}

	@Override
	public INT_O<Div> div() {
		return div;
	}

	protected abstract double getDD(HCLASS s, Race r, int daysBack);

	@Override
	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	@Override
	public int dataDivider() {
		return 1;
	}

}