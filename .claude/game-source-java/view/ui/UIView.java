package view.ui;

import view.sett.ui.health.UIHealth;
import view.ui.div.UIDiv;
import view.ui.economy.UITreasury;
import view.ui.goods.UIGoods;
import view.ui.log.UILog;
import view.ui.manage.IManager;
import view.ui.profile.UILevel;
import view.ui.profile.UIProfile;
import view.ui.raider.UIRaiding;
import view.ui.tech.UITechTree;
import view.ui.tourism.UITourists;
import view.ui.wiki.WIKI;

public class UIView {

	public final UITreasury economy;
	public final UITourists tourists;
	public final UIGoods goods;
	public final UITechTree tech;
	public final UIRaiding raider;
	public final UIProfile profile;
	public final UILevel level;
	public final UIHealth health;
	public final UILog log = new UILog(null);
	public final WIKI wiki = new WIKI();
	public final UIDiv div = new UIDiv();
	public final IManager manager;
	public UIView() {
		economy = new UITreasury();
		goods = new UIGoods();
		tech = new UITechTree();
		level = new UILevel();
		profile = new UIProfile(true);
		health = new UIHealth();
		tourists = new UITourists();
		raider = new UIRaiding();
		manager = new IManager(this);
	}
	
}
