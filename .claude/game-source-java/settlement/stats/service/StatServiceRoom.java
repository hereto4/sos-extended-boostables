package settlement.stats.service;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.Booster;
import game.boosting.BoosterValue;
import init.race.Race;
import init.race.bio.Opinion;
import init.sprite.UI.Icon;
import init.type.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.room.service.module.RoomServiceAccess;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.StatInfo;
import settlement.stats.util.StatBooster;
import settlement.stats.util.StatHoverer;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.Dic;

public final class StatServiceRoom extends StatServiceImp{

	public final static int TARGET_MAX = 16;
	private final STAT access;
	private final STAT quality;
	private final STAT proximity;
	private final STAT upgrade;
	private final STAT total;
	private final RoomServiceAccess room;

	StatServiceRoom(LISTE<StatServiceImp> all, RoomServiceAccess service, StatsInit init) {
		super(service.room().key, all, init, service.room().info.name, service.room().info.desc, service.room().icon, service.need);
		this.room = service;

		
		access = new STATData(null, init, init.count.new DataBit("SERVICEA_" + service().room().key),  new StatInfo(¤¤Access, ¤¤Access, ¤¤AcessDesc));
		access.info().setMatters(false, true);
		quality = new STATData(null, init, init.count.new DataNibble("SERVICEQ_" + service().room().key), new StatInfo(¤¤Quality, ¤¤Quality, ¤¤QualityDesc));
		quality.info().setMatters(false, true);
		proximity = new STATData(null, init, init.count.new DataNibble("SERVICEP_" + service().room().key), new StatInfo(¤¤Distance, ¤¤Distance, ¤¤DistanceDesc));
		proximity.info().setMatters(false, true);
		upgrade = new STATData(null, init, init.count.new DataNibble("SERVICEUP_" + service().room().key), new StatInfo(Dic.¤¤Upgrade, Dic.¤¤Upgrade, ¤¤UpDesc));
		upgrade.info().setMatters(false, true);
		
		init.onArrivalStats.add(access);
		init.onArrivalStats.add(quality);
		init.onArrivalStats.add(proximity);
		init.onArrivalStats.add(upgrade);
		
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				double a = access.indu().getD(t);
				if (a == 0)
					return 0;
				double q = quality.indu().getD(t);
				double p = proximity.indu().getD(t);
				double u = upgrade.indu().getD(t);
				
				return (int) (64*(a * (0.2 + 0.8*u) * (0.2 + 0.8 * q)
						* (0.5 + 0.5 * p)));
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 64;
			}

			@Override
			public void set(Induvidual t, int i) {
				
			}
			
			
		};
		
		StatInfo info = new StatInfo(service.room().info.names, ¤¤TotalDesc);
		info.setOpinion(new Opinion().setMore(service.induMore));
		
		total = new STATFacade(service().room().key, init, indu, info) {


			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double a = access.data(s).getD(r, daysBack);
				if (a == 0)
					return 0;
				double q = quality.data(s).getD(r, daysBack)/a;
				double p = proximity.data(s).getD(r, daysBack)/a;
				double u = upgrade.data(s).getD(r, daysBack)/a;
				return a * (0.2 + 0.8*u)*(0.2 + 0.8 * q)
						* (0.5 + 0.5 * p);
			}
			
			@Override
			public void hover(GUI_BOX text, HCLASS cl, Race race) {
				GBox b = (GBox) text;
				StatHoverer.hover(b, this);
				b.sep();
				b.textLL(access().info().name);
				b.add(GFORMAT.perc(b.text(), access().data(cl).getD(race)));
				b.NL().text(access().info().desc);
				b.NL(4);
				b.textLL(proximity().info().name);
				double p = proximity().data(cl).getD(race)/access().data(cl).getD(race);
				
				b.add(GFORMAT.perc(b.text(), p));
				b.NL().text(proximity().info().desc);
				b.NL(4);
				b.textLL(quality().info().name);
				b.add(GFORMAT.perc(b.text(), CLAMP.d(quality().data(cl).getD(race)/access().data(cl).getD(race), 0, 1)));
				b.NL().text(quality().info().desc);			
				b.NL(4);
				b.textLL(upgrade().info().name);
				b.add(GFORMAT.perc(b.text(), upgrade().data(cl).getD(race)/access().data(cl).getD(race)));
				b.NL().text(upgrade().info().desc);			
				b.NL(4);
				b.textLL(Dic.¤¤Total);
				b.add(GFORMAT.perc(b.text(), data(cl).getD(race)));
				
				b.sep();
				StatHoverer.hover(text, this, cl, race);
			}
			
			@Override
			public void hover(GUI_BOX text, Induvidual indu) {
				GBox b = (GBox) text;
				StatHoverer.hover(b, this);
				b.sep();
				b.textLL(access().info().name);
				b.add(GFORMAT.perc(b.text(), access().indu().getD(indu)));
				b.NL().text(access().info().desc);
				b.NL(4);
				b.textLL(proximity().info().name);
				

				b.add(GFORMAT.perc(b.text(), proximity().indu().getD(indu)/access().indu().getD(indu)));
				b.NL().text(proximity().info().desc);
				b.NL(4);
				b.textLL(quality().info().name);
				b.add(GFORMAT.perc(b.text(), quality().indu().getD(indu)/access().indu().getD(indu)));
				b.NL().text(quality().info().desc);			
				b.NL(4);
				b.textLL(upgrade().info().name);
				b.add(GFORMAT.perc(b.text(), upgrade().indu().getD(indu)/access().indu().getD(indu)));
				b.NL().text(upgrade().info().desc);			
				b.NL(4);
				b.textLL(Dic.¤¤Total);
				b.add(GFORMAT.perc(b.text(), indu().getD(indu)));
				
				b.sep();
				StatHoverer.hover(text, this, indu);
			}
			
		};
		total.standing = new StatStanding(total, 0, service().standingDef);
		total.info().icon = service.room().icon.resized(Icon.S);
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				for (BoostSpec t : service.boosts.all()) {
					
					BValue v = StatBooster.make(total);
					BSourceInfo in = new BSourceInfo(service.room().info.names, service.room().icon.small);
					Booster b = new BoosterValue(v, in, t.booster.from(), t.booster.to(), t.booster.isMul);
					
					total.boosters.push(b, t.boostable);
					
					
					
				}
			}
		});
		
		
		
		

	}

	@Override
	public boolean access(Humanoid h) {
		return access.indu().get(h.indu()) == 1;
	}

	public double quality(Humanoid h) {
		return quality.indu().getD(h.indu());
	}

	public double proximity(Humanoid h) {
		return proximity.indu().getD(h.indu());
	}

	public double total(Humanoid h) {
		return total(h.indu());
	}
	
	public double total(Induvidual a) {
		return access.indu().getD(a) * (0.2 + quality.indu().getD(a) * 0.8)
				* (0.5 + proximity.indu().getD(a) * 0.5)
				* (0.5 + upgrade.indu().getD(a) * 0.5);
	}

	protected double pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	public void setAccess(Humanoid h, boolean access, double quality, double proximity, int upgrade) {
		setAccess(h.indu(), access, quality, proximity, upgrade);
	}
	
	public void setAccess(Induvidual i, boolean access, double quality, double proximity, int upgrade) {
		
		this.access.indu().set(i, access ? 1 : 0);
		if (!access) {
			quality = 0;
			proximity = 0;
			upgrade = -1;
		}
			
		this.quality.indu().setD(i, quality);
		this.proximity.indu().setD(i, proximity);
		
		this.upgrade.indu().setD(i, (upgrade+1.0)/(room.room().upgrades().max()+1.0));
	}

	@Override
	public void clearAccess(Induvidual i) {
		setAccess(i, false, 0, 0, 0);
	}
	
	public void setProximity(Humanoid h, double proximity) {
		Induvidual i = h.indu();
		this.proximity.indu().setD(i, proximity);
	}

	public RoomServiceAccess service() {
		return room;
	}

	public STAT access() {
		return access;
	}

	public STAT quality() {
		return quality;
	}
	
	public STAT upgrade() {
		return upgrade;
	}

	public STAT proximity() {
		return proximity;
	}

	@Override
	public STAT total() {
		return total;
	}

	@Override
	public void cheatSetTotal(Induvidual i, double tot) {
		access.indu().set(i, tot > 0 ? 1 : 0);
		quality.indu().setD(i, tot);
		upgrade.indu().setD(i, tot);
		proximity.indu().setD(i, tot);
		
	}

}