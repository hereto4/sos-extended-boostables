package world.army;

import game.GAME;
import game.battle.DivisionBanners.DivisionBanner;
import game.battle.util.DIV_SETTING;
import game.battle.util.DIV_SPEC;
import game.battle.util.DivGeneration;
import settlement.stats.Induvidual;
import snake2d.util.color.COLOR;
import snake2d.util.misc.CLAMP;
import world.entity.army.WArmy;

public interface WDIV extends DIV_SPEC{
	
	public void resolve(Induvidual[] hs);
	public void resolve(int surviviors, double experiencePerMan);
	public default void resolve(int survivors) {
		double xp = 0;
		
		if (survivors > 0) {
			xp = (double)0.1*men()/survivors;
			xp += experience();
			xp = CLAMP.d(xp, 0, 1);
		}
		resolve(survivors, xp);
	}
	
	public DivGeneration generate();
	
	public default int provess() {
		return (int) GAME.battle().power.get(this);
	}
	
	public int daysUntilMenArrives();
	public boolean needSupplies();

	public default DivisionBanner banner() {
		return GAME.ARMIES().banners.get(bannerI());
	}
	public void bannerSet(int bi);
	public int menTarget();
	
	public default int costPerMan() {
		return 0;
	}
	
	public default boolean needConscripts() {
		return false;
	}
	
	public  WArmy army();
	
	public COLOR color();
	
	public DIV_SETTING target();
}
