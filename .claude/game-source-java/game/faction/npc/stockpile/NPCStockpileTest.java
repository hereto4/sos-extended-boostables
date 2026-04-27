package game.faction.npc.stockpile;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;

public class NPCStockpileTest {

	private static final double AISize = 5000;
	
	public NPCStockpileTest(){
		
		FactionNPC f = FACTIONS.NPCs().rnd();

		
		
		int[] workers = new int[] {
				50,
				100,
				150,
				200,
				250,
				300,
				
		};
		
		for (int w : workers) {
			System.out.println("WORKERS: " + w);
			
			RESOURCE ma = RESOURCES.map().get("MACHINERY").get(0);
			sell(f, ma, w);
			
			ma = RESOURCES.map().get("GRAIN").get(0);
			sell(f, ma, w);
			
			System.out.println();
		}
		
		
//		for (int i = 0; i < 10; i++) {
//			System.out.println("WORKERS: " + i*1000);
//			
//			for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
//				sell(f, RESOURCES.ALL().get(ri), i*1000);
//			}
//			
//		}
		

		
	}
	
	private void sell(FactionNPC f, RESOURCE ma, final double workers) {
		

		NPCStockpile s = f.stockpile;

		
		s.saver().clear();
		s.update(f, 0, AISize);
		f.credits().set(0);

		double po =  s.priceBuy(ma.index(), 1);

		
		
		double credits = 0;
		double items = 0;
		int resBought = 0;
		double tot = 0;
		
		double cr = 0;
		double am = 0;
		
		final int iterations = 100;
		
		for (int i = 0; i < iterations; i++) {
			
			am += workers*s.res(ma.index()).rate();
			items += am;
			
			
			while(am > 0) {
				double p = s.priceBuy(ma.index(), 1);
				cr += p;
				credits += p;
				s.inc(ma, 1);
				f.credits().inc(-p, CTYPE.TAX);
				am--;
			}
			
//			System.out.println(s.priceBuy(ma.index(), 1)/po);
//			
			

			for (RESOURCE r : RESOURCES.ALL()) {
				tot += Math.max(s.priceSell(r.index(), 1), 1);
			}
			
			while(cr > 0) {
				RESOURCE rr = RESOURCES.ALL().rnd();
				if (rr == ma)
					continue;
				double c = Math.max(s.priceSell(rr.index(), 1), 1);
				s.inc(rr, -1);
				cr -= c;
				f.credits().inc(c, CTYPE.TAX);
				resBought ++;
			}
			
			s.update(f, TIME.secondsPerDay(), AISize);
			f.credits().inc(-f.credits().credits()*0.05, CTYPE.INFLATION);
		}

		tot /= RESOURCES.ALL().size()*iterations;

		System.out.println(ma.key);
		System.out.println("starprice: " + (int)po);
		System.out.println("aveprice: " + (int)(credits/items));
		System.out.println("items sold: " + (int)items);
		System.out.println("money circulated: " + (int)credits);
		System.out.println("goods purchased: " + (int)resBought);
		System.out.println("average buy price: " + (int)tot);
		double d = s.res(ma.index()).rateTot()/s.res(ma.index()).rate();
		System.out.println("profits: " + d*resBought/workers);
		
		
	}
	

	
}
