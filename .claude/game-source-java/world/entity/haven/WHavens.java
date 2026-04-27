package world.entity.haven;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import util.info.INFO;
import util.text.D;
import world.entity.WEntityConstructor;
import world.map.regions.Region;
import world.region.RD;

public class WHavens extends WEntityConstructor<WHaven>{

	public final LIST<WHavenType> types = WHavenType.types();
	final Stack<WHaven> free = new Stack<>(64);
	private final WHavenFactionData[] factions = new WHavenFactionData[FACTIONS.MAX()];
	private final WHavenType[][] racemap = new WHavenType[RACES.all().size()][0];
	private final Player player;
	
	{
		D.t(this);
	}
	
	public final INFO info = new INFO(D.g("Havens"), D.g("HavenD", "Havens are smaller settlements that contain individuals of a specific race. They are independent, but can join a faction if their criteria are met and the faction controls the region in which they are located."));
	
	
	public WHavens(LISTE<WEntityConstructor<?>> tot) throws IOException{
		super(tot, false);
		
		for (int i = 0; i < factions.length; i++)
			factions[i] = new WHavenFactionData(this, i);
		
		for (Race race : RACES.all()) {
			int am = 0;
			for (WHavenType t : types)
				if (t.race == race) {
					am++;
				}
			racemap[race.index()] = new WHavenType[am];
			for (WHavenType t : types)
				if (t.race == race) {
					racemap[race.index][--am] = t;
				}
				
		}
	
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				setDirty(oldOwner);
				setDirty(newOwner);
			}
		};
		
		player = new Player(this);
	}

	@Override
	protected WHaven create() {
		if (!free.isEmpty()) {
			return free.pop();
		}
		return new WHaven();
	}

	@Override
	protected void update(double ds) {
		player.update(ds);
	}
	
	public WHaven create(int tx, int ty, WHavenType type, double size, CharSequence name) {
		WHaven c = create();
		c.add(tx, ty, type, size, name);
		
		if (c.added())
			return c;


		free.push(c);
		return null;
	}
	
	@Override
	protected void clear() {
		for (WHavenFactionData d : factions)
			d.dirty = true;
		player.clear();
	}
	
	@Override
	protected void save(FilePutter file) {
		player.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		player.load(file);
		for (WHavenFactionData d : factions)
			d.dirty = true;
	}
	
	public void setDirty(Faction f) {
		if (f == null)
			return;
		factions[f.index()].dirty = true;
	}
	
	public boolean available(Race race) {
		return racemap[race.index()].length > 0;
	}
	
	public int current(Faction f, Race type) {
		int am = 0;
		for (WHavenType t : racemap[type.index()])
			am += max(f, t)*player.get(f, t);
				
		return am;
	}
	
	public int max(Faction f, Race type) {
		int am = 0;
		for (WHavenType t : racemap[type.index()])
			am += max(f, t);
				
		return am;
	}
	
	public double replenishPerDay(Faction f, Race type) {
		double am = 0;
		for (WHavenType t : racemap[type.index()])
			am += replenishPerDay(f, t)*player.get(f, t);
				
		
		return am;
	}
	
	public int current(Faction f, WHavenType type) {
		factions[f.index()].init();
		return (int) (factions[f.index()].all[type.index()].pop*player.get(f, type));
	}
	
	public int max(Faction f, WHavenType type) {
		factions[f.index()].init();
		return factions[f.index()].all[type.index()].pop;
	}
	
	public double replenishPerDay(Faction f, WHavenType type) {
		factions[f.index()].init();
		return factions[f.index()].all[type.index()].replenish*player.get(f, type);
	}
	
	public int camps(Faction f, WHavenType type) {
		factions[f.index()].init();
		return factions[f.index()].all[type.index()].camps;
	}
	
	
	
}
