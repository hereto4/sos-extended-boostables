package view.sett.ui.subject;

import game.GAME;
import game.boosting.tmp.TmpBoostingButt;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.room.service.nursery.ROOM_NURSERY;
import settlement.stats.STATS;
import settlement.stats.equip.Equip;
import settlement.stats.equip.StatsEquip;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.panel.GFrame;
import util.info.GFORMAT;
import util.text.D;
import view.main.VIEW;

final class SInfoPortrait extends GuiSection{
	
	private final AInfo a;
	final GuiSection section = new GuiSection();
	
	private static CharSequence ¤¤resource = "¤Carried resource";
	static {
		D.ts(SInfoPortrait.class);
	}

	SInfoPortrait(AInfo a, HTYPE t) {
		this.a = a;
		
		HOVERABLE sprite = new HOVERABLE.HoverableAbs(RPortrait.P_WIDTH*4+16,RPortrait.P_HEIGHT*4+16 ) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				a.a.indu().hType().color.render(r, body());
				GFrame.render(r, body().x1(), body().x2(), body().y1(), body().y2());
				
				if (a.a.indu().hType() == HTYPES.CHILD()) {
					for (ROOM_NURSERY ro : SETT.ROOMS().NURSERIES) {
						if (ro.race == a.a.race()) {
							int w = RPortrait.P_WIDTH*4;
							int h = RPortrait.P_HEIGHT*4;
							w -= Icon.HUGE*2;
							h -= Icon.HUGE*2;
							w /= 2;
							h /= 2;
							ro.icon.renderScaled(r, body.x1()+8+w, body.y1()+8+h, 4);
							return;
						}
					}
				}else {
					STATS.APPEARANCE().portraitRender(r, a.a.indu(), body.x1()+8, body().y1()+8, 4);
				}
				
				
				OPACITY.O25TO100.bind();
				if (SProblem.problem(a.a) != null) {
					GCOLOR.UI().BAD.hovered.bind();
					UI.icons().s.flag.renderScaled(r, body().x1()+8, body().y1()+8, 2);
				}
				else if (SProblem.warning(a.a) != null) {
					GCOLOR.UI().SOSO.hovered.bind();
					UI.icons().s.flag.renderScaled(r, body().x1()+8, body().y1()+8, 2);
				}
				OPACITY.unbind();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				if (SProblem.problem(a.a) != null) {
					b.add(b.text().errorify().add(SProblem.problem(a.a)));
					
				}
				else if (SProblem.warning(a.a) != null) {
					b.add(b.text().warnify().add(SProblem.warning(a.a)));
				}
				super.hoverInfoGet(text);
			}
		};
		
		RENDEROBJ l = pleft();
		RENDEROBJ r = new SInfoActions(a, t);
		
		int w = Math.max(l.body().width(), r.body().width()) + 16;
		section.body().setWidth(w*2 + sprite.body().width());
		section.addC(sprite, section.body().cX(), 0);
		
		l.body().moveCY(section.body().cY());
		l.body().moveCX(section.body().x1() + w/2);
		section.add(l);
		
		r.body().moveCY(section.body().cY());
		r.body().moveCX(section.body().x2() - w/2);
		section.add(r);
	}
	
	
	private GuiSection pleft() {
		
		GuiSection s = new GuiSection();
		
		StatsEquip pr = STATS.EQUIP();		
	
		int ii = 0;
		final int rrr = 4;
		for (Equip pp : pr.allE()) {
			STAT p = pp.stat();
			CLICKABLE c = new CLICKABLE.ClickableAbs(Icon.L+8, Icon.L+Icon.M/2) {
				private final GText t = new GText(UI.FONT().S, 8); 
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					
					if (isHovered) {
						COLOR.BLUEDARK.render(r, body());
					}
					
					pp.resource().icon().renderC(r, body().cX(), body().y1()+Icon.L/2);
					t.clear();
					int am = p.indu().get(a.a.indu());
					int max = pp.max(a.a.indu());
					GFORMAT.iofk(t, am, max);
					t.lablify();
					t.adjustWidth();
					t.renderC(r, body().cX(), body().y1()+Icon.L);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {

					pp.hover(text, a.a.indu());
				}
				
				@Override
				protected void clickA() {
					if (S.get().developer)
						SDebugInput.activate(p.indu(), a.a);
					else
						VIEW.s().ui.standing.openAccess(a.a.race());
				}
			};
			
			s.addGrid(c, ii++, rrr, 4, 4);
		}
		
		
		s.addRelBody(8, DIR.N, new HoverableAbs(Icon.M*2, Icon.M) {
			private final GText t = new GText(UI.FONT().M, 4);
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				if (a.a.ai().resourceCarried() != null) {
					t.clear().add(a.a.ai().resourceA());
					t.renderCY(r, body().x1(), body().cY());
					a.a.ai().resourceCarried().icon().render(r, body().x1()+Icon.M, body().y1());
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤resource);
				text.NL();
				if (a.a.ai().resourceCarried() != null) {
					text.text(a.a.ai().resourceCarried().name);
					
				}
			}
			
		
		});
		
		GETTER<POP_CL> g = new GETTER<POP_CL>() {

			@Override
			public POP_CL get() {
				return POP_CL.clP(a.a.indu());
			}
			
		};
		
		s.addRelBody(8, DIR.N, TmpBoostingButt.make(g, GAME.BOOST().popcl));
		return s;
	}

	
}
