package game.faction.player;

import java.io.IOException;

import game.GameDisposable;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;

public class PlayerColors {

	private final static KeyMap<LinkedList<PlayerColor>> cats = new KeyMap<>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				cats.clear();
			}
		};
	}
	
	public static KeyMap<LinkedList<PlayerColor>> cats(){
		return cats;
	}
	
	
	
	public static class PlayerColor {
		
		public final ColorImp color;
		public final COLOR def;
		public final CharSequence name;
		public final String cat;
		public final String key;
		
		public PlayerColor(String key, CharSequence category, CharSequence name){
			this(new ColorImp(), key, category, name);
		}
		
		public PlayerColor(ColorImp col, String key, CharSequence category, CharSequence name){
			this.key = key;
			this.cat = ""+category;
			this.name = name;
			this.color = col;
			this.def = new ColorImp(col);
			if (!cats.containsKey(cat))
				cats.put(cat, new LinkedList<>());
			cats.get(cat).add(this);;
		}
		
	}

	public static final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			int am = 0;
			for (LinkedList<PlayerColor> li : cats.all()) {
				am += li.size();
			}
			file.i(am);
			for (LinkedList<PlayerColor> li : cats.all()) {
				for (PlayerColor col : li) {
					file.chars(col.cat+col.key);
					col.color.save(file);
				}
			}
		}

		@Override
		public void load(FileGetter file) throws IOException {
			KeyMap<PlayerColor> map = new KeyMap<>();
			for (LinkedList<PlayerColor> li : cats.all()) {
				for (PlayerColor col : li) {
					map.put(col.cat+col.key, col);
				}
			}
			int am = file.i();
			while(am-- > 0) {
				String k = file.chars();
				ColorImp c = new ColorImp();
				c.load(file);
				if (map.containsKey(k))
					map.get(k).color.set(c);
			}
			
		}

		@Override
		public void clear() {
			for (LinkedList<PlayerColor> li : cats.all()) {
				for (PlayerColor col : li) {
					col.color.set(col.def);
				}
			}
		}
	};


	
}
