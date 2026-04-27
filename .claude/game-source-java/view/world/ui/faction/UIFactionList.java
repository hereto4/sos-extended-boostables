package view.world.ui.faction;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.WORLD;
import world.region.RD;

final class UIFactionList extends ISidePanel{

	private final ArrayList<FactionNPC> sorted = new ArrayList<>(FACTIONS.MAX());
	
	public static int ROW_HEIGHT = 30;
	private final int width = C.SG*264;
	
	private final StringInputSprite filter = new StringInputSprite(20, UI.FONT().S);
	private final GTableBuilder builder;
	private Faction hovered;

	private final Tree<FactionNPC> sorter = new Tree<FactionNPC>(FACTIONS.MAX()) {

		@Override
		protected boolean isGreaterThan(FactionNPC current, FactionNPC cmp) {
			return value(current) > value(cmp);
		}
		
		private double value(Faction f) {
			double d = 1.0-1.0/RD.DIST().distance(f);
			if (DIP.WAR().is(FACTIONS.player(), f))
				return 0+d;
			if (DIP.get(FACTIONS.player(), f).trades)
				return FACTIONS.MAX()+d;
			if (RD.DIST().reachable(f))
				return FACTIONS.MAX()*2+d;
			if (RD.DIST().factionHasRegionBorderingPlayer(f))
				return FACTIONS.MAX()*3+d;
			return FACTIONS.MAX()*4+d;
		}
		
	};
	
	
	UIFactionList(int HEIGHT) {
		
		this.section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				sorted.clear();
				for (FactionNPC f : FACTIONS.NPCs())
					sorter.add(f);
				while(sorter.hasMore()) {
					FactionNPC f = sorter.pollSmallest();
					if (filter.text().length() > 0) {
						if (f.name.containsText(filter.text()))
							sorted.add(f);
					}else
						sorted.add(f);
				}
				WORLD.OVERLAY().factions.add();
				
				super.render(r, ds);
				hovered = null;
			}
		};

		filter.placeHolder(Dic.¤¤Search);
		
		GInput in = new GInput(filter);
		
		section.addDownC(8, in);
		
		
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorted.size();
			}			
		};
		
		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		section.addDownC(8, builder.createHeight(HEIGHT-16-section.body().height(), false));
		
	}
	

	
	
	private final class Button extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			
			RENDEROBJ o;
			
			o = new RENDEROBJ.RenderImp(Icon.L*2+16, Icon.L*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					FactionNPC f = g();
					if (f == null)
						return;
					f.banner().HUGE.render(r,  body().x1(), body().y1());
					Royalty ro = f.court().king().roy();
					int x1 = body().x1()+Icon.L+Icon.L/2;
					int y1 = body().y1()+8;
					STATS.APPEARANCE().portraitRender(r, ro.induvidual, x1, y1, 1);
					ro.induvidual.race().appearance().crown.crowns().get(0).renderScaled(r, x1, y1+8, 1);
				}
			};

			add(o);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f != null)
						text.lablifySub().add(f.name);
				}
			}.r(DIR.NW);
			add(o, getLastX2()+12, 4);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f == null)
						return;
					
					int am = RD.RACES().population.faction().get(f);
					GFORMAT.i(text, am);
				}
			}.hh(SPRITES.icons().s.human);
			add(o, getLastX1(), getLastY2()+4);
			addRightC(55, new GStat() {
				
				@Override
				public void update(GText text) {
					FactionNPC f = g();
					if (f == null)
						return;
					GFORMAT.i(text, FACTIONS.player().emissaries.spent(f));
				}
			}.hh(UI.icons().s.flags));
			
			
			add(new SPRITE.Imp(100, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					FactionNPC f = g();
					if (f == null)
						return;
					double c = 0.5 + ROPINIONS.peaceValue(f.court().king().roy())/8.0;
					GMeter.renderC(r, c, c, X1, X2, Y1, Y2);// TODO Auto-generated method stub
					
				}
			}, o.body().x1(), getLastY2()+1);
			
			
			add(GMeter.sprite(GMeter.C_ORANGE, new DOUBLE() {

				@Override
				public double getD() {
					FactionNPC f = g();
					if (f == null)
						return 0;
					return RD.RACES().population.faction().get(f)/(10*RD.RACES().maxPop());
				}
				
			}, 100, 12), getLastX1(), getLastY2()+1);
			
			pad(8, 6);
			body().setWidth(width);
			
			add(new GButt.ButtPanel(UI.icons().s.crossheir) {
				@Override
				protected void clickA() {
					FactionNPC f = g();
					VIEW.world().window.centererTile.set(f.cx(), f.cy());
				}
			}, body().x2()-32, body().y2()-32);
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			boolean hovered = hoveredIs();
			FactionNPC f = g();
			boolean selected = VIEW.world().UI.factions.openIs(f);
			boolean active = f.capitolRegion() != null;
			
			hovered |= UIFactionList.this.hovered == f;
			
			if (hovered || selected) {
				WORLD.MINIMAP().hilight(f);
				WORLD.OVERLAY().hover(f.capitolRegion());
			}
			
			GButt.ButtPanel.renderBG(r, active, selected, hovered, body());
			
			if (DIP.WAR().is(FACTIONS.player(), f)) {
				OPACITY.O25.bind();
				COLOR.RED100.render(r, body(),-4);
				OPACITY.unbind();
			}
			
			super.render(r, ds);
			
			DIP.get(f).icon.render(r, body().x2()-18, body().y1()+2);
			if (!RD.DIST().reachable(f)) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body(),-4);
				OPACITY.unbind();
			}
			
			if (ROPINIONS.isPoisoning(f)) {
				GCOLOR.UI().BAD.hovered.bind();
				OPACITY.O75TO100.bind();
				UI.icons().s.angry.render(r, body().x2()-18-16, body().y1()+2);
				COLOR.unbind();
				OPACITY.unbind();
				
			}
			
			GButt.ButtPanel.renderFrame(r, body());
			
			
		}
		
		@Override
		protected void clickA() {
			
			open(g(), false);
		
		}
		
		private FactionNPC g() {
			return sorted.get(ier.get());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs())
				VIEW.world().UI.factions.hover(text, g());
			
		}
		
	}
	
	void open(FactionNPC f, boolean shove) {

		if (f != null) {
			if (shove)
				builder.set(sorted.indexOf(f));
			VIEW.world().UI.factions.open(f);
//			VIEW.world().UI.faction.open(f);
//			VIEW.world().window.centererTile.set(f.capitolRegion().cx(), f.capitolRegion().cy());
		}

	}
	
	
	void hover(Faction f) {
		hovered = f;
	}
	
}
