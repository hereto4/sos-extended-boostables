package game.faction.player;

import game.GAME;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.npc.FactionNPC;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public abstract class BoostCompound<T> implements ACTION {

	private final BoostSpecs bos;
	private final LIST<T> all;
	private final ArrayListGrower<Boo> boos = new ArrayListGrower<>();
	
	public BoostCompound(BoostSpecs bos, LIST<T> all) {
		this.bos = bos;
		this.all = all;
		
		BOOSTING.connecter(this);
		
	}

	protected abstract BoostSpecs bos(T t);
	
	protected abstract double getValue(T t);
	
	protected double get(Boostable bo, FactionNPC f, boolean isMul) {
		return ((FactionNPC)f).bonus.getD(bo);
	}
	
	public void clearChache() {
		for (Boo b : boos)
			b.cacheI = GAME.updateI()-100;
	}
	
	@Override
	public void exe() {
		
		KeyMap<LinkedList<Value>> map = new KeyMap<LinkedList<Value>>();
		
		for (T t : all) {
			
			BoostSpecs bos = bos(t);
			
			for (BoostSpec s : bos.all()) {
				
				String k = s.boostable.key + s.booster.isMul;
				if (!map.containsKey(k)) {
					map.put(k, new LinkedList<>());
				}
				map.get(k).add(new Value(t, s));
			}
		}
		
		for (LinkedList<Value> l : map.all()) {
			Boo b = new Boo(l);
			bos.push(b, b.bo);
			b.bo.fGlobal.add(b);
		}
		
		
		
	}
	
	private class Boo extends Booster {

		final Boostable bo;
		private int cacheI = -1000;
		private double cache;
		private final BValue value;
		private final double from;
		private final double to;
		
		public Boo(LIST<Value> all) {
			super(bos.info, all.get(0).bo.booster.isMul);
			this.bo = all.get(0).bo.boostable;
			
			double from = 1;
			double to = 1;
			if (isMul) {
				for (Value v : all) {
					double d = (v.bo.booster.getValue(1.0)-1);
					if (d < 0)
						from *= d;
					else
						to *= d;
				}
			}else {
				from = 0;
				to = 0;
				for (Value v : all) {
					double d = (v.bo.booster.getValue(1.0));
					if (d < 0)
						from += d;
					else
						to += d;
				}
			}
			
			this.from = from;
			this.to = to;
			
			this.value = new BValue.BValueFaction(bo) {
				
				@Override
				public double vGet(Player f) {
					int ci = GAME.updateI();
					if (ci != cacheI) {
						cacheI = GAME.updateI();
						cache = 0;
						if (isMul) {
							for (Value v : all) {
								cache += (v.bo.booster.getValue(BoostCompound.this.getValue(v.t))-1);
							}
							cache += 1;
							cache = Math.max(0, cache);
						}else {
							for (Value v : all) {
								cache += v.bo.booster.getValue(BoostCompound.this.getValue(v.t));
							}
						}
					}
					return cache;
				}

				@Override
				public double vGet(FactionNPC f) {
					return Boo.this.from + (Boo.this.to-Boo.this.from)* BoostCompound.this.get(bo, (FactionNPC) f, isMul);
				}
				
				
			};
			
		}
		
		@Override
		public double getValue(double input) {
			return input;
		}

		@Override
		protected double pget(BOOSTABLE_O o) {
			return o.boostableValue(value);
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

	}
	
	private class Value {
		
		public final T t;
		public final BoostSpec bo;
		
		Value(T t, BoostSpec bo){
			this.t = t;
			this.bo = bo;
		}
	}
	
}
