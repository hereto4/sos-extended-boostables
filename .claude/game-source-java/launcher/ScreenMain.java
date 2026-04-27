package launcher;


import java.io.IOException;

import launcher.GUI.BSpriteBig;
import launcher.GUI.BText;
import snake2d.CORE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.text.D;

class ScreenMain extends GuiSection{
	
	ScreenMain(Launcher l, ScreenLang lang){
		
		add(l.res.smallPanel[0], 0, 0);
		for (int i = 0; i <= 5; i++)
			addDown(0, l.res.smallPanel[1]);
		addDown(0, l.res.smallPanel[2]);
		
		//BUTTONS
		
		GuiSection buttons = new GuiSection();
		
		CLICKABLE b;
		
		int ww = 150;
		D.gInit(this);
		
		b = new BText(l.res, D.g("Launch"), ww);
		b.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMods();
			}
		});
		buttons.add(b);
		
		
		b = new BText(l.res, D.g("Settings"), ww).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setSetts();
			}
		});
		buttons.addRightC(2, b);
		
		b = new BText(l.res, D.g("Info"), ww).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				l.setInfo();
			}
		});
		buttons.addRightC(2, b);
		
		b = new BText(l.res, D.g("Exit"), ww).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				Launcher.startGame = false;
				CORE.annihilate();
			}
		});
		buttons.addRightC(2, b);
		
		b = new BSpriteBig(l.res.social[3]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://songsofsyx.mod.io/");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(2, b);
		b = new BSpriteBig(l.res.social[2]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://discord.gg/eacfCuE");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(2, b);
		b = new BSpriteBig(l.res.social[1]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://twitter.com/songsofsyx");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(2, b);
		b = new BSpriteBig(l.res.social[0]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://www.youtube.com/channel/UCuWzoe8gnqI1brHv-k3oVyA");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(2, b);
		
		body().centerY(0, Sett.HEIGHT);
		body().centerX(0, Sett.WIDTH);
		
		add(l.res.logo, (Sett.WIDTH-l.res.logo.width())/2, body().y1()+60);
		
		buttons.body().centerX(0, Sett.WIDTH);
		buttons.body().moveY1(getLastY2()+10);
		

		
		add(buttons);
		
		add(lang.butt(), 820, 160);
		
//		b = new GUI.Button.Sprite(Resources.Sprites.kickstarter) {
//			@Override
//			protected void clickA() {
//				
//			};
//		};
//		
//		b.body().moveX2(Sett.WIDTH-20);
//		b.body().moveY1(20);
//		add(b);
		

	}
	
	public static void openBrowser(String url) throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			Runtime rt = Runtime.getRuntime();
			rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
		}else if(os.indexOf("mac") >= 0) {
			Runtime rt = Runtime.getRuntime();
			rt.exec("open " + url);
		}else if(os.indexOf("nix") >=0 || os.indexOf("nux") >=0) {
			Runtime rt = Runtime.getRuntime();
			String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
			                                 "netscape", "opera", "links", "lynx" };

			StringBuffer cmd = new StringBuffer();
			for (int i = 0; i < browsers.length; i++)
			    if(i == 0)
			        cmd.append(String.format(    "%s \"%s\"", browsers[i], url));
			    else
			        cmd.append(String.format(" || %s \"%s\"", browsers[i], url)); 

			rt.exec(new String[] { "sh", "-c", cmd.toString() });
		}
	}
	

}
