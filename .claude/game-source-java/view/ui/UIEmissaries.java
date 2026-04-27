package view.ui;



import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.emmi.EmiType;
import game.faction.player.emmi.EmiTypeReg;
import game.faction.player.emmi.EmiTypeRoy;
import game.faction.player.emmi.Emissaries;
import game.faction.royalty.Royalty;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.WORLD;
import world.map.regions.Region;

public class UIEmissaries extends ISidePanel {
	
	private ArrayList<Data> data = new ArrayList<>(512);
	
	
	public UIEmissaries() {
		
		titleSet(Emissaries.¤¤name);
		
		
		while(data.hasRoom())
			data.add(new Data());
		
		section.addDownC(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, FACTIONS.player().emissaries.produced());
			}
		}.hh(Dic.¤¤Production, 250));
		
		for (EmiType<?> t : FACTIONS.player().emissaries.all) {
			section.addDownC(0, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -t.total());
				}
			}.hh(t.name, t.desc, 250));
		}
		
		section.addDownC(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, FACTIONS.player().emissaries.available());
			}
		}.hh(Dic.¤¤Total, 250));
		
		section.addDownC(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, FACTIONS.player().emissaries.penaltyMul());
			}
		}.hh(Dic.¤¤Efficiency, 250));
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				int tot = 0;
				for (Region reg : WORLD.REGIONS().active()) {
					if (tot > data.size())
						break;
					for (EmiTypeReg t : FACTIONS.player().emissaries.regs) {
						if (t.get(reg) > 0) {
							
							data.get(tot).reg = reg;
							data.get(tot).t = t;
							data.get(tot).treg = t;
							tot ++;
							
						}
							
					}
				}
				for (FactionNPC f : FACTIONS.NPCs()) {
					if (tot > data.size())
						break;
					for (EmiTypeRoy t : FACTIONS.player().emissaries.roys) {
						for (Royalty r : f.court().all()) {
							if (t.get(r) > 0) {
								if (tot > data.size())
									break;
								data.get(tot).roy = r;
								data.get(tot).t = t;
								data.get(tot).troy = t;
								tot ++;
								
							}
						}
					}
				}
				
				return tot;
			}
		};
		
		
		
		bu.column(null, new But(new GETTER.GETTER_IMP<Integer>(0)).body().width(), new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new But(ier);
			}
		});
		
		
		section.add(bu.createHeight(HEIGHT-64, false), section.body().x1(), section.body().y2()+8);
		
		
		
	}
	
	private class But extends GButt.BSection {
		
		private final GETTER<Integer> ier;
		private final GSliderInt sl;
		But(GETTER<Integer> ier){
			this.ier = ier;
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					text.lablify();
					text.add(d().t.name);
				}
			}.r(DIR.NW));
			addDown(0, new GStat() {
				
				@Override
				public void update(GText text) {
					if (d().t == d().treg) {
						text.color(GCOLOR.T().faction(d().reg.faction()));
						text.add(d().reg.info.name());
					}else if(d().t == d().troy) {
						d().roy.nameSucc(text);
					}
					
				}
			}.r(DIR.NW));
			
			
			addRelBody(8, DIR.W, new SPRITE.Imp(Icon.S*2) {
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					d().t.icon.render(r, X1, X2, Y1, Y2);
				}
			});
			
			addRelBody(250, DIR.E, new GStat() {
				
				@Override
				public void update(GText text) {
					if (d().t == d().treg) {
						d().treg.formatValue(text, d().reg);
						
					}else if(d().t == d().troy) {
						d().troy.formatValue(text, d().roy);
					}
				}
			});
			
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					if (d().t == d().treg) {
						return d().treg.max(d().reg);
						
					}else if(d().t == d().troy) {
						return d().troy.max(d().roy);
					}
					return 0;
				}
				
				@Override
				public int get() {
					if (d().t == d().treg) {
						return d().treg.get(d().reg);
						
					}else if(d().t == d().troy) {
						return d().troy.get(d().roy);
					}
					return 0;
				}
				
				@Override
				public void set(int t) {
					if (t == 0) {
						sl.reset();
					}
					if (d().t == d().treg) {
						d().treg.set(d().reg, t);
						
					}else if(d().t == d().troy) {
						d().troy.set(d().roy, t);
					}
				}
			};
			
			sl = new GSliderInt(ii, 160, true, true);
			
			
			addRelBody(120, DIR.E, sl);
			
			pad(6);
			
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs()) {
				if (d().t == d().treg) {
					VIEW.world().UI.regions.hover(d().reg, text);
					
				}else if(d().t == d().troy) {
					VIEW.world().UI.factions.hover(text, d().roy);
				}
			}
		}
		

		@Override
		protected void clickA() {
			
			if (d().t == d().treg) {
				VIEW.world().activate();
				VIEW.world().panels.addDontRemove(UIEmissaries.this, VIEW.world().UI.regions.get(d().reg));
				
			}else if(d().t == d().troy) {
				VIEW.world().UI.factions.open((FactionNPC) d().roy.court.faction);
			}
		}
		
		
		private Data d() {
			return data.get(ier.get());
		}
	}
	

	
	private class Data {
		EmiType<?> t;
		EmiTypeRoy troy;
		EmiType<Region> treg;
		Royalty roy;
		Region reg;
		
		
	}
	
	
}
