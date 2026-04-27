package settlement.stats.service;

import init.type.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.stat.STAT;
import snake2d.util.sprite.SPRITE;

public abstract class StatService{

	public final CharSequence name;
	public final CharSequence desc;
	public final SPRITE icon;
	public final NEED need;
	public double usage = 1;
	
	public StatService(CharSequence name, CharSequence desc, SPRITE icon, NEED need) {
		this.name = name;
		this.desc = desc;
		this.icon = icon;
		this.need = need;
	}

	public abstract boolean access(Humanoid h);
	public abstract void clearAccess(Induvidual i);
	public abstract STAT total();
	public abstract void cheatSetTotal(Induvidual i, double tot);
	
	
	public CharSequence name(Induvidual i) {
		return name;
	}
	
	public SPRITE icon(Induvidual i) {
		return icon;
	}
	
}
