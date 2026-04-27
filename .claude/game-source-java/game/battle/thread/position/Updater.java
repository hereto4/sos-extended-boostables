package game.battle.thread.position;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormation;
import game.battle.formation.DivPositionCopyable;
import game.battle.thread.position.DivCentres.Context;
import init.constant.C;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;

final class Updater {

	volatile boolean stop = false;
	private final Rec body = new Rec();
	
	Updater(){

	}
	
	void init(Context c) {
		
		for (short di = 0; di < c.statuses.length; di++) {
			if (stop)
				return;
			Div d = GAME.ARMIES().division(di);
			init(c, d);
		}
		
	}
	
	void init(Context c, Div d) {
		
		DivCentre s = c.statuses[d.index()];
		s.clear();
		
		
		if (d.menNrOf() == 0)
			return;
		DivPositionCopyable pos = d.current();
		if (pos.deployed() == 0)
			return;
		DivFormation form = d.position();
		double am = 0;
		for (int i = 0; i < form.deployed() && i < pos.deployed(); i++) {
			if (!d.reporter.reachable(i))
				continue;
			double dist = COORDINATE.tileDistance(form.px(i), form.py(i), pos.px(i), pos.py(i));
			if (dist < C.TILE_SIZE) {
				double a = 1.0 - (dist / C.TILE_SIZE);
				a = CLAMP.d(a, 0, 1);
				am += a;
			}
		}
	
		s.inPosition = (short) (am);

		int xx = 0;
		int yy = 0;
		am = 0;
		
		for (int pi = 0; pi < pos.deployed(); pi++) {
			if (d.reporter.reachable(pi)) {
				int x = pos.px(pi);
				int y = pos.py(pi);
				if (pi == 0) {
					body.clear();
					body.moveX1Y1(x, y);
					body.setDim(1, 1);
				}else {
					body.unify(x, y);
				}
				xx += x;
				yy += y;
				am++;
			}
		}
		
		if (am == 0) {
			for (int pi = 0; pi < pos.deployed(); pi++) {
				int x = pos.px(pi);
				int y = pos.py(pi);
				if (pi == 0) {
					body.clear();
					body.moveX1Y1(x, y);
					body.setDim(1, 1);
				}else {
					body.unify(x, y);
				}
				xx += x;
				yy += y;
				am++;
			}
		}
		
		s.squareCX = body.cX();
		s.squareCY = body.cY();
		
		if (am == 0) {
			s.cx = -1;
			s.cy = -1;
		}else {
			xx /= am;
			yy /= am;
			
			s.cxSoft = xx;
			s.cySoft = yy;
			int best = -1;
			int bestV = Integer.MAX_VALUE;
			
			for (int pi = 0; pi < pos.deployed(); pi++) {
				int dist = Math.abs(xx-pos.px(pi)) + Math.abs(yy-pos.py(pi));
				
				if (!d.reporter.reachable(pi)) {
					dist += Integer.MAX_VALUE/2;
				}
				
				if (dist < bestV) {
					best = pi;
					bestV = dist;
				}
			}
			
			xx = pos.px(best);
			yy = pos.py(best);
		}
		
		s.cx = xx;
		s.cy = yy;
	}



	
	
}
