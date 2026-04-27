package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.THINGS;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES.ResumerRaw;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Tuple;
import snake2d.util.sprite.text.Str;
import util.text.D;

class PlanOddHunt {

	private static CharSequence ¤¤verb = "Hunting";
	
	private final SoundRace sound = AUDIO.race("SLAUGHTER");
	
	static{
		D.ts(PlanOddHunt.class);
	}

	final ResumerRaw stalk;
	final ResumerRaw drag_back;
	final ResumerRaw butcher;

	double progress = 0;
	
	public AISubActivation init(Humanoid a, AIManager d) {
		
		Animal prey = SETT.PATH().finders.prey.findAndReserve(a.physics.tileC(), d.path, Integer.MAX_VALUE);
		
		if (prey == null) {
			return null;
		}
		d.planObject = prey.id();
		return stalk.set(a,d);
		
	}

	protected PlanOddHunt(AIPLAN.PLANRES res) {
		stalk = new HResumer(res) {
			

			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.SCARE_ANIMAL_NOT)
					return 1;
				return super.poll(a, d, e);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_SOFT) {
					
					if (e.other instanceof Animal) {
						Animal prey = getPrey(a, d);
						Animal an = ((Animal)e.other);
						if (prey != an) {
							if (an.huntReservable()) {
								if (prey != null)
									prey.huntReserveCancel();;
								prey = an;
								prey.huntReserve();
								d.planObject = prey.id();
							}
						}
						
						if (prey == an) {
							a.speed.magnitudeInit(0);
							AISubActivation s = drag_back.trySet(a, d);
							if (s != null) {
								d.overwrite(a, s);
								return false;
							}	
						}
							
							
						
					}
				}
				return super.event(a, d, e);
			}
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation ac = AI.SUBS().walkTo.path(a, d);
				if (ac != null)
					return ac;
				can(a, d);
				return null;
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				Animal prey = getPrey(a, d);
				int dx = prey.physics.tileC().x() - a.physics.tileC().x();
				int dy = prey.physics.tileC().y() - a.physics.tileC().y();
				
				if (Math.abs(dx) + Math.abs(dy) == 1) {
					return drag_back.set(a, d);
					
				}else {
					
					AISubActivation ac = AI.SUBS().walkTo.coo(a, d, prey.physics.tileC());
					if (ac != null)
						return ac;
					can(a, d);
					return null;
				}
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return getPrey(a, d) != null;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				Animal prey  = getPrey(a, d);
				if (prey != null)
					prey.huntReserveCancel();
			}

		};
		
		drag_back = new HResumer(res) {
			

			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return getCadaver(a, d) != null;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				Cadaver c = getPrey(a, d).slaugher();
				
				if (c != null) {
					d.planObject = c.index();
					Tuple<COORDINATE, RESOURCE> coo = SETT.PATH().finders.storage.reserve(a.tc().x(), a.tc().y(), c.spec().rBit, Integer.MAX_VALUE);
					if (coo == null)
						return butcher.set(a, d);
					d.planTile.set(coo.a());
					SETT.PATH().finders.storage.cancelReservation(d.planTile, coo.b().bIndex());
					AISubActivation ac = AI.SUBS().walkTo.drag(a, d, THINGS().cadavers.draggable, c.index(), d.planTile);
					if (ac != null)
						return ac;
					return butcher.set(a, d);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return butcher.set(a, d);
			}
		};
		
		butcher = new HResumer(res) {
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Cadaver prey = getCadaver(a, d);
				
				if (prey == null) {
					can(a,d);
					return null;
				}
				a.speed.setDirCurrent(DIR.get(a.tc(), prey.ctx(), prey.cty()));
				if (prey.resHas()) {
					RESOURCE r = prey.resRemove();
					THINGS().resources.create(prey.ctx(), prey.cty(), r, 1);
					FACTIONS.player().res().inc(r, RTYPE.PRODUCED, 1);
				}
				
				if (prey.resHas()) {
					sound.rnd(a);
					return AI.SUBS().WORK_HANDS.activate(a, d, 5);
				}else {
					return null;
				}
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().WORK_HANDS.activate(a, d, 12);
			}
		};

		

		
	}



	
	private Animal getPrey(Humanoid a, AIManager d) {
		if (d.planObject == -1)
			return null;
		ENTITY e = ENTITIES().getByID(d.planObject);
		
		if (e == null || !(e instanceof Animal) || !((Animal) e).huntReserved()) {
			d.planObject = -1;
			return null;
		}
		return (Animal) e;
	}
	
	private Cadaver getCadaver(Humanoid a, AIManager d) {
		if (d.planObject == -1)
			return null;
		Cadaver e = THINGS().cadavers.getByIndex(d.planObject);
		
		if (e == null || e.isRemoved() || !e.resHas()) {
			d.planObject = -1;
			return null;
		}
		return e;
	}

	

	private abstract class HResumer extends ResumerRaw{

		public HResumer(PLANRES daddy) {
			super(daddy);
		}


		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.SCARE_ANIMAL_NOT)
				return 1;
			return super.poll(a, d, e);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			string.add(¤¤verb);
		}
		
	}
	
}
