package settlement.battle.invasion;

import game.battle.util.DivGeneration;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.message.MessageSection;

class Prompt extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static CharSequence ¤¤invasion = "¤Invasion!";
	static CharSequence ¤¤invasionD = "¤An enemy host stands gathered {0} of the city, preparing for an assault. They will attack at any moment. If they reach the throne, it is all over.";
	static CharSequence ¤¤count = "Our scouts report {0} soldiers.";
	static CharSequence ¤¤attack = "There is still a window of opportunity to attack them before they begin their assault. If we don't, we'll fight in the city.";
	
	static {
		D.ts(Prompt.class);
	}
	
	private final int men;
	private final int ref;
	private final String dir;
	
	Prompt(InvasionSpec spec, DIR dd){
		super(¤¤invasion);
		
		dir = "" + dd.getName();
		ref = spec.ref;
		int men = 0;
		for (DivGeneration g : spec.divs) {
			men += g.indus.length;
		}
		this.men = men;
	}

	@Override
	protected void make(GuiSection section) {
		
		paragraph(Str.TMP.clear().add(¤¤invasionD).insert(0, dir));
		
		
		paragraph(Str.TMP.clear().add(¤¤count).insert(0, men));
		
		paragraph(¤¤attack);
		
		
		section.addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤Attack){
			
			@Override
			protected void renAction() {
				InvasionSpec sp = SETT.INVADOR().spec(ref);
				activeSet(sp != null && sp.canBeAttacked);
			}
			
			@Override
			protected void clickA() {
				InvasionSpec sp = SETT.INVADOR().spec(ref);
				if (sp != null && sp.canBeAttacked) {
					VIEW.inters().messages.hide();
					new Attack(sp);
				}
			}
			
			
		});
		
		
	}
	
	
}
