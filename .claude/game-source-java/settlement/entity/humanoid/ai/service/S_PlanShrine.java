package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

final class S_PlanShrine extends S_Plan{

	S_PlanShrine() {
		super(STATS.RELIGION().SHRINE, 1.0);
	}

	@Override
	public boolean hasAccess(Humanoid a, AIManager d) {
		return STATS.RELIGION().SHRINE.ACCESS.indu().get(a.indu()) > 0;
	}

	@Override
	public boolean allowed(Humanoid a, AIManager d) {
		return STATS.RELIGION().getter.get(a.indu()).permission.has(a);
	}

	@Override
	public boolean goodTime(Humanoid a, AIManager d) {
		return true;
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		AiPlanActivation p = plan.activate(a, d);
		if (p == null) {
			STATS.RELIGION().SHRINE.clearAccess(a);
		}
		return p;
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d, int dist) {
		return getPlan(a, d);
	}
	
	public LIST<ROOM_SHRINE> services(Humanoid a, AIManager d) {
		return SETT.ROOMS().TEMPLES.shrines(STATS.RELIGION().getter.get(a.indu()).religion);
	}
	
	private final AIPLAN plan = new AIPLAN.PLANRES("serShrine"){

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(null) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte4 = 0;
				for (ROOM_SHRINE t : services(a, d)) {
					AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, t.service().finder, t.service().radius);
					if (s != null) {
						d.planByte4 = (byte) t.typeIndex();
						return s;
					}
					
				}
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				STATS.RELIGION().SHRINE.setAccess(a);
				return pray.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}
			
			@Override
			protected void name(Humanoid a, AIManager d, Str string) {
				string.add(temp(d).service().verb);
			};
		};
		
		private final Resumer pray = new Resumer(null) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planTile.set(d.path.destX(), d.path.destY());
				d.planByte1 = (byte) (5 + RND.rInt(10));
				
				FSERVICE s = temp(d).service(d.planTile.x(), d.planTile.y()).get(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.startUsing();
				
				FurnisherItem it = SETT.ROOMS().fData.item.get(a.tc());
				if (it != null) {
					COORDINATE c = SETT.ROOMS().fData.itemX1Y1(a.tc(), Coo.TMP);
					if (c != null) {
						
						int dx = c.x()+it.width()/2;
						int dy = c.y()+it.height()/2;
						
						DIR dir = DIR.get(a.tc().x(), a.tc().y(), dx, dy);
						a.speed.setDirCurrent(dir);
					}
				}

				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 <= 0) { 
					can(a, d);
					return null;
				}
				if (RND.rBoolean()) {
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.lay, 4 + RND.rInt(4));
				}else {
					if (RND.rBoolean())
						return AI.SUBS().single.activate(a, d, AI.STATES().anima.carry, 4 + RND.rInt(4));
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.stand, 4 + RND.rInt(4));
				}
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = temp(d).service().finder.getReserved(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.findableReserveCancel();
			}
			
			@Override
			protected void name(Humanoid a, AIManager d, Str string) {
				string.add(temp(d).service().verb);
			};
		};
		
		private ROOM_SHRINE temp(AIManager d) {
			return SETT.ROOMS().TEMPLES.SHRINES.get(d.planByte4);
		}

		
	};


	
}
