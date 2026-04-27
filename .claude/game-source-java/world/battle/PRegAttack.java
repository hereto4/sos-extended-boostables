package world.battle;

import java.io.IOException;

import game.faction.FACTIONS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitmap1D;
import world.WORLD;
import world.army.AD;
import world.battle.Side.Conflict;
import world.battle.Util.Pair;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

class PRegAttack implements SAVABLE{

	private final ArrayListInt active = new ArrayListInt(WREGIONS.MAX);
	private final Bitmap1D map = new Bitmap1D(WREGIONS.MAX, false);
	private final ArrayListResize<WArmy> armies = new ArrayListResize<WArmy>(120);	
	private final Conflict conflict;
	private final Resolver init;
	private final Util util;
	private int playerRegAttackRegion = -1;
	private int playerRegAttackArmy = -1;
	
	
	public PRegAttack(Conflict conflict, Resolver init, Util util) {
		this.conflict = conflict;
		this.util = util;
		this.init = init;
	}

	@Override
	public void save(FilePutter file) {
		active.save(file);
		map.save(file);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		active.load(file);
		map.load(file);
	}
	@Override
	public void clear() {
		active.clear();
		map.clear();
	}

	public void register(WArmy a) {
		if (a.region() != null && !map.get(a.region().index())) {
			map.set(a.region().index(), true);
			active.add(a.region().index());
		}
	}
	
	public void regAttack(Region reg, WArmy a) {
		playerRegAttackArmy = a.armyIndex();
		playerRegAttackRegion = reg.index();
	}
	
	public boolean poll() {
		
		
		if (playerRegAttackArmy >= 0) {
			Region reg = WORLD.REGIONS().getByIndex(playerRegAttackRegion);
			WArmy a = WORLD.ENTITIES().armies.get(playerRegAttackArmy);
			playerRegAttackArmy = -1;
			if (reg.faction() == FACTIONS.player()) {
				if (create(reg, a))
					return true;
			}
		}
		
		int death = 1000;
		
		while(!active.isEmpty()) {
			int ai = active.get(active.size()-1);
			Region reg = WORLD.REGIONS().all().get(ai);
			if (create(reg))
				return true;
			else {
				map.set(active.get(active.size()-1), false);
				active.remove(active.size()-1);
			}
			if (death -- < 0) {
				LOG.err("NOHA!");
				break;
			}
		}
		return false;
	}
	
	private boolean create(Region reg) {
		
		if (reg == null)
			return false;
		
		if (RD.MILITARY().garrison.get(reg) <= 0)
			return false;
		
		if (reg.faction() == FACTIONS.player()) {
			return false;
		}
		
		if (reg.besieged()) {
			WArmy a = util.getBesieger(reg);
			if (a != null) {
				return create(reg, a);
			}
		}
		
		armies.clearSoft();
		armies.add(WORLD.ENTITIES().armies.fill(reg));

		for (WArmy a : armies) {
			if (a.region() == reg && create(reg, a))
				return true;
		}
		
		return false;
		
	}
	

	
	private boolean create(Region reg, WArmy a) {
		
		if (Util.valid(a) == null)
			return false;
		
		if (reg == null)
			return false;
		
		if (RD.MILITARY().garrison.get(reg) <= 0)
			return false;
		
		if (!Util.enemies(reg.faction(), a.faction()))
			return false;
		
		if (reg.besieged()) {
			if (a.besieging() != reg)
				return false;
		}
		
		double regPow = RD.MILITARY().power.getD(reg);
		double aPow = AD.power().get(a);
		
		
		Pair allies = util.fill(reg.faction(), a.faction(), a.ctx(), a.cty());
		boolean p = reg.faction() == FACTIONS.player();
		for (WArmy a2 : allies.a) {
			if ((p || a2.faction() == FACTIONS.player()))
				aPow += AD.power().get(a2);	
		}
		
		for (WArmy a2 : allies.b) {
			if (a2 != a)
				aPow += AD.power().get(a2);	
		}
		
		if (!p && regPow < aPow)
			return false;
		
		conflict.clear();
		conflict.A.add(reg);
		for (WArmy a2 : allies.a) {
			conflict.A.add(a2);
		}
		conflict.B.add(a);
		for (WArmy a2 : allies.b) {
			if (a != a2)
				conflict.B.add(a2);
		}
		
		init.init(conflict.A, conflict.B);
		return true;
		
	}
	
}