package game.battle.thread.status;

import game.GAME;
import game.battle.div.Div;
import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

class Tests {

	Tests(BattleStatus status){
		ON_TOP_RENDERABLE top = new ON_TOP_RENDERABLE() {
			
			private final ArrayList<Div> res = new ArrayList<>(16);
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				RenderData.RenderIterator it = data.onScreenTiles();
				
				DivsTileMap m = BattleStatus.map();
				
				while(it.has()) {
					
					res.clear();
					int ai = 0;
					for (Div d : m.get(res, it.tx(), it.ty())){
						if (d.army() == GAME.ARMIES().player()) {
							GCOLOR.MAP().BEST.bind();
							
						}else {
							GCOLOR.MAP().BAD.bind();
						}
						SPRITES.icons().s.dot.renderC(r, it.x()+(ai%4)*16, it.y()+ (ai/4)*16);
						ai++;
						
					}
					
					
					
					it.next();
					
				}
				
				COLOR.unbind();
				
				
			}
		};
		
		IDebugPanel.add("battle status map", new ACTION() {
			
			@Override
			public void exe() {
				top.add();
			}
		});
		
	}
	
}
