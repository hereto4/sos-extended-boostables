package view.world.ui.faction;

import game.faction.npc.FactionNPC;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.gui.common.UIPickerArmy;
import util.gui.common.UIPickerRegion;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.regions.Region;

final class Realm extends GuiSection{
	
	Realm(GETTER_IMP<FactionNPC> f, int height){
		
		GuiSection s = new GuiSection();
		s.addRelBody(8, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)AD.men(null).faction(f.get()));
			}
			
			
			
		}.hh(Dic.¤¤Soldiers));
		
		s.addRelBody(8, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)AD.conscripts().total(null).get(f.get()));
			}
			
		}.hh(Dic.¤¤Conscripts));
		s.addRelBody(8, DIR.S, new UIPickerArmy(f, height-body().height()-16) {

			@Override
			protected boolean canBePicked(WArmy a) {
				return !WORLD.FOW().is(a.ctx(), a.cty());
			}

			@Override
			protected void pick(WArmy a) {
				if (!WORLD.FOW().is(a.ctx(), a.cty())) {
					VIEW.world().activate();
					VIEW.UI().manager.close();
					VIEW.world().window.centererTile.set(a.ctx(), a.cty());
				}
			}
			
			@Override
			public void hover(GUI_BOX text, WArmy a) {
				if (!WORLD.FOW().is(a.ctx(), a.cty()))
					super.hover(text, a);				
			}
			
		});
		
		add(s);
		
		s = new GuiSection();
		addRelBody(16, DIR.E, new RENDEROBJ.RenderImp(1, height) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body);
			}
		});
		
		
		s.addRelBody(8, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (Region r : f.get().realm().all())
					am += r.info.area();
				GFORMAT.i(text, am);
			}
			
		}.hh(Dic.¤¤Area));
		s.addRelBody(8, DIR.S, new UIPickerRegion(f, height-16) {

			@Override
			protected void toggle(Region reg) {
				VIEW.world().activate();
				VIEW.UI().manager.close();
				VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
				VIEW.world().UI.regions.open(reg);
			}
			
			
			
			
			
		});
		addRelBody(16, DIR.E, s);
		

	}

	
	
}
