package game.events.faction;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.diplomacy.UIDipMess;
import world.WORLD;
import world.entity.army.WArmy;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;

public class EventFactionCollapse extends EventResource{

	private static final double dTime = TIME.secondsPerDay()*32;
	private double timer = dTime;
	private final ArrayList<Region> tmp = new ArrayList<Region>(32);
	
	private static CharSequence ¤¤title = "Realm Collapses";
	private static CharSequence ¤¤desc = "Due to internal strife, the realm of {0} has collapsed and much of its lands have been lost.";
	private static CharSequence ¤¤mess = "This might be a good time to expand our kingdom into these lawless lands without diplomatic penalty.";
	private int nextFaction;
	private double nextAm;
	
	static {
		D.ts(EventFactionCollapse.class);
	}
	
	EventFactionCollapse(){
		super("FACTION_COLLAPSE");
		clear();
	}
	
	@Override
	protected void update(double ds) {
		
		timer -= ds;
		if (timer < 0) {
			
			Faction f = FACTIONS.getByIndex(nextFaction);

			if (f.isActive() && f instanceof FactionNPC && DIP.WAR().all(f).size() == 0) {
				FactionNPC ff = (FactionNPC) f;
				if (!ff.sanctified)
					shatter((FactionNPC) f);
			}
			clear();
		}
		
		
	}

	void shatter(FactionNPC f){
		
		if (f.realm().regions() <= 1)
			return;
		int si = 1;
		if (f.realm().regions() > 2)
			si = 1 + RND.rInt(f.realm().regions()-1);
		Region start = f.realm().region(si);
		
		int am = (int) (f.realm().regions()*nextAm);
		am = CLAMP.i(am, 1, 32);
		
		tmp.clear();
		for (RegDist d : WORLD.PATH().regFinder.all(start, Treaty.FACTION, WRegSel.DUMMY())){
			if (d.reg.faction() == f && d.reg != f.capitolRegion()) {
				tmp.add(d.reg);
				am--;
				if (am <= 0)
					break;
			}
			
		}
		
		if (tmp.size() > 0) {
			Str.TMP.clear().add(¤¤desc).insert(0, f.name);
			WORLD.LOG().log(f, null, UI.icons().s.degrade, Str.TMP, f.cx(), f.cy());
			if (RD.DIST().factionHasRegionBorderingPlayer(f)) {
				new UIDipMess(¤¤title, Str.TMP.clear().add(¤¤desc).insert(0, f.name), ¤¤mess, f).send();
			}
			for (Region reg : tmp) {
				if (reg.faction() != f)
					continue;
				RD.setFaction(reg, null, true);
				for (WArmy a : WORLD.ENTITIES().armies.fill(reg)) {
					if (a.faction() == f)
						a.disband();
				}
			}
		}
		
		
	}
	
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(nextFaction);
		file.d(nextAm);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		nextFaction = file.i();
		nextAm = file.d();
	}

	@Override
	protected void clear() {
		timer = RND.rFloat()*dTime;
		nextFaction = RND.rInt(FACTIONS.MAX());
		nextAm = RND.rFloat();
	}	

}
