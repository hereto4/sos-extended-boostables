package init.type;



import java.io.Serializable;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.keymap.MAPPED;
import util.keymap.RMAPS;

public final class HGROUP implements MAPPED{

	private static LIST<HGROUP> ALL;
	private static LIST<HGROUP> CIT;
	private static LIST<HGROUP> SLAVE;
	private static RMAPS<HGROUP> MAP;
	
	
	static void init(){
		ArrayListGrower<HGROUP> l = new ArrayListGrower<HGROUP>();
		ArrayListGrower<HGROUP> ll = new ArrayListGrower<HGROUP>();
		for (Race r : RACES.all()) {
			HGROUP g = new HGROUP(l.size(),HCLASSES.SLAVE(), r);
			l.add(g);
			ll.add(g);
		}
		SLAVE = ll;
		ll = new ArrayListGrower<HGROUP>();
		for (Race r : RACES.all()) {
			HGROUP g = new HGROUP(l.size(),HCLASSES.CITIZEN(), r);
			l.add(g);
			ll.add(g);
		}
		CIT = ll;
		ALL = l;
		MAP = new RMAPS<>("HGROUP", l);
	}
	
	public final HCLASS type;
	public final Race race;
	public final SPRITE icon;
	public final String name;
	public final int index;
	public final String key;
	
	public static LIST<HGROUP> all(){
		return ALL;
	}
	
	public static HGROUP get(HCLASS c, Race r) {
		if (c == HCLASSES.SLAVE())
			return SLAVE.get(r.index());
		else if (c == HCLASSES.CITIZEN()) {
			return CIT.get(r.index());
		}
		return null;
	}
	
	public static HGROUP get(Humanoid h){
		return get(h.indu());
	}
	
	public static HGROUP get(Induvidual i) {
		return get(i.clas(), i.race());
	}
	
	public static RMAPS<HGROUP> MAP(){
		return MAP;
	}
	
	private HGROUP(int index, HCLASS t, Race r) {
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
				type.iconSmall().render(rr, x2, (int)(x2 + type.iconSmall().width()*scale), Y1, (int)(Y1 + type.iconSmall().width()*scale));
				
				
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
		

		public default boolean is(HGROUP type) {
			if (type == null)
				return false;
			return is(type.index);
		}
		
		public boolean is(int index);
		
		public default boolean is(Humanoid h) {
			if (h.indu().clas().player)
				return is(HGROUP.get(h));
			return false;
		}
		
	}
	
	public static class HTypeBitsImp implements HTypeBits, Serializable{
		
		private static HTypeBitsImp[] specific;
		
		public static HTypeBits specific(HGROUP t) {
			if (specific == null) {
				specific = new HTypeBitsImp[HGROUP.all().size()];
				for (int i = 0; i < HGROUP.all().size(); i++) {
					specific[i] = new HTypeBitsImp(false);
					specific[i].set(HGROUP.all().get(i));
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
				data = new int[(int) (Math.ceil(HGROUP.all().size()/32.0))];
			else if (data.length != (Math.ceil(HGROUP.all().size()/32.0))) {
				data = new int[(int) (Math.ceil(HGROUP.all().size()/32.0))];
			}
			return data;
		}
		
		@Override
		public boolean is(int index) {
			int ii = index>>>5;
			int m = 1 << (index & 0b011111); 
			return (bits()[ii] & m) != 0;
		}
		
		public HTypeBitsImp set(HGROUP type) {
			int index = type.index;
			int ii = index>>>5;
			int m = 1 << (index & 0b011111); 
			bits()[ii] |= m;
			
			return this;
		}
		
		public HTypeBitsImp copy(HTypeBits other) {
			clear();
			for (int i = 0; i < HGROUP.all().size(); i++) {
				if (other.is(i))
					set(HGROUP.all().get(i));
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
		
		public HTypeBitsImp clear(HGROUP type) {
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
