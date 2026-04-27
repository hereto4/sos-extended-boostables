package menu;

import static menu.GUI.getNavButt;

import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Font;
import util.gui.table.GScrollRows;
import util.text.D;
import view.menu.MenuScreen;

class ScCredits implements SC{

	
	private final GuiSection current;	
	static CharSequence ¤¤name = "¤credits";
	
	static {
		D.ts(ScCredits.class);
	}
	
	private final SC fame;
	ScCredits(Menu menu) {

		GuiSection main = new GUI.Shadower();
		
		MenuScreen sc = new MenuScreen(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		main.add(sc);
		
		Json json = new Json(PATHS.BASE().DATA.get("Credits"));
		
		
		
		int width = MenuScreen.inner.width();
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (String s : json.keys()) {
			Json jj = json.json(s);
			rows.add(new RENDEROBJ.RenderImp(width, 48) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GUI.COLORS.copper.bind();
					UI.FONT().H2.renderCX(r, body().cX(), body().y2()-24,jj.text("TITLE"));
				}
			});
			
			String[] nn = jj.texts("CREDS");
			for (int i = 0; i < nn.length; i++) {
				
				final int from = i;
				int w = 0;
				for (; i < nn.length; i++) {
					if (w > 0 && w + 50 + UI.FONT().M.getDim(nn[i]).x() > width)
						break;
					w += 50 + UI.FONT().M.getDim(nn[i]).x();
					
				}
				final int wi = w;
				final int to = i;
				rows.add(new RENDEROBJ.RenderImp(width, 32) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						GUI.COLORS.label.bind();
						Font f = UI.FONT().M;
						int x = body().cX()-wi/2 + 25;
						for (int i = from; i < to; i++) {
							f.render(r, nn[i], x, body().y1());
							x += f.getDim(nn[i]).x() + 50;
						}
						
						
					}
				});
				
			}
//			
//			for (String n : jj.texts("CREDS")) {
//				rows.add(new RENDEROBJ.RenderImp(width, 32) {
//					
//					@Override
//					public void render(SPRITE_RENDERER r, float ds) {
//						GUI.COLORS.label.bind();
//						UI.FONT().M.renderCX(r, body().cX(), body().y1(),n);
//					}
//				});
//			}
			
		}
		
		RENDEROBJ ta = new GScrollRows(rows, 32*13).view();
		
		ta.body().centerIn(main.body());
		main.add(ta);
		
		current = main;
		fame = new ScCreditsFame(menu);
		CLICKABLE cl = getNavButt(ScCreditsFame.¤¤name);
		cl.clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				menu.switchScreen(fame);
			};
			
		});
		sc.addButt(cl);
	}

	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (current.hover(mCoo))
			return true;
		return false;
	}

	@Override
	public boolean click() {
		current.click();
		return false;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		current.render(r, ds);
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
}
