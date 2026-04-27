package world.region;

import game.faction.Faction;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD.RDInit;

public class RData implements INT_O<Region>{
	
	public final CharSequence name;
	
	public RData(String key, INT_OE<Region> plocal, RDInit init, CharSequence name){
		this.name = name;
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				change(oldOwner);
				change(newOwner);
			}
			
			private void change( Faction f) {
				if (f != null) {
					ftotal.set(f, 0);
					for (int i = 0; i < f.realm().regions(); i++)
						ftotal.inc(f, plocal.get(f.realm().region(i)));
				}
			}
		};
		this.plocal = plocal;
		ftotal = init.rCount.new DataInt(key);
	}
	
	protected final INT_OE<Region> plocal;
	protected final INT_OE<Faction> ftotal;
	
	@Override
	public int get(Region t) {

		return plocal.get(t);
	}

	@Override
	public int min(Region t) {
		return 0;
	}

	@Override
	public int max(Region t) {
		return plocal.max(t);
	}

	public INT_O<Faction> faction() {
		return ftotal;
	}
	
	public static class RDataE extends RData implements INT_OE<Region> {

		public RDataE(String key, INT_OE<Region> plocal, RDInit init, CharSequence name) {
			super(key, plocal, init, name);
		}
		
		@Override
		public void set(Region t, int i) {
			if (i != get(t)) {
				if (t.faction() != null) {
					ftotal.inc(t.faction(), -plocal.get(t));
				}
				this.plocal.set(t, i);
				if (t.faction() != null) {
					ftotal.inc(t.faction(), plocal.get(t));
				}
			}
		}
	}

}
