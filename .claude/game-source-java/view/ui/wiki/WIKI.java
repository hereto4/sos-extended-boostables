package view.ui.wiki;



import game.GAME;
import game.GameDisposable;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.text.D;
import view.interrupter.Interrupter;
import view.main.VIEW;

public class WIKI extends Interrupter{
	
	public final static int TOP_HEIGHT = 0;
	public static final int WIDTH = C.WIDTH()-32;
	public static final int HEIGHT = C.HEIGHT()-16;
	
	private final GuiSection section = new GuiSection();
	public static CharSequence ¤¤name = "Tome of Knowledge";
	
	private static final ArrayList<Article> articles = new ArrayList<Article>(1024);
	private ArrayList<Article> added;
	private GuiSection sAdded = new GuiSection();
	private final Article[] race = new Article[RACES.all().size()];
	private final WikiList list;
	
	final static KeyMap<Article> links = new KeyMap<Article>();
	
	static {
		D.ts(WIKI.class);
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				articles.clear();
				links.clear();
			}
		};
	}
	
	public static ACTION add(Json json) {
		if (!json.has("WIKI"))
			return null;
		json = json.json("WIKI");
		return padd(json);
	}
	
	public static ACTION padd(Json json) {
		ArticleText a = new ArticleText(json, links);
		articles.add(a);
		return new ACTION() {
			@Override
			public void exe() {
				VIEW.UI().wiki.activate();
				VIEW.UI().wiki.set(a);
			}
		};
	}
	
	public WIKI(){
		//super(¤¤name, UI.icons().l.book);
		new Colors();
		
		{
			PATH p = PATHS.TEXT().getFolder("wiki");
			String[] files = p.getFiles();
			for (String f : files) {
				Json j = new Json(p.get(f));
				if (j.has("WIKI")) {
					add(j);
				}
				if (j.has("WIKIS")) {
					
					Json[] json = j.jsons("WIKIS");
					for (Json jj : json) {
						padd(jj);
					}
				}
				
			}
		}
		
		for (Race r : RACES.all()) {
			race[r.index()] = new WikiRace(r);
			articles.add(race[r.index()]);
		}
		
		Tree<Article> sort = new Tree<Article>(articles.size()) {
			@Override
			protected boolean isGreaterThan(Article ce, Article c2) {
				return smaller(ce.key, c2.key);
			}
			
			boolean smaller(CharSequence current, CharSequence cmp) {
				for (int i = 0; i < current.length(); i++) {
					if (i >= cmp.length())
						return false;
					if ((int)current.charAt(i) > (int)cmp.charAt(i))
						return false;	
					if ((int)current.charAt(i) < (int)cmp.charAt(i))
						return true;			
				}
				return false;
			}
		};
		
		for (Article a : articles)
			sort.add(a);
		
		articles.clearSloppy();
		while (sort.hasMore())
			articles.add(sort.pollGreatest());
		
		list = new WikiList(articles, HEIGHT);
		
		section.add(list, 0, TOP_HEIGHT);
		int x1 = section.getLastX2();
		section.add(sAdded, section.getLastX2(), TOP_HEIGHT);

		int width = 600;
		int am = (WIDTH-x1)/600;
		int ex = WIDTH-x1 - am*width;
		ex /= am;
		width += ex;
		
		added = new ArrayList<Article>(am);
		
		for (int i = 0; i < articles.size(); i++) {
			Article a = articles.get(i);
			a.init(articles, width);
		}
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo); 
		return true;
	}
 
	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.RIGHT) {
			if (!back())
				hide();
		}else if(button == MButt.LEFT) {
			section.click();
		}
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean update(float ds) {
		GAME.SPEED.tmpPause();
		return false;
	}
	
	@Override
	protected boolean render(Renderer r, float ds) {
		
		GCOLOR.UI().bg().render(r, C.DIM());
		section.render(r, ds);
		return false;
	}
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		super.hide();
	}
	


	private boolean back() {
		if(added.size() > 0) {
			remove(added.get(added.size()-1));
			return true;
		}else
			return false;
	}
	
	public void activate() {
		added.clear();
		adjust();
		super.show(VIEW.inters().manager);
	}
	
	void remove(Article a) {
		
		added.removeOrdered(a);
		
		adjust();
	}
	
	void set(Article a) {
		
		if (!added.isEmpty() && a == added.get(0))
			return;
		
		if (added.contains(a))
			added.remove(a);
		
		if (!added.hasRoom())
			added.removeLast();
		
		added.insert(0, a);
		adjust();
		
	}
	
	private void adjust() {
		int x = sAdded.body().x1();
		int y = sAdded.body().y1();
		sAdded.clear();
		
		for (Article aa : added) {
			sAdded.addRightC(0, aa.section);
		}
		sAdded.body().moveX1Y1(x, y);
	}

	
	public void showRace(Race r) {
		activate();
		list.setList(race[r.index]);
		set(race[r.index]);
	}
	
	public LIST<Article> added(){
		return added;
	}
	
}
