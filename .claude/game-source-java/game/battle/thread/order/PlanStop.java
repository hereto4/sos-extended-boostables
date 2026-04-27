package game.battle.thread.order;

import game.battle.formation.DivFormation;
import game.battle.formation.DivFormationImp;
import game.battle.formation.DivPlacability;
import game.battle.formation.DivPosition;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.battle.thread.order.BattleOrderUpdater.Data;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import game.battle.thread.order.BattleOrderUpdater.PlanData;
import game.battle.thread.order.Tools.Pos;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;

final class PlanStop extends Plan{
	
	private final VectorImp vec = new VectorImp();
	
	private final INT_OE<PlanData> timer;
	private final INT_OE<PlanData> timer2;
	
	public PlanStop(Tools tools, LISTE<Plan> all, Data data) {
		super(tools, all, data, DIVTASK.STOP);
		timer = data.new DataInt();
		timer2 = data.new DataShort();
	}
	
	@Override
	void init() {
		
		wait.set();
		
		order.path.set(path);
		
		if (current.deployed() == 0)
			return;
		if (!div.active())
			return;
		
		DivFormationImp form = null;
		
		if (Plan.prev.deployed() == men && prev.isCoherent() && t.deployer.isValid(div.info, Plan.prev, a) && t.div.inPosition(current, prev, 1.5*C.TILE_SIZE) > men/2) {
			form = prev;
		}else if(Plan.dest.deployed() == men && t.deployer.isValid(div.info, Plan.dest, a) && t.div.inPosition(current, dest, 1.5*C.TILE_SIZE) > men/2) {
			form = dest;
		}else {
			DivPosition p = current;
			
			int cx = 0;
			int cy = 0;
			int ci = 0;
			
			for (int i = 0; i < p.deployed(); i++) {
				if (!DivPlacability.pixelIsBlocked(p.px(i), p.px(i), C.TILE_SIZE, a)) {
					if (div.reporter.reachable(i)) {
						cx += p.px(i);
						cy += p.py(i);
						ci++;
					}
				}
				
			}
			
			if (ci == 0) {
				for (int i = 0; i < p.deployed(); i++) {
					if (!DivPlacability.pixelIsBlocked(p.px(i), p.px(i), C.TILE_SIZE, a)) {
						cx += p.px(i);
						cy += p.py(i);
						ci++;
					}
				}
			}
			
			if (ci == 0) {
				return;
			}
			
			cx /= ci;
			cy /= ci;
			
			double dist = Double.MAX_VALUE;
			int bi = -1;
			
			for (int i = 0; i < p.deployed(); i++) {
				
				if (DivPlacability.pixelIsBlocked(p.px(i), p.px(i), C.TILE_SIZE, a)) {
					continue;
				}
				
				double d = COORDINATE.tileDistance(cx, cy, p.px(i), p.py(i));
				if (!div.reporter.reachable(i))
					d += 100000;
				
				if (d < dist) {
					dist = d;
					bi = i;
				}
			}
			
			if (bi == -1) {
				return;
			}
			
			int size = div.settings().formation.size(div);
			int width = (int) (Math.sqrt(Plan.men))*size;
			
			if (prev.width()/size > 0) {
				if (men / (prev.width()/size) > 2) {
					width = prev.width();
				
				}
			}
			
			double dx = prev.dx();
			double dy = prev.dy();
			if (dx == 0 && dy == 0)
				dx = 1;
			 
			//width /= size;
			
			form = t.deployer.deployArroundCentre(div.info, men, prev.formation(), cx, cy,  dx, dy, width, a);
			
			if (form == null) {
				LOG.err("nay1 " + cx/C.TILE_SIZE + " " + cy/C.TILE_SIZE + " " + current.deployed());
			}else {
				
			}
			
			
		}
	
		if (form != null) {
			form = t.mover.getFromMovedIntoTo(current, form);
			dest.copy(form);
			order.dest.set(dest);
			nextPos = dest;
		}
		
		
	
		

		
	}
	
	@Override
	boolean continueWhenFighting() {
		return true;
	}

	@Override
	void update(int gamemillis) {
		state(m).update(gamemillis);
	}
	
	private STATE wait = new STATE("wait") {
		
		@Override
		void update(int gameMillis) {


			
			shouldBreak = true;
			timer.inc(m, gameMillis);
			timer2.inc(m, gameMillis);
			if (timer.get(m) < 1000)
				return;
			timer.set(m, 0);
			nextPos = prev;
			
			if (t.div.fixIfNeeded(dest)) {
				order.dest.set(dest);
				return;
			}
			
			
			
			int ddx = prev.start().x()-dest.start().x();
			int ddy = prev.start().y()-dest.start().y();
			double engagement = engagement(dest, ddx, ddy);


			if (engagement > 0) {
				
				timer2.set(m, 0);
				
				unfuckPrev();
				
				
				
				if (!div.settings().guard) {
					if (engagement > 1.25)
						tryStep(stepBack(engagement));
					else if (engagement < 0.2)
						tryStep(stepForward(engagement));
					advanceColumn(2.0);
				}else if (engagement > 1.25){
					if (engagement > 1.25){
						tryStep(stepBack(engagement));
						advanceColumn(1.0);
					}else if (engagement < 0.2)
						tryStep(stepForward(engagement));
					advanceColumn(1.0);
				}
				
				
				
				

			}else {
				
				if (ddx != 0 || ddy != 0) {
					timer2.set(m, 0);
					
					unfuckPrev();
//					ddx = prev.start().x()-dest.start().x();
//					ddy = prev.start().y()-dest.start().y();
					
					double m = vec.set(ddx, ddy);
					
					m = CLAMP.d(m, 0, C.TILE_SIZE);
					nextPos = t.deployer.move(div.info, prev, -(int)Math.round(vec.nX()*m), -(int)Math.round(vec.nY()*m), a);
					
					return;
				}
				
				nextPos = dest;
				
				if (!div.settings().guard) {
					advanceColumn(2.0);
				}else if (!t.walk.hasReachedPrev() && timer.get(m) > 5000) {
					task.move(div);
					div.order().task.set(task);
					return;
				}
			}
			
			
			
			
		}
		
		private void tryStep(DivFormationImp n) {
			if (n == null)
				return;
			dest.copy(n);
			nextPos = dest;
		}
		
		@Override
		boolean setAction() {
			timer.set(m, 0);
			timer2.set(m, 0);
			return true;
		}
	};
	
	private final DivFormationImp ff = new DivFormationImp();

	private DivFormationImp unfuckPrev() {
		
		if (prev.deployed() == 0) {
			prev.copy(dest);
			return prev;
		}
			
		
		int ddx = prev.start().x()-dest.start().x();
		int ddy = prev.start().y()-dest.start().y();
		
		for (int i = 0; i < dest.deployed(); i++) {
			prev.set(i, dest.px(i)+ddx, dest.py(i)+ddy);
		}
//		DivFormationImp f = t.deployer.getFixedFormation(div.info, prev, div.settings().formation, men, a);
//		if (f == null)
//			f = dest;
//		prev.copy(f);
		return prev;
	}
	
	private DivFormation advanceColumn(double am) {
		
		
		
		ff.deployInit(dest.dir(), dest.start().x(), dest.start().y(), dest.dx(), dest.dy(), dest.formation(), dest.width());
		vec.set(dest.dx(), dest.dy());
		vec.rotate90().rotate90().rotate90();
		
		
		LIST<Pos> ll = t.getPosColumnSort(dest);
		for (int pi = 0; pi < ll.size(); pi++) {
			Pos start = ll.get(pi);
			
			int ddx = 0;
			int ddy = 0;
			
			int size = ff.formation().size(div);
			
			for (double a = 0.25; a < am; a+=0.25) {
				int px = dest.px(start.pos);
				int py = dest.py(start.pos);
				int dx = (int) (size*a*vec.nX());
				int dy = (int) (size*a*vec.nY());
				
				px += dx;
				py += dy;
				if (!DivPlacability.pixelIsBlocked(px, py, size, Plan.a) && BattleStatus.map().hasEnemy.is(px/C.TILE_SIZE, py/C.TILE_SIZE, Plan.a)) {
					ddx = dx;
					ddy = dy;
					break;
				}
			}
			
			
			
			
			while(pi < ll.size() && ll.get(pi).columnI == start.columnI) {
				int px = dest.px(ll.get(pi).pos)+ddx;
				int py = dest.py(ll.get(pi).pos)+ddy;
				ff.deploy(px, py, ll.get(pi).pos, div.info);
				pi++;
			}
			pi--;
			
		}
		
		ff.deployFinish(t.pather.filler, div.info);
		
		nextPos = ff;
		return null;
		
	}
	

	private DivFormationImp stepForward(double currentEngagement) {
		
		vec.set(dest.dx(), dest.dy());
		vec.rotate90();
		vec.rotate90();
		vec.rotate90();
		for (int i = 1; i < 8; i++) {
			
			int dx = (int) (vec.nX()*8*i);
			int dy = (int) (vec.nY()*8*i);
			if (!t.deployer.canDeploy(dest.start().x()+dx, dest.start().y()+dy, dest.dx(), dest.dy(), dest.width(), dest.formation().size(div), a, div.race()))
				break;
			if (engagement(dest, dx, dy) > currentEngagement) {
				nextPos = t.deployer.move(div.info, dest, dx, dy, a);
				return nextPos;
			}
		}
		
		return null;
		
	}
	
	private DivFormationImp stepBack(double currentEngagement) {
		
		vec.set(dest.dx(), dest.dy());
		vec.rotate90();

		for (int i = 1; i < 8; i++) {
			
			int dx = (int) (vec.nX()*8*i);
			int dy = (int) (vec.nY()*8*i);
			if (!t.deployer.canDeploy(prev.start().x()+dx, prev.start().y()+dy, dest.dx(), dest.dy(), dest.width(), dest.formation().size(div), a, div.race()))
				break;
			if (engagement(prev, dx, dy) < currentEngagement) {
				nextPos = t.deployer.move(div.info, prev, dx, dy, a);
				//DivFormationImp f = t.mover.getFromMovedIntoTo(current, nextPos);
				return nextPos;
			}
		}
		
		return null;
		
	}

	
	double engagement(DivFormationImp f, double dx, double dy) {
		
		double enemies = 0;
		
		if (f.deployed() == 0)
			return 0;
		
		for (int i = 0; i < f.deployed(); i++) {
			int x = (int) (f.px(i)+dx);
			int y = (int) (f.py(i)+dy);
			enemies += BattleStatus.map().soldiers(Plan.div.armyEnemy()).get(x>>C.T_SCROLL, y>>C.T_SCROLL);
		}
		
		return enemies / (f.width()/f.formation().size(Plan.div));
		
	}

}
