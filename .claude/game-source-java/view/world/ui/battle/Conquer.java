package view.world.ui.battle;

import game.faction.FACTIONS;
import game.faction.royalty.opinion.ROPINIONS;
import init.race.RACES;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.battle.spec.WBattleSiege;
import world.battle.spec.WBattleSiege.Result;
import world.region.RD;
import world.region.RDOutputs.RDResource;
import world.region.pop.RDRace;

class Conquer extends GuiSection{

	private static CharSequence ¤¤name = "¤Region Captured";
	private static CharSequence ¤¤desc = "¤The city of {0} is captured my lord. What shall be the fate of its citizens?";
	
	
	private static CharSequence ¤¤Enslave = "¤Enslave";
	private static CharSequence ¤¤EnslaveD = "¤Line the population up and ship the healthiest specimen to the capitol as slaves";
	private static CharSequence ¤¤Plunder = "¤Plunder";
	private static CharSequence ¤¤Sack = "¤Loot";
	private static CharSequence ¤¤sackD = "¤Grab what we can of valuables.";
	private static CharSequence ¤¤Raze = "¤Raze";
	private static CharSequence ¤¤RazeD = "¤Let your men blow off some steam after a tough siege. Spare none, leave no stone unturned and teach this settlement a lesson that will be remembered for generations.";
	
	private static CharSequence ¤¤Occupy = "¤Occupy";
	private static CharSequence ¤¤Abandon = "¤Abandon";
	private static CharSequence ¤¤Puppet = "¤Puppet";
	
	private static CharSequence ¤¤OccupyD = "¤Take full control of this region.";
	private static CharSequence ¤¤AbandonD = "¤Let this settlement find its future on its own.";
	private static CharSequence ¤¤PuppetP = "¤Currently, there are no nobles available that can take on the job of governing the region.";
	private static CharSequence ¤¤PuppetD = "¤Install a puppet regime. A new faction will be created, which will be long indebted to you.";
	
	static {
		D.ts(Conquer.class);
	}

	public static final int width = 600;
	
	private boolean enslave;
	private boolean loot;
	private boolean raze;
	
	private final WBattleSiege.Result result;
	
	Conquer(ACTION close, WBattleSiege.Result result){
		
		this.result = result;
		CharSequence[] descs = UI.FONT().M.getRows(Str.TMP.clear().add(¤¤desc).insert(0, result.besiged.info.name()), width);
		
		for (CharSequence d : descs) {
			GText t = new GText(UI.FONT().M, d);
			t.warnify();
			addRelBody(4, DIR.S, t);
		}
		
		{
			GuiSection ss = new GuiSection();
			
			ss.add(new GButt.ButtPanel(¤¤Enslave) {

				@Override
				protected void clickA() {
					enslave = !enslave;
				};
				
				@Override
				protected void renAction() {
					selectedSet(enslave);
				};
				
			}.setDim(150, 30).hoverInfoSet(¤¤EnslaveD));
			
			ss.addRightC(2, new GButt.ButtPanel(¤¤Sack) {

				@Override
				protected void clickA() {
					loot = !loot;
				};
				
				@Override
				protected void renAction() {
					selectedSet(loot);
				};
				
			}.setDim(150, 30).hoverInfoSet(¤¤sackD));
			
			ss.addRightC(2, new GButt.ButtPanel(¤¤Raze) {

				@Override
				protected void clickA() {
					raze = !raze;
				};
				
				@Override
				protected void renAction() {
					selectedSet(raze);
				};
			}.setDim(150, 30).hoverInfoSet(¤¤RazeD));
			
			addRelBody(16, DIR.S, ss);
		}
		
		Slaves slaves = new Slaves();
		addRelBody(16, DIR.S, slaves);
		
		Spoils spoils = new Spoils();
		addRelBody(16, DIR.S, spoils);
		{
			GuiSection stats = new GuiSection();
			
			stats.addRightC(64, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, deva());
				}
			}.hh(UI.icons().m.repair).hoverTitleSet(RD.DEVASTATION().current.info().name).hoverInfoSet(RD.DEVASTATION().current.info().desc));
			
			stats.addRightC(64, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -(int)(death()*RD.RACES().population.get(result.besiged)));
				}
			}.hh(UI.icons().m.skull).hoverInfoSet(Dic.¤¤Deaths));
			
			stats.addRightC(64, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f0(text, mercy());
				}
			}.hh(UI.icons().m.heart).hoverInfoSet(ROPINIONS.STANCE().chivalry.info.name));
			
			addRelBody(16, DIR.S, stats);
		}
		
		
		{
			GuiSection butts = new GuiSection();
			butts.add(new Battle.Butt(FACTIONS.player().banner().MEDIUM, ¤¤Occupy) {
				
				@Override
				protected void clickA() {
					close.exe();
					AD.stats().mercy().incD(FACTIONS.player(), mercy());
					result.occupy(deva(), death(), slaves.accepted(), spoils.accepted());
				}
				
			}.hoverInfoSet(¤¤OccupyD));
			
			butts.addRightC(0, (new Battle.Butt(SPRITES.icons().m.rebellion, ¤¤Abandon) {
				
				@Override
				protected void clickA() {
					close.exe();
					AD.stats().mercy().incD(FACTIONS.player(), mercy());
					result.abandon(deva(), death(), slaves.accepted(), spoils.accepted());
				}
				
			}.hoverInfoSet(¤¤AbandonD)));
			
			butts.addRightC(0, new Battle.Butt(SPRITES.icons().m.flag, ¤¤Puppet) {
				
				@Override
				protected void clickA() {
					close.exe();
					
					AD.stats().mercy().incD(FACTIONS.player(), mercy());
					result.puppet(deva(), deva(), slaves.accepted(), spoils.accepted());
				}
				
				
				
				@Override
				protected void renAction() {
					activeSet(Result.canPuppet());
				};
				@Override
				public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
					if (!Result.canPuppet()) {
						text.text(¤¤PuppetP);
					}else
						super.hoverInfoGet(text);
				};
				
			}.hoverInfoSet(¤¤PuppetD));
			
			addRelBody(8, DIR.S, butts);
		}
		
	}
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		int w = UI.FONT().H2.width(¤¤name);
		UI.PANEL().titleBoxes[1].renderCY(r, body().cX()-w/2, body().y1()-16, w);
		GCOLOR.T().H1.bind();
		UI.FONT().H2.renderC(r, body().cX(), body().y1()-16, ¤¤name);
		COLOR.unbind();
	}
	
	public double deva() {
		double d = 0.1;
		if (loot)
			d += 0.25;
		if (raze)
			d = 1;
		return d;
	}
	
	public double death() {
		double d = 0.1;
		if (enslave)
			d += 0.25;
		if (loot)
			d += 0.1;
		if (raze)
			d = 0.95;
		return d;
	}
	
	public double mercy() {
		double m = RD.RACES().population.get(result.besiged)*0.25;
		
		if (enslave) {
			m -= RD.RACES().population.get(result.besiged)*0.25;
		}
		if (loot) {
			m -= RD.RACES().population.get(result.besiged)*0.25;
		}
		if (raze) {
			m -= RD.RACES().population.get(result.besiged);
		}
		
		m /= 1 + STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null);
		m = CLAMP.d(m, -5, 5);
		return m;
	}
	
	private class Spoils extends GuiSection {

		Spoils() {
			
			int am = 4;
			GRows rows = new GRows(am).setMin(100);
			for (RDResource res: RD.OUTPUT().RES) {
				
				if (res.loot(result.besiged) > 0) {
					
					rows.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, am(res));
						}
					}.hh(res.res.icon()));
				}
			}
			
			add(new GScrollRows(rows.rows(), 28 * 3).view());
			
			addC(GCOLOR.UI().border().makeFrame(body().width()+8, body().height()+8, 1), body().cX(), body().cY());
			
			addRelBody(8, DIR.N, new GHeader(¤¤Plunder));
		}

		public int am(RDResource res) {
			return loot ? res.loot(result.besiged)*8 : 0;
		}
		
		public int[] accepted() {
			
			int[] accepted = new int[RESOURCES.ALL().size()];
			for (RDResource res: RD.OUTPUT().RES) {
				accepted[res.res.index()] += am(res);
			}
			return accepted;
		}
	}
	
	private class Slaves extends GuiSection {

		public Slaves() {
			
			

			int am = 4;
			GRows rows = new GRows(am).setMin(100);

			for (RDRace race : RD.RACES().all) {

				if (race.pop.get(result.besiged) <= 0)
					continue;
				
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, slaves(race));
					}
				}.hh(race.race.appearance().icon));
			}

			addRelBody(4, DIR.S, new GScrollRows(rows.rows(), 24 * 2).view());
			
			addC(GCOLOR.UI().border().makeFrame(body().width()+8, body().height()+8, 1), body().cX(), body().cY());
			
			addRelBody(8, DIR.N, new GHeader(HTYPES.PRISONER().names));
			
		}
		
		private int slaves(RDRace race) {
			if (enslave)
				return (int) (race.pop.get(result.besiged)*0.3);
			return 0;
		}
		
		public int[] accepted() {
			
			int[] accepted = new int[RACES.all().size()];
			
			if (enslave) {
				for (RDRace race : RD.RACES().all) {
					accepted[race.race.index] = slaves(race);
				}
			}
			return accepted;
		}
		
	}
	
}
