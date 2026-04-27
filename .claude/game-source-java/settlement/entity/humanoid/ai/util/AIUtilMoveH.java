package settlement.entity.humanoid.ai.util;

import init.constant.C;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.ROOMA;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;

public class AIUtilMoveH {

	private final static Rec rec = new Rec();
	private final static Coo coo = new Coo();

	private AIUtilMoveH() {
		
	}
	
	public static void unfuck(Humanoid h) {

		ROOMA r = SETT.ROOMS().map.rooma.get(h.tc());
		if (r != null) {
			FurnisherItem it = SETT.ROOMS().fData.item.get(h.tc());
			if (it != null) {
				SETT.ROOMS().fData.itemX1Y1(h.tc(), coo);
				rec.moveX1Y1(coo.x(), coo.y());
				rec.setDim(it.width(), it.height());
				double best = Double.MAX_VALUE;
				int bi = -1;
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR d = DIR.ORTHO.get(di);
					if (rec.holdsPoint(h.tc(), d)) {
						AVAILABILITY a = SETT.PATH().availability.get(h.tc(), d);
						if (a.player > 0) {
							double v = a.player + a.from;
							if (v < best) {
								best = v;
								bi = di;
							}
						}

					}
				}
				if (bi != -1) {
					unfuck(h, DIR.ORTHO.get(bi));
				}
			}
		}
		
		double best = Double.MAX_VALUE;
		int bi = -1;
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			AVAILABILITY a = SETT.PATH().availability.get(h.tc(), d);
			if (a != null && a.player > 0) {
				double v = a.player + a.from;
				if (v < best) {
					best = v;
					bi = di;
				}
			}
		}
		if (bi != -1) {
			unfuck(h, DIR.ORTHO.get(bi));
		}
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (!SETT.PATH().solidity.is(h.tc(), d)) {
				unfuck(h, d);
				return;
			}
		}
	}

	public static void unfuck(Humanoid a, DIR dir) {
		int x = (a.tc().x() + dir.x()) * C.TILE_SIZE + C.TILE_SIZEH;
		int y = (a.tc().y() + dir.y()) * C.TILE_SIZE + C.TILE_SIZEH;
		int dw = (C.TILE_SIZE - a.physics.body().width()) / 2 - 1;
		x += -dir.x() * dw;
		y += -dir.y() * dw;
		a.physics.body().moveC(x, y);
	}
	
	public static void moveToTile(Humanoid a, int tx, int ty, DIR dir){

		int x = tx*C.TILE_SIZE + C.TILE_SIZEH;
		int y = ty*C.TILE_SIZE + C.TILE_SIZEH;
		
		x += dir.x()*(C.TILE_SIZEH-1);
		y += dir.y()*(C.TILE_SIZEH-1);
		
		a.physics.body().moveC(x, y);
		if (dir != DIR.C)
			a.speed.setDirCurrent(dir);
	}

}
