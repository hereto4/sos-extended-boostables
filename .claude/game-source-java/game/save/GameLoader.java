package game.save;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import game.GameSpec;
import game.VERSION;
import init.paths.PATHS;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.CORE_STATE.Constructor;
import snake2d.Errors;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import view.main.VIEW;

public class GameLoader implements Constructor {

	public final Path saveFile;
	public final String[] newScripts;
	
	public GameLoader(java.nio.file.Path path, String... newScripts){
		saveFile = path;
		this.newScripts = newScripts;
	}
	
	@Override
	public CORE_STATE getState() {
		FileGetter fg = null;
		GameSpec ss = null;
		CharSequence error = "No detectable error";
		
		try {
			fg = new FileGetter(saveFile, true);
			ss = GameSpec.get(fg, newScripts);
			CharSequence s = ss.crashCause();
			if (s != null)
				error = s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Errors.DataError("Save is corrupted and can not be loaded!" + System.lineSeparator() + " " + e, saveFile);
			
		}
		
		
		LOG.ln("LOADING GAME", "Game version: " + VERSION.VERSION_STRING + " save: " + VERSION.versionString(VERSION.VERSION));
		String m = "";
		for (String mm : ss.mods)
			m += mm + " | ";
		LOG.ln("mod: " + m);
		
		//final CharSequence p = ""+b.problem(true);

		try {
			VIEW v = GAME.create(ss);
			VIEW.inters().load.activate();
			CORE.getInput().clearAllInput();
			GAME.saver().load(fg);
			return v;
		} catch(Errors.GameError ee) {
			
			throw ee;
		}catch (Exception e) {
			e.printStackTrace(System.out);
			throw new Errors.DataError("Save is corrupted and can not be loaded!" + System.lineSeparator() + " " + error + System.lineSeparator() + " " + e, saveFile);
		} 
		
		

		
	}

	public final void set() {
		CORE.setCurrentState(this);
	}
	
	public static boolean quickload() {
		String ff = null;
		long time = -1;
		for (String s : PATHS.local().save().getFiles()) {
			
			if (ff == null || SaveFile.time(s) > time) {
				time = SaveFile.time(s);
				ff = s;
			}
		}
		
		if (ff != null) {
			CORE.setCurrentState(new GameLoader(PATHS.local().save().get(ff)));
			return true;
		}
		return false;
	}
	
	
}