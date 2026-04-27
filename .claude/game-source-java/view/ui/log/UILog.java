package view.ui.log;

import game.time.TIME;
import init.constant.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.D;
import util.text.DicTime;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.WORLD;
import world.log.LogEntry;

public final class UILog extends Interrupter {

	public static CharSequence ¤¤name = "World log";
	private final GuiSection section;
	private final static int ww = 450;
	
	static {
		D.ts(UILog.class);
	}
	
	public UILog(VIEW view) {

		section = new GuiSection();
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return WORLD.LOG().all().size();
			}

		};
		
		builder.column(null, ww, new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Entry(ier);
			}
		});
		
		section.add(builder.createHeight(700, false));
		
		GPanel p = new GPanel().setBig();
		p.set(section.body());
		
		p.setCloseAction(new ACTION() {
			@Override
			public void exe() {
				hide();
			}
		});
		p.body().centerY(C.DIM());
		p.body().moveX2(C.WIDTH()-20);
		section.body().centerIn(p);
		section.add(p);
		section.moveLastToBack();
		p.setTitle(¤¤name, UI.FONT().H2);
		
		
		
	}

	public void activate() {
		show(VIEW.inters().manager);
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		if (button == MButt.RIGHT)
			hide();
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private static class Entry extends ClickableAbs{
		
		private final GETTER<Integer> ier;
		private final static Str tmp = new Str(128);
		
		Entry(GETTER<Integer> ier){
			super(ww, UI.FONT().M.height()*2 + 30 + 16);
			this.ier = ier;
		}
		
		private LogEntry e() {
			if (ier.get() >= WORLD.LOG().all().size())
				return null;
			return WORLD.LOG().all().get(WORLD.LOG().all().size()-1-ier.get());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			LogEntry e = e();
			if (e == null)
				return;
			
			if (isHovered)
				COLOR.WHITE15.render(r, body);
			
			{
				int x1 = body.x1() + 16;
				int cy = body.y1()+20;
				
				e.icon().renderCY(r, x1, cy);
				
				if (e.bannerA() != null) {
					e.bannerA().MEDIUM.renderCY(r, x1+24, cy);
				}
				if (e.bannerB() != null) {
					e.bannerB().MEDIUM.renderCY(r, x1+50, cy);
				}
				
				tmp.clear();
				DicTime.setDateShort(tmp, e.daySinceStart()*TIME.secondsPerDay());
				
				GCOLOR.T().H1.bind();
				UI.FONT().H2.render(r, tmp, x1+76, cy-UI.FONT().H2.height()/2);
				COLOR.unbind();

			}
			
			
			
			{
				int x1 = body.x1();
				int y1 = body.y1()+32;
				UI.FONT().M.renderIn(r, x1, y1, DIR.NW, e.message, body.width() , body.y2()-y1-8, 1);
			}
			
			GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
			
		}
		
		@Override
		protected void clickA() {
			LogEntry e = e();
			if (e == null)
				return;
			VIEW.world().activate();
			VIEW.world().window.centererTile.set(e.tx(), e.ty());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			LogEntry e = e();
			if (e == null)
				return;
			text.text(e.message);
		}
		
	}
	
}