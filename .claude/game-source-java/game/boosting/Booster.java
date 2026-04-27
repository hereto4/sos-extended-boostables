package game.boosting;

public abstract class Booster extends BoosterAbs<BOOSTABLE_O>{
	
	public Booster(BSourceInfo info, boolean isMul){
		super(info, isMul);
	}
	
	public BoostSpec add(Boostable boostable, CharSequence append){
		BoostSpec b = new BoostSpec(this, boostable, append);
		boostable.addFactor(b);
		return b;
	}
	
	public BoostSpec add(Boostable boostable){
		return add(boostable, null);
	}

	
}
