package view.sett.ui.bottom;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import snake2d.KEYCODES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.tool.PLACABLE;

public final class UIBuildPanel extends Interrupter{
	
	private final GuiSection section = new GuiSection();
	private SearchToolPanel searchPanel; 
	private BuildMain main;
	private final UIRoomPlacer placer;
	
	public UIBuildPanel(UIRoomPlacer placer, InterManager m) {
		pin();
		m.add(this);
		this.placer = placer;
		SearchToolPanel.all = new LinkedList<>();
		SETT.addGeneratorHook(new ACTION() {
			
			@Override
			public void exe() {
				SearchToolPanel.all = new LinkedList<>();
				create();
			}
		});
		create();
		
	
		
	}
	
	private void create() {
		section.clear();
		section.add(SPRITES.specials().lowerPanel(), 0, 0);
		
		section.body().centerX(0, C.WIDTH());
		section.body().moveY2(C.HEIGHT());
		
		GuiSection s = new GuiSection();
		D.gInit(this);


		Inter inter = new Inter();

		{
			CLICKABLE c = new GButt.ButtPanel(new SPRITE.Wrap(SPRITES.icons().m.search, 32, 32));
			ACTION sa = new ACTION() {
				
				@Override
				public void exe() {
					searchPanel.open(c, inter);
					
				}
			};
			c.clickActionSet(sa);
			CLICKABLE cc = KeyButt.wrap(sa, c, KEYS.SETT(), "toolSearch", D.g("Search"), D.g("SearchD", "Search for tools and rooms"), KEYCODES.KEY_LEFT_CONTROL, KEYCODES.KEY_F);
			s.addRightC(8, cc);
		}
		main = new BuildMain(inter, placer);
		s.addRightC(8, main.create());
		
		{
			Options sec = new Options();
			

			CLICKABLE c = new GButt.ButtPanel(new SPRITE.Wrap(SPRITES.icons().m.cog_big, 32, 32)) {
				
				@Override
				protected void clickA() {
					inter.set(this, sec);
				}
				
				@Override
				protected void renAction() {
					selectedSet(SETT.JOBS().planMode.is());
				}
				
			};   
			c.hoverTitleSet(Dic.¤¤Tools);
			s.addRightC(0, c);
		}
		
		{
			Delete delete = new Delete();
			CLICKABLE c = new GButt.ButtPanel(new SPRITE.Wrap(SPRITES.icons().m.cancel, 32, 32)) {
				
				@Override
				protected void clickA() {
					inter.set(this, delete);
				}
				
			}; 
			c.hoverTitleSet(Dic.¤¤delete);
			s.addRightC(0, c);
		}
	
		
		s.body().centerIn(section);
		s.body().incrY(4);
		section.add(s);
		
		searchPanel = new SearchToolPanel();
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		section.render(r, ds);
		
		return true;
	}
	
	
	public void hilight(String key) {
		main.hilight(key);
	}
	
	

	@Override
	protected boolean update(float ds) {
		return true;
	}
	
	protected static class Butt extends GButt.ButtPanel {
		
		private final PLACABLE p;
		
		Butt(PLACABLE p){
			super(p.getIcon());
			this.p = p;
		}
		
		Butt(PLACABLE p, Icon icon){
			super(p.getIcon());
			this.p = p;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			p.hoverDesc((GBox)text);
		}
		
		@Override
		protected void clickA() {
			VIEW.s().tools.place(p);
		}
		
		@Override
		protected void renAction() {
			selectedSet(p == VIEW.s().tools.placer.getCurrent());
		}
		
	}
	

	
}
