package view.menu;

import game.save.GameLoader;
import game.save.SaveFile;
import init.paths.PATHS;
import init.sprite.SPRITES;
import snake2d.util.gui.GuiSection;
import util.colors.GCOLOR;

class IMenuLoad extends GuiSection{

	final MenuScreenLoad m;
	
	IMenuLoad(IMenu m) {
		this.m = new MenuScreenLoad(MenuScreenLoad.¤¤name, GCOLOR.T().H1, true, PATHS.local().save()) {
			
			@Override
			protected void load(SaveFile f) {
				SPRITES.loader().printempty();
				new GameLoader(f.path).set();
			}
			
			@Override
			protected void back() {
				m.setMain();
				
			}
		};
		add(this.m);
		
		
	}

	public void init() {
		m.populateSaves();
		
	}
	
	public void setOther() {
		
	}
	
}
