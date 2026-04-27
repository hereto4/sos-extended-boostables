package view.ui.message;

import init.sprite.UI.UI;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Font;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;

public class MessageText extends Message {

	private static final long serialVersionUID = 1L;
	private String[] paragraphs = new String[0];
	
	public MessageText(CharSequence title){
		super(title);
	}
	
	public MessageText(CharSequence title, CharSequence body){
		this(title);
		paragraph(body);
	}
	
	public MessageText(Json json){
		
		
		this(json.text("TITLE"));
		if (json.has("PARAGRAPHS")) {
			for (String s : json.texts("PARAGRAPHS"))
				paragraph(s);
		}else if (json.has("MESSAGE")) {
			paragraph(json.text("MESSAGE"));
		}
		
	}
	
	public MessageText paragraph(CharSequence text) {
		String[] ps = new String[paragraphs.length+2];
		for (int i = 0; i < paragraphs.length; i++) {
			ps[i] = paragraphs[i];
		}
		ps[ps.length-2] = " ";
		ps[ps.length-1] = "" + text;
		paragraphs = ps;
		return this;
	}
	
	@Override
	protected RENDEROBJ makeSection() {
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		Font f = UI.FONT().M;
		
		int mw = 0;
		
		for (String body : paragraphs) {
			int ei = 0;
			while(ei < body.length()) {
				int n = f.getEndIndex(body, ei, WIDTH);
				GTextR t = new GTextR(f, body.subSequence(ei, n));
				mw = Math.max(mw, t.body().width());
				rows.add(t);
				
				n = f.getStartIndex(body, n);
				ei =  f.getStartIndex(body, n);
			}
		}
		
		if (rows.size()*f.height() < HEIGHT) {
			GuiSection s = new GuiSection();
			for (RENDEROBJ r : rows)
				s.addDown(0, r);
			return s;
		}
		
		rows.add(new RENDEROBJ.RenderDummy(mw+16, 1));
		return new GScrollRows(rows, HEIGHT).view();
	}
	
	
}