package view.ui.tourism;

import init.sprite.UI.UI;
import util.text.Dic;
import view.ui.manage.IFullView;

public final class UITourists extends IFullView {

	
	public UITourists() {
		super(Dic.¤¤Tourists, UI.icons().l.tourist);
		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addDownC(0, new Tourism(HEIGHT));
		

	}




}
