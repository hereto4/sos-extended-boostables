package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.keymap.MAPPED;
import util.text.D;
import util.text.Dic;

public class DipStance implements MAPPED{

	private final List list = new List();
	
	public final CharSequence name;
	public final CharSequence desc;
	public final SPRITE icon;
	public final boolean trades;
	public final boolean transit;
	public final boolean ally;
	public final double loyalty;
	public final double minLoyalty;
	public final double tarif;
	private final int index;
	private final String key;
	
	DipStance(LISTE<DipStance> all, String key, double loyalty, double minLoyalty, double tarif, boolean trades, boolean transit, boolean ally, CharSequence name, CharSequence desc, SPRITE icon){
		this.name = name;
		this.desc = desc;
		this.icon = icon;
		this.trades = trades;
		this.transit = transit;
		this.ally = ally;
		this.index = all.add(this);
		this.key = key;
		this.loyalty = loyalty;
		this.minLoyalty = minLoyalty;
		this.tarif = tarif;
	}
	
	public boolean is(Faction faction, Faction other) {
		return DIP.s.is(faction, other, this);
	}
	
	public final boolean is(FactionNPC faction) {
		return is(faction, FACTIONS.player());
	}

	public final void set(Faction instigator, Faction accepter) {
		DIP.s.set(instigator, accepter, this);
	}
	
	public final void set(FactionNPC a) {
		set(FACTIONS.player(), a);
	}
	
	public LIST<? extends Faction> all(Faction f){
		return list.all(f);
	}
	
	public boolean any(Faction f){
		for (int fi = 0; fi < FACTIONS.active().size(); fi++) {
			Faction f2 = FACTIONS.active().get(0);
			if (f != f2 && is(f, f2))
				return true;
		}
		return false;
	}
	
	public LIST<FactionNPC> player(){
		return list.player();
	}
	
	final class List {
		private int[] state = new int[FACTIONS.MAX()];
		private Faction cf;
		private final ArrayList<Faction> tmp = new ArrayList<Faction>(FACTIONS.MAX());
		private final ArrayList<FactionNPC> player = new ArrayList<FactionNPC>(FACTIONS.MAX());
		
		public LIST<? extends Faction> all(Faction f){
			if (f == FACTIONS.player()) {
				if (state[f.index()] != DIP.s.stateI) {
					state[f.index()] = DIP.s.stateI;
					player.clearSloppy();
					for (FactionNPC o : FACTIONS.NPCs()) {
						if (f != o && is(o, f))
							player.add(o);
					}
				}
				
				return player;
			}
			if (cf != f || state[f.index()] != DIP.s.stateI) {
				cf = f;
				state[f.index()] = DIP.s.stateI;
				tmp.clearSloppy();
				for (Faction o : FACTIONS.active()) {
					if (f != o && is(o, f))
						tmp.add(o);
				}
			}
			return tmp;
		}
		
		public LIST<FactionNPC> player(){
			all(FACTIONS.player());
			return player;
		}
	}

	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String key() {
		return key;
	}
	
	private static CharSequence ¤¤minOpinion = "Minimum Opinion";
	static {
		D.ts(DipStance.class);
	}
	
	
	public void hover(GUI_BOX box) {
		GBox b = (GBox) box;
		box.title(name);
		box.text(desc);
		box.NL();
		
		if (this != DIP.WAR()) {
			b.textLL(¤¤minOpinion);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), minLoyalty));
			b.NL();
			
			b.textLL(Dic.¤¤Tariff);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), tarif));
			b.NL();
			
		}
		
	}

	
}
