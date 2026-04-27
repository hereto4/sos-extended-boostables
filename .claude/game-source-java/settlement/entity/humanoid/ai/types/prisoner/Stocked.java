package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.stocks.ROOM_STOCKS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Extra;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

class Stocked extends AIPLAN.PLANRES{

	
	public Stocked() {
		super("prisStocked");
		// TODO Auto-generated constructor stub
	}

	private final Extra p = LAW.process().stocks;
	private final ROOM_STOCKS blue = SETT.ROOMS().STOCKS;
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		
		AISubActivation s = walk.set(a, d);
		if (s != null)
			return s;
		
		return null;
	}
	
	private final Resumer walk = new Resumer(p.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			COORDINATE c = blue.stockReserve();
			if (c == null)
				return null;
			d.planTile.set(c);
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, c);
			if (s == null) {
				can(a, d);
				return null;
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return sit.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue.stockIsReserved(d.planTile.x(), d.planTile.y());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			blue.stockCancel(d.planTile.x(), d.planTile.y());
		}
	};
	
	private final Resumer sit = new Resumer(p.verb) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			DIR dir = blue.stockDir(d.planTile.x(), d.planTile.y(), a.speed.dir());
			a.speed.setDirCurrent(dir);
			blue.stockUse(d.planTile.x(), d.planTile.y());
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (TIME.light().nightIs()) {
				can(a, d);
				PrisonerData.self.stocked.set(d, 1);
				LAW.process().stocks.inc(a.race(), true);
				PrisonerData.self.prisonTimeLeft.inc(d, -1);
				return null;
			}
			return AI.SUBS().LAY.activateTime(a, d, 16);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue.stockIsReserved(d.planTile.x(), d.planTile.y());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			blue.stockCancel(d.planTile.x(), d.planTile.y());
		}
		
	};

}
