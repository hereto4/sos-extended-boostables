package game.events;

import java.io.IOException;

import game.GAME.GameResource;
import game.debug.Profiler;
import game.GameDisposable;
import game.events.advice.EventAdvisor;
import game.events.citizen.EventCitizen;
import game.events.disaster.EventAccident;
import game.events.disaster.EventDisease;
import game.events.disaster.EventDiseaseMild;
import game.events.disaster.EventTemperature;
import game.events.faction.EventWorld;
import game.events.general.EventGeneral;
import game.events.killer.EventKiller;
import game.events.misc.EventSlaver;
import game.events.slave.EventUprising;
import game.events.world.EventWorldRebellion;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SuperSaver;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public final class EVENTS extends GameResource{

	public final EventSlaver slaver = new EventSlaver();
	public final EventCitizen riot = new EventCitizen();
	public final EventUprising uprising = new EventUprising();
	public final EventDisease disease = new EventDisease();
	public final EventDiseaseMild diseaseM = new EventDiseaseMild();
	public final EventKiller killer = new EventKiller();
	public final EventTemperature temperature = new EventTemperature();
	public final EventAdvisor advice = new EventAdvisor();
	public final EventAccident accident = new EventAccident();
	public final EventWorld world = new EventWorld();
	public final EventWorldRebellion rebellion = new EventWorldRebellion();
	public final EventGeneral general = new EventGeneral();
	
	private final SuperSaver<EventResource> saver = new SuperSaver<EventResource>(this.getClass(), all) {
		
		@Override
		protected void save(EventResource t, FilePutter f) {
			t.save(f);
		}
		
		@Override
		protected void load(EventResource t, FileGetter f) throws IOException {
			t.load(f);
		}
		
		@Override
		protected String key(EventResource t) {
			return t.key;
		}
		
		@Override
		protected void clear(EventResource t) {
			t.clear();
		}
	};
	
	public EVENTS() throws IOException {
		super("EVENTS", false);
	}
	
	@Override
	protected void save(FilePutter file) {
		saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		saver.load(file);
	}
	
	public void generate() {
		saver.clear();
	}
	
	@Override
	protected void loadFail() {
		saver.clear();
	}
	
	private static LinkedList<EventResource> all = new LinkedList<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all = new LinkedList<>();
			}
		};
	}
	
	public LIST<EventResource> all(){
		return all;
	}
	
	public static abstract class EventResource{
		
		private boolean supress;
		public final String key;
		
		protected EventResource(String key) {
			all.add(this);
			this.key = key;
		}
		
		protected abstract void update(double ds);

		protected abstract void save(FilePutter file) ;

		protected abstract void load(FileGetter file) throws IOException;

		protected abstract void clear();
		
		/**
		 * will stop the event from updating.
		 */
		public void supress(boolean supress) {
			this.supress = supress;
		}
		
	}

	@Override
	protected void update(double ds, Profiler prof) {
		if (!SETT.exists())
			return;
		prof.logStart(EVENTS.class);
		for (EventResource e : all)
			if (!e.supress)
				e.update(ds);
		prof.logEnd(EVENTS.class);
	}

}
