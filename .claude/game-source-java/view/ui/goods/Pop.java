package view.ui.goods;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;

class Pop extends GuiSection {
	
	RESOURCE res;
	private ArrayList<Region> regs = new ArrayList<Region>(128);
	
	Pop(){
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return regs.size();
			}
		};
		
		bu.column(null, 250, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GuiSection s = new GuiSection() {
					@Override
					protected void clickA() {
						Region r = regs.get(ier.get());
						if (r != null) {
							VIEW.UI().manager.close();
							VIEW.world().activate();
							VIEW.world().UI.regions.open(r);
						}
						
						super.clickA();
					}
				};
				s.add(new GStat() {
					
					@Override
					public void update(GText text) {
						Region r = regs.get(ier.get());
						if (r != null) {
							text.add(r.info.name());
						}
					}
				}.r());
				
				s.addRightC(180, new GStat() {
					
					@Override
					public void update(GText text) {
						Region r = regs.get(ier.get());
						if (r != null) {
							GFORMAT.iIncr(text, RD.OUTPUT().get(res).getDelivery(r));
						}
					}
				}.r());
				s.pad(0, 6);
				
				return s;
				
			}
		});
		
		add(bu.createHeight(400, true));
	}
	
	@Override
	public void render(SPRITE_RENDERER ren, float ds) {
		regs.clear();
		for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
			Region r = FACTIONS.player().realm().region(i);
			if (RD.OUTPUT().get(res).getDelivery(r) > 0) {
				regs.add(r);
			}
		}
		
		super.render(ren, ds);
	}
	
}
