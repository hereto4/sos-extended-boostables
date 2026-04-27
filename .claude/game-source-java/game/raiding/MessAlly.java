package game.raiding;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.raiding.MessDemand.Demand;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import view.ui.diplomacy.UIDipMess;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import world.region.RD;

class MessAlly {


	private static CharSequence ¤¤title = "Help!";
	private static CharSequence ¤¤desc = "If we don't pay this ransom, chances are neighbour allows for free passage though their lands, or gets attacked themselves.";
	private static CharSequence ¤¤destroy = "Raider destroys!";
	
	private static CharSequence ¤¤ftitle = "Raider Chased off";
	private static CharSequence ¤¤fdesc = "Due to your failure to pay off the raider, this faction is upset with you.";
	
	private static CharSequence ¤¤fwtitle = "Raider On their way";
	private static CharSequence ¤¤fwdesc = "Since we didn't pay our neighbour, they have now allowed for free passage for {0} into our lands. Muster the men!";
	
	private static CharSequence ¤¤requestHelp = "Request Assistance";
	private static CharSequence ¤¤requestHelpD = "Request that the faction attack this bandit. The consequences of this might are hard to foresee.";
	static {
		D.ts(MessAlly.class);
	}
	
	private MessAlly() {
		
	}
	


	
	static void help(Raider r, FactionNPC f) {
		
		Str.TMP.clear();
		Str.TMP.add(f.race().info.raiderMess.allyHelp.rnd());
		RaiderText.insert.set(Str.TMP, r);
		
		new Help(Str.TMP, f, new Demand(r)).send();
		
	}
	
	static void fight(Raider r, FactionNPC f) {
		
		if (r.army.power*(1 + RND.rFloat()*2) > f.offensivePower() + RD.MILITARY().power.getD(f.capitolRegion())) {
			FACTIONS.remove((FactionNPC) f, true);
			new MessDestroy(r).send();
		}else {
			Str.TMP.clear();
			Str.TMP.add(f.race().info.raiderMess.allyFight.rnd());
			RaiderText.insert.set(Str.TMP, r);
			ROPINIONS.GIFTS().makeDeal(f, -1);
			new Fight(Str.TMP, f).send();
		}
		
	}
	
	static void letThrough(Raider r, FactionNPC f) {
		
		Str.TMP.clear();
		Str.TMP.add(¤¤fwdesc);
		Str.TMP.insert(0, r.name);
		
		new MessageText(¤¤fwtitle, Str.TMP).send();
		
	}
	
	private static class Help extends UIDipMess {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Demand demand;
		private boolean requested = false;
		
		private Help(CharSequence message, FactionNPC f, Demand demand) {
			super(¤¤title, message, ¤¤desc, f);
			this.demand = demand;
		}
		
		@Override
		protected void make(GuiSection section) {
			super.make(section);
			section.addRelBody(8, DIR.S, demand.section(true));
			section.addRelBody(8, DIR.S, new GButt.ButtPanel(¤¤requestHelp) {
				
				@Override
				protected void renAction() {
					selectedSet(requested);
					activeSet(demand.canRespond());
				};
				
				@Override
				protected void clickA() {
					if (!requested && demand.canRespond()) {
						requested = true;
						GAME.raiders().current.setAllyFight();
					}
				};
				
			}.hoverInfoSet(¤¤requestHelpD));
		}
		
	}
	
	private static class Fight extends UIDipMess {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private Fight(CharSequence message, FactionNPC f) {
			super(¤¤ftitle, message, ¤¤fdesc, f);
		}
		
	}
	
	
	
	static class MessDestroy extends MessageSection{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Raider raider;
		
		public MessDestroy(Raider raider) {
			super(¤¤destroy);
			this.raider = raider;
		}

		@Override
		protected void make(GuiSection section) {
			
			Str.TMP.clear();
			Str.TMP.add(raider.indu.race().info.raiderMess.allyDead.rnd());
			RaiderText.insert.set(Str.TMP, raider);
			
			paragraph(Str.TMP);
			
			section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
			
		}

	}

}
