package util.race;

import java.io.IOException;

import init.race.RACES;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.Bitmap1D;
import util.info.INFO;
import util.text.D;

public interface PERMISSION {

	public boolean get(HCLASS cl, Race race);
	public void set(HCLASS cl, Race race, boolean value);
	public default void toggle(HCLASS cl, Race race) {
		set(cl, race, !get(cl, race));
	}
	public default boolean get(Induvidual indu) {
		return get(indu.clas(), indu.race());
	}
	public default boolean has(Humanoid h) {
		return get(h.indu());
	}
	public INFO info();
	
	
	
	public class Permission implements PERMISSION, SAVABLE{
		private static CharSequence ¤¤name = "¤Permission";
		private static CharSequence ¤¤desc = "¤Toggle permission";
		static {
			D.ts(PERMISSION.class);
		}
		
		private final Bitmap1D access = new Bitmap1D(RACES.all().size()*HCLASSES.ALL().size(), false); 
		private boolean def = false;
		private final INFO info;
		
		public Permission(INFO info){
			this.info = info;
		}
		
		public Permission(CharSequence name, CharSequence desc){
			this.info = new INFO(name, desc);
		}
		
		public Permission(){
			this.info = new INFO(¤¤name, ¤¤desc);
		}

		
		
		@Override
		public boolean get(HCLASS cl, Race race) {
			if (race == null) {
				for (Race r : RACES.all()) {
					if (get(cl, r))
						return true;
				}
				return false;
			}
			return access.get(cl.index()*RACES.all().size()+race.index);
		}

		@Override
		public void set(HCLASS cl, Race race, boolean value) {
			if (race == null) {
				for (Race r : RACES.all()) {
					set(cl, r, value);
				}
			}else {
				access.set(cl.index()*RACES.all().size()+race.index, value);
			}
		}

		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void save(FilePutter file) {
			access.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			access.load(file);
		}

		@Override
		public void clear() {
			access.setAll(def);
		}
		
		public void setDef(boolean def) {
			this.def = def;
			clear();
		}
		
	}
	
}
