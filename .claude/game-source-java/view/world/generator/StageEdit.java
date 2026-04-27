package view.world.generator;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.text.D;
import view.tool.PLACABLE;
import world.WORLD;

class StageEdit{

	static CharSequence ¤¤name = "Edit terrain";
	static {
		D.ts(StageEdit.class);
	}

	public StageEdit(WorldViewGenerator stages) {
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				WORLD.OVERLAY().landmarks.add();
				super.render(r, ds);
			}
			
		};
		
		PLACABLE first = null;
		for (PLACABLE p : WORLD.TERRAIN().saver().makePlacers(stages.tools)) {
			if (first == null)
				first = p;
			s.addRightC(0, new B(p, stages));
		}
		

		s.addRightC(16, new GButt.ButtPanel(UI.icons().m.ok) {
			@Override
			protected void clickA() {
				stages.set();
			}
		});
		
		GPanel p = new GPanel();
		p.inner().set(s);
		s.add(p);
		s.moveLastToBack();
		s.body().centerIn(C.DIM());
		s.body().moveY1(5);

		stages.dummy.add(s, null, false);
		s.body().moveY1(10);
		
		stages.tools.place(first);
		
	}
	
	private static class B extends GButt.ButtPanel {

		private final PLACABLE p;
		private final WorldViewGenerator stages;
		public B(PLACABLE p, WorldViewGenerator stages) {
			super(p.getIcon());
			this.p = p;
			this.stages = stages;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(p.name());
		}
		
		@Override
		protected void renAction() {
			selectedSet(stages.tools.placer.isActivated() && (stages.tools.placer.getCurrent() == p || stages.tools.placer.getCurrent() == p.getUndo()));
		}
		
		@Override
		protected void clickA() {
			stages.tools.place(p);
		}
		
	}
	
}
