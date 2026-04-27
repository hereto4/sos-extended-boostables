package view.interrupter;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.text.Dic;
import view.ui.top.UIPanelTop;

public final class ISidePanels extends Interrupter{
	
	private final ArrayList<Panel> free = new ArrayList<>(16);
	private final ArrayList<Panel> added = new ArrayList<>(16);
	private final GuiSection section = new GuiSection();
	private int x2;
	private final int x1;
	private final InterManager m;
	
	public ISidePanels(InterManager m, int x1) {
		this.m = m;
		for (int i = 0; i < 16; i++)
			free.add(new Panel());
		this.x1 = x1-1;
	}
	
	public void add(ISidePanel panel, boolean clear) {
		add(panel, clear, false);
	}
	
	public void addDontRemove(ISidePanel panel, ISidePanel panel2) {
		if (added(panel)) {
			add(panel, true);
			add(panel2, false);
		}else {
			add(panel2, true);
		}
	}
	
	public void addDontRemove(ISidePanel panel, ISidePanel panel2, ISidePanel panel3) {
		boolean p1 = added(panel);
		boolean p2 = added(panel2);
		clear();
		if (p1)
			add(panel, false);
		if (p2)
			add(panel2, false);
		add(panel3, false);
		
	}
	
	public void toggle(ISidePanel panel, boolean clear) {
		if (added(panel))
			remove(panel);
		else
			add(panel, clear, false);
	}
	
	public void add(ISidePanel panel, boolean clear, boolean pin) {
		if (clear) {
			remove();
		}
		for (int i = 0; i < added.size(); i++) {
			Panel p = added.get(i);
			if (p.panel == panel) {
				p.set(panel);
				rearrange();
				show(m);
				return;
			}
		}
		
		addP(panel, pin);
		show(m);
	}
	
	public void remove(ISidePanel panel) {
		for (int i = 0; i < added.size(); i++) {
			Panel p = added.get(i);
			if (p.panel == panel) {
				added.removeOrdered(i);
				free.add(p);
				rearrange();
				return;
			}
		}
	}
	
	@Override
	protected boolean otherClick(MButt button) {
		if (button == MButt.RIGHT && added.size() > 0) {
			for (int i = added.size()-1; i >= 0; i--) {
				Panel p = added.get(i);
				if (p.panel.back())
					return false;
				if (!p.pinned) {
					added.removeOrdered(i);
					free.add(p);
					rearrange();
					return true;
				}
			}
			
			
		}
		return false;
			
	}
	
	public void clear() {
		for (Panel p : added)
			free.add(p);
		added.clear();
		rearrange();
	}
	
	public boolean added(ISidePanel panel) {
		if (!super.isActivated())
			return false;
		for (int i = 0; i < added.size(); i++) {
			Panel p = added.get(i);
			if (p.panel == panel)
				return true;
		}
		return false;
	}
	
	private void addP(ISidePanel panel, boolean pinned) {
		Panel p = free.removeLast();
		p.set(panel);
		added.add(p);
		rearrange();
		p.pinned = pinned;
		panel.addAction();
		panel.update(0);
	}
	
	private void remove() {
		for (int i = 0; i < added.size(); i++) {
			Panel p = added.get(i);
			if (!p.pinned) {
				free.add(p);
				added.removeOrdered(i);
				i--;
			}
		}
	}
	
	private void rearrange() {
		section.clear();
		x2 = x1;
		for (Panel p : added) {
			p.panel.last = this;
			p.body().moveX1Y1(section.getLastX2(), UIPanelTop.HEIGHT);
			section.add(p);
		}
		section.body().moveX1(x1);
		section.body().moveY1(UIPanelTop.HEIGHT);
		x2 = section.body().x2();
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (MButt.LEFT == button)
			section.click();
		if (MButt.RIGHT == button) {
			otherClick(button);
		}
			
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if (x2 > 0) {
			addManager.viewPort().moveX1(x2);
			addManager.viewPort().setWidth(C.WIDTH()-x2);
		}
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		for (Panel p : added)
			p.panel.update(ds);
		return true;
	}
	
	private class Panel extends GuiSection{
		boolean pinned;
		private GText title = new GText(UI.FONT().H2, 20).lablify();
		private ISidePanel panel;
		private final CLICKABLE close = new GButt.ButtPanel(SPRITES.icons().m.exit) {
			@Override
			protected void clickA() {
				remove(panel);
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(Dic.¤¤Close);
				text.add(text.text().add('(').add(Dic.¤¤RightClick).add(')'));
			}
		};
		
		void set(ISidePanel panel) {
			
			clear();
			CLICKABLE s = panel.section();
			body().setHeight(C.HEIGHT()-UIPanelTop.HEIGHT);
			body().setWidth(s.body().width()+ISidePanel.M*2);
			body().moveY1(ISidePanel.Y1);
			s.body().centerIn(this);
			s.body().moveY1(ISidePanel.Y2+ISidePanel.M);
			add(s);
			close.body().moveC(body().x2()-(close.body().width()/2+8), ISidePanel.Y1+(ISidePanel.Y2-ISidePanel.Y1)/2);
			add(close);
			this.panel = panel;
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (panel.title != null)
				this.title.clear().add(panel.title).adjustWidth();
			
			
			
			
			
			COLOR.WHITE10.render(r, body().x1(), body().x2(), ISidePanel.Y1, C.HEIGHT());
			UI.PANEL().butt.render(r, body().x1(), body().x2()-3, ISidePanel.Y1+UI.PANEL().butt.margin, ISidePanel.Y2-UI.PANEL().butt.margin, 0, DIR.N.mask()|DIR.S.mask());
			
			GCOLOR.UI().border(r, body().x1(), body().x1()+3, ISidePanel.Y1, C.HEIGHT());
			GCOLOR.UI().border(r, body().x2()-3, body().x2(), ISidePanel.Y1, C.HEIGHT());

			


		
			{

				if (title.length() != 0) {
					title.adjustWidth();
					int x = body().x1() + (close.body().x1()-body().x1())/2;
					int y = close.body().cY();
					title.renderC(r, x, y);
				}
			}
			
			
//			
//			
//			
//			{
//				SPRITE s = UI.PANEL().panelL.get(DIR.W, DIR.E, DIR.S);
//				
//				int x1 = body().x1();
//				int y1 = ISidePanel.Y2;
//				for (int x = 0; x < ws; x++) {
//					s.render(r, x1+x*s.width(), y1);
//				}
//				if (rm != 0) {
//					s.render(r, body().x2()-s.width(), y1);
//				}
//				s = UI.PANEL().panelL.get(DIR.W, DIR.E, DIR.N, DIR.S);
//				y1+=s.height();
//				
//				while(y1 < body().y2()) {
//					for (int x = 0; x < ws; x++) {
//						s.render(r, x1+x*s.width(), y1);
//					}
//					if (rm != 0) {
//						s.render(r, body().x2()-s.width(), y1);
//					}
//					y1+=s.height();
//				}
//			}
//			
//			UI.PANEL().hollow.renderVertical(r, body().x1(), UIPanelTop.HEIGHT, body().height());
//			
//			UI.PANEL().hollow.renderVertical(r, body().x2()-GFrame.MARGIN*3, UIPanelTop.HEIGHT, body().height());
			
			super.render(r, ds);
			
		}
		
	}

}
