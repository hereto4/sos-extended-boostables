package game.faction.player;

import java.io.IOException;

import game.GAME;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.value.GVALUES;
import init.value.Lockable;
import init.value.Lockers;
import integrations.SteamAchieve;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.process.Proccesser;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.data.DOUBLE_O;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.info.INFO;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.text.D;
import view.interrupter.IDebugPanel;
import view.ui.message.MessageSection;
import world.map.regions.Region;

public final class PTitles {

	private final LIST<PTitle> titles;
	private int newAmount = 0;
	public final INFO info;
	public final BoostSpecs boosters;
	private final BoostCompound<PTitle> bos;
	private static CharSequence ¤¤name = "Titles";
	private static CharSequence ¤¤desc = "Titles are unlocked by various achievements. At the start of each game, you may choose 5 of these unlocked titles to be associated with your name and boost your kingdom in various ways.";
	private static CharSequence ¤¤title = "Title Unlocked";
	private static CharSequence ¤¤titleD = "Congratulations, you've now earned the right to style yourself '{0}'!.";
	private static CharSequence ¤¤titleDD = "In your next play-through, or resettle, you will be allowed to select this title to boost your game.";
	
	public static CharSequence ¤¤racesUnlocked = "Races unlocked";
	public static CharSequence ¤¤racesUnlockedD = "Each race you unlock this title with will make its boosts more potent by an additional +{0}%.";
	public static CharSequence ¤¤currentBoost = "Current Boost";
	static {
		D.ts(PTitles.class);
	}
	
	
	PTitles() throws IOException{
		
		
		info = new INFO(¤¤name, ¤¤desc);
		
		PATH data = PATHS.INIT().getFolder("player").getFolder("titles");
		PATH text = PATHS.TEXT().getFolder("player").getFolder("titles");
		IconMaker mm = new IconMaker();
		String[] ss = data.getFiles();
		ArrayList<PTitle> all = new ArrayList<>(ss.length);
		for (String s : ss) {
			Json j = new Json(data.get(s));
			Json t = new Json(text.get(s));
			new PTitle(s, all, j, t, mm);
		}
		this.titles = all;
		
		{
			KeyMap<PTitle> map = new KeyMap<>();
			for (PTitle t : all) {
				map.put(t.key, t);
			}
			try {
				if (PATHS.local().PROFILE.exists("Titles")) {
					Json j = new Json(PATHS.local().PROFILE.get("Titles"));
					if (j.has("UNLOCKED")) {
						String[] sss = j.values("UNLOCKED");
						for (String s : sss) {
							if (map.containsKey(s)) {
								map.get(s).unlocked = true;
							}
								
						}
					}
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				GAME.Notify("old one didn't load");
			}
			
			
			
			try {
				Json j = new Json(PATHS.local().PROFILE.get("Titles2"));
				
				for (String k : j.keys()) {
					if (map.containsKey(k)) {
						PTitle t = map.get(k);
						String[] rr = j.values(k);
						t.unlocked = true;
						for (String r : rr) {
							Race race = RACES.map().tryGet(r);
							if (race != null) {
								t.races[race.index] = true;
								t.raceValue++;
							}
								
						}
					}
				}
				
			}catch(Exception e) {
				GAME.Notify("resetting");
				saveUnlocked();
			}
		}
		
		boosters = new BoostSpecs(¤¤name, UI.icons().s.chevron(DIR.N), true);
		bos = new BoostCompound<PTitle>(boosters, titles) {

			@Override
			protected double getValue(PTitle t) {
				return t.selected ? (0.5+0.5*t.raceValue/RACES.playable().size()) : 0;
			}
			
			@Override
			protected BoostSpecs bos(PTitle t) {
				return t.boosters;
			}
			
			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return 0;
				//return npc*super.get(bo, f, isMul);
			}
		};
		
		IDebugPanel.add("STEAM ACHIEVE", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles2")) {
					achieve();
				}
			}
		});
		IDebugPanel.add("STEAM ACHIEVE_ALL", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles2")) {
					String[] ss = new String[all().size()];
					for (int i = 0; i< ss.length; i++) {
						ss[i] = all().get(i).key;
					}
					Proccesser.exec(SteamAchieve.class, new String[] {}, ss, new String[] {});
				}
			}
		});
		IDebugPanel.add("STEAM ACHIEVE_CLEAR", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles2")) {
					Proccesser.exec(SteamAchieve.class, new String[] {}, new String[] {}, new String[] {});
				}
			}
		});
		
	}

	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(titles.size());
			for (PTitle t : titles) {
				file.bool(t.isNew);
				file.bool(t.selected);
			}
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			
			for (PTitle t : titles) {
				t.isNew = false;
				t.selected = false;
			}
			
			int am = file.i();
			for (int i = 0; i < am; i++) {
				boolean n = file.bool();
				boolean s = file.bool();
				if (i < titles.size()) {
					PTitle t = titles.get(i);
					t.isNew = n;
					t.selected = s;
				}
				
				
			}
			bos.clearChache();
		}
		
		@Override
		public void clear() {
			for (PTitle t : titles) {
				t.isNew = false;
				t.selected = false;
			}
			bos.clearChache();
		}
	};
	
	double ddd = 0;
	
	void update(double ds) {
		if (!GAME.achieving())
			return;
		
		int o = (int) ddd;
		ddd += ds;
		int n = (int) ddd;
		if (o == n)
			return;
		if (n >= titles.size()) {
			ddd = 0;
			n = 0;
		}
		
		PTitle t = titles.get(n);
		if ((!t.unlocked || !t.race(FACTIONS.player().race())) && t.unlockable()) {
			t.timer += titles.size();
			if (t.timer > TIME.secondsPerDay()*0.25) {
				unlock(t);
			}
		}else {
			
			t.timer = 0;
		}
	}

	
	public void unlock(PTitle t) {
		t.isNew = true;
		t.unlocked = true;
		
		t.races[FACTIONS.player().race().index] = true;
		t.raceValue++;
		new Message(t).send();
		saveUnlocked();
	}
	
	public int selected() {
		int am = 0;
		for (PTitle t : titles) {
			if (t.selected)
				am++;
		}
		return am;
	}
	
	public int unlocked() {
		int am = 0;
		for (PTitle t : titles) {
			if (t.unlocked)
				am++;
		}
		return am;
	}
	
	public boolean hasNew() {
		return newAmount > 0;
	}
	
	private void saveUnlocked() {
		try {
			
			
			KeyMap<KeyMap<String>> all = new KeyMap<KeyMap<String>>();
			for (PTitle t : titles) {
				if (t.unlocked) {
					KeyMap<String> ss = new KeyMap<String>();
					for (Race r : RACES.all()) {
						if (t.race(r))
							ss.put(r.key, r.key);
					}
					all.put(t.key, ss);
				}
				
			}
			
			if (PATHS.local().PROFILE.exists("Titles2")) {
				Json old = new Json(PATHS.local().PROFILE.get("Titles2"));
				
				for (String s : old.keys()) {
					if (!all.containsKey(s)) {
						all.put(s, new KeyMap<String>());
					}
					for (String r : old.values(s)) {
						all.get(s).putReplace(r, s);
					}
					
				}
			}else {
				PATHS.local().PROFILE.create("Titles2");
			}
			
			JsonE to = new JsonE();
			
			for (String tkey : all.keysSorted()) {
				KeyMap<String> race = all.get(tkey);
				to.add(tkey, race.keysSorted());
				
			}
			to.save(PATHS.local().PROFILE.get("Titles2"));
		}catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static void achieve() {
		
		if (PATHS.isSteam() && PATHS.local().PROFILE.exists("Titles2")) {
			Json old = new Json(PATHS.local().PROFILE.get("Titles2"));
			String[] sss = new String[old.keys().size()];
			int i = 0;
			for (String k : old.keys()) {
				sss[i++] = k;
			}
			Proccesser.exec(SteamAchieve.class, new String[] {}, sss, new String[] {});
			
			
		}
	}
	
	
	public LIST<PTitle> all(){
		return titles;
	}
	
	public static final class PTitle extends INFO implements INDEXED{

		private double timer = 0;
		private final int index;
		public final Lockers lockers;
		public final Lockable<Faction> lockable;
		public final BoostSpecs boosters;
		public final SPRITE icon;
		
		private final String key;
		private boolean selected;
		private boolean isNew;
		private boolean unlocked;
		private boolean[] races = new boolean[RACES.all().size()];
		private double raceValue = 0;
		
		PTitle(String key, LISTE<PTitle> all, Json jdata, Json jtext, IconMaker iconM) throws IOException{
			super(jtext);
			this.key = key;
			index = all.add(this);
			
			lockable = GVALUES.FACTION.LOCK.push();
			lockable.push(jdata);
			icon = iconM.get(jdata);
			lockers = new Lockers(¤¤name + ": " + name, UI.icons().s.chevron(DIR.N));
			lockers.add(GVALUES.FACTION, jdata, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					if (t == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.INDU, jdata, new DOUBLE_O<Induvidual>() {

				@Override
				public double getD(Induvidual t) {
					if (t.faction() == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.REGION, jdata, new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					if (t.faction() == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			boosters = new BoostSpecs(name, UI.icons().s.chevron(DIR.N), false);
			boosters.read(jdata, null);
		}

		@Override
		public int index() {
			return index;
		}
		
		public void select(boolean s) {
			if (s == selected)
				return;
			selected = s;
			FACTIONS.player().titles.bos.clearChache();
		}
		
		public boolean selected() {
			return selected;
		}
		
		private boolean unlockable() {
			return lockable.passes(FACTIONS.player());
		}
		
		public boolean unlocked() {
			return unlocked || unlockable();
		}
		
		public boolean isNew() {
			return isNew;
		}
		
		public void consumeNew() {
			isNew = false;
		}

		public boolean race(Race r) {
			return races[r.index()];
		}

		public double boosterValue() {
			if (unlocked || selected)
				return 0.5 + 0.5*raceValue/RACES.playable().size();
			return 0;
		}
		
	}
	
	private static class IconMaker {
		
		private final int WW = 5;
		private int txs = 6;
		private int tys = 4;
		
		IconMaker() throws IOException{
			new ComposerThings.IInit(PATHS.SPRITE().getFolder("ui").get("Titles"), 540, 190) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
					int hi = c.getSource().height/(tys*8+6);
					s.full2.init(0, 0, WW, hi, txs, tys, d.s8);
				}
				
			};
		}
		
		public SPRITE get(Json json) throws IOException {
			
			int ii = json.i("ICON_I");
			
			TILE_SHEET s = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full2.setVar(ii);
					s.full2.paste(true);
					return d.s8.saveGui();
				}
			}.get();
			
			return new SPRITE.Imp(txs*8, tys*8) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					int w = (X2-X1)/txs;
					int h = (Y2-Y1)/tys;
					int y = Y1;
					int i = 0;
					for (int dy = 0; dy < tys; dy++) {
						int x = X1;
						for (int dx = 0; dx < txs; dx++) {
							s.render(r, i, x, x+w, y, y+h);
							x += w;
							i++;
							
						}
						y+= h;
					}
					
				}
			};
		}
		
	}
	
	private static class Message extends MessageSection{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String tkey;
		
		public Message(PTitle t) {
			super(¤¤title);
			tkey = t.key;
		}

		@Override
		protected void make(GuiSection section) {
			PTitle t = get();
			if (t == null)
				return;
			
			paragraph(t.desc);
			
			GText tt = new GText(UI.FONT().S, ¤¤titleD);
			tt.normalify2();
			tt.insert(0, t.name);
			tt.setMaxWidth(WIDTH);
			tt.setMultipleLines(true);
			tt.adjustWidth();
			section.addRelBody(8, DIR.N, tt);
			
			section.addRelBody(8, DIR.N, t.icon.scaled(4));
			
			tt = new GText(UI.FONT().S, ¤¤titleDD);
			tt.normalify2();
			tt.setMaxWidth(WIDTH);
			tt.setMultipleLines(true);
			tt.adjustWidth();
			section.addRelBody(8, DIR.S, tt);
			
			
			GHeader h = new GHeader(¤¤racesUnlocked);
			section.addRelBody(8, DIR.S, h);
			GuiSection s = new GuiSection();
			for (Race r : RACES.playable()) {
				if (t.race(r))
					s.addRightC(8, new HOVERABLE.Sprite(r.appearance().iconBig).hoverTitleSet(r.info.names));
			}
			section.addRelBody(8, DIR.S, s);
			
			tt = new GText(UI.FONT().S, ¤¤racesUnlockedD);
			tt.insert(0, 50.0/RACES.playable().size(), 1);
			tt.normalify2();
			tt.setMaxWidth(WIDTH);
			tt.setMultipleLines(true);
			tt.adjustWidth();
			section.addRelBody(4, DIR.S, tt);
			
			h = new GHeader(¤¤currentBoost);
			section.addRelBody(8, DIR.S, h);
			
			
			int hi = 32 + 32*t.boosters.all().size()/2;
			
			section.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(700, hi) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					
					GBox.tmp.clear();
					GBox.tmp.maxWidth = 700;
					GBox.tmp.maxHeight = 500;
					t.boosters.hover(GBox.tmp, t.boosterValue(), null, -1);
					GBox.tmp.renderWithout(r, body.x1(), body.y1());
				}
			});
			
		}
		
		
		private PTitle get() {
			for (PTitle t : FACTIONS.player().titles.all()) {
				if (t.key.equals(tkey))
					return t;
			}
			return null;
		}
	}
	
}
