package view.sett.ui.room;

import static settlement.main.SETT.ROOMS;

import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.category.RoomCategorySub;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.table.GScrollRows;
import view.interrupter.ISidePanel;

final class UIPanelUtil extends ISidePanel {

	
	static abstract class BlueprintList extends GuiSection{
		
		private RoomCategoryMain catCurrent = ROOMS().CATS.MAINS.get(0);
		private GuiSection list = new GuiSection();
		
		BlueprintList(int height){
			
			CatButt first = null;
			
			for (RoomCategoryMain m : ROOMS().CATS.MAINS) {
				CatButt b = new CatButt(m);
				b.add(m.icon, 0, 0);
				addToCat(b, m);
				b.pad(8, 8);
				
				RENDEROBJ list = makeList(m, height-b.body().height()-8);
				
				if (list != null) {
					
					b.list = list;
					if (first == null) {
						first = b;
					}
					addRightC(0, b);
				}
				
				
				
			}
			
			if (first == null)
				return;
			
			list.add(first.list);
			addRelBody(4, DIR.S, list);
			
		}
		
		private void set(RoomCategoryMain m, RENDEROBJ list) {
			int x1 = this.list.body().x1();
			int y1 = this.list.body().y1();
			this.list.clear();
			this.list.add(list);
			this.list.body().moveX1Y1(x1, y1);
			catCurrent = m;
		}
		
		private RENDEROBJ makeList(RoomCategoryMain cat, int height) {
			
			ArrayListGrower<RENDEROBJ> rows = new ArrayListGrower<>();
			ArrayListGrower<RoomBlueprintIns<?>> rooms = new ArrayListGrower<>();
			for (RoomCategorySub s : cat.subs) {
				//int am = 0;
				for (RoomBlueprintImp p : s.rooms()) {
					if (p instanceof RoomBlueprintIns<?>)
						if (p.isAvailable(SETT.ENV().climate())) {
							rooms.add((RoomBlueprintIns<?>) p);
							RENDEROBJ r = row((RoomBlueprintIns<?>) p);
							if (r != null) {
								rows.add(r);
								//am++;
							}
						}
				}
//				if (am > 0) {
//					sep(rows);
//				}
				
			}
			for (RoomBlueprintImp p : cat.misc.rooms()) {
				if (p instanceof RoomBlueprintIns<?>)
					if (p.isAvailable(SETT.ENV().climate())) {
						rooms.add((RoomBlueprintIns<?>) p);
						RENDEROBJ r = row((RoomBlueprintIns<?>) p);
						if (r != null) {
							rows.add(r);
						}
						
					}
			}
			if (rows.size() == 0)
				return null;
			GScrollRows s = new GScrollRows(rows, height) {
				@Override
				protected boolean passesFilter(int i, RENDEROBJ o) {
					return passes(rooms.get(i));
				}
			};
			return s.view();
		}
		
//		private void sep(LinkedList<RENDEROBJ> rows) {
//			rows.add(new RENDEROBJ.RenderImp(rows.getFirst().body().width(), rows.getFirst().body().height()) {
//				
//				@Override
//				public void render(SPRITE_RENDERER r, float ds) {
//					GCOLOR.UI().border().render(r, body.x1()+8, body.x2()-8, body.cY()-2, body.cY()+2);
//					GCOLOR.UI().bg().render(r, body.x1()+7, body.x2()-7, body.cY()-1, body.cY()+1);
//				}
//			});
//		}
		
		abstract RENDEROBJ row(RoomBlueprintIns<?> b);
		protected void addToCat(GuiSection s, RoomCategoryMain cat) {

		}
		
		void hoverCat(GBox b, RoomCategoryMain cat) {
			b.title(cat.name);
		}
		
		private class CatButt extends GButt.BSection{
			
			RENDEROBJ list;
			private final RoomCategoryMain m;
			
			CatButt(RoomCategoryMain m){
				this.m = m;
				
			}
			@Override
			protected void clickA() {
				if (hovered() == null || !(hovered() instanceof CLICKABLE)) {
					catCurrent = m;
					set(m, list);
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				hoverCat((GBox) text, m);
				
			}
			
			@Override
			protected void renAction() {
				selectedSet(catCurrent == m);
			}
		}
		
		public boolean passes(RoomBlueprintIns<?> blue) {
			return true;
		}
		
	}
	

	
	static class RoomRow extends GButt.BSection {
		
		protected final RoomBlueprintImp p;
		
		RoomRow(RoomBlueprintImp p){
			this.p = p;
			addRightC(4, p.iconBig());
			addRightC(8, new GHeader(p.info.names, 13).subify());
			body().setWidth(280);
			pad(2, 4);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(p.info.names);
		}
		
	}
	

}
