package view.ui.diplomacy;

import java.io.Serializable;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.race.appearence.RPortrait;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GText;
import view.ui.message.MessageSection;

public class UIDipMess extends MessageSection{
	
	
	private static final long serialVersionUID = 1L;
	private final String mess;
	private final String desc;
	private final MessIntro intro;
	
	public UIDipMess(CharSequence title, CharSequence message, CharSequence desc, FactionNPC f) {
		super(title);
		this.desc = ""+desc;
		this.mess = ""+message;
		intro = new MessIntro(f);
	}

	
	@Override
	protected void make(GuiSection section) {
		
		
		paragraph(mess);
		section.addRelBody(8, DIR.N, intro.make());
		
		section.addRelBody(8, DIR.S, new GText(UI.FONT().M, desc).lablifySub().setMaxWidth(WIDTH));
		
	}
	
	static class MessFaction implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int fi;
		private final int fii;
		
		MessFaction(FactionNPC f){
			this.fi = f.index();
			this.fii = f.iteration();
		}
		
		public FactionNPC faction() {
			Faction f = FACTIONS.getByIndex(fi);
			if (f == null || !f.isActive() || !(f instanceof FactionNPC))
				return null;
			FactionNPC npc = (FactionNPC) f;
			if (npc.iteration() != fii)
				return null;
			return npc;
		}
		
		
	}
	
	static class MessIntro extends MessFaction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Induvidual iking;
		private final String sKingName;
		private final String sRealmIntro;
		
		MessIntro(FactionNPC f){
			super(f);
			Induvidual k = f.court().king().roy().induvidual;
			iking = new Induvidual(k.hType(), k.race());
			iking.copyFrom(k);
			STATS.NEEDS().clear(iking);
			
			sKingName = f.court().king().name.toString();
			sRealmIntro = f.nameIntro.toString() + " " + f.name.toString();
			
		}
		
		public GuiSection make() {
			
			GuiSection r = new GuiSection();
			if (faction() != null)
				r.add(faction().banner().HUGE, 0, 0);
			
			SPRITE pp = new SPRITE.Imp(RPortrait.P_WIDTH*2, RPortrait.P_HEIGHT*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					STATS.APPEARANCE().portraitRender(r, iking, X1, Y1, 2);
					if (iking.race().appearance().crown.crowns().size() > 0)
						iking.race().appearance().crown.crowns().get(0).renderScaled(r, X1, Y1+16, 2);
				}
			};
			
			r.addRelBody(32, DIR.E, pp);
			if (faction() != null)
				r.addRelBody(32, DIR.E, faction().banner().HUGE);
			
		

			r.addRelBody(4, DIR.S,  new GText(UI.FONT().H2, sKingName).lablify());
			
			r.addRelBody(4, DIR.S, new GText(UI.FONT().M, sRealmIntro).normalify());
			
			return r;
		}
		
	}

}