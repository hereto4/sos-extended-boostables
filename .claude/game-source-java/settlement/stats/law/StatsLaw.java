package settlement.stats.law;

import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.law.Processing.PunishmentDec;
import settlement.stats.law.Processing.PunishmentImp;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.text.D;

public class StatsLaw extends StatCollection{
	
	public final LAW law;
	public STAT EQUALITY;
	public STAT EX_CON;
	public STAT lLAW;
	public final LIST<StatLaw> punishments;
	public final GETTER_TRANSE<Induvidual, PRISONER_TYPE> prisonerType;
	
	private static CharSequence ¤¤name = "Law";
	private static CharSequence ¤¤descc = "Statistics regarding law";
	
	static {
		D.ts(StatsLaw.class);
	}
	
	public StatsLaw(StatsInit init){
		super(init, "LAW", ¤¤name, ¤¤descc);
		law = new LAW(init);
		
		for (PunishmentImp p : LAW.process().punishments) {
			new StatLaw(p, p.key, init);
		}
		for (PunishmentImp p : LAW.process().extras) {
			new StatLaw(p, p.key, init);
		}

		EQUALITY = new STATImp("EQUALITY", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double d = 0;
				double t = 0;
				for (PunishmentDec p : LAW.process().punishmentsdec) {
					d += p.limit(r)*(1+p.multiplier);
					t += p.limit(null)*(1+p.multiplier);
				}
				
				if (d*0.6 > t) {
					return (int) (pdivider(s, r, 0)*t/d); 
				}
				
				return pdivider(s, r, 0);
			}
		};
		EQUALITY.info().icon = UI.icons().m.descrimination;
		EQUALITY.standing = new StatStanding(EQUALITY, 1.0);
		
		EX_CON = new STATData("EX_CON", "EX_CON", init, init.count.new DataNibble("EX_CON"));
		EX_CON.info().icon = UI.icons().m.law;
		
		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				if (day)
					EX_CON.indu().inc(h.indu(), -1);
			}
		});
		
		
		lLAW = new STATFacade("LAW", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				return LAW.law().rate().getD(daysBack);
			}
		};
		lLAW.info().icon = UI.icons().m.law;
		
		final INT_OE<Induvidual> data = init.count.new DataNibble("LAW_PRISONERT");
		
		prisonerType = new GETTER_TRANSE<Induvidual, PRISONER_TYPE>(){

			@Override
			public PRISONER_TYPE get(Induvidual f) {
				return PRISONER_TYPE.ALL.get(data.get(f));
			}

			@Override
			public void set(Induvidual f, PRISONER_TYPE t) {
				data.set(f, t.index());
			}
			
		};
	
		LinkedList<StatLaw> ll = new LinkedList<>();
		for (STAT s : all())
			if (s instanceof StatLaw)
				ll.add((StatLaw) s);
		
		punishments = new ArrayList<StatsLaw.StatLaw>(ll);
		
		
	}

	public static class StatLaw extends STATFacade {

		public final PunishmentImp p;
		
		StatLaw(PunishmentImp p, String key, StatsInit init) {
			super(key, init);
			this.p = p;
			info.icon = p.icon;
		}

		@Override
		protected double getDD(HCLASS s, Race r, int daysBack) {
			return p.rate(null).getD(daysBack);
		}

	}
	
	
	
}
