package view.ui.profile;

import game.faction.FACTIONS;
import game.faction.FactionProfileFlusher;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.text.D;
import view.ui.manage.IFullView;

public final class UIProfile extends IFullView{

	private static CharSequence ¤¤Name = "¤Player";
	private static CharSequence ¤¤Save = "¤Save as Default";
	private static CharSequence ¤¤Load = "¤Load Default";
	private static CharSequence ¤¤FName = "¤Faction Name";
	private static CharSequence ¤¤RName = "¤Ruler Name";
	
	static {
		D.ts(UIProfile.class);
	}

	
	public UIProfile(boolean crashThing) {
		super(¤¤Name, FACTIONS.player().banner().BIG);

		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addRelBody(16, DIR.S, section(crashThing));
		
	}
	
	public static GuiSection section(boolean crashThing) {
		
		GuiSection s = new GuiSection();
		s.add(new UIFactionBanner(FACTIONS.player()));
		
		
		
		
		s.addRelBody(16, DIR.N, info(crashThing));
		s.addRelBody(8, DIR.S, loadButts());
		return s;
	}
	
	
	private static GuiSection loadButts() {
		GuiSection s = new GuiSection();
		
		s.add(new GButt.ButtPanel(¤¤Save) {
			@Override
			protected void clickA() {
				FactionProfileFlusher.flush(FACTIONS.player());
			}
		});
		
		s.addRightC(32, new GButt.ButtPanel(¤¤Load) {
			
			@Override
			protected void renAction() {
				activeSet(FactionProfileFlusher.canLoad(FACTIONS.player()));
			}
			
			@Override
			protected void clickA() {
				FactionProfileFlusher.load(FACTIONS.player());
			}
			
		});
	
		return s;
	}
	
	private static GuiSection info(boolean crashThing) {
		GuiSection s = new GuiSection();
		StringInputSprite t = new StringInputSprite(24, UI.FONT().H2) {
			
			@Override
			public Str text() {
				return FACTIONS.player().name;
			}
			
			@Override
			protected void change() {
				if (FACTIONS.player().capitolRegion() != null)
					FACTIONS.player().capitolRegion().info.name().clear().add(text());
				super.change();
			}
			
		};
		GInput in = new GInput(t);
		s.add(new GHeader(¤¤FName));
		s.addRightCAbs(210, in);
		
		t = new StringInputSprite(24, UI.FONT().H2) {

			
			@Override
			public Str text() {
				return FACTIONS.player().rulerName;
			}
		};
		in = new GInput(t);
		s.add(new GHeader(¤¤RName), 0, s.getLastY2()+12);
		s.addRightCAbs(210, in);
		
//		t = new StringInputSprite(24, UI.FONT().H2) {
//			
//			@Override
//			public Str text() {
//				return FACTIONS.player().desc;
//			}
//		};
//		in = new GInput(t);
//		s.add(new GHeader(D.g("desc", "Game Desc.")), 0, s.getLastY2()+12);
//		s.addRightCAbs(210, in);
		
		if (crashThing)
			s.addRelBody(16, DIR.E, new ColorPop().butt());
		
		return s;
	}
	

	
}
