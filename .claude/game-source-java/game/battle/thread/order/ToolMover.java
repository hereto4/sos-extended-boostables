package game.battle.thread.order;

import static game.battle.thread.order.BattleOrderUpdater.Plan.div;

import game.battle.formation.DivFormationImp;
import game.battle.formation.DivPlacability;
import game.battle.formation.DivPosition;
import game.battle.formation.DivPositionImp;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import init.constant.C;
import init.constant.Config;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;

final class ToolMover {

	private final DivFormationImp positions;
	private final TREE toTree;
	private final TREE fromTree;
	private final POS[] toPosses;
	private final POS[] fromPosses;
	private final ArrayList<POS> cache;
	private boolean[] tosThatHasBeenPlaced;
	private final PathUtilOnline pu;
	private final VectorImp vec = new VectorImp();

	ToolMover(PathUtilOnline pu){
		
		int maxmen = Config.battle().MEN_PER_DIVISION;
		positions = new DivFormationImp();
		
		tosThatHasBeenPlaced = new boolean[maxmen];
		toTree = new TREE(maxmen);
		fromTree = new TREE(maxmen);

		toPosses = new POS[maxmen];
		fromPosses = new POS[maxmen];
		for (int i = 0; i < maxmen; i++) {
			toPosses[i] = new POS();
			fromPosses[i] = new POS();
		}
		cache = new ArrayList<POS>(maxmen);
		this.pu = pu;
	}
	
	void rearrangeDest(DivFormationImp from, DivFormationImp to) {
		if (from.deployed() == 0 || to.deployed() == 0) {
			positions.copy(to);
			from.copy(positions);
			return;
		}
		
		for (int i = 0; i < to.deployed(); i++)
			tosThatHasBeenPlaced[i] = false;
		
		positions.copy(to);
		
		centers(from, to);
		
		if (toTree.hasMore()) {
			for (int i = 0; i < to.deployed(); i++) {
				if (!tosThatHasBeenPlaced[i]) {
					COORDINATE c = to.pixel(toTree.pollGreatest().i);
					positions.set(i, c.x(), c.y());
				}
			}
		}
		
		to.copy(positions);
		to.deployFinish(pu.filler, div.info);
	}
	
	
	public DivFormationImp getFromMovedIntoTo(DivPositionImp current, DivFormationImp to) {
		
		if (current == positions || to == positions)
			throw new RuntimeException();
		
		if (current.deployed() == 0 || to.deployed() == 0) {
			positions.copy(to);
			return positions;
		}
		
		for (int i = 0; i < to.deployed(); i++)
			tosThatHasBeenPlaced[i] = false;
		
		positions.copy(to);
		
		centers(current, to);
		
		if (toTree.hasMore()) {
			for (int i = 0; i < to.deployed(); i++) {
				if (!tosThatHasBeenPlaced[i]) {
					COORDINATE c = to.pixel(toTree.pollGreatest().i);
					positions.set(i, c.x(), c.y());
				}
			}
		}
		positions.deployFinish(pu.filler, div.info);
		return positions;
	}

	private final Rec centre = new Rec();
	
	private void centers(DivPosition from, DivFormationImp to) {
		toTree.clear();
		fromTree.clear();
		
		centre.clear();
		for (int i = 0; i < from.deployed() && i < to.deployed(); i++) {
			centre.unify(from.pixel(i).x(), from.pixel(i).y());
			
		}
		
		double toDistX = to.body().cX()-centre.cX();
		double toDistY = to.body().cY()-centre.cY();
		
		for (int i = 0; i < from.deployed() && i < to.deployed(); i++) {
			double dx = from.pixel(i).x()-centre.cX();
			double dy = from.pixel(i).y()-centre.cY();
			double d = Math.sqrt(dx*dx+dy*dy);
			fromPosses[i].i = i;
			fromPosses[i].value = d;
			fromTree.add(fromPosses[i]);
		}
		
		
		for (int i = 0; i < from.deployed() && i < to.deployed(); i++) {
			double dx = to.pixel(i).x()-toDistX-centre.cX();
			double dy = to.pixel(i).y()-toDistY-centre.cY();
			double d = Math.sqrt(dx*dx+dy*dy);
			toPosses[i].i = i;
			toPosses[i].value = d;
			toTree.add(toPosses[i]);
		}
		
		final double M = Math.max(to.formation().size(div), 0)*2; 
		
		while(toTree.hasMore() && fromTree.hasMore()) {
			POS pTo = toTree.pollGreatest();
			
			cache.clear();
			
			
			POS pFrom = fromTree.pollGreatest();
			cache.add(pFrom);
			double max = pFrom.value;
			double dx = to.pixel(pTo.i).x() - (from.pixel(pFrom.i).x()+toDistX);
			double dy = to.pixel(pTo.i).y() - (from.pixel(pFrom.i).y()+toDistY);
			double lastDist = Math.sqrt(dx*dx+dy*dy);
			
			while(fromTree.hasMore()) {
				POS candidate = fromTree.pollGreatest();
				cache.add(candidate);
				
				if (Math.abs(candidate.value-max) > M)
					break;
				
				dx = to.pixel(pTo.i).x() - (from.pixel(candidate.i).x()+toDistX);
				dy = to.pixel(pTo.i).y() - (from.pixel(candidate.i).y()+toDistY);
				double dist = Math.sqrt(dx*dx+dy*dy);
				if (dist < lastDist) {
					lastDist = dist;
					pFrom = candidate;
					
				}
			}


			COORDINATE t = to.pixel(pTo.i);
			positions.set(pFrom.i, t.x(), t.y());
			tosThatHasBeenPlaced[pFrom.i] = true;
			
			for (int i = 0; i < cache.size(); i++) {
				POS p = cache.get(i);
				if (p != pFrom) {
					fromTree.add(p);
				}
			}
		}
	}
	
	
	/**
	 * will interpolate between two positions, returns true if the old from is not the same as to
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean merge(DivFormationImp from, DivFormationImp to) {
		
		boolean move = false;
		
		if (from.deployed() != to.deployed())
			return false;
		
		for (int i = 0; i < to.deployed(); i++) {
			
			double tx = to.pixel(i).x();
			double ty = to.pixel(i).y();
			if (i >= from.deployed()) {
				move = true;
				from.set(i, (int)tx, (int)ty);
			}
			
			double fx = from.pixel(i).x();
			double fy = from.pixel(i).y();
			int size = from.formation().size(div);
			if (fx != tx || fy != ty) {
				move = true;
				double mag = vec.set(fx, fy, tx, ty);
				if (mag > C.TILE_SIZE)
					mag = C.TILE_SIZE;
				int nx = (int) (fx + vec.nX()*mag);
				int ny = (int) (fy + vec.nY()*mag);
				if (DivPlacability.pixelIsBlocked(nx, ny, size, Plan.a)) {
					from.set(i, (int)tx, (int)ty);
				}else {
					from.set(i, (int)nx, (int)ny);
				}
				
			}
			
		}
		if (move) {
			from.init(to.deployed());
			from.deployFinish(pu.filler, div.info);
		}
		return move;
		
	}
	
	public boolean mergeNeeds(DivFormationImp from, DivFormationImp to) {
		
		for (int i = 0; i < to.deployed(); i++) {
			double fx = from.pixel(i).x();
			double fy = from.pixel(i).y();
			double tx = to.pixel(i).x();
			double ty = to.pixel(i).y();
			if (fx != ty || fy != ty) {
				double mag = vec.set(fx, fy, tx, ty);
				if (mag > C.TILE_SIZE*2)
					return true;
				
			}
			
		}
		return false;
		
	}
	
	private static class POS {
		
		private int i;
		private double value;
		
	}
	
	private static class TREE extends Tree<POS>{

		public TREE(int size) {
			super(size);
		}

		@Override
		protected boolean isGreaterThan(POS current, POS cmp) {
			return current.value > cmp.value;
		}
		
	}


	
}
