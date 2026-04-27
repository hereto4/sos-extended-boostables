package settlement.stats.disease;

import init.type.DISEASE;
import init.type.DISEASES;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.stat.STATData;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;

class Data {

	final STATData infected;
	final STATData incubating;
	private final INT_OE<Induvidual> index;
	public final INT_OE<Induvidual> count;
	private final INT_OE<Induvidual> status;
	final INT_OE<Induvidual> die;
	private int[][] active = new int[POP_CL.ALL().size()][DISEASES.all().size()];
	private final Addable adder;

	Data(StatsInit init) {
		
		infected = new SS(init, "INFECTED", "D_INFECTED", DiseaseStatus.ISICK);
		incubating = new SS(init, "INCUBATE", "D_INCUBATE", DiseaseStatus.INCUBATING);
		
		incubating.info().setMatters(false, true);
		
		index = init.count.new DataByte("DIS_INDEX");
		count = init.count.new DataNibble("DIS_COUNT");
		die = init.count.new DataBit("DIS_DIE");
		adder = new Addable() {

			@Override
			public void addPrivate(Induvidual i) {
				
				if (i.player() && get(i) != null && status(i).active) {
					active[POP_CL.clP(i).index][get(i).index()] ++;
					active[POP_CL.clP(i.clas()).index][get(i).index()] ++;
				}
			}

			@Override
			public void removePrivate(Induvidual i) {
				if (i.player() && get(i) != null && status(i).active) {
					
					active[POP_CL.clP(i).index][get(i).index()] --;
					active[POP_CL.clP(i.clas()).index][get(i).index()] --;
					
					active[POP_CL.clP(i).index][get(i).index()] &= Integer.MAX_VALUE;
					active[POP_CL.clP(i.clas()).index][get(i).index()] &= Integer.MAX_VALUE;
				}
			}
		};
		init.addable.add(adder);
		
		status = init.count.new DataNibble("DIST_STATUS");
		
	}
	
	private class SS extends STATData {
		
		SS(StatsInit init, String key, String dkey, DiseaseStatus status){
			super(key, key, init, new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				if (Data.this.get(t) != null && status(t) == status)
					return 1;
				return 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {

			}
		
		});
		}
		
	}
	
	public DISEASE get(Induvidual t) {
		int in = index.get(t)-1;
		if (in < 0)
			return null;
		return DISEASES.all().get(in);
	}
	
	public DiseaseStatus status(Induvidual t) {
		return DiseaseStatus.ALL.getC(status.get(t));
	}
	
	public int cases(POP_CL pop, DISEASE d) {
		return active[pop.index][d.index()];
	}
	

	public void set(Induvidual t, DISEASE d, DiseaseStatus status) {
		infected.removeH(t);
		incubating.removeH(t);
		adder.removeH(t);
		
		int i = 0;
		if (d != null) {
			i = d.index()+1;
			die.set(t, RND.rFloat()<d.fatalityRate ? 1 : 0);
			
		}else {
			status = DiseaseStatus.NONE;
		}
		
		index.set(t, i);
		this.status.set(t, status.ordinal());
		count.set(t, 0);
		
		infected.addH(t);
		incubating.addH(t);
		adder.addH(t);
	}
	
}
