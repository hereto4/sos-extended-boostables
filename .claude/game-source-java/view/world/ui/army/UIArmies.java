package view.world.ui.army;

import snake2d.util.gui.GUI_BOX;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.entity.army.WArmy;

public final class UIArmies {

	private final List list = new List();
	final Army army = new Army();
	private final Hoverer hoverer = new Hoverer();
	
	public void openList(WArmy f) {
		openList(f, VIEW.world().panels);
	}
	
	public void open(WArmy f) {
		VIEW.world().panels.add(army.get(f), true);
	}
	
	public void openList(WArmy f, ISidePanels m) {
		
		m.add(list, true);
		if (f != null) {
			list.set(f);
			m.add(army.get(f), false);
			VIEW.world().window.centerer.set(f.body().cX(), f.body().cY());
		}else {
			m.add(list, true);
		}
			
	}
	
	public boolean listIsOpen(ISidePanels m) {
		return m.added(list);
	}
	
	public void close(ISidePanels m) {
		m.remove(list);
	}
	
	public void hover(GUI_BOX box, WArmy a) {
		hoverer.hover(box, a);
	}
	
}
