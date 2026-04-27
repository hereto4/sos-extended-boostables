package view.menu;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollable;
import util.text.D;
import view.keyboard.KEYS;
import view.keyboard.Key;

class ScKeys extends GuiSection{

	

	
	final CharSequence ¤¤nameBig = "¤KEY SETTINGS";
	private final CharSequence ¤¤name = "¤key settings";
	private int page = 0;
	private Key hoveredKey;
	
	ScKeys(IMenu m, Font font, Font small) {
		
		D.t(this);
		
		CLICKABLE restore = new GButt.Glow(font.getText(D.g("restore"))) {
			
			@Override
			protected void clickA() {
				KEYS.get().restore();
				KEYS.get().save();
			}
		};
		add(restore);
		CLICKABLE cancel = new GButt.Glow(font.getText(D.g("cancel"))) {
			
			@Override
			protected void clickA() {
				m.setMain();
			}
		};
		addRightC(100, cancel);

		GuiSection keys = new GuiSection();
		
		ScrollRow[] butts = new ScrollRow[] {
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
			new Row(),
		};
		
		Scrollable s = new GScrollable(butts) {
			
			@Override
			public int nrOFEntries() {
				return (int) Math.ceil(KEYS.pages().get(page).all().size()/2.0);
			}
		};
		keys.add(s.getView());
		
		{
			GuiSection ss = new GuiSection();
			ss.add(new GButt.Glow(SPRITES.icons().m.arrow_left) {
				@Override
				protected void clickA() {
					page --;
					if (page < 0)
						page += KEYS.pages().size();
				}
			});
			ss.addRightC(100, new GStat() {
				
				@Override
				public void update(GText text) {
					text.setFont(UI.FONT().H2);
					text.add(KEYS.pages().get(page).name());
				}
			}.r(DIR.N));
			ss.addRightC(100, new GButt.Glow(SPRITES.icons().m.arrow_right) {
				@Override
				protected void clickA() {
					page ++;
					if (page >= KEYS.pages().size())
						page -= KEYS.pages().size();
				}
			});
			
			keys.addRelBody(8, DIR.N, ss);
		}
		
		keys.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				if (hoveredKey != null) {
					text.clear().add(hoveredKey.desc);
				}
			}
		}.r(DIR.N));
		
		keys.body().incrH(24);;
		
	
		keys.add(UI.decor().frame(keys.body()));
		
		addRelBody(32, DIR.N, keys);
		addRelBody(0, DIR.N, UI.decor().decorate(¤¤name));
		
		body().centerIn(C.DIM());
		
	}

	public GuiSection activate() {
		hoveredKey = null;
		page = 0;
		//GSettings.get().readFromFile();
		return this;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
	
		super.render(r, ds);
		hoveredKey = null;
	}
	
	private class Row extends GuiSection implements ScrollRow {
		
		private final KeyCode a;
		private final KeyCode b;
		
		Row(){
			a = new KeyCode();
			b = new KeyCode();
			add(a);
			addRightC(20, b);
		}
		
		@Override
		public void init(int index) {
			a.init(index);
			b.init((int) (Math.ceil(KEYS.pages().get(page).all().size()/2.0) + index));
		}
		
		
	}
	
	private class KeyCode extends CLICKABLE.ClickableAbs{

		private Key key;
		
		protected KeyCode() {
			body.setWidth(550);
			body.setHeight(UI.FONT().M.height());
			visableSet(false);
		}
		
		@Override
		protected void clickA() {
			if (key.rebindable) {
				KEYS.bind(key);
			}
			super.clickA();
		}
		
		void init(int i) {
			if (i < KEYS.pages().get(page).all().size()) {
				visableSet(true);
				key = KEYS.pages().get(page).all().get(i);
			}else {
				visableSet(false);
			}
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hoveredKey = key;
				return true;
			}
			return false;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			
			Str.TMP.clear();
			Str.TMP.add(key.name);
			
			isActive &= key.rebindable;
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isHovered && isSelected)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else
				GCOLOR.T().CLICKABLE.bind();
			UI.FONT().M.render(r, Str.TMP, body().x1(), body().y1());
			UI.FONT().M.render(r, key.repr(), body().x1() + 200, body().y1());
			COLOR.unbind();
			
		}
		
	}




	
}
