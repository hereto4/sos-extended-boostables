package world.log;

import java.io.IOException;

import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.time.TIME;
import init.sprite.UI.Icons.S.IconS;
import init.sprite.UI.UI;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.text.D;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;

public final class WorldLog extends WorldResource{

	private static CharSequence ¤¤war = "The {0} declares war on {1}";
	static {
		D.ts(WorldLog.class);
	}
	public WorldLog() {
		super("log", "WLOGs");
		new DIP.DipActivityListener() {
			
			@Override
			public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
				if (nn == DIP.WAR()) {
					Str.TMP.clear();
					Str.TMP.add(¤¤war);
					Str.TMP.insert(0, faction.name);
					Str.TMP.insert(1, other.name);
					WORLD.LOG().log(faction, other, UI.icons().s.sword,Str.TMP, faction.cx(), faction.cy());
				}
				
			}
		};
	}

	public final int MAX = 256;
	private final ArrayList<LogEntry> all = new ArrayList<>(MAX);

	
	
	private LogEntry next() {
		if (!all.hasRoom()) {
			LogEntry e = all.get(0);
			all.shiftLeft();
			return e;
		}
		return new LogEntry();
	}
	
	public void log(Faction a, Faction b, IconS icon, CharSequence message, int tx, int ty) {
		
		int day = TIME.days().bitsSinceStart();
		short fa = (short) (a == null ? -1 : a.index());
		short fb = (short) (b == null ? -1 : b.index());
		short ii = (short) (icon == null ? -1 : icon.index);
		for (int i = all.size()-1; i >= 0; i--) {
			LogEntry o = all.get(i);
			if (o.day != day)
				break;
			if (o.ii == ii && o.fa == fa && o.fb == fb && o.message.equals(message))
				return;
		}
		
		LogEntry e = next();
		e.ii = ii;
		e.day = day;
		e.fa = fa;
		e.fb = fb;
		e.tx = (short) tx;
		e.ty = (short) ty;
		e.message.clear().add(message);
		all.add(e);
	}
	
	public LIST<LogEntry> all(){
		return all;
	}

	private WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			file.i(all.size());
			for (LogEntry e : all) {
				e.save(file);
			}
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			int am = file.i();
			all.clear();
			for (int i = 0; i < am; i++) {
				all.add(new LogEntry(file));
			}
			
		}
		
		@Override
		public void clear() {
			all.clearSloppy();
		}
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}
	
	


	
}
