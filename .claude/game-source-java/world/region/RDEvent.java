package world.region;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.data.INT_O.INT_OE;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD.RDInit;

public class RDEvent {

	
	
	private int am;
	public final INT_OE<Region> ii;
	public final INT_OE<Region> mark;
	
	RDEvent(RDInit init){
		
		String key = "EVENT_MARK";
		ii = init.count.new DataBit(key) {
			@Override
			public void set(Region t, int s) {
				am -= get(t);
				super.set(t, s);
				am += get(t);
			}
			
			@Override
			public int get(Region t) {
				if (t == null)
					return am;
				return super.get(t);
			}
		};
		
		mark = init.count.new DataShort("EVENT");
		
		init.savable.add(new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				am = 0;
				for (Region reg : WORLD.REGIONS().all())
					am += ii.get(reg);
				
			}
			
			@Override
			public void clear() {
				am = 0;
				
			}
		});
		
		

	}
	
	public int total() {
		return am;
	}
	
}
