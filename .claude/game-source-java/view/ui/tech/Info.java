package view.ui.tech;

import game.faction.FACTIONS;
import game.faction.player.PTech.TechCurr;
import init.settings.S;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.room.infra.admin.AdminData;
import settlement.room.infra.admin.AdminData.ROOM_ADMIN_HOLDER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;

class Info extends GuiSection{


	Info(UITechTree tree, int width){

		
		
		GuiSection filter = filter(tree);
		
		int wi = width-filter.body().width();
		
		for (TechCurr c : FACTIONS.player().tech().currs()) {
			
			RENDEROBJ cc = curr(c);
			if (getLastX2() + cc.body().width() > wi) {
				add(cc, 0, body().y2());
			}else {
				addRight(0, cc);
			}
			
		}
		
		if (S.get().developer) {
			addRight(0, new GButt.ButtPanel(UI.icons().m.questionmark) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(TechTest.get(), this);
				}
			});
		}
		
		
		addCentredY(filter, wi);
		
	}
	
	private static GuiSection filter (UITechTree tree) {
		GuiSection s = new GuiSection();
		
		GInput in = new GInput(tree.filter);
		s.add(in);
		
		CLICKABLE boosts = new GButt.ButtPanel("+++") {
			
			@Override
			protected void clickA() {
				boolean bo = tree.bonuses();
				tree.filter.set(Dic.empty);
				tree.bonuses(bo ? false : true);
			}
			
			@Override
			protected void renAction() {
				selectedSet(tree.bonuses());
			};
			
		};
		boosts.hoverInfoSet(Dic.¤¤Boosts);
		
		s.addDown(4, boosts);
		s.pad(32, 4);
		return s;
		
	}

	private static GuiSection curr(TechCurr c) {
		GuiSection res = new GuiSection();
		
		GuiSection s = new GuiSection() {
			
			@Override
			protected void hoverInfoSelf(GUI_BOX box) {
				c.hover(box);
			}
			
		};
		
		s.add(UI.icons().s.dot.createColored(Node.cols.get(c.cu.index)), 0, 0);
		
		s.addRightC(4, c.cu.bo.icon);
		s.addRightC(4, new GHeader(c.cu.bo.name));
		s.addRightC(8, new GStat(UI.FONT().M) {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, c.available());
			}
		}.r(DIR.NW));
		GStaples st = new GStaples(c.produced().historyRecords()) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int i = c.produced().historyRecords()-1-stapleI;
				GText t = box.text();
				DicTime.setDaysAgo(t, i);
				box.add(t);
				box.NL();
				box.add(GFORMAT.i(box.text(), c.produced().get(i)));
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = c.produced().historyRecords()-1-stapleI;
				return c.produced().get(i);
			}
		};
		st.body().setDim(8*c.produced().historyRecords(), 32);
		s.add(st, s.body().x1(), s.body().y2()+4);
		
		res.add(s);
		
		for (RoomBlueprint b : SETT.ROOMS().all()) {
			if (b instanceof RoomBlueprintIns<?> && b instanceof ROOM_ADMIN_HOLDER) {
				AdminData d = ((ROOM_ADMIN_HOLDER)b).admin();
				if (d.target == c.cu.bo)
					res.addRelBody(0, DIR.E, producer((RoomBlueprintIns<?>) b, d));
			}
		}
		
		if (S.get().developer) {
			res.addRightC(8, new GButt.ButtPanel(UI.icons().s.plus) {
				@Override
				protected void clickA() {
					for (RoomBlueprint b : SETT.ROOMS().all()) {
						if (b instanceof RoomBlueprintIns<?> && b instanceof ROOM_ADMIN_HOLDER) {
							
							AdminData d = ((ROOM_ADMIN_HOLDER)b).admin();
							if (d.target == c.cu.bo)
								d.cheatAdd(500);
						}
					}
				}
			});
		}
		
		res.addRightC(8, GCOLOR.UI().border().makeSprite(1, res.body().height()));
		res.body().incrW(16);
		

		
		return res;
	}
	
	private static GuiSection producer(RoomBlueprintIns<?> blue, AdminData data) {
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(blue.info.names);
				

				b.textL(Dic.¤¤Produced);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), data.value()));
				b.NL();
				
				b.textL(Dic.¤¤ProductionRate);
				b.tab(6);
				b.add(GFORMAT.fRel(b.text(), data.knowledgePerStation*(blue.bonus() == null ? 1 : blue.bonus().get(POP_CL.clP())), data.knowledgePerStation));
				
				b.NL();
				b.textL(Dic.¤¤Employees);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), blue.employment().employed()));
				b.NL();
				
				b.sep();
				blue.bonus().hover(b, POP_CL.clP(), Dic.¤¤Boosts, true);
				
				
			}
			
		};
		

		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, data.value());
				
			}
		}.r(DIR.NW));
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.fRel(text, data.knowledgePerStation*(blue.bonus() == null ? 1 : blue.bonus().get(POP_CL.clP())), data.knowledgePerStation);
				
			}
		}.r(DIR.NW));
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blue.employment().employed());
				
			}
		}.r(DIR.NW));
		
		s.addRelBody(4, DIR.W, blue.iconBig());
		s.body().incrW(64);
		s.pad(32, 8);
		return s;
		
	}
	
}
