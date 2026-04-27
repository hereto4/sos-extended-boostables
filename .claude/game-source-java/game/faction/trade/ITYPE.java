package game.faction.trade;

import game.faction.FResources.RTYPE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public final class ITYPE {
	
	private final static ArrayListGrower<ITYPE> pall = new ArrayListGrower<ITYPE>();
	
	public final static LIST<ITYPE> all = pall;
	
	public static final ITYPE tax = new ITYPE(RTYPE.TAX);
	public static final ITYPE trade = new ITYPE(RTYPE.TRADE);
	public static final ITYPE spoils = new ITYPE(RTYPE.SPOILS);
	public static final ITYPE diplomacy = new ITYPE(RTYPE.DIPLOMACY);
	
	public final CharSequence name;
	public final int index;
	public final RTYPE rtype;
	
	private ITYPE(RTYPE rtype) {
		this.name = rtype.name;
		this.index = pall.add(this);
		this.rtype = rtype;
	}

}