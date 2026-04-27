package settlement.weather;

import java.io.IOException;

import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.DOUBLE;
import util.text.D;

public final class WeatherWind extends WeatherThing{

	private static CharSequence ¤¤name = "Wind";
	private static CharSequence ¤¤desc = "Wind Strength";
	
	static {
		D.ts(WeatherWind.class);
	}
	
	WeatherWind() {
		super(¤¤name, ¤¤desc);
		target = RND.rFloat();
		setD(RND.rFloat());
		dayM = -0.5 + RND.rFloat();
	}

	private final double MAX = 1.0;
//	private double current;
	private double target;
	private double dayM;
	private double speed = 0.1;
	private int day = -1;
	private double t;
	
	@Override
	protected void update(double ds) {
		double next = adjustTowards(getD(), ds*speed, target);
		if (next == getD())
			reset();
		setD(CLAMP.d(next, 0, MAX));
		t += ds*getD();
		if (t > Integer.MAX_VALUE)
			t -= Integer.MAX_VALUE;
		//setD(0.5);
	}
	
	private void reset() {
		if (day != TIME.days().bitsSinceStart()) {
			day = TIME.days().bitsSinceStart();
			dayM = -0.5 + RND.rFloat();
		}
		target = CLAMP.d(dayM + RND.rFloat(), 0, 1);
		speed = 0.1 * (0.25 + RND.rFloat()*0.75);
	}
	
	public void setDayTarget(double target) {
		dayM = target;
	}

	@Override
	protected void save(FilePutter file) {
		file.d(target);
		file.d(dayM);
		file.d(t);
		file.i(day);
		super.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		target = file.d();
		dayM = file.d();
		t = file.d();
		day = file.i();
		super.load(file);
	}
	
	public double x() {
		return -getD();
	}
	
	public double y() {
		return -getD();
	}
	
	public double dirX() {
		return -1;
	}
	
	public double dirY() {
		return 1;
	}
	
	@Override
	protected void init() {
		reset();
		setD(target);
	}
	
	public final DOUBLE time = new DOUBLE() {
		
		@Override
		public double getD() {
			return t;
		}
	};

	
}
