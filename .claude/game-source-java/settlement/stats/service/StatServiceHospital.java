package settlement.stats.service;

import init.race.Race;
import init.sprite.UI.Icon;
import init.type.HCLASS;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.health.hospital.ROOM_HOSPITAL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatInfo;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.Dic;

public final class StatServiceHospital extends StatServiceImp{

	private final STAT stat;
	StatServiceHospital(LISTE<StatServiceImp> all, ROOM_HOSPITAL ho, StatsInit init) {
		super(ho.key, all, init, ho.info.name, ho.info.desc, ho.icon, null);
		stat = new STATImp(ho.key, ho.key + "D", init, new StatInfo(ho.info.name, ho.info.desc)) {

			@Override
			protected int getDD(HCLASS s, Race r) {
				if (!permission().is(POP_CL.clP(r, s)))
					return 0;
				double e = SETT.ROOMS().HOSPITAL.employment().employed();
				return (int) CLAMP.i((int)(75*e), 0, pdivider(s, r, 0));
			}	
			
			@Override
			public void hover(GUI_BOX text, HCLASS cl, Race type) {
				GBox b = (GBox) text;
				b.textLL(SETT.ROOMS().HOSPITAL.employment().title);
				b.NL();
				b.textL(Dic.¤¤Employees);
				b.tab(6);
				b.add(GFORMAT.i(b.text(),  SETT.ROOMS().HOSPITAL.employment().employed()));
				b.NL();
				b.textL(Dic.¤¤Target);
				b.tab(6);
				b.add(GFORMAT.i(b.text(),  (int)Math.ceil((STATS.POP().POP.data().get(null) + 1)/75.0)));
				b.NL();
				b.textL(Dic.¤¤Access);
				b.tab(6);
				b.add(GFORMAT.bool(b.text(),  permission().is(POP_CL.clP(type, cl))));
				b.NL();
				b.textL(Dic.¤¤Value);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), CLAMP.d(100.0*SETT.ROOMS().HOSPITAL.employment().employed()/(STATS.POP().POP.data().get(null) + 1), 0, 1)));
				b.NL();
				
				super.hover(text, cl, type);
			}
			
			@Override
			public void hover(GUI_BOX text, Induvidual indu) {
				hover(text, indu.clas(), indu.race());
			}
		};
		stat.info().icon = SETT.ROOMS().HOSPITAL.icon.resized(Icon.S);

	}

	@Override
	public boolean access(Humanoid h) {
		return stat.indu().getD(h.indu()) >= 1;
	}

	@Override
	public STAT total() {
		return stat;
	}

	@Override
	public void clearAccess(Induvidual i) {
		
	}

	@Override
	public void cheatSetTotal(Induvidual i, double tot) {
		
	}
	
}