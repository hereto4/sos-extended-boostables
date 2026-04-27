package world.map.regions;

import java.io.IOException;

import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.Faction;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.INDEXED;
import world.WORLD;
import world.region.RD;
import world.region.Realm;

public class Region implements MAP_BOOLEAN, BOOSTABLE_O, INDEXED{

	private final short index;
	public final RegionInfo info = new RegionInfo();
	
	Region(int index) {
		this.index = (short) index;
		clear();
	}

	@Override
	public int index() {
		return index;
	}
	
	public boolean besieged() {
		return WORLD.BATTLES().besiged(this);
	}
	
	void save (FilePutter f) {
		info.save(f);
	}
	
	void load (FileGetter f) throws IOException {
		info.load(f);
	}
	
	void clear(){
		info.clear();
	}

	public Faction faction() {
		Realm realm = realm();
		if (realm != null)
			return realm.faction();
		return null;
	}

	public Realm realm() {
		return RD.REALM(this);
	}
	
	public COLOR color() {
		if (faction() != null)
			return faction().banner().colorBG();
		return COLOR.WHITE65;
	}
	
	public void fationSet(Faction f, boolean log) {
		RD.setFaction(this, f, log);
	}
	
	public void setCapitol() {
		RD.setCapitol(this);
	}
	
	public boolean capitol() {
		if (realm() == null)
			return false;
		return realm().capitol() == this;
	}
	
	public boolean canSetCapitol() {
		
		if (faction() == null)
			return false;
		
		return true;
			
	}
	
	public int cx() {
		return info.cx();
	}
	
	public int cy() {
		return info.cy();
	}
	
	public boolean active() {
		return info.area() > 0 && WORLD.REGIONS().map.is(cx(), cy());
	}
	
	@Override
	public String toString() {
		return info.name() + " " + cx() + " " + cy() + " " + info.area() + " " + index();
	}

	@Override
	public boolean is(int tile) {
		return WORLD.REGIONS().map.get(tile) == this;
	}

	@Override
	public boolean is(int tx, int ty) {
		return WORLD.REGIONS().map.get(tx, ty) == this;
	}

	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}
	
	public boolean isBesigeTile(int tx, int ty) {
		if (WORLD.REGIONS().centreEdgeTile().is(tx, ty)) {
			if (WORLD.PATH().map.is.is(tx, ty) && WORLD.REGIONS().centre.get(tx, ty) != this) {
			
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (WORLD.PATH().map.can(tx,  ty, d) & WORLD.REGIONS().centre.get(tx+d.x(), ty+d.y()) == this) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
}
