package util;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import game.save.GameLoader;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.CORE;
import snake2d.CircleCooIterator;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.TextureHolder;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.Debugger;
import snake2d.util.misc.Debugger.Formatter;
import snake2d.util.sets.ArrayCooShort;
import util.data.AreaTmp;
import util.data.RANMAP;
import view.interrupter.IDebugPanel;

public class GUTIL {

	public static Data data;
	
	public final class Data {
		
		private final Debugger debugger;
		private final CircleCooIterator circleIterator;
		private final AreaTmp areaTmp = new AreaTmp();
		private final ArrayCooShort coos = new ArrayCooShort(Room.MAX_SIZE+1);
		//final PathThreadManager pathManager;
		private final PathUtilOnline pathOnline;
		private final RANMAP ran1 = new RANMAP();
		private final RANMAP ran2 = new RANMAP();
		

		TextureHolder texture;
		
		private Data() throws IOException{
			GUTIL.data = this;
	
			
			debugger = new Debugger(UI.FONT().M);
			
			//pathManager = new PathThreadManager(1, C.SETTLE_TSIZE, C.SETTLE_TSIZE);
			


			pathOnline = new PathUtilOnline(SETT.TWIDTH);
			circleIterator = new CircleCooIterator(120, pathOnline.getFlooder());
			debugger.add(debugger.new Value("Ents", 0, Formatter.Amount) {
				
				@Override
				protected double getValue() {
					if (SETT.ENTITIES() == null)
						return 0;
					return SETT.ENTITIES().size();
				}
			});
			debugger.add(debugger.new Value("Speed", 0, Formatter.Amount) {
				
				double t;
				double am;
				
				@Override
				protected double getValue() {
					t += GAME.SPEED.speed();
					am ++;
					if (am > 30) {
						t = t/am;
						am = 1;
					}
					return t / am;
				}
			});
			
//			debugger.add(debugger.new Value("pathLoad", 0, Formatter.PERCENTAGE){
//				protected double getValue(){
//					return pathManager.getLoadPercent();
//				}
//			});
			
			IDebugPanel.add("Reload Assets", new ACTION() {
				
				@Override
				public void exe() {
					Path p = GAME.saver().save("debugReload");
					if (p != null)
						CORE.setCurrentState(new GameLoader(p));
					
				}
				
			});
		}
		
	}
	
	public GUTIL() throws IOException{
		new Data();
		
	}
	
	public static Debugger debugger(){
		return data.debugger;
	}
	
	public static Flooder flooder(){
		return data.pathOnline.getFlooder();
	}
	
	public static PathUtilOnline.Marker marker(){
		return data.pathOnline.marker;
	}
	
	public static PathUtilOnline.Filler filler(){
		return data.pathOnline.filler;
	}
	
	public static PathUtilOnline.AStar astar(){
		return data.pathOnline.astar;
	}
	
	public static PathUtilOnline pathTools(){
		return data.pathOnline;
	}
	
	public static CircleCooIterator circle() {
		return data.circleIterator;
	}
	
	public static ArrayCooShort coos() {
		return data.coos;
	}
	
	public static RANMAP ran1() {
		return data.ran1;
	}
	
	public static RANMAP ran2() {
		return data.ran2;
	}
	
	public static AreaTmp AREA() {
		return data.areaTmp;
	}


}
