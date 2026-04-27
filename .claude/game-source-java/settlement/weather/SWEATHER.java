package settlement.weather;

import java.io.IOException;

import game.debug.Profiler;
import game.time.TIME;
import init.settings.S;
import settlement.main.SETT.SettResource;
import snake2d.Renderer;
import snake2d.util.color.RGB.RGBImp;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.rendering.RenderData;

public final class SWEATHER extends SettResource{


	private final RGBImp lightColor = new RGBImp();
	private final WeatherDownfallRenderer rainer = new WeatherDownfallRenderer();

	
	{
		WeatherThing.all = new LinkedList<>();
	}
	public final WeatherMoisture moisture = new WeatherMoisture();
	public final WeatherSnow snow = new WeatherSnow();
	public final WeatherIce ice = new WeatherIce();
	public final WeatherWind wind = new WeatherWind();
	public final WeatherTemp temp = new WeatherTemp();
	public final WeatherClouds clouds = new WeatherClouds();
	public final WeatherDownfall rain = new WeatherDownfall();
	public final WeatherThunder thunder = new WeatherThunder();
	public final WeatherGrowth growth = new WeatherGrowth();
	public final WeatherGrowthRipe growthRipe = new WeatherGrowthRipe();
	public final RainEvent downfall = new RainEvent();
	
	public SWEATHER() {
		super("WEATHER", true);
		new UIWeather(this);
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		lightColor.set(1, 1, 1);
		if (temp.cold() > 0 && TIME.light().dayIs()) {
			double d = 0.2*CLAMP.d(temp.cold()*2, 0, 1);
			lightColor.set(1-d/2, 1-d/2, 1+d);
		}else if(temp.heat() > 0 && TIME.light().dayIs()) {
			double d = 0.3*CLAMP.d(temp.heat()*2, 0, 1);
			lightColor.set(1+d/2, 1+d/4, 1-d/4);
		}
		lightColor.shade(1.0 - 0.5*clouds.getD());
				for (WeatherThing t : WeatherThing.all)
			t.update(ds);
		
		downfall.update(ds);
	}
	
	@Override
	protected void init(boolean loaded) {
		if (loaded)
			return;
		for (WeatherThing t : WeatherThing.all)
			t.init();
		downfall.saver.clear();
	}
	
	@Override
	protected void save(FilePutter file) {
		for (WeatherThing t : WeatherThing.all)
			t.save(file);
		downfall.saver.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		for (WeatherThing t : WeatherThing.all)
			t.load(file);
		downfall.saver.load(file);
	}
	
	public LIST<WeatherThing> all(){
		return WeatherThing.all;
	}
	
	public RGBImp lightColor() {
		return lightColor;
	}
	
	public void apply(RECTANGLE rec) {
		apply(rec.x1(), rec.x2(), rec.y1(), rec.y2());
		if (S.get().downpour.get() == 1)
			thunder.apply(0, 0, 0, 0);
	}
	
	public void apply(int x1, int x2, int y1, int y2) {
		TIME.light().apply(x1, x2, y1, y2, lightColor);
		if (S.get().downpour.get() == 1)
			thunder.apply(x1, x2, y1, y2);
	}
	
	public void renderDownfall(Renderer r, float ds, RenderData data, int zoomout) {
		if (S.get().downpour.get() == 1)
			rainer.render(r, ds, data, zoomout);
	}
	
	
}
