package view.battle;

import game.GAME;
import game.battle.Armies;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.text.D;

public final class DivSelection {

	public static CharSequence ¤¤MusterOneProblem = "¤One or more divisions do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	public static CharSequence ¤¤MusterProblem = "¤The division do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	
	static {
		D.ts(DivSelection.class);
	}
	
	private final boolean[] selected = new boolean[Armies.DIVISIONS];
	private final ArrayList<Div> selection = new ArrayList<>(Armies.DIVISIONS);
	private final boolean[] hovered = new boolean[Armies.DIVISIONS];
	private final DivFormationImp tmp = new DivFormationImp();
	
	public void select(Div f) {
		if (selected[f.index()])
			return;
		selection.add(f);
		selected[f.index()] = true;
		
	}
	
	public void deSelect(Div f) {
		if (!selected[f.index()])
			return;
		selection.remove(f);
		selected[f.index()] = false;
	}
	
	public boolean selected(Div f) {
		return selected[f.index()];
	}
	
	public void sToggle(Div f) {
		if (!selected[f.index()])
			select(f);
		else
			deSelect(f);
	}
	
	public void clear() {
		selection.clearSloppy();
		for (int i = 0; i < selected.length; i++) {
			selected[i] = false;
		}
		artillery.clear();
	}
	
	public LIST<Div> selection(){
		return selection;
	}
	
	public int allSelected() {
		return selection.size() + artillery.selection().size();
	}
	
	public boolean isClear() {
		return selection.size() == 0 && artillery.isClear();
	}

	public void toggle(Div f) {
		if (selected(f)){
			deSelect(f);
		}else
			select(f);
	}
	
	public boolean hovered(Div d) {
		return hovered[d.index()];
	}
	
	public void hover(Div d) {
		hovered[d.index()] = true;
	}
	
	public void clearHover() {
		for (int i = 0; i < hovered.length; i++) {
			hovered[i] = false;
		}
		artillery.clearHover();
	}
	
	public final CatSelection artillery = new CatSelection();

	
	public int destinations() {
		int i = 0;
		for (Div d : selection()) {
			if (d.menNrOf() > 0) {
				d.order().dest.get(tmp);
				if (tmp.deployed() > 0)
					i++;
			}
		}
		return i;
	}
	
	public CharSequence musterProblem() {
		int i = selection().size()-destinations();
		if (i > 1) {
			return ¤¤MusterProblem;
		}else if (i > 0) {
			return ¤¤MusterOneProblem;
		}
		return null;
	}
	
	public boolean shouldMuster() {
		for (Div d : GAME.ARMIES().player().divisions()) {
			if (d.menNrOf() > 0 && !d.settings().mustering())
				return true;
		}
		return false;
	}
	
}
