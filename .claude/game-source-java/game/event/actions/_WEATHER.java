package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import settlement.main.SETT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

final class _WEATHER extends EventActionConstructor{

	
//	private final ECollision coll = new ECollision();
	
	
	_WEATHER() {
		super("WEATHER");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}

	
	public final class Imp extends EventAction  {

		private double temperature = Double.NaN;
		private double downpour = Double.NaN;
		private double wind = Double.NaN;
		private double lightning = Double.NaN;
		private double clouds = Double.NaN;
//		private double destruction = Double.NaN;
		private COLOR sky = null;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			temperature = data.dTry("TEMPERATURE", -1, 1, Double.NaN);
			downpour = data.dTry("DOWNPOUR", 0, 1, Double.NaN);
			wind = data.dTry("WIND", 0, 1, Double.NaN);
			lightning = data.dTry("LIGHTNING", 0, 1, Double.NaN);
			clouds = data.dTry("CLOUDS", 0, 1, Double.NaN);
			if (data.has("SKY")) {
				sky = new ColorImp(data, "SKY");
			}
//			destruction = data.dTry("DESTRUCTION", 0, 1, Double.NaN);
			data.checkUnused();
		}
		
		@Override
		void setContext(Event e, EContext data) {
			acc = 0;
		}

		
		double acc = 0;
		
		@Override
		public void update(Event e, EContext context, double ds, double second) {
			double dd = 1.0;
			if (second < 10)
				dd = second/10.0;
			else if (GAME.EVENT().current().duration.seconds-second < 10)
				dd = (GAME.EVENT().current().duration.seconds-second)/10;
			if (Double.isFinite(temperature)) {
				
				SETT.WEATHER().temp.setD((temperature+1)*0.5);
			}
			if (Double.isFinite(downpour)) {
				SETT.WEATHER().rain.setD(downpour*dd);
			}
			if (Double.isFinite(wind)) {
				SETT.WEATHER().wind.setD(wind*dd);
			}
			if (Double.isFinite(lightning)) {
				SETT.WEATHER().thunder.setD(lightning*dd);
			}
			if (Double.isFinite(clouds)) {
				SETT.WEATHER().clouds.setD(clouds*dd);
			}
			if (sky != null) {
				SETT.WEATHER().lightColor().interpolate(COLOR.WHITE200, sky, dd);
			}
			
//			if (destruction != Double.NaN) {
//				
//				acc += ds*destruction;
//				
//				while(acc > 1) {
//					
//					
//					
//					acc -= 1;
//					int x = RND.rInt(SETT.PWIDTH);
//					int y = RND.rInt(SETT.PHEIGHT);
//					
//					for (ENTITY ee : SETT.ENTITIES().getArroundPoint(x, y, C.TILE_SIZE*2)){
//						double mom = EPHYSICS.MOM_TRESHOLDI + RND.rFloat()*2*EPHYSICS.MOM_TRESHOLDI;
//						coll.dirDot = 1.0;
//						coll.momentum = mom*ee.physics.getMass();
//						coll.damageStrength = 0;
//						coll.norX = SETT.WEATHER().wind.dirX();
//						coll.norY = SETT.WEATHER().wind.dirY();
//						coll.leave = CAUSE_LEAVES.getAccident();
//						coll.other = null;
//						
//						ee.collide(coll);
//					}
//					
//				}
//				
//			}
		}

		
	}




	
}
