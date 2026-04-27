package world.entity.haven;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import init.race.Race;
import init.type.CLIMATES;
import init.type.TERRAINS;
import init.value.COMPARATOR;
import init.value.GVALUES;
import init.value.Lockable;
import init.value.Value;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.info.GFORMAT;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class WHavenType implements INDEXED{

	private final int index;
	public final TILE_SHEET sheet;
	public final COLOR cMask;
	public final LIST<CharSequence> names;
	public final CharSequence sJoin;
	public final CharSequence sLeave;
	public final Race race;
	public final int popFrom;
	public final int popTo;
	public final double replenishMin;
	public final double replenishMax;
	public final Lockable<Faction> reqsFrom;
	public final Lockable<Faction> reqsTo;
	
	
	
	public final double[] climates;
	public final double[] terrains;

	private ArrayListGrower<Delta> deltas = new ArrayListGrower<>();
	private ArrayListGrower<Gauge> gauges = new ArrayListGrower<WHavenType.Gauge>();
	
	WHavenType(String key, LISTE<WHavenType> all, Json jdata, Json jtext, TILE_SHEET sheet) throws IOException{
		index = all.add(this);
		this.sheet = sheet;
		cMask = new ColorImp(jdata, "COLOR_MASK");
		names = new ArrayList<>(jtext.texts("NAMES", 1, 500));
		sJoin = jtext.text("JOIN");
		sLeave = jtext.text("LEAVE");
		
		race = RACES.map().get(jdata.value("RACE"), jdata);
		climates = CLIMATES.MAP().readFill(jdata, 1);
		terrains = TERRAINS.MAP().readFill(jdata, 100);
		reqsFrom = GVALUES.FACTION.LOCK.push("WORLD_CAMP_"+key, race.info.names, race.info.names, race.appearance().icon);
		reqsFrom.push("REQUIRES_MIN", jdata);
		reqsTo = GVALUES.FACTION.LOCK.push();
		reqsTo.push("REQUIRES_MAX", jdata);
		
		GVALUES.FACTION.new LockJson("REQUIRES_MIN", jdata) {
			
			@Override
			public void callback(COMPARATOR comp, Value<Faction> value, String key, Json json) {
				add(true, comp, value, json.d(key));
			}
		};
		
		GVALUES.FACTION.new LockJson("REQUIRES_MAX", jdata) {
			
			@Override
			public void callback(COMPARATOR comp, Value<Faction> value, String key, Json json) {
				add(false, comp, value, json.d(key));
			}
		};
		
		popFrom = jdata.i("CAMP_SIZE_FROM", 1, 1000);
		popTo = jdata.i("CAMP_SIZE_TO", popFrom, 1000);
		
		replenishMin = jdata.d("REPLENISH_PER_DAY_FROM");
		replenishMax = jdata.d("REPLENISH_PER_DAY_TO");
		
	}
	
	private void add(boolean from, COMPARATOR comp, Value<Faction> value, double target) {
		for (Delta d : deltas) {
			if (d.comp == comp && d.value == value) {
				if (from)
					d.from = target;
				else
					d.to = target;
				return;
			}
		}
		
		Delta d = new Delta();
		d.comp = comp;
		d.value = value;
		if (from)
			d.from = target;
		else
			d.to = target;
		deltas.add(d);
		
		gauges.add(new Gauge(d));
	}
	
	static LIST<WHavenType> types() throws IOException{
		
		LinkedList<WHavenType> all = new LinkedList<>();
		KeyMap<TILE_SHEET> sheets = new KeyMap<>();
		
		ResFolder f = PATHS.RACE().folder("worldcamp");
		
		for (String file : f.init.getFiles()) {
			
			Json jdata = new Json(f.init.get(file));
			Json jtext = new Json(f.text.get(file));
			
			String ssprite = jdata.value("SPRITE");
			TILE_SHEET sheet = sheets.get(ssprite);
			if (sheet == null) {
				TILE_SHEET s = new ITileSheet(f.sprite.get(ssprite), 132, 126) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 2, 4, d.s24);
						s.singles.paste(3, true);
						return d.s24.saveGame();
					}
				}.get();
				sheets.put(ssprite, s);
				sheet = s;
			}
			new WHavenType(file, all, jdata, jtext, sheet);
		}
		
		return new ArrayList<>(all);
		
	}
	
	@Override
	public int index() {
		return index;
	}
	
	private static class Delta {
		
		double from = Double.NaN;
		double to = Double.NaN;
		COMPARATOR comp;
		Value<Faction> value;
		
		
	}
	
	public double amount() {
		
		if (!reqsFrom.passes(FACTIONS.player()))
			return 0;
		
		double d = 1.0;
		int am = 0;
		
		for (Delta de : deltas) {
			
			if (de.to == Double.NaN)
				continue;
			
			double v = de.value.d.getD(FACTIONS.player());
			
			if (de.from == Double.NaN) {
				//d += CLAMP.d(de.comp.progress(v, de.to), 0, 1);
			}else {
				
				double zero = CLAMP.d(de.comp.progress(de.from, de.to), 0, 1);
				if (zero >= 1) {
					//d += 1;
				}else {
					double delta = 1.0-zero;
					double vv = CLAMP.d(de.comp.progress(v, de.to), 0, 1);
					vv -= zero;
					vv /= delta;
					
					vv = CLAMP.d(vv, 0, 1);
					d += vv;
				}
					
				
				
				
			}
			am++;
		}
		
		if (am > 0)
			d /= am;
		
		d = 0.2 + 0.8*((int)Math.round(d*5)/5.0);
		
		return d;
		
		
	}
	
	public void hoverProgress(GUI_BOX bb) {
		GBox b = (GBox) bb;
		for (Delta de : deltas) {
			
			if (de.to == Double.NaN)
				continue;
			
			double v = de.value.d.getD(FACTIONS.player());
			
			b.text(de.value.name);
			b.tab(6);
			b.add(GFORMAT.f(b.text(), v, 2));
			b.tab(8);
			b.add(b.text().add('/'));
			
			b.add(GFORMAT.f(b.text(), de.to, 2));
			b.NL();
		}
		
		for (Gauge de : gauges) {
			b.add(de);
			b.NL();
		}
		
	}
	
	public double progress() {
		double d = 0;
		int am = 0;
		for (Delta de : deltas) {
			
			am++;
			d += de.value.d.getD(FACTIONS.player());
		}
		
		return d/am;
	}
	
	private class Gauge extends SPRITE.Imp{

		private final Delta de;
		
		Gauge(Delta delta){
			super(128, 16);
			this.de = delta;
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			double vv = 0;
			double v = de.value.d.getD(FACTIONS.player());
			vv = de.comp.progress(v, de.to);
			vv = CLAMP.d(vv, 0, 1);
			GMeter.render(r, GMeter.C_BLUE, vv, X1, X2, Y1, Y2);
			
			
		}
		
		
	}

	
}
