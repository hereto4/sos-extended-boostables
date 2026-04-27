package game.events.general;

import java.io.IOException;

import game.GAME;
import game.event.engine.Event;
import game.event.engine.EventCollection;
import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.paths.PATHS;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Tree;
import view.interrupter.IDebugPanel;

public final class EventGeneral extends EventResource{

	private final EventCollection coll;
	
	private final double eventsPerSecondLow;
	private final double eventsPerSecondHigh;
	private final Tree<Event> spawnSort;
	
	private double timer = 0;

	public EventGeneral() throws IOException {
		super("ENGINE");

		double d = new Json(PATHS.EVENT().init.get("_CONFIG")).d("DAYS_BETWEEN_EVENTS_POP_0", 0, 1000);
		eventsPerSecondLow = 1.0/(d*TIME.secondsPerDay());
		
		d = new Json(PATHS.EVENT().init.get("_CONFIG")).d("DAYS_BETWEEN_EVENTS_POP_5000", 0, 1000);
		eventsPerSecondHigh = 1.0/(d*TIME.secondsPerDay());
		
		coll = new EventCollection(PATHS.EVENT());
		
		spawnSort = new Tree<Event>(coll.all.size()) {

			@Override
			protected boolean isGreaterThan(Event current, Event cmp) {
				return GAME.EVENT().acc(current) > GAME.EVENT().acc(cmp);
			}
			
		};
		
		IDebugPanel.add("Spawn next event", new ACTION() {
			
			@Override
			public void exe() {
				spawn();
			}
		});
		
		IDebugPanel.add("tick all events", new ACTION() {
			
			@Override
			public void exe() {
				for (int i = 0; i < coll.all.size(); i++) {
					
					Event e = coll.all.get(i);
					GAME.EVENT().accInc(e);
				}
			}
		});
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
	}

	@Override
	protected void clear() {
		timer = 0;
	}
	
	@Override
	protected void update(double ds) {
		
		if (GAME.EVENT().current() != null)
			return;
		
		if (timer >= coll.all.size()) {
			if (!spawn())
				timer -= 5;
			else
				timer -= (int) timer;
		}
		
		int no = (int) timer;
		
		double d = STATS.POP().POP.data().get(null)/10000.0;
		d = CLAMP.d(d, 0, 1);
		d *= (eventsPerSecondHigh-eventsPerSecondLow);
		
		
		d = eventsPerSecondLow + d;
		
		timer += ds*d*coll.all.size();
		int nn = (int) timer;
		
		for (; no < nn && no < coll.all.size(); no++) {
			
			Event e = coll.all.get(no);
			GAME.EVENT().accInc(e);
		}
		
		
	}
	
	
	
	boolean spawn() {
		
		spawnSort.clear();
		for (Event e : coll.all) {
			if (GAME.EVENT().acc(e) > 0) {
				spawnSort.add(e);
			}
		}
		
		int m = 0;
		
		while(m++ < 5 && spawnSort.hasMore()) {
			Event e = spawnSort.pollGreatest();
			if (GAME.EVENT().trySet(e)) {
				return true;
			}
		}
		return !spawnSort.hasMore();
	}
	

	
	
}
