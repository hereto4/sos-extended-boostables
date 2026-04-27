package view.sett.ui.room.prints;

import settlement.room.main.RoomBlueprintImp;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;

class Cat {

	public boolean expanded = false;
	public int entries;
	public SPRITE icon;
	final Class<? extends RoomBlueprintImp> classs;
	public final ArrayListGrower<RoomBlueprintImp> prints = new ArrayListGrower<>();
	
	public Cat(RoomBlueprintImp blue) {
		this.icon = blue.iconBig();
		this.classs = blue.getClass();
		this.prints.add(blue);
	}
	
}
