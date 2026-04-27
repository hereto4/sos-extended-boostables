package view.ui.div;

import game.battle.util.DIV_SPEC;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;

public final class UIDivCardBasic implements DIMENSION{

	private final int WIDTH;
	private final int HEIGHT;
	private final UIDiv m;
	private final Rec body = new Rec();
	
	private GuiSection sec = new GuiSection();
	private DIV_SPEC current;
	private final UIDivStats stat = new UIDivStats();
	
	UIDivCardBasic(UIDiv m){
		this.m = m;
		this.WIDTH = m.WIDTH;
		this.HEIGHT = m.HEIGHT;
		
		{
			GuiSection s = new GuiSection();
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				SPRITE hh = new SPRITE.Imp(Icon.M) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (current.equip(e) == 0) {
							OPACITY.O50.bind();
						}
						e.resource.icon().render(r, X1, X2, Y1, Y2);
						OPACITY.unbind();
						
					}
				};
				
				RENDEROBJ o = new GStat() {
					
					@Override
					public void update(GText text) {
						if (current.equip(e) == 0) {
							text.color(COLOR.WHITE50).add('-');
						} else {
							GFORMAT.f(text, ((int)10*(current.equip(e)*e.equipMax))/10.0, 1);
						}
					}
				}.hh(hh);
				s.addGrid(o, e.indexMilitary(), 4, 48, 0);
				
			}
			
			GCOLOR.T().H1.bind();
			s.add(UI.icons().s.death, 0, s.body().y2()+2);
			
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percGood(text, ((int)100*(current.experience()))/100.0);
				}
			}.hh(Dic.¤¤Experience, 220));
			
			for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
				s.add(tt.room.icon.small, 0, s.body().y2()+2);
				s.addRightC(4, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.percGood(text, ((int)(100*current.training(tt)))/100.0);
					}
				}.hh(tt.stat.info().name, 220));
				
			}
			sec.add(s);
			
			sec.addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(WIDTH*2, HEIGHT*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UIDivCardBasic.this.render(r, body.x1(), body.y1(), 2, current, true, false, false);
				}
			});
		}
		
	}
	
	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int height() {
		return HEIGHT;
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1, int scale, DIV_SPEC d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		if (d == null)
			return;
		
		body.set(x1,x1+WIDTH*scale, y1, y1+HEIGHT*scale);
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		
		m.renderBasics(r, x1, y1, scale, d);
		
		int cx = body.cX();
		
		COLOR.BLACK.bind();
		UI.FONT().S.renderC(r, cx+1, body.y2()-9*scale, Str.TMP.clear().add(d.men()), scale);
		COLOR.unbind();
		UI.FONT().S.renderC(r, cx, body.y2()-10*scale, Str.TMP.clear().add(d.men()), scale);
		
		if (d.men() == 0 || !isActive) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, body);
			OPACITY.unbind();
		}
		
		GCOLOR.UI().border().renderFrame(r, body, 0, 1);
		
		
		
	}
	
	public void hover(DIV_SPEC d, GUI_BOX box) {
		if (d == null)
			return;
		GBox b = (GBox) box;

		b.title(d.name());
		
		current = d;
		b.add(sec);

		b.sep();

		b.add(stat.get(d));
	}
	
}
