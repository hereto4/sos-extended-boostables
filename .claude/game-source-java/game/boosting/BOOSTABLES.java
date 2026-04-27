package game.boosting;

import java.io.IOException;

import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.keymap.MAPPED;
import util.keymap.RMAP;
import util.text.D;
import util.text.Dic;

public class BOOSTABLES {

	static BOOSTABLES self;
	
	{
		D.gInit(this);
	}
	
	private final Physics physics = new Physics();
	private final Battle battle  = new Battle();
	private final Behaviour behaviour = new Behaviour();
	private final Activity activities = new Activity();
	private final Civic civics = new Civic();
	private final Noble noble = new Noble();
	
	public final BoostableCat ROOMS = new BoostableCollection("ROOM", Dic.¤¤Buildings);
	public final LIST<BoostableCat> collections = new ArrayList<>(physics, behaviour, activities, battle, civics, ROOMS, noble);
	
	
	private BOOSTABLES() throws IOException{
		self = this;
		ResFolder f = PATHS.STATS().folder("boost");
		KeyMap<BoostableCat> map = new KeyMap<BoostableCat>();
		for (BoostableCat c : collections)
			map.put(c.prefix.substring(0, c.prefix.length()-1), c);
		
		for (String key : f.init.getFiles()) {
			
			Json j = new Json(f.init.get(key));
			String ck = j.value("CATEGORY");
			if (!map.containsKey(ck)) {
				LOG.err("No CATEGORY named: " + ck + " available: " + map.keysString());
				continue;
			}
			BoostableCat c = map.get(ck);
			double base = j.d("BASE_VALUE", -100000, 100000);
			Json t = new Json(f.text.get(key));
			SPRITE icon = UI.icons().get(j);
			
			BOOSTING.push(key, base, t.text("NAME"), t.text("DESC"), icon, c);
			
			
		}
	}
	
	static void init() throws IOException {
		new BOOSTABLES();
		
	}
	
	public static Physics PHYSICS() {
		return self.physics;
	}
	
	public static Battle BATTLE() {
		return self.battle;
	}
	
	public static Behaviour BEHAVIOUR() {
		return self.behaviour;
	}
	
	public static Activity ACTIVITY() {
		return self.activities;
	}
	
	public static Noble NOBLE() {
		return self.noble;
	}
	
	public static Civic CIVICS() {
		return self.civics;
	}
	
	public static BoostableCat ROOMS() {
		return self.ROOMS;
	}
	
	public static LIST<BoostableCat> colls(){
		return self.collections;
	}
	

	
	public static final class Physics extends BoostableCollection{
		
		Physics(){
			super("PHYSICS", D.g("Physics"));
		}
		
		public final Boostable MASS = make("MASS", 80.0, UI.icons().s.law,
				D.g("PHYSICS_MASS", "Weight"),
				D.g("PHYSICS_MASS_D", "The Weight of a subject."));

			public final Boostable STAMINA = make("STAMINA", 1.0, UI.icons().s.heat,
				D.g("PHYSICS_STAMINA", "Stamina"),
				D.g("PHYSICS_STAMINA_D", "How long a subject can walk or run before needing to rest."));

			public final Boostable SPEED = make("SPEED", 4.5, UI.icons().s.arrow_right,
				D.g("PHYSICS_SPEED", "Speed"),
				D.g("PHYSICS_SPEED_D", "The speed of a subject, expressed in tiles per second."));

			public final Boostable ACCELERATION = make("ACCELERATION", 3.0, UI.icons().s.speed,
				D.g("PHYSICS_ACCELERATION", "Acceleration"),
				D.g("PHYSICS_ACCELERATION_D", "How fast a subject speeds up."));

			public final Boostable HEALTH = make("HEALTH", 1.0, UI.icons().s.plusBig,
				D.g("PHYSICS_HEALTH", "Health"),
				D.g("PHYSICS_HEALTH_D", "General health of subject. The higher health, the less likely a subject is to contract disease. Low values (below 1), might lead to serious outbreaks and should be top priority to fix."));

			public final Boostable DEATH_AGE = make("DEATH_AGE", 100.0, UI.icons().s.death,
				D.g("PHYSICS_DEATH_AGE", "Lifespan"),
				D.g("PHYSICS_DEATH_AGE_D", "The maximum amount of years a subject can live for."));

			public final Boostable RESISTANCE_HOT = make("RESISTANCE_HOT", 0.5, UI.icons().s.heat,
				D.g("PHYSICS_RESISTANCE_HOT", "Heat Resistance"),
				D.g("PHYSICS_RESISTANCE_HOT_D", "The ability for a subject to endure hot temperatures."));

			public final Boostable RESISTANCE_COLD = make("RESISTANCE_COLD", 0.5, UI.icons().s.ice,
				D.g("PHYSICS_RESISTANCE_COLD", "Cold Resistance"),
				D.g("PHYSICS_RESISTANCE_COLD_D", "The ability for a subject to endure cold temperatures."));
			
			public final Boostable SOILING = make("SOILING", 0.125, UI.icons().s.ice,
					D.g("PHYSICS_SOILING", "Soiling"),
					D.g("PHYSICS_SOILING_D", "The rate at which a subject becomes dirty."));
			
		
	}
	
	public static final class Battle extends BoostableCollection{
		
		public final Boostable OFFENCE = make("OFFENCE_SKILL", 1, UI.icons().s.sword,
			D.g("BATTLE_OFFENCE_SKILL", "Offence"),
			D.g("BATTLE_OFFENCE_SKILL_D", "Increases the ability to attack an enemy."));
		
		public final Boostable DEFENCE = make("DEFENCE_SKILL", 1, UI.icons().s.shield,
			D.g("BATTLE_DEFENCE_SKILL", "Defence"),
			D.g("BATTLE_DEFENCE_SKILL_D", "Increases the ability to avoid attacks when attacked frontally."));
		
		public final Boostable DEXTERITY = make("DEXTERITY", 5, UI.icons().s.crossheir,
				D.g("BATTLE_DEXTERITY", "Dexterity"),
				D.g("BATTLE_DEXTERITY_D", "Ability to avoid the targets block armour when attacking."));
		
		public final Boostable PARRY = make("BLOCK", 1, UI.icons().s.crossheir,
				D.g("BATTLE_BLOCK", "Block"),
				D.g("BATTLE_BLOCK_D", "Ability to use the parry attacks and reduce damage with block armour."));
		
		public final Boostable CHARGE = make("CHARGE", 1, UI.icons().s.speed,
				D.g("BATTLE_CHARGE", "Charge"),
				D.g("BATTLE_CHARGE_D", "Adds extra attack to charge attacks."));
		
		public final Boostable FORMATION = make("FORMATION_SKILL", 0, UI.icons().s.muster,
				D.g("BATTLE_FORMATION_SKILL", "Formation"),
				D.g("BATTLE_FORMATION_SKILL_D", "Increases defence and parry when in an intact formation and attacked from the front."));
		
		public final Boostable BLUNT_ATTACK = make("BLUNT_ATTACK", 40, UI.icons().s.fist,
			D.g("BATTLE_BLUNT_DAMAGE", "Force"),
			D.g("BATTLE_BLUNT_DAMAGE_D", "The force of any attack. Force applies damage in itself, but also amplified the attack of other damage types when attacking. Force also creates knock-back that can disrupt enemy formations."));
		
		public final Boostable BLUNT_DEFENCE = make("BLUNT_DEFENCE", 40, UI.icons().s.fist,
			D.g("BATTLE_BLUNT_DEFENCE", "Force Absorbtion"),
			D.g("BATTLE_BLUNT_DEFENCE_D", "Ability to absorb damage."));
		
		public final Boostable BLUNT_DEFENCE_DIR = make("BLUNT_DEFENCE_DIR", 1, UI.icons().s.fist,
				D.g("BATTLE_BLUNT_DEFENCE_DIR", "Force Block"),
				D.g("BATTLE_BLUNT_DEFENCE_DIR_D", "Force damage absorbtion when an attack is parried."));
		
		
//		public final Boostable ATTACK_RATE = make("ATTACK_RATE", 1.0, UI.icons().s.sword,
//				D.g("ATTACK_RATE", "Attack Rate"),
//				D.g("ATTACK_RATE_D", "The rate at which a soldier performs melee attacks."));
//			
		public final Boostable MORALE = make("MORALE", 4.0, UI.icons().s.standard,
			D.g("BATTLE_MORALE", "Morale"),
			D.g("BATTLE_MORALE_D", "A soldier's morale is what determines how long, and against what odds and setbacks, it will fight for before taking flight."));
		

		public final LIST<BDamage> DAMAGES;
		public final RMAP<BDamage> DAMAGE_COLL;
		
		Battle() throws IOException{
			super("BATTLE", D.g("Battle"));
			
			
			{
				ResFolder p = PATHS.STATS().folder("damage");
				LinkedList<BDamage> pairs = new LinkedList<>();
				
				int index = 0;
				LinkedList<BDamage> da = new LinkedList<>();
				
				for (String f : p.init.getFiles()) {
					BDamage d = new BDamage(index++, this, f, new Json(p.init.get(f)), new Json(p.text.get(f)));
					pairs.add(d);
					da.add(d);
				}
				
				
				KeyMap<BDamage> map = new KeyMap<BOOSTABLES.BDamage>();
				for (BDamage pa : pairs)
					map.put(pa.key, pa);
				
				this.DAMAGES = new ArrayList<BOOSTABLES.BDamage>(pairs);
				
				DAMAGE_COLL = new RMAP<BOOSTABLES.BDamage>("DAMAGE", this.DAMAGES);
				
			}
			
			
		}

	}
	
	
	public static final class BDamage implements MAPPED{
		
		private final int index;
		public final String key;
		public Boostable attack;
		public Boostable defence;
		public Boostable defenceDir;
		public final CharSequence name;
		
		
		private BDamage(int index, BoostableCat cat, String key, Json data, Json text) throws IOException{
			this.key = key;
			name = text.text("NAME");
			attack = make(cat, key, "ATTACK", data, text);
			defence = make(cat, key, "DEFENCE", data, text);
			defenceDir = make(cat, key, "DEFENCE_DIR", data, text);
			this.index = index;
		}
		
		private Boostable make(BoostableCat cat, String key, String pp, Json data, Json text) throws IOException {
			key += "_" + pp;
			data = data.json(pp);
			
			double dd = data.d("DEFAULT_VALUE", 0, 100000);
			SPRITE icon = SPRITES.icons().get(data);
			INFO info = new INFO(text.json(pp));
			return BOOSTING.push(key, dd, info.name, info.desc, icon, cat);
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public String key() {
			return key;
		}
		
	}
	
	
	
	public static final class Behaviour extends BoostableCollection{
		
		Behaviour(){
			super("BEHAVIOUR", D.g("Behaviour"));
		}
		
		public final Boostable LAWFULNESS = make("LAWFULNESS", 1.0, UI.icons().s.law,
				D.g("BEHAVIOUR_LAWFULNESS", "Lawfulness"),
				D.g("BEHAVIOUR_LAWFULNESS_D", "A lawful citizen is one that is reluctant to commit crime."));

			public final Boostable SUBMISSION = make("SUBMISSION", 1.0, UI.icons().s.slave,
				D.g("BEHAVIOUR_SUBMISSION", "Submission"),
				D.g("BEHAVIOUR_SUBMISSION_D", "Submission is useful for slaves. Submissive slaves will be reluctant to revolt and you can mistreat them more."));

			public final Boostable LOYALTY = make("LOYALTY", 1.0, UI.icons().s.column,
				D.g("BEHAVIOUR_LOYALTY", "Loyalty"),
				D.g("BEHAVIOUR_LOYALTY_D", "Increases the Loyalty of your citizens, preventing riots and usurping."));
			
			public final Boostable HAPPI = make("HAPPINESS", 1.0, UI.icons().s.heart,
					D.g("BEHAVIOUR_HAPPINESS", "Happiness"),
					D.g("BEHAVIOUR_HAPPINESS_D", "Increases the happiness of your subjects. Happy subjects will be more loyal and submissive."));

			public final Boostable SANITY = make("SANITY", 1.0, UI.icons().s.crazy,
				D.g("BEHAVIOUR_SANITY", "Sanity"),
				D.g("BEHAVIOUR_SANITY_D", "Determines the chance a subject will become deranged during its lifetime."));
	}

	public static final class Civic extends BoostableCollection{
		
		Civic(){
			super("CIVIC", D.g("Civic"));
			
			if (false) {
				//pasifism double entry
			}
		}
		
		public final Boostable MAINTENANCE = make("MAINTENANCE", 1.0, UI.icons().s.degrade,
				D.g("CIVIC_MAINTENANCE", "Robustness"),
				D.g("CIVIC_MAINTENANCE_D", "Decreases the rate at which our buildings degrade."));

			public final Boostable SPOILAGE = make("SPOILAGE", 1.0, UI.icons().s.fly,
				D.g("CIVIC_SPOILAGE", "Conservation"),
				D.g("CIVIC_SPOILAGE_D", "Decreases the decay rate of goods."));

			public final Boostable ACCIDENT = make("ACCIDENT", 1.0, UI.icons().s.boom,
				D.g("CIVIC_ACCIDENT", "Safety"),
				D.g("CIVIC_ACCIDENT_D", "Decreases the chance of work related accidents."));

			public final Boostable FURNITURE = make("FURNITURE", 1.0, UI.icons().s.bed,
				D.g("CIVIC_FURNITURE", "Furnishing"),
				D.g("CIVIC_FURNITURE_D", "Decreases the rate at which subjects use up the furniture of their homes."));

			public final Boostable RAID_SECURITY = make("RAIDING", 1.0, UI.icons().s.headspike,
				D.g("CIVIC_RAIDING", "Raid Security"),
				D.g("CIVIC_RAIDING_D", "Determines the chances of us being raided"));
			
			public final Boostable DEFALTION = make("DEFLATION", 1, UI.icons().s.money,
					D.g("Deflation"), 
					D.g("deflationD", "Decreases the inflation of your treasury, allowing you to save up more."));
			
			public final Boostable IMMIGRATION = make("IMMIGRATION", 1, UI.icons().s.human,
					D.g("Immigration"),
					D.g("ImmigrationD", "Increases the replenishment and capacity of the immigration pool.")
					);

			public final Boostable INNOVATION = make("INNOVATION", 0.0, UI.icons().s.vial,
					D.g("INNOVATION", "Innovation"),
					D.g("INNOVATION_D", "Used for technologies."));
				
			public final Boostable DIPLOMACY = make("DIPLOMACY", 0.0, UI.icons().s.flag,
					D.g("EMISSARY", "Emissary Points"),
					D.g("EMISSARY_D", "Used to manipulate opinions of factions."));
		
			public final Boostable LANDING = make("LANDING", 0.0, UI.icons().s.arrowUp,
					D.g("START_LANDING", "Settle"),
					D.g("START_LANDING_D", "Increases your starting subjects and resources."));
			
			public final Boostable LAW = make("LAW", 0.0, UI.icons().s.law,
					D.g("CIVIC_LAW", "Law"),
					D.g("CIVIC_LAW_D", "The law of your city."));
			
			public final Boostable GOV = make("GOV", 5, UI.icons().m.gov,
					D.g("CIVIC_GOV", "Gov Points"),
					D.g("CIVIC_GOV_D", "The main currency to build your realm with. Gained by assigning nobles to government duties."));
			
			public final Boostable bOpinion = make("OPINION", 1.5, UI.icons().s.soso,
					D.g("CIVIC_OPINION", "Opinion"),
					D.g("CIVIC_OPINION_D", "Determines the opinion of other factions."));
			
			public final Boostable PASIFISM = make("PASIFISM", 1, UI.icons().s.sword,
					D.g("CIVIC_PASIFISM", "Pacifism"),
					D.g("CIVIC_PASIFISM_D", "Determines the aggression of other factions towards you."));
			
			public final Boostable TRADE_FEE = make("TRADE_FEE", 1, UI.icons().s.money,
					D.g("CIVIC_TRADE_FEE", "Pacifism"),
					D.g("CIVIC_TRADE_FEE", "Determines the aggression of other factions towards you."));
		
	}
	
	public static final class Activity extends BoostableCollection{
		
		Activity(){
			super("ACTIVITY", D.g("Activities"));
		}
		
		public final Boostable MOURN = make("MOURN", 1.0, UI.icons().s.death,
				D.g("ACTIVITY_MOURN", "Mourning"),
				D.g("ACTIVITY_MOURN_D", "Sentimentality for the dead. How often a subject will visit a grave and mourn."));
		
		public final Boostable PUNISHMENT = make("PUNISHMENT", 1.0, UI.icons().s.law,
				D.g("ACTIVITY_PUNISHMENT", "Punishment"),
				D.g("ACTIVITY_PUNISHMENT_D", "How often a subject wants to see a punishment such as an execution."));
		
		public final Boostable JUDGE = make("JUDGE", 1.0, UI.icons().s.honor,
				D.g("ACTIVITY_JUDGE", "Judgement"),
				D.g("ACTIVITY_JUDGE_D", "How often a subject wants to visit a court."));
		
		public final Boostable SOCIAL = make("SOCIAL", 1.0, UI.icons().s.handOpen,
				D.g("ACTIVITY_SOCIAL", "Social"),
				D.g("ACTIVITY_SOCIAL_D", "How often a subject wants to socialize with others."));
		
	}
	
	public static final class Noble extends BoostableCollection{
		
		Noble(){
			super("NOBLE", D.g("Personality"));
		}
		
		public final Boostable AGRESSION = make("AGRRESSION", 1.0, UI.icons().s.sword,
				D.g("NOBLE_AGRRESSION", "Aggression"),
				D.g("NOBLE_AGRRESSION_D", "How much war is liked."));
		
		public final Boostable PRIDE = make("PRIDE", 1.0, UI.icons().s.law,
				D.g("NOBLE_PRIDE", "Pride"),
				D.g("NOBLE_PRIDE_D", "Prideful people put great value in flattery and gifts."));
		
		public final Boostable HONOUR = make("HONOUR", 1.0, UI.icons().s.honor,
				D.g("NOBLE_HONOR", "Honour"),
				D.g("NOBLE_HONOR_D", "The value put in pledges and agreements."));
		
		public final Boostable MERCY = make("MERCY", 1.0, UI.icons().s.handOpen,
				D.g("NOBLE_MERCY", "Mercy"),
				D.g("NOBLE_MERCY_D", "The inclination towards mercy as opposed to cruelty."));
		
		public final Boostable COMPETANCE = make("COMPETENCE", 1.0, UI.icons().s.cog,
				D.g("NOBLE_COMPETENCE", "Competence"),
				D.g("NOBLE_COMPETANCE_D", "The general competence."));
		
		public final Boostable TOLERANCE = make("TOLERANCE", 1.0, UI.icons().s.tolerence,
				D.g("NOBLE_TOLERANCE", "Tolerance"),
				D.g("NOBLE_TOLERANCE_D", "Tolerance to new and different things."));
		
	}
	
	private static class BoostableCollection extends BoostableCat{


		BoostableCollection(String key, CharSequence name){
			super(key + "_", name, "", TYPE_SETT, UI.icons().s.house);
		}
		
		Boostable make(String key, double vv, Icon icon, CharSequence name, CharSequence desc) {
			Boostable b = BOOSTING.push(key, vv, name, desc, icon, this);
			return b;
		}

	}
	
}
