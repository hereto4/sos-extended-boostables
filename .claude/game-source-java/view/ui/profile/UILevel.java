package view.ui.profile;

import game.GAME;
import game.boosting.BOOSTABLE_O;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.UI.UI;
import init.type.POP_CL;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.ui.manage.IFullView;

public final class UILevel extends IFullView{

	private final CLICKABLE.ClickSwitch switcher;
	private final Level level;	
	private final UIBonus bonus;	
	private final Titles titles;	
	
	private static CharSequence ¤¤Name = "¤Status";
	
	static {
		D.ts(UILevel.class);
	}

	
	public UILevel() {
		super(¤¤Name, UI.icons().l.up);


		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addRelBody(8, DIR.S, picker());
		
		int height = HEIGHT-section.body().height()-16;
		
		level = new Level(height);
		GETTER<BOOSTABLE_O> g = new GETTER<BOOSTABLE_O>() {
			
			@Override
			public BOOSTABLE_O get() {
				return POP_CL.clP(null, null);
			}
			
		};
		GETTER<Faction> fff = new GETTER<Faction>() {

			@Override
			public Faction get() {
				return FACTIONS.player();
			}
			
		};
		bonus = new UIBonus(g, fff, height) {

			@Override
			protected boolean is(Boostable bo) {
				return true;
			}
			
		};
		titles = new Titles(height);
		
		switcher = new CLICKABLE.ClickSwitch(level);
		switcher.setD(DIR.N);
		
	
		
		section.addRelBody(16, DIR.S, switcher);
		
	}
	
	private GuiSection picker() {
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new GButt.ButtPanel(GAME.player().level().info.name) {
			@Override
			protected void clickA() {
				switcher.set(level);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == level);
			}
		}.setDim(180, 32).hoverSet(GAME.player().level().info));
		
		s.addRightC(0, new GButt.ButtPanel(FACTIONS.player().titles.info.name) {
			@Override
			protected void clickA() {
				switcher.set(titles);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == titles);
				if (!selectedIs() && !hoveredIs() && FACTIONS.player().titles.hasNew()) {
					bg(COLOR.WHITE202WHITE100);
				}else {
					bgClear();
				}
			}
			
			
		}.setDim(180, 32).hoverSet(FACTIONS.player().titles.info));
		
		s.addRightC(0, new GButt.ButtPanel(Dic.¤¤Boosts) {
			@Override
			protected void clickA() {
				switcher.set(bonus);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == bonus);
			}
		}.setDim(180, 32).hoverTitleSet(Dic.¤¤Boosts));
		
		return s;
	}

//	public void activate() {
//		show(VIEW.inters().manager);
//	}
	
//	@Override
//	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
//		return section.hover(mCoo);
//	}
//
//	@Override
//	protected void mouseClick(MButt button) {
//		if (button == MButt.RIGHT)
//			hide();
//		else if (button == MButt.LEFT)
//			section.click();
//	}
//	
//	@Override
//	protected boolean otherClick(MButt button) {
//		hide();
//		return true;
//	}
//
//	@Override
//	protected void hoverTimer(GBox text) {
//		section.hoverInfoGet(text);
//	}
//
//	@Override
//	protected boolean render(Renderer r, float ds) {
//		section.render(r, ds);
//		return true;
//	}
//
//	@Override
//	protected boolean update(float ds) {
//		// TODO Auto-generated method stub
//		return true;
//	}
	
}
