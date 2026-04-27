package world.battle;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageText;
import world.WORLD;
import world.army.AD;
import world.battle.Side.Conflict;
import world.battle.Util.Pair;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

final class PSiege implements SAVABLE{

	private final ArrayListInt besieged = new ArrayListInt(WREGIONS.MAX);
	private final Bitmap1D besigedMap = new Bitmap1D(WREGIONS.MAX, false);
	private final Bitmap1D first = new Bitmap1D(WREGIONS.MAX, false);
	private final double[] besigeTime = new double[WREGIONS.MAX];
	private final ArrayListInt active = new ArrayListInt(WREGIONS.MAX);
	private final Bitmap1D map = new Bitmap1D(WREGIONS.MAX, false);
	private double dd;
	private static double dTime = 128;
	private static double dTimeI = WREGIONS.MAX/dTime;

	private final Util util;
	private final Conflict conflict;
	private final Resolver resolver;
	private int playerRegAttackRegion = -1;
	
	
	private static CharSequence ¤¤name = "Besieged";
	private static CharSequence ¤¤desc = "The city of {0} have been besieged by our enemies!";
	
	static {
		D.ts(PSiege.class);
	}
	
	public PSiege(Util util, Conflict conflict, Resolver resolver) {
		this.util = util;
		this.conflict = conflict;
		this.resolver = resolver;
	}
	
	@Override
	public void save(FilePutter file) {
		besieged.save(file);
		besigedMap.save(file);
		file.ds(besigeTime);
		file.d(dd);
		active.save(file);
		map.save(file);
		first.save(file);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		besieged.load(file);
		besigedMap.load(file);
		file.ds(besigeTime);
		dd = file.d();
		active.load(file);
		map.load(file);
		first.load(file);
			
	}
	@Override
	public void clear() {
		besieged.clear();
		besigedMap.clear();
		Arrays.fill(besigeTime, 0);
		dd = 0;
		active.clear();
		map.clear();
		first.clear();
	}
	
	public void register(WArmy a) {
		Region reg = a.besieging();
		if (reg != null && !map.get(reg.index())) {
			map.set(reg.index(), true);
			first.set(reg.index(), true);
			active.add(reg.index());
		}
	}
	
	public void playerBesige(Region reg) {
		playerRegAttackRegion = reg.index();
	}

	public void update(double ds) {
		
		for (int i = 0; i < besieged.size(); i++) {
			int ri = besieged.get(i);
			Region reg = WORLD.REGIONS().getByIndex(ri);
			if (util.getBesieger(reg) == null) {
				besieged.remove(i);
				besigedMap.set(ri, false);
				i--;
			}
		}
		
		int current = (int) dd;
		dd += ds*dTimeI;
		int next = (int) dd;
		
		while(current < next) {
			
			int ri = current % WREGIONS.MAX;
			current ++;
			Region reg = WORLD.REGIONS().getByIndex(ri);
			if (!reg.active())
				continue;
			WArmy a = util.getBesieger(reg);
			if (a != null) {
				besigeTime[ri] += dTime;
			}else {
				besigeTime[ri] -= dTime*4;
			}
			besigeTime[ri] = CLAMP.d(besigeTime[ri], 0, Double.MAX_VALUE/2);
			
		}
		
		while(dd >= WREGIONS.MAX)
			dd -= WREGIONS.MAX;
		
	}

	public double besigedTime(Region reg) {
		return besigeTime[reg.index()];
	}
	
	public boolean besiged(Region reg) {
		return besigedMap.get(reg.index());
	}
	
	public void besige(WArmy a, Region reg) {
		
		if (!besigedMap.get(reg.index())) {
			besigedMap.set(reg.index(), true);
			besieged.add(reg.index());
			if (reg.faction() == FACTIONS.player() && !reg.capitol()) {
				double regPow = RD.MILITARY().defensePower(reg);
				double aPow = AD.power().get(a);
				
				Pair allies = util.fill(a.faction(), reg.faction(), reg.cx(), reg.cy());
				for (WArmy a2 : allies.a) {
					if (a2 != a)
						aPow += AD.power().get(a2);	
				}
				
				if (regPow > aPow) {
					new MessageText(¤¤name, Str.TMP.clear().add(¤¤desc).insert(0, reg.info.name())).send();
				}
				
			}
			
		}
		if (a.faction() == FACTIONS.player())
			playerRegAttackRegion = reg.index();
	}
	
	public boolean poll() {
		
		
		if (playerRegAttackRegion >= 0) {
			Region reg = WORLD.REGIONS().getByIndex(playerRegAttackRegion);
			playerRegAttackRegion = -1;
			WArmy a = util.getBesieger(reg);
			
			if (a != null && a.faction() == FACTIONS.player()) {
				if (create(a, reg, true))
					return true;
			}
		}
		
		int death = 1000;
		
		while(!active.isEmpty()) {
			int ai = active.get(active.size()-1);
			Region reg = WORLD.REGIONS().all().get(ai);
			boolean f = first.get(reg.index());
			first.set(reg.index(), false);
			if (create(reg, f))
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
	
	private boolean create(Region reg, boolean first) {
		
		if (reg == null)
			return false;
		
		WArmy a = util.getBesieger(reg);
		if (a != null && a.faction() != FACTIONS.player())
			return create(a, reg, first);
		return false;
		
	}
	

	
	private boolean create(WArmy a, Region reg, boolean first) {

		if (!Util.enemies(reg.faction(), a.faction()))
			return false;
		
		if (a.besieging() != reg)
			return false;
		
		double regPow = RD.MILITARY().defensePower(reg);
		double aPow = AD.power().get(a);
		
		Pair allies = util.fill(a.faction(), reg.faction(), reg.cx(), reg.cy());
		for (WArmy a2 : allies.a) {
			if (a2 != a)
				aPow += AD.power().get(a2);	
		}
		
		for (WArmy a2 : allies.b) {
			regPow += AD.power().get(a2);
		}
		
		if (a.faction() != FACTIONS.player() && regPow > aPow) {
			return false;
		}

		conflict.clear();
		conflict.A.add(a);

		for (WArmy a2 : allies.a) {
			if (a != a2)
				conflict.A.add(a2);
		}
		conflict.B.add(reg);
		for (WArmy a2 : allies.b) {
			conflict.B.add(a2);
		}
		
		
		
		return resolver.besige(conflict.A, conflict.B, first);
		
	}
}
