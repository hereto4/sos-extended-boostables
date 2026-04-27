package settlement.entity;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import game.GAME;
import init.constant.C;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.GUTIL;

public class ResolverTile {

	private ResolverTile() {

	}

	private static double attack(int tx, int ty, double m, ENTITY e) {
		
		if (!SETT.IN_BOUNDS(tx, ty))
			return m;
		
		
		if (e instanceof Humanoid && GAME.ARMIES().map.attackableI.is(tx, ty, ((Humanoid) e).indu())) {
			double str = GAME.ARMIES().map.strength.get(tx, ty)*2;
			str +=  RND.rFloat()*str;
			
			if (m > str + RND.rFloat()*str) {
				GAME.ARMIES().map.breakIt(tx, ty);
				return str;
			}
			if (!collides(tx, ty, e)) {
				return CLAMP.d(m-str, 0, m);
			}
		}
		
		return m;
		
	}
	
	private static void resolve(int tx, int ty, ENTITY a, EPHYSICS.Solid p) {

		double x1 = p.body().x1();
		double ox1 = p.x1;
		double y1 = p.body().y1();
		double oy1 = p.y1;
		double size = p.body().width();

		double vx = (x1 - ox1);
		final double targetX;
		if (vx > 0) {
			targetX = (tx << C.T_SCROLL) - size - 1;
		} else if (vx < 0) {
			targetX = (tx << C.T_SCROLL) + C.TILE_SIZE+1;
		} else {
			targetX = x1;
		}

		double vy = (y1 - oy1);
		final double targetY;
		if (vy > 0) {
			targetY = (ty << C.T_SCROLL) - size - 1;
		} else if (vy < 0) {
			targetY = (ty << C.T_SCROLL) + C.TILE_SIZE+1;
		} else {
			targetY = y1;
		}

		
		
		if (vy == 0 && vx == 0) {
			GAME.Notify("entity stuck in tile, but havent moved " + tx + " " + ty);
//			int cx = (p.hitbox.cX() >> C.T_SCROLL);
//			int cy = (p.hitbox.cY() >> C.T_SCROLL);
//			if (map().getAvailability(cx, cy).tileCollide)
//				return;
//			int dx = cx - tx;
//			int dy = cy - ty;
//			if (dx == 0 && dy == 0)
//				return;
//			p.hitbox.moveX1((tx + dx) >> C.T_SCROLL);
//			p.hitbox.moveX1((ty + dy) >> C.T_SCROLL);
		} else if (vx == 0) {
			p.hitbox.moveY1(targetY);
			if (vy * a.speed.nY() > 0) {
				double m = Math.abs(a.speed.y() * p.getMass());
				double m2 = attack(tx, ty, m, a);
				boolean broken = m2 < m;
				if (a.collideTile(broken, 0, vy < 0 ? -1 : 1, m2*a.physics.massI, tx, ty)) {
					if (broken) {
						double ny = m2 * p.getMassI();
						if (a.speed.nY() < 0)
							ny = -ny;
						a.speed.setRaw(a.speed.x(), ny);
					} else {
						a.speed.setRaw(a.speed.x(), -a.speed.y() * p.getRestitution());
					}
				}else if(!broken) {
					a.speed.magnitudeInit(0);
				}

			}
		} else if (vy == 0) {
			p.hitbox.moveX1(targetX);
			if (vx * a.speed.nX() > 0) {
				double m = Math.abs(a.speed.x() * p.getMass());
				double m2 = attack(tx, ty, m, a);
				boolean broken = m2 < m;
				if (a.collideTile(broken, vx < 0 ? -1 : 1, 0, m2*a.physics.massI, tx, ty)) {
					if (broken) {
						double nx = m2 * p.getMassI();
						if (a.speed.nX() < 0)
							nx = -nx;
						a.speed.setRaw(nx, a.speed.y());
					} else {
						a.speed.setRaw(-a.speed.x() * p.getRestitution(), a.speed.y());
					}
				}else if(!broken) {
					a.speed.magnitudeInit(0);
				}
			}
		} else {
			double dx = targetX - x1;
			double dy = targetY - y1;
			double xx = Math.abs(dx / vx);
			double yy = Math.abs(dy / vy);
			if (xx == yy) {
				p.hitbox.moveX1(targetX);
				p.hitbox.moveY1(targetY);
				double mres = 0;
				double sx = a.speed.x();
				double sy = a.speed.y();
				if (vx * a.speed.nX() > 0) {
					double m = Math.abs(a.speed.x() * p.getMass());
					double m2 = attack(tx, ty, m, a);
					if (m2 < m) {
						if (a.speed.x() < 0)
							m2 *= -1;
						mres += m2;
						sx = m2 * p.getMassI();
					} else {
						mres += m;
						sx = -a.speed.x() * p.getRestitution();
					}
				}
				if (vy * a.speed.nY() > 0) {
					double m = Math.abs(a.speed.y() * p.getMass());
					double m2 = attack(tx, ty, m, a);
					if (m2 < m) {
						if (a.speed.y() < 0)
							m2 *= -1;
						sy = m2 * p.getMassI();
						mres += m2;
					} else {
						sy = -a.speed.y() * p.getRestitution();
						mres += m;
					}
				}
				
				if (a.collideTile(false, vx < 0 ? -1 : 1, vy < 0 ? -1 : 1, mres*a.physics.massI, tx, ty)) {
					a.speed.setRaw(sx, sy);
				}else {
					a.speed.magnitudeInit(0);
				}

			} else if (xx < yy) {
				p.hitbox.moveX1(targetX);
				double d = xx / yy;
				p.hitbox.incrY(-vy * d);
				if (vx * a.speed.nX() > 0) {

					double m = Math.abs(a.speed.x() * p.getMass());
					double m2 = attack(tx, ty, m, a);
					boolean broken = m2 < m;
					if (a.collideTile(broken, vx < 0 ? -1 : 1, 0, m2*a.physics.massI, tx, ty)) {
						if (broken) {
							double nx = m2 * p.getMassI();
							if (a.speed.nX() < 0)
								nx = -nx;
							a.speed.setRaw(nx, a.speed.y());
						} else {
							a.speed.setRaw(-a.speed.x() * p.getRestitution(), a.speed.y());
						}
					}else if(!broken){
						a.speed.magnitudeInit(0);
					}

				}
			} else {
				p.hitbox.moveY1(targetY);
				double d = yy / xx;
				p.hitbox.incrX(-vx * d);
				p.hitbox.moveY1(targetY);
				if (vy * a.speed.nY() > 0) {

					double m = Math.abs(a.speed.y() * p.getMass());
					double m2 = attack(tx, ty, m, a);
					boolean broken = m2 < m;
					if (a.collideTile(broken, 0, vy < 0 ? -1 : 1, m2*a.physics.massI, tx, ty)) {
						if (broken) {
							double ny = m2 * p.getMassI();
							if (a.speed.nY() < 0)
								ny = -ny;
							a.speed.setRaw(a.speed.x(), ny);
						} else {
							a.speed.setRaw(a.speed.x(), -a.speed.y() * p.getRestitution());
						}
					}else if(!broken) {
						a.speed.magnitudeInit(0);
					}

				}
			}

		}
	}

	private static boolean collides(int tx, int ty, ENTITY e) {
		AVAILABILITY a = map().getAvailability(tx, ty);
		if (!a.tileCollide)
			return false;
		if (e instanceof Humanoid) {
			return a.isSolid(((Humanoid)e).indu().army());
		}
		return a.player < 0;
	}
	
	private static boolean resolve(ENTITY a, EPHYSICS.Solid p) {
		int tx1 = (p.hitbox.x1() >> C.T_SCROLL);
		int ty1 = (p.hitbox.y1() >> C.T_SCROLL);
		int tx2 = (p.hitbox.x2() >> C.T_SCROLL);
		int ty2 = (p.hitbox.y2() >> C.T_SCROLL);
		if (tx1 == p.tx1 && ty1 == p.ty1 && tx2 == p.tx2 && ty2 == p.ty2) {
			return false;
		}

		if (collides(tx1, ty1, a)) {
			resolve(tx1, ty1, a, p);
			return true;
		}
		if (tx1 != tx2 && collides(tx2, ty1, a)) {
			resolve(tx2, ty1, a, p);
			return true;
		}
		if (ty1 != ty2 && collides(tx1, ty2, a)) {
			resolve(tx1, ty2, a, p);
			return true;
		}
		if (tx1 != tx2 && ty1 != ty2 &&collides(tx2, ty2, a)) {
			resolve(tx2, ty2, a, p);
			return true;
		}

		p.tx1 = (short) tx1;
		p.ty1 = (short) ty1;
		p.tx2 = (short) tx2;
		p.ty2 = (short) ty2;
		
		if (map().getAvailability(p.tileC().x(), p.tileC().y()).player < 0)
			a.collideUnconnected();
		
		return false;
	}

	public static boolean collide(ENTITY a) {

		EPHYSICS.Solid p = a.physics;

		if (resolve(a, p)) {

			int i = 0;
			while (resolve(a, p)) {
				i++;
				if (i > 4) {
					GAME.Notify("killing trapped entity... solong" + " " + a.physics.tileC());
					a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
					return false;
				}
			}

			int tx1 = (p.hitbox.x1() >> C.T_SCROLL);
			int ty1 = (p.hitbox.y1() >> C.T_SCROLL);
			int tx2 = (p.hitbox.x2() >> C.T_SCROLL);
			int ty2 = (p.hitbox.y2() >> C.T_SCROLL);

			p.tx1 = (short) tx1;
			p.ty1 = (short) ty1;
			p.tx2 = (short) tx2;
			p.ty2 = (short) ty2;
			return true;
		}

		return false;
	}

	private static settlement.path.PATHING map() {
		return PATH();
	}

	static boolean trapped(ENTITY a) {

		EPHYSICS.Solid p = a.physics;

		int cx = p.hitbox.cX();
		int cy = p.hitbox.cY();
		int tx = cx >> C.T_SCROLL;
		int ty = cy >> C.T_SCROLL;
		int index = 0;
		
		while(index < GUTIL.circle().length()) {
			index++;
			if (GUTIL.circle().radius(index) > 10)
				break;
			COORDINATE c = GUTIL.circle().get(index);
			int dx = c.x()+tx;
			int dy = c.y()+ty;
			if (!IN_BOUNDS(dx, dy))
				continue;
			if (!collides(dx, dy, a)) {
				

				p.hitbox.moveC((dx<<C.T_SCROLL)+C.TILE_SIZEH, (dy<<C.T_SCROLL)+C.TILE_SIZEH);
				
				double norX = dx - tx;
				double norY = dy - ty;

				p.hitbox.moveC((dx<<C.T_SCROLL)+C.TILE_SIZEH, (dy<<C.T_SCROLL)+C.TILE_SIZEH);
				
				if (norX < 0) {
					
					norX = -1;
				} else if (norX > 0) {
					
					norX = 1;
				}
				if (norY < 0) {
					
					norY = -1;
				} else if (norY > 0) {
					
					norY = 1;
				}

				if (norX != 0 && norY != 0) {
					norX *= C.SQR2I;
					norY *= C.SQR2I;
				}

				a.collideTile(false, norX, norY, 0, tx, ty);
				
				int tx1 = (p.hitbox.x1() >> C.T_SCROLL);
				int ty1 = (p.hitbox.y1() >> C.T_SCROLL);
				int tx2 = (p.hitbox.x2() >> C.T_SCROLL);
				int ty2 = (p.hitbox.y2() >> C.T_SCROLL);

				p.tx1 = (short) tx1;
				p.ty1 = (short) ty1;
				p.tx2 = (short) tx2;
				p.ty2 = (short) ty2;
				return true;

			}
		}

		System.err.println("killing trapped entity... solong");
		a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
		return false;
	}

}
