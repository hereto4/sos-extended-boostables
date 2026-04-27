package view.world.ui.battle;

import game.time.TIME;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import world.WORLD;
import world.battle.spec.WBattleSiege;
import world.battle.spec.WBattleSpec;
import world.map.regions.Region;
import world.region.RD;

final class BattleSiege extends Battle{

	private static CharSequence ¤¤name = "Siege of {0}";
	private static CharSequence ¤¤desc = "Our armies are at the walls of an enemy settlement. Its garrison still defiant. What are your orders?";
	
	private static CharSequence ¤¤Wait = "Wait";
	private static CharSequence ¤¤WaitD = "Continue the siege and wait. Eventually the defenders will starve and tire.";
	
	private static CharSequence ¤¤Lift = "¤Lift";
	private static CharSequence ¤¤LiftD = "¤Lift and abort siege.";
	
	private static CharSequence ¤¤BesigeTime = "¤Besiege Time.";
	private static CharSequence ¤¤BesigeTimeD = "¤After a day of siege, the defenders will start dying. After a full year, the defenders should be dead.";
	
	static {
		D.ts(BattleSiege.class);
	}
	
	private final ACTION close;
	private WBattleSiege spec;
	
	BattleSiege(ACTION close){
		super(¤¤desc);
		this.close = close;
	}
	
	@Override
	protected CharSequence title(WBattleSpec g) {
		Region reg = WORLD.REGIONS().map.get(g.enemy.coo());
		if (reg == null)
			reg = WORLD.REGIONS().map.get(g.player.coo());
		Str.TMP.clear().add(¤¤name);
		Str.TMP.insert(0, reg.info.name());
		return Str.TMP;
	}
	
	GuiSection getS(WBattleSiege spec) {
		this.spec = spec;
		return super.get(spec);
	}
	
	@Override
	protected RENDEROBJ buttons() {
		
		GuiSection ss = new GuiSection();
		GButt.ButtPanel bb;
				
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

		bb = new Butt(SPRITES.icons().s.clock, ¤¤Wait){
			
			
			@Override
			protected void clickA() {
				close.exe();
			}

			
		};
		bb.hoverInfoSet(¤¤WaitD);
		ss.addRightC(0, bb);
		
		bb = new Butt(SPRITES.icons().s.arrow_left, ¤¤Lift) {
			
			@Override
			protected void clickA() {
				close.exe();
				spec.retreat();
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
		bb.hoverInfoSet(¤¤LiftD);
		ss.addRightC(0, bb);

		
		GuiSection s = new GuiSection();
		s.addRightC(0, new GStat() {
			
			@Override
			public void update(GText text) {
				
				text.add('x').s();
				GFORMAT.f1(text, spec.fortifications);
			}
			
		}.hh(UI.icons().m.fortification).hoverTitleSet(Dic.¤¤Fort).hoverInfoSet(Dic.¤¤FortD));
		
		s.addRightC(86, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, RD.MILITARY().besigeMul(spec.besiged));
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤BesigeTime);
				b.text(¤¤BesigeTimeD);
				b.NL();
				b.textLL(DicTime.¤¤Days);
				b.tab(6);
				b.add(GFORMAT.fofkInv(b.text(), WORLD.BATTLES().besigedTime(spec.besiged)*TIME.secondsPerDayI(), 18));
				b.NL();
				b.textLL(Dic.¤¤Value);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), RD.MILITARY().besigeMul(spec.besiged)));
			};
			
		}.hh(UI.icons().m.time));
		
		ss.addRelBody(4, DIR.N, s);
		
		return ss;
	}

	


	
}
