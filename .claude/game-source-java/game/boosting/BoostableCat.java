package game.boosting;

import init.sprite.UI.UI;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEANO;
import util.text.D;
import util.text.Dic;

public class BoostableCat {
	
	public static final class All {
		public final BoostableCat WORLD_CIVICS = new BoostableCat("WORLD_", Dic.¤¤World + ": " + Dic.¤¤Civics, "", TYPE_WORLD, UI.icons().s.world);
		public final BoostableCat WORLD_PRODUCTION = new BoostableCat("WORLD_", Dic.¤¤World + ": " + Dic.¤¤Production, "", TYPE_WORLD, UI.icons().s.world);
		public final BoostableCat RES_CONSUMPTION = new BoostableCat("CON_", Dic.¤¤ConsumptionRate, "", TYPE_SETT, UI.icons().s.storage);
		public final BoostableCat WORLD = new BoostableCat("WORLD_", Dic.¤¤World, "", TYPE_WORLD, UI.icons().s.world);
		public final BoostableCat RELIGION = new BoostableCat("RELIGION_", ¤¤conversion, "", TYPE_WORLD | TYPE_SETT, UI.icons().s.shrine);
		public final BoostableCat WORLD_DUMP = new BoostableCat("WORLD_", Dic.¤¤World + ": " + Dic.¤¤Misc, "", TYPE_CRAP, UI.icons().s.world);
	}
	
	public static final int TYPE_CRAP = 0b0001;
	public static final int TYPE_WORLD = 0b0010;
	public static final int TYPE_SETT = 0b0100;
	
	public final String prefix;
	public final CharSequence name;
	public final CharSequence desc;
	public final SPRITE icon;
	public final int typeMask;
	ArrayListGrower<Boostable> all = new ArrayListGrower<>();
	private static CharSequence ¤¤conversion = "¤Conversion";
	static {
		D.ts(BoostableCat.class);
	}

	
	public BoostableCat(String prefix, CharSequence name, CharSequence desc, int typeMask, SPRITE icon) {
		this.prefix = prefix;
		this.name = name;
		this.desc = desc;
		this.typeMask = typeMask;
		this.icon = icon;
	}
	
	public LIST<Boostable> all() {
		return all;
	}
	
	private static All al;
	
	static void init() {
		al = new All();		
	}
	
	public static All ALL() {
		return al;
	}
	
	public final BOOLEANO<BoostSpec> filter = new BOOLEANO<BoostSpec>() {

		@Override
		public boolean is(BoostSpec t) {
			return t.boostable.cat == BoostableCat.this;
		}
		
	};
	
}