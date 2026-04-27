package settlement.tilemap.ground;



import init.type.CLIMATES;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GButt;
import util.gui.misc.GColorPicker;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class Debug {

	Debug(Ground g){
		
		LinkedList<CLICKABLE> bs = new LinkedList<>();
		GETTER_IMP<GroundType> get = new GETTER_IMP<GroundType>(g.types.NORMAL);
		
		for (GroundType t : g.types.ALL) {
			GButt b = new GButt.ButtPanel(t.icon) {
				@Override
				protected void clickA() {
					get.set(t);
				}
				@Override
				protected void renAction() {
					selectedSet(get.get() == t);
				}
			}.hoverSet(t);
			bs.add(b);
		}
		
		PlacableMulti p = new PlacableMulti("GROUND: Types") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				g.MAP.set(tx, ty, get.get());
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return bs;
			}
		};
		
		IDebugPanelSett.add(p);
		
		PlacableMulti undo = new PlacableMulti("GROUND: Moisture down") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				g.MOISTURE_CURRENT.increment(tx, ty, -Ground.MOISTURE_MAXI);	
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};
		
		p = new PlacableMulti("GROUND: Moisture") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				g.MOISTURE_CURRENT.increment(tx, ty, Ground.MOISTURE_MAXI);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public PLACABLE getUndo() {
				return undo;
			}
		};
		IDebugPanelSett.add(p);
		
		IDebugPanelSett.add("GROUND: set color", new ACTION() {
			int i = 0;
			
			@Override
			public void exe() {
				i++;
				i%= CLIMATES.ALL().size();
				COLOR wet = CLIMATES.ALL().get(i).colorGroundWet;
				COLOR dry = CLIMATES.ALL().get(i).colorGroundDry;
				LOG.ln(i);
				g.setColors(dry, wet, 0);
			}
		});
		
		IDebugPanelSett.add("GROUND: set color", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.s().panels.add(new DebugCol(), true);
			}
		});
		
	}
	
	private static class DebugCol extends ISidePanel {
		
		private final ColorImp dry = new ColorImp(COLOR.WHITE50);
		private final ColorImp wet = new ColorImp(COLOR.WHITE50);
		
		DebugCol(){
			titleSet("ground color");
			dry.set(SETT.GROUND().dry);
			wet.set(SETT.GROUND().wet);
			section.addDown(2, new GColorPicker(false, "dry") {
				
				@Override
				public ColorImp color() {
					return dry;
				}
				
				@Override
				public void change() {
					SETT.GROUND().setColors(dry, wet, 0);
				}
				
			});
			
			section.addDown(2, new GColorPicker(false, "wet") {
				
				@Override
				public ColorImp color() {
					return wet;
				}
				
				@Override
				public void change() {
					SETT.GROUND().setColors(dry, wet, 0);
				}
				
			});
		}
		
	}
	
}
