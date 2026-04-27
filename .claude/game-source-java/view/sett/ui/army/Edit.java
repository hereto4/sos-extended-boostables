package view.sett.ui.army;

import game.battle.div.Div;
import game.faction.FACTIONS;
import init.constant.Config;
import init.race.RACES;
import init.sprite.SPRITES;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.div.UIDivEditor;

final class Edit {

	private static CharSequence ¤¤title = "{0} divisions";
	private static CharSequence ¤¤Time = "¤The amount of days it will take to train this division to specification.";
	
	static {
		D.ts(Edit.class);
	}
	
	private final UIDivEditor editor = new UIDivEditor(STATS.BATTLE().TRAINING_ALL.size(), true, false, true, RACES.all());
	private GuiSection section = new GuiSection();
	private final ArrayList<Div> all = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final INT.IntImp men = new INT.IntImp(0, (int) Math.ceil(Config.battle().MEN_PER_DIVISION/10));
	
	public int realMen() {
		return men.get()*10;
	}
	

	
	Edit() {
		
		section.add(editor);
		
		{
			GuiSection s = new GuiSection();
			
			GStat ss = new GStat() {
				
				@Override
				public void update(GText text) {
					
					GFORMAT.i(text, (long) ti());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.text(¤¤Time);
				}
				
				double ti() {
					
					double am = ROOM_M_TRAINER.basicTrainingTimedays();
					
					for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
						am += t.room.TRAINING_DAYS*editor.div().training(t)/t.room.bonus().get(FACTIONS.player());
					}
					
					return am;
					
				}
				
			};
			
			s.addRightC(0, ss.hh(SPRITES.icons().m.time));
			
			s.addRightC(48, new GButt.ButtPanel(Dic.¤¤Accept) {
				
				@Override
				protected void clickA() {
				
					
					for (Div d : all) {
						editor.copyChanges(d.info);
					}
					
					VIEW.inters().popup.close();
				};
				
				@Override
				protected void renAction() {
					activeSet(editor.hasChanges());
				};
				
			}.setDim(180, 32));
			s.addRightC(0, new GButt.ButtPanel(Dic.¤¤cancel) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.close();
				};
				
			}.setDim(180, 32));
			
			section.addRelBody(8, DIR.S, s);
		}
		
	}
	
	public GuiSection get(LIST<Div> all, CLICKABLE trigger) {
		
		this.all.clearSloppy();
		this.all.add(all);

		editor.div().copyFrom(all.get(0).info);
		if (all.size() > 1)
			editor.div().nameE().clear().add(¤¤title).insert(0, all.size());
		editor.clearChanges();
		

		return section;
		
		
	}
	
}
