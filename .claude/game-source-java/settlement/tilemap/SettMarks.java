package settlement.tilemap;

import java.io.IOException;

import game.GAME;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

public final class SettMarks extends TileMap.Resource{

	public final int max = 32;
	public int state;
	public final LIST<SettMark> all;
	private final ArrayList<SettMark> active = new ArrayList<SettMark>(max);
	
	SettMarks(){
		ArrayList<SettMark> all = new ArrayList<SettMark>(max);
		while(all.hasRoom())
			all.add(new SettMark());
		this.all = all;
			
	}
	
	int upI = 0;
	
	public LIST<SettMark> active(){
		if (upI == GAME.updateI())
			return active;
		upI = GAME.updateI();
		active.clearSloppy();
		for (SettMark s : all)
			if (s.active)
				active.add(s);
		return active;
	}
	
	public int state() {
		return state;
	}
	
	
	@Override
	protected void save(FilePutter file) {
		
		file.i(state);
		for (SettMark d : all) {
			file.bool(d.active);
			file.i(d.tile.x());
			file.i(d.tile.y());
			
			d.color.save(file);
			d.name.save(file);
		}
	
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		active.clear();
		state = file.i();
		for (SettMark d : all) {
			d.active = file.bool();
			d.tile.set(file.i(), file.i());
			d.color.load(file);
			d.name.load(file);
			
		}
	}
	
	@Override
	protected void clearAll() {
		state = 0;
		for (SettMark d : all) {
			d.active = false;
		}
	}
	
	public SettMark make() {
		for (SettMark d : all)
			if (!d.active) {
				d.active = true;
				state++;
				return d;
			}
		
		return null;
			
	}
	
	public final class SettMark implements SAVABLE {
			
		public final ShortCoo tile = new ShortCoo();
		public final ColorImp color = new ColorImp();
		public final Str name = new Str(20);
		public boolean active;
		
		SettMark(){
			

		}
		
		public void setPosition(int position) {
			int i = 0;
			for (SettMark d : active()) {
				if (i == position) {
					if (d == this)
						return;
					if (!d.active)
						return;
					state++;
					int tx = d.tile.x();
					int ty = d.tile.y();
					ColorImp.TMP.set(d.color);
					Str.TMP.clear().add(d.name);
					
					d.tile.set(tile);
					d.color.set(color);
					d.name.clear().add(name);
					
					tile.set(tx, ty);
					color.set(ColorImp.TMP);
					name.clear().add(Str.TMP);
				}
				if (d.active)
					i++;
				
			}
			
		}
		
		public void set(int tx, int ty) {
			active = true;
			tile.set(tx, ty);
			color.set(RND.rInt(127), RND.rInt(127), RND.rInt(127));
			
			name.clear().add('?');
			state++;
			
		}
		
		public void remove() {
			if (active) {
				active = false;
				state++;
			}
		}
		
		@Override
		public void save(FilePutter file) {
			file.bool(active);
			file.i(tile.x());
			file.i(tile.y());
			
			color.save(file);
			name.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			active = file.bool();
			tile.set(file.i(), file.i());
			color.load(file);
			name.load(file);
		}
		
		@Override
		public void clear() {
			active = false;
		}
	}


	
}
