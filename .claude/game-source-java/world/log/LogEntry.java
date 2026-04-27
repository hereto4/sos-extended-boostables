package world.log;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.FBanner;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;

public class LogEntry {
	
	short ii = -1;
	short fa = -1;
	short fb = -1;
	short tx; 
	short ty;
	int day;
	public Str message = new Str(64);
	
	LogEntry(){
		
	}
	
	LogEntry(FileGetter file) throws IOException{
		ii = file.s();
		fa = file.s();
		fb = file.s();
		tx = file.s();
		ty = file.s();
		day = file.i();
		message.load(file);
	}
	
	public FBanner bannerA() {
		if (fa >= 0)
			return FACTIONS.getByIndex(fa).banner();
		return null;
	}
	
	public FBanner bannerB() {
		if (fb >= 0)
			return FACTIONS.getByIndex(fb).banner();
		return null;
	}
	
	public int daySinceStart() {
		return day;
	}
	
	public Icon icon() {
		return UI.icons().s.get(ii);
	}
	
	public int tx() {
		return tx;
	}
	
	public int ty() {
		return ty;
	}

	void save(FilePutter file) {
		file.s(ii);
		file.s(fa);
		file.s(fb);
		file.s(tx);
		file.s(ty);
		file.i(day);
		message.save(file);
	}

}