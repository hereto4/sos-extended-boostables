package game.faction.diplomacy.deal;

import game.faction.diplomacy.DIP;
import game.faction.diplomacy.deal.DealRegs.DealReg;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import util.data.INT.INTE;

public class DealDrawfter {

	public static void draftPeace(Deal deal, FactionNPC enemy, boolean playerIsMakingDeal) {
		
		deal.setFactionAndClear(enemy, playerIsMakingDeal);
		
		deal.bools.PEACE.set(true);
		
		draft(deal, 0, true, true);
		
	}
	
	public static void draft(Deal deal, boolean bools, boolean regs) {
		draft(deal, 0, bools, regs);
	}
	
	public static void draft(Deal deal, double dcreds, boolean bools, boolean regs) {

		
		int v = (int) (deal.valueCredits()+dcreds);
		if (v < 0) {
			give(deal, deal.player, -v, bools, regs);
		}else {
			give(deal, deal.npc, v, bools, regs);
		}
	}
	
	private static int[] rr;

	private static ArrayListGrower<Gift> giftable = new ArrayListGrower<>();
	private static ArrayList<DealBool> tbools = new ArrayList<>(16);
	private static boolean log = false;
	
	private static void init(DealParty g, Deal deal) {
		if (rr == null) {
			for (RESOURCE res : RESOURCES.ALL()) {
				giftable.add(new Gift() {

					@Override
					public int max() {
						return gg.resources.max(res);
					}
					
					@Override
					public int get() {
						return gg.resources.get(res);
					}
					
					@Override
					public void set(int t) {
						gg.resources.set(res, t);
					}

					@Override
					public int value(int am) {
						return gg.valueResource(res, am);
					}

					@Override
					public boolean canGift() {
						return oo.resources.get(res) <= 0;
					}
				});
			}
			for (Race res : RACES.all()) {
				giftable.add(new Gift() {

					
					@Override
					public int max() {
						return gg.slaves.max(res);
					}
					
					@Override
					public int get() {
						return gg.slaves.get(res);
					}
					
					@Override
					public void set(int t) {
						gg.slaves.set(res, t);
					}

					@Override
					public int value(int am) {
						return gg.npc().slaves().price(res, am);
					}
					
					@Override
					public boolean canGift() {
						return oo.slaves.get(res) <= 0;
					}
				});
			}
			
			rr = new int[giftable.size()];
			
			
		}
		Gift.gg = g;
		Gift.oo = deal.npc == g ? deal.player : deal.npc;
		for (int i = 0; i < rr.length; i++)
			rr[i] = i;
		for (int i = 0; i < rr.length; i++) {
			int o = rr[i];
			int ii = RND.rInt(rr.length);
			rr[i] = rr[ii];
			rr[ii] = o;
		}
	}
	
	private static void give(Deal deal, DealParty g, int value, boolean bools, boolean regs) {
		
		int creds = (int) (g.credits.max()*0.9 - g.credits.get());
		creds = Math.max(creds, 0);

		if (log) {
			LOG.ln(value + " " + bools + " " + regs + " " + creds);
		}
		

		
		if (bools) {
			
			if (creds >= value*2) {
				g.credits.inc(value);
				return;
			}
			
			if (DIP.WAR().is(deal.player.f(), deal.npc.f())) {
				tbools.clearSloppy();
				tbools.add(deal.bools.OVERLORD);
				tbools.add(deal.bools.VASSAL);
				tbools.add(deal.bools.PEACE);
				boolean wasPeace = deal.bools.PEACE.is();
				boolean hasSet = false;
				for (DealBool b : tbools) {
					b.set(false);
				}
				for (DealBool b : tbools) {
					if (b.problem() == null) {
						double v = b.value()*(deal.player == g ? 1 : -0);
						if (v > 0 && value-v >= 0) {
							if (log) {
								LOG.ln("wbool" + " " + b.info.name + " " + v + " " + value);
							}
							value -= v;
							b.set(true);
							hasSet = true;
							break;
						}
					}
				}
				if (!hasSet && wasPeace) {
					deal.bools.PEACE.set(true);
				}
			}else {
				boolean hasOne = false;
				for (DealBool b : deal.bools.all()) {
					if (b.is()) {
						hasOne = true;
						break;
					}
				}
				
				if (!hasOne) {
					for (DealBool b : deal.bools.all()) {
						if (b.problem() == null && !b.is()) {
							double v = b.value()*(deal.player == g ? 1 : -0);
							if (v > 0 && value-v >= 0) {
								value -= v;
								b.set(true);
								if (log) {
									LOG.ln("bool" + " " + b.info.name + " " + v + " " + value);
								}
							}
						}
					}
				}
				
			}
			
			
			if (creds >= value*2) {
				g.credits.inc(value);
				return;
			}
		}
		
		regs &= !deal.bools.ABSORB.is();
		
		if (regs) {
			
			if (creds >= value*2) {
				g.credits.inc(value);
				return;
			}
			
			boolean hasreg = true;
			while(hasreg) {
				hasreg = false;
				for (DealReg r : g.regs.all()) {
					if (r.value() > 0 && r.canSelect() && !r.is() && r.value() < value) {
						if (log) {
							LOG.ln("reg" + " " + r.value() + " " + value);
						}
						hasreg = true;
						r.set(true);
						value -= r.value();
					}
					
				}			
			}
			
			if (creds >= value*2) {
				g.credits.inc(value);
				return;
			}
		}
		


		
		init(g, deal);
		
		for (int ri : rr) {
			
			Gift oo = giftable.get(ri);
			if (oo.value(1) <= 0)
				continue;
			
			int am = oo.max()-oo.get();
			int oldValue = oo.value(oo.get());
			am = CLAMP.i(oo.max()/5, 0, am);
			if (am <= 0)
				continue;
			if (!oo.canGift())
				continue;
			if (oo.value(oo.get()+1)-oldValue > value)
				continue;
			
			int cv = oo.value(oo.get()+am)-oldValue;
			if (cv == 0)
				continue;
			while(cv > value && am > 0) {
				
				double dec = value;
				dec /= cv;
				dec = 1.0 - dec;
				
				am -= Math.ceil(dec*am*0.5);
				cv = oo.value(oo.get()+am)-oldValue;
			}
			
			if (am > 0) {
				value -= cv;
				oo.inc(am);
				if (log) {
					LOG.ln(RESOURCES.ALL().get(ri) + " " + am + " " + oo.value(am) + " " + oldValue);
				}
				if (value <= 0)
					break;
			}	
			
			if (creds >= value) {
				g.credits.inc(value);
				return;
			}
		}
		
		if (creds >= value) {
			g.credits.inc(value);
			return;
		}
		
		for (int nopI = 0; nopI < rr.length && value > 0; nopI++) {
			Gift petit = null;
			int MV = Integer.MAX_VALUE;
			
			
			for (int ri : rr) {
				Gift oo = giftable.get(ri);
				int am = oo.max()-oo.get();
				if (am > 0 && oo.canGift()) {
					int v = oo.value(oo.get()+1)-oo.value(oo.get());
					if (v > 0 && v < MV) {
						petit = oo;
						MV = v;
					}
				}
			}
			
			if (petit != null) {;
				int max = petit.max();
				int base = petit.value(petit.get());
				while(petit.get()+1 < max-1) {
					int v = petit.value(petit.get()+1)-base;
					petit.inc(1);
					if (v >= value) {
						value -= v;
						
						break;
					}
					
				}
			}else {
				break;
			}
			
			
		}
		
		if (value > 0 && creds >= value) {
			g.credits.inc(value);
			return;
		}
		
		
	}

	private static abstract class Gift implements INTE {
		
		public static DealParty gg;
		public static DealParty oo;
		
		@Override
		public int min() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public abstract boolean canGift();
		
		public abstract int value(int am);
		
	}
	
	
}
