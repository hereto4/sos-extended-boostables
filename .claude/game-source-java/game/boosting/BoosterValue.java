package game.boosting;

import snake2d.util.misc.CLAMP;

public class BoosterValue extends Booster {

	private final double from;
	private final double to;
	private final BValue value;

	public BoosterValue(BValue v, BSourceInfo info, double from, double to, boolean isMul) {
		super(info, isMul);
		this.from = from;
		this.to = to;
		this.value = v;
	}
	
	public BoosterValue(BValue v, BSourceInfo info, double to, boolean isMul) {
		super(info, isMul);
		this.value = v;
		this.to = to;
		
		if (isMul) {
			from = 1;
		}else {
			from = 0;
		}
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

	@Override
	protected double pget(BOOSTABLE_O o) {
		return o.boostableValue(value);
	}
	

}