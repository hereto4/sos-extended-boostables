package settlement.thing.projectiles;

import game.GAME;
import game.battle.Army;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.thing.projectiles.PData.Data;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.GEO;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class Updater {

	private final SProjectiles p;
	private static final VectorImp vec = new VectorImp();
	private final double max = 1/64.0;
	
	Updater(SProjectiles p) {
		this.p = p;
	}

	void update(final int i, double dd) {

		float ds = (float) max;
		while(dd > 0) {
			
			dd-=max;
			Data d = p.data.data(i);

			d.dzSet(d.dz() - Trajectory.G * ds);
			
			float z = d.z() + d.dz() * ds;
			if (z <= 0) {
				ds *= d.z() / (d.z() - z);
				z = d.z() + d.dz() * ds;
			}
			d.zSet(z);
			
			double mag = d.dMagnitude();
			mag -= Trajectory.FRICTION * ds;
			if (mag < 0)
				mag = 0;
			d.magnitudeSet(mag);
			
			float x = (float) (d.x() + d.speedX() * ds);
			float y = (float) (d.y() + d.speedY() * ds);
			

			if (d.z() <= 0) {
				if (collide(i, d, x, y, p.data.type(i)))
					return;
				p.data.type(i).soundHit().rnd((int) x, (int) y, 1);
				p.data.type(i).impact(p.data.ref(i), x, y, d.speedX(), d.speedY(), d.dz());
				p.data.remove(i);
				
				return;
			} 
			if (collide(i, d, x, y, p.data.type(i)))
				return;
			
			if (!p.data.move(i, x, y))
				return;
			
		}
		
		

	}

	private final Rec rTile = new Rec(C.TILE_SIZE);
	
	private boolean collide(int e, Data d, float destX, float destY, Projectile type) {

		destY /= C.TILE_SIZE;
		destX /= C.TILE_SIZE;

		final double startX = d.x() / C.TILE_SIZE;
		final double startY = d.y() / C.TILE_SIZE;

		if ((int) destX == (int) startX && (int) destY == (int) startY && d.z() > 0)
			return false;

		double dx = destX - startX;
		double dy = destY - startY;
		double adx = Math.abs(dx);
		double ady = Math.abs(dy);
		double mag = 0;
		if (adx > ady) {
			mag = adx;
		} else {
			if (ady <= 0)
				mag = 1;
			else
				mag = ady;
		}

		dx /= mag;
		dy /= mag;

		double x = startX;
		double y = startY;

		
		while (mag > 0) {
			double dd = CLAMP.d(mag, 0, 1);
			double ox = x;
			double oy = y;
			x += dx * dd;
			y += dy * dd;
			mag -= 1;
			int tx = (int) x;
			int ty = (int) y;
			
			if (!SETT.IN_BOUNDS(tx, ty)) {
				p.data.remove(e);
				return true;
			}
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			int min = t.heightStart(tx, ty) * C.TILE_SIZE;
			int max = t.heightEnd(tx, ty) * C.TILE_SIZE;
			
			boolean tree = SETT.TERRAIN().TREES.isTree(tx, ty);
			
			if (tree && (e & 1) == 0) {
				
			}else if (d.z() <= 0 ||  (d.z() > min && d.z() < max)) {
				
				p.data.type(e).soundHit().rnd((int) (x * C.TILE_SIZE), (int) (y * C.TILE_SIZE), 1);
				double mom = d.dMagnitude()*p.data.type(e).mass(p.data.ref(e));
				double str = GAME.ARMIES().map.strength.get(tx, ty);
				if (p.data.live(e) && mom > str*RND.rFloat()) {
					GAME.ARMIES().map.breakIt(tx, ty);
					double ddd = (mom-str)/mom;
					ddd = Math.max(ddd, 0);
					d.magnitudeSet(d.dMagnitude()*ddd);
					continue;
				}
				
				type.impact(p.data.ref(e),  x*C.TILE_SIZE, y*C.TILE_SIZE, d.speedX(), d.speedY(), d.dz());
				
				
				if (d.z() > 0 && !tree && RND.oneIn(5)) {

					
					double cx = tx + 0.5;
					double cy = ty + 0.5;
					double prevX = x - dx * dd;
					double prevY = y - dy * dd;
					double ddx = Math.abs(cx - prevX);
					double ddy = Math.abs(cy - prevY);
					double r = RND.rFloat();
					d.magnitudeSet(d.dMagnitude()*r);
					if (ddx > ddy) {
						d.nxSet(-d.nx());
					} else {
						d.nySet(-d.ny());
					}
					p.data.move(e, prevX * C.TILE_SIZE, prevY * C.TILE_SIZE);
					
				} else {

					p.data.remove(e);

				}
				return true;
			}
			
			int hel = (int) (t.heightEnt(tx, ty) * C.TILE_SIZE);
			int heh = hel + Trajectory.HIT_HEIGHT;
			if (d.z() >= hel && d.z() < heh) {
				rTile.moveC(x*C.TILE_SIZE, y*C.TILE_SIZE);
				for (ENTITY ent : SETT.ENTITIES().fill(rTile)) {
					
					if (!intesects(ent, ox, oy, x, y))
						continue;
					
					double ref = p.data.ref(e);
					
					//Report to div
					if (ent instanceof Humanoid) {
						Humanoid a = (Humanoid) ent;
						if (a.division() != null)
							GAME.ARMIES().factors.reportProjectile(a.division());
					}
					//speed
					double esx = ent.speed.x();
					double esy = ent.speed.y();
					double xs = d.speedX()-esx;
					double ys = d.speedY()-esy;
					double speed = Math.sqrt(xs*xs + ys*ys + d.dz()*d.dz());
					
					
					
					vec.set(ox*C.TILE_SIZE, oy*C.TILE_SIZE, ent.body().cX(), ent.body().cY());
					if (GAME.battle().fight.projectileAttack(ent, vec.nX(), vec.nY(), speed, type, ref)) {
						p.data.type(e).soundHit().rnd((int) (x * C.TILE_SIZE), (int) (y * C.TILE_SIZE), 1);
						
						if (ent.isRemoved()) {
							
							//report the kill
							ENTITY o = p.data.shooter(e);
							if (o != null && o instanceof Humanoid) {
								Humanoid a = (Humanoid) o;
//								if (ent instanceof Humanoid) {
//									Humanoid a2 = (Humanoid) ent;
//									if (a2.division() != null && a.division() != null && a.division().player() != a2.division().player())
//										LOG.ln("ops");
//								}
								STATS.BATTLE().makeAKill(a);
							}
							
							//how much momentum is lost from passing through the body
							double mom = type.mass(ref) * speed;
							double momExchange = ent.physics.getMass()*C.TILE_SIZE*4;
							if (mom - momExchange > 0) {
								//the projectile passed right through, and will continue its journey through more bodies.
								mom-= momExchange;
								mom/= type.mass(ref);
								double ddd = mom/speed;
								d.magnitudeSet(d.dMagnitude()*ddd);
							}else {
								type.impact(ref, x*C.TILE_SIZE, y*C.TILE_SIZE, d.speedX(), d.speedY(), d.dz());
								p.data.remove(e);
								return true;
							}
							
						}else{
							//the arrow was absorbed by the entity
							type.impact(ref, x*C.TILE_SIZE, y*C.TILE_SIZE, d.speedX(), d.speedY(), d.dz());
							p.data.remove(e);
							return true;
							
						}
						
					}
					
				}
			}
		}
		
		return false;
	}
	

	static CharSequence test(Army ally, Trajectory traj, double height, double sx, double sy) {
		
		double time = traj.getTime(height);
		float vz = (float) traj.vz();
//		float vx = (float) traj.vx();
//		float vy = (float) traj.vy();
		double mag = vec.set(traj.vx(), traj.vy());
		double length = Trajectory.getLength(mag, time);
		
		length *= C.ITILE_SIZE;
		
		double ds = time/length;
		int imax = (int) Math.ceil(length);
		
		
		double x = sx;
		double y = sy;
		double z = height;
		
		for (int i = 0; i < imax; i++) {
			

			mag -= Trajectory.FRICTION * ds;
			if (mag < 0)
				mag = 0;
			
			vz -= Trajectory.G*ds;
			
			x += vec.nX()*mag*ds;
			y += vec.nY()*mag*ds;
			z += vz*ds;
			
			float zz = (float) (z + vz * ds);
			if (zz <= 0) {
				ds *= z / (vz - zz);
			}
			
			if (z <= 0)
				return null;
			int tx = ((int) x)>>C.T_SCROLL;
			int ty = ((int) y)>>C.T_SCROLL;
			
			if (!SETT.IN_BOUNDS(tx, ty)) {
				return null;
			}
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			int min = t.heightStart(tx, ty) * C.TILE_SIZE;
			int max = t.heightEnd(tx, ty) * C.TILE_SIZE;
			
			if (!SETT.TERRAIN().TREES.isTree(tx, ty) && z > min && z < max) {
				if (imax - i < 10)
					return null;
				return SProjectiles.¤¤TERRAIN;
			}
			int eh = t.heightEnt(tx, ty) * C.TILE_SIZE;
			if (z >= eh && z <= eh+Trajectory.HIT_HEIGHT) {
				if (BattleStatus.map().hasAlly.is(tx,  ty, ally)) {
					return SProjectiles.¤¤FRIENDLIES;
				}
				if (BattleStatus.map().hasEnemy.is(tx,  ty, ally))
					return null;
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR dir = DIR.ORTHO.get(di);
					if (BattleStatus.map().hasAlly.is(tx,  ty, dir, ally)) {
						return SProjectiles.¤¤FRIENDLIES;
					}
				}
			}
			
			

		}
		
		return null;
		
	}

	private boolean intesects(ENTITY e, double ox, double oy, double nx, double ny) {
		double x1 = ox;   
		double y1 = oy;
		double x2 = nx;   
		double y2 = ny;

		double w = e.body().width()*C.ITILE_SIZE;
		double x = e.body().x1()*C.ITILE_SIZE;
		double y = e.body().y1()*C.ITILE_SIZE;
		if (GEO.collides(x1, y1, x2, y2, x, y, x+w, y))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x+w, y, x+w, y+w))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x+w, y+w, x, y+w))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x, y+w, x, y))
			return true;
		return false;
	}

}
