package launcher;

import init.paths.PATH;
import init.paths.PATHS.PATHS_BASE;
import launcher.GUI.BText;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.text.D;

final class ScreenLang extends GuiSection{

	private final PATH plang = PATHS_BASE.langs();
	private CharSequence hov;
	private final Launcher l;
	public ScreenLang(Launcher l, boolean exit) {
		this.l = l;
		int i = 0;
		int cols = 10;
		int width = 64;
		int height = 64;
		
		addGridD(new Butt(null, i), i++, cols, width, height, DIR.C);
		for (String s : plang.folders()) {
			addGridD(new Butt(s, i), i++, cols, width, height, DIR.C);
		}
		
		body().moveC(Sett.WIDTH/2, Sett.HEIGHT/2);
		
		D.gInit(this);
		
		if (exit) {
			CLICKABLE b = new BText(l.res, D.g("Back")) {
				@Override
				protected void clickA() {
					l.setMain();
				}
			};
			b.body().moveY1(16).moveX2(Sett.WIDTH-16);
			add(b);
			
		}
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		super.render(r, ds);
		if (hov != null) {
			l.res.font.renderC(r, Sett.WIDTH/2, body().y2()+24, hov);
			hov = null;
		}
	};
	
	public CLICKABLE butt() {
		int si = 0;
		if (!l.s.lang.get().equals("")) {
			for (String s : plang.folders()) {
				si++;
				if (l.s.lang.get().equals(s)) {
					break;
				}
			}
		}
		
		return new GUI.Button(l.res.langs[si].scaled(2.0)) {
			
			@Override
			protected void clickA() {
				l.setLang();
			}
			
		};
		
	}
	
	private class Butt extends GUI.Button {
		private final String code;
		private final String name;
		
		Butt(String folder, int iconI){
			super(l.res.langs[iconI].scaled(2.0));
			
			if (folder == null) {
				code = "";
				name = "English";
			}else {
				code = folder;
				Json j = new Json(plang.getFolder(folder).get("_Info"));
				name = j.text("NAME") + " " + (int) (100 * j.d("COVERAGE")) + "%";
			}
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hov = name;
				return true;
			}
			return false;
		}
		
		@Override
		protected void clickA() {
			if (l.s.lang.get().equals(code)) {
				l.setMain();
			}else {
				l.s.lang.set(code);
				l.s.save();
				l.reboot();
			}
			
		}
		
	}
	
}
