
package launcher;

import java.io.IOException;
import java.util.Locale;

import game.VERSION;
import init.paths.PATHS;
import launcher.GUI.BText;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.text.D;

class ScreenInfo extends GuiSection{
	
	private Str hoverInfo = new Str(200);
	private final Launcher l;
	
	ScreenInfo(Launcher l){
		D.gInit(this);
		this.l = l;
		
		
		{
			RENDEROBJ r = new GUI.Header(l.res, D.g("Version"));
			CLICKABLE c = new BText(l.res, VERSION.VERSION_STRING, 200).clickActionSet(new ACTION() {
				
				@Override
				public void exe() {
					l.setLog();
				}
			});
			
			add(r, c, 0);
		}
		
		{
			CharSequence[] keys = new CharSequence[] {
				D.g("Platform"),
				D.g("JRE"),
				D.g("GPU"),
				D.g("GPU-Driver"),
			};
			String[] values = new String[]{
				System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT),
				System.getProperty("java.version") + " bits:" + System.getProperty("sun.arch.data.model"),
				CORE.getGraphics().render(),
				CORE.getGraphics().renderV(),
			};
			
			for (int i = 0; i < keys.length; i++){
				RENDEROBJ r = new GUI.Header(l.res, keys[i]);
				add(r, new RENDEROBJ.Sprite(new Text(l.res.font, values[i]).setScale(1)), 2);
			}
		}
		
		
		COLOR clink = new ColorImp(20, 100, 100);
		COLOR clinkH = new ColorImp(20, 127, 127);
		
		{
			CharSequence[] keys = new CharSequence[] {
				D.g("localF", "Local Files"),
				D.g("Saves"),
				D.g("Screenshots"),
				D.g("Mods"),
			};
			String[] values = new String[]{
				""+PATHS.local().ROOT.get(),
				""+PATHS.local().save().get(),
				""+PATHS.local().SCREENSHOT.get(),
				""+PATHS.local().MODS.get(),
			};
			
			for (int i = 0; i < keys.length; i++){
				final String v = values[i];
				RENDEROBJ r =  new GUI.Header(l.res,keys[i]);
				CLICKABLE c = new ClickableAbs() {
					Text t = new Text(l.res.font, v).setScale(1);
					{
						body.setDim(t);
					}
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
						if (isHovered)
							clinkH.bind();
						else
							clink.bind();
						t.render(r, body);
						COLOR.unbind();
					}
					
					@Override
					protected void clickA() {
						FileManager.openDesctop(v);
					}
				};
				add(r, c, 2);
			}
		}

		
		
		
		{
			RENDEROBJ r;
			
			
			
			CLICKABLE c;
			
			r = new GUI.Header(l.res, D.g("Contact"));
			c = new ClickableAbs() {
				Text t = new Text(l.res.font, "info@songsofsyx.com").setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						clinkH.bind();
					else
						clink.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.sendEmail("info@songsofsyx.com", "Greetings, oh great dev", "Inquiry");
				}
			};
			add(r, c, 2);
			
			
			r = new GUI.Header(l.res, D.g("Road-map"));
			c = new ClickableAbs() {
				Text t = new Text(l.res.font, "https://trello.com/b/wF5RYqdF/songs-of-syx");
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						clinkH.bind();
					else
						clink.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					try {
						ScreenMain.openBrowser("https://trello.com/b/wF5RYqdF/songs-of-syx");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			add(r, c, 2);
		}
		
		RENDEROBJ b = new BText(l.res, D.g("Back")).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMain();
			}
		});
		b.body().moveX2(Sett.WIDTH - 40).moveY1(body().y1());
		add(b);
		
		
		body().moveX1Y1(8, 8);
		
	
		
	}

	private void add(RENDEROBJ title, RENDEROBJ oo, int dy) {
		title.body().moveY1(body().y2()+dy);
		title.body().moveX2(150);
		add(title);
		addRightC(10, oo);
	}
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		super.render(r, ds);
		if (hoverInfo.length() != 0) {
			GUI.c_label.bind();
			l.res.font.render(r, hoverInfo, 40, 315, 450, 1);
			hoverInfo.clear();
		}
		COLOR.unbind();
	};
	

	
	
}
