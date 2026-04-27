package view.ui.message;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Font;
import util.gui.misc.GText;

public abstract class MessageSection extends Message {

	private static final long serialVersionUID = 1L;
	protected static final int WIDTH = 900;
	private static final int PM = C.SG*10;
	private transient GuiSection section;
	
	
	public MessageSection(CharSequence title) {
		super(title);
	}
	
	protected MessageSection paragraph(CharSequence text) {
		GText t = new GText(UI.FONT().M, text).clickify();
		t.setMaxWidth(WIDTH-2*PM);
		t.adjustWidth();
		section.add(t, 0, section.getLastY2()+PM);
		return this;
	}
	
	protected MessageSection paragraph(CharSequence text, Font font, COLOR color) {
		GText t = new GText(font, text).color(color);
		t.setMaxWidth(WIDTH-2*PM);
		t.adjustWidth();
		section.add(t, 0, section.getLastY2()+PM);
		return this;
	}

	@Override
	protected GuiSection makeSection() {
		section = new GuiSection();
		make(section);
		return section;
	}
	
	protected abstract void make(GuiSection section);
	
	
}