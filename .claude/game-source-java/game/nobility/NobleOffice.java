package game.nobility;

import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.room.main.RoomBlueprintIns;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GBox;
import util.info.GFORMAT;

public abstract class NobleOffice {

	public final CharSequence name;
	public final CharSequence desc;
	public final BoostSpecs boosts;
	public final double add;
	public final Boostable target;
	public final Icon icon;
	public final int index;
	public boolean special;
	
	NobleOffice(ArrayListGrower<NobleOffice> all, double add, Boostable target, CharSequence name, CharSequence desc, Icon icon){
		this.add = add;
		this.target = target;
		this.name = name;
		this.desc = desc;
		boosts = new BoostSpecs(HCLASSES.NOBLE().name, UI.icons().s.noble, false);
		boosts.push(target, add, false);
		this.icon = icon;
		this.index = all.add(this);
	}
	
	public abstract double value(int slots);
	
	public abstract int popBoosted(int slots);
	
	public abstract void hoverValue(GBox b, int slots);
	
	public int allocated(Noble n) {
		if (n.office() == this)
			return 1 + 4*n.rank();
		return 0;
	}
	
	public RoomBlueprintIns<?> room(){
		return null;
	}
	
	public boolean leavesMap() {
		return false;
	}
	
	public void hover(GUI_BOX box) {
		GBox b = (GBox) box;
		b.title(name);
		b.text(desc);
		b.NL(4);
		b.add(target.icon);
		b.textL(target.name);
		b.tab(6);
		double d = value(1);
		d*= add;
		b.add(GFORMAT.f0(b.text(), d, 4));
		
	}
	



	
}
