package settlement.entity.animal;

import static settlement.main.SETT.ANIMALS;
import static settlement.main.SETT.ROOMS;

import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import view.keyboard.KEYS;
import view.main.VIEW;

enum State {

	STAND{

		@Override
		boolean update(Animal a, double ds) {
			if (!a.speed.isZero()) {
				a.speed.magnitudeAdjust(ds, 1.0, 1);
				return true;
			}
			return super.update(a, ds);
		}
		
		@Override
		void activate(Animal a, double duration) {
			super.activate(a, duration);
			a.speed.magnitudeTargetSet(0);
		}
		
		@Override
		Sprite sprite(Animal a) {
			return a.speed.isZero() ? Sprite.STAND_STILL : Sprite.MOVE;
		}
		
	},
	
	GRACE{

		@Override
		void activate(Animal a, double duration) {
			super.activate(a, duration);
			a.speed.magnitudeTargetSet(0);
		}
		
		@Override
		Sprite sprite(Animal a) {
			return Sprite.EATING;
		}
		
		@Override
		boolean update(Animal a, double ds) {
			if (!super.update(a, ds)) {
				grace(a);
				return false;
			}
			return true;
		}
		
		void grace(Animal a) {
			int tx = a.physics.tileC().x();
			int ty = a.physics.tileC().y();
			if(ROOMS().map.is(tx, ty) && ROOMS().map.get(tx, ty) instanceof ANIMAL_ROOM_RUINER) {
				ANIMAL_ROOM_RUINER i = (ANIMAL_ROOM_RUINER) ROOMS().map.get(tx, ty);
				if (i.canBeGraced(tx, ty))
					i.grace(tx, ty);
				else
					return;
			}else if (RND.oneIn(8)){
				SETT.TILE_MAP().growth.tear(tx, ty);
			}
			
			
		}
		
	},
	
	WALK_RANDOM{

		@Override
		void activate(Animal a, double duration) {
			a.stateTimer = (float) duration;
			a.speed.magnitudeTargetSet(0.3 + RND.rFloat(0.1));
			
			int d = C.TILE_SIZE;
			
			
			
			for (int i = 0; i < 4; i++) {
				int x = (int) (a.body().cX() + a.speed.nX()*d);
				int y = (int) (a.body().cY() + a.speed.nY()*d);
				int tx = x >> C.T_SCROLL;
				int ty = y >> C.T_SCROLL;
				if (!SETT.PATH().solidity.is(tx, ty) && !SETT.ENTITIES().hasAtTile(tx, ty)) {
					return;
				}
				a.speed.turn90();
				
			}
			
		}
		
		@Override
		Sprite sprite(Animal a) {
			return Sprite.MOVE;
		}
		
		@Override
		void meet(Animal a, ENTITY other) {
			if (other(a) != other)
				a.speed.magnitudeTargetSet(0);
		}
		
	},
	
	PANNIC{


		@Override
		void activate(Animal a, double duration) {
			a.stateTimer = (float) duration;
			a.speed.magnitudeTargetSet(0.8 + RND.rFloat(0.2));
			a.species().sound.rnd(a.body());
		}
		
		@Override
		void collide(Animal a, ENTITY other, double norX, double norY, double momentum) {
			if (momentum > a.species().momTresholdFly)
				super.collide(a, other, norX, norY, momentum);
			else
				a.speed.turn2(-norX, -norY);
		}

		@Override
		boolean collideTile(Animal a, boolean broken, double norX, double norY, double momentum) {
			if (momentum > a.species().momTreshold) {
				super.collideTile(a, broken, norX, norY, momentum);
			}
			collideUnwalkable(a);
			return false;
		}

		@Override
		void collideUnwalkable(Animal a) {
			int tx = a.tc().x();
			int ty = a.tc().y();
			DIR d = a.speed.dir();
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				if (!SETT.PATH().solidity.is(tx, ty, d)) {
					break;
				}
				a.speed.turn90();
				d = d.next(2);
			}
		}

		@Override
		void meet(Animal a, ENTITY other) {
			if (other instanceof Animal) {
				Animal o = (Animal) other;
				o.scare(a, true);
			}
			
		}
		
		@Override
		void scare(Animal a, ENTITY other, boolean flee) {
			if (other instanceof Animal)
				;
			else
				super.scare(a, other, flee);
		}
		
		@Override
		Sprite sprite(Animal a) {
			return Sprite.MOVE;
		}
		
	},
	
	UNCONSIOUS{

		@Override
		void collide(Animal a, ENTITY other, double norX, double norY, double momentum) {
	
		}

		@Override
		boolean collideTile(Animal a, boolean broken, double norX, double norY, double momentum) {
			return true;
		}

		@Override
		boolean wantsToCollide(Animal a, double mom) {
			return false;
		}

		@Override
		void collideUnwalkable(Animal a) {
			
		}

		@Override
		boolean update(Animal a, double ds) {
			a.physics.setHeightOverGround(CLAMP.d(a.physics.getZ()-ds*10, 0, 10));
			if (a.physics.getZ() > 0)
				return true;
			if (!a.speed.isZero()) {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeAdjust(ds, 1f, 1);
				return true;
			}
			return super.update(a, ds);
		}
		
		@Override
		void meet(Animal a, ENTITY other) {
			PANNIC.meet(a, other);
		}
		
		@Override
		void scare(Animal a, ENTITY other, boolean flee) {
			
		}
		
		@Override
		Sprite sprite(Animal a) {
			if (a.physics.getZ() > 0)
				return Sprite.LAYING_SPIN;
			else
				return Sprite.LAYING_STILL;
		}
		
	},
	
	CONTROLLED{

		@Override
		void collide(Animal a, ENTITY other, double norX, double norY, double momentum) {
	
		}

		@Override
		boolean collideTile(Animal a, boolean broken, double norX, double norY, double momentum) {
			return true;
		}

		@Override
		boolean wantsToCollide(Animal a, double mom) {
			return mom > a.species().momTreshold;
		}

		@Override
		void collideUnwalkable(Animal a) {
			
		}

		@Override
		void activate(Animal a, double duration) {
			a.speed.magnitudeMaxInit(8*C.TILE_SIZE);
			a.speed.magnitudeTargetSet(1.0);
			a.physics.setMass(500);
			super.activate(a, duration);
		}
		
		@Override
		boolean update(Animal a, double ds) {
			
			if (MButt.RIGHT.consumeClick()){
				return false;
			}
			
			final float y;
			if (KEYS.MAIN().MUP.isPressed()){
				y = -1;
			}else if (KEYS.MAIN().MDOWN.isPressed()){
				y = 1;
			}else{
				y = 0;
			}
			
			final float x;
			if (KEYS.MAIN().MRIGHT.isPressed()){
				x = 1;
			}else if (KEYS.MAIN().MLEFT.isPressed()){
				x = -1;
			}else{
				x = 0;
			}
			
			if (x != 0 || y != 0) {
				VIEW.s().getWindow().centerAt(a.body().cX(), a.body().cY());
				a.speed.turn2(x, y);
				a.speed.magnitudeTargetSet(1.0);
			}else {
				a.speed.magnitudeTargetSet(0);
			}
			a.speed.magnitudeAdjust(ds, 1.0, 1f);
			return true;
		}
		
		@Override
		void meet(Animal a, ENTITY other) {
			
		}
		
		@Override
		void scare(Animal a, ENTITY other, boolean flee) {
			
		}
		
		@Override
		Sprite sprite(Animal a) {
			return Sprite.MOVE;
		}
		
		@Override
		boolean willCollideWith(Animal a, ENTITY other){
			return true;
		}
		
	},
	
	
	;
	
	
	State(){
		
	}
	
	boolean update(Animal a, double ds) {
		a.speed.magnitudeAdjust(ds, 1.0, 1);
		a.stateTimer -= ds;
		return a.stateTimer > 0;
	}
	
	void activate(Animal a, double duration) {
		a.stateTimer = (float) duration;
	}
	
	Sprite sprite(Animal a) {
		return Sprite.STAND_STILL;
	}
	
	void scare(Animal a, ENTITY other, boolean flee) {
		if (a.domesticated())
			return;
		if (other == null)
			return;
		otherSet(a, other);
		
		boolean shouldFlee = flee || other instanceof Animal || a.cub;
		
		if (!shouldFlee)
			shouldFlee = !ANIMALS().spawn.isTimeForAKill(a.species());
		
		if (shouldFlee) {
			a.speed.turn2(other.body().cX()+RND.rInt0(C.TILE_SIZEH), other.body().cY()+RND.rInt0(C.TILE_SIZEH), a.body().cX(), a.body().cY());
		}else
			a.speed.turn2(a.body(), other.body());
		a.setState(PANNIC, 5);
	}
	
	void collide(Animal a, ENTITY other, double norX, double norY, double momentum) {
		if (momentum < a.species().momTreshold) {
			if (!a.domesticated()){
				scare(a, other, false);
			}
			a.setState(STAND, 1);
		}else if(momentum < a.species().momTreshold*1.6) {
			a.setState(UNCONSIOUS, 4);
		}else {
			a.physics.setHeightOverGround(a.physics.getZ()+(momentum-a.species().momTreshold)*4);
			a.setState(UNCONSIOUS, 8);
		}
		
		

	}

	boolean collideTile(Animal a, boolean broken, double norX, double norY, double momentum) {
		
		if (momentum < a.species().momTreshold) {
			a.setState(STAND, 1);
			return false;
		}else if(momentum < a.species().momTreshold*1.5) {
			a.setState(UNCONSIOUS, 4);
		}else {
			a.physics.setHeightOverGround(a.physics.getZ()+(momentum-a.species().momTreshold)*4);
			a.setState(UNCONSIOUS, 8);
		}
					
		return true;

	}

	boolean wantsToCollide(Animal a, double mom) {
		return mom > a.species().momTreshold;
	}

	void collideUnwalkable(Animal a) {
		
		int tx = a.tc().x();
		int ty = a.tc().y();
		DIR d = a.speed.dir();
		for (int i = 0; i < DIR.ALL.size(); i++) {
			if (!SETT.PATH().solidity.is(tx, ty, d)) {
				a.speed.setRaw(d, 1.0);
				break;
			}
			d = d.next(1);
		}
		
		
	}

	void meet(Animal a, ENTITY other) {
		otherSet(a, other);
		a.setState(WALK_RANDOM, 1f);
	}
	
	private static ENTITY other(Animal a) {
		ENTITY e = SETT.ENTITIES().getByID(a.stateI);
		if (e == null)
			a.stateI = -1;
		return e;
	}
	
	private static void otherSet(Animal a, ENTITY other) {
		if (other != null)
			a.stateI = other.id();
		else
			a.stateI = -1;
	}
	
	boolean willCollideWith(Animal a, ENTITY other) {
		return other instanceof Humanoid && !a.domesticated();
	}

	static final State[] all = values();
	
}
