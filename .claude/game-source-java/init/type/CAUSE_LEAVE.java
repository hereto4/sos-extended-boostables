package init.type;

import snake2d.util.sets.LISTE;
import util.info.INFO;
import util.keymap.MAPPED;

public final class CAUSE_LEAVE extends INFO implements MAPPED{
	
	public final String key;
	public final boolean death,natural,leavesCorpse;
	private final int index;
	public final int indexDeath;
	double defAgony;
		
	CAUSE_LEAVE(LISTE<CAUSE_LEAVE> all, LISTE<CAUSE_LEAVE> deaths, String key, CharSequence name, CharSequence names, CharSequence desc, boolean death, boolean natural, boolean leavesCorpse) {
		super(name, names, desc, null);
		this.key = key;
		this.death = death;
		this.natural = natural;
		this.leavesCorpse = leavesCorpse;
		index = all.add(this);
		if (death)
			indexDeath = deaths.add(this);
		else
			indexDeath = -1;
	}

	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String key() {
		return key;
	}
	
	public double defaultStanding() {
		return defAgony;
	}
	
}
