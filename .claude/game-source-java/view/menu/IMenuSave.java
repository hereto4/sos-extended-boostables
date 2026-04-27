package view.menu;

import game.GAME;
import game.VERSION;
import game.faction.FACTIONS;
import game.save.SaveFile;
import init.constant.C;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.table.GScrollable;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

class IMenuSave extends GuiSection implements STRING_RECIEVER{
	
	private final CLICKABLE overwrite;
	private final CLICKABLE delete;
	private ACTION successfullAction;
	
	private SaveFile[] saves = new SaveFile[0];
	
	private int selectedSave = -1;
	
	private final ACTION overwriteAction;
	
	private static CharSequence ¤¤¤nameYour = "¤Name your save-game";
	private static CharSequence ¤¤failed = "¤failed to be overwritten";
	private static CharSequence ¤¤success = "¤successfully overwritten";
	private static CharSequence ¤¤overwrite = "¤overwrite";
	private static CharSequence ¤¤successSave = "{0} successfully saved!";
	private static CharSequence ¤¤charsAllowed = "Only characters: {0} are allowed!";
	private static CharSequence ¤¤fail = "Save failed. See error report!";
	
	static {
		D.ts(IMenuSave.class);
	}
	
	IMenuSave(IMenu m, Font font, Font small, ACTION successfullAction) {
		
		
		MenuScreen sc = new MenuScreen(Dic.¤¤save, GCOLOR.T().H1) {
			
			@Override
			protected void back() {
				m.setMain();
			}
		};
		
		this.successfullAction = successfullAction;
		
		SaveEntry[] entries = new SaveEntry[] {
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
		};
		
		GScrollable scroll = new GScrollable(entries) {
			
			@Override
			public int nrOFEntries() {
				return saves.length;
			}
			
		};
		
		scroll.getView().body().centerIn(C.DIM());
		add(scroll.getView());

		
		//NEW
		CLICKABLE newButt = new MenuScreen.ScreenButton(Dic.¤¤new) {
			@Override
			protected void clickA() {
				String name = FACTIONS.player().name + "-";
				KeyMap<String> m = new KeyMap<>();
				for (SaveFile f : saves) {
					if (f.name.startsWith(name)) {
						String n = f.name.substring(name.length(), f.name.length());
						m.putReplace(n, n);
					}
				}
				
				String ph = "";
				
				for (int i = 0; i < 512; i++) {
					String k = ""+i;
					if (!m.containsKey(k)) {
						ph = name + k;
						break;
					}
				}
				
				
				VIEW.inters().input.requestInput(IMenuSave.this, ¤¤¤nameYour, ph);
			}
		};
		sc.addButt(newButt);


		//OVERWRITE
		GButt yes = new GButt.Glow(Dic.¤¤confirm);
		yes.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				
				PATHS.local().save().delete(saves[selectedSave].fullName);
				if (GAME.saver().save(SaveFile.stamp(saves[selectedSave].name)) == null) {
					VIEW.inters().fullScreen.activate(saves[selectedSave].name + " " + ¤¤failed, COLOR.RED100, null);
				}else {
					VIEW.inters().fullScreen.activate(saves[selectedSave].name + " " + ¤¤failed, COLOR.RED100, null);
				}
				VIEW.inters().fullScreen.activate(saves[selectedSave].name + " " + ¤¤success, COLOR.WHITE100, successfullAction);
				m.setMain();
			}
		});
		GButt no = new GButt.Glow(Dic.¤¤cancel);
		
		overwriteAction = new ACTION() {
			@Override
			public void exe() {
				VIEW.inters().fullScreen.activate(¤¤overwrite + " " + saves[selectedSave].name + "?", COLOR.WHITE100, null, yes, no);
			}
		};
		overwrite = new MenuScreen.ScreenButton(¤¤overwrite) {
			
			@Override
			protected void renAction() {
				activeSet(selectedSave != -1);
			}
			
		};
		overwrite.clickActionSet(overwriteAction);
		sc.addButt(overwrite);
		
		//DELETE
		GButt yes2 = new GButt.Glow(Dic.¤¤confirm);
		yes2.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				PATHS.local().save().delete(saves[selectedSave].fullName);
				VIEW.inters().fullScreen.activate(saves[selectedSave].name + " deleted!", COLOR.WHITE100, null);
				populateSaves();
			}
		});
		
		delete = new MenuScreen.ScreenButton(Dic.¤¤delete) {
			@Override
			protected void clickA() {
				VIEW.inters().fullScreen.activate(Dic.¤¤delete + " " + saves[selectedSave].name, COLOR.WHITE100, null, yes2, no);
			}
			
			@Override
			protected void renAction() {
				activeSet(selectedSave != -1);
			}
		};
		sc.addButt(delete);
		
		
		
		add(sc);
		moveLastToBack();
		
		populateSaves();
		
		
	}
	
	private void populateSaves(){
		saves = SaveFile.list();
		

		selectedSave = -1;
		overwrite.activeSet(false);
		delete.activeSet(false);
		
	}

	@Override
	public void acceptString(CharSequence string) {
		
		if (string == null)
			return;
		
		if (!FileManager.NAME.okName(string)){
			Str.TMP.clear().add(¤¤charsAllowed).insert(0,  FileManager.NAME.legalChars);
			VIEW.inters().fullScreen.activate(Str.TMP, COLOR.RED100, null);
			return;
		}
		
		for (int i = 0; i < saves.length; i++) {
			if ((""+ saves[i].name).contentEquals(string)) {
				selectedSave = i;
				overwriteAction.exe();
				return ;
			}
		}
		
		if (GAME.saver().save(SaveFile.stamp(string)) == null) {
			VIEW.inters().fullScreen.activate(¤¤fail, COLOR.RED100, null);
			return;
		}else {
			VIEW.inters().fullScreen.activate(¤¤fail, COLOR.RED100, null);
		}
		
		
		VIEW.inters().menu.setMain();
		Str.TMP.clear().add(¤¤successSave).insert(0, string);
		VIEW.inters().fullScreen.activate(Str.TMP, COLOR.WHITE100, successfullAction);
	}
	
	private class SaveEntry extends Savebutt {
		
		public SaveEntry() {
			
		}

		@Override
		protected void clickA() {
			selectedSave = index;
			overwrite.activeSet(true);
			delete.activeSet(true);
			if (MButt.LEFT.isDouble()) {
				overwriteAction.exe();
			}
		}

		@Override
		protected boolean selected(int index) {
			return index == selectedSave;
		}

		@Override
		protected SaveFile save(int index) {
			return saves[index];
		}
		
	}
	
	private static abstract class Savebutt extends CLICKABLE.ClickableAbs implements ScrollRow {

		private static GText version = new GText(UI.FONT().M, 16);
		int index = -1;
		
		public Savebutt() {
			body.setWidth(1000);
			body.setHeight(28);
		}
		
		@Override
		public void init(int index) {
			this.index = index;
		}
		
		private Font font() {
			return UI.FONT().H2;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			SaveFile s = save(index);
			
			if (s == null)
				return;
			
			version.clear();
			version.add(VERSION.versionMajor(s.version));
			version.add('.');
			version.add(VERSION.versionMinor(s.version));
			if (VERSION.VERSION_MAJOR != VERSION.versionMajor(s.version)) {
				COLOR.RED100.bind();
			}else if (s.problem() != null) {
				COLOR.YELLOW100.bind();
			}else {
				if (selected(index)) {
					GCOLOR.T().SELECTED.bind();
				}else if (isHovered){
					GCOLOR.T().HOVERED.bind();
				}
			}
			font().render(r, version, body().x1(), body().y1());
			
			if (selected(index)) {
				GCOLOR.T().SELECTED.bind();
			}else if (isHovered){
				GCOLOR.T().HOVERED.bind();
			}else {
				GCOLOR.T().CLICKABLE.bind();
			}
			
			font().render(r, s.name, body().x1() + 60, body().y1());
			
			
			version.clear().add('p').s();
			GFORMAT.i(version, s.pop);
			font().render(r, version, body().x1() + 740, body().y1());
			
			font().render(r, s.ago, body().x1() + 820, body().y1());
			COLOR.unbind();
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			SaveFile s = save(index);
			if (s != null) {
				CharSequence p = s.problem();
				if (p != null)
					((GBox)text).error(p);
			}		
		}
		
		protected abstract boolean selected(int index);
		protected abstract SaveFile save(int index);
		
	}
	
}
