package view.battle;

import game.GAME;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;

public final class CatSelection {

	private final ArrayListResize<ArtilleryInstance> all = new ArrayListResize<ArtilleryInstance>(128);
	private final ArrayListResize<ArtilleryInstance> selection = new ArrayListResize<>(128);
	private int upI = -1;
	
	public LIST<ArtilleryInstance> all(){
		if (upI != GAME.updateI()) {
			all.clearSoft();
			selection.clearSoft();
			upI = GAME.updateI();
			for (ROOM_ARTILLERY cat : SETT.ROOMS().ARTILLERY) {
				for (int i = 0; i < cat.instancesSize(); i++) {
					ArtilleryInstance ins = cat.getInstance(i);
					all.add(ins);
					if (ins.selected)
						selection.add(ins);
				}
			}
		}
		return all;
	}
	
	public void select(ArtilleryInstance s) {
		if (!s.selected) {
			s.selected = true;
			selection.add(s);
		}
	}
	
	public void deSelect(ArtilleryInstance s) {
		if (s.selected) {
			s.selected = false;
			selection.remove(s);
		}
	}
	
	public void clear() {
		for (ArtilleryInstance ins : all()) {
			ins.selected = false;
			ins.hovered = false;
		}
		selection.clearSoft();
	}
	
	public LIST<ArtilleryInstance> selection(){
		all();
		return selection;
	}
	
	public boolean isClear() {
		return selection.size() == 0;
	}

	public void toggle(ArtilleryInstance f) {
		if (f.selected){
			deSelect(f);
		}else
			select(f);
	}
	
	public void clearHover() {
		for (ArtilleryInstance ins : all()) {
			ins.hovered = false;
		}
	}

	
}
