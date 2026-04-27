package init.type;



import java.io.Serializable;
import java.util.Arrays;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.keymap.MAPPED;
import util.keymap.RMAPS;

public final class WGROUP implements MAPPED{

	private static LIST<WGROUP> ALL;
	private static RMAPS<WGROUP> MAP;
	private static final int[] toIndex = new int[HTYPES.ALL().size()];
	
	
	static void init(){
		ArrayListGrower<HTYPE> l = new ArrayListGrower<HTYPE>();
		for (HTYPE t : HTYPES.ALL())
			if (t.works)
				l.add(t);
		ArrayList<WGROUP> all = new ArrayList<WGROUP>(l.size()*RACES.all().size());
		Arrays.fill(toIndex, -1);
		for (int ci = 0; ci < l.size(); ci++) {
			toIndex[l.get(ci).index()] = ci;
			for (Race r : RACES.all()) {
				all.add(new WGROUP(ci*RACES.all().size()+r.index, l.get(ci), r));
			}
		}
		ALL = all;
		MAP = new RMAPS<>("EGROUP", all);
	}
	
	public final HTYPE type;
	public final Race race;
	public final SPRITE icon;
	public final String name;
	public final int index;
	public final String key;
	
	public static LIST<WGROUP> all(){
		return ALL;
	}
	
	public static WGROUP get(HTYPE c, Race r) {
		int ci = toIndex[c.index()];
		if (ci < 0)
			return null;
		return ALL.get(ci*RACES.all().size()+r.index);
	}
	
	public static WGROUP get(Humanoid h){
		return get(h.indu());
	}
	
	public static WGROUP get(Induvidual i) {
		return get(i.hType(), i.race());
	}
	
	public static RMAPS<WGROUP> MAP(){
		return MAP;
	}
	
	private WGROUP(int index, HTYPE t, Race r) {
		this.type = t;
		this.race = r;
		this.index = index;
		key = t.key + "_" + r.key;
		name = r.info.names + " ("+t.names + ")";
		icon = new SPRITE.Imp(Icon.M+12, Icon.M) {
			
			@Override
			public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
				if (race == null || race.appearance() == null || race.appearance().icon == null)
					return;
				double scale = (double)(Y2-Y1)/height();
				int x2 =(int)(X1 +race.appearance().icon.width()*scale);
				race.appearance().icon.render(rr, X1, x2, Y1, (int)(Y1 +race.appearance().icon.height()*scale));
				x2 -= 6*scale;
				type.CLASS.iconSmall().render(rr, x2, (int)(x2 + type.CLASS.iconSmall().width()*scale), Y1, (int)(Y1 + type.CLASS.iconSmall().width()*scale));
				
				
			}
		};
		
		
	}
		
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return race.info.name + " " + type.name;
	}

	@Override
	public String key() {
		return key;
	}
	
	public interface HTypeBits {
		

		public default boolean is(WGROUP type) {
			return is(type.index);
		}
		
		public boolean is(int index);
		
		public default boolean is(Humanoid h) {
			return is(WGROUP.get(h));
		}
		
	}
	
	public static class HTypeBitsImp implements HTypeBits, Serializable{
		
		private static HTypeBitsImp[] specific;
		
		public static HTypeBits specific(WGROUP t) {
			if (specific == null) {
				specific = new HTypeBitsImp[WGROUP.all().size()];
				for (int i = 0; i < WGROUP.all().size(); i++) {
					specific[i] = new HTypeBitsImp(false);
					specific[i].set(WGROUP.all().get(i));
				}
			}
			return specific[t.index];
		}
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] data;
		
		public HTypeBitsImp(boolean everyone) {
			if (everyone)
				setEveryone();
		}

		private int[] bits() {
			if (data == null)
				data = new int[(int) (Math.ceil(WGROUP.all().size()/32.0))];
			else if (data.length != (Math.ceil(WGROUP.all().size()/32.0))) {
				data = new int[(int) (Math.ceil(WGROUP.all().size()/32.0))];
			}
			return data;
		}
		
		@Override
		public boolean is(int index) {
			int ii = index>>>5;
			int m = 1 << (index & 0b011111); 
			return (bits()[ii] & m) != 0;
		}
		
		public HTypeBitsImp set(WGROUP type) {
			int index = type.index;
			int ii = index>>>5;
			int m = 1 << (index & 0b011111); 
			bits()[ii] |= m;
			
			return this;
		}
		
		public HTypeBitsImp copy(HTypeBits other) {
			clear();
			for (int i = 0; i < WGROUP.all().size(); i++) {
				if (other.is(i))
					set(WGROUP.all().get(i));
			}
			return this;
		}
		
		public HTypeBitsImp setEveryone() {
			int[] bits = bits();
			for (int i = 0; i < bits.length; i++) {
				bits[i] = -1;
			}
			return this;
		}
		
		public HTypeBitsImp clear(WGROUP type) {
			int index = type.index;
			int ii = index>>>5;
			int m = 1 << (index & 0b011111); 
			bits()[ii] &= ~m;
			return this;
		}
		
		public HTypeBitsImp clear() {
			int[] bits = bits();
			for (int i = 0; i < bits.length; i++) {
				bits[i] = 0;
			}
			return this;
		}
		
	}
	
}
