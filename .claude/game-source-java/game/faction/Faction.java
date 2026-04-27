package game.faction;

import java.io.IOException;

import game.GameDisposable;
import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.npc.FactionNPC;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.text.Dic;
import world.WORLD;
import world.army.AD;
import world.army.ADArmies;
import world.map.regions.Region;
import world.region.RD;
import world.region.Realm;

public abstract class Faction implements BOOSTABLE_O, INDEXED{

	boolean wasActive = false;
	public final Str name = new Str(24);
	private final int index;
	protected boolean event;
	public short eventMark;
	
	protected Faction(LISTE<Faction> all){
		index = all.add(this);
	}

	public abstract Race race();
	
	

	@Override
	public final int index() {
		return index;
	}
	
	public final Region capitolRegion() {
		return RD.REALM(this).capitol();
	}

	protected void save(FilePutter file) {
		name.save(file);
		res().save(file);
		banner().save(file);
		credits().save(file);
		slaves().save(file);
		file.bool(wasActive);
		file.bool(event);
		file.s(eventMark);
	}

	protected void load(FileGetter file) throws IOException {
		name.load(file);
		res().load(file);
		banner().load(file);
		credits().load(file);
		slaves().load(file);
		wasActive = file.bool();
		event = file.bool();
		eventMark = file.s();	
	}

	protected void clear() {
		name.clear();
		res().clear();
		banner().clear();
		credits().clear();
		slaves().clear();
		event = false;
		eventMark = 0;
	}

	protected void update(double ds) {
		res().update(ds, this);
		banner().update(ds, this);
		credits().update(ds, this);
		slaves().update(ds, this);
	}
	
	public final boolean isActive() {
		return realm().capitol() != null;
	}

	public int cx() {
		if (realm().regions() > 0)
			return realm().capitol().cx();
		else if (armies().all().size() > 0)
			return armies().all().get(0).ctx();
		return WORLD.TWIDTH()/2;
	}
	
	public int cy() {
		if (realm().regions() > 0)
			return realm().capitol().cy();
		else if (armies().all().size() > 0)
			return armies().all().get(0).cty();
		return WORLD.THEIGHT()/2;
	}
	
	public Realm realm() {
		return RD.REALM(this);
	}
	
	public abstract FACTION_IMPORTER buyer();
	
	public abstract FACTION_EXPORTER seller();
	
	public abstract FResources res();

	public abstract FBanner banner();
	
	public abstract FCredits credits();
	
	public abstract FSlaves slaves();
	
	public ADArmies armies() {
		return AD.army(this);
	}

	public abstract CharSequence rulerName();
	
//	public abstract FRuler ruler();

	public static abstract class FactionActivityListener {
		
		static final LinkedList<FactionActivityListener> all = new LinkedList<>();
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		public FactionActivityListener() {
			all.add(this);
		}
		
		public abstract void remove(FactionNPC f);
		public abstract void add(FactionNPC f);
	}
	
	@Override
		public String toString() {
			return "[" + index + "]" + name;
		}

	
//	public double power() {
//		return AD.power().get(this) + RD.MILITARY().power.getD(capitolRegion());
//	}
	

	
	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}
	
	public static CharSequence name(Faction f) {
		if (f != null)
			return f.name;
		return Dic.¤¤Rebels;
	}

	public abstract double offensivePower();
	
	public abstract int citizens(Race race);

	public boolean event() {
		return event;
	}
	
	public void eventSet(boolean e) {
		event = e;
	}
	
	
	
}
