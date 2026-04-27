package settlement.thing.projectiles;

import settlement.main.SETT;
import settlement.thing.projectiles.PData.Data;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.ArrayListInt;

final class Quad {
	
	private int first = -1;
	@SuppressWarnings("unused")
	private final int qx,qy;
	

	
	
	public Quad(int qx, int qy) {
		this.qx = qx;
		this.qy = qy;
	}
	
	void add(int index){
		SETT.PROJS().data.nextSet(index, first);
		first = index;
	}
	
	void remove(final int e){
		

		int first = this.first;
		this.first = -1;
		

		while(first != -1) {
			int ee = first;
			first = SETT.PROJS().data.next(first);
			if (ee != e) {
				SETT.PROJS().data.nextSet(ee, this.first);
				this.first = ee;
			}
		}
		
		SETT.PROJS().data.nextSet(e, -1);
	}
	
	boolean contains(int e) {
		int di = first;
		while(di != -1) {
			if (di == e)
				return true;
			di = SETT.PROJS().data.next(di);
		}
		return false;
	}
	
	void clear(){
		first = -1;
	}
	
	void fill(RECTANGLE bounds, ArrayListInt result) {
		
		int di = first;
		while(di != -1 && result.hasRoom()) {
			Data data = SETT.PROJS().data.data(di);
			if (bounds.holdsPoint(data.x(), data.y())) {
				result.add(di);
			}
			di = SETT.PROJS().data.next(di);
		}
		
		
			
	}
	
}
