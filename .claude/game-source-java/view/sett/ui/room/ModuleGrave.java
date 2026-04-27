package view.sett.ui.room;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.grave.GraveData.GRAVE_DATA_HOLDER;
import settlement.room.spirit.grave.GraveInfo;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleGrave implements ModuleMaker {


	
	public ModuleGrave(Init init) {
		
	}
	

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof GRAVE_DATA_HOLDER) {
			l.add(new I(((GRAVE_DATA_HOLDER) p).graveData()));
		}
		
	}
	
	private final class I extends UIRoomModule {
		
		private final GraveData g;
		
		I(GraveData g){
			this.g = g;
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {
			text.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text,  g.available.get(null), g.total.get(null));
				}
			}.hh(g.available.info()));
			
			text.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text,  g.respect.getD(null));
				}
			}.hh(g.respect.info()));
		
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
		}

		@Override
		public void appendButt(GuiSection s, GETTER<RoomInstance> ins) {
			DOUBLE d = new DOUBLE() {
				
				@Override
				public double getD() {
					double d = (double)g.available.get(ins.get())/g.total.get(ins.get());
					return d;
				}
			};
			
			s.addRightC(16, SPRITES.icons().s.death);
			
			s.addRightC(2, new GMeter.GMeterSprite(GMeter.C_REDGREEN, d, 48, 12));
			
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			GuiSection se = new GuiSection();
			se.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, g.available.get(get.get()), g.total.get(get.get()));
				}
			}.hv(g.available.info()));
			
			se.addRightC(100, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, g.respect.getD(get.get()));
				}
			}.hv(g.respect.info()));
			
			section.addRelBody(8, DIR.S, se);
			
			GTableBuilder builder = new GTableBuilder() {
				
				@Override
				public int nrOFEntries() {
					return g.total.get(get.get());
				}
			};
			
			builder.column(null, 350, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new HOVERABLE.HoverableAbs(350, Icon.M) {

						private final GText text = new GText(UI.FONT().M, 32);

						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							GraveInfo info = g.info(get.get(), ier.get());
							if (info != null) {
								int x1 = body().x1();
								//info.race().appearance().icon.render(r, body().x1(), body().y1());
								//x1 += Icon.M + 4;
								text.lablify().clear().set(info.name());
								text.renderCY(r, x1+8, body().cY());
//								x1 += text.width()+6;
//								text.clear().add(info.years());
//								text.renderCY(r, x1, body().cY());
//								
//								text.normalify2().clear().set(info.cause().name);
//								text.renderCY(r, body().x2()-4-text.width(), body().cY());
							}
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GraveInfo info = g.info(get.get(), ier.get());
							if (info != null) {
								GBox b = (GBox) text;
								b.title(info.name());
								
								b.text(info.race().info.namePosessive);
								b.text(info.type().name);
								b.NL();
								
								b.textL(Dic.¤¤Age);
								b.tab(6);
								b.add(GFORMAT.i(b.text(), info.years()));
								b.NL();
								
								b.NL(8);
								b.textL(info.cause().name);
								b.NL();
								b.add(b.text().add('(').add(info.cause().desc).add(')'));
							}
						}
					};
				}
			});
			
			int h = ISidePanel.HEIGHT-section.body().height()-16;
			
			section.addRelBody(8, DIR.S, builder.createHeight(h, true));
			
		}
		
		@Override
		public void hover(GBox box, Room i, int rx, int ry) {
			box.textL(g.available.info().name);
			box.add(GFORMAT.iofkInv(box.text(), g.available.get(i), g.total.get(i)));
			box.NL(2);
		}
		
	}

	





	
}
