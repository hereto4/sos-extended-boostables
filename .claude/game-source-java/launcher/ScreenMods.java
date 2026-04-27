package launcher;

import game.VERSION;
import init.paths.ModInfo;
import init.paths.ModInfo.ModInfoException;
import init.paths.PATHS;
import launcher.GUI.BSprite;
import launcher.GUI.BText;
import launcher.GUI.ScrollBox;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.text.D;

class ScreenMods extends GuiSection{

	{
		D.gInit(ScreenMods.this);
	}
	
	private ModInfo hoveredMod = null;
	private String errorMod = null;
	private String errorMessage = null;
	private Text hs;
	private final GuiSection mods = new GuiSection();
	private final Launcher l;
	
	
	private final CharSequence sOutdated = "(" + D.g("Outdated") + ")";
	private final CharSequence sBy = D.g("Author") + ": ";
	private final CharSequence sBroken = D.g("mborked", "Unsupported Mod");
	
	ScreenMods(Launcher l) {
		hs = new Text(l.res.font, 200).setScale(1);
		this.l = l;
		
		{
			GuiSection butts = new GuiSection();
			
			
			CLICKABLE b = new BText(l.res, D.g("Play"), 200){
				
				@Override
				protected void clickA() {
					
					if (PATHS.SCRIPT().hasExternal(l.s.mods.get())) {
						l.setModWarning();
						return;
					}
					
					l.s.save();
					Launcher.startGame = true;
					CORE.annihilate();
				}

			};
			
			butts.addRightC(64, b);
			
			b = new BText(l.res, D.g("Back"), 200).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					l.setMain();
				}
			});
			butts.addRightC(4, b);
			
			butts.body().moveX2(Sett.WIDTH-16);
			butts.body().moveY1(0);
			
			RENDEROBJ rs = new GUI.Header(l.res, D.g("Mods"));
			rs.body().moveX1(64);
			rs.body().moveCY(butts.body().cY());
			butts.add(rs);
			
			
			add(butts);
		}
		
		
		
		
		
		mods.body().setHeight(Sett.HEIGHT-body().height()-24);
		add(mods, 10, body().y2()+16);
		
		update(0);
		
		
		
		
		

		int am = 0;
		for (String s : l.s.mods.get()) {
			if (PATHS.local().MODS.exists(s))
				am++;
		}
		String[] mods = new String[am];
		am = 0;
		for (String s : l.s.mods.get()) {
			if (PATHS.local().MODS.exists(s))
				mods[am++] = s;
		}
		l.s.mods.set(mods);
		
		body().moveX1Y1(10, 10);
		
		update(0);
	}
	

	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		
		super.render(r, ds);
		int sx = body().x1()+470;
		int y1 = body().y1()+70;
		if (hoveredMod != null) {
			
			hs.setMaxWidth(400);
			
			hs.clear().add(hoveredMod.name).add(' ').add(hoveredMod.version);
			COLOR.GREEN100.bind();
			if (hoveredMod.majorVersion != VERSION.VERSION_MAJOR) {
				COLOR.ORANGE100.bind();
				hs.add(sOutdated);
			}
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.unbind();
			
			hs.clear().add(hoveredMod.desc);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			COLOR.BLUEISH.bind();
			hs.clear().add(sBy).add(hoveredMod.author);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.GREENISH.bind();
			hs.clear().add(hoveredMod.info);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.unbind();
			
			hs.clear();
			hs.add(hoveredMod.absolutePath);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			hoveredMod = null;
		}else if (errorMod != null) {
			hs.clear();
			hs.add(errorMessage);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			hs.clear();
			hs.add(errorMod);
			hs.adjustWidth();
			hs.render(r, sx, y1);
		}
		errorMod = null;
	}
	
//	private boolean error = true;
	
	private void update(double ds) {
		
		
		String[] paths = PATHS.local().MODS.folders();
		
		ScrollBox labels = new ScrollBox(this.mods.body().height());
		
		for (String st : paths){
			ModInfo i;
			try {
				i = new ModInfo(st);
				labels.add(new ModButt(i, l));
			} catch (ModInfoException e) {
				
				labels.add(new Borked(""+PATHS.local().MODS.getFolder(st).get().toAbsolutePath(), e.getMessage(), l));
				
				if (l.s.mods.get().length > 0) {
					boolean contains = false;
					for (String s : l.s.mods.get()) {
						if (s.equals(st)) {
							contains = true;
						}
					}
					if (contains) {
						String[] mods = new String[l.s.mods.get().length - 1];
						int k = 0;
						for (String s : l.s.mods.get()) {
							
							if (!s.equals(st)) {
								mods[k] = s;
								k++;
							}
						}
						l.s.mods.set(mods);
						l.s.save();
					}
					
				}
				
				
			}
			
		}

//		error = false;
		
		labels.body().incr(120, 110);
		
		CLICKABLE up = new BSprite(l.res.arrowUpDown[0]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				labels.scrollUp();
			}
		}); 

		CLICKABLE down = new BSprite(l.res.arrowUpDown[1]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				labels.scrollDown();
			}
		});
		labels.addNavButts(up, down);
		
		int x = this.mods.body().x1();
		int y = this.mods.body().y1();
		int h = this.mods.body().height();
		this.mods.clear();
		this.mods.add(up);
		this.mods.add(down, 0, h-down.body().height()-16);
		
		this.mods.add(labels, 48, 0);
		this.mods.body().moveX1(x).moveY1(y);
		
	}
	
	private void toggle(ModButt butt) {
		
	}
	
	private final Str str = new Str(5);
	
	private class ModButt extends Butt{

		final ModInfo i;
		private final Font font;
		ModButt(ModInfo info, Launcher l) {
			super(info.majorVersion != VERSION.VERSION_MAJOR ? COLOR.ORANGE100 : COLOR.WHITE100, l.res, info.name);
			i = info;
			font = l.res.font;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			int selectedIndex = getS();
			isSelected = selectedIndex != -1;
			if (i.majorVersion != VERSION.VERSION_MAJOR)
				isActive = false;
				
			super.render(r, ds, isActive, isSelected, isHovered);
			if (isSelected) {
				str.clear();
				str.add(selectedIndex);
				font.render(r, str, body().x1()+8, body().y1()+4, 1);
			}
		}
		
		@Override
		protected void clickA() {
			
			int selectedIndex = getS();
			if (selectedIndex == -1) {
				String[] mods = new String[l.s.mods.get().length + 1];
				for (int i = 0; i < l.s.mods.get().length; i++)
					mods[i] = l.s.mods.get()[i];
				mods[mods.length-1] = i.path;
				l.s.mods.set(mods);
			}else {
				
				String[] mods = new String[l.s.mods.get().length - 1];
				int k = 0;
				for (String s : l.s.mods.get()) {
					if (!s.equals(i.path)) {
						mods[k] = s;
						k++;
					}
				}
				l.s.mods.set(mods);
			}
			
			
			toggle(this);
			super.clickA();
		}
		
		private int getS() {
			if (l.s.mods.get() == null)
				return -1;
			int k = 0;
			for (String s : l.s.mods.get()) {
				if (i.path.equals(s))
					return k;
				k++;
			}
			return -1;
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hoveredMod = i;
				return true;
			}
			return false;
		}
		
	}
	

	
	private class Borked extends Butt{

		final String path;
		final String message;
		
		Borked(String path, String message, Launcher l) {
			super(COLOR.REDISH, l.res, sBroken);
			this.path = path;
			this.message = message;
			
		}
		
		
		@Override
		protected void clickA() {
			FileManager.openDesctop(path);
		}
		
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				errorMod = path;
				errorMessage = message;
				return true;
			}
			return false;
		}
		
	}

	public static abstract class Butt extends GUI.Button {

		Butt(COLOR col, RES res, CharSequence text) {
			super(sp(res, text, col));
		}

		private static SPRITE sp(RES res, CharSequence text, COLOR color) {
			return new SPRITE.Imp(380, res.font.height()+8) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					color.bind();
					res.font.renderCropped(r, text, X1+48, Y1+4, width()-48);
					COLOR.unbind();
				}
			};
		}
		
	}
}
