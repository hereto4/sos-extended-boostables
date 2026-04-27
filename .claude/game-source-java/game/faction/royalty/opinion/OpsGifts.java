package game.faction.royalty.opinion;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROpper.ROpperDown;
import init.sprite.UI.UI;
import util.text.D;

public final class OpsGifts {

	private static CharSequence ¤¤name = "Generosity";
	private static CharSequence ¤¤nameE = "Extortion";
	private static CharSequence ¤¤nameD = "Based on your previous dealings and gifts.";

	static {
		D.ts(OpsGifts.class);
	}
	
	private final ROpperDown op;
	private final ROpperDown ex;
	
	OpsGifts(double year){
		op = new ROpperDown("DEALINGS", ¤¤name, ¤¤nameD, UI.icons().s.happy, 100, false, year*10*100) {
			@Override
			public double getModifier(Royalty roy) {
				return 0.25 + 0.75*BOOSTABLES.NOBLE().PRIDE.get(roy.induvidual);
			}
			
			@Override
			public double increase(Royalty roy) {
				return (1 + 99*value.getD(roy))*super.increase(roy);
			}
			
		};
		ex = new ROpperDown("DEALINGSE", ¤¤nameE, ¤¤nameD, UI.icons().s.happy, -100, false, year*10*100) {
			@Override
			public double getModifier(Royalty roy) {
				return 0.25 + 0.75*BOOSTABLES.NOBLE().PRIDE.get(roy.induvidual);
			}
			
			@Override
			public double increase(Royalty roy) {
				return (1 + 99*value.getD(roy))*super.increase(roy);
			}
		};
	}
	
	public double getGenerosityNeededForPeace(FactionNPC f) {
		return (ROPINIONS.getPeaceValue(f, op, 1)-op.value.getD(f.king()))*op.to();
	}
	
	public double getGenerosityNeededForOpinion(FactionNPC f, double target) {
		return (ROPINIONS.getOpinionValue(f, op, target)-op.value.getD(f.king()))*op.to();
	}
	
	
	public void makeDeal(FactionNPC f, double generousity) {

		for (Royalty r : f.court().all()) {
			makeDeal(r, r.isKing() ? generousity : generousity*0.25);
		}
	}
	
	private void makeDeal(Royalty roy, double generousity) {
		if (generousity < 0) {
			ex.value.incD(roy, generousity/(ex.to()*ex.getModifier(roy)));
		}else
			op.value.incD(roy, generousity/(op.to()*op.getModifier(roy)));
	}
	
//	public double dealIncrease(FactionNPC f, double v) {
//		
//		if (v > 0) {
//			v = CLAMP.d(v, 0, 1);
//			double now = op.value.getD(f.court().king().roy());
//			now = CLAMP.d(now, 0, 1);
//			double next = now + v;
//			next = CLAMP.d(next, 0, 1);
//			
//			v = Math.pow(next, 0.5)-Math.pow(now, 0.5);
//		}
//		
//		
//		double m = op.getModifier(f.court().king().roy());
//		return 10.0*v*m;
//	}

	
}
