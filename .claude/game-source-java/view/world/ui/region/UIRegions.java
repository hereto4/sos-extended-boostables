package view.world.ui.region;

import game.faction.FACTIONS;
import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.tool.ToolManager;
import world.map.regions.Region;

public final class UIRegions {

	private final Other other;
	final Play player;
	final PlayCapitol cap;
	
	public final ISidePanel playerList;
	public final ISidePanel allList;
	private final ISidePanels panels;
	
	public UIRegions (ISidePanels panels, ToolManager tools) {
		playerList = new ListPlayer(panels);
		allList = new ListAll();
		player = new Play(tools, panels);
		other = new Other(tools, panels);
		cap = new PlayCapitol(tools, panels);
		this.panels = panels;
	}
	
	public void open(Region reg) {
		
		
		panels.add(get(reg), true);
		
		
	}
	
	public ISidePanel get(Region reg) {
		
		if (reg.faction() == FACTIONS.player()) {
			if (reg.capitol())
				return cap.get(reg);
			return player.get(reg);
		}else {
			return other.get(reg);
		}
	}
	
	private RV getp(Region reg) {
		if (reg.faction() == FACTIONS.player()) {
			if (reg.capitol())
				return cap;
			return player;
		}else {
			return other;
		}
	}
	
	public void hover(Region reg, GUI_BOX b) {
		getp(reg).hover((GBox) b, reg);
	}
	
	public void hoverGarrison(Region reg, GUI_BOX b) {
		getp(reg).hoverGarrison((GBox)b, reg);
	}
	
	public void open(Region reg, boolean disturb) {
		
		panels.add(get(reg), disturb);
		
		
	}
	
	public void openPlayerList() {
		panels.add(playerList, true);
	}
	
	public void openOtherList() {
		panels.add(allList, true);
	}
	
	boolean active(Region reg) {
		return cap.added(panels, reg) || player.added(panels, reg) || other.added(panels, reg);
	}
	

}
