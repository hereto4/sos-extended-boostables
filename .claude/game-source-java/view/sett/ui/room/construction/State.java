package view.sett.ui.room.construction;

import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.FurnisherItemGroup;
import settlement.room.main.placement.PLACEMENT;
import snake2d.util.misc.CLAMP;

final class State {

	final int[] item = new int[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
//	final int[] upgradeMax = new int[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
	final PLACEMENT placement = SETT.ROOMS().placement;
	RoomBlueprintImp b;
	boolean refurnishing;
	final Config config = new Config(this);
	private int itemI = 0;
	RoomCategorySub collection;
	
	
	FurnisherItemGroup problemGroup;
	boolean problemneedDoor;
	boolean problemneedArea;
	double problemTimer;
	
	public void setItem(int itI) {
		itemI = CLAMP.i(itI, 0, b.constructor().pgroups().size());
		item[b.index()] = itemI;
	}
	
	public int item() {
		return itemI;
	}
	
	public void init(RoomBlueprintImp b2, boolean refurnishing) {
		collection = null;
		
		config.build = true;
		this.b = b2;
		if (b2.constructor().usesArea())
			setItem(0);
		else
			setItem(item[b2.index()]);
		this.refurnishing = refurnishing;
//		setUpgrade(FACTIONS.player().locks.maxUpgrade(b2), b2);
		problemGroup = null;
		problemneedDoor = false;
		problemneedArea = false;
	}
	
//	void setUpgrade(int up, RoomBlueprintImp b2) {
//		int max = FACTIONS.player().locks.maxUpgrade(b2);
//		upgradeMax[b2.index()] = max;
//		upgrade[b2.index()] = CLAMP.i(up, 0, upgradeMax[b2.index()]);
//	}
	
	public void init(RoomBlueprintImp b2, RoomCategorySub collection) {
		init(b2, false);
		this.collection = collection;
	}
	
	
}
