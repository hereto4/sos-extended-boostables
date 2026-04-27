package view.sett.ui.standing;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public final class UICitizens extends ISidePanel {

	private final Cats cats = new Cats(HCLASSES.CITIZEN());
	private final CharSequence ¤¤all = "¤All";
	
	public UICitizens() {
		D.t(this);
		titleSet(HCLASSES.CITIZEN().names);
		section.add(new CitizenMain(HEIGHT, cats));
		section.addRelBody(8, DIR.W, makeList(¤¤all));

	}


	private static RENDEROBJ makeList(CharSequence ¤¤all) {
		RENDEROBJ[] rens = new RENDEROBJ[RACES.all().size() + 1];
		int i = 0;
		{
			GuiSection all = new GButt.BSection() {
				@Override
				protected void clickA() {
					CitizenMain.current = null;
				}

				@Override
				protected void renAction() {
					selectedSet(CitizenMain.current == null);
				}
			};
			all.body().incrW(Icon.L * 2);
			GHeader h = new GHeader(¤¤all);
			h.body().centerIn(all);
			all.add(h);
			DOUBLE d = new DOUBLE() {

				@Override
				public double getD() {
					return STANDINGS.CITIZEN().loyalty.getD(null);
				}
			};
			all.addDownC(2, GMeter.sprite(GMeter.C_REDGREEN, d, all.body().width(), 16));
			all.addDownC(2, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null, 0));
				}
			}.decrease().r(DIR.C));
			
			all.pad(4);
			
			rens[i++] = all;

		}

		for (int ii = 0; ii < RACES.all().size(); ii++) {
			final int ri = ii;
			GButt.BSection s = new GButt.BSection() {

				@Override
				protected void clickA() {
					CitizenMain.current = FACTIONS.player().races.get(ri);
				}

				@Override
				protected void renAction() {
					selectedSet(CitizenMain.current == FACTIONS.player().races.get(ri));
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(FACTIONS.player().races.get(ri).info.names);
					super.hoverInfoGet(text);
				}

			};
			s.add(new SPRITE.Imp(Icon.L*2, Icon.L*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					FACTIONS.player().races.get(ri).appearance().iconBig.render(r, X1, X2, Y1, Y2);
				}
			}, 0, 0);
			DOUBLE d = new DOUBLE() {

				@Override
				public double getD() {
					return STANDINGS.CITIZEN().loyalty.getD(FACTIONS.player().races.get(ri));
				}
			};
			s.addDown(2, GMeter.sprite(GMeter.C_REDGREEN, d, s.body().width(), 16));
			s.addDownC(2, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().POP.data(HCLASSES.CITIZEN()).get(FACTIONS.player().races.get(ri)));
				}
			}.decrease().r(DIR.C));
			s.pad(4);

			rens[i++] = s;
		}

		GScrollRows sc = new GScrollRows(rens, HEIGHT, 0);
		return sc.view();
	}


	public void open(Race res) {
		CitizenMain.current = res;
		VIEW.s().panels.add(this, true);
		VIEW.s().panels.add(cats.all.get(0), false);
		
	}
	
	public void openAccess(Race res) {
		CitizenMain.current = res;
		VIEW.s().panels.add(this, true);
		VIEW.s().panels.add(cats.access, false);
	}
	
	public void openEquip(Race res) {
		CitizenMain.current = res;
		VIEW.s().panels.add(this, true);
		VIEW.s().panels.add(cats.access, false);
	}
	
	public boolean equipIs(Race res) {
		return CitizenMain.current == res && VIEW.s().panels.added(cats.access);
	}



}
