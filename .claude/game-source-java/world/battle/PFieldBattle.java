package world.battle;

import java.io.IOException;

import snake2d.LOG;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.Bitmap1D;
import world.WORLD;
import world.army.AD;
import world.battle.Side.Conflict;
import world.battle.Util.Pair;
import world.entity.army.WArmy;
import world.entity.army.WArmyConstructor;

final class PFieldBattle implements SAVABLE{
	
	private final Util util;
	private final Conflict con;
	private final Resolver init;
	private final Bitmap1D map = new Bitmap1D(WArmyConstructor.MAX, false);
	private final ArrayListInt current = new ArrayListInt(WArmyConstructor.MAX);
	
	public PFieldBattle(Conflict con, Resolver init, Util util) {
		this.con = con;
		this.init = init;
		this.util = util;
	}
	
	@Override
	public void save(FilePutter file) {
		map.save(file);
		current.save(file);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		map.load(file);
		current.load(file);
	}
	@Override
	public void clear() {
		map.clear();
		current.clear();
	}

	public void add(WArmy a) {
		
		if (map.get(a.armyIndex()))
			return;
		
		map.set(a.armyIndex(), true);
		current.add(a.armyIndex());
		
	}

	
	public boolean poll() {
		
		int death = 1000;
		
		while(!current.isEmpty()) {
			int ai = current.get(current.size()-1);
			WArmy a = WORLD.ENTITIES().armies.get(ai);
			if (create(a))
				return true;
			else {
				map.set(current.get(current.size()-1), false);
				current.remove(current.size()-1);
			}
			if (death -- < 0) {
				LOG.err("NOHA!");
				break;
			}
		}
		return false;
	}

	private boolean create(WArmy a) {
		if (!valid(a))
			return false;
		
		WArmy e = enemy(a);
		if (e == null)
			return false;
		Pair allies = util.fill(a.faction(), e.faction(), a.ctx(), a.cty());
		con.clear();
		
		con.clear();
		con.A.add(a);
		for (WArmy a2 : allies.a) {
			if (a != a2)
				con.A.add(a2);
		}	
		con.B.add(e);
		for (WArmy a2 : allies.b) {
			if (a != a2)
				con.B.add(a2);
		}
		init.init(con.A, con.B);
		return true;
	}
	

	
	private boolean valid(WArmy a) {
		return (a != null && AD.men(null).get(a) > 0);
	}
	
	private WArmy enemy(WArmy a) {
		if (WORLD.PATH().map.is.is(a.ctx(), a.cty())) {
			
			for (int di = 0; di < DIR.ALLC.size(); di++) {
				DIR d = DIR.ALLC.get(di);
				if (d == DIR.C || WORLD.PATH().map.can( a.ctx(), a.cty(), d)) {
					
					int dx = a.ctx() + d.x();
					int dy = a.cty() + d.y();
					
					for (WArmy a2 : WORLD.ENTITIES().armies.fillTile(dx, dy)) {
						if (a2.ctx() == dx && a2.cty() == dy) {
							if (valid(a2) && Util.enemies(a.faction(), a2.faction())) {
								return a2;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void register(WArmy a) {
		if (map.get(a.armyIndex()))
			return;
		
		map.set(a.armyIndex(), true);
		current.add(a.armyIndex());
	}
	
}
