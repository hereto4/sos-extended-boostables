package settlement.entity.humanoid.ai.main;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.thing.DRAGGABLE;
import settlement.thing.DRAGGABLE.DRAGGABLE_HOLDER;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

public class AISTATES {

	public final STOP_DIV STAND_SWORD = new STOP_DIV("STANDS", HSprites.SWORD_STAND);
	public final STOP STAND = new STOP("STAND", HSprites.STAND);
	public final MOVE_TOWARDS MOVE_TO = new MOVE_TOWARDS("MOVETO");
	public final PUSH_TOWARDS PUSH_TO = new PUSH_TOWARDS("PUSHTO", HSprites.SWORD_STAND);
	public final WALK WALK = new WALK("WALK", 0.6);
	public final WALK RUN = new WALK("RUN", 1.0);
	public final STOP STOP = new STOP("STOP", HSprites.STAND);

	public final WALK_DEST WALK2 = new WALK_DEST("WALK2", 0.6);
	public final WALK_DEST RUN2 = new WALK_DEST("DEST_RUN", 0.9);
	public final WALK_DEST DRAG = new WALK_DEST("DRAG", true);
	public final WALK_DEST WALK2_SWORD = new WALK_DEST("WALK2_SWORD", 0.4, HSprites.SWORD_STAND);
	public final Animation WORK = new Animation("WORK", "working", HSprites.TOOL_HIT);
	public final SLEEP SLEEP = new SLEEP();
	public final Animations anima = new Animations();
	public final AnimationArrays animaArr = new AnimationArrays();
	public final STOP layStop = new STOP("LAYSTOP", HSprites.LAY);
	public final Animation LAY = new Animation("LAY", "laying", HSprites.LAY);
	public final FLY FLY = new FLY("FLY");
	
	public final WALK jog = new AISTATES.WALK("SPRINT", 0.7);
	public final WALK jogCrazy = new AISTATES.WALK("SPRINT_CRAZY", 0.7, HSprites.WAVE);

	public final Sword SWORD = new Sword();
	
	public class SLEEP {

		private SLEEP() {

		}

		public AISTATE activate(Humanoid a, AIManager d, float time) {
			d.stateTimer = time;
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
			if (!a.speed.dir().isOrtho())
				a.speed.setDirCurrent(a.speed.dir().next(1));
			return state;
		};

		private final AISTATE state = new AISTATE("SLEEP", "sleeping") {

			@Override
			public HSprite sprite(Humanoid a) {
				return HSprites.SLEEP;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, double ds) {
				d.stateTimer -= ds;
				if (d.stateTimer <= 0) {
					return false;
				}
				return true;
			}

		};

	}

	public static class WALK {

		private final double target;
		private final AISTATE state;
		
		public WALK(String key, double target) {
			this(key, target, HSprites.MOVE);
		}

		public WALK(String key, double target, HSprite sprite) {
			this.target = target;
			state = new AISTATE(key, "walking") {

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					// a.stats.staminaIncrement(-0.3f*ds);
					d.stateTimer -= ds;
					return d.stateTimer > 0;
				}

			};
			
		}

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			d.stateTimer = (float) time;
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return state;
		};

		AISTATE activate(Humanoid a, AIManager d, float time, float x, float y) {
			a.speed.turn2(x, y).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		public AISTATE activate(Humanoid a, AIManager d, float time, ENTITY other) {
			a.speed.turn2(a.body(), other.body()).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}
		
		public AISTATE activateFRom(Humanoid a, AIManager d, float time, ENTITY other) {
			a.speed.turn2(other.body(), a.body()).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		AISTATE activate(Humanoid a, AIManager d, float time, double deg) {
			a.speed.turnWithAngel(deg);
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		AISTATE activateRND(Humanoid a, AIManager d, float time) {
			a.speed.turnRandom();
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}



	}

	public static class STOP {

		private final AISTATE state;
		
		
		public STOP(String key, HSprite s) {
			state = new AISTATE(key, "stopping") {

				@Override
				public HSprite sprite(Humanoid a) {
					return a.speed.magnitude() >= a.speed.magintudeMax() ? HSprites.LAY : s;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					
					if (a.speed.isZero()) {
						d.stateTimer -= ds;
						return d.stateTimer > 0;
					}
					a.speed.brake(ds);
					return true;
				}

			};
		}

		public AISTATE activate(Humanoid a, AIManager d) {
			d.stateTimer = 0.1f;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};
		
		public AISTATE instant(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			d.stateTimer = 0.1f;
			return state;
		};
		
		public AISTATE aDirRND(Humanoid a, AIManager d, float time) {
			a.speed.turnRandom();
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			d.stateTimer = (float) time;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};
		
		
	}
	
	public static class STOP_DIV {

		private final AISTATE state;
		
		private final double max = C.TILE_SIZE*C.TILE_SIZE*4*4;
		
		public STOP_DIV(String key, HSprite s) {
			state = new AISTATE(key, "stopping div") {

				@Override
				public HSprite sprite(Humanoid a) {
					return a.speed.magnitude() >= a.speed.magintudeMax() ? HSprites.LAY : s;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					
					if (a.speed.isZero()) {
						d.stateTimer -= ds;
						return d.stateTimer > 0;
					}
					
					if (!goingTowardsDiv(a, d))
						a.speed.brake(ds);
					return true;
				}

			};
		}

		private boolean goingTowardsDiv(Humanoid a, AIManager d) {
			Div div = a.division();
			if (div != null) {
				
				if (div.reporter.posHas(a)) {
					COORDINATE dest = div.reporter.getPixel(a);
					double dx = dest.x()-a.body().cX();
					double dy = dest.y() -a.body().cY();
					
					
					
					if (dx*dx + dy*dy < max) {
						if (dx*a.speed.nX() >= 0 && dy*a.speed.nY() >= 0)
							return true;
					}
					
				}
			}
			return false;
		}
		
//		public AISTATE activate(Humanoid a, AIManager d) {
//			d.stateTimer = 0.1f;
//			a.speed.magnitudeTargetSet(0);
////			a.speed.magnitudeAdjust(0.25f, 1, 1);
//			return state;
//		};
//		
//		public AISTATE instant(Humanoid a, AIManager d) {
//			a.speed.magnitudeInit(0);
//			a.speed.magnitudeTargetSet(0);
////			a.speed.magnitudeAdjust(0.25f, 1, 1);
//			d.stateTimer = 0.1f;
//			return state;
//		};
//		
//		public AISTATE aDirRND(Humanoid a, AIManager d, float time) {
//			a.speed.turnRandom();
//			d.stateTimer = time;
//			a.speed.magnitudeTargetSet(0);
////			a.speed.magnitudeAdjust(0.25f, 1, 1);
//			return state;
//		};
//
		public AISTATE activate(Humanoid a, AIManager d, double time) {
			d.stateTimer = (float) time;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};
		
		
	}
	
	

	public class FLY {

		private final AISTATE state;
		private FLY(String key) {
			state = new AISTATE(key, "flying") {

				@Override
				public HSprite sprite(Humanoid a) {
					return HSprites.LAY;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					a.physics.setHeightOverGround(a.physics.getZ() + ds * d.stateTimer * C.TILE_SIZE);
					d.stateTimer -= ds;
					if (a.physics.getZ() < 0) {
						a.physics.setHeightOverGround(0);
						return false;
					}
					return true;
				}

			};
		}

		public AISTATE activate(Humanoid a, AIManager d, float seconds) {
			d.stateTimer = seconds / 2f;
			return state;
		};
		
		public AISTATE add(Humanoid a, AIManager d, float seconds) {
			d.stateTimer += seconds / 2f;
			return state;
		};

	}

	public class MOVE_TOWARDS {

		private final AISTATE state;

		private MOVE_TOWARDS(String key) {
			this(key, HSprites.MOVE);
		}
		
		public MOVE_TOWARDS(String key, HSprite sprite) {
			state = new AISTATE(key, "walking") {

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					float dx = d.X - a.body().cX();
					float dy = d.Y - a.body().cY();

					if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
						a.physics.body().moveC(d.X, d.Y);
						return false;
					} else if (dx == 0 && dy == 0) {
						return false;
					}
					d.stateTimer -= ds;
					if (d.stateTimer <= 0) {
						return false;
					}
					return true;
				}

			};
		}

		public AISTATE move(Humanoid a, AIManager d, int destX, int destY, double time, double speed) {
			d.X = destX;
			d.Y = destY;
			if (d.X != a.physics.body().cX() || d.Y != a.physics.body().cY()) {
				a.speed.turn2(a, d.X, d.Y).magnitudeTargetSetPrecise(speed);

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (float) time;
			return state;
		}

	}
	
	public class PUSH_TOWARDS {

		private final AISTATE state;
		
		public PUSH_TOWARDS(String key, HSprite sprite) {
			state = new AISTATE(key, "pushing") {

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					
					double dx = d.X - a.body().cX();
					double dy = d.Y - a.body().cY();
					if (ds == 0)
						return true;
					
					if (dx == 0 && dy == 0)
						return false;
					
					double nx = a.speed.x();
					double ny = a.speed.y();
					double mag = a.speed.magnitude();
					if (mag > 0 && (dx * nx < 0 || dy*ny < 0)) {
						double m = mag - ds*(4*C.TILE_SIZE+a.speed.magnitude()*0.1);
						if (m < 0) {
							m = 0;
							a.speed.turn2(dx, dy);
						}
						a.speed.magnitudeInit(m);
						a.speed.setDirCurrent(DIR.get(dx, dy));
						a.speed.setPrevDir();
					}else {
						if (same(dx, nx) != same(dy, ny)) {
							a.speed.setPrevDir();
							a.speed.turn2(dx, dy);
						}
						
						a.speed.magnitudeAdjust(ds, 1.0, 1.0);
						
						double ddx = a.body().cX()-d.X;
						double ddy = a.body().cY()-d.Y;
						
						if (ddx*(ddx+Math.ceil(a.speed.x()*ds)) < 0 || ddy*(ddy+Math.ceil(a.speed.y()*ds)) < 0) {
							a.physics.body().moveC(d.X, d.Y);
							return false;
						}
						
					}
					
					
					
					d.stateTimer -= ds;
					return d.stateTimer > 0;
					
				}
				
				private double same(double dx, double sx) {
					if (dx == sx)
						return 1;
					if (dx == 0) {
						return sx == 0 ? 1 : 0;
					}
					return dx/sx;
				}

			};
		}

		public AISTATE move(Humanoid a, AIManager d, int destX, int destY, double time, double speed) {
			d.X = destX;
			d.Y = destY;
			a.speed.magnitudeTargetSet(speed);
			d.stateTimer = (float) time;
			return state;
		}

	}
	
	public static class WALK_DEST {

		private final AISTATE state;
		private final double speed;

		public WALK_DEST(String key, double speed, HSprite sprite) {
			this.speed = speed;

			state = new AISTATE(key, "walking") {

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					float dx = d.X - a.body().cX();
					float dy = d.Y - a.body().cY();

					if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
						a.physics.body().moveC(d.X, d.Y);
						return false;
					} else if (dx == 0 && dy == 0) {
						return false;
					}
					d.stateTimer -= ds;
					if (d.stateTimer <= 0) {
						LOG.ln(ds + " " + a.tc() + " " + a.body().cX() + " " + d.X + " " + a.speed.nX() + " " + a.speed.magnitude() + " " + a.speed.magintudeMax() + " " + BOOSTABLES.PHYSICS().SPEED.get(a.indu())*C.TILE_SIZE);
						d.debug(a, "strange");
//						GAME.Notify(a.speed.magnitude() + " " + a.physics.tileC() + " " + d.path + " " + a.speed.nX()
//								+ " " + a.speed.nY());
						d.stateTimer = (10f);
						a.physics.body().moveC(d.X, d.Y);
						return false;
					}
					return true;
				}

			};
		}
		
		private WALK_DEST(String key, double speed) {
			this(key, speed, HSprites.MOVE);
		}
		
		private WALK_DEST(String key, boolean shittycoding) {
			this.speed = 0.3;
			state = new AISTATE(key, "dragging") {

				@Override
				public HSprite sprite(Humanoid a) {
					return HSprites.DRAG;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					float dx = d.X - a.body().cX();
					float dy = d.Y - a.body().cY();

					if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
						a.physics.body().moveC(d.X, d.Y);
						return false;
					} else if (dx == 0 && dy == 0) {
						return false;
					}
					d.stateTimer -= ds;
					if (d.stateTimer <= 0) {
						GAME.Notify(a.speed.magnitude() + " " + a.physics.tileC() + " " + d.path + " " + a.speed.nX()
								+ " " + a.speed.nY());
						d.stateTimer = (48f);
					}
					DRAGGABLE c = DRAGGABLE_HOLDER.all().get(d.subPathByte).draggable((short) d.planObject);
					if (c != null) {
						if (c.canBeDragged())
							c.drag(a.speed.dir(), a.physics.body().cX(), a.physics.body().cY(), a.physics.body().width()<<1);
					}else {
						d.debug(a, "draggable has mysteriously dissapeared!");
						
					}
					
					return true;
				}

			};
		}

		public AISTATE cTile(Humanoid a, AIManager d) {
			int x2 = (a.physics.tileC().x() << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (a.physics.tileC().y() << C.T_SCROLL) + C.TILE_SIZEH;
			return free(a, d, x2, y2);
		};

//		public AISTATE path(Humanoid a, AIManager d) {
//			int x2 = d.path.getSettCX();
//			int y2 = d.path.getSettCY();
//			if (d.path.isDest()) {
//				int dd = (C.TILE_SIZE - a.species().physics.hitBoxsize() - 2) / 2;
//				if (d.path.isFull()) {
//					if (dd > 3)
//						dd = 3;
//					x2 += RND.rInt0(dd);
//					y2 += RND.rInt0(dd);
//				} else {
//					int dy = d.path.destY() - d.path.y();
//					int dx = d.path.destX() - d.path.x();
//					x2 += dx * dd;
//					y2 += dy * dd;
//				}
//			}
//			return free(a, d, x2, y2);
//		};
		//natural pathing
		public AISTATE path(Humanoid a, AIManager d) {

			int x2 = d.path.getSettCX();
			int y2 = d.path.getSettCY();
			
			if (x2 == a.physics.body().cX() && y2 == a.physics.body().cY()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			if (a.physics.tileC().isSameAs(d.path) && a.physics.isWithinTile()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			
			a.speed.turn2(a, x2, y2);
			double dy = Math.max(
					(a.body().y2()+2)-(d.path.y()+1)*C.TILE_SIZE, 
					d.path.y()*C.TILE_SIZE - (a.body().y1()-2))
					/ Math.abs(a.speed.nY());
			double dx = Math.max(
					(a.body().x2()+2)-(d.path.x()+1)*C.TILE_SIZE, 
					d.path.x()*C.TILE_SIZE - (a.body().x1()-2)) 
					/ Math.abs(a.speed.nX());
			
			if (dx > dy) {
				x2 =  (int) (a.physics.body().cX() + a.speed.nX()*dx);
				y2 = (int) (a.physics.body().cY() + a.speed.nY()*dx);
			}else {
				x2 =  (int) (a.physics.body().cX() + a.speed.nX()*dy);
				y2 = (int) (a.physics.body().cY() + a.speed.nY()*dy);
			}
			d.X = x2;
			d.Y = y2;
			a.speed.turn2(a, x2, y2);
			a.speed.magnitudeTargetSet(speed + RND.rFloat(0.05));
			d.stateTimer = (48f);
			return state;
			
			
			
		};
		
		public AISTATE edge(Humanoid a, AIManager d, DIR dir) {

			int dd = (C.TILE_SIZE - a.body().width() - 1) / 2;
			
			int x2 = a.physics.tileC().x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*dd;
			int y2 = a.physics.tileC().y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*dd;
			
			if (x2 == a.physics.body().cX() && y2 == a.physics.body().cY()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			if (!a.physics.isWithinTile()) {
				return cTile(a, d);
			}
			
			free(a, d, x2, y2);
			a.speed.magnitudeTargetSet(0.2);
			d.stateTimer = (48f);
			return state;
			
			
			
		};
		
		public AISTATE moveToEdge(Humanoid a, AIManager d, DIR dir) {

			int x2 = (a.tc().x())*C.TILE_SIZE + C.TILE_SIZEH;
			int y2 = (a.tc().y())*C.TILE_SIZE + C.TILE_SIZEH;
			int dd = ((C.TILE_SIZE - a.body().width())-2) / 2;
			x2 += dir.x() * dd;
			y2 += dir.y() * dd;
			
			return free(a, d, x2, y2);
			
			
			
		};

		public AISTATE dirTile(Humanoid a, AIManager d, DIR dir) {
			int x2 = (a.physics.tileC().x() << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (a.physics.tileC().y() << C.T_SCROLL) + C.TILE_SIZEH;
			x2 += dir.x() * C.TILE_SIZE;
			y2 += dir.y() * C.TILE_SIZE;
			return free(a, d, x2, y2);
		}

		public AISTATE free(Humanoid a, AIManager d, int x2, int y2) {
			d.X = x2;
			d.Y = y2;
			if (x2 != a.physics.body().cX() || y2 != a.physics.body().cY()) {
				a.speed.turn2(a, x2, y2).magnitudeTargetSet(speed + RND.rFloat(0.05));

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (48f);
			return state;
		}

		public AISTATE tile(Humanoid a, AIManager d, int tx, int ty) {
			int x2 = (tx << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (ty << C.T_SCROLL) + C.TILE_SIZEH;
			d.X = x2;
			d.Y = y2;
			if (x2 != a.physics.body().cX() || y2 != a.physics.body().cY()) {
				a.speed.turn2(a, x2, y2).magnitudeTargetSet(speed + RND.rFloat(0.05));

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (48f);
			return state;
		}

	}

	public final class Animations {

		private Animations() {
			
		}
		
		public final Animation work = new Animation("A1", "Working", HSprites.TOOL_HIT);
		public final Animation box = new Animation("A2", "box", HSprites.GRAB);
		public final Animation wave = new Animation("A3", "waiving", HSprites.WAVE);
		public final Animation throww = new Animation("A4", "throw", HSprites.THROW);
		public final Animation stand = new Animation("A5", "waiving", HSprites.STAND);
		public final Animation grab = new Animation("A6", "grabbing", HSprites.BOX);
		public final Animation fist = new Animation("A7", "shaking fist", HSprites.FIST);
		public final Animation fistRight = new Animation("A8", "shaking fist", HSprites.ARM_RIGHT);
		public final Animation fistLeft = new Animation("A9", "shaking fist", HSprites.ARM_LEFT);
		public final Animation dance = new Animation("A10", "shaking fist", HSprites.DANCE);
		public final Animation danceE = new Animation("A11", "shaking fist", HSprites.DANCE_EXTRA);
		public final Animation sword = new Animation("A12", "bracing", HSprites.SWORD_STAND);
		public final Animation stab = new Animation("A13", "stab", HSprites.SWORD_STAB);
		public final Animation sword_out = new Animation("A14", "bracing", HSprites.SWORD_OUT);
		public final Animation sword_in = new Animation("A15", "bracing", HSprites.SWORD_IN);
		public final Animation lay = new Animation("A16", "laying", HSprites.LAY);
		public final Animation carry = new Animation("A17", "carry", HSprites.CARRY);
		public final Animation armsOut = new Animation("A18", "carry", HSprites.ARMS_OUT);
		public final Animation layoff = new Animation("A19", "laying", HSprites.LAYOFF);
		public final Animation archer0 = new Animation("A20", "archer", HSprites.ARCHER2);
		public final Animation archer1 = new Animation("A21", "archer", HSprites.ARCHER3);
		public final Animation archer2 = new Animation("A22", "archer", HSprites.ARCHER4);
		public final Animation toolSlam = new AISTATES.Animation("W_TOOL", "working", HSprites.TOOL_HIT);
		public final Animation toolBack = new AISTATES.Animation("W_TOOL_BACK", "working", HSprites.TOOL_BACK);
		
		
		// final Animation sword_stab = new Animation("stabbing", Sprite.SWORD_STAB);

	}
	
	public final class AnimationArrays {

		private AnimationArrays() {
			
		}
		private final Animation[] speak = new Animation[] {
			anima.carry,
			anima.fist,
			anima.grab,
			anima.fistRight,
			anima.fistRight,
			anima.fistRight,
		};
		private final Animation[] dance = new Animation[] {
			anima.carry,
			anima.fist,
			anima.grab,
			anima.fistRight,
			anima.fistLeft,
			anima.dance,
			anima.dance,
			anima.dance,
			anima.danceE,
			anima.danceE,
			anima.danceE,
		};
		private final Animation[] lecture = new Animation[] {
			anima.box,
			anima.fist,
			anima.grab,
			anima.wave,
		};
		
		public Animation speak() {
			return get(speak);
		}
		
		public Animation dance() {
			return get(dance);
		}
		
		public Animation lecture() {
			return get(lecture);
		}
		
		public Animation get(Animation[] as) {
			return as[RND.rInt(as.length)];
		}
	}

	public static final class Animation {

		final AISTATE state;
		public final double time;

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			a.spriteTimer = 0;
			d.stateTimer = ((float) time);
			return state;
		}
		
		public AISTATE resume(Humanoid a, AIManager d, double time) {
			d.stateTimer = ((float) time);
			return state;
		}

		public AISTATE activate(Humanoid a, AIManager d) {
			a.spriteTimer = 0;
			d.stateTimer = (float) (state.sprite(a).time);
			return state;
		}

		public Animation(String key, String name, HSprite sprite) {

			state = new AISTATE(key, name) {

				@Override
				public boolean update(Humanoid a, AIManager d, double ds) {
					d.stateTimer -= ds;
					return d.stateTimer >= 0;
				}

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}
			};

			time = sprite.time;
		}

	}
	
	public static final class Sword {
		
		private Sword() {
			
		}
		
		public final AISTATES.STOP STOP_SWORD = new AISTATES.STOP("SF_STAND", HSprites.SWORD_STAND);
		public final AISTATES.WALK RUN = new AISTATES.WALK("SF_RUN", 0.9, HSprites.SWORD_STAND);
		public final AISTATES.WALK WALK = new AISTATES.WALK("SF_WALK", 0.2, HSprites.SWORD_STAND);
		public final AISTATE strike = new AISTATE.Custom("SF_STRIKE", "striking", HSprites.SWORD_OUT) {
			@Override
			public boolean update(Humanoid a, AIManager d, double ds) {
				a.speed.magnitudeAdjust(ds, 1.5, 1.0);
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};
		public final AISTATE strikeIn = new AISTATE.Custom("SF_STRIKE2", "striking", HSprites.SWORD_IN) {
			@Override
			public boolean update(Humanoid a, AIManager d, double ds) {
				a.speed.magnitudeAdjust(ds, 1.5, 1.0);
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};
		public final AISTATE backup = new AISTATE.Custom("SF_BACKUP", "backing up", HSprites.SWORD_STAND) {
			@Override
			public boolean update(Humanoid a, AIManager d, double ds) {
				a.speed.magnitudeAdjust(ds, 1.0, 1.0);
				// int dist = a.body().getDistance(d.otherEntity().body());
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};
	}

}
