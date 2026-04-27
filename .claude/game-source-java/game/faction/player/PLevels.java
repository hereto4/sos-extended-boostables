package game.faction.player;

import java.io.IOException;

import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.UI.UI;
import init.value.GVALUES;
import init.value.Lockable;
import init.value.Lockers;
import settlement.stats.Induvidual;
import snake2d.Errors;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import view.interrupter.IDebugPanel;
import view.ui.message.MessageSection;
import world.map.regions.Region;

public final class PLevels {


	
	private static CharSequence ¤¤mTitle = "¤New level unlocked!";
	private static CharSequence ¤¤mMessage = "¤A new level has been bestowed upon your name!";
	{
		D.t(this);
	}
	
	public final INFO info = new INFO(
			D.g("Level"), 
			D.g("Desc", "As you grow in might and population, titles will be bestowed upon your name. Levels will unlock great advantages to a ruler."));
	
	
	private final ArrayList<Level> levels;
	
	private Level current;
	private boolean increase;
	public final BoostSpecs boosters;
	private final BoostCompound<Level> bos;
	private double time;
	
	public PLevels() {
		
		PATH data = PATHS.INIT().getFolder("player").getFolder("level");
		PATH text = PATHS.TEXT().getFolder("player").getFolder("level");
		String[] ss = data.getFiles();
		
		levels = new ArrayList<>(ss.length);
		
		for (String s : ss) {
			new Level(levels, s, data, text);
		}
		
		if (levels.size() == 0)
			new Errors.DataError("Insufficient levels declared. Needs more than 0", data.get());
		current = levels.get(0);
		

		
		IDebugPanel.add("Increase level", new ACTION() {
			
			@Override
			public void exe() {
				increase = true;
				time = TIME.secondsPerDay();
			}
		});
		
		boosters = new BoostSpecs(Dic.¤¤Level, UI.icons().s.star, true);
		bos = new BoostCompound<PLevels.Level>(boosters, levels) {
			
			@Override
			protected double getValue(Level t) {
				return current.index >= t.index ? 1 : 0;
			}
			
			@Override
			protected BoostSpecs bos(Level t) {
				return t.boosters;
			}
		};
		
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(current.index);
			file.d(time);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int i = file.i();
			if (i >= levels.size())
				i = levels.size()-1;
			current = levels.get(i);
			
			bos.clearChache();
			time = file.d();
		}
		
		@Override
		public void clear() {
			current = levels.get(0);
			bos.clearChache();
			time = 0;
		}
	};
	
	void update(double ds) {
		if (current().index() < levels.size()-1 && (increase || levels.get(current().index()+1).lockable.passes(FACTIONS.player()))) {
			time += ds;
			if (time > TIME.secondsPerDay()) {
				current = levels.get(current().index()+1);
				bos.clearChache();
				new Mess(current.index).send();
				increase = false;
			}
		}else {
			time = TIME.currentSecond();
		}
	}
	
	public LIST<Level> all(){
		return levels;
	}
	
	public Level current() {
		return current;
	}	
	
	public void set(int level) {
		this.current = levels.get(level);
	}
	
	public static class Level implements INDEXED{
		
		private final int index;
		public final CharSequence male;
		public final CharSequence female;
		public final CharSequence desc;
		
		public final BoostSpecs boosters;
		public final Lockable<Faction> lockable;
		public final Lockers lockers;
		
		Level(ArrayList<Level> all, String key, PATH data, PATH text){
			this.index = all.add(this);
			Json d = new Json(data.get(key));
			Json t = new Json(text.get(key));
			male = t.text("MALE");
			female = t.text("FEMALE");
			desc = t.text("DESC");
			
			lockable = GVALUES.FACTION.LOCK.push();
			lockable.push(d);
			lockers = new Lockers(Dic.¤¤Level + ": " + male, UI.icons().s.star);
			
			lockers.add(GVALUES.FACTION, d, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					
					if (t == FACTIONS.player()) {
						if (FACTIONS.player().level().current().index() >= Level.this.index())
							return 1.0;
						return 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.INDU, d, new DOUBLE_O<Induvidual>() {

				@Override
				public double getD(Induvidual t) {
					if (t.faction() == FACTIONS.player()) {
						if (FACTIONS.player().level().current().index() >= Level.this.index())
							return 1.0;
						return 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.REGION, d, new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					if (t.faction() == FACTIONS.player()) {
						if (FACTIONS.player().level().current().index() >= Level.this.index())
							return 1.0;
						return 0;
					}
					return 1;
				}
			
			});
			
			boosters = new BoostSpecs(Dic.¤¤Level + ": " + male, UI.icons().s.star, false);
			boosters.read(d, null);
		}
		
		public CharSequence name() {
			return male;
		}
		
		@Override
		public int index() {
			return index;
		}
		
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(name());
			GText t = b.text();
			t.add(desc);
			b.add(t);
			b.NL(4);
			
			lockable.hover(text, FACTIONS.player());
			b.sep();
			lockers.hover(text);
			b.NL(8);
			boosters.hover(text, 1.0, -1);
			
		}
	}
	
	private static class Mess extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int lev;
		
		public Mess(int lev) {
			super(¤¤mTitle);
			this.lev = lev;
			
		}

		@Override
		protected void make(GuiSection section) {
			
			Level l = FACTIONS.player().level().all().get(lev);
			paragraph(¤¤mMessage);
			section.addRelBody(16, DIR.S, new GHeader(l.name()));
			
			section.addRelBody(8, DIR.S, new RENDEROBJ.RenderImp(700, GBox.Dummy().maxHeight) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GBox.tmp.clear();
					GBox.tmp.maxWidth = 700;
					GBox.tmp.maxHeight = 500;
					l.hoverInfoGet(GBox.tmp);
					GBox.tmp.renderWithout(r, body.x1(), body.y1());
				}
			});
			
		}
		
		
	}
	
}
