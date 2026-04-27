package settlement.entity.humanoid.ai.battle;

import game.battle.div.Div;
import init.constant.C;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.type.HTYPES;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES;
import settlement.entity.humanoid.ai.main.AISTATES.STOP;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.Equip;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

final class MarchPlan extends AIPLAN.PLANRES{

	public MarchPlan(String key) {
		super(key);
	}

	private static CharSequence ¤¤Reforming = "¤Reforming";
	private static CharSequence ¤¤Waiting = "¤Waiting for orders";
	private static CharSequence ¤¤Breaking = "¤Breaking Formation";
	private static CharSequence ¤¤Firing = "¤Firing";
	private final int cutDistance = 32;
	
	static {
		D.ts(MarchPlan.class);
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		
		if (a.indu().player() && d.plan() != this) {
			AISubActivation s = fetchGear.set(a, d);
			if (s != null)
				return s;
		}
		
		return retry(a, d);
		
	}
	
	private AISubActivation retry(Humanoid a, AIManager d) {
		return waitForDeploy.set(a, d);
	}
	
	private AISubActivation retry2(Humanoid a, AIManager d) {

		Div div = a.division();
		COORDINATE c = a.physics.tileC();
		
		
		if (!div.reporter.posHas(a)) {
			return pathToDestination.set(a, d);
		}
		
		if (isInPosition(div.reporter.getPixel(a), a, d)) {
			return beBraced.set(a, d);
		}
		
		COORDINATE de = div.reporter.getTile(a);
		if (COORDINATE.tileDistance(c, de) < cutDistance) {
			return cutToPosition.set(a, d);
		}
		
		if (c != null && SETT.PATH().isInTheNeighbourhood(de.x(), de.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
			return pathToPosition.set(a, d);
		}
		
		return pathToDestination.set(a, d);
	}
	
	private boolean conn(Humanoid a, AIManager d) {
		Div div = a.division();
		return div != null && div.settings().mustering() && div.deployed() > 0;
	}
	
	private boolean isInPosition(COORDINATE dest, Humanoid a, AIManager d) {
		return dest.isSameAs(a.physics.body().cX(), a.physics.body().cY());
	}
	

	
	private Resumer waitForDeploy = new Resumer(¤¤Reforming) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return res(a, d);
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!should(a, d))
				return null;
			Div div = a.division();
			if (div.deployed() == 0) {
				if (d.planByte1++ > 8)
					return null;
				return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
			}
				
			AISubActivation s = retry2(a, d);
			
			if (s == null) {
				div.reporter.reportReachable(a, false);
				if (d.planByte1++ > 8)
					return null;
				return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
			}
			return s;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			Div div = a.division();
			if (div == null || !div.settings().mustering())
				return false;
			
			
			if (!AI.modules().battle.moduleCanContinue(a, d)) {
				return false;
			}
			return true;
		}
		
		private boolean should(Humanoid a, AIManager d) {
			Div div = a.division();
			if (div == null || !div.settings().mustering())
				return false;
			
			
			if (!AI.modules().battle.moduleCanContinue(a, d)) {
				return false;
			}
			return true;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	};
	
	private final Resumer cutToPosition = new Resumer(¤¤Reforming) {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (a.indu().hType().player && a.division() != null) {
				DIR dir = DIR.get(a.tc(), a.division().reporter.getTile(a));
				if (dir.x() != 0 || dir.y() != 0) {
					if (SETT.PATH().coster.player.getCost(a.tc().x(), a.tc().y(), a.tc().x()+dir.x(), a.tc().y()+dir.y()) < 0)
						return pathToDestination.set(a, d);
				}
			}
			
			
			return AI.modules().battle.subCutTo.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			Div div = a.division();
			if (!div.reporter.posHas(a))
				return pathToDestination.set(a, d);
			if (BattleUtil.isInPosition(div.reporter.getPixel(a), a, d)) {
				return arriveInFormation.set(a, d);
			}
			return AI.modules().battle.subCutTo.activate(a, d);
		}
		
		@Override
		public AISubActivation resFailed(Humanoid a, AIManager d, HEvent event) {
			if (event == HEvent.COLLISION_TILE && a.division() != null && a.division().reporter.posHas(a)) {
				return pathToPosition.set(a, d);
			}
			return null;
		};
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.pollReady(a, d, e);
		}
	};
	
	private final Resumer pathToDestination = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple("MarchPath") {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1) {
					Div div = a.division();
					if (div.settings().running)
						return AI.STATES().RUN2.path(a, d);
					else
						return AI.STATES().WALK2.path(a, d);
				}

				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}

		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			Div div = a.division();
			if (div.deployed() == 0)
				return waitForSpot.set(a, d);
			if (!div.reporter.posHas(a))
				return null;
			
			COORDINATE c = div.reporter.getDestTile(a);
			int sx = c.x();
			int sy = c.y();
			COORDINATE dest = SETT.PATH().finders.arround.find(sx, sy, 0, 15);
			if (dest == null)
				return null;
			d.planByte1 = 0;
			d.path.request(a.physics.tileC(), dest.x(), dest.y());
			if (!d.path.isSuccessful())
				return null;
			
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!d.path.isSuccessful())
				return null;
			if (d.path.isDest()) {
				return waitInDestination.set(a, d);
			}
			d.path.setNext();
			Div div = a.division();
			if (!div.reporter.posHas(a))
				return null;
			COORDINATE c = div.reporter.getDestTile(a);
			int tx = c.x();
			int ty = c.y();
			if (COORDINATE.tileDistance(tx, ty, d.path.destX(), d.path.destY()) > 15) {
				return retry(a, d);
			}
			d.planByte1 ++;
			if (d.planByte1 == 5) {
				c = div.reporter.getTile(a);
				if (c != null && SETT.PATH().isInTheNeighbourhood(c.x(), c.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
					
					a.speed.magnitudeInit(0);
					return pathToPosition.set(a, d);
				}
				d.planByte1 = 0;
			}
			
			return sub.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer pathToPosition = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple("MarchCut") {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1)
					return AI.STATES().RUN2.path(a, d);
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			Div div = a.division();
			
			if (!div.reporter.posHas(a))
				return pathToDestination.set(a, d);
			COORDINATE c = div.reporter.getTile(a);
			d.planByte1 = (byte) (0x0FF&div.order().dest.setI());
			d.planByte2 = 0;
			d.path.request(a.physics.tileC(), c.x(), c.y());
			if (!d.path.isSuccessful())
				return waitForSpot.set(a, d);
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!d.path.isSuccessful())
				return null;
			Div div = a.division();
			if (d.path.isDest()) {
				//fix here
				a.speed.magnitudeInit(0);
				if (d.planByte2 == 5 && d.planByte1 != (byte) (0x0FF&div.order().dest.setI())) {
					a.speed.magnitudeInit(0);
					return set(a, d);
				}
				if (!div.reporter.posHas(a))
					return waitForSpot.set(a, d);
				return cutToPosition.set(a, d);
			}
			d.path.setNext();
			d.planByte2++;
			
			if (d.planByte2 == 5 && d.planByte1 != (byte) (0x0FF&div.order().dest.setI())) {
				d.planByte2 = 0;
				a.speed.magnitudeInit(0);
				return set(a, d);
			}
			
			return sub.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT) {
				d.interrupt(a, e);
				d.overwrite(a, AI.modules().battle.subSoft.initCoo(d, a, e.other, d.path.x()*C.TILE_SIZE + C.TILE_SIZEH, d.path.y()*C.TILE_SIZE + C.TILE_SIZEH));
			}
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer arriveInFormation = new Resumer(¤¤Reforming) {
		
		private final AISUB sub = new AISUB.Simple("MarchStand") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte == 1)
					return AI.STATES().STAND.activate(a, d, 0.1);
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return InterBattle.listener.event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return InterBattle.listener.poll(a, d, e);
			}
		};
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (5+RND.rInt(5));
			return sub.activate(a, d);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Div div = a.division();
			
			if (!div.reporter.posHas(a))
				return null;
			COORDINATE dest = div.reporter.getPixel(a);
			if (!isInPosition(dest, a, d)) {
				return retry(a, d);
			}
			
			if (d.planByte1 > 0) {
				d.planByte1--;
			}else if (d.planByte1 == 0) {
				return beBraced.set(a, d);
			}
			return sub.activate(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final STOP stand = new AISTATES.STOP("MP_STAND", HSprites.SWORD_STAND_SWAY);
	
	private final Resumer beBraced = new Resumer(¤¤Waiting) {
		
		
		

		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.subByte = 0;
			return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!conn(a, d)) {
				return null;
			}
			
			byte ss = d.subByte++;
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			if (a.division().settings().shouldbreak) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					int dx = a.tc().x()+DIR.ORTHO.get(i).x();
					int dy = a.tc().y()+DIR.ORTHO.get(i).y();
					if (AI.modules().battle.tile.shouldattackTile(d, a, dx, dy)) {
						return d.resumeOtherPlan(a, AI.modules().battle.tile.init(d, a, dx, dy));
					}
				}

			}
			
			if (!a.physics.isWithinTile()) {
				
			}
			
			
			if (shouldFire(a, d))
				return fire.set(a, d);
			
			d.subByte = ss;
			
			if (d.subByte < 50) {
				
				if (!con(a, d)) {
					return retry(a, d);
				}
				Div div = a.division();

				
				if (!div.reporter.posHas(a)) {
					return retry(a, d);
				}
				
				COORDINATE dest = div.reporter.getPixel(a);
				
				if (!isInPosition(dest, a, d)) {
					return retry(a, d);
				}
				
				DIR dir = div.position().dir(a.divSpot());
				if (dir == null || !div.status().threatAt(dir, div)) {
					dir = a.division().dir();
				}
				
				if (RND.oneIn(30)) {
					a.speed.turn2(dir.next(RND.rInt0(1)));
					return AI.SUBS().single.activate(a, d, stand.activate(a, d, 1.0 + RND.rFloat(0.5)));
				}else {
					a.speed.turn2(dir);
				}
				
				
				return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
			}
			return retry(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.pollReady(a, d, e);
		}
	};
	
	private boolean shouldFire(Humanoid a, AIManager d) {
		return conn(a, d) && a.division().settings().shouldFire() && a.division().settings().ammo().ammoD(a.division()) > 0;
	}
	
	private final Resumer fire = new Resumer(¤¤Firing) {
		
		private double drawInter(Humanoid a, AIManager d) {
			return a.division().settings().ammo().drawInter(a.division());
		}

		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return res(a, d);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!shouldFire(a, d))
				return retry(a, d);
			
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			Div div = a.division();
			
			if (!div.reporter.posHas(a)) {
				return retry(a, d);
			}
			COORDINATE dest = div.reporter.getPixel(a);
			if (!isInPosition(dest, a, d)) {
				return retry(a, d);
			}
			Trajectory t = div.traj(a);
			
			if (t == null) {
				
				DIR dir = div.position().dir(a.divSpot());
				if (dir == null || !div.status().threatAt(dir, div)) {
					dir = a.division().dir();
				}
				
				if (RND.oneIn(30)) {
					a.speed.turn2(dir.next(RND.rInt0(1)));
					return AI.SUBS().single.activate(a, d, stand.activate(a, d, 1.0 + RND.rFloat(0.5)));
				}else {
					a.speed.turn2(dir);
					return AI.SUBS().single.activate(a, d, stand.activate(a, d, 0.5));
				}
			}
			a.speed.setDirCurrent(DIR.get(t.vx(), t.vy()));
			if (d.planByte1 == 1 && drawInter(a, d) >= 0.75) {
				d.planByte1 = 0;
				
				
				EquipRange rr = a.division().settings().ammo();
				rr.launch(a, t);
				return AI.SUBS().single.activate(a, d,  AI.STATES().anima.archer2, 0.1+RND.rFloat()*0.1);
			}else {
				if (drawInter(a, d) < 0.75)
					d.planByte1 = 1;
				if (drawInter(a, d) > 0.5)
					return AI.SUBS().single.activate(a, d,  AI.STATES().anima.archer2, 0.1+RND.rFloat()*0.1);
				return AI.SUBS().single.activate(a, d,  AI.STATES().anima.archer1, 0.1+RND.rFloat()*0.1);
			}
				
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer waitForSpot = new Resumer(¤¤Waiting) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
	private final Resumer waitInDestination = new Resumer(¤¤Waiting) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.oneIn(5))
				a.speed.turnRandom();
			return AI.SUBS().STAND.activateTime(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {

			AISubActivation s = tryMopup(a, d);
			if (s != null)
				return s;
			
			Div div = a.division();
			COORDINATE c = div.reporter.getDestTile(a);
			int tx = c.x();
			int ty = c.y();
			if (COORDINATE.tileDistance(tx, ty, d.path.destX(), d.path.destY()) > 15) {
				return null;
			}
			
			if (RND.oneIn(5) && div.reporter.posHas(a)) {
				c = div.reporter.getTile(a);
				if (c != null && SETT.PATH().isInTheNeighbourhood(c.x(), c.y(), a.physics.tileC().x(), a.physics.tileC().y())) {
					return pathToPosition.set(a, d);
				}
			}
			
			return setAction(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return conn(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	

	
	private final Resumer fetchGear = new Resumer("Getting Battlegear") {
		
		final RBITImp bi = new RBITImp();
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			bi.clear();
			Div div = STATS.BATTLE().DIV.get(a);
			if (div == null)
				return null;
			
			for (Equip e : STATS.EQUIP().BATTLE_ALL()) {
				if (e.stat().indu().get(a.indu()) < e.target(a.indu())) {
					bi.or(e.resource(a.indu()));
				}
			}
			
			if (bi.isClear())
				return null;
			
			return AI.SUBS().walkTo.resource(a, d, bi, Integer.MAX_VALUE);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			RESOURCE r = d.resourceCarried();
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				if (e.stat().indu().get(a.indu()) < e.target(a.indu()) && e.resource(a.indu()) == r) {
					e.inc(a.indu(), 1);
					d.resourceCarriedSet(null);
					break;
				}
			}
			AISubActivation s = set(a, d);
			return s;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return STATS.BATTLE().DIV.get(a) != null;
		}
		
		@Override
		public 
		void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private AISubActivation tryMopup(Humanoid a, AIManager d) {
		
		if (!a.division().settings().moppingUp())
			return null;
		
		if (STATS.POP().pop(HTYPES.ENEMY()) == 0 && STATS.POP().pop(HTYPES.RIOTER()) == 0)
			return null;
		
		Div div = STATS.BATTLE().DIV.get(a);
		Humanoid h = div.targets.getNextTarget();
		if (h != null) {
			AISubActivation s = AI.SUBS().walkTo.follow(a, d, h, true, (byte)10);
			
			h.target(2);
			if (s != null) {
				mopup.set(a, d);
				return s;
			}
		}
		return null;
	}
	
	private final Resumer mopup = new Resumer(¤¤Breaking) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return tryMopup(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			Div div = a.division();
			return div != null && div.settings().mustering() && div.settings().moppingUp();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT)
				return super.event(a, d, e);
			return InterBattle.listener.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return InterBattle.listener.poll(a, d, e);
		}
	};
	
}
