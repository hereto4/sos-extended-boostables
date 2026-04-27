package settlement.stats.service;

import init.type.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.StatInfo;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;

public class StatServiceSimple extends StatServiceImp{

	private final STAT access;

	StatServiceSimple(String key, LISTE<StatServiceImp> all, StatsInit init, CharSequence name, CharSequence desc, SPRITE icon, NEED need) {
		super(key, all, init, name, desc, icon, need);

		
		access = new STATData(key, init, init.count.new DataBit("SERVICEA_" + key),  new StatInfo(name, ¤¤TotalDesc));
		access.info().setMatters(false, true);
		access.info().icon = icon;
		init.onArrivalStats.add(access);

	}

	@Override
	public boolean access(Humanoid h) {
		return access.indu().get(h.indu()) == 1;
	}

	public void setAccess(Humanoid h, boolean access) {
		setAccess(h.indu(), access);
	}
	
	public void setAccess(Induvidual i, boolean access) {
		
		this.access.indu().set(i, access ? 1 : 0);
	}

	@Override
	public STAT total() {
		return access;
	}
	
	@Override
	public void clearAccess(Induvidual i) {
		access.indu().set(i, 0);
	}

	@Override
	public void cheatSetTotal(Induvidual i, double tot) {
		access.indu().set(i, tot > 0 ? 1 : 0);
	}

}