package settlement.stats.colls;

import game.battle.Armies;
import game.battle.div.Div;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.TRAIT;
import init.type.TRAITS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatInitable;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.text.D;

public final class StatsTraits{

	private final ArrayList<StatTrait> all;
	public final INFO info;
	
	
	public StatsTraits(StatsInit init){

		D.t(this);
		info = new INFO(
				D.g("Trait"), 
				D.g("Traits"), 
				D.g("TraitDesc", "Traits can manifest themselves in individual subjects and in turn change properties of said person."), null);
		
		
		all = new ArrayList<StatTrait>(TRAITS.ALL().size());
		
		for (TRAIT t : TRAITS.ALL()) {
			new StatTrait(init, all, t);
		}
		
		init.onConstruct.add(new StatInitable() {
			
			@Override
			public void init(Induvidual h) {
				TRAITS.init(h);
			}
		});
		

	}
	
	public LIST<StatTrait> all() {
		return all;
	}
	
	public StatTrait stat(TRAIT t) {
		return all.get(t.index());
	}
	
	public static final class StatTrait implements DOUBLE_OE<Induvidual>{

		public final TRAIT trait;
		private final Data data;
		private final double di = 1.0/0x0F;
		
		StatTrait(StatsInit init, LISTE<StatTrait> all, TRAIT trait){
			all.add(this);
			this.trait = trait;
			this.data = new Data(init, "TRAIT_"+trait.key());
		}
		
		public int get(HCLASS c, Race r) {
			int ci = c == null ? HCLASSES.ALL().size() : c.index();
			int ri = r == null ? RACES.all().size() : r.index;
			return data.gdata[ci][ri];
		}
		
		public double getD(HCLASS c, Race r) {
			double p = STATS.POP().POP.data(c).get(r);
			double v = get(c, r)*di;
			if (p == 0)
				return CLAMP.d(v, 0, 1);
			return CLAMP.d(v/p, 0, 1);
		}
		
		public double getD(Div div) {
			return data.ddata[div.index()]*di;
		}

		@Override
		public double getD(Induvidual t) {
			return data.indu.get(t)*di;
		}
		
		@Override
		public DOUBLE_OE<Induvidual> setD(Induvidual t, double d) {
			data.removePrivate(t);
			data.indu.set(t, (int) (d*0x0F));
			data.addPrivate(t);
			return data.indu;
		}
		
		private static class Data implements Addable{
			
			private final INT_OE<Induvidual> indu;
			private int[][] gdata = new int[HCLASSES.ALL().size()+1][RACES.all().size()+1]; 
			private int[] ddata = new int[Armies.DIVISIONS];
			
			Data(StatsInit init, String key) {
				indu = init.count.new DataNibble("TRAIT_" + key);
				init.copier.add(new INT_OE<Induvidual>(){

					@Override
					public int get(Induvidual t) {
						return indu.get(t);
					}

					@Override
					public int min(Induvidual t) {
						return 0;
					}

					@Override
					public int max(Induvidual t) {
						return 0x0F;
					}

					@Override
					public void set(Induvidual t, int i) {
						removeH(t);
						indu.set(t, i);
						addH(t);
					}
					
				});
				init.addable.add(this);
			}

			@Override
			public void addPrivate(Induvidual i) {
				if (i.player()) {
					gdata[i.clas().index()][RACES.all().size()] += indu.get(i);
					gdata[HCLASSES.ALL().size()][RACES.all().size()] += indu.get(i);
				}
				gdata[i.clas().index()][i.race().index] += indu.get(i);
			}

			@Override
			public void removePrivate(Induvidual i) {
				if (i.player()) {
					gdata[i.clas().index()][RACES.all().size()] -= indu.get(i);
					gdata[HCLASSES.ALL().size()][RACES.all().size()] -= indu.get(i);
				}
				gdata[i.clas().index()][i.race().index] -= indu.get(i);
				
			}
			
			
		}



		
		
	}
	
}
