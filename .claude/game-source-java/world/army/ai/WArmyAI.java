package world.army.ai;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import world.entity.army.WArmy;

public final class WArmyAI {

	final War war = new War();
	final Rebel rebel = new Rebel();
	boolean upAll = false;
	private ArrayList<Faction> fas = new ArrayList<>(FACTIONS.MAX());
	
	public WArmyAI(){
		new DIP.DipActivityListener() {

			@Override
			public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
				if (nn == DIP.WAR() || old == nn) {
					if (!fas.contains(faction))
						fas.add(faction);
					if (!fas.contains(other))
						fas.add(other);
					
				}
			}

		};
	}
	
	public SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			war.save(file);
			rebel.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			war.load(file);
			rebel.load(file);	
		}
		
		@Override
		public void clear() {
			war.clear();
			rebel.clear();
		}
	};
	
	public void update(WArmy a) {
		rebel.updateRebel(a);
	}
	
	public void update(double ds) {
		for(Faction f : fas) {
			war.planForWar(f);
		}
		fas.clearSloppy();
		war.update(ds);
	}

	public void init(Faction f) {
		war.init(f);
	}
	


}
