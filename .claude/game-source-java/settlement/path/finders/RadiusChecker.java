package settlement.path.finders;

import java.util.Arrays;

import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentEdge;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import util.GUTIL;

public class RadiusChecker {

	
	public static RadiusChecker self = new RadiusChecker();
	
	private byte ri = 0;
	private int[] levelStarts;
	private int[] sizes;
	private byte[] ids = new byte[0];
	
	public void check(int sx, int sy, int radius) {
		
		if (needsSizeFix()) {
			levelStarts = new int[SETT.PATH().comps.all.size()];
			sizes = new int[SETT.PATH().comps.all.size()];
			int start = 0;
			for (int l = 0; l < levelStarts.length; l++) {
				int size = SETT.PATH().comps.all.get(l).componentsMax();
				sizes[l] = size;
				
				
				levelStarts[l] = start;
				start += size;
			}
			
			ids = new byte[start+64];
			ri = 0;
		}else if(ri == -1) {
			Arrays.fill(ids, (byte)0);
			ri = 0;
		}
		
		ri++;
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(sx, sy, 0);
		
		while(GUTIL.flooder().hasMore()){
			PathTile t = GUTIL.flooder().pollSmallest();
			check(t);
			if (t.getValue() > radius)
				break;
			
			SComponent c = SETT.PATH().comps.zero.get(t);
			SComponentEdge e = c.edgefirst();
			while(e != null) {
				GUTIL.flooder().pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+e.distance());
				e = e.next();
			}
		}
		
		GUTIL.flooder().done();
		
		
	}
	
	private void check(COORDINATE c) {
		SComponent start = SETT.PATH().comps.zero.get(c);
		int s = 0;
		while(start != null) {
			ids[start.index()+levelStarts[s]] = ri;
			start = start.superComp();
			s++;
		}
	}
	
	public boolean is(SComponent c) {
		return ids[c.index()+c.level().level()] == ri;
	}
	
	public boolean is(int tx, int ty) {
		SComponent c = SETT.PATH().comps.zero.get(tx, ty);
		if (c == null)
			return false;
		return ids[c.index()+c.level().level()] == ri;
	}
	
	
	private boolean needsSizeFix() {
		if (levelStarts == null)
			return true;
		
		int start = 0;
		
		for (int l = 0; l < sizes.length; l++) {
			start += SETT.PATH().comps.all.get(l).componentsMax();
		}
		
		return start >= ids.length;
	}
	
	
}
