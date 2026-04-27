package settlement.entity.humanoid.ai.danger;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES.Resumer;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.health.hospital.ROOM_HOSPITAL;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class SubPlanSeekHospital {

	private final Resumer start;
	
	private final  ROOM_HOSPITAL b = SETT.ROOMS().HOSPITAL;
	public final SoundRace sound = AUDIO.race("SICK_MOAN");
	
	AISubActivation init(Humanoid a, AIManager d) {
		if (STATS.SERVICE().hospital.accessRequest(a))
			return start.set(a, d);
		return null;
	}
	
	public SubPlanSeekHospital(AIPLAN.PLANRES p) {
		
		Resumer rest = p.new Resumer(b.service().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (4 + RND.rInt(4));
				double liveChance = b.recoverRate(d.planTile.x(), d.planTile.y());
				if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
					if (!STATS.NEEDS().INJURIES.willDie(a.indu(), liveChance)) {
						STATS.NEEDS().INJURIES.setNonDanger(a.indu());
					}
				}
				
				return AI.SUBS().LAY.activateTime(a, d, 15);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s == null) {
					return null;
				}
				sound.rnd(a);
				double liveChance = b.recoverRate(d.planTile.x(), d.planTile.y());
				
				if (STATS.DISEASE().status(a.indu()).active && !STATS.DISEASE().diseaseIsDone(a, liveChance))
					return AI.SUBS().LAY.activateTime(a, d, 60);
				return fix(a, d);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s != null && s.findableReservedIs())
					s.consume();
			}
		};
		
		start = p.new Resumer(b.service().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = AI.SUBS().walkTo.service(a, d, b.service().finder,  b.service().radius());
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
				}
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s == null || !s.findableReservedIs()) {
					return null;
				}
				int x = d.planTile.x()*C.TILE_SIZE + C.TILE_SIZEH;
				int y = d.planTile.y()*C.TILE_SIZE + C.TILE_SIZEH;
				DIR dir = SETT.ROOMS().HOSPITAL.layCoo(d.planTile.x(), d.planTile.y());
				x += dir.x()*(C.TILE_SIZEH-2);
				y += dir.y()*(C.TILE_SIZEH-2);
				a.physics.body().moveC(x, y);
				a.speed.setDirCurrent(dir);
				return rest.set(a, d);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
	
			}
		};
		

		
	}
	
	private AISubActivation fix(Humanoid a, AIManager d) {
		FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
		
		double liveChance = b.recoverRate(d.planTile.x(), d.planTile.y());
		if ((STATS.DISEASE().status(a.indu()).active) && STATS.DISEASE().shouldDie(a) && RND.rFloat() > liveChance) {
			AIManager.dead = CAUSE_LEAVES.DISEASE();
			AIManager.deadGore = false;
			return AI.SUBS().LAY.activate(a, d);
		
		}
		
		if (STATS.NEEDS().INJURIES.willDie(a.indu(), liveChance)) {
			HumanoidResource.dead = a.lastLeaveCause() != null ? a.lastLeaveCause() : CAUSE_LEAVES.getAccident();
			AIManager.deadGore = false;
			return AI.SUBS().LAY.activate(a, d);
		}
		
		if (s != null && s.findableReservedIs()) {
			s.consume();
			for (DIR dir : DIR.ORTHO) {
				if (!SETT.PATH().solidity.is(a.tc(), dir)) {
					int x = (a.tc().x()+dir.x())*C.TILE_SIZE + C.TILE_SIZEH;
					int y = (a.tc().y()+dir.y())*C.TILE_SIZE + C.TILE_SIZEH;
					a.physics.body().moveC(x, y);
				}
			}
		}

		STATS.DISEASE().cure(a.indu(), true);
		STATS.NEEDS().INJURIES.setNonDanger(a.indu());
		
		return null;
	}
	
	



}
