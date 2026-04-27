package game.faction.npc.stockpile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile.SRes;
import game.faction.npc.stockpile.UpdaterTree.ResIns;
import game.faction.npc.stockpile.UpdaterTree.TreeRes;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.industry.module.FlatIndustries.FlatIndustry;
import settlement.room.industry.module.IndustryResource;
import snake2d.LOG;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import view.interrupter.IDebugPanel;
import world.map.regions.Region;
import world.region.RD;

class Updater {

	public final UpdaterTree tree = new UpdaterTree();
	public final double recoveryRate = 0.02;
	
	Updater(){
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				for (FactionNPC f : FACTIONS.NPCs()) {
					f.stockpile.saver().clear();
					f.stockpile.update(f, 0);
					f.credits().set(0);
				}
				GAME.factions().prime();
			}
		};
		

		IDebugPanel.add("TRADE RESET", a);
		IDebugPanel.add("TRADE PLAYER COUNT", new ACTION() {
			
			@Override
			public void exe() {
				
				double tot = 0;
				double balance = 0;
				for (RESOURCE res : RESOURCES.ALL()) {
					
					int am = 0;
					for (int i = 0; i < 3; i++) {
						am += FACTIONS.player().res().in(RTYPE.TRADE).history(res).get(i) - FACTIONS.player().res().out(RTYPE.TRADE).history(res).get(i);
					}
					
					if (am != 0) {
						ResIns ii =  tree.o(res).producers.get(0);
						for (ResIns i : tree.o(res).producers) {
							if (i.rateTot < ii.rateTot)
								ii = i;
						}
						
						tot += am*ii.rateTot;
						balance += Math.ceil(am*ii.rateTot);
						LOG.ln(res + " " + am + " " + ii.rateTot + " " + am*ii.rateTot);
					}
					
				}
				LOG.ln("TRADED MANDAYS " + tot + " " + balance + " " + tot/balance);
			}
		});
		IDebugPanel.add("TRADE DEBUG", new ACTION() {
			
			@Override
			public void exe() {
				
				double[] rb = new double[SETT.ROOMS().industries.flat.all().size()];
				double[] inss = new double[SETT.ROOMS().industries.flat.all().size()];
				for (FactionNPC f : FACTIONS.NPCs()) {
					for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

						double b = 1;
						if (ins.industry.reg() != null) {
							b = 0;
							for (int i = 0; i < f.realm().regions(); i++) {
								Region reg = f.realm().region(i);
								b += RD.PROSPECT().getAi(ins.industry, reg);
							}
							b /= f.realm().regions();
						}
						rb[ins.index] += b;
						inss[ins.index] += ins.industry.aiBonus(f, ins);
					}
				}
				

				for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

					LOG.ln(ins.blue.key + " " + Math.round(100*rb[ins.index()]/FACTIONS.NPCs().size()) + " " + Math.round(100*inss[ins.index()]/FACTIONS.NPCs().size()));
					
				}

				
				
			}
		});
	}
	
	public void update(NPCStockpile s, double time) {
		
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			player(i, s, time);
			equalize(i, s, time);
			
		}
		
	}
	
	private void player(int ri, NPCStockpile s, double time) {
		RESOURCE res = RESOURCES.ALL().get(ri);
		double pam = s.playerTraded(res);
		if (pam == 0)
			return;
		double am = Math.abs(pam);
		double max = s.playerTradeLimit(res);
		am -= max*time;
		am = CLAMP.d(am, 0, Double.MAX_VALUE);
		am -= am*time*0.5;
		s.res(ri).playerSet(Math.signum(pam)*am);
	}
	
	private void equalize(int ri, NPCStockpile s, double time) {
		

		SRes res = s.res(ri);
		
		double overflow = res.offset();
		if (overflow == 0)
			return;
		double target = res.amTarget();
		
		double delta = Math.abs(overflow/target);
		delta = CLAMP.d(delta, 0, 1);

		double am = recoveryRate*time*overflow*delta;
		
		res.offsetInc(-am);
		consume(s, tree.o(ri), am);
		
	}
	

	
	
	private void consume(NPCStockpile s, TreeRes res, double amount) {
		double tot = 0;
		
		for (int i = 0; i < res.producers.size(); i++) {
			ResIns r = res.producers.get(i);
			double am = r.rate;
			double rate = am/r.prodSpeedTot;
			tot += rate;
		}
		tot = 1.0/tot;
		
		
		for (int i = 0; i < res.producers.size(); i++) {
			ResIns r = res.producers.get(i);
			double am = r.rate;
			double rate = r.rate/r.prodSpeedTot;
			
			double part = rate*tot;
			double d = part*amount/am;
			
			for (IndustryResource ii : r.ins.industry.ins()) {
				s.res(ii.resource.index()).offsetInc(ii.rate*d);
			}
			
			
			//
		}
	}
	
}
