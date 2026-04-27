package util.info;

import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import view.ui.wiki.WIKI;

public class INFO {

	public final CharSequence name;
	public final CharSequence names;
	public final CharSequence desc;
	public final ACTION wiki;

	public INFO(Json json, ACTION wiki) {
		if (!json.has("NAME"))
			json = json.json("INFO");
		name = json.text("NAME");
		if (json.has("NAMES"))
			names = json.text("NAMES");
		else
			names = name;
		desc = json.text("DESC");
		if (wiki == null)
			wiki = WIKI.add(json);
		this.wiki = wiki;
	}
	
	public INFO(Json json) {
		this(json, null);
	}
	
	public INFO(CharSequence name, CharSequence desc) {
		this(""+name, name+"s", desc, null);
	}
	
	public INFO(CharSequence name, CharSequence names, CharSequence desc, ACTION wiki) {
		this.name = name;
		this.names = names;
		this.desc = desc;
		this.wiki = wiki;
	}
	
	public void hover(GUI_BOX box) {
		box.title(name);
		box.text(desc);
		box.NL();
	}
}
