package view.sett.ui.subject;

import init.sprite.UI.UI;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

final class SInfo{
	
	private static CharSequence ¤¤title = "Set Punishment";
	private static CharSequence ¤¤cancel = "Cancel manually assigned punishment, and let it be decided by your law settings.";
	static {
		D.ts(SInfo.class);
	}
	
	private final GuiSection section = new GuiSection();
	private final AInfo a;
	public static final int width = 560;
	

	
	SInfo(AInfo a, int height, HTYPE t) {
		
		section.add(new SInfoPortrait(a, t).section);
		
		section.addRelBody(4, DIR.S, top());
		
		if (t.player)
			section.addRelBody(4, DIR.S, new SInfoDesc(a, height-section.body().height()));
		else if (t == HTYPES.PRISONER()) {
			section.addRelBody(4, DIR.S, new UIPrisoner(a));
			section.addRelBody(4, DIR.S, new SInfoDesc(a, height-section.body().height()));
		}
		this.a = a;
		
		

		
	}
	
	private GuiSection top() {
		
		GuiSection s = new GuiSection();
		

		
		s.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(500, UI.FONT().H2.height()) {
			
			final GText name = new GText(UI.FONT().H2, 24);
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.T().H2.bind();
				name.clear();
				name.add(STATS.APPEARANCE().name(a.a.indu()));
				name.setMaxWidth(550);
				name.setMultipleLines(false);
				name.lablify();
				name.adjustWidth();
				name.renderC(r, body().cX(), body().cY());
				
			}
			
		});
		
		s.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(400, UI.FONT().S.height()) {
			
			GText text = new GText(UI.FONT().S, 36);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				text.clear();
				a.a.ai().getOccupation(a.a, text);
				text.normalify();
				text.adjustWidth();
				text.renderC(r, body().cX(), body().cY());
			}
			
		});
		
		
		return s;
	}
	
	GuiSection activate() {
		return section;
	}
	
	private final static class UIPrisoner extends GuiSection{

		private AInfo a;
		

		
		UIPrisoner(AInfo a){
			
			this.a = a;
			
			{
				GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.cancel) {
					
					@Override
					protected void clickA() {
						AIModule_Prisoner.DATA().punishmentSet.set(a.a.ai(), null);
						a.a.interrupt();
					}
					
					@Override
					protected void renAction() {
						activeSet(AIModule_Prisoner.DATA().punishmentSet.get(a.a.ai()) != null);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.text(¤¤cancel);
					}
					
				};
				
				b.setDim(40, 40);
				addRight(4, b);
			}
			for (Punishment p : LAW.process().punishments) {
				GButt.ButtPanel b = new GButt.ButtPanel(p.icon) {
					
					@Override
					protected void clickA() {
						if(p != AIModule_Prisoner.punishment(a.a, a.a.ai())) {
							AIModule_Prisoner.DATA().punishmentSet.set(a.a.ai(), p);
							a.a.interrupt();
						}
					}
					
					@Override
					protected void renAction() {
						selectedSet(p == AIModule_Prisoner.punishment(a.a, a.a.ai()));
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(p.action);
						b.text(p.desc);
						b.NL(4);
						b.textLL(Dic.¤¤Law);
						b.tab(6);
						b.add(GFORMAT.percInc(b.text(), p.multiplier));
					}
					
				};
				
				b.setDim(40, 40);
				
				addRight(4, b);
			}
			
			addRelBody(8, DIR.N, new GHeader(¤¤title));
			
			pad(8);
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (a == null || a.a.indu().hType() != HTYPES.PRISONER()) {
				VIEW.inters().section.deactivate();
				return;
			}
			super.render(r, ds);
		}



	}
	
}
