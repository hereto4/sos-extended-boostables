package view.world.generator;

import static world.WORLD.MINIMAP;

import init.constant.C;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.world.generator.tools.UIWorldGenerateTerrain;
import world.WORLD;

class StageTerrain{

	static CharSequence ¤¤title = "Generate Terrain";
	static CharSequence ¤¤warning = "Regenerating terrain will reset your current world. Proceed?";
	
	static {
		D.ts(StageTerrain.class);
	}
	
	public StageTerrain(WorldViewGenerator stages) {
		
		stages.reset();
		GuiSection s = new UIWorldGenerateTerrain(WORLD.GEN());
		s.body().centerIn(C.DIM());
		
		GuiSection ss = new GuiSection();
		
		ACTION generate = new ACTION() {
			
			@Override
			public void exe() {
				if (WORLD.GEN().playerX != -1) {
					StageCapitol.clear();
				}
				
				stages.reset();
				
				
				WORLD.TERRAIN().saver().generate(WorldViewGenerator.loadPrint);
				WORLD.LANDMARKS().saver().generate(WorldViewGenerator.loadPrint);
				WorldViewGenerator.loadPrint.exe();
				MINIMAP().repaint();
				WorldViewGenerator.loadPrint.exe();
				
				WORLD.GEN().hasGeneratedTerrain = true;
				stages.set();
			}
		};
		
		ss.addRightC(2, new GButt.ButtPanel(WorldViewGenerator.¤¤generate) {
			@Override
			protected void clickA() {
				if (WORLD.GEN().playerX != -1) {
					VIEW.inters().yesNo.activate(¤¤warning, generate, ACTION.NOP, true);
				}else {
					generate.exe();
				}
			}
			
		});
		if (WORLD.GEN().hasGeneratedTerrain) {
			ss.addRightC(2, new GButt.ButtPanel(Dic.¤¤cancel) {
				@Override
				protected void clickA() {
					stages.set();
				}
				
			});
		}
		
		s.addRelBody(8, DIR.S, ss);
		
		stages.dummy.add(s, UIWorldGenerateTerrain.¤¤MapType);

		
	}
	
}
