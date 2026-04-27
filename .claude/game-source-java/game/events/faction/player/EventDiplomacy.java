package game.events.faction.player;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.Faction.FactionActivityListener;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import settlement.stats.Induvidual;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class EventDiplomacy extends EventResource{

	
	private final double dTime = TIME.secondsPerDayI();
	private int ii;
	private double timer = 1;
	
	private final War war = new War();
	private final Peace peace = new Peace();
	private final Vassal vassal = new Vassal();
	private final Stance stance = new Stance();

	private final EData[] datas = new EData[FACTIONS.MAX()];
	
	public EventDiplomacy() {
		super("DIPLOMACY");
		for (int i = 0; i < datas.length; i++)
			datas[i] = new EData();
		
		new FactionActivityListener() {
			
			@Override
			public void remove(FactionNPC f) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void add(FactionNPC f) {
				datas[f.index()].clear();
			}
		};
		
		new DIP.DipActivityListener() {
			
			@Override
			public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
				boolean w = datas[faction.index()].welcomed;
				datas[faction.index()].clear();
				datas[faction.index()].welcomed = w;
				w = datas[other.index()].welcomed;
				datas[other.index()].clear();
				datas[other.index()].welcomed = w;
				datas[other.index()].stanceMess = false;
				datas[faction.index()].stanceMess = false;
			}
		};
		
	}
	
	
	@Override
	protected void update(double ds) {
		timer -= ds*dTime*FACTIONS.NPCs().size();
		while(timer < 0) {
			timer ++;
			if (ii >= FACTIONS.NPCs().size()) {
				ii = 0;
				war.update(datas);
				peace.update();
			}
			FactionNPC fa = FACTIONS.NPCs().get(ii);
			process(fa);
			ii++;
		}
		
		
	}
	
	private void process(FactionNPC fa) {
		
		if (fa.request.has()) {
			return;
		}
		
		if (DIP.WAR().is(fa)) {
			return;
		}
		
		EData data = datas[fa.index()];
		Induvidual king = fa.court().king().roy().induvidual;
		
		if (DIP.VASSAL().is(fa) && vassal.process(fa, data)) {
			return;
		}
		
		if (stance.process(fa, king, data))
			return;

		
		
	}
	
	
	

	@Override
	protected void save(FilePutter file) {
		file.i(ii);
		file.d(timer);
		for (int i = 0; i < datas.length; i++) {
			datas[i].save(file);
		}
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		ii = file.i();
		timer = file.d();
		for (int i = 0; i < datas.length; i++) {
			datas[i].load(file);
		}
	}

	@Override
	protected void clear() {
		ii = 0;
		timer = 0;
		for (int i = 0; i < datas.length; i++) {
			datas[i].clear();
		}
	}


	public void dismissWelcome(FactionNPC f) {
		datas[f.index()].welcomed = true;
	}

	class EData implements SAVABLE{

		public boolean welcomed = false;
		public boolean warMess = false;
		public boolean vassal = false;
		public boolean stanceMess = false;
		
		@Override
		public void save(FilePutter file) {
			file.bool(welcomed);
			file.bool(warMess);
			file.bool(stanceMess);
			file.bool(vassal);
		}
		@Override
		public void load(FileGetter file) throws IOException {
			welcomed = file.bool();
			warMess = file.bool();
			stanceMess = file.bool();
			vassal = file.bool();
			
		}
		@Override
		public void clear() {
			welcomed = false;
			warMess = false;
			stanceMess = false;
			vassal = false;
		}
		
	}
	

}
