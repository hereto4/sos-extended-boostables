package game.raiding;

import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class RaiderTextsRace {
	
	public final LIST<CharSequence> greetings;
	public final LIST<CharSequence> mids;
	public final LIST<CharSequence> bodies;
	public final LIST<CharSequence> ends;
	
	public final LIST<CharSequence> rgreetings;
	public final LIST<CharSequence> rmids;
	public final LIST<CharSequence> rbodies;
	public final LIST<CharSequence> rends;
	
	public final LIST<CharSequence> rejected;
	public final LIST<CharSequence> payed;
	public final LIST<CharSequence> afterRaid;
	
	public final LIST<CharSequence> allyHelp;
	public final LIST<CharSequence> allyDead;
	public final LIST<CharSequence> allyFight;
	
	public RaiderTextsRace(Json j){
		
		
		{
			Json jj = j.json("FIRST");
			greetings = tt(jj.texts("GREETING"));
			mids = tt(jj.texts("INTROS"));
			bodies = tt(jj.texts("BODIES"));
			ends = tt(jj.texts("ENDS"));
		}
		{
			Json jj = j.json("REPEAT");
			rgreetings = tt(jj.texts("GREETING"));
			rmids = tt(jj.texts("INTROS"));
			rbodies = tt(jj.texts("BODIES"));
			rends = tt(jj.texts("ENDS"));
		}
		
		rejected = tt(j.texts("REJECTED"));
		payed = tt(j.texts("PAYED"));
		afterRaid = tt(j.texts("AFTER_RAID"));
		allyHelp = tt(j.texts("HELP"));
		allyDead = tt(j.texts("HELP_DESTROYED"));
		allyFight = tt(j.texts("HELP_FIGHT"));
	}
	
	private ArrayList<CharSequence> tt(CharSequence[] tt) {
		RaiderText.insert.check(tt);
		return new ArrayList<CharSequence>(tt);
	}
	
}