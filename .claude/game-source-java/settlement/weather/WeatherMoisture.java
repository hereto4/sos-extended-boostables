package settlement.weather;

import game.faction.FACTIONS;
import game.time.TIME;
import settlement.main.SETT;
import settlement.room.industry.module.RoomBoost;
import settlement.room.main.RoomInstance;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.info.INFO;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageText;

public final class WeatherMoisture extends WeatherThing{


	private static CharSequence ¤¤name = "Precipitation";
	private static CharSequence ¤¤desc = "How often it has rained. Lack of downfall will decline Precipitation. Values below 25% will cause a drought that can be detrimental to growth.";
	
	private static CharSequence ¤¤drought = "¤Drought";
	private static CharSequence ¤¤droughtD = "¤Lack of rain has caused a drought. Our irrigation is working, but it's not enough. All our rooms dependent on water will be affected";
	private static final INFO binfo = new INFO(¤¤drought, ¤¤droughtD);
	private static CharSequence ¤¤mTitle = "¤Drought!";
	private static CharSequence ¤¤mBody = "¤The gods have forsaken {0}, and the rains have stopped. If this keeps up, it will devastate our crops!. Everyone must now pray.";
	
	private static double rainspeed = 2.0/(TIME.secondsPerHour());
	private static double dry = 1.0/(8*TIME.secondsPerDay());
	private double lastSnow = 0;
	private double sendTimer;
	
	
	static {
		D.ts(WeatherMoisture.class);
	}
	
	
	WeatherMoisture() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		double d = getD();
		if (!SETT.WEATHER().snow.rainIsSnow()) {
			d += ds*rainspeed*SETT.WEATHER().rain.getD();
		}
		
		double snow = SETT.WEATHER().snow.getD();
		double thawed = lastSnow - snow;
		lastSnow = snow;
		
		if (thawed > 0)
			d += thawed;
		lastSnow = SETT.WEATHER().snow.getD();
	
		
		if (SETT.WEATHER().temp.heat() > 0)
			d -= dry*ds;
		
		sendTimer -= ds;;
		

		setD(d);
		
	}
	
	public static RoomBoost makeBoost() {
		return new RoomBoost() {
			
			@Override
			public INFO info() {
				return binfo;
			}
			
			@Override
			public double get(RoomInstance r) {
				return CLAMP.d(SETT.WEATHER().moisture.growthValue(), 0, 1);
			}
		};
	}

	
	@Override
	public DOUBLE_MUTABLE setD(double d) {
		if (d < 0.25 && getD() >= 0.25) {
			if (sendTimer < 0 && !VIEW.b().isActive()) {
				Str.TMP.clear().add(¤¤mBody).insert(0, FACTIONS.player().name);
				new MessageText(¤¤mTitle).paragraph(Str.TMP).send();
				sendTimer = 10;
			}
		}
		return super.setD(d);
	}
	
	public double growthValue() {
		return CLAMP.d(getD()*4.0, 0, 1);
	}
	
	@Override
	protected void init() {
		setD(0.75);
	}
	
}
