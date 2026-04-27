package settlement.battle.invasion;

import game.GAME;
import game.battle.state.BattleState;
import game.battle.state.BattleStateExiter;
import game.battle.state.BattleStateResult;
import game.battle.state.BattleStateSpec;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.STOCKPILE.StockpileImp;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import world.WORLD;
import world.army.AD;
import world.army.WDIV;
import world.battle.spec.BATTLE_RESULT;
import world.entity.army.WArmy;
import world.region.RD;

final class Attack extends BattleStateExiter{

	private static CharSequence ¤¤vTitle = "¤Victory";
	private static CharSequence ¤¤vBody = "¤The enemy is beaten. Rejoice! Spoils from the battlefield will soon arrive. Do you wish to accept the captives, or should we 'release' them my lord?.";
	
	private static CharSequence ¤¤dTitle = "¤Defeat";
	private static CharSequence ¤¤dBody = "¤You have lost! Our men have thrown away their lives in vain. The enemy will be be at our gates at any moment! We must pray for mercy.";
	
	static {
		D.ts(Attack.class);
	}
	
	private final int ref;
	
	public Attack(InvasionSpec invasion) {
		ref = invasion.ref;
		
		BattleStateSpec s = new BattleStateSpec();
		
		s.player.wCoo.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
		s.player.moraleBase = 1.0;
		for (WDIV d : RD.MILITARY().divisions(FACTIONS.player().capitolRegion())) {
			s.player.divs.add(d.generate());
		}
		for (WArmy a : armies()) {
			for (int di = 0; di < a.divs().size(); di++) {
				if (s.player.divs.hasRoom()) {
					s.player.divs.add(a.divs().get(di).generate());
				}
			}
		}
		
		for (DivGeneration g : invasion.divs) {
			s.enemy.divs.add(g);
		}
		
		for (int i = 0; i < invasion.artillery.length; i++) {
			s.enemy.artillery[i] = invasion.artillery[i];
		}
		

		
		
		BattleState.setGenerate(this, s);
		
	}
	

	@Override
	public void afterExit(BattleStateResult result) {
		
		
		InvasionSpec invasion = SETT.INVADOR().spec(ref);
		AD.stats().report(FACTIONS.player(), result.result == BATTLE_RESULT.VICTORY,result.playerLosses, result.enemyLosses);
		
	
		if (result.result == BATTLE_RESULT.VICTORY) {
			
			int di = 0;
			
			
			
			int[] ress = new int[RESOURCES.ALL().size()];
			
			{
				double tot = 0;
				double death = 0;
				
				for (DivGeneration s : invasion.divs) {
					tot += s.indus.length;
					death += (s.indus.length-result.enemySurvivors[di]-result.enemyCaptured[di]);
				}
				double d = death / (1+ tot);
				d*= 0.25;
				
				for (DivGeneration s : invasion.divs) {
					for (int i = 0; i < s.indus.length; i++) {
						for (int ei = 0; ei < STATS.EQUIP().BATTLE_ALL().size(); ei++) {
							EquipBattle e = STATS.EQUIP().BATTLE_ALL().get(ei);
							invasion.loot.add(e.resource, e.get(s.indus[i]));
						}
					}
					
					
				}
				
				
				
				
				
				for (RESOURCE res : RESOURCES.ALL()) {
					
					int am = (int) Math.ceil(d * invasion.loot.get(res));
					ress[res.index()] = am;
					
					if (am > 0) {
						FACTIONS.player().buyer().deliver(res, am, ITYPE.spoils);
					}
				}
			}
			
			int[] sla = new int[RACES.all().size()];
			
			for (Race r : RACES.all()) {
				int am = result.enemyCaptured[r.index()];
				sla[r.index()] = am;
				if (am > 0) {
					//SETT.ENTRY().add(r, HTYPES.PRISONER(), am);
				}
			}
			invasion.canBeAttacked = false;
			
			new MVictory2(ress, sla).send();
			
			for (InvasionListener ll : InvasionListener.all) {
				ll.victory(result.playerLosses, result.enemyLosses, invasion.ref);
			}
			
			GAME.count().INVASIONS_WON.inc(1);
			
		}else {
			StockpileImp stock = new StockpileImp();
			ArrayList<DivGeneration> nnew = new ArrayList<>(invasion.divs.size());
			
			int di = 0;
			for (DivGeneration s : invasion.divs) {
				s.setMen(result.enemySurvivors[di++]);
				if (s.indus.length > 0)
					nnew.add(s);
			}

			di = 0;
			for (WDIV d : RD.MILITARY().divisions(FACTIONS.player().capitolRegion())) {
				for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
					double dd = (double)(d.men()- result.playerSurvivors[di].length)/d.men();
					stock.inc(e.resource, (int)Math.ceil(d.equipI(e)*d.men()*dd));
				}
				di++;
			}
			
			RESOURCE.remove(stock, RTYPE.SPOILS);
			
			new MessageText(¤¤dTitle).paragraph(Str.TMP.clear().add(¤¤dBody)).send();
			
			invasion.divs.clearSloppy();
			invasion.divs.add(nnew);
			GAME.count().INVASIONS_LOST.inc(1);
		}
		
		{
			int ddi = 0;
			for (WDIV d : RD.MILITARY().divisions(FACTIONS.player().capitolRegion())) {
				d.resolve(result.playerSurvivors[ddi++]);
			}
			for (WArmy a : armies()) {
				for (int di = 0; di < a.divs().size(); di++) {
					if (ddi < result.playerSurvivors.length) {
						a.divs().get(di).resolve(result.playerSurvivors[ddi++]);
					}
				}
			}
		}
		
	}
	
	private LIST<WArmy> armies(){
		LinkedList<WArmy> armies = new LinkedList<>();
//		for (WArmy a : WORLD.ENTITIES().armies.fill(FACTIONS.player().capitolRegion())) {
//			if (a.faction() == FACTIONS.player())
//				armies.add(a);
//		}
//		return armies;
		
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(FACTIONS.player().cx(), FACTIONS.player().cy(), 0);
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			
			if (t.getValue() > WArmy.reinforceTiles)
				break;
			
			for (WArmy ar : WORLD.ENTITIES().armies.fillTile(t.x(), t.y())) {
				if (ar != null && AD.men(null).get(ar) == 0 || ar.faction() != FACTIONS.player())
					continue;
				if (armies.contains(ar))
					continue;
				armies.add(ar);
			}

			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.PATH().map.can(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		GUTIL.flooder().done();
		return armies;
	}
	
//	private static class MVictory extends MessageSection {
//
//		private static final long serialVersionUID = 1L;
//		private final int[] res;
//		private final int[] slaves;
//		
//		
//		public MVictory(int[] res, int[] slaves) {
//			super(¤¤vTitle);
//			this.res = res;
//			this.slaves = slaves;
//		}
//
//		@Override
//		protected void make(GuiSection section) {
//			
//			String st = ""+Str.TMP.clear().add(¤¤vBody);
//			
//			section.addDown(8, new GText(UI.FONT().M, st).setMaxWidth(WIDTH));
//			
//			GuiSection s = new GuiSection();
//			int gi = 0;
//			for (RESOURCE r : RESOURCES.ALL()) {
//				if (r.index() >= res.length || res[r.index()] <= 0)
//					continue;
//				RENDEROBJ o = new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.i(text, res[r.index()]);
//					}
//				}.hv(r.icon()).hoverInfoSet(r.names);
//				s.addGrid(o, gi++, 6, 64, 32);
//			}
//			for (Race r : RACES.all()) {
//				if (r.index() >= slaves.length || slaves[r.index()] <= 0)
//					continue;
//				RENDEROBJ o = new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.i(text, slaves[r.index()]);
//					}
//				}.hv(r.appearance().icon).hoverInfoSet(r.info.names);
//				s.addGrid(o, gi++, 6, 64, 32);
//			}
//			
//			section.addRelBody(16, DIR.S, s);
//			
//		}
//		
//	}
	
	private static class MVictory2 extends MessageSection {

		private static final long serialVersionUID = 1L;
		private final int[] res;
		private final int[] slaves;
		private final double time = TIME.currentSecond();
		private boolean accepted = false;
		
		public MVictory2(int[] res, int[] slaves) {
			super(¤¤vTitle);
			this.res = res;
			this.slaves = slaves;
		}

		@Override
		protected void make(GuiSection section) {
			
			String st = ""+Str.TMP.clear().add(¤¤vBody);
			
			section.addDown(8, new GText(UI.FONT().M, st).setMaxWidth(WIDTH));
			
			GuiSection s = new GuiSection();
			int gi = 0;
			for (RESOURCE r : RESOURCES.ALL()) {
				if (r.index() >= res.length || res[r.index()] <= 0)
					continue;
				RENDEROBJ o = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, res[r.index()]);
					}
				}.hv(r.icon()).hoverInfoSet(r.names);
				s.addGrid(o, gi++, 6, 64, 32);
			}
			section.addRelBody(16, DIR.S, s);
			
			s = new GuiSection();
			gi = 0;
			for (Race r : RACES.all()) {
				if (r.index() >= slaves.length || slaves[r.index()] <= 0)
					continue;
				RENDEROBJ o = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, slaves[r.index()]);
					}
				}.hv(r.appearance().icon).hoverInfoSet(r.info.names);
				s.addGrid(o, gi++, 6, 64, 32);
			}
			section.addRelBody(16, DIR.S, s);
		
			
			section.addRelBody(8, DIR.S, new GButt.ButtPanel(Dic.¤¤Accept) {
				@Override
				protected void clickA() {
					if (TIME.currentSecond()-time < TIME.secondsPerDay() && !accepted) {
						accepted = true;
						for (Race r : RACES.all()) {
							int am = slaves[r.index()];
							if (am > 0) {
								SETT.ENTRY().add(r, HTYPES.PRISONER(), am);
							}
						}
						VIEW.inters().messages.hide();
					}
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					activeSet(TIME.currentSecond()-time < TIME.secondsPerDay() && !accepted);
				}
			});
			
			section.addRelBody(8, DIR.S, new GButt.ButtPanel(Dic.¤¤Decline) {
				@Override
				protected void clickA() {
					if (TIME.currentSecond()-time < TIME.secondsPerDay() && !accepted) {
						accepted = true;
						VIEW.inters().messages.hide();
					}
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					activeSet(TIME.currentSecond()-time < TIME.secondsPerDay() && !accepted);
				}
			});
			
		}
		
	}

}
