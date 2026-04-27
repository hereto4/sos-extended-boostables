package init.type;

import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;
import util.text.D;

public final class CAUSE_LEAVES {

	{
		D.gInit(this);
	}
	
	private final ArrayListGrower<CAUSE_LEAVE> all = new ArrayListGrower<>();
	private final ArrayListGrower<CAUSE_LEAVE> deaths = new ArrayListGrower<>();
	
	private final CAUSE_LEAVE ARMY = new CAUSE_LEAVE(all, deaths,
			"ARMY",
			D.g("Army-Duty"),
			D.g("Army-Duties"),
			D.g("ArmyDutyD", "Subjects that have left your city to join distant armies."),
			false,
			true,
			false
			); 
	
	private final CAUSE_LEAVE EMMIGRATED = new CAUSE_LEAVE(all, deaths,
			"EMMIGRATED",
			D.g("Emigrated"),
			D.g("Emigration"),
			D.g("EmmigratedD", "Subjects that have left your city."),
			false,
			true,
			false
			); 
	
	private final CAUSE_LEAVE STARVED = new CAUSE_LEAVE(all, deaths,
			"STARVED",
			D.g("Starved"),
			D.g("Starvation"),
			D.g("StarvedD", "Subjects that have starved to death from lack of food."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE SACRIFICED = new CAUSE_LEAVE(all, deaths,
			"SACRIFICED",
			D.g("Sacrificed"),
			D.g("Sacrifices"),
			D.g("SacrificedD", "Subjects that have been sacrificed to the gods."),
			true,
			true,
			true
			); 
	
	private final CAUSE_LEAVE SLAYED = new CAUSE_LEAVE(all, deaths,
			"SLAYED",
			D.g("Slain"),
			D.g("Slaying"),
			D.g("SlainD", "Subjects that have fallen in battle."),
			true,
			true,
			true
			); 

	private final CAUSE_LEAVE ANIMAL = new CAUSE_LEAVE(all, deaths,
			"ANIMAL",
			D.g("Mauled"),
			D.g("Mauling"),
			D.g("AnimalsD", "Subjects that have been slain by wild beasts."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE AGE = new CAUSE_LEAVE(all, deaths,
			"AGE",
			D.g("Natural"),
			D.g("Natural Causes"),
			D.g("AgeD", "Subjects that have died naturally from old age."),
			true,
			true,
			true
			); 

	private final CAUSE_LEAVE ACCIDENT = new CAUSE_LEAVE(all, deaths,
			"ACCIDENT",
			D.g("Accident"),
			D.g("Accidents"),
			D.g("AccidentD", "Subjects that have died from accidents."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE HEAT = new CAUSE_LEAVE(all, deaths,
			"HEAT",
			D.g("Heat"),
			D.g("Heat Exposure"),
			D.g("HeatD", "Subjects that have died from heat exposure. Build bodies of water or wells to prevent this."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE COLD = new CAUSE_LEAVE(all, deaths,
			"COLD",
			D.g("Cold"),
			D.g("Cold Exposure"),
			D.g("ColdD", "Subjects that have frozen to death. Build hearths to avoid."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE MURDER = new CAUSE_LEAVE(all, deaths,
			"MURDER",
			D.g("Murdered"),
			D.g("Murders"),
			D.g("MurderD", "Subjects that have been murdered."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE DISEASE = new CAUSE_LEAVE(all, deaths,
			"DISEASE",
			D.g("Disease"),
			D.g("Diseases"),
			D.g("DiseaseD", "Subjects that have died from diseases."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE EXECUTED = new CAUSE_LEAVE(all, deaths,
			"EXECUTED",
			D.g("Executed"),
			D.g("Executions"),
			D.g("ExecutedD", "Subjects that have been executed."),
			true,
			false,
			true
			);
	
	private final CAUSE_LEAVE PUNISHED = new CAUSE_LEAVE(all, deaths,
			"PUNISHED",
			D.g("Punished"),
			D.g("Punishment"),
			D.g("PunishmentD", "Subjects that have been turned into prisoners."),
			true,
			true,
			true
			); 
	
	private final CAUSE_LEAVE DROWNED = new CAUSE_LEAVE(all, deaths,
			"DROWNED",
			D.g("Drowned"),
			D.g("Drownings"),
			D.g("DrownedD", "Subjects that have drowned."),
			true,
			true,
			false
			); 
	
	private final CAUSE_LEAVE DESERTED = new CAUSE_LEAVE(all, deaths,
			"DESERTED",
			D.g("Deserted"),
			D.g("Desertion"),
			D.g("DesertedD", "Soldiers that have deserted."),
			true,
			true,
			false
			); 
	
	private final CAUSE_LEAVE EXILED = new CAUSE_LEAVE(all, deaths,
			"EXILED",
			D.g("Exiled"),
			D.g("Exile"),
			D.g("ExileD", "People condemned to exile."),
			true,
			true,
			false
			); 
	
	private final CAUSE_LEAVE BRAWL = new CAUSE_LEAVE(all, deaths,
			"BRAWL",
			D.g("Brawl"),
			D.g("Brawls"),
			D.g("BrawlD", "Subjects that have died from a brawl that has gone too far. Try separating the homes of species that hate each other."),
			true,
			false,
			true
			); 
	
	private final CAUSE_LEAVE OTHER = new CAUSE_LEAVE(all, deaths,
			"OTHER",
			D.g("Other"),
			D.g("Others"),
			D.g("OtherD", "Other causes."),
			true,
			true,
			false
			); 
	
	private final CAUSE_LEAVE INSANITY = new CAUSE_LEAVE(all, deaths,
			"INSANE",
			D.g("Insane"),
			D.g("Insanity"),
			D.g("InsanityD", "Subjects that have become insane."),
			false,
			true,
			false
			); 
	private final CAUSE_LEAVE SOLD = new CAUSE_LEAVE(all, deaths,
			"SOLD",
			D.g("Sold"),
			D.g("Sold"),
			D.g("SoldD", "Subjects that have been sold."),
			false,
			true,
			false
			); 

	public final RMAPS<CAUSE_LEAVE> map = new RMAPS<>("DEATH_CAUSE", all);
	private static CAUSE_LEAVES self;
	
	
	CAUSE_LEAVES(){
		self = this;
		Json json = new Json(PATHS.CONFIG().get("LEAVE_CAUSE"));
		for (CAUSE_LEAVE l : all) {
			l.defAgony = json.d(l.key, 0, 10);
		}
		
	}
	
	public static LIST<CAUSE_LEAVE> ALL(){
		return self.all;
	}
	
	public static final RMAPS<CAUSE_LEAVE> MAP(){
		return self.map;
	}
	
	public static LIST<CAUSE_LEAVE> DEATHS(){
		return self.deaths;
	}


	public static CAUSE_LEAVE ARMY() {
		return self.ARMY;
	}

	public static CAUSE_LEAVE EMMIGRATED() {
		return self.EMMIGRATED;
	}

	public static CAUSE_LEAVE STARVED() {
		return self.STARVED;
	}

	public static CAUSE_LEAVE SACRIFICED() {
		return self.SACRIFICED;
	}

	public static CAUSE_LEAVE SLAYED() {
		return self.SLAYED;
	}

	public static CAUSE_LEAVE ANIMAL() {
		return self.ANIMAL;
	}

	public static CAUSE_LEAVE AGE() {
		return self.AGE;
	}

	public static CAUSE_LEAVE getAccident() {
		return self.ACCIDENT;
	}

	public static CAUSE_LEAVE HEAT() {
		return self.HEAT;
	}

	public static CAUSE_LEAVE COLD() {
		return self.COLD;
	}

	public static CAUSE_LEAVE MURDER() {
		return self.MURDER;
	}

	public static CAUSE_LEAVE DISEASE() {
		return self.DISEASE;
	}

	public static CAUSE_LEAVE EXECUTED() {
		return self.EXECUTED;
	}

	public static CAUSE_LEAVE PUNISHED() {
		return self.PUNISHED;
	}

	public static CAUSE_LEAVE DROWNED() {
		return self.DROWNED;
	}

	public static CAUSE_LEAVE DESERTED() {
		return self.DESERTED;
	}

	public static CAUSE_LEAVE EXILED() {
		return self.EXILED;
	}

	public static CAUSE_LEAVE BRAWL() {
		return self.BRAWL;
	}

	public static CAUSE_LEAVE OTHER() {
		return self.OTHER;
	}
	
	public static CAUSE_LEAVE INSAVITY() {
		return self.INSANITY;
	}
	
	public static CAUSE_LEAVE SOLD() {
		return self.SOLD;
	}
	
}
