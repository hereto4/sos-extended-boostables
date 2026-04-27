package snake2d;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayCooShort;

public final class CircleCooIterator {

	private final ArrayCooShort coos;
	private final byte[] radiuses;
	private final byte[] sides;
	
	public CircleCooIterator(int radius, PathUtilOnline.Flooder p) {
		
		if (radius > 127)
			throw new RuntimeException();
	
		{
			p.init(this);
			
			int amount = 0;
			p.pushSloppy(radius, radius, 0);
			
			while(p.hasMore()) {
				
				PathTile t = p.pollSmallest();
				if (t.getValue() > radius)
					break;
				
				amount++;
				
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					int x = t.x()+d.x();
					int y = t.y()+d.y();
					if (x < 0 || y < 0)
						continue;
					double v = Math.sqrt((x-radius)*(x-radius)+(y-radius)*(y-radius));
					p.pushSmaller(x, y, v);
				}
				
			}
			
			coos = new ArrayCooShort(amount);
			radiuses = new byte[amount];
			sides = new byte[amount];
			
			p.done();
		}
		
		{
			p.init(this);
			
			int index = 0;
			p.pushSloppy(radius, radius, 0);
			
			while(p.hasMore()) {
				
				PathTile t = p.pollSmallest();
				if (t.getValue() > radius)
					break;
				
				coos.set(index).set(t.x()-radius, t.y()-radius);
				radiuses[index] = (byte) t.getValue();
				sides[index] = (byte) (Math.abs(t.x()-radius) > Math.abs(t.y()-radius) ? Math.abs(t.x()-radius) : Math.abs(t.y()-radius));
				index++;
				
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					int x = t.x()+d.x();
					int y = t.y()+d.y();
					if (x < 0 || y < 0)
						continue;
					double v = Math.sqrt((x-radius)*(x-radius)+(y-radius)*(y-radius));
					p.pushSmaller(x, y, v);
				}
				
			}
			p.done();
		}
	}
	
	public COORDINATE get(int index) {
		return coos.set(index);
	}
	
	public int radius(int index) {
		return radiuses[index];
	}
	
	public int sideLength(int index) {
		return sides[index];
	}
	
	public int length() {
		return coos.size();
	}
	
	public static void main(String[] args) {
		CircleCooIterator c = new CircleCooIterator(50, new PathUtilOnline(200).getFlooder());
		
		int i = 0;
		while(c.sideLength(i) <= 5) {
			Printer.ln(c.get(i).x() + " " + c.get(i).y());
			i++;
		}
		Printer.ln(i);
		
	}
	
}
