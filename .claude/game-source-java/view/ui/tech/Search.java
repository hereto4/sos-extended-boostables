package view.ui.tech;

import game.boosting.BoostSpec;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.text.D;

final class Search extends GuiSection{

	private static CharSequence ¤¤no = "No results";
	
	static {
		D.ts(Search.class);
	}
	
	private final Node[] nodes = new Node[TECHS.ALL().size()];
	private final RENDEROBJ no = new GTextR(new GText(UI.FONT().M, ¤¤no).warnify());
	private final int width;
	private final int height;
	
	Search(int height, int width){
		this.width = width;
		this.height = height;
		NodeBoosts bos = new NodeBoosts();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node(TECHS.ALL().get(i), bos);
		}
		body().setDim(width, height);
	}
	
	GuiSection set(CharSequence s) {
		
		int x1 = body().x1();
		int y1 = body().y1();
		clear();
		
		int x = 0;
		int y = 0;
		
		for (TECH t : TECHS.ALL()) {
			if (!contains(t, s))
				continue;
			
			Node n = nodes[t.index()];
			
			if (x + 8 + Node.WIDTH > width) {
				
				y += 8 + Node.HEIGHT();
				if (y + 8 + Node.HEIGHT() > height)
					break;
			}
			
			
			
			n.body().moveX1Y1(x, y);
			add(n);
			x += 16 + Node.WIDTH;
		}
		
	
		
		body().moveX1Y1(x1, y1);
		body().setDim(width, height);
		
		if (x == 0 && y == 0) {
			add(no, body().cX(), body().cY());
		}
		
		return this;
	}
	
	
	private boolean contains(TECH t, CharSequence s) {
		if (Str.containsText(t.name(), s))
			return true;
		if (Str.containsText(t.desc(), s))
			return true;
		
		for (BoostSpec b : t.boosters.all()) {
			if (Str.containsText(s, b.boostable.name))
				return true;
			
		}
		
		return false;
	}
}
