package settlement.environment;

import java.io.IOException;

import game.debug.Profiler;
import init.type.CLIMATE;
import init.type.CLIMATES;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ENVIRONMENT extends SettResource{

	private CLIMATE climate = CLIMATES.COLD();
	public final SettEnvMap map = new SettEnvMap();
	public final Foundation foundation;
	
	public ENVIRONMENT() throws IOException{
		super("ENVIRONMENT", true);
		foundation = new Foundation();
	}



	
	@Override
	protected void generate(CapitolArea area) {
		this.climate = area.climate();
		foundation.generate();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(climate.index());
		foundation.saver.save(file);
		
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		map.update(ds);
	}
	
	@Override
	protected void init(boolean loaded) {
		map.init();
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		climate = CLIMATES.ALL().get(file.i());
		foundation.saver.load(file);
	}
	
	public CLIMATE climate() {
		return climate;
	}
	

	
}
