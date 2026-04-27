package game.faction.player.emmi;

import snake2d.util.sprite.SPRITE;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public abstract class EmiTypeReg extends EmiType<Region> {
	
	EmiTypeReg(SPRITE icon, CharSequence name, CharSequence desc) {
		super(icon, name, desc, WREGIONS.MAX, 100);
	}
	
	
	@Override
	int index(Region reg) {
		return reg.index();
	}
	
}
