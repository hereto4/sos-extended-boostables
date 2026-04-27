package settlement.stats.stat;

import game.battle.div.Div;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.Race;
import init.type.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public class STATInduOnly extends STAT {

	private final INT_OE<Induvidual> indu;
	private final static HISTORY_INT_OBJECT<Race> data = new HISTORY_INT_OBJECT<Race>() {

		@Override
		public int min(Race t) {
			return 0;
		}

		@Override
		public int max(Race t) {
			return 1;
		}

		@Override
		public double getD(Race t, int fromZero) {
			return 0;
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
			return 0;
		}
	
	};
	private final static INT_O<Div> div = new INT_O<Div>() {

		@Override
		public int get(Div t) {
			return 0;
		}

		@Override
		public int min(Div t) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int max(Div t) {
			return 1;
		}

	
	};

	public STATInduOnly(String key, StatsInit init, INT_OE<Induvidual> data) {
		super(key, init, null);
		this.indu = data;

	}

	@Override
	public int pdivider(HCLASS c, Race r, int daysback) {
		return 1;
	}

	@Override
	public int dataDivider() {
		return indu.max(null);
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
		return data;
	}

	@Override
	public INT_O<Div> div() {
		return div;
	}

	@Override
	public boolean hasIndu() {
		return key != null && key.length() > 0;
	}

}