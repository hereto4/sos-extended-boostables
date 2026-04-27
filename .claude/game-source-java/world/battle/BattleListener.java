package world.battle;

import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.UI.UI;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.Str;
import util.text.D;
import world.WORLD;
import world.battle.Side.SideUnit;
import world.entity.army.WArmy;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;

public abstract class BattleListener {
	
	private static CharSequence ¤¤siege = "{0} forces have taken control of {1}.";
	private static CharSequence ¤¤battle = "An army of {0} defeated an army of {1} near {2}.";

	
	
	static {
		D.ts(BattleListener.class);
	}
	
	public static int changeI;
	static final ArrayListGrower<BattleListener> all = new ArrayListGrower<>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	public BattleListener() {
		all.add(this);
	}
	
	public abstract void battle(Faction a, boolean victory, int losses, int kills, Faction against);
	public abstract void battle(WArmy a, boolean victory, int losses, int kills, Faction against);
	public abstract void siege(WArmy attacker, Region reg);
	public abstract void siege(Faction attacker, Region reg);
	
	private static Bitmap1D map = new Bitmap1D(FACTIONS.MAX(), false);
	private static int[] ulosses = new int[Math.max(128, FACTIONS.MAX())];
	
	static void notify(ResolverSide winner, ResolverSide looser) {
		int cas = 0;
		int loss = 0;
		
		for (int i = 0; i < winner.us.size(); i++)
			cas += winner.us.get(i).losses;
		for (int i = 0; i < looser.us.size(); i++)
			loss += looser.us.get(i).losses;
		
		Str.TMP.clear().add(¤¤battle);
		Str.TMP.insert(0, FACTIONS.name(winner.us.get(0).unit.faction()));
		Str.TMP.insert(1, FACTIONS.name(looser.us.get(0).unit.faction()));
		int tx = looser.us.get(0).unit.x();
		int ty = looser.us.get(0).unit.y();
		Region reg = WORLD.REGIONS().map.get(tx, ty);
		if (reg == null) {
			RegDist r = WORLD.PATH().regFinder.single(tx, ty, Treaty.DUMMY, WRegSel.DUMMY(null));
			if (r != null) {
				reg = r.reg;
			}
		}
		
		if (reg != null) {
			Str.TMP.insert(2, reg.info.name());
		}
		WORLD.LOG().log(winner.us.get(0).unit.faction(), looser.us.get(0).unit.faction(), UI.icons().s.sword, Str.TMP, tx, ty);
		
		noti(winner, true, loss, cas, looser.side);
		noti(looser, false, cas, loss, winner.side);
		
		
	}
	
	static void notify(Side side, Region reg) {
		map.clear();
		for (SideUnit u : side.us) {
			if (u.a() != null) {
				for (BattleListener li : BattleListener.all)
					li.siege(u.a(), reg);
			}
			if (u.faction() != null && !map.get(u.faction().index())) {
				map.set(u.faction().index(), true);
				
				for (BattleListener li : BattleListener.all)
					li.siege(u.faction(), reg);
			}
		}
		
		Str.TMP.clear().add(¤¤siege);
		Str.TMP.insert(0, FACTIONS.name(side.us.get(0).faction()));
		Str.TMP.insert(1, reg.info.name());
		WORLD.LOG().log(side.us.get(0).faction(), reg.faction(), UI.icons().s.degrade, Str.TMP, reg.cx(), reg.cy());
	}
	
	private static void noti(ResolverSide side, boolean victory, int kills, int cas, Side against) {
		map.clear();
		for (int i = 0; i < side.us.size(); i++) {
			if (side.us.get(i).unit.faction() != null) {
				int mi = side.us.get(i).unit.faction().index();
				if (!map.get(mi)) {
					ulosses[mi] = 0;
					map.set(mi, true);
				}
				ulosses[side.us.get(i).unit.faction().index()] += side.us.get(i).losses;
			}
		}
		map.clear();
		for (int i = 0; i < side.us.size(); i++) {
			
			if (side.us.get(i).unit.faction() != null) {
				int mi = side.us.get(i).unit.faction().index();
				if (!map.get(mi)) {
					map.set(mi, true);
					for (BattleListener li : BattleListener.all)
						li.battle(side.us.get(i).unit.faction(), victory, ulosses[mi], kills, against.us.get(0).faction());
				}
			}
		}
		

		for (int ui = 0; ui < side.us.size(); ui++) {
			if (side.us.get(ui).unit.a() != null) {
				for (BattleListener li : BattleListener.all)
					li.battle(side.us.get(ui).unit.a(), victory, cas, kills, against.us.get(0).faction());
			}
		}
	}
	
}