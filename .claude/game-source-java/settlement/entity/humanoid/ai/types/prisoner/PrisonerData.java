package settlement.entity.humanoid.ai.types.prisoner;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.HAI;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing;
import settlement.stats.law.Processing.Punishment;
import settlement.stats.law.Processing.PunishmentDec;
import snake2d.util.bit.Bits;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;

public final class PrisonerData {

	static PrisonerData self;
	
	final INT_OE<HAI> jailed;
	
	public final INT_OE<HAI> judged;
	public final INT_OE<HAI> stocked;
	public final INT_OE<HAI> stockedTried;
	final INT_OE<HAI> hasWaitedJudge;
	private final INT_OE<HAI> punishReported;
	
	public final GETTER_TRANSE<HAI, Punishment> punishmentSet;
	public final INT_OE<HAI> prisonTimeLeft;

	
	public PrisonerData() {
		self = this;
		judged = new Wrap(new Bits(0b0000_0001), AIModules.data().byte1);
		hasWaitedJudge = new Wrap(new Bits(0b0000_0110), AIModules.data().byte1);
		stocked = new Wrap(new Bits(0b0000_1000), AIModules.data().byte1);
		stockedTried = new Wrap(new Bits(0b0001_0000), AIModules.data().byte1);
		jailed = new Wrap(new Bits(0b0010_0000), AIModules.data().byte1);
		Wrap pp = new Wrap(new Bits(0b0000_1111), AIModules.data().byte2);
		
		punishmentSet = new GETTER_TRANSE<HAI, Processing.Punishment>() {
			
			@Override
			public Punishment get(HAI f) {
				int i = pp.get(f);
				if (i == 0)
					return null;
				return LAW.process().punishments.get(i-1);
			}
			
			@Override
			public void set(HAI f, Punishment t) {
				int i = t == null ? 0 : t.index()+1;
				pp.set(f, i);
			}
		};
		punishReported = new Wrap(new Bits(0b1111_0000), AIModules.data().byte2);
		prisonTimeLeft = new Wrap(new Bits(0x0FF), AIModules.data().byte3);
		
	}
	
	protected void init(Humanoid a, AIManager d) {
		AI.modules().coo(d).set(-1, -1);
		AIModules.data().byte1.set(d, 0);
		AIModules.data().byte2.set(d, 0);
		prisonTimeLeft.set(d, AIModule_Prisoner.PRISON_DAYS);
	}
	
	public void reportPunishment(Humanoid a, AIManager d, PunishmentDec dec) {
		int i = punishReported.get(d)-1;
		if (i == dec.index())
			return;
		if (i >= 0) {
			Punishment p = LAW.process().punishments.get(i);
			
			p.dec(a.race());
		}
		punishReported.set(d, dec.index()+1);
		dec.inc(a.race());
	}
	
	private static class Wrap implements INT_OE<HAI>{
		
		private final Bits bits;
		private final INT_OE<AIManager> data;
		
		Wrap(Bits bits, INT_OE<AIManager> data){
			this.bits = bits;
			this.data = data;
		}

		@Override
		public int get(HAI t) {
			return bits.get(data.get((AIManager) t));
		}

		@Override
		public int min(HAI t) {
			return 0;
		}

		@Override
		public int max(HAI t) {
			return bits.mask;
		}

		@Override
		public void set(HAI t, int i) {
			int d = data.get((AIManager) t);
			d = bits.set(d, i);
			data.set((AIManager) t, d);
		}
		
	}
}
