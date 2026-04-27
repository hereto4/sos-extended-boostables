package view.ui.profile;

import game.faction.FBanner;
import game.faction.Faction;
import init.sprite.SPRITES;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.common.BitmapSpriteEditor;
import util.gui.misc.GButt;
import util.gui.misc.GColorPicker;
import util.gui.misc.GHeader;
import util.text.D;
import world.WORLD;
import world.map.regions.Region;

public final class UIFactionBanner extends GuiSection{

	private static CharSequence ¤¤Banner = "¤Banner";
	private static CharSequence ¤¤BackGround = "¤Background";
	private static CharSequence ¤¤Foreground = "¤Foreground";
	private static CharSequence ¤¤Pole = "¤Pole";
	private static CharSequence ¤¤Border = "¤Border";
	
	static {
		D.ts(UIFactionBanner.class);
	}

	private final Faction f;
	
	public UIFactionBanner(Faction f) {
		this.f = f;
		add(bannerHeader());
		addRelBody(8, DIR.S, banner());
		addRelBody(16, DIR.E, colors());
	}
		
	private GuiSection banner() {
		return new BitmapSpriteEditor(f.banner().sprite);
	}
	
	private GuiSection bannerHeader() {
		final FBanner b = f.banner();
		GuiSection s = new GuiSection();
		s.add(b.HUGE, 0, 0);
		s.addRightC(8, b.BIG);
		s.addRightC(8, b.MEDIUM);
		
		s.addRightC(20, new GHeader(¤¤Banner));
		s.addRightC(16, new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				b.bannerTypeSet(b.bannerType()-1);
			}
		});
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				b.bannerTypeSet(b.bannerType()+1);
			}
		});
		return s;
	}
	
	private GuiSection colors() {
		final FBanner b = f.banner();
		GuiSection s = new GuiSection();
		s.add(new GColorPicker(false, ¤¤BackGround) {
			
			@Override
			public ColorImp color() {
				
				return b.colorBG();
			}
			
			@Override
			public void change() {
				for (Region r : f.realm().all())
					WORLD.MINIMAP().updateRegion(r);
			}
		});
		s.addDownC(8, new GColorPicker(false, ¤¤Foreground) {
			
			@Override
			public ColorImp color() {
				return b.colorFG();
			}
		});
		s.addDownC(8, new GColorPicker(false, ¤¤Border) {
			
			@Override
			public ColorImp color() {
				return b.colorBorder();
			}
		});
		s.addDownC(8, new GColorPicker(false, ¤¤Pole) {
			
			@Override
			public ColorImp color() {
				return b.colorPole();
			}
		});
		return s;
	}
	
}
