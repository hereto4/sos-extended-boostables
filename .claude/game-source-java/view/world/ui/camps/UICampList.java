package view.world.ui.camps;

import java.util.Comparator;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.C;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.WORLD;
import world.entity.WEntity;
import world.entity.haven.WHaven;
import world.entity.haven.WHavenType;
import world.map.regions.Region;
import world.region.RD;

public final class UICampList extends ISidePanel{

	private final Rec hBody = new Rec(C.TILE_SIZE*2);
	
	private final ArrayListResize<WHaven> al = new ArrayListResize<>(256, 1024*2);
	
	private final Comparator<WHaven> sort = new Comparator<WHaven>() {
		
		@Override
		public int compare(WHaven o1, WHaven o2) {
			return get(o1) - get(o2);
		}
		
		private int get(WHaven current) {
			int res = current.index();
			Faction f = current.faction();
			if (f == FACTIONS.player()) {
				;
			}else if (f == null && WORLD.REGIONS().map.get(current.ctx(), current.cty()) != null && WORLD.REGIONS().map.get(current.ctx(), current.cty()).faction() == FACTIONS.player()) {
				res += 10000;
			}else if (f == null) {
				res += 10000*2;
			}else
				res += 10000*3;
			
			return res;
			
		}
	};

	private final GTableBuilder builder;
	private int upI = -1;
	
	public UICampList() {
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				upI = -1;
			}
		};
		
		GuiSection s = new GuiSection();
		
		for (WHavenType t : WORLD.camps().types) {
			s.addDownC(4, new CampInfo(t));
		}
		
		section.add(s);
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return all().size();
			}
			
			@Override
			public void hover(int index) {
//				Region r = sorter.get(index);
//				if (r != null) {
//					WorldHoverer.hover(VIEW.hoverBox(), r);
//				}
			}
			
			
		};

		builder.column(null, 290, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		s = builder.createHeight(HEIGHT-section.body().height()-C.SG*4, true);
		section.addDown(2, s);

		titleSet(WORLD.camps().info.names);
		
	}
	
	public LIST<WHaven> all() {
		if (upI == -1 || Math.abs(upI-GAME.updateI()) > 200) {
			upI = GAME.updateI();
			al.clearSoft();
			
			for (WEntity e : WORLD.ENTITIES().allSlow()) {
				if (al.hasRoom() && e instanceof WHaven) {
					al.add((WHaven) e);
				}
			}
			al.sort(sort);
		}
		return al;
		
	}
	
	@Override
	protected void update(float ds) {

	}
	
	private final class Button extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			
			add(new SPRITE.Imp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WHaven f = all().get(ier.get());
					f.type().race.appearance().icon.render(r, X1, X2, Y1, Y2);
				}
			},0 ,0);
			
			addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					WHaven f = all().get(ier.get());
					text.setMaxWidth(232);
					text.setMultipleLines(false);
					text.lablify().add(f.name);
					
				}
			});
			
			body().setWidth(276);
			
			pad(4, 2);
			

			
			
		}
		
		@Override
		protected void clickA() {
			WHaven f = all().get(ier.get());
			VIEW.world().window.centererTile.set(f.ctx(), f.cty());
			
//			Region f = sorter.get(ier);
//			VIEW.world().UI.region.openFromList(f, VIEW.world().panels);
			
		}
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GButt.ButtPanel.renderBG(r, true, false, hoveredIs(), body());
			super.render(r, ds);
			WHaven f = all().get(ier.get());
			if (f.faction() != FACTIONS.player()) {
				OPACITY.O66.bind();
				COLOR.BLACK.render(r, body(), -2);
				OPACITY.unbind();
			}
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			if (super.hover(mCoo)) {
				WHaven f = all().get(ier.get());
				hBody.moveC(f.ctx()*C.TILE_SIZE+C.TILE_SIZEH, f.cty()*C.TILE_SIZE+C.TILE_SIZEH);
				WORLD.OVERLAY().things.hover(hBody, GCOLOR.MAP().get(f.faction()), false, 0);
				return true;
			}

			return false;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WHaven f = all().get(ier.get());
			CampInfo.hover(text, f);
		}
		
		
	}
	
	public ISidePanel get(WHaven f) {
		
		upI = -1;
		
		
		return this;

	}

	public void hover(GUI_BOX box, WHaven w) {
		CampInfo.hover(box, w);
	}
	
	
}
