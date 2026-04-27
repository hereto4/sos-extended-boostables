package settlement.battle;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.VERSION;
import game.battle.div.Div;
import game.battle.div.DivInfo;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import world.army.AD;

public final class ArmyTrainingInfo {

	private boolean dirty = false;
	private final int[] targets = new int[RACES.all().size()];
	
	final RaceDiv[] perRace = new RaceDiv[RACES.all().size()];
	private int raceU = 0;
	private boolean sendOutWithoutTraining = true;
	
	ArmyTrainingInfo(){
		for (int i = 0; i < perRace.length; i++)
			perRace[i] = new RaceDiv(RACES.all().get(i));
	}
	
	public ROOM_M_TRAINER<?> updateAndGetEmployment(Humanoid a, ROOM_M_TRAINER<?> current){
		
		Div div = updateExisting(a);
		
		if (div == null)
			return null;
		
		DivInfo in = div.info;
		
		
		if (current != null && current.employable() >= 0 && current.training().shouldTrain(a.indu(), in.training(current.training()), true)) {
			return current;
		}
		
		
		
		
		current = employmentTarget(a, div, current != null);
		if (current != null)
			return current;

		STATS.BATTLE().RECRUIT.set(a, null);
		return null;
		
	}
	
	private ROOM_M_TRAINER<?> employmentTarget(Humanoid a, Div div, boolean training){

		
		
		
		double bestV = 0;
		ROOM_M_TRAINER<?> best = null;
		DivInfo in = div.info;
		for (StatTraining tra : STATS.BATTLE().TRAINING_ALL) {
			
			double emp = tra.room.employable();
			
			
			
			if (emp >= 1) {
				if (tra.shouldTrain(a.indu(), in.training(tra), training)) {
					return tra.room;
				}else if (emp > bestV) {
					bestV = emp;
					best = tra.room;
				}
			}
			
		}
		
		if (!STATS.BATTLE().basicTraining.isMax(a.indu()))
			return best;
		return null;
	}
	
	private Div updateExisting(Humanoid a) {
		
		Div div = STATS.BATTLE().DIV.get(a);
		
		if (div != null) {
			if (!canStayInDiv(a, div, false)) {
				STATS.BATTLE().DIV.set(a, null);
				return setNew(a);
			}
			
			if (perRace[a.race().index].tryBetter(a.indu(), div)) {
				Div match = perRace[a.race().index].getMatch(a.indu());
				if (match != null && match != div) {
					STATS.BATTLE().DIV.set(a, match);
					div = match;
					
				}
			}
			return div;
		}
		
		div = STATS.BATTLE().RECRUIT.get(a);
		if (div != null) {
			
			if (!canStayInDiv(a, div, true)) {
				STATS.BATTLE().RECRUIT.set(a, null);
				return setNew(a);
			}
			
			if (STATS.BATTLE().basicTraining.isMax(a.indu())) {
				STATS.BATTLE().RECRUIT.set(a, null);
				STATS.BATTLE().DIV.set(a, div);
				return div;
			}
			return div;
		}
		
		return setNew(a);

	}
	
	public boolean shouldJoinArmy(Humanoid a) {
	
		Div div = STATS.BATTLE().DIV.get(a);
		if (div == null)
			return false;
		if (AD.cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a)) == null) {
			return false;
		}
		if (!SETT.ENTRY().points.hasAny())
			return false;
		if (sendOutWithoutTraining)
			return true;
		
		DivInfo in = div.info;
		boolean training = a.indu().hType() == HTYPES.RECRUIT();
		for (StatTraining tra : STATS.BATTLE().TRAINING_ALL) {
			
			if (tra.shouldTrain(a.indu(), in.training(tra),  training)) {
				return false;
			}
			
		}
		
		return employmentTarget(a, div, false) == null;
	}
	
	public boolean sendOutWithoutTraining() {
		return sendOutWithoutTraining;
	}
	
	public void sendOutWithoutTraining(boolean s) {
		sendOutWithoutTraining = s;
	}
	
	public void clearTargets() {
		dirty = true;
	}
	
	private void cache() {
		if (dirty) {
			Arrays.fill(targets, 0);
			for (int di = 0; di < GAME.ARMIES().player().divisions().size(); di++) {
				Div d = GAME.ARMIES().player().divisions().get(di);
				if (d.info.race() != null) {
					targets[d.info.race().index()] += d.info.men();
				}
			}
			dirty = false;
		}
	}
	
	public int targetMen(Race race) {
		cache();
		if (race == null) {
			int am = 0;
			for (int i : targets)
				am+= i;
			return am;
		}
		
		return targets[race.index];
	}
	
	public int targetMen() {
		return targetMen(null);
	}
	

	
	private boolean thereAreDivsToSignUpTo(Race race) {
		int am = (int) AD.cityDivs().total(race) + (STATS.BATTLE().DIV.stat().data(null).get(race, 0) + STATS.BATTLE().RECRUIT.stat().data(null).get(race, 0));
		return (am < targetMen(race));
	}

	
	private Div setNew(Humanoid a) {
		
		if (!thereAreDivsToSignUpTo(a.race())) {
			return null;
		}
		
		if (perRace[a.race().index].has(a.indu())) {
			Div match = perRace[a.race().index].getMatch(a.indu());
			if (match != null) {
				if (STATS.BATTLE().basicTraining.isMax(a.indu())) {
					STATS.BATTLE().DIV.set(a, match);
					return match;
				}else {
					STATS.BATTLE().RECRUIT.set(a, match);
					return match;
				}
			}
			
		}
		return null;
	}
	

	
	private boolean canStayInDiv(Humanoid a, Div div, boolean recruit) {
		DivInfo in = div.info;
//		if (STATS.BATTLE().COMBAT_EXPERIENCE.indu().get(a.indu()) < in.experience())
//			return false;
		if (a.race() != in.race())
			return false;
		if (recruit && AD.cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div) > in.men())
			return false;
		if (!recruit && !STATS.BATTLE().basicTraining.isMax(a.indu()))
			return false;
		if (AD.cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) > in.men())
			return false;
		return true;
	}
	

	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.bool(sendOutWithoutTraining);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			if (!VERSION.versionIsBefore(70, 5))
				sendOutWithoutTraining = file.bool();
			dirty = true;			
			
			for (int i = 0; i <= Config.battle().DIVISIONS_PER_ARMY; i++)
				update();
		}
		
		@Override
		public void clear() {
			dirty = true;

			for (int i = 0; i < 4; i++) {
				GAME.ARMIES().division((short) i).info.menSet(50);
			}
			
			for (int i = 0; i <= perRace.length; i++)
				update();
		}
	};

	public void update() {

		
		if (raceU == Config.battle().DIVISIONS_PER_ARMY) {
			for (RaceDiv d : perRace)
				d.update();
			raceU = 0;
			return;
		}
		
		Div d = GAME.ARMIES().player().divisions().get(raceU);
		DivInfo in = d.info;
		perRace[in.race().index].update(d, in);
		
		raceU++;
	}
	
	private static class RaceDiv {
		
		private final Race race;
		private ArrayList<Div> divs = new ArrayList<>(16);
		
		RaceDiv(Race race){
			this.race = race;
		}
		
		public Div getMatch(Induvidual in) {
			
			return getNext();
			
		}
		
		private Div getNext() {
			while(!divs.isEmpty()) {
				Div div = divs.get(divs.size()-1);
				if (div.info.race() == race) {
					int am = div.info.men() - (AD.cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
					if (am > 0)
						return div;
					
				}
				divs.removeLast();
			}
			return null;
		}
		
		public boolean has(Induvidual i) {
			return getNext() != null;
				
		}
		
		public boolean tryBetter(Induvidual i, Div div) {
			return false;
				
		}
		
		void update() {
			divs.clearSloppy();
		}
		
		void update(Div div, DivInfo in) {
			
			if (!divs.hasRoom())
				return;
			int am = in.men() - (AD.cityDivs().get(div).men() + STATS.BATTLE().DIV.stat().div().get(div) + STATS.BATTLE().RECRUIT.inDiv(div));
			if (am > 0) {
				divs.add(div);
			}
			
		}
		
	}

	
}
