package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.THINGS;

import game.GAME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.work.SubWork.SubWorkTool;
import settlement.misc.job.SETT_JOB;
import settlement.room.spirit.grave.GRAVE_JOB;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.grave.ROOM_GRAVEYARD;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.DIR;

final class WorkGraveDigger extends PlanBlueprint {

	private final SubWorkTool subTool;
	
	private final GraveData p;
	
	private final Resumer fetchCorpse;
	private final Resumer returnCorpse;
	private final Resumer work;
	
	WorkGraveDigger(AIModule_Work module, PlanBlueprint[] map, GraveData.GRAVE_DATA_HOLDER pl) {
		super(module, pl.graveData().blueprint(), map);
		this.p = pl.graveData();
		
		 subTool = new SubWorkTool("WORK_GRAVE" + pl.graveData().blueprint().key) {

				@Override
				protected SETT_JOB getJob(Humanoid a, AIManager d) {
					if (work(a) != null)
						return p.work(d.planTile.x(), d.planTile.y());
					return null;
				}
				
			};
		
		fetchCorpse = new Resumer(pl.graveData().blueprint().employment().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return returnCorpse.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				return job != null && job.jobReservedIs(null);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				if (job != null)
					job.jobReserveCancel(null);
			}
		};
		
		returnCorpse = new Resumer(pl.graveData().blueprint().employment().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				
				if (p.blueprint() instanceof ROOM_GRAVEYARD) {
					ROOM_GRAVEYARD g = (ROOM_GRAVEYARD) p.blueprint();
					for (DIR dir : DIR.ORTHO) {
						if (g.isGraveHead(d.planTile.x()+dir.x(), d.planTile.y()+dir.y())) {
							AISubActivation s = AI.SUBS().walkTo.drag(a, d, THINGS().corpses.draggable, job.corpse().index(), d.planTile.x()+dir.x(), d.planTile.y()+dir.y());
							if (s != null)
								return s;
						}
					}
					
				}
				
				AISubActivation s = AI.SUBS().walkTo.drag(a, d, THINGS().corpses.draggable, job.corpse().index(), d.planTile);
				if (s == null)
					can(a, d);
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return work.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				return job != null && job.jobReservedIs(null);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				if (job != null)
					job.jobReserveCancel(null);
			}
		};
		
		work = new Resumer(pl.graveData().blueprint().employment().verb) {
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				job.jobPerform(a, null, 0);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				return job != null && job.jobReservedIs(null);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				if (job != null)
					job.jobReserveCancel(null);
			}

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				GRAVE_JOB job = p.work(d.planTile.x(), d.planTile.y());
				job.jobStartPerforming();
				return subTool.activate(a, d, job);
			}
			
		};
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		if (GAME.ARMIES().enemy().men() > 0)
			return null;
		
		GRAVE_JOB job = p.work(work(a));
		if (job == null)
			return null;
		Corpse corpse = job.corpse();
		
		if (d.path.request(a.tc(), corpse.ctx(), corpse.cty())) {
			job.jobReserve(null);
			d.planTile.set(job.jobCoo());
			fetchCorpse.set(a, d);
			return AI.SUBS().walkTo.path(a, d);
		}
		
		return null;
	}
	

	

}