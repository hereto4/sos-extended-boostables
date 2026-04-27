package world.army;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import game.VERSION;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayListIntegerResize;
import snake2d.util.sets.ArrayListResize;
import world.entity.army.WArmy;

public final class WDivRegionalAll implements SAVABLE{

	static final int type = 0;
	private final ArrayListResize<WDivRegional> all = new ArrayListResize<>(1024*4, 1024*1024);
	private final ArrayListIntegerResize free = new ArrayListIntegerResize(1024*4, 1024*1024);
		
	WDivRegionalAll(){
		GAME.saver().onAfterLoad(new ACTION_O<Path>() {
			
			@Override
			public void exe(Path t) {
				if (VERSION.versionIsBefore(70, 23)) {
					for (WDivRegional rr : all) {
						if (rr.army() != null)
							rr.menSet(rr.men());
					}
				}
			}
		});
	}

	private int create() {
		if (free.isEmpty()) {
			WDivRegional div = new WDivRegional(all.size());
			all.add(div);
			return div.index;
		}
		int i = free.get(free.size()-1);
		free.remove(free.size()-1);
		return i;

	}
	
	public WDivRegional create(Race race, double amount, WArmy a) {
		int i = create();
		get(i).init(race, amount, a);
		return get(i);

	}
	
	WDivRegional get(int index) {
		return all.get(index);
	}
	
	void retire(WDivRegional div) {
		free.add(div.index);
	}

	@Override
	public void save(FilePutter file) {
		file.i(all.size());
		for (WDivRegional r : all) {
			r.save(file);
		}
		free.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int am = file.i();
		all.clear();
		free.clear();
		for (int i = 0; i < am; i++) {
			int k = create();
			get(k).load(file);
		}
		free.load(file);
	}

	@Override
	public void clear() {
		all.clear();
		free.clear();
	}
	
}
