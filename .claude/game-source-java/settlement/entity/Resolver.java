package settlement.entity;

final class Resolver {

	Resolver() {

	}

	private final ECollision ca = new ECollision();
	private final ECollision cb = new ECollision();
	
	
	void resolveCollision(ENTITY a, ENTITY b) {		

		if (!a.collidesWithOthers(b) || !b.collidesWithOthers(a)) {
			a.meet(b);
			b.meet(a);
			return;
		}
		
		if (!a.willCollideWith(b) && !b.willCollideWith(a)) {
			a.meet(b);
			b.meet(a);
			return;
		}
		
		double ya1 = a.physics.getZ();
		double ya2 = ya1 + a.physics.getHeight();
		double yb1 = b.physics.getZ();
		double yb2 = yb1 + b.physics.getHeight();

		if (ya1 > yb2 || ya2 < yb1) {
			return;
		}

		final double distX = b.body().cX() - a.body().cX();
		final double distY = b.body().cY() - a.body().cY();
		
		
		final double norLength = Math.sqrt((distX * distX + distY * distY));

		if (norLength == 0) {
			// do something making sense
			// a.interactor().collide(b, 0, 0, 0);
			// b.interactor().collide(a, 0, 0, 0);
			return;
		}

		final double norX = distX / norLength;
		final double norY = distY / norLength;

		final double mA = a.physics.getMass();
		final double mB = b.physics.getMass();

		if (mA <= 0 || mB <= 0)
			return;

		ESpeed.Imp va = a.speed;
		ESpeed.Imp vb = b.speed;
		final double vAX = va.x();
		final double vAY = va.y();
		final double vBX = vb.x();
		final double vBY = vb.y();

		double dVX = vBX - vAX;
		double dVY = vBY - vAY;
		if (dVX * norX > 0 && dVY * norY > 0) {
			// a.interactor().collide(b, norX, norY, 0);
			// b.interactor().collide(a, -norX, -norY, 0);
			return;
		}

		double momX = dVX * norX < 0 ? dVX : 0;
		double momY = dVY * norY < 0 ? dVY : 0;

		double mom = Math.sqrt(momX * momX + momY * momY);

		double speedDotA = (1 +a.speed.dir().xN()*norX +a.speed.dir().yN()*norY)*0.5;
		double speedDotB = (1 -b.speed.dir().xN()*norX -b.speed.dir().yN()*norY)*0.5;
		ca.other = b;
		ca.dirDot = speedDotA;
		ca.dirDotOther = speedDotB;
		ca.norX = norX;
		ca.norY = norY;
		ca.tileMomentum = 0;
		ca.damagetileStrength = 0;
		ca.speedHasChanged = false;
		ca.leave = null;
		
		
		
		cb.other = a;
		cb.dirDot = speedDotB;
		cb.dirDotOther = speedDotA;
		cb.norX = -norX;
		cb.norY = -norY;
		cb.tileMomentum = 0;
		cb.damagetileStrength = 0;
		cb.speedHasChanged = false;
		cb.leave = null;
		
		
		a.setCollideDamage(ca, cb);
		b.setCollideDamage(cb, ca);

		if (mom > 0) {
			
			mom *= 1 + (a.physics.getRestitution() + b.physics.getRestitution()) * 0.5;
			
			if (mA <= 0) {
				solidCollision(a, b, -norX, -norY, mA);
				ca.speedHasChanged = true;
				cb.speedHasChanged = true;
				ca.tileMomentum += mom;
				a.collide(ca);
				cb.tileMomentum += mom;
				b.collide(cb);

				return;
			} else if (mB <= 0) {
				solidCollision(b, a, norX, norY, mB);
				mom *= mA;
				ca.speedHasChanged = true;
				cb.speedHasChanged = true;
				ca.tileMomentum += mom;
				a.collide(ca);
				cb.tileMomentum = +mom;
				b.collide(cb);
				return;
			}
			double magScale = 1.0;
			if (mA > mB) {
				dVX /= mom;
				dVY /= mom;
				mom *= mB;
				magScale -= 0.3 * mB / mA;
			} else {
				dVX /= mom;
				dVY /= mom;
				mom *= mA;
				magScale -= 0.3 * mA / mB;
			}

			collidePair(a, b, -norX, -norY, mom, dVX, dVY, magScale);
			ca.tileMomentum += mom;
			cb.tileMomentum += mom;
			ca.speedHasChanged = true;
			cb.speedHasChanged = true;
			a.collide(ca);
			b.collide(cb);

		} else {
			a.collide(ca);
			b.collide(cb);

		}

	}

	private static void collidePair(ENTITY a, ENTITY b, double norX, double norY, double mom, double dx, double dy,
			double magScale) {

		if (mom == 0)
			mom = 1;

		ESpeed.Imp va = a.speed;
		ESpeed.Imp vb = b.speed;

		double x = mom, y = mom;

//		norX = norX * 0.5 + 0.5 * norX * dx;
//		norY = norY * 0.5 + 0.5 * norY * dy;

		x *= norX;
		y *= norY;

		va.setRaw(va.x() + x * a.physics.massI, va.y() + y * a.physics.massI);
		vb.setRaw(vb.x() - x * b.physics.massI, vb.y() - y * b.physics.massI);
		
		va.magnitudeInit(va.magnitude() * magScale);
		vb.magnitudeInit(vb.magnitude() * magScale);

	}

	private static double solidCollision(ENTITY p, ENTITY solid, final double norX, final double norY, final double m) {

		final double vAX = p.speed.nX();
		final double vAY = p.speed.nY();

		// no real collision
		if (vAX * norX + vAY * norY >= 0)
			return 0;

		final double r = (p.physics.getRestitution() + solid.physics.getRestitution()) / 2;
		double resX = 5 * norX;
		double resY = 5 * norY;

		if (vAX * norX > 0) {
			resX -= vAX * r;
		} else {
			resX += vAX;
		}
		if (vAY * norY > 0) {
			resY -= vAY * r;
		} else {
			resY += vAY;
		}

		p.speed.setRaw(resX, resY);
		double momX = (vAX - resX);
		double momY = (vAY - resY);
		return p.physics.getMass() * Math.sqrt(momX * momX + momY * momY);

	}

}
