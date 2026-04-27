package view.ui.tech;

import game.faction.player.PTech;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.text.StringInputSprite;
import util.text.Dic;
import view.ui.manage.IFullView;

public class UITechTree extends IFullView{

	final Tree tree;
	final Search search;
	final InfoBonuses bonuses;
	
	final CLICKABLE.ClickSwitch swit;

	final StringInputSprite filter = new StringInputSprite(16, UI.FONT().S) {
		@Override
		protected void change() {
			if (text() == null || text().length() == 0)
				swit.set(tree);
			else
				swit.set(search.set(text()));
		};
	};
	
	public UITechTree(){
		super(PTech.¤¤name, UI.icons().l.vial);
		
		filter.placeHolder(Dic.¤¤Filter);
		Info info = new Info(this, WIDTH);
		
		
		bonuses = new InfoBonuses(this, HEIGHT-(info.body().height()+16), WIDTH);
		search = new Search(info.body().height()+16, WIDTH);
		tree = new Tree(HEIGHT-(info.body().height()+16), WIDTH);
		swit = new CLICKABLE.ClickSwitch(tree);
		section.add(swit, 0, 0);
		
		
		bonuses.body().moveX1(section.body().x2()+8);
		bonuses.body().moveY1(section.body().y1());
		
		section.addRelBody(16, DIR.N, info);
		
//		section.add(oo);
//		
	}
	
//	void filter(CharSequence f) {
//		if (f == null || f.length() == 0)
//			swit.set(tree);
//		else
//			swit.set(search.set(f));
//	}
	
	@Override
	public boolean back() {
		if (swit.current() != tree) {
			filter.set(Dic.empty);
			return true;
		}
		return super.back();
	}
	
	void bonuses(boolean b) {
		if (b)
			swit.set(bonuses);
		else
			swit.set(tree);
	}
	
	boolean bonuses() {
		return swit.current() == bonuses;
	}
	

}
