package game.boosting.superb;

import game.boosting.BSourceInfo;
import game.boosting.Booster;
import game.boosting.BoosterAbs;
import game.time.TIME;
import init.type.POP_CL;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O.DOUBLE_OE;

public abstract class SuperSpec<T extends SuperBoostableObj> extends BoosterAbs<T> {
	
	private final double from;
	private final double to;
	public final CharSequence desc;
	public boolean hidden = false;
	
	SuperSpec(SuperBoostable<T> self, BSourceInfo info, CharSequence desc, double to, boolean isMul) {
		super(info, isMul);
		this.to = to;
		this.desc = desc;
		if (isMul) {
			from = 1;
		}else {
			from = 0;
		}
		self.all.add(this);
	}
		
	@Override
	public double from() {
		return from;
	}

	@Override
	public double to() {
		return to;
	}

	@Override
	public double getValue(double input) {
		input = CLAMP.d(input, 0, 1);
		return from() + input*(to()-from());
	}
	
	public abstract double secondsRemaining(T bo);
	public abstract double increase(T bo);
	
	public double getModifier(T bo) {
		return 1;
	}
	
	public static abstract class SuperSpecImp<T extends SuperBoostableObj> extends SuperSpec<T> {

		public final int index;
		public final String key;
		
		public SuperSpecImp(SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul) {
			super(self, info, desc, to, isMul);
			
			while(self.map.containsKey(key)) {
				key += "0";
			}
			this.key = key;
			self.map.put(this.key, this);
			index = self.ups.add(this);
		}
		
		public final DOUBLE_OE<SuperBoostableObj> value = new DOUBLE_OE<SuperBoostableObj>() {

			@Override
			public double getD(SuperBoostableObj t) {
				return t.boostingData().values()[index];
			}

			@Override
			public DOUBLE_OE<SuperBoostableObj> setD(SuperBoostableObj t, double d) {
				t.boostingData().values()[index] = CLAMP.d(d, 0, 1);
				return this;
			}
		};
		
		public final DOUBLE_OE<SuperBoostableObj> time = new DOUBLE_OE<SuperBoostableObj>() {

			@Override
			public double getD(SuperBoostableObj t) {
				return t.boostingData().times()[index];
			}

			@Override
			public DOUBLE_OE<SuperBoostableObj> setD(SuperBoostableObj t, double d) {
				t.boostingData().times()[index] = d;
				return this;
			}
		};
		
		public final DOUBLE_OE<T> state = new DOUBLE_OE<T>() {

			@Override
			public double getD(T t) {
				return t.boostingData().states()[index];
			}

			@Override
			public DOUBLE_OE<T> setD(T t, double d) {
				t.boostingData().states()[index] = d;
				return this;
			}
		};
		
		public void toggle(T bo) {
			activate(bo, !activated(bo));
		}
		
		public abstract void update(T bo, double time);
		public abstract void activate(T bo, boolean active);
		public abstract boolean activated(T bo);
		
	}
	
	public static class Permanent<T extends SuperBoostableObj> extends SuperSpecImp<T> {
		
		public Permanent(SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul){
			super(self, key, info, desc, to, isMul);
		}

		@Override
		public void update(T bo, double time) {
			
		}

		@Override
		public void activate(T bo, boolean active) {
			value.setD(bo, active ? 1 : 0);
		}

		@Override
		public boolean activated(T bo) {
			return value.getD(bo) > 0;
		}

		@Override
		public double secondsRemaining(T bo) {
			return 0;
		}

		@Override
		public double increase(T bo) {
			return 0;
		}

		@Override
		protected double pget(T bo) {
			return value.getD(bo);
		}
		
	}
	
	static class Wrap<T extends SuperBoostableObj> extends SuperSpec<T> {
		
		private final Booster boo;
		
		public Wrap(Booster b, SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc){
			super(self, info, desc, b.to(), b.isMul);
			this.boo = b;
		}

		@Override
		public double get(T o) {
			return boo.get(POP_CL.clP());
		}
		
		@Override
		public double pget(T bo) {
			return 0;
		}

		@Override
		public double secondsRemaining(T bo) {
			return 0;
		}

		@Override
		public double increase(T bo) {
			return 0;
		}
		
	}
	
	public static class TimeLimit<T extends SuperBoostableObj> extends SuperSpecImp<T> {
		
		private final double seconds;
		
		public TimeLimit(double days, SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul){
			super(self, key, info, desc, to, isMul);
			this.seconds = days*TIME.secondsPerDay();
			
		}

		@Override
		public void update(T bo, double time) {
			if (this.time.getD(bo) > 0)
				this.time.incD(bo, -time);
			
		}

		@Override
		public double pget(T bo) {
			if (time.getD(bo) > 0)
				return 1;
			return 0;
		}

		@Override
		public void activate(T bo, boolean active) {
			time.setD(bo, active ? seconds : 0);
		}

		@Override
		public boolean activated(T bo) {
			return time.getD(bo) > 0;
		}

		@Override
		public double secondsRemaining(T bo) {
			return time.getD(bo);
		}

		@Override
		public double increase(T bo) {
			return 0;
		}
		
	}
	
	public static class Downer<T extends SuperBoostableObj>extends SuperSpecImp<T> {
		
		private final double decreaseTime;
		private final double durationDays;
		
		public Downer(double daysToDecrease, SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul, double durationDays){
			super(self, key, info, desc, to, isMul);
			if (daysToDecrease == 0)
				throw new RuntimeException();
			this.decreaseTime = daysToDecrease*TIME.secondsPerDayI();
			this.durationDays = durationDays;
		}

		@Override
		public void update(T bo, double time) {
			if (this.time.getD(bo) > 0) {
				this.time.incD(bo, -time);
				return;
			}
			value.incD(bo, -time*decreaseTime);
		}

		@Override
		public void activate(T bo, boolean active) {
			value.setD(bo, active ? 1.0 : 0);
			time.setD(bo, durationDays*TIME.secondsPerDay());
		}

		@Override
		public boolean activated(T bo) {
			return value.getD(bo) > 0;
		}

		@Override
		public double secondsRemaining(T bo) {
			return time.getD(bo);
		}

		@Override
		public double increase(T bo) {
			return  -decreaseTime*TIME.secondsPerDay();
		}

		@Override
		protected double pget(T o) {
			return value.getD(o);
		}
		
		
	}
	
	public static class Uper<T extends SuperBoostableObj> extends SuperSpecImp<T>  {
		
		private final double decreaseTime;
		private final double maxTime;
		
		public Uper(double daysToIncrease, SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul){
			this(daysToIncrease, -1, self, key, info, desc, to, isMul);
		}
		
		public Uper(double daysToDecrease, double maxDays, SuperBoostable<T> self, String key, BSourceInfo info, CharSequence desc, double to, boolean isMul){
			super(self, key, info, desc, to, isMul);
			if (daysToDecrease == 0)
				throw new RuntimeException();
			this.decreaseTime = daysToDecrease*TIME.secondsPerDayI();
			this.maxTime = maxDays*TIME.secondsPerDay();
		}

		@Override
		public void update(T bo, double time) {
			if (state.getD(bo) == 1) {
				if (maxTime >= 0) {
					double t = this.time.incD(bo, -time).getD(bo);
					if (t <= 0) {
						activate(bo, false);
						return;
					}
					
				}
				
				value.incD(bo, time*decreaseTime);
			}
			
		}

		@Override
		public double pget(T bo) {
			if (state.getD(bo) == 0)
				return 0;
			if (maxTime > 0 && time.getD(bo) <= 0)
				return 0;
			return value.getD(bo);
		}

		@Override
		public void activate(T bo, boolean active) {
			time.setD(bo, maxTime);
			value.setD(bo, 0);
			state.setD(bo, active ? 1 : 0);
			
		}

		@Override
		public boolean activated(T bo) {
			return state.getD(bo) == 1;
		}

		@Override
		public double secondsRemaining(T bo) {
			if (maxTime >= 0)
				return time.getD(bo);
			return 0;
		}

		@Override
		public double increase(T bo) {
			return decreaseTime*TIME.secondsPerDay();
		}
		
	}
	
}