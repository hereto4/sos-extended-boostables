package settlement.path.finders;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.updating.IUpdater;

public class SFindersUpdater {

	private final IUpdater upper;
	public final static int quad = 16;
	final static int W = SETT.TWIDTH/quad;
	final static int H = SETT.THEIGHT/quad;;
	
	
	public SFindersUpdater() {
		int am = SFinderFindableMap.W*SFinderFindableMap.H;
		
		upper = new IUpdater(am, TIME.days().bitSeconds()*2) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				boolean d = (TIME.days().bitsSinceStart() & 1) == 1;
				for (SFinderFindable a : SFinderFindable.all) {
					a.map.update(i, d);
				}
			}
		};
	}
	
	public void update(double ds) {
		upper.update(ds);
	}
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(SFinderFindable.all.size());
			for (SFinderFindable a :SFinderFindable.all) {
				a.map.save(file);
			}
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int am = file.i();
			if (am != SFinderFindable.all.size()) {
				for (int i = 0; i < am; i++) {
					SFinderFindable.all.get(0).map.load(file);
				}
				clear();
				
			}else {
				for (SFinderFindable a :SFinderFindable.all) {
					a.map.load(file);
				}
			}
		}
		
		@Override
		public void clear() {
			for (int i = 0; i < SFinderFindable.all.size(); i++) {
				SFinderFindable.all.get(i).map.clear();
			}
		}
	};
	
}
