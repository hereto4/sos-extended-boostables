package view.world.ui.battle;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.battle.spec.WBattleSpec;
import world.map.regions.Region;

final class BattleSally extends Battle{

	private static CharSequence ¤¤name = "The Sally of {0}";
	private static CharSequence ¤¤desc = "Our garrison is about to sally out to break the siege.";
	private static CharSequence ¤¤CommandD = "¤Take personal command and fight this battle on the field. The outcome will depend on your skill of leading men into battle.";

	private static CharSequence ¤¤Retire = "¤Fall Back";
	private static CharSequence ¤¤RetireD = "¤Abort the whole operation. No one will be harmed.";
	static {
		D.ts(BattleSally.class);
	}
	
	private final ACTION close;
	
	BattleSally(ACTION close){
		super(¤¤desc);
		this.close = close;
	}
	
	@Override
	protected CharSequence title(WBattleSpec g) {
		Region reg = WORLD.REGIONS().map.get(g.player.coo());
		Str.TMP.clear().add(¤¤name);
		Str.TMP.insert(0, reg.info.name());
		return Str.TMP;
	}
	
	@Override
	protected RENDEROBJ buttons() {
		
		GuiSection ss = new GuiSection();
		GButt.ButtPanel bb;
		
		bb = new Butt(UI.icons().s.sword, ¤¤Command) {
			@Override
			protected void clickA() {
				close.exe();
				g.engage();
			}
		};
		bb.hoverInfoSet(¤¤CommandD);
		ss.addRightC(0, bb);
		
		bb = new Butt(UI.icons().s.cog, ¤¤AutoResolve) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(¤¤autoD);
				t.insert(0, g.victory ? Dic.¤¤Victory
						: (g.player.losses() >= g.player.men() ? ¤¤Annihilation : Dic.¤¤Defeat));
				t.insert(1, g.player.losses());
				t.insert(2, g.enemy.losses());
				text.add(t);
			}

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (g.victory) {
					OPACITY.O25.bind();
					COLOR.ORANGE100.render(r, body, -4);
					OPACITY.unbind();
				}
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {

				if (super.hover(mCoo)) {
					setCas(false, true);
					return true;
				}
				return false;
			}
			
			@Override
			protected void clickA() {
				close.exe();
				g.auto();
			}
		};
		ss.addRightC(0, bb);

		bb = new Butt(UI.icons().s.arrow_left, ¤¤Retire) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (g.player.lossesRetreat() >= g.player.men()) {
					OPACITY.O25.bind();
					COLOR.RED100.render(r, body, -4);
					OPACITY.unbind();
				}
			}

			@Override
			protected void clickA() {
				if (g.player.lossesRetreat() >= g.player.men()) {
					return;
				}
				close.exe();
				g.retreat();
			}

			@Override
			public boolean hover(COORDINATE mCoo) {

				if (super.hover(mCoo)) {
					setCas(true, false);
					return true;
				}
				return false;
			}
		};
		bb.hoverInfoSet(¤¤RetireD);
		ss.addRightC(0, bb);

		return ss;
	}

	


	
}
