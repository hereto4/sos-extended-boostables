package game.faction.royalty.opinion;

import game.boosting.BSourceInfo;
import game.boosting.superb.SuperSpec.SuperSpecImp;
import game.faction.royalty.Royalty;
import snake2d.util.sprite.SPRITE;

abstract class ROpper extends SuperSpecImp<Royalty> {
	
	public ROpper(String key, CharSequence name, CharSequence desc, SPRITE icon,
			double to, boolean isMul) {
		super(ROPINIONS.BOOST(), key, new BSourceInfo(name, desc, null, icon), desc, to, isMul);
	}
	
	@Override
	public double pget(Royalty bo) {
		return value.getD(bo)*getModifier((Royalty)bo);
	}
	
	@Override
	public final double get(Royalty o) {
		return super.get(o);
	}
	
	@Override
	public void update(Royalty bo, double time) {
		
		value.incD(bo, time*increase((Royalty)bo));
		
	}
	
	@Override
	public double increase(Royalty roy) {
		return 0;
	}
	
	@Override
	public void activate(Royalty bo, boolean active) {
		
	}

	@Override
	public boolean activated(Royalty bo) {
		return get(bo) > 0;
	}
	
	@Override
	public double secondsRemaining(Royalty bo) {
		return 0;
	}
	
	static class ROpperDown extends ROpper {

		private final double dc;
		
		public ROpperDown(String key,  CharSequence name, CharSequence desc, SPRITE icon,
				double to, boolean isMul, double downSpeed) {
			super(key, name, desc, icon, to, isMul);
			dc = 1.0/downSpeed;
		}

		@Override
		public double increase(Royalty roy) {
			return -dc;
		}		
		
	}


	
}
