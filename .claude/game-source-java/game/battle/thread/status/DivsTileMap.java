package game.battle.thread.status;

import java.util.Arrays;

import game.GAME;
import game.battle.Army;
import game.battle.Armies;
import game.battle.div.Div;
import game.battle.formation.DivPositionImp;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_INT;
import snake2d.util.map.MAP_OBJECT_ISSER;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sets.LISTE;

public final class DivsTileMap {


	private final Tile[] tiles = new Tile[0x0FFFF];
	private int tileNewI = 1;
	private final short[] firstTiles = new short[SETT.TAREA];
	
	private final Bitsmap2D[] soldiers = new Bitsmap2D[] {
			new Bitsmap2D(0, 4, SETT.TWIDTH, SETT.THEIGHT),
			new Bitsmap2D(0, 4, SETT.TWIDTH, SETT.THEIGHT)
	};
	private final DivStatus[] statuses;
	private static IntChecker checker = new IntChecker(Config.battle().DIVISIONS_PER_ARMY*2);
	
	private final ArrayList<Div> tmp = new ArrayList<>(16);
	
	public DivsTileMap(DivStatus[] statuses) {
		this.statuses = statuses;
		
		if (Config.battle().MEN_PER_ARMY*2 > 0xFFFF)
			throw new RuntimeException("too many men in battle. This class must be fixed! Use ints");
		
		for (int i = 1; i < tiles.length; i++) {
			tiles[i] = new Tile();
		}
	}
	

	void add(short div, DivPositionImp next) {

		checker.init();
		
		for (int i = 0; i < next.deployed(); i++) {
			int x = next.tx(i);
			int y = next.ty(i);
			if (SETT.IN_BOUNDS(x, y)) {
				add(x, y, div);
			}
		}
	}
	
	private void add(int x, int y, short currentI) {
		int tileI = x + y*SETT.TWIDTH;
		Tile first = tiles[firstTiles[tileI]&0x0FFFF];
		Div current = GAME.ARMIES().division(currentI);
		
		soldiers[current.army().index()].increment(tileI, 1);
		
		if (tileNewI > 0x0FFFF)
			return;
		
		if (first == null) {
			firstTiles[tileI] = makeNewTile((short)0, current);
			tmp.clear();
			
			return;
		}
		
		{
			Tile t = first;
			while(t != null) {
				if (t.divI == currentI) {
					return;
				}
				t = tiles[t.next&0x0FFFF];
			}
		}
		
		Tile t = first;
		
		DivStatus currentOrder = statuses[currentI];
		
		boolean engaged = false;
		
		while(t != null) {
			
			Div other = GAME.ARMIES().division(t.divI);
			if (!engaged && other.army() != current.army()) {
				currentOrder.engagements ++;
				statuses[other.index()].engagements ++;
				engaged = true;
			}
			
			if (!checker.isSetAndSet(t.divI)) {
				if (other.army() == current.army()) {
					currentOrder.friendlyCollisionSet(t.divI);
					statuses[t.divI].friendlyCollisionSet(currentI);
				}else {
					currentOrder.enemyCollisionSet(t.divI);
					statuses[t.divI].enemyCollisionSet(currentI);
				}
			}
			t = tiles[t.next&0x0FFFF];
		}
		
		for (int di = 0; di < DIR.ORTHO.size() && !engaged; di++) {
			
			DIR dd = DIR.ORTHO.get(di);
			if (!SETT.IN_BOUNDS(x, y, dd))
				continue;
			
			int ti = tileI + dd.x() + dd.y()*SETT.TWIDTH;
			
			
			
			if (ti >= firstTiles.length)
				continue;
			first = tiles[firstTiles[ti]&0x0FFFF];
			while(t != null) {
				
				Div other = GAME.ARMIES().division(t.divI);
				if (other.army() != current.army()) {
					currentOrder.engagements ++;
					statuses[other.index()].engagements ++;
					engaged = true;
					break;
				}
				t = tiles[t.next&0x0FFFF];
			}
			
		}
		
		firstTiles[tileI] = makeNewTile(firstTiles[tileI], current);
		
		
		
	}
	
	private short makeNewTile(short next, Div div) {
		short i = (short) tileNewI;
		Tile t = tiles[tileNewI&0x0FFFF];
		t.next = next;
		t.divI = div.index();
		tileNewI ++;
		return i;
	}
	
	public Iterable<Div> get(LISTE<Div> res, int tx, int ty){
		return get(res, tx, ty, Armies.ARMIES_BITS);
	}
	
	public Iterable<Div> get(LISTE<Div> res, int tx, int ty, DIR d){
		tx+= d.x();
		ty += d.y();
		return get(res, tx, ty);
	}
	
	public Iterable<Div> getAlly(LISTE<Div> res, int tx, int ty, Army a){
		return get(res, tx, ty, a.bit);
	}
	
	public Iterable<Div> getEnemy(LISTE<Div> res, int tx, int ty, Army a){
		return get(res, tx, ty, ~a.bit);
	}
	
	public Div getEnemySingle(int tx, int ty, Army a){
		return getSingle(tx, ty, ~a.bit);
	}
	
	public Div get(int tx, int ty, Army a) {
		return get(tx + ty*SETT.TWIDTH, a.bit);
	}
	
	public MAP_INT soldiers(Army a) {
		return soldiers[a.index()];
	}
	
	public MAP_OBJECT_ISSER<Army> hasEnemy = new MAP_OBJECT_ISSER<Army>() {

		@Override
		public boolean is(int tile, Army value) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return soldiers[(value.index()+1)&1].get(tile) > 0;
		}
	
	};
	
	public MAP_OBJECT_ISSER<Army> hasAlly = new MAP_OBJECT_ISSER<Army>() {

		@Override
		public boolean is(int tile, Army value) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return soldiers[value.index()].get(tile) > 0;
		}
	
	};
	
	public MAP_OBJECT_ISSER<Div> hasOtherAlly = new MAP_OBJECT_ISSER<Div>() {

		@Override
		public boolean is(int tileI, Div value) {
			Tile t = tiles[firstTiles[tileI]&0x0FFFF];
			while(t != null) {
				Div d = GAME.ARMIES().division(t.divI);
				if (d != value && d.army() == value.army())
					return true;
				t = tiles[t.next&0x0FFFF];
			}
			return false;
		}

		@Override
		public boolean is(int tx, int ty, Div value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return is(tile, value);
		}
	
	};
	
	public MAP_OBJECT_ISSER<Div> isser = new MAP_OBJECT_ISSER<Div>() {

		@Override
		public boolean is(int tileI, Div value) {
			Tile t = tiles[firstTiles[tileI]&0x0FFFF];
			while(t != null) {
				Div d = GAME.ARMIES().division(t.divI);
				if (d == value)
					return true;
				t = tiles[t.next&0x0FFFF];
			}
			return false;
		}

		@Override
		public boolean is(int tx, int ty, Div value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return is(tile, value);
		}
	
	};
	
	private Iterable<Div> get(LISTE<Div> res, int tx, int ty, int aMask){
		if (!SETT.IN_BOUNDS(tx, ty))
			return res;
		int tileI = tx + ty*SETT.TWIDTH;
		return getMask(res, tileI, aMask);
	}
	
	private Div getSingle(int tx, int ty, int aMask){
		if (!SETT.IN_BOUNDS(tx, ty))
			return null;
		int tileI = tx + ty*SETT.TWIDTH;
		return getMaskSingle(tileI, aMask);
	}
	
	public Iterable<Div> get(LISTE<Div> res, int tileI){
		return getMask(res, tileI, Armies.ARMIES_BITS);
	}
	
	private Iterable<Div> getMask(LISTE<Div> res, int tileI, int aMask){
		Tile t = tiles[firstTiles[tileI]&0x0FFFF];
		while(t != null && res.hasRoom()) {
			Div d = GAME.ARMIES().division(t.divI);
			if ((d.army().bit & aMask) != 0)
				res.add(d);
			t = tiles[t.next&0x0FFFF];
		}
		return res;
	}
	
	private Div getMaskSingle(int tileI, int aMask){
		Tile t = tiles[firstTiles[tileI]&0x0FFFF];
		while(t != null) {
			Div d = GAME.ARMIES().division(t.divI);
			if ((d.army().bit & aMask) != 0)
				return d;
			t = tiles[t.next&0x0FFFF];
		}
		return null;
	}
	
	private Div get(int tileI, int aMask){
		Tile t = tiles[firstTiles[tileI]&0x0FFFF];
		while(t != null) {
			Div d = GAME.ARMIES().division(t.divI);
			if ((d.army().bit & aMask) != 0)
				return d;
			t = tiles[t.next&0x0FFFF];
		}
		return null;
	}

	void clear() {
		Arrays.fill(firstTiles, (short)0);
		for (Bitsmap2D m : soldiers)
			m.clear();
		tileNewI = 1;
	}

	
	private static final class Tile {
		
		private short next;
		private short divI;
		
	}
	

	
}
