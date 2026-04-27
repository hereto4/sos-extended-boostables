package settlement.thing.pointlight;

import static settlement.main.SETT.IN_BOUNDS;

import init.constant.C;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap1D;

class PointRayTracer {

	private final Bitmap1D lit;
	private final Bitsmap1D corners;
	private final int tileDiameter;
	
	public PointRayTracer(int tileDiameter) {
		this.tileDiameter = tileDiameter;
		lit = new Bitmap1D(tileDiameter*tileDiameter, false);
		corners = new Bitsmap1D(0, 4, tileDiameter*tileDiameter);
	}
	
	boolean litIs(int tx, int ty) {
		return (corners.get(tx + ty * tileDiameter)) != 0;
	}
	
	private boolean lit(int tx, int ty) {
		return lit.get(tx + ty * tileDiameter);
	}
	
	private boolean lit(int tx, int ty, DIR d) {
		return lit(tx+d.x(), ty+d.y());
	}


	protected void init(int x, int y) {

		int ctx = x >> C.T_SCROLL;
		int cty = y >> C.T_SCROLL;

		if (!shouldBeLit(ctx, cty)) {
			return;
		}
		
		lit.clear();
		lit.set(tileDiameter/2 + tileDiameter*tileDiameter/2, true);
		int tx1 = ctx - tileDiameter/2;
		int ty1 = cty - tileDiameter/2; 
		
		
		for (int gy = 0; gy <= tileDiameter; gy++) {
			rayTrace(-tx1, -ty1, ctx, cty, tx1, ty1+gy);
			rayTrace(-tx1, -ty1, ctx, cty, tx1+tileDiameter-1, ty1+gy);
		}
		
		for (int gx = 1; gx <= tileDiameter-1; gx++) {
			rayTrace(-tx1, -ty1, ctx, cty, tx1+gx, ty1);
			rayTrace(-tx1, -ty1, ctx, cty, tx1+gx, ty1+tileDiameter-1);
		}
		
		corners.clear();
		
		for (int ty = 1; ty <= tileDiameter-1; ty++) {
			for (int tx = 1; tx <= tileDiameter-1; tx++) {
				int t = tx + ty * tileDiameter;
				if (lit.get(t)) {
					corners.set(t, 0x0F);
				}else {
					for (DIR d : DIR.NORTHO) {
						if (!cornercheck(tx, ty, d, tx1, ty1, d))
							if (!cornercheck(tx, ty, d.next(1), tx1, ty1, d))
								cornercheck(tx, ty, d.next(-1), tx1, ty1, d);
					}
				}
				
			}
		}
		
	}
	
	private boolean cornercheck(int tx, int ty, DIR d, int tx1, int ty1, DIR dir) {
		if (lit(tx, ty, d)) {
			int fx = tx1+tx;
			int fy = ty1+ty;
			int tox = tx1+tx+d.x();
			int toy = ty1+ty+d.y();
			
			LOS from = SETT.LIGHTS().los().get(fx, fy);
			LOS to = SETT.LIGHTS().los().get(tox, toy);
			if (from.passesToOtherFromThis(fx, fy, tox, toy) && to.passesFromOtherToThis(fx, fy, tox, toy)) {
				int t = tx + ty * tileDiameter;
				int c = corners.get(t);
				c |= dir.mask();
				corners.set(t, c);
				return true;
			}
		}
		return false;
	}
	
	private void rayTrace(int mx, int my, int fromx, int fromy, int tox, int toy) {

		double divider;		
		if (Math.abs(tox-fromx) > Math.abs(toy-fromy)) {
			divider = Math.abs(tox-fromx);
		}else if(Math.abs(tox-fromx) < Math.abs(toy-fromy)) {
			divider = Math.abs(toy-fromy);
		}else {
			divider = Math.abs(tox-fromx);
		}
		
		double dx = (tox-fromx)/divider;
		double dy = (toy-fromy)/divider;
		
		double x = fromx + 0.5;
		double y = fromy + 0.5;
		
		for (int i = 0; i < divider; i++) {
			
			int otx = (int) x;
			int oty = (int) y;
			lit.set(otx+mx + (oty+my)*tileDiameter, true);
			LOS lFrom = SETT.LIGHTS().los().get(otx, oty);
			x += dx;
			y += dy;
			int tx = (int) x;
			int ty = (int) y;
			
			LOS lTo = SETT.LIGHTS().los().get(tx, ty);
			
			if (!lFrom.passesToOtherFromThis(otx, oty, tx, ty) || !lTo.passesFromOtherToThis(otx, oty, tx, ty)) {
				return;
			}
		}
		
	}
	
	byte getSide(int tx, int ty, DIR d) {
		return (corners.get(tx+ty*tileDiameter) & d.mask()) != 0 ? Byte.MAX_VALUE : 0;
		
	}
	
	private boolean shouldBeLit(int x, int y) {
		return IN_BOUNDS(x, y);
	}
	
}
