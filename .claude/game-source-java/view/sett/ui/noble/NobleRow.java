package view.sett.ui.noble;

import game.GAME;
import game.nobility.Noble;
import game.nobility.NobleOffice;
import init.race.appearence.RPortrait;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import view.main.VIEW;

class NobleRow extends GuiSection{

	private static CharSequence ¤¤Rank = "Current rank of this noble. More ranks allows a noble to contribute a lot more towards their current assignment. Ranks are gained by levelling up your city. A noble can not be stripped of ranks, but the ranks will become available after their death.";
	private static CharSequence ¤¤no = "Unassigned Nobility";
	private static CharSequence ¤¤assign = "Assign this noble to an office.";
	static {
		D.ts(NobleRow.class);
	}
	
	public static int width = 500;
	private final GETTER<Integer> ier;
	
	public NobleRow(GETTER<Integer> ier) {
		this.ier = ier;
		
		GStat s = new GStat(UI.FONT().S) {
			
			@Override
			public void update(GText text) {
				text.lablifySub();
				text.add(n().rankName());
			}
			
		};
		add(s, 0, 0);
		
		s = new GStat(UI.FONT().H2) {
			
			@Override
			public void update(GText text) {
				text.lablify();
				text.add(STATS.APPEARANCE().nameFirst(n().subject().indu())).s().add(STATS.APPEARANCE().nameLast(n().subject().indu()));
				text.setMaxWidth(420);
				text.setMultipleLines(false);
				
			}
		};
		addDown(2, s);
		
		s = new GStat(UI.FONT().S) {
			
			@Override
			public void update(GText text) {
				if (n().office() == null) {
					text.warnify().add(¤¤no);
				}else
					text.lablifySub().add(n().title());
				
			}
		};
		addDown(2, s);
				
		{
			GButt.ButtPanel p = new GButt.ButtPanel(new Assignments()) {
				
				@Override
				protected void clickA() {
					VIEW.s().ui.nobles.assigns.n = n();
					VIEW.inters().popup.show(VIEW.s().ui.nobles.assigns, this);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (n().office() == null)
						text.text(¤¤assign);
					else
						n().hoverOffice(text);
					
				}
				
			};
			p.pad(4, 4);
			addDown(4, p);
			
			GuiSection rank = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤Rank);
				}
			};
			
			
			rank.addRightC(8, new GButt.ButtPanel(UI.icons().s.chevron(DIR.N)) {
				@Override
				protected void clickA() {
					GAME.NOBLE().ranksAllocate(n());
				}
				@Override
				protected void renAction() {
					activeSet(n().rank() < GAME.NOBLE().maxRanks()-1 && GAME.NOBLE().ranksAllocated() < (int) GAME.NOBLE().MAX_RANKS.get(POP_CL.clP()));
				}
			}.pad(4, 4));
			
			rank.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, n().rank());
				}
			});
			
			addRightC(16, rank);
			
			addRight(32, new GButt.ButtPanel(UI.icons().m.crossair) {
				@Override
				protected void clickA() {
					VIEW.s().activate();
					VIEW.s().getWindow().centerer.set(n().subject().body().cX(), n().subject().body().cY());
				}
			});
			
			
		}
		
		
		
		
		SPRITE p = new SPRITE.Imp(RPortrait.P_WIDTH*2, RPortrait.P_HEIGHT*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				STATS.APPEARANCE().portraitRender(r, n().subject().indu(), X1, Y1, 2);
			}
		};
		
		addRelBody(16, DIR.W, p);
		body().pad(16, 2);
		body().setWidth(width);
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		GButt.ButtPanel.renderBG(r, true, false, false, body());
		GButt.ButtPanel.renderFrame(r, body());
		super.render(r, ds);
	}

	
	private Noble n() {
		return GAME.NOBLE().active().get(ier.get());
	}
	
	private class Assignments extends SPRITE.Imp {

		public Assignments() {
			super(Icon.L, Icon.L);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int cy = Y1 + (Y2-Y1)/2;
			int cx = X1 + (X2-X1)/2;
			NobleOffice o = n().office();
			if (o == null)
				UI.icons().m.questionmark.renderC(r, cx, cy);
			else {
				o.icon.renderC(r, cx, cy);
			}
		}
		
		
	}
	
}
