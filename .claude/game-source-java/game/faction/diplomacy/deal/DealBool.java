package game.faction.diplomacy.deal;

import game.faction.diplomacy.DipStance;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.gui.misc.GBox;
import util.info.INFO;

public abstract class DealBool implements BOOLEAN_MUTABLE {
	
	public final INFO info;
	public final SPRITE icon;

	private boolean toggled;
	
	DealBool(LISTE<DealBool> bools, CharSequence name, CharSequence desc, SPRITE icon){
		info = new INFO(name, desc);
		this.icon = icon;
		bools.add(this);
	}
	
	public abstract CharSequence problem();
	
	public abstract double value();
	
	public abstract void execute();
	
	protected abstract void pInit(DealParty a, DealParty b);
	
	protected abstract DipStance stance();
	
	@Override
	public boolean is() {
		return toggled;
	}
	
	@Override
	public BOOLEAN_MUTABLE set(boolean b) {
		toggled = b;
		return this;
	}

	public void hover(GBox b) {
		b.title(info.name);
		b.text(info.desc);
		b.NL();
	}
	
}