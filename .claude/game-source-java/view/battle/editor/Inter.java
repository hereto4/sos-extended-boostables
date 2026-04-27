package view.battle.editor;

import init.constant.C;
import init.constant.Config;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.rnd.RND;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.WORLD;

class Inter extends Interrupter{


	public ArmySide player = new ArmySide(); 
	public ArmySide enemy = new ArmySide(); 
	
	private Army current = new Army(player, enemy);
	private Placer placer = new Placer(player, enemy);
	

	public Inter(InterManager m) {
		pin();
		persistantSet();
		show(m);
		
		double pow = Config.battle().MEN_PER_DIVISION*Config.battle().DIVISIONS_PER_ARMY*(0.1+RND.rFloat()*1.2);
		
		player.generate(pow);
		enemy.generate(pow);
		
		{
			GuiSection buttons = new GuiSection();
			
			buttons.add(new GButt.ButtPanel(Dic.¤¤OK) {
				@Override
				protected void clickA() {
					VIEW.b().editor.tools.place(placer);
					if (!WORLD.GEN().hasGeneratedTerrain) {
						placer.generate.exe();
					}
					
				}
				
				@Override
				protected void renAction() {
					activeSet(player.divs.size() > 0 && enemy.divs.size() > 0);
				}
				
			});
			
			buttons.addRelBody(8, DIR.E, new GButt.ButtPanel(Dic.¤¤Clear) {
				@Override
				protected void clickA() {
					player.clear();
					enemy.clear();
					
				}
				
			});
			
			
			current.addRelBody(8, DIR.S, buttons);
		}
		
		
		
		GPanel pan = new GPanel();
		pan.setBig();
		pan.inner().setDim(current.body().width(), current.body().height());
		pan.body.centerIn(current);
		pan.setTitle(Army.¤¤name);
		current.add(pan);
		current.moveLastToBack();
		current.body().moveCY(C.HEIGHT()/2);
		current.body().moveCX(C.WIDTH()/2);

	}
	

	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (!VIEW.b().editor.tools.placer.isActivated())
			return current.hover(mCoo);
		return false;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (!VIEW.b().editor.tools.placer.isActivated() && button == MButt.LEFT)
			current.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		if (!VIEW.b().editor.tools.placer.isActivated())
			current.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if (!VIEW.b().editor.tools.placer.isActivated())
			current.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		VIEW.b().editor.tools.placer.isActivated();
		return false;
	}

}
