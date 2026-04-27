package game.battle.thread.position;

import game.GAME;
import game.battle.div.Div;
import init.constant.C;
import init.sprite.UI.UI;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

class Tests {

	Tests(DivCentres status){
		ON_TOP_RENDERABLE top = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				
				for (Div d : GAME.ARMIES().divisions()) {
					
					DivCentre s = status.centre(d);
					if (s.cx == -1)
						continue;
					int x = s.cxSoft;
					int y = s.cySoft;
					
					x = data.transformGX(x);
					y = data.transformGY(y);
					UI.icons().s.alert.renderCScaled(r, x, y, C.SCALE*2);
					UI.FONT().S.renderC(r, x, y+16*C.SCALE*2, Str.TMP.clear().add(s.inFormation()), C.SCALE*2);
				}
				
			}
		};
		
		IDebugPanel.add("battle div centres", new ACTION() {
			
			@Override
			public void exe() {
				top.add();
			}
		});
		
	}
	
}
