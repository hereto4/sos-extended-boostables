package menu;

import java.nio.file.Path;
import java.util.HashSet;

import cutscene.CutScene;
import game.GameSpec;
import game.VERSION;
import game.save.GameLoader;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.GUI.Shadower;
import script.ScriptEngine;
import script.ScriptLoad;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.data.GETTER;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GTextScroller;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import view.menu.MenuScreen;

class ScCampaign extends Shadower implements SC{

	
	private Campaign current;
	
	static CharSequence ¤¤name = "¤campaigns";
	private static CharSequence ¤¤go = "go!";
	static {
		D.ts(ScCampaign.class);
	}
	
	private final Menu menu;
	private final HashSet<String> completed = PATHS.local().campaignsUnlocked();
	
	
	ScCampaign(Menu menu){

		this.menu = menu;
		
		MenuScreen screen = new MenuScreen(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		
		CLICKABLE b = new MenuScreen.ScreenButton(¤¤go) {
			@Override
			protected void clickA() {
				start();
					
			}
			
			@Override
			protected void renAction() {
				activeSet(canStart());
			}
		};
		
		screen.addButt(b);
		
		GuiSection s = new GuiSection();
		
		KeyMap<Campaign> cmap = new KeyMap<Campaign>();
		
		for (String f : PATHS.MISC().CAMPAIGNS.getFiles()) {
			
			Campaign c = new Campaign(new Json(PATHS.MISC().CAMPAIGNS.get(f)), f);
			cmap.put(f, c);
		}
		
		{
			
			
			
			
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			for (Campaign c : cmap.allSorted()) {

				SPRITE sp = new SPRITE.Imp(400, UI.FONT().H2.height()*2+8) {
					Text t = UI.FONT().H2.getText(17);
					{
						t.setMaxWidth(368);
						t.setMultipleLines(true);
						t.add(c.info.name);
					}
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						
						t.renderCY(r, X1+32, Y1+(Y2-Y1)/2);
						
						COLOR col = COLOR.GREEN100;
						if (c.locked())
							col = COLOR.WHITE50;
						else if (!completed.contains(c.key))
							col = COLOR.BLUEISH;
						col.bind();
						UI.icons().s.dot.big.renderCY(r, X1, Y1+(Y2-Y1)/2);
					}
				};
				
				rows.add(new GUI.Button(sp) {
					
					{
						body.incrW(24);
					}
					
					@Override
					protected void renAction() {
						selectedSet(current == c);
					}
					
					@Override
					protected void clickA() {
						current = c;
						if (MButt.LEFT.isDouble()) {
							start();
						}
					}
					
					
					
				});
				
				
			}
			
			s.add(new GScrollRows(rows, 400).view());
		}
		
		{
			GuiSection ss = new GuiSection();
			ss.add(new GStat(UI.FONT().H2) {
				
				@Override
				public void update(GText text) {
					if (current != null) {
						text.color(COLORS.unclickable);
						text.add(current.info.name);
					}
				}
			}.r(DIR.N));
			
			GETTER<CharSequence> g = new GETTER<CharSequence>() {

				@Override
				public CharSequence get() {
					if (current == null)
						return Dic.empty;
					else {
						Str.TMP.clear();
						Str.TMP.add(current.info.desc);
						return Str.TMP;
					}
				}
				
			};
			
			GTextScroller sc = new GTextScroller(UI.FONT().M, g, 400, 300);
			
			ss.addRelBody(8, DIR.S, sc);
			
			SPRITE sp = new SPRITE.Imp(400, UI.FONT().H2.height()*3+8) {
				Text t = UI.FONT().M.getText(17);
				{
					t.setMaxWidth(400);
					t.setMultipleLines(true);
					
				}
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					if (current == null)
						return;
					
					COLOR col = COLOR.GREEN100;
					if (current.locked())
						col = COLOR.REDISH;
					else if (!completed.contains(current.key))
						col = COLOR.BLUEISH;
					col.bind();
					
					t.clear();
					if (current.requires.length > 0) {
						t.add(Dic.¤¤Requires).add(':');
						t.s();
						for (String s : current.requires) {
							if (cmap.containsKey(s))
								t.add(cmap.get(s).info.name);
							else {
								t.add('?').s().add(s);
							}
						}
					}
					
					t.renderCY(r, X1, Y1+(Y2-Y1)/2);
					
					
					
				}
			};
			
			ss.addRelBody(8, DIR.S, sp);
			
			s.addRelBody(64, DIR.E, ss);
			
		}
		

		s.body().centerIn(body());
		
		add(s);
		
	}
	
	private void start() {
		if (canStart()) {
			GameLoader loader = new GameLoader(current.save, current.scripts);
			
			
			menu.start(CutScene.make(current.cutsceneData, current.cutsceneText, loader));
		}
	}
	
	private boolean canStart() {
		return current != null && !current.locked();
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
	private class Campaign {
		
		public final Json cutsceneData;
		public final Json cutsceneText;
		public final INFO info;
		public String[] requires;
		public final Path save;
		public final String[] scripts;
		public final String key;
		
		public Campaign(Json json, String key) {
			this.key = key;
			Json text = new Json(PATHS.TEXT().getFolder("campaign").get(key));
			info = new INFO(text);
			cutsceneText = text.json("CUTSCENE");
			cutsceneData = json.json("CUTSCENE");
			requires = json.values("REQUIRES");
			if (json.bool("SAVE_LOCAL")) {
				Path s = PATHS.local().SAVE_CAMPAIGN.exists(key) ? PATHS.local().SAVE_CAMPAIGN.get(key) : null;
				if (s != null) {
					GameSpec f = GameSpec.get(s);
					if (VERSION.versionMajor(f.version) != VERSION.VERSION_MAJOR)
						s = null;
				}
				save = s;
			}
			else
				save = PATHS.MISC().SAVES_CAMPAIGN.get(key);
			
			String[] ss = json.values("SCRIPTS");
			ArrayListGrower<ScriptLoad> scripts = new ArrayListGrower<>();
			for (int i = 0; i < ss.length; i++) {
				if (!PATHS.SCRIPT().jar.exists(ss[i])) {
					json.error(PATHS.SCRIPT().jar.get().toAbsolutePath() + " /" + ss[i] + " does not exist", "SCRIPTS");
				}else {
					scripts.add(ScriptEngine.getInJar(ss[i]));
					
				}
				
			}
			this.scripts = new String[scripts.size()];
			int ii = 0;
			for (ScriptLoad l : scripts)
				this.scripts[ii++] = l.key;
			
		}
		
		boolean locked() {
			if (save == null)
				return true;
			for (String s : requires)
				if (!completed.contains(s))
					return true;
			return false;
		}
		
	}
	


	
}
