package view.world.ui.faction;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.Boostable;
import game.faction.Faction;
import game.faction.diplomacy.deal.Deal;
import game.faction.npc.FactionNPC;
import init.settings.S;
import init.value.GVALUES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GButt;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.ui.profile.UIBonus;
import view.ui.util.UIValues;

final class UIFaction extends ISidePanel{

	private final GETTER_IMP<FactionNPC> f;
	private final CLICKABLE.ClickSwitch sw;
	private final UIDiplomacy dip; 
	
	UIFaction(GETTER_IMP<FactionNPC> f, Deal deal, final int WIDTH, final int HEIGHT){

		this.f = f;
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (f.get() == null)
					return;
				if (!f.get().isActive()) {
					f.set(null);
					return;
				}
				super.render(r, ds);
			}
			
		};
		
		section.body().setWidth(WIDTH).setHeight(1);
		section.addRelBody(0, DIR.S, new Banner(this.f, WIDTH));
		
		
		

		
		GuiSection butts = new GuiSection();
		
		
		
		{
			CLICKABLE c = new Court(f, WIDTH, HEIGHT-40);
			sw = new CLICKABLE.ClickSwitch(c);
			sw.setD(DIR.N);
			butts.addRightC(0, sb(Dic.¤¤Court, c));
					
		}
		int hi = HEIGHT - section.body().height()-butts.body().height()-24;
		dip = new UIDiplomacy(f, deal, hi);
		butts.addRightC(0, sb(Dic.¤¤Realm, new Realm(f, hi)));
		butts.addRightC(0, sb(Dic.¤¤goods, new Goods(f, hi)));
		
		GETTER<BOOSTABLE_O> bbb = new GETTER<BOOSTABLE_O>() {

			@Override
			public BOOSTABLE_O get() {
				return f.get();
			}
			
		};
		GETTER<Faction> fff = new GETTER<Faction>() {

			@Override
			public Faction get() {
				return f.get();
			}
			
		};
		butts.addRightC(0, sb(Dic.¤¤Boosts, new UIBonus(bbb, fff, hi) {

			@Override
			protected boolean is(Boostable bo) {
				return bo.cat == BOOSTABLES.BATTLE() || bo.cat == BOOSTABLES.ROOMS();
			}
			
		}));
		
		
		butts.addRightC(0, sb(Dic.¤¤Diplomacy, dip));
		if (S.get().developer) {
			butts.addRightC(0, UIValues.butt(GVALUES.FACTION, fff));
		}
		
		section.addRelBody(8, DIR.S, butts);
		section.addRelBody(0, DIR.S, new RENDEROBJ.RenderImp(WIDTH-128, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border(r, body().x1(), body().x2(), body().y1()+5, body().y1()+8);
			}
		});
		section.addRelBody(0, DIR.S, sw);
		section.body().setWidth(WIDTH).setHeight(HEIGHT);
	}
	
	public void dip() {
		sw.set(dip);
	}
	
	public boolean dipIS() {
		return sw.current() == dip;
	}

	private CLICKABLE sb(CharSequence name, CLICKABLE s) {
		
		GButt.ButtPanel b = new GButt.ButtPanel(name) {
			
			@Override
			protected void clickA() {
				sw.set(s);
			}
			
			@Override
			protected void renAction() {
				selectedSet(sw.current() == s);
			}
			
		};
		
		b.body.setWidth(140);
		return b;
	}
	
	public Faction f() {
		return f.get();
	}
}
