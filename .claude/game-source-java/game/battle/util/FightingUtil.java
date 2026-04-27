package game.battle.util;

import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import init.constant.C;
import init.constant.Config;
import init.type.CAUSE_LEAVES;
import settlement.entity.ECollision;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.thing.projectiles.Projectile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.rnd.RND;

public final class FightingUtil {

	private final VectorImp vec = new VectorImp();
	private ECollision coll = new ECollision();
	
	private final double CHANCE_MIN = 1.0/Config.battle().DAMAGE_REDUCTION;
	private final double CHANCE_SPAN = Config.battle().DAMAGE_REDUCTION-CHANCE_MIN;
	
	
	public void attack(Humanoid a, ENTITY enemy) {
		
		
		DIR od = enemy.speed.dir();
		vec.set(a.body(), enemy.body());
		coll.other = a;
		
		double dot = (1 -od.xN()*vec.nX() -od.yN()*vec.nY())*0.5;
		
		coll.norX = vec.nX();
		coll.norY = vec.nY();
		coll.dirDot = dot;
		coll.dirDotOther = dot;
		coll.speedHasChanged = false;
		coll.tileMomentum = 0;
		

		
		if (dodge(BOOSTABLES.BATTLE().OFFENCE.get(a.indu()), enemy.getDefenceSkill(dot, coll.norX, coll.norY))) {
			
			coll.damagetileStrength = 0;
			coll.tileMomentum = 0;
			
			enemy.collide(coll);
			return;
		}
		
		setDamage(coll, a);
		
		if (enemy instanceof Humanoid) {
			Humanoid e = (Humanoid) enemy;
			if (!doesNotBlock(BOOSTABLES.BATTLE().DEXTERITY.get(a.indu()), e, dot, coll.norX, coll.norY)) {
				blockDamage(coll, e);
			}
		}
		
		
		
		AI.modules().battle.soundSword.rnd(a);
		
		double nY = enemy.speed.y() + vec.nY()*coll.tileMomentum*enemy.physics.getMassI();
		double nX = enemy.speed.x() + vec.nX()*coll.tileMomentum*enemy.physics.getMassI();
		coll.speedHasChanged = true;
		enemy.speed.setRaw(nX, nY);

		
		enemy.collide(coll);
		if (enemy.isRemoved()) {
			STATS.BATTLE().makeAKill(a);
		}
	
	}

	private boolean dodge(double attackSpeed, double defenceAgility) {
		
		if (defenceAgility <= 0)
			return false;
		
		if (attackSpeed <= 0)
			return true;
		
		double r = attackSpeed/(defenceAgility*(CHANCE_MIN+RND.rFloat()*CHANCE_SPAN));	
		if (r > RND.rFloat())
			return false;
		return true;
		
	}
	
	private boolean doesNotBlock(double attackSkill, Humanoid enemy, double dot, double adx, double ady) {
		
		if (attackSkill <= 0)
			return false;
		
		double def =  HPoll.Handler.parrySkill(enemy, dot, adx, ady);

		if (def <= 0)
			return true;
		
		if (enemy.division() == null)
			def *= 0.25;
			
		
		double r = attackSkill/(def*(CHANCE_MIN+RND.rFloat()*CHANCE_SPAN));	
		if (r > RND.rFloat())
			return true;
		return false;
		
	}
	

	private double formationValue(Humanoid a, double ax, double ay) {
		Div div = a.division();
		if (div != null) {
			DIR dd = div.position().dir();
			double dot = 0.5*(1 + (dd.xN()*-ax + dd.yN()*-ay));
			dot = (int)(dot*4)*0.25;
			
			if (dot > 0) {
				return dot*BOOSTABLES.BATTLE().FORMATION.get(a.division());
			}
		}
		return 0;
	}
	
	private void setDamage(ECollision e, Humanoid a) {
		
		double h = 1.0 + SETT.TERRAIN().get(a.tc()).heightEnt(a.tc().x(), a.tc().y())/10.0;
		e.damagetileStrength = BOOSTABLES.BATTLE().BLUNT_ATTACK.get(a.indu())*h;

		for (int i = 0; i < e.damage.length; i++) {
			double da = BOOSTABLES.BATTLE().DAMAGES.get(i).attack.get(a.indu());
			e.damage[i] = da;
		}
		coll.tileMomentum = C.TILE_SIZE*h*BOOSTABLES.BATTLE().BLUNT_ATTACK.get(a.indu())*(0.5+RND.rFloat()*2);
	}
	
	private void blockDamage(ECollision e, Humanoid blocker) {
		e.damagetileStrength /= BOOSTABLES.BATTLE().BLUNT_DEFENCE_DIR.get(blocker.indu());
		for (int i = 0; i < e.damage.length; i++) {
			e.damage[i] /= 1 + BOOSTABLES.BATTLE().DAMAGES.get(i).defenceDir.get(blocker.indu());
		}
	}
	
	public double valueDefenceSkill(Humanoid a,  double attackDot, double ax, double ay) {
		double def = BOOSTABLES.BATTLE().DEFENCE.get(a.indu());
		double res = (0.1 + 0.9*attackDot)*def;
		res += formationValue(a, ax, ay);
		return res;
	}
	
	public double valueParrySkill(Humanoid a,  double attackDot, double ax, double ay) {
		double def = BOOSTABLES.BATTLE().PARRY.get(a.indu());
		def = attackDot*def;
		def += formationValue(a, ax, ay);
		return def;
	}
	
	
	public double getDamageDone(ECollision coll, Humanoid a) {
		double dam = 1;
		dam/= 1.0 + SETT.TERRAIN().get(a.tc()).heightEnt(a.tc().x(), a.tc().y())/10.0;
		
		for (int i = 0; i < BOOSTABLES.BATTLE().DAMAGES.size(); i++) {
			dam += coll.damage[i]/(1+BOOSTABLES.BATTLE().DAMAGES.get(i).defence.get(a.indu()));
		}
		
		dam *= coll.damagetileStrength;
		dam /= (CHANCE_MIN+RND.rFloat()*CHANCE_SPAN)*BOOSTABLES.BATTLE().BLUNT_DEFENCE.get(a.indu());
		
		double ch = RND.rFloat();
		if (a.division() == null)
			ch *= 0.25;
		if (dam > ch) {
			
			return dam;
		}
		
		
		return 0;
	}

	double bb = 0;
	double am = 0;
	
	public boolean projectileAttack(ENTITY e, double angleX, double angleY, double speed, Projectile type, double ref) {
		int sp = (int) (speed);
		DIR od = e.speed.dir();
		double dot = (1 - od.xN()*angleX-od.yN()*angleY)*0.5;
		
		if (dodge(sp, e.getDefenceSkill(dot, angleX, angleY))) {
			return false;
		}
		
		coll.norX = angleX;
		coll.norY = angleY;
		coll.dirDot = dot;
		coll.dirDotOther = dot;
		coll.leave = CAUSE_LEAVES.SLAYED();
		coll.other = null;
		coll.damagetileStrength = type.mass(ref)*sp*C.ITILE_SIZE;
		for (int i = 0; i < coll.damage.length; i++) {
			coll.damage[i] = type.damage(i, ref);;
		}
		
		double mom = type.mass(ref)*speed;
		mom *= 1 + RND.rFloat(8);
		coll.tileMomentum = mom;
		mom *= e.physics.getMassI();
		double nX = angleX*mom;
		double nY = angleY*mom;
		e.speed.setRaw(e.speed.x()+nX, e.speed.y()+nY);

		am++;
		if(e instanceof Humanoid) {
			Humanoid eh = (Humanoid) e;
			if (doesNotBlock(type.skill(ref), eh, dot, angleX, angleY))
				;
			else {
				bb++;
				blockDamage(coll, eh);
			}
		}
//		
//		if (am > 100) {
//			System.out.println(bb/am);
//			am = 0;
//			bb = 0;
//		}
		
		e.collide(coll);
		
		return true;
	}


	public void setImpactDamage(Humanoid a, ECollision coll, ECollision damage) {
		
		
		double speed = a.speed.magnitude();
		
		
		damage.damagetileStrength = 0;
		
		if (dodge(BOOSTABLES.BATTLE().DEXTERITY.get(a.indu())+speed, coll.other.getDefenceSkill(coll.dirDotOther, coll.norX, coll.norY))) {
			return;
		}
		
		setDamage(damage, a);
		
		speed = speed*C.ITILE_SIZE-2;
		if (speed < 0)
			speed = 0;
		
		STATS.NEEDS().EXHASTION.indu().incD(a.indu(), -speed*0.25);
		
		double bonus = speed*coll.dirDot;
		bonus *= BOOSTABLES.BATTLE().CHARGE.get(a.indu());
		
		damage.damagetileStrength *= bonus;
		
		if (coll.other instanceof Humanoid) {
			Humanoid e = (Humanoid) coll.other;
			if (doesNotBlock(BOOSTABLES.BATTLE().OFFENCE.get(a.indu()), e, coll.dirDotOther, coll.norX, coll.norY)) {
				blockDamage(damage, e);
			}
		}
		
	
	}
	
	

	
}

