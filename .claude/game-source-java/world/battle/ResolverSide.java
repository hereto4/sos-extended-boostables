package world.battle;

import java.util.Arrays;

import game.faction.FACTIONS;
import init.constant.Config;
import init.race.RACES;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import view.main.VIEW;
import world.army.AD;
import world.army.ADSupply;
import world.army.WDIV;
import world.army.ADSupplies.ADArtillery;
import world.battle.Side.SideUnit;
import world.battle.spec.WBattleSide;
import world.battle.spec.WBattleUnit;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

final class ResolverSide implements WBattleSide{
	
	
	private final ArrayList<ResolverUnit> all = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	public final ArrayList<ResolverUnit> us = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final ArrayList<WBattleUnit> units = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);

	public Side side;
	private final Coo coo = new Coo();
	public boolean player;
	public double powerBalance;
	private int men;
	private int losses;
	private int lossesRetreat;
	private final int[] artilleryPieces = new int[AD.supplies().arts().size()];
	public final Coo retreatCoo = new Coo();
	
	ResolverSide(){
		while(all.hasRoom())
			all.add(new ResolverUnit());
	}
	
	public void init(Side side, double powerBalance) {
		this.side = side;
		this.powerBalance = powerBalance;
		men = 0;
		player = false;
		losses = 0;
		lossesRetreat = 0;
		us.clearSloppy();
		units.clearSloppy();
		for (int i = 0; i < side.us.size(); i++) {
			ResolverUnit u = all.get(i);
			u.init(side.us.get(i));
			player |= side.us.get(i).faction() == FACTIONS.player();
			men += side.us.get(i).men();
			us.add(u);
			units.add(u);
		}
		coo.set(side.us.get(0).x(), side.us.get(0).y());
		for (int ai = 0; ai < AD.supplies().arts().size(); ai++) {
			int am = 0;
			for (SideUnit u : side.us) {
				if (u.a() != null) {
					am += AD.supplies().arts().get(ai).current(u.a());
				}
			}
			artilleryPieces[ai] = am;
			
		}
		retreatCoo.set(-1, -1);
	}
	
	public void count(RCount c, double looseAmount, boolean ret) {
		for (ResolverUnit u : us) {
			u.count(c, looseAmount, ret);
		}
		
	}
	
	public ResolverSide clear() {
		losses = 0;
		lossesRetreat = 0;
		
		for (ResolverUnit u : us) {
			u.losses = 0;
			u.lossesRetreat = 0;
		}
		return this;
	}
	
	public void extract(double looseAmount) {
		for (ResolverUnit u : us) {
			u.extract(looseAmount);
		}
	}
	
	@Override
	public COORDINATE coo() {
		return coo;
	}

	@Override
	public int men() {
		return men;
	}

	@Override
	public int losses() {
		return losses;
	}

	@Override
	public int lossesRetreat() {
		return lossesRetreat;
	}

	@Override
	public LIST<WBattleUnit> units() {
		return units;
	}

	@Override
	public int artillery(ADArtillery a) {
		return artilleryPieces[a.index()];
	}
	
	@Override
	public double powerBalance() {
		return powerBalance;
	}
	
	final class ResolverUnit implements WBattleUnit{
		
		public SideUnit unit;
		int losses;
		int lossesRetreat;
		double defences;
		
		private ResolverUnit() {
			
		}
		
		private void init(SideUnit u) {
			this.unit = u;
			this.defences = 0;
			losses = 0;
			lossesRetreat = 0;
		}

		public void count(RCount c, double looseAmount, boolean ret) {
			int losses = (int) Math.ceil(looseAmount*unit.men());
			count(c, losses, ret);
		}
		
		public void count(RCount c, int loss, boolean ret) {
			double looseAm = (double)loss/unit.men();
			int dead = 0;
			if (unit.a() != null) {
				WArmy a = unit.a();
				double d = (double)1.1*looseAm;
				d = CLAMP.d(d, 0, 1);
				for (ADSupply s : AD.supplies().all) {
					int am = (int) (s.current().get(a)*d);
					c.res[s.res.index()] += am;
				}
				
				for (int di = 0; di < a.divs().size(); di++) {
					WDIV div = a.divs().get(di);
					int dd = (int) Math.ceil(d*div.men());
					c.dead[div.race().index] += dd;
					dead += dd;
				}
			}else {
				Region reg = unit.r();
				double d = (double)1.1*looseAm;
				for (int di = 0; di < RD.MILITARY().divisions(reg).size(); di++) {
					WDIV div = RD.MILITARY().divisions(reg).get(di);
					int dd = (int) Math.ceil(d*div.men());
					dead += dd;
					c.dead[div.race().index()] += dd;
				}
			}
			if (ret) {
				ResolverSide.this.lossesRetreat += dead-lossesRetreat;
				lossesRetreat = dead;
			}else {
				ResolverSide.this.losses += dead-losses;
				losses = dead;
			}
		}
		
		public void extract(double looseAmount) {
			if (unit.a() != null)
				extract(unit.a(), looseAmount);
			else
				extract(unit.r(), looseAmount);
		}
		
		public void extract(WArmy a, double looseAmount) {
			double d = (double)1.1*looseAmount;
			for (ADSupply s : AD.supplies().all) {
				int am = (int) Math.ceil(s.current().get(a)*d);
				s.current().inc(a, -am);
			}
			
			for (int di = 0; di < a.divs().size(); di++) {
				WDIV div = a.divs().get(di);
				kill(div, looseAmount);
			}
		}
		
		public void extract(Region reg, double looseAmount) {
			for (int di = 0; di < RD.MILITARY().divisions(reg).size(); di++) {
				WDIV div = RD.MILITARY().divisions(reg).get(di);
				kill(div, looseAmount);
			}
		}
		
		private void kill(WDIV div, double looseAmount) {
			int l = (int) Math.ceil(looseAmount*div.men());
			losses += l;
			
			int survivors = div.men() - l;
			
			double xp = 0;
			
			if (survivors > 0) {
				xp = (double)0.1*div.men()/survivors;
				xp += div.experience();
				xp = CLAMP.d(xp, 0, 1);
			}
			
			div.resolve(survivors, xp);
		}

		@Override
		public CharSequence name() {
			return  unit.a() != null ? unit.a().name : unit.r().info.name();
		}

		@Override
		public int men() {
			return men;
		}

		@Override
		public int losses() {
			return losses;
		}

		@Override
		public int lossesRetreat() {
			return lossesRetreat;
		}

		@Override
		public SPRITE icon() {
			return unit.faction() != null ? unit.faction().banner().MEDIUM : UI.icons().m.rebellion;
		}

		@Override
		public void hover(GUI_BOX box) {
			if (unit.a() != null) {
				VIEW.world().UI.armies.hover(box, unit.a());
			}else if (unit.r() != null) {
				VIEW.world().UI.regions.hoverGarrison(unit.r(), box);
			
			}
			
		}

		@Override
		public double defences() {
			return defences;
		}
		

		
	}

	public static class RCount {
		
		public int[] res = new int[RESOURCES.ALL().size()];
		public int[] dead = new int[RACES.all().size()];
		
		public RCount clear() {
			Arrays.fill(res, 0);
			Arrays.fill(dead, 0);
			return this;
		}
		
		
	}
	

	
}