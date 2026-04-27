package game.battle.thread.order;

import game.battle.div.Div;
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
import init.constant.Config;
import settlement.main.SETT;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;

class PlanAttackDiv extends PlanWalkAbs{

	private final INT_OE<PlanData> pathI;
	protected final INT_OE<PlanData> pathd;
	protected final INT_OE<PlanData> width;
	protected final INT_OE<PlanData> timer;
	protected final INT_OE<PlanData> targetID;
	private final VectorImp vec = new VectorImp();
	private final Projector proj = new Projector();
	
	
	public PlanAttackDiv(Tools tools, LISTE<Plan> all, Data data) {
		super(tools, all, data, DIVTASK.ATTACK_MELEE);
		timer = data.new DataShort();
		pathI = data.new DataShort();
		pathd = data.new DataBit();
		width = data.new DataInt();
		targetID = data.new DataShort();
	}
	
	@Override
	void init() {

		
		width.set(m, prev.width());
		if (!checkTarget())
			return;
		
		blobDest();
	}
	
	protected boolean checkTarget() {
		Div target = task.targetDiv();
		
		if (target == null || !target.active()) {
			fail.set();
			return false;
		}
		return true;
	}
	

	
	private void blobDest() {
		
		Div target = task.targetDiv();
		if (target == null) {
			fail.set();
		}
		this.targetID.set(m, target.indexArmy());

		DivFormationImp f =  div.settings().guard ? getGuardDest() : getOverlappedDest();
		
		blobDest(f);
		
	}
	
	private void blobDest(DivFormationImp f) {
		

		if (f == null) {
			fail.set();
			return;
		}
		
		
		
		order.dest.set(f);
		dest.copy(f);
		
		
		setWalkToDest();
		
		pathd.set(m, path.currentI()&1);
		pathI.set(m, 0);
	}
	
	private DivFormationImp getOverlappedDest() {
		if (!checkTarget()) {
			fail.set();
			return null;
		}
		

		
		
		Div target = task.targetDiv();
		
		int sx = div.centre().cX();
		int sy = div.centre().cY();
		
		boolean useDest = false;
		
		if (t.div.inPosition(current, dest, C.TILE_SIZEH) + div.reporter.unreachable() > men/2) {
			sx = (int) (dest.start().x()+dest.dx()*dest.width()/2);
			sy = (int) (dest.start().y()+dest.dy()*dest.width()/2);
			useDest = true;

		}else {

		}
		
		vec.set(dest.dx(), dest.dy());
		vec.rotate90();
		
		sx += C.TILE_SIZE*4*vec.nX();
		sy += C.TILE_SIZE*4*vec.nY();
		
		int dx = target.centre().cUnitX();
		int dy = target.centre().cUnitY();
		
		double nx = 1;
		double ny = 0;
		double dist = 0;
		
		if (sx != dx || sy != dy) {
			dist = vec.set(sx, sy, dx, dy);
			vec.rotate90();

			nx = vec.nX();
			ny = vec.nY();
		}
		
		if (useDest && dest.dx()*nx + dest.dy()*ny > 0.8) {
			
			vec.set(dest.dx(), dest.dy());
			vec.rotate90();
			vec.rotate90();
			vec.rotate90();
			
			int x = (int) (sx + vec.nX()*dist);
			int y = (int) (sy + vec.nY()*dist);
			
			if (COORDINATE.tileDistance(x, y, dx, dy) < C.TILE_SIZE*2) {
				dx = x;
				dy = y;
				nx = dest.dx();
				ny = dest.dy();
			}
			

		}
		
		
		int w = width.get(m)/div.settings().formation.size(div);
		if (w < 3)
			w = 3;
		if (w > men/3)
			w = men/3;
		
		return t.deployer.deployCentre(div.info, men, div.settings().formation, dx, dy, nx, ny, w, a);
	}
	

	
	@Override
	void update(int gamemillis) {
		
		Div target = task.targetDiv();
		if (target == null) {
			fail.set();
			return;
		}
		
		if (targetID.get(m) != target.indexArmy()) {
			blobDest();
			return;
		}
		
		boolean engaged = isEngaged();
		if (!engaged)
			timer.set(m, 0);
		
		if (engaged && state(m) != fightGuard && div.settings().guard) {
			timer.set(m, 0);
			fightGuard.set();
		}else if (engaged && !div.settings().guard && state(m) != fight) {
			timer.inc(m, gamemillis);
			if (state(m) == fightGuard || timer.get(m) > 4000)
				fight.set();
		}else if (!engaged && (state(m) == fight || state(m) == fightGuard)) {
			setWalkToDest();
		}else {
			if (charging())
				Plan.chargeSpeed = true;
		}
		
	
		
		state(m).update(gamemillis);
		
		
		
		if (state(m) != fightGuard && state(m) != fight && (path.currentI() & 1) != pathd.get(m)) {
			pathd.set(m, path.currentI()&1);
			pathI.inc(m, 1);
			
			int length = path.length();
			if (!path.isComplete()) {
				length = BattleOrderPath.size;
			}
			int check = 4 + length/10;
			
			
			
			
//			int tres = 50;
//			if (path.isComplete()) {
//				tres = CLAMP.i(path.length()-path.currentI(), 1, 50); 
//			}
			
			if (pathI.get(m) > check) {
				DivFormationImp f =  div.settings().guard ? getGuardDest() : getOverlappedDest();
				COORDINATE dest = t.div.getSafeCentrePixel(Plan.dest);
				if (dest.tileDistanceTo(path.finalTDest()) < 2)
					return;
				blobDest(f);
			}
		}
	}
	
	@Override
	protected boolean running() {
		return super.running() | charging();
	}
	
	private boolean charging() {
		if (div.settings().guard)
			return false;
		return path.length() == 0 || (path.isComplete() && path.length()-path.currentI() < 32) && t.walk.canMoveAllTheWayToDest();
	}
	
	
	@Override
	void finished() {
		if (checkTarget())
			init();
		else
			fail.set();
	}
	
	private boolean isEngaged() {
		if (div.status().engagements() > 0) {
			for (int i = 0; i < current.deployed(); i++) {
				Div target = task.targetDiv();
				if (BattleStatus.map().isser.is(current.tx(i), current.ty(i), target)) {
					return true;
				}
				
			}
		}
		return false;
	}
	
	@Override
	boolean continueWhenFighting() {
		return div.status().engagements() < men/8 || isEngaged();
	}
	
	protected final STATE fail = new STATE("fail") {
		
		@Override
		void update(int gameMillis) {
			
		}
		
		@Override
		boolean setAction() {
			task.stop(div);
			order.task.set(task);
			return true;
		}
	};
	
	DivFormationImp res = new DivFormationImp(); 

	protected final STATE fight = new STATE("fight") {
		
		private final byte[] dirs = new byte[Config.battle().MEN_PER_DIVISION];
		
		@Override
		void update(int gameMillis) {
			
			timer.inc(m, -gameMillis);
			if (timer.get(m) > 0)
				return;
			timer.set(m, 1000);
			
			
			
			DivFormationImp nn = retreatedDest(0);
			if (nn == null) {
				fail.set();
				return;
			}
			
			dest.copy(t.mover.getFromMovedIntoTo(current, nn));
			
			//t.mover.moveFromIntoTo(current, prev, nn);
			
			
			
			div.order().dest.set(dest);
			
			nn = dest;
			
			
			res.deployInit(nn.dir(), nn.start().x(), nn.start().y(), nn.dx(), nn.dy(), nn.formation(), nn.width());

			Div target = task.targetDiv();
			if (target == null) {
				fail.set();
				return;
			}
				
			
			DivPosition tdiv = task.targetDiv().current();
			Flooder f = t.pather.getFlooder();
			f.init(this);
			//f.done();
			for (int i = 0; i < target.deployed(); i++) {
				if (target.reporter.reachable(i))
				proj.smark(tdiv.px(i), tdiv.py(i));
			}
			
			int cx = task.targetDiv().centre().cUnitX();
			int cy = task.targetDiv().centre().cUnitY();
			
			LIST<Pos> ll = t.getPosColumnSort(nn);
			for (int pi = 0; pi < ll.size(); pi++) {
				Pos pos = ll.get(pi);
				final double sx = nn.px(pos.pos);
				final double sy = nn.py(pos.pos);
				double m = vec.set(sx, sy, cx, cy);
				double dx = vec.nX();
				double dy = vec.nY();
				DIR d = vec.dir();
				
				vec.set(nn.dx(), nn.dy());
				vec.rotate90().rotate90().rotate90();
				
				if (deploy(sx, sy, vec.nX(), vec.nY(), m)) {
					dirs[pi] = (byte) d.id();
				}
//				else if(deploy(sx, sy, dx, dy, m)) {
//					dirs[pi] = (byte) vec.dir().id();
//				}
				else {
					res.deploy((int)sx, (int)sy, div.info);
					dirs[pi] = (byte) prev.dir().id();
				}
				
				
			}

			f.done();
			res.deployFinish(t.pather.filler, div.info);
			res.coherentSetNot();
			for (int i = 0; i < res.deployed(); i++) {
				res.setDir(i, DIR.ALL.get(dirs[i]));
			}
			
			nextPos = t.mover.getFromMovedIntoTo(current, res);
		}
		
		private boolean deploy(double sx, double sy, double dx, double dy, double m) {

			for (int dist = 0; dist <= m; dist+=proj.size) {
				int x = (int) (sx + dx*dist);
				int y = (int) (sy + dy*dist);
				
				if (proj.is(x, y) || BattleStatus.map().hasEnemy.is(x/C.TILE_SIZE, y/C.TILE_SIZE, a)) {
					
					res.deploy(x, y, div.info);
					x = (int) (sx + dx*(dist-proj.size));
					y = (int) (sy + dy*(dist-proj.size));
					proj.mark(x, y);
					return true;
				}
				
			}
			return false;
		}
		
		@Override
		boolean setAction() {
			timer.set(m, 0);
			update(0);
			return true;
		}
		
	};
	
	protected final STATE fightGuard = new STATE("fight") {
		
		@Override
		void update(int gameMillis) {
			
			timer.inc(m, -gameMillis);
			
			if (timer.get(m) > 0) {
				return;				
			}
			
			timer.set(m, 1000);

			DivFormationImp nn = getGuardDest(); 
			
			if (nn == null) {
				fail.set();
				return;
			}
			
			
			order.dest.set(nn);
			
			nextPos = nn;
			
		}
		
		@Override
		boolean setAction() {
			timer.set(m, 0);
			update(0);
			timer.set(m, 1000);
			return true;
		}
		

		
		
	};
	
	private DivFormationImp getGuardDest() {
		
		
		DivPosition target = task.targetDiv().position();
		
		
		DivFormationImp nn = retreatedDest(0);
		
		
		if (nn == null) {
			return prev;
		}
		
		Flooder f = t.pather.getFlooder();
		f.init(this);
		
//		for (int i = 0; i < target.deployed(); i++) {
//			proj.mark(target.px(i), target.py(i));
//		}
		target = task.targetDiv().current();
		for (int i = 0; i < target.deployed(); i++) {
			proj.mark(target.px(i), target.py(i));
		}
		
		double engagement = engagement(prev, 0, 0);
		
		if (prev.isCoherent() && engagement > 0.1 && engagement < 0.4 && !t.div.needsFixing(prev, men, a, div.settings().formation)) {
			f.done();
			return prev;
		}
		
		engagement = engagement(nn, 0, 0);
		
		double bestI = 0;
		double best = engagement;
		final double ideal = 0.25;
		vec.set(nn.dx(), nn.dy());
		vec.rotate90();
		vec.rotate90();
		vec.rotate90();
	
		
		
		if (engagement > 0.4) {
			for (double i = 0.25; i < 8; i+=0.25) {
				double dx = -vec.nX()*i*C.TILE_SIZE;
				double dy = -vec.nY()*i*C.TILE_SIZE;
				if (!t.deployer.canMove(div.info, nn, dx, dy, a))
					break;
				
				
				double e = engagement(nn, dx, dy);
				if (e <= 0)
					break;
				if (e < best && e >= ideal) {
					best = e;
					bestI = -i;
				}
			}
		}else {
			for (double i = 0.25; i < 8; i+=0.25) {
				double dx = vec.nX()*i*C.TILE_SIZE;
				double dy = vec.nY()*i*C.TILE_SIZE;
				
				if (!t.deployer.canMove(div.info, nn, dx, dy, a)) {
					break;
				}
				
				double e = engagement(nn, dx, dy);
				if (e > best && e <= ideal) {
					
					bestI = i;
					best = e;
				}
			}
		}
		
		f.done();
		
		nn = t.mover.getFromMovedIntoTo(current, nn);
		
		nn = t.deployer.move(div.info, nn, (int)(vec.nX()*bestI*C.TILE_SIZE), (int) (vec.nY()*bestI*C.TILE_SIZE), a);
		
		return nn;
	}
	
	public double engagement(DivFormationImp f, double dx, double dy) {
		
		double enemies = 0;
		
		for (int i = 0; i < f.deployed(); i++) {
			int x = (int) (f.px(i)+dx);
			int y = (int) (f.py(i)+dy);
			if (proj.is(x, y))
				enemies++;
		}
		
		return enemies / (f.width()/f.formation().size(Plan.div));
		
		
	}
	
	private DivFormationImp retreatedDest(int distance) {
		
		DivFormationImp f = getOverlappedDest();
		if (f == null)
			return null;
		
		
		Div target = task.targetDiv();
		dest.copy(f);
		f = retreatedDest(dest, target.current(), distance);
		
		return f;
	}
	
	
	DivFormationImp retreatedDest(DivFormationImp result, DivPosition target, int distance) {
		
		vec.set(result.dx(), result.dy());
		vec.rotate90();
		
		Flooder f = t.pather.getFlooder();
		f.init(this);
		for (int i = 0; i < target. deployed(); i++) {
			f.close(target.tx(i), target.ty(i), 0);
			
		}
		double dist = 0;
		
		while(true) {
			
			double dx = vec.nX()*(dist+1);
			double dy = vec.nY()*(dist+1);
			
			boolean enemies = false;
			
			for (int i = 0; i < result.deployed(); i++) {
				int tx = (int) (result.tx(i)+dx);
				int ty = (int) (result.ty(i)+dy);
				if (!SETT.IN_BOUNDS(tx, ty) || !DivPlacability.tileIsOK(tx, ty, a)) {

					f.done();
					return t.deployer.move(div.info, result, (int)(vec.nX()*dist*C.TILE_SIZE), (int)(vec.nY()*dist*C.TILE_SIZE), a);
				}

				enemies |= f.hasBeenPushed(tx, ty);
			}
			
			dist++;
			
			if (!enemies) {
				distance --;
				if (distance <= 0) {
					f.done();
					return t.deployer.move(div.info, result, (int)(dx*C.TILE_SIZE), (int)(dy*C.TILE_SIZE), a);
				}
			}
			
		}
		
		
		
	}
	
	
	private class Projector {
		
		final int size = C.TILE_SIZE/2;
		Projector(){
			
		}
		
		
		void smark(int px, int py) {
			mark(px, py);
			for (int di = 0; di < DIR.ALL.size(); di++) {
				mark(px+DIR.ALL.get(di).x()*size, py+DIR.ALL.get(di).y()*size);
			}

		}
		
		void mark(int px, int py) {
			
			px = t(px, SETT.TWIDTH);
			py = t(py, SETT.THEIGHT);
			
			
			if (!SETT.IN_BOUNDS(px, py))
				return;
			
			t.pather.getFlooder().close(px, py, 0);
			t.pather.getFlooder().setValue2(px, py, 0);
			
		}
		
		private int t(int p, int max) {
			p = p*2/C.TILE_SIZE;
			while (p < 0) {
				p += max;
			}
			while(p >= max) {
				p -= max;
			}
			return p;
		}
		
		boolean is(int px, int py) {
			px = t(px, SETT.TWIDTH);
			py = t(py, SETT.THEIGHT);

			if (!SETT.IN_BOUNDS(px, py))
				return true;
			
			return t.pather.getFlooder().hasBeenPushed(px, py);
		}
		
		
	}


}
