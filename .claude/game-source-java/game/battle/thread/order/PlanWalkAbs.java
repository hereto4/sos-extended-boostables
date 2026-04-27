package game.battle.thread.order;

import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.battle.thread.order.BattleOrderUpdater.Data;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import game.battle.thread.order.BattleOrderUpdater.PlanData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;

abstract class PlanWalkAbs extends Plan {

	private final INT_OE<PlanData> inPosition;
	private final INT_OE<PlanData> timer;
	final INT_OE<PlanData> colTimer;
	private final INT_OE<PlanData> tilesDestCheck;
	private final INT_OE<PlanData> destId;
	static int amountOfPaths = 0;

	public PlanWalkAbs(Tools tools, LISTE<Plan> all, Data data, DIVTASK task) {
		super(tools, all, data, task);
		inPosition = data.new DataByte();
		timer = data.new DataInt();
		colTimer = data.new DataShort();
		tilesDestCheck = data.new DataByte();
		destId = data.new DataNibble();

	}

	void setWalkToDest() {

		path.clear();
		order.path.set(path);

		wait.set();
	}

	private boolean checkNextDest() {
		int di = order.dest.setI() & 0x0F;

		if (destId.get(m) != di) {
			destId.set(m, di);
			return true;
		}
		
		if (t.div.needsFixing(dest, men, a, div.settings().formation)) {
			int w = dest.width();
			int destX = dest.start().x();
			int destY = dest.start().y();
			
			if (t.deployer.fixFormation(div.info, dest, div.settings().formation, men, a)) {
				order.dest.set(dest);
				di = order.dest.setI() & 0x0F;
				destId.set(m, di);
				if (!dest.start().isSameAs(destX, destY) || Math.abs(w-dest.width()) > dest.formation().size(div)) {
					return true;
				}
			}
		}

		
		return false;
	}

	private final STATE wait = new STATE("wait") {

		@Override
		void update(int gameMillis) {
			
			if (!div.active() || men <= 0)
				return;
			
			if (amountOfPaths > 1)
				return;
			amountOfPaths ++;
			destId.set(m, order.dest.setI() & 0x0F);
			setStart.set();
		}

		@Override
		boolean setAction() {
			
			return true;
		}
	};

	private final STATE setStart = new STATE("setStart") {

		@Override
		boolean setAction() {
			
			if (!t.walk.setStart(ToolsWalk.destMoveStart)) {
				return moveIntoDest.set();
			}

			if (t.div.intersectsSomewhat(prev, dest)) {
				return moveIntoDest.set();
			}
			timer.set(m, 0);
			inPosition.set(m, t.walk.countPosition());
			return true;
		}

		@Override
		void update(int gamemillis) {
			
			if (men == 0)
				return;
			
			if (checkNextDest()) {
				
				wait.set();
				return;
			}

			if (prev.deployed() == 0) {
				task.stop(div);
				div.order().task.set(task);
				return;
			}
			
			if (prev.deployed() > 0 && t.div.fixIfNeeded(prev)) {
				nextPos = prev;
			}

			int pos = t.walk.countPosition();
			if (pos > 0) {
				if (pos == 1) {
					timer.set(m, 0);
				}
				
				
				
				if (pos >= men - unreachable || running()) {
					followPath.set();
					return;
				}
				
				timer.inc(m, gamemillis);
				if (pos > inPosition.get(m)) {
					timer.set(m, 0);
				}
				inPosition.set(m, pos);

				
				if (running() || t.div.isCloseToFighting() || timer.get(m) >= 1500) {
					followPath.set();
					return;
				}
			} else {
				timer.inc(m, gamemillis);
				if (timer.get(m) > 1000) {
					setAction();
					return;
				}
			}

			if (path.isDest()) {
				return;
			}

			if (path.currentI() < path.length() - 1) {
				COORDINATE cc = t.div.currentCentre();
				double d1 = cc.tileDistanceTo(path.x(), path.y());
				path.currentIInc(1);
				double d2 = cc.tileDistanceTo(path.x(), path.y());
				path.currentIInc(-1);

				if (d1 <= d2+3)
					return;
				t.walk.setNextPosition(ToolsWalk.destMoveStart, gamemillis);
				timer.set(m, 0);
			}

		}

	};
	
	private final STATE followPath = new STATE("follow path") {

		@Override
		boolean setAction() {
			tilesDestCheck.set(m, ToolsWalk.destMoveStart - ToolsWalk.destMoveResume);
			inPosition.set(m, t.walk.countPosition());
			timer.set(m, 0);
			colTimer.set(m, 0);
			return true;
		}

		@Override
		void update(int gameMillis) {

			

			double sp = speed(gameMillis);
			
			if (sp == 0) {
				return;
			}
			if (path.isDest()) {
				resume();
				return;
			}
			order.path.get(path);
			tilesDestCheck.inc(m, -1);
			int pi = path.currentI();
			if (!t.walk.setNextPosition(ToolsWalk.destMoveResume + tilesDestCheck.get(m), (int)Math.ceil(gameMillis*sp))) {
				init();
				return;
			}
			if (pi != path.currentI()) {
				if (checkNextDest()) {
					wait.set();
				}
			}

			// double dist = t.div.distanceMaxFromCurrentToNext(tmp, next);
			// timer.set(m, 0);
			// double ma = C.TILE_SIZE + C.TILE_SIZEH;
			// if (dist > ma) {
			// timer.inc(m, (int) (1000.0*(dist-ma)/ma));
			// }

		}



		private double speed(int gameMillis) {

//			if (t.div.fixIfNeeded(prev)) {
//				nextPos = prev;
//			}

			int in = t.walk.countPosition();

			if (in == 0) {
				colTimer.inc(m, gameMillis);
				if (colTimer.get(m) > 3000) {
					setStart.set();
				} else if (path.currentI() < path.length() - 1) {

					COORDINATE cc = t.div.currentCentre();
					double d1 = cc.tileDistanceTo(path.x(), path.y());
					path.currentIInc(1);
					double d2 = cc.tileDistanceTo(path.x(), path.y());
					path.currentIInc(-1);

					if (d1 > d2+3) {
						if (!t.walk.setNextPosition(ToolsWalk.destMoveResume + tilesDestCheck.get(m), gameMillis)) {
							
							init();
							return 0;
						}
						
						
						timer.set(m, 0);
						colTimer.set(m, 0);
						return 0;
					}
				}

				return 0;
			}
			colTimer.set(m, 0);

			if (running())
				return 1.0;
			
			in += unreachable;
			
			if (in >= prev.deployed())
				return 1.0;
			
			double d = (men-in)/(men);
			return CLAMP.d(0.25 + d, 0, 1);

		}

		void resume() {
			if (path.isDest() && path.isComplete()) {
				moveIntoDest.set();
				return;
			}
			if (!t.walk.setStart(ToolsWalk.destMoveStart)) {
				init();
				return;
			}
			if (t.div.intersectsSomewhat(prev, dest))
				moveIntoDest.set();
			else
				inPosition.set(m, CLAMP.i(t.walk.countPosition(), 0, men));
		}

	};

	protected boolean running() {
		if (div.settings().running)
			return true;
		return false;
	}

	private final STATE moveIntoDest = new STATE("move into dest") {

		@Override
		boolean setAction() {
			path.clear();
			order.path.set(path);
			t.mover.rearrangeDest(prev, dest);
			order.dest.set(dest);
			int di = order.dest.setI() & 0x0F;
			destId.set(m, di);
			timer.set(m, 100);
			inPosition.set(m, t.walk.countPosition());
			update(0);
			return true;
		}

		@Override
		void update(int gamemillis) {
			if (checkNextDest()) {
				wait.set();
				return;
			}
			if (wait(m, gamemillis))
				return;
			if (!t.mover.merge(prev, dest)) {
				stayInDest.set();
				return;
			}

			nextPos = prev;
		}

		private boolean wait(PlanData m, int gamemillis) {

			timer.inc(m, -gamemillis);
			int in = t.walk.countPosition();

			if (in == 0)
				return true;

			if (in < inPosition.get(m)) {
				timer.inc(m, -gamemillis);
				if (timer.get(m) <= 0) {
					inPosition.inc(m, -1);
					timer.set(m, 100);
				}
				return true;
			} else {
				inPosition.set(m, in);
			}
			return false;
		}

	};

	private final STATE stayInDest = new STATE("stay in dest") {

		@Override
		boolean setAction() {

			nextPos = dest;

			return true;
		}

		@Override
		void update(int gamemillis) {

			if (checkNextDest()) {
				wait.set();
				return;
			}

			if (!t.walk.hasReachedPrev())
				return;
			
			finished();
		}

	};

	abstract void finished();

}
