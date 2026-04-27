
package launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import init.paths.PATHS;
import launcher.GUI.BText;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Font;
import util.text.D;

class ScreenLog extends GuiSection{

	private final Lines lines;
	
	ScreenLog(Launcher l){
	
		D.gInit(this);
		add(new GUI.Header(l.res, D.g("Change-Log")), 20, 16);
		
		CLICKABLE b = new BText(l.res, "BACK").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMain();
			}
		});
		b.body().moveX2(Sett.WIDTH-20).moveCY(getLast().cY());
		add(b);
		
		lines = new Lines(l.res, Sett.HEIGHT-body().y2()-16);
		lines.body().moveX1Y1(10, body().y2()+8);
		add(lines);
		
		
		CLICKABLE up = new GUI.BSprite(l.res.arrowUpDown[0]).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				lines.top -=5;
			}
		});
		up.body().moveY1(60+25).moveX2(Sett.WIDTH-10);
		add(up);
		
		CLICKABLE down = new GUI.BSprite(l.res.arrowUpDown[1]).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				lines.top +=5;
			}
		});
		down.body().moveY2(Sett.HEIGHT-60).moveX2(Sett.WIDTH-10);
		add(down);
		
		
		
		
		
		//body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		
		
	}


	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();

		
		super.render(r, ds);
		
	};

	
	private static class Lines extends RenderImp {
		
		private CharSequence[] lines = lines();
		int top = 0;
		private final String sep = "-";
		private final COLOR[] cols = new COLOR[] {
			COLOR.WHITE100,
			new ColorImp(127, 110, 100),
		};

		private final Font font;
		
		Lines(RES res, int height){
			font = res.font;
			body().setWidth(Sett.WIDTH-100).setHeight(height);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int i = (int) MButt.clearWheelSpin();
			top -= i;
			
			if (top >= lines.length-1)
				top = lines.length-1;
			if (top < 0)
				top = 0;
			
			int y1 = body().y1();
			int line = top;
			while(line < lines.length && y1 < body().y2()) {
				y1 = render(y1, lines[line], r, cols[line&1]);
				line++;
			}
			
			
		}
		
		private int render(int y1, CharSequence s, SPRITE_RENDERER r, COLOR color) {
			int start = 0;
			color.bind();
			while(true) {
				int end = font.getEndIndex(s, start, body().width());
				int y2 = y1 + font.height();
				if (y2 < body().y2()) {
					if (start == 0) {
						if (s.length() > 0 && s.charAt(0) == '!') {
							start = 1;
							COLOR.BLUEISH.bind();
						}else if (s.length() > 0){
							font.render(r, sep, body().x1(), y1);
						}
					}
					
					font.render(r, s, body().x1()+20, y1, start, end, 1);
					
				}
				y1 = y2;
				start = end;
				if (start == s.length())
					break;
			}
			COLOR.unbind();
			return y1+2;
		}
		
		private CharSequence[] lines() {
			try {
				List<String> ss = Files.readAllLines(PATHS.BASE().TXT.get("Patchnotes"));
				CharSequence[] res = new CharSequence[ss.size()];
				for (int i = 0; i < ss.size(); i++)
					res[i] = ss.get(i);
				return res;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new CharSequence[] {
				"error"
			};
		}
		
	}
	
	
	
	
}
