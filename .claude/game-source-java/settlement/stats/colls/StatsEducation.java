package settlement.stats.colls;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.StatCollection;
import snake2d.util.rnd.RND;
import util.data.BOOLEANO.BOOLEAN_OE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.keymap.RMapInt;
import util.text.D;

public class StatsEducation extends StatCollection {

	private final INT_OE<Induvidual> data;
	private final INT_OE<Induvidual> dataType;
	private final STATData dEDUCATION;
	private final STATData dINDOCTRINATION;
	public final STAT EDUCATION;
	public final STAT INDOCTRINATION;
	private final RMapInt<Race> policyData = new RMapInt<Race>(RACES.map(), 0, 1);
	private final INFO policyInfo;

	private static CharSequence ¤¤pname = "¤Indoctrinate";
	private static CharSequence ¤¤pdesc = "¤Teach nothing much other than how glorious you, and your kingdom is. Will make your citizens more submissive";
	static CharSequence ¤¤name = "Education";
	static CharSequence ¤¤desc = "Stats regarding your education levels, and boosts surrounding it.";
	
	static {
		D.ts(StatsEducation.class);
	}
	
	public StatsEducation(StatsInit init) {
		super(init, "EDUCATION", ¤¤name, ¤¤desc);

		if (false) {
			//change education -> enlightenment.
		}
		
		init.savers.put("EDUCATION_POL", policyData);
		data = init.count.new DataByte("EDUCATION_DATA");
		dataType = init.count.new DataBit("EDUCATION_DATA_TYPE");
		
		dEDUCATION = new STATData("EDUCATION", "EDUCATION", init, make(0));
		EDUCATION = dEDUCATION;
		dINDOCTRINATION = new STATData("INDOCTRINATION", "EDUCATION_INOCTOR", init, make(1));
		INDOCTRINATION = dINDOCTRINATION;
		EDUCATION.info().icon = UI.icons().l.book.small;
		INDOCTRINATION.info().icon = UI.icons().l.work.small;
		policyInfo = new INFO(¤¤pname, ¤¤pdesc);
		init.copier.add(dEDUCATION.indu());
		init.copier.add(dINDOCTRINATION.indu());
	}

	public void educate(Induvidual i, double speed) {
		STAT target = EDUCATION;
		STAT other = INDOCTRINATION;
		if (policyIndoctor.is(i.race())) {
			target = INDOCTRINATION;
			other = EDUCATION;
		}
		
		double am = EDUCATION.indu().max(i) * speed;
		
		
		int a = (int) am;
		if (RND.rFloat() < am - a) {
			a++;
		}

		if (other.indu().get(i) > 0) {
			other.indu().inc(i, -a * 4);
		} else {
			target.indu().inc(i, a);
		}
	}
	
	public INT_O<Induvidual> TOTAL(){
		return data;
	}

	public boolean canEducateChild(Induvidual i) {
		return true;
	}
	
	public double total(Induvidual i) {
		return data.getD(i);
	}

	public BOOLEAN_OE<Race> policyIndoctor = new BOOLEAN_OE<Race>() {

		@Override
		public INFO info() {
			return policyInfo;
		}

		@Override
		public boolean is(Race f) {
			if (f == null) {
				for (Race r : RACES.all()) {
					if (policyData.get(r) == 1)
						return true;
				}
				return false;
			}
			return policyData.get(f) == 1;
		}

		@Override
		public BOOLEAN_OE<Race> set(Race f, boolean b) {
			if (f == null) {
				for (int ri = 0; ri < RACES.all().size(); ri++)
					policyData.set(RACES.all().get(ri), b ? 1 :0);
			} else
				policyData.set(f, b ? 1 :0);
			return this;
		};

	};

	private INT_OE<Induvidual> make(int c) {
		return new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return dataType.get(t) == c ? data.get(t) : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return data.max(t);
			}

			@Override
			public void set(Induvidual t, int i) {
				if (get(t) == i)
					return;
				(c == 0 ? dINDOCTRINATION : dEDUCATION).indu().set(t, 0);
				dataType.set(t, c);
				data.set(t, i);
			}
		};
	}
	
}
