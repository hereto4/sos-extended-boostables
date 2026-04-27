package init.religion;

import java.io.IOException;

import init.INIT;
import init.INIT.InitResource;
import init.paths.PATHS;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.RMAP;

public final class RELIGIONS extends InitResource{

	private static RELIGIONS self;
	private final ArrayListGrower<Religion> all = new ArrayListGrower<>();
	private final RMAP<Religion> MAP;
	
	public RELIGIONS(INIT init) throws IOException{
		super(init);
		self = this;

		for (String k : PATHS.INIT().getFolder("religion").getFiles()) {
			Religion r = new Religion(k, all.size());
			all.add(r);
		}
		
		
		MAP = new RMAP<Religion>("RELIGION", all);
		for (Religion r : all) {
			r.init();
		}
	}
	
	public static LIST<Religion> ALL(){
		return self.all;
	}
	
	public static RMAP<Religion> MAP(){
		return self.MAP;
	}
	
}
