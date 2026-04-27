package settlement.job;

import game.save.PROP;
import init.constant.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import view.main.VIEW;
import view.tool.ToolConfig;

final class JobComboPlacer implements ToolConfig{

	protected GuiSection section;
	private final GPanel panel = new GPanel();
	private GuiSection full = new GuiSection();
	private Job place;
	private final String selectKey;
	private final LIST<? extends Job> jobs;
	
	ACTION exit = new ACTION() {
		
		@Override
		public void exe() {
			VIEW.s().tools.placer.deactivate();
		}
	};
	
	public ToolConfig get(Job j) {
		place = j;
		return this;
	}
	
	public Job current() {
		
		
		
		if (place == null || place.lockText() != null) {
			int i = PROP.propI(this.selectKey, 0);
			i = CLAMP.i(i, 0, jobs.size()-1);
			place = jobs.get(i);
			if (place == null || place.lockText() != null) {
				int ii = 0;
				for (Job jj : jobs) {
					ii++;
					if (jj.lockText() == null) {
						this.place = jj;
						PROP.propISet(selectKey, ii);
					}
				}
			}
		}
		return place;
	}
	
	JobComboPlacer(LIST<? extends Job> jobs, String ss){
		this.selectKey = "JOB_SELECTION_" + ss;
		this.jobs = jobs;
		this.section = new GuiSection();
		
		int in = 0;
		for (Job j : jobs) {
			
			final int inn = in;
			in++;
			
			GButt.ButtPanel b = new GButt.ButtPanel(j.placer().getIcon()) {
				
				@Override
				protected void clickA() {
					if (j.lockText() == null) {
						place = j;
						PROP.propISet(selectKey, inn);
						VIEW.s().tools.place(j.placer(), JobComboPlacer.this);
					}
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					j.placer().hoverDesc(b);
					if (j.lockText() != null) {
						b.NL(8);
						b.error(j.lockText());
					}
				}
				
				@Override
				protected void renAction() {
					
					selectedSet(VIEW.s().tools.placer.getCurrent() == j.placer());
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					super.render(r, ds, isActive, isSelected, isHovered);
					if (j.lockText() != null) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body);
						OPACITY.unbind();
					}
				}
				
			};
			
			section.addRightC(0, b);
		}
		
		int i = PROP.propI(this.selectKey, 0);
		i = CLAMP.i(i, 0, jobs.size()-1);
		place = jobs.get(i);
		
		
		for (Job jj : jobs)
			if (jj.lockText() == null)
				this.place = jj;
	}
	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis) {
		full.clear();
		
		VIEW.s().tools.placer.stealButtons(full);
		if (place.placer().getAdditionalButt() != null)
			for (CLICKABLE p : place.placer().getAdditionalButt())
				full.addRightC(0, p);
		full.body().centerX(C.DIM());
		full.addRelBody(C.SG*8, DIR.N, section);
		
		panel.setButt();
		panel.inner().set(full);
		panel.clickActionSet(exit);
		full.add(panel);
		full.moveLastToBack();
		full.body().moveY1(90);
		uis.add(full);
	}
	
}
