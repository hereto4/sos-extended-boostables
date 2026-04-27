package util.gui.misc;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.panel.GPanel;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.main.VIEW;

public class GDropDown<E extends CLICKABLE> extends CLICKABLE.ClickableAbs implements CLICKABLE {

	private final SPRITE title;
	private final int mX = C.SG*4;
	private final int mY = C.SG*1;
	private E selected;
	private GuiSection expansion = new GuiSection();
	private final Inter inter;
	private final ArrayListResize<E> es = new ArrayListResize<>(20, 500);
	private final CLICKABLE.ClickableAbs dummy = new CLICKABLE.ClickableAbs() {
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public GDropDown(SPRITE title) {
		this.title = title;
		body.setHeight(UI.FONT().S.height()+2*mY);
		this.inter = new Inter();
	}
	
	public GDropDown(CharSequence title) {
		this((SPRITE)new GText(UI.FONT().S, title).lablify());
	}
	
	public GDropDown(CharSequence title, int width) {
		this(sp(title, width));
	}
	
	private static SPRITE sp(CharSequence title, int width) {
		GText t = new GText(UI.FONT().S, title).lablify();
		return new SPRITE.Imp(width, t.height()+4) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				t.render(r, X1+2, Y1+2);
			}
		};
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		COLOR.WHITE05.render(r, body);
		
		if (!isActive) {
			COLOR.WHITE15.render(r, body.x1()+1, body.x2()-1, body.y1()+1, body.y2()-1);
		}else if(isHovered) {
			COLOR.WHITE30.render(r, body.x1()+1, body.x2()-1, body.y1()+1, body.y2()-1);
		}else {
			COLOR.WHITE20.render(r, body.x1()+1, body.x2()-1, body.y1()+1, body.y2()-1);
		}
		
		COLOR.WHITE05.render(r, body.x1()+title.width()+2*mX, body.x1()+title.width()+2*mX+1, body.y1(), body.y2());
		
		if (!isActive) {
			COLOR.WHITE50.bind();
		}else if(isHovered) {
			SPRITES.icons().s.arrowDown.render(r, body.x2()-Icon.S-mX, body.y1()+(body.height()-Icon.S)/2);
			COLOR.WHITE150.bind();
		}
		
		title.render(r, body.x1()+mX, body.y1() +(body.height()-title.height())/2);
		

		
		COLOR.unbind();
		
		if (selected != null) {
			int x1 = selected.body().x1();
			int y1 = selected.body().y1();
			selected.body().centerY(body);
			selected.body().moveX1(body.x1()+title.width()+3*mX);
			selected.render(r, ds);
			selected.body().moveX1Y1(x1, y1);
		}
	}
	
	@Override
	public boolean click() {
		if (super.click()) {
			if (!inter.isActivated())
				inter.show();
			else
				inter.hide();
			return true;
		}
		return false;
	}
	
	public E selected() {
		return selected;
	}
	
	public void setSelected(E s) {
		selected = s;
	}
	
	public GDropDown<E> add(E e){
		es.add(e);
		if (selected == null)
			selected = e;
		return this;
	}
	
//	protected InterManager interManager() {
//		return m;
//	}
	
	public GDropDown<E> init(){
		expansion.clear();
		int w = 0;
		int h = 0;
		for (E e : es) {
			if (e.body().width() > w)
				w = e.body().width();
			if (e.body().height() > h)
				h = e.body().height();
		}
		body.setWidth(title.width() + 4*mX + w);
		dummy.body.setWidth(w).setHeight(h);
		es.trim();
		
		GTableBuilder builder = new GTableBuilder() {
			@Override
			public int nrOFEntries() {
				return es.size();
			}
		};
		
		final int width = w;
		final int height = h;
		
		builder.column(null, w+2*mX, new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				
				
				CLICKABLE.ClickWrap wr = new CLICKABLE.ClickWrap(width, height) {
					@Override
					protected CLICKABLE pget() {
						if (ier.get() == null)
							return dummy;
						int i = ier.get();
						if (i >= es.size())
							return dummy;
						return es.get(i);
					}
					
					@Override
					public boolean click() {
						int i = ier.get();
						setSelected(es.get(i));
						if (super.click()) {
							
							if (i < es.size()) {
								
								inter.hide();
							}
							return true;
						}
						return false;
					}
				};
				return wr;
						
			}
		});
		
		int rows = Math.min(10, es.size());
		expansion = builder.create(rows, true);
		GPanel p = new GPanel();
		p.inner().set(expansion);
		expansion.add(p);
		expansion.moveLastToBack();
		
		
		return this;
	}
	
	private final class Inter extends Interrupter {

		protected Inter() {
			super();
		}

		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			return expansion.hover(mCoo);
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT)
				expansion.click();
			else if(button == MButt.RIGHT)
				hide();
		}

		private void show() {
			if (isActivated())
				return;
			
			expansion.body().moveC(body().cX(), 0);
			expansion.body().moveY1(body().y2());
			if (expansion.body().y2() > C.HEIGHT())
				expansion.body().moveY2(body().y2());
			super.show(VIEW.current().uiManager);
		}
		
		@Override
		protected boolean otherClick(MButt button) {
			hide();
			return false;
		}
		
		@Override
		public void hide() {
			super.hide();
		}
		
		
		@Override
		protected void hoverTimer(GBox text) {
			expansion.hoverInfoGet(text);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			expansion.render(r, ds);
			return true;
		}

		@Override
		protected boolean update(float ds) {
			if (KEYS.anyDown())
				hide();
			return true;
		}
		
	}
	
	

}
