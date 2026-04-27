package view.sett.ui.noble;

import game.GAME;
import game.boosting.BoostSpec;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.POP_CL;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import view.interrupter.ISidePanel;

public final class UINobles extends ISidePanel{

	private static CharSequence ¤¤expla = "To assign another noble you must click a subject and elevate them from there.";
	static {
		D.ts(UINobles.class);
	}
	final NobleAssigns assigns = new NobleAssigns();
	
	public UINobles() {
		titleSet(HCLASSES.NOBLE().names);
		
		section.addRelBody(0, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, GAME.NOBLE().active().size(), (int)GAME.NOBLE().MAX.get(POP_CL.clP()));
				
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤expla);
				b.NL();
				GAME.NOBLE().MAX.hoverDetailed(b, POP_CL.clP(), null, true);
			};
			
		}.hv(HCLASSES.NOBLE().names));
		
		section.addRelBody(80, DIR.E, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, GAME.NOBLE().ranksAllocated(), (int)GAME.NOBLE().MAX_RANKS.get(POP_CL.clP()));
				
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(GAME.NOBLE().MAX_RANKS.desc);
				b.NL();
				GAME.NOBLE().MAX_RANKS.hoverDetailed(b, POP_CL.clP(), null, true);
			};
			
		}.hv(GAME.NOBLE().MAX_RANKS.name));
		
		section.addRelBody(80, DIR.E, new GButt.ButtPanel(UI.icons().m.plus) {
			

			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				
				for (int si = 0; si < GAME.NOBLE().boosters.all().size(); si++) {
					BoostSpec s = GAME.NOBLE().boosters.all().get(si);
					double v = s.get(POP_CL.clP());
					if (v > 0) {
						GAME.NOBLE().boosters.hover(b, s, v, 0);
						b.tab(8);
						
						b.NL();
					}
				}
				
			}
			
		});
		
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return GAME.NOBLE().active().size();
			}
		};
		
		bu.column(null, NobleRow.width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new NobleRow(ier);
			}
		});
		
		section.addRelBody(8, DIR.S, bu.createHeight(HEIGHT-32-section.body().height(), false));
		
		
	}

}
