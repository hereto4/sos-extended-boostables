package view.sett.ui.room.copy;

import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LISTE;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.Dic;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.tool.ToolConfig;

final class FirstConfig implements ToolConfig{

	private final GuiSection section = new GuiSection();
	private final ON_TOP_RENDERABLE top;
	private final GPanel p = new GPanel();
	private final CLICKABLE butt;
	private final SecondConfig sConfig;
	
	final ACTION exit = new ACTION() {
		
		@Override
		public void exe() {
			VIEW.s().tools.placer.deactivate();
		}
	};
	
	
	FirstConfig(Source source, Second second, First first){
		sConfig = new SecondConfig(source, first, this);
		butt = new GButt.Panel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				VIEW.s().tools.place(second, sConfig);
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
						GCOLOR.MAP().BEST_DARK.bind();
						SPRITES.cons().BIG.dashed.render(r, m, it.x(), it.y());
					}
					it.next();
				}
				top.remove();
				COLOR.unbind();
			}
		};
	}
	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis){
		section.clear();
		
		VIEW.s().tools.placer.stealButtons(section);
		
		section.pad(20, 0);
		
		section.addRelBody(4, DIR.S, butt);
		
		section.body().centerX(C.DIM());
		
		
		
		p.setButt();
		p.inner().set(section);
		
		
		p.setCloseAction(exit);
		p.setTitle(Dic.¤¤Copy);
		section.add(p);
		section.moveLastToBack();
		section.body().moveY1(75);
		section.body().centerX(C.DIM());

		uis.add(section);
	}
	
	@Override
	public void update(boolean UIHovered) {
		top.add();
	}
	
}
