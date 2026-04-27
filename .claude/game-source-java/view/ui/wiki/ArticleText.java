package view.ui.wiki;

import game.GAME;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;
import view.main.VIEW;

class ArticleText extends Article{
	
	private final CharSequence text;
	private final ArrayListGrower<Special> links = new ArrayListGrower<>();
	
	ArticleText(Json json, KeyMap<Article> amap){
		super(json.text("NAME"), json.text("CATEGORY"));
		if (json.has("LINK_KEY")) {
			String k = json.value("LINK_KEY");
			if (amap.containsKey(k))
				json.error("this link key already exists", k);
			amap.put(k, this);
		}
		String t = json.text("TEXT");
		
		StringBuilder bu = new StringBuilder();

		for (int ci = 0; ci < t.length(); ci++) {
			
			
			
			if (t.charAt(ci) == '<') {
				int ei = ci;
				ci++;
				String error = "either <a LINK_KEY TEXT> (wiki link), <c RRR_GGG_BBB TEXT> (colored text) or <u TEXT> (underscore)";
				
				if (ci >= t.length()) {
					err(json, t, error, ei);
					continue;
				}
				
				if (t.charAt(ci) == 'a') {
					error = "Expecting: <a LINK_KEY TEXT> where LINK_KEY is another wiki entry's link, and TEXT is the text for the link.";
					ci = next(t, ci, ' ') + 1;
					if (ci < 0) {
						err(json, t, error, ei);
						continue;
					}
					int ni = next(t, ci, ' ');
					if (ni < 0) {
						err(json, t, error, ei);
						continue;
					}
					String key = t.substring(ci, ni);
					ci = ni+1;
					ni = next(t, ci, '>');
					if (ni < 0) {
						err(json, t, error, ei);
						continue;
					}
					String content = t.substring(ci, ni);
					int pos = bu.length();
					bu.append(content);
					links.add(new LinkButt(pos, bu, content, key));
					ci = ni;
				}else if(t.charAt(ci) == 'c') {
					error = "Expecting: <c RRR_GGG_BBB TEXT> where RRR_GGG_BBB is a color, and TEXT is the text for the link.";
					ci = next(t, ci, ' ') + 1;
					if (ci < 0) {
						err(json, t, error, ei);
						continue;
					}
					int ni = next(t, ci, ' ');
					if (ni < 0) {
						err(json, t, error, ei);
						continue;
					}
					String key = t.substring(ci, ni);
					COLOR col = COLOR.WHITE100;
					try {
						col = new ColorImp().set(key, json);
					}catch(Throwable e) {
						err(json, t, error, ei);
						e.printStackTrace();
						continue;
					}
					
					ci = ni+1;
					ni = next(t, ci, '>');
					if (ni < 0) {
						err(json, t, error, ei);
						continue;
					}
					String content = t.substring(ci, ni);
					int pos = bu.length();
					bu.append(content);
					links.add(new ColButt(pos, bu, content, col));
					ci = ni;
				}else {
					err(json, t, error, ei);
					continue;
				}
				
			}else {
				bu.append(t.charAt(ci));
			}
			
		}
		
		
		text = bu.toString();
		
	}
	
	private int next(String t, int ci, char c) {
		for (; ci < t.length(); ci++) {
			if (t.charAt(ci) == c)
				return ci;
		}
		return -10;
	}
	
	private void err(Json json, String t, String error, int ci) {
		GAME.Warn(json.errorGet("TEXT", "Error in wiki entry. " + error +  " Around: '" + t.substring(Math.max(ci-8, 0), Math.min(ci+32, t.length()-1)) + "'"));
	}
	
	@Override
	GuiSection makeSection(LIST<Article> all, int width) {
		return new WikiArticle(this, width, all);
	}


	
	final static class WikiArticle extends GuiSection{

		private static Special hovered;
		
		private int row;
		private final int max;
		private final LIST<Special> linkButts;
		private final ArticleText e;
		
		WikiArticle(ArticleText e, int width, LIST<Article> all){
			this.e = e;
			Font f = UI.FONT().M;
			
			body().setWidth(width);
			
			int maxRows = (HEIGHT)/f.height();
			int rows = UI.FONT().M.getRowAmount(e.text, width-32);
			
			int m = rows-maxRows;
			
			if (m < 0)
				m = 0;
			max = m;
			
			
			INTE tar = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return max;
				}
				
				@Override
				public int get() {
					return row;
				}
				
				@Override
				public void set(int t) {
					row = t;
				}
			};
			GSliderVer sl = new GSliderVer(tar, HEIGHT);
			add(sl, body().x2()-sl.body().width(),0);
			
			body().moveX1Y1(0,0);
			
			linkButts = e.links;

			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;

			
			f.renderFromRow(e.text, x, y, body().width()-32, row, body().height()-8);
			if (hoveredIs()) {
				double m = MButt.clearWheelSpin();
				if (m < 0) {
					row += Math.ceil(-m); 
				}else if(m > 0) {
					row -= Math.ceil(m);
				}
				row = CLAMP.i(row, 0, max);
			}
			super.render(r, ds);
			for (Special b : linkButts) {
				
				
				COORDINATE c = f.getStartPosition(e.text, 0, b.position, b.text.length(), body().width()-32, 1.0);
				int offY = c.y();
				int offX = c.x();
				
				int startI = 0;
				int y1 = y -row*f.height() + offY;
				
				while(true) {
					
					int end = f.getEndIndex(b.text, startI, body().width()-offX-32);
					if (y1 >= y && y1 + f.height() <= body().y2()) {
						int x1 = x + offX;
						b.render(r, hovered == b, Str.TMP.clear().add(b.text, startI, end), x1, y1);
					}
					y1 += f.height();
					startI = Math.max(startI, f.getStartIndex(b.text, end));
					offX = 0;
					if (startI >= b.text.length())
						break;
				}
				

				
			}
			hovered = null;
			COLOR.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			super.hover(mCoo);
			hovered = null;
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;
			for (Special b : linkButts) {
				
				
				COORDINATE c = f.getStartPosition(e.text, 0, b.position, b.text.length(), body().width()-32, 1.0);
				int offY = c.y();
				int offX = c.x();
				
				int startI = 0;
				int y1 = y -row*f.height() + offY;
				
				while(true) {
					
					int end = f.getEndIndex(b.text, startI, body().width()-offX-32);
					if (y1 >= y && y1 + f.height() <= body().y2()) {
						int x1 = x + offX;
						int x2 = x1 + f.width(b.text, startI, end, 1.0);
						if (mCoo.x() >= x1 && mCoo.x() <= x2)
							if (mCoo.y() >= y1 && mCoo.y() <= y1+f.height()) {
								hovered = b;
								return true;
							}
					}
					y1 += f.height();
					startI = Math.max(startI+1, f.getStartIndex(b.text, end));
					offX = 0;
					if (startI >= b.text.length())
						break;
				}
				
			}
			
			return hoveredIs();
		}
		
		@Override
		public boolean click() {
			hovered = null;
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;
			COORDINATE mCoo = VIEW.mouse();
			for (Special b : linkButts) {
				
				COORDINATE c = f.getStartPosition(e.text, 0, b.position, b.text.length(), body().width()-32, 1.0);
				int offY = c.y();
				int offX = c.x();
				
				int startI = 0;
				int y1 = y -row*f.height() + offY;
				
				while(true) {
					
					int end = f.getEndIndex(b.text, startI, body().width()-offX-32);
					if (y1 >= y && y1 + f.height() <= body().y2()) {
						int x1 = x + offX;
						int x2 = x1 + f.width(b.text, startI, end, 1.0);
						if (mCoo.x() >= x1 && mCoo.x() <= x2)
							if (mCoo.y() >= y1 && mCoo.y() <= y1+f.height()) {
								b.click();
								return true;
							}
					}
					y1 += f.height();
					startI = Math.max(startI+1, f.getStartIndex(b.text, end));
					offX = 0;
					if (startI >= b.text.length())
						break;
				}
				
				
				
			}
			return super.click();
		}
		

		
	}
	
	private static abstract class Special {
		
		public final int position;
		public final CharSequence text;
		public abstract void render(SPRITE_RENDERER r, boolean hovered, CharSequence text, int x1, int y1);
		
		public abstract void click();
		
		Special(int position, CharSequence all, CharSequence text){
			this.text = text;
			this.position = position;
		}
		
	}

	
	private static class LinkButt extends Special{
		
		private final String key;
		
		LinkButt(int position, CharSequence all, CharSequence text, String key){
			super(position, all, text);
			this.key = key;
		}

		@Override
		public void render(SPRITE_RENDERER r, boolean hovered, CharSequence text, int x1, int y1) {
			
			Article entry = WIKI.links.get(key);
			
			if (hovered)
				COLOR.WHITE100.bind();
			else if (entry == null)
				GCOLOR.T().IBAD.bind();
			else
				GCOLOR.T().IGOOD.bind();
			UI.FONT().M.render(r, text, x1, y1);
			
		}

		@Override
		public void click() {
			Article entry = WIKI.links.get(key);
			if (entry != null) {
				VIEW.UI().wiki.set(entry);
			}
		}
		
		
	}
	
	private static class ColButt extends Special{
		
		private final COLOR col;
		
		ColButt(int position, CharSequence all, CharSequence text, COLOR color){
			super(position, all, text);
			this.col = color;
		}

		@Override
		public void render(SPRITE_RENDERER r, boolean hovered, CharSequence text, int x1, int y1) {
			
			col.bind();
			UI.FONT().M.render(r, text, x1, y1);
			
		}

		@Override
		public void click() {
			
		}
		
		
	}


	
}
