package world.entity;

import init.sprite.SPRITES;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.slider.GAllocator;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;
import world.WORLD;
import world.entity.haven.WHaven;
import world.entity.haven.WHavenType;

final class Placers extends ArrayListGrower<PLACABLE>{

	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Placers(LIST<WHavenType> types) {
		
		{
			final PlacableSimpleTile undo = new PlacableSimpleTile("camps remove") {
				
				@Override
				public void place(int tx, int ty) {
					for (WHaven h : WORLD.ENTITIES().havens.fillTile(tx, ty)) {
						h.delete();
					}
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty) {
					return WORLD.ENTITIES().havens.fillTile(tx, ty).size() > 0 ? null : E;
				}
			};
			
			GETTER_IMP<WHavenType> type = new GETTER_IMP<WHavenType>(types.get(0));
			
			GuiSection s = new GuiSection();
			for (WHavenType t : types) {
				s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
					
					@Override
					protected void clickA() {
						type.set(t);
					}
					
					@Override
					protected void renAction() {
						label =  t.race.appearance().icon;
						selectedSet(type.get() == t);
					}
					
				}.hoverInfoSet(t.race.info.names));
			}
			
			INTE inte = new INTE() {
				
				int i = 0;
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 8;
				}
				
				@Override
				public int get() {
					return i;
				}
				
				@Override
				public void set(int t) {
					i = t;
				}
			};
			
			s.addRelBody(2, DIR.S, new GAllocator(COLOR.RED100, inte, 6, 16));
			
			ArrayList<CLICKABLE> ss = new ArrayList<CLICKABLE>(s);
			
			add(new PlacableSimpleTile("camps") {
				@Override
				public CharSequence isPlacable(int tx, int ty) {
					return null;
				}

				int ni = 0;
				
				@Override
				public void place(int tx, int ty) {
					CharSequence nn = type.get().names.getC(ni++);
					
					Str.TMP.clear().add(nn);
					Str.TMP.insert(0, type.get().race.appearance().lastNamesNoble.getC(RND.rInt(0x0FFFF)));
					
					WORLD.ENTITIES().havens.create(tx, ty, type.get(), inte.getD(), nn);
					
				}
				
				@Override
				public LIST<CLICKABLE> getAdditionalButt() {
					return ss;
				}
				
				@Override
				public PLACABLE getUndo() {
					return undo;
				}
				
				@Override
				public SPRITE getIcon() {
					return WORLD.ENTITIES().havens.types.get(0).race.appearance().icon;
				}
			});

		}
		
		
	}



}
