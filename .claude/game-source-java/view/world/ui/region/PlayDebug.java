package view.world.ui.region;

import game.GAME;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.boosting.tmp.TmpBoostSpec;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.value.GVALUES;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import view.ui.util.UIValues;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuildPoints.RDBuildPoint;
import world.region.pop.RDRace;

class PlayDebug extends GuiSection{

	Region reg;
	
	PlayDebug(){
		
		for (RDBuildPoint p : RD.BUILDINGS().costs.ALL) {
			padd(new GButt.ButtPanel(p.bo.name) {
				
				@Override
				protected void clickA() {
					
					new RBooster(new BSourceInfo("cheat", null), 0, 2500, false) {
						final Region rr = reg;

						@Override
						protected double get(Region reg) {
							return reg == rr ? 1 : 0;
						}

						
					}.add(p.bo);
				}
				
			});
		}
		
		
		
		padd(new GButt.Checkbox("claim") {
			
			@Override
			protected void renAction() {
				selectedSet(RD.REALM(reg) == RD.REALM(FACTIONS.player()));
			}
			
			@Override
			protected void clickA() {
				if (reg.capitol()) {
					FACTIONS.remove((FactionNPC) reg.faction(), true);
				}
				
				RD.setFaction(reg, selectedIs() ? null : FACTIONS.player(), true);
			}
		});
		
		padd(new GButt.ButtPanel("affiliate") {
			@Override
			protected void clickA() {
				RD.OWNER().affiliation.setD(reg, 1.0);
			}
		});
		
		{
			GuiSection s = new GuiSection();
			s.add(new GText(UI.FONT().S, "devastation"), 0, 0);
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return RD.DEVASTATION().current.max(null);
				}
				
				@Override
				public int get() {
					return RD.DEVASTATION().current.get(reg);
				}
				
				@Override
				public void set(int t) {
					RD.DEVASTATION().current.set(reg, t);
				}
			};
			
			s.addRightC(8, new GSliderInt(ii, 100, false));
			padd(s);
		}
		
		padd(new GButt.ButtPanel("garrison") {
			@Override
			protected void clickA() {
				RD.MILITARY().garrison.inc(reg, 50);
			}
		});
		
//		padd(new GButt.ButtPanel("finish b.") {
//			@Override
//			protected void clickA() {
//				for (RDBuilding b : RD.BUILDINGS().all) {
//					b.level.set(reg, b.targetLevel.get(reg));
//				}
//			}
//		});
		
		padd(new GButt.ButtPanel("build") {
			@Override
			protected void clickA() {
				RD.UPDATER().BUILD(reg);
//				for (RDBuilding b : RD.BUILDINGS().all) {
//					b.level.set(reg, b.targetLevel.get(reg));
//				}
			}
		});
		
		padd(new GButt.ButtPanel("boost") {

			TmpBoostSpec s = new TmpBoostSpec("_DEBUG", "PlayDebug", "PlayDebug", UI.icons().s.alert);
			{
				s.spec.push(new BoosterValue(BValue.VALUE1, new BSourceInfo("", UI.icons().s.alert), 100, false), RD.OUTPUT().get(RESOURCES.WOOD()).boost);
				s.spec.push(new BoosterValue(BValue.VALUE1, new BSourceInfo("", UI.icons().s.alert), 4, true), RD.OUTPUT().get(RESOURCES.WOOD()).boost);
			}
			
			@Override
			protected void clickA() {
				GAME.BOOST().regions.toggle(reg, s);
				
			}
		});
		
		padd(new GButt.ButtPanel("pop ini") {
			
			@Override
			protected void clickA() {
				for (RDRace rr : RD.RACES().all)
					rr.pop.init(reg);
			}
			
		});
		
		padd(new GButt.ButtPanel("pop clear") {
			
			@Override
			protected void clickA() {
				
				for (RDRace r : RD.RACES().all) {
					r.pop.set(reg, 0);
				}
			}
			
		});

		padd(UIValues.butt(GVALUES.REGION, new GETTER<Region>(){

			@Override
			public Region get() {
				return reg;
			}
			
		}));
		
		for (RDRace r : RD.RACES().all) {
			padd(new GButt.ButtPanel(r.race + "++") {
				
				@Override
				protected void clickA() {
					r.pop.inc(reg, 100);
				}
				
			});
		}
	}
	
	void padd(RENDEROBJ o) {
		if (getLastX2() > 600) {
			add(o, 0, body().y2());
		}else
			addRightC(0, o);
		
	}
	
}
