package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;
import view.world.ui.WorldHoverer;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;

final class List extends ISidePanel{

	private Faction f = FACTIONS.player();
	private static int width = 200;
	private final GTableBuilder builder;
	
	public List() {
		titleSet(Dic.¤¤Armies);
		
		{
			int ww = 200;
			
			section.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, AD.conscripts().available(null).get(f));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤Conscriptable);
					b.text(Dic.¤¤ConscriptsD);
					b.NL(8);
					
					for (Race r : RACES.all()) {
						if (r.population().max == 0)
							continue;
						b.add(r.appearance().icon);
						b.add(GFORMAT.iIncr(b.text(), AD.conscripts().available(r).get(f)));
						b.NL();
					}
					
				};
				
			}.hh(Dic.¤¤Conscriptable, ww));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					
					int am = 0;
					for (ResSupply  res : RESOURCES.SUP().ALL) {
						am += SETT.ROOMS().SUPPLY.tally.amount.total(res.resource);
					}
					
					GFORMAT.iIncr(text, am);
				}
				
				RBITImp rs = new RBITImp();
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤Supplies);
					b.text(Dic.¤¤SuppliesD);
					b.sep();
					
					
					b.tab(1);
					b.textLL(Dic.¤¤Current);
					b.tab(4);
					b.textLL(Dic.¤¤Needed);
					b.tab(7);
					b.textLL(Dic.¤¤Consumed);
					b.tab(10);
					b.textLL(Dic.¤¤Available);
					b.NL();
					
					rs.clear();
					for (ResSupply supp : RESOURCES.SUP().ALL) {
						b.add(supp.resource.icon());
						ADSupply sup = AD.supplies().get(supp);
						b.tab(1);
						b.add(GFORMAT.i(b.text(), sup.current().faction(f)));
						b.tab(4);
						b.add(GFORMAT.i(b.text(), sup.targetAmount(f)));
						b.tab(7);
						b.add(GFORMAT.f0(b.text(), -sup.consumedPerDayCurrent(f)));
						b.tab(10);
						b.add(GFORMAT.i(b.text(), SETT.ROOMS().SUPPLY.tally.amount.total(supp.resource)));
						b.NL();
					}
					b.NL(8);
					
					
				};
				
			}.hh(Dic.¤¤Supplies, ww));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.i(text, AD.stats().wins.f().get(f));
				}
				
			}.hh(AD.stats().wins.name, ww).hoverInfoSet(AD.stats().wins.desc));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.i(text, AD.stats().siegeWon.f().get(f));
				}
				
			}.hh(AD.stats().siegeWon.name, ww).hoverInfoSet(AD.stats().siegeWon.desc));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.i(text, AD.stats().defeats.f().get(f));
				}
				
			}.hh(AD.stats().defeats.name, ww).hoverInfoSet(AD.stats().defeats.desc));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.i(text, AD.stats().kills.f().get(f));
				}
				
			}.hh(AD.stats().kills.name, ww).hoverInfoSet(AD.stats().kills.desc));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.i(text, AD.stats().losses.f().get(f));
				}
				
			}.hh(AD.stats().losses.name, ww).hoverInfoSet(AD.stats().losses.desc));
			
			section.addDownC(4, new GStat() {
				
				@Override
				public void update(GText text) {					
					GFORMAT.percInc(text, AD.stats().repF().getD(f));
				}
				
			}.hh(AD.stats().repF().info().name, ww).hoverInfoSet(AD.stats().repF().info().desc));
			
			section.body().incrW(80);
			
		}
		
		
		
		GButt.ButtPanel bb = new GButt.ButtPanel(Dic.¤¤Recruit) {
			
			PLACABLE p = new Placer();
			
			@Override
			protected void renAction() {
				activeSet(f.armies().canCreate());
			}
			
			@Override
			protected void clickA() {
				last().add(List.this, true);
				if (FACTIONS.player().armies().all().size() == 0) {
					COORDINATE c = WORLD.PATH().rnd(FACTIONS.player().capitolRegion());
					int tx = c.x();
					int ty = c.y();
					WArmy e = WORLD.ENTITIES().armies.create(tx, ty, FACTIONS.player());
					VIEW.world().UI.armies.openList(e);
				}else
					VIEW.world().tools.place(p);
			}
			
		};
		bb.body.setWidth(section.body().width());
		section.addDownC(4, bb);
		
		
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return f.armies().all().size();
			}
			
			@Override
			public void hover(int index) {
				if (index >= 0)
					WORLD.OVERLAY().things.hover(f.armies().all().get(index).body(), GCOLOR.MAP().get(f), false, 4);
			}
			
			
		};

		builder.column(null, width+32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		
		section.addRelBody(8, DIR.S, builder.createHeight(HEIGHT-section.getLastY2()-8, false));
		
	
	}
	
	void set(WArmy a) {
		int i = 0;
		for (WArmy aa : f.armies().all()) {
			if (aa == a) {
				builder.set(i);
				break;
			}
			i++;
		}
		
		
	}
	
	private class Button extends GButt.BSection {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			add(new GStat(UI.FONT().M) {
				
				@Override
				public void update(GText text) {
					text.lablify();
					text.add(g().name, 12);
				}
			}, 0, 0);
			
			addRightCAbs(width, new RENDEROBJ.RenderImp(Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {

					if (g().region() != null && DIP.WAR().is(g().region().faction(), GAME.player())) {
						GCOLOR.T().IBAD.bind();
						SPRITES.icons().s.world.render(r, body);
						COLOR.unbind();

					}else if (g().path().moving(g().body())) {
						SPRITES.icons().s.crossheir.render(r, body);
					}else if (g().recruiting()){
						SPRITES.icons().s.muster.render(r, body);
					}
					
					
				}
			});
			
			
			add(new RENDEROBJ.RenderImp(body().width(), 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double dw = (double)AD.menTarget(null).get(g())/Config.battle().MEN_PER_ARMY;
					dw = Math.sqrt(dw);
					int ww = (int) (body.width()*dw);
					GMeter.renderDelta(r, (double)AD.men(null).get(g())/AD.menTarget(null).get(g()), 1.0, body.x1(), body.x1()+ww, body.y1(), body.y2());
					
				}
			}, 0, body().y2()+4);
			

			
			pad(6, 6);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WorldHoverer.hover(text, g());
		}
		
		private WArmy g() {
			return f.armies().all().get(ier.get());
		}
		
		@Override
		protected void clickA() {
			VIEW.world().UI.armies.openList(g(), last());
		}
		
		@Override
		protected void renAction() {
			selectedSet(last().added(VIEW.world().UI.armies.army) && Army.army == g());
		}
	}
	
	private class Placer extends PlacableSimpleTile {

		public Placer() {
			super(Dic.¤¤Recruit);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty) {
			if (!WORLD.PATH().map.is.is(tx, ty))
				return Dic.¤¤Unreachable;
			if (WORLD.REGIONS().map.get(tx, ty) == null || WORLD.REGIONS().map.get(tx, ty).faction() != f)
				return Dic.¤¤MustBeOwnRegion;
			return null;
		}

		@Override
		public void place(int tx, int ty) {
			WArmy e = WORLD.ENTITIES().armies.create(tx, ty, FACTIONS.player());
			VIEW.world().tools.place(null);
			VIEW.world().UI.armies.openList(e);
			
		}
		
		@Override
		public void renderOverlay(GameWindow window) {
			WORLD.OVERLAY().hoverArmy(FACTIONS.player());
		}
		
	}
	
	
	
}
