package view.sett.ui.room.copy;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LISTE;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.Dic;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.tool.ToolConfig;

final class SecondConfig implements ToolConfig{

	private final GuiSection section = new GuiSection();
	private final ON_TOP_RENDERABLE top;
	private final GPanel p = new GPanel();
	private final RENDEROBJ butts;
	private final CLICKABLE butt;
	private final First first;
	private final FirstConfig fConfig;
	ACTION exit = new ACTION() {

		@Override
		public void exe() {
			VIEW.s().tools.placer.deactivate();
		}
		
	};
	
	
	SecondConfig(Source source, First first, FirstConfig fConfig){
		this.first = first;
		this.fConfig = fConfig;
		butt = new GButt.Panel(SPRITES.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				VIEW.s().tools.place(first, fConfig);
			}
			
			@Override
			protected void renAction() {
				activeSet(false);
				for (COORDINATE c : source.area()) {
					if (source.is(c)) {
						activeSet(true);
						return;
					}
				}
			}
		};
		
		butts = new RENDEROBJ.RenderImp(32, 32) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				UI.PANEL().butt.render(r, body, 0);
			}
		};
		
		top = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					if (source.is(it.tile())) {
						int m = 0;
						for (DIR d : DIR.ORTHO) {
							if (source.is(it.tx(),  it.ty(), d))
								m |= d.mask();
						}
						SPRITES.cons().BIG.dashed.render(r, m, it.x(), it.y());
					}
					it.next();
				}
				top.remove();
				
			}
		};
	}
	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis){
		section.clear();
		
		VIEW.s().tools.placer.stealButtons(section);
		
		section.pad(20, 0);
		
		section.addRelBody(4, DIR.S, butts);
		butt.body().centerIn(butts);
		section.add(butt);
		
		section.body().centerX(C.DIM());
		
		
		
		p.setButt();
		p.inner().set(section);
		
		
		
		p.setCloseAction(exit);
		p.setTitle(Dic.¤¤Copy);
		section.add(p);
		section.moveLastToBack();
		section.body().moveY1(100);
		section.body().centerX(C.DIM());

		uis.add(section);
	}
	
	@Override
	public void update(boolean UIHovered) {
		top.add();
	}
	
	@Override
	public boolean back() {
		VIEW.s().tools.place(first, fConfig);
		return false;
	}
	
}
