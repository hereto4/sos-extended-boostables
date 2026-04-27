package settlement.entry;

import java.io.IOException;

import game.debug.Profiler;
import game.faction.FACTIONS;
import init.race.Race;
import init.type.HTYPE;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.rendering.RenderData;
import view.main.VIEW;

public final class SENTRY extends SettResource{

	public SENTRY() {
		super("SENTRY", false);
		// TODO Auto-generated constructor stub
	}

	private final PeopleSpawner spawn = new PeopleSpawner();
	private final Immigration im = new Immigration();
	public final EntryPoints points = new EntryPoints();
	private final Updater updater = new Updater();
	
	public void add(Race race, HTYPE type, int amount) {
		if (amount <= 0)
			return;
		spawn.add(race, type, amount);
	}
	
	public int onTheirWay(Race race, HTYPE type) {
		return spawn.onTheirWay(race, type);
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		
		if (VIEW.b().isActive())
			return;
		
		if (FACTIONS.player().capitolRegion() == null)
			return;
		
		points.update();
		
		updater.update(ds, points);
		
		if (isClosed()) {
			spawn.update(0);
			im.update(0);
		}else {
			spawn.update(ds);
			im.update(ds);
		}
	}
	
	public void render(Renderer r, RenderData renData) {
		
		points.render(r, renData);
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		updater.save(file);
		points.saver.save(file);
		spawn.save(file);
		im.saver.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
		points.saver.load(file);
		spawn.load(file);
		im.saver.load(file);
	}
	
	@Override
	protected void clear() {
		updater.clear();
		points.saver.clear();
		spawn.clear();
		im.saver.clear();
	}
	
	@Override
	protected void init(boolean loaded) {

	}
	
	public Immigration immi() {
		return im;
	}
	
	public boolean isClosed() {
		return updater.isClosed() || SETT.INVADOR().invading();
	}
	
	public boolean beseiged() {
		return updater.beseiged();
	}
	
	public double besigeTime() {
		return updater.besigeTime();
	}
	
	
}
