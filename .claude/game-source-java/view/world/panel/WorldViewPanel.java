package view.world.panel;


import game.GAME;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.faction.player.emmi.Emissaries;
import init.settings.S;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.UIEmissaries;
import view.ui.top.UIPanelTop;
import view.ui.top.UIPanelTopButtL;
import view.ui.top.UIPanelTopButtS;
import view.world.WorldView;
import view.world.ui.panels.UIAdminPanel;
import view.world.ui.panels.UICaravanList;
import world.WORLD;
import world.army.AD;
import world.entity.haven.WHavenType;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuildPoints.RDBuildPoint;
import world.region.pop.RDRace;

public class WorldViewPanel{
	
	private int bi = 0;
	
	public WorldViewPanel(WorldView w, UIPanelTop top){
	
		D.gInit(this);
		CLICKABLE b;
		
		GuiSection bigButts = new GuiSection();
		
		GuiSection butts = new GuiSection();
		
		b = new UIPanelTopButtL(SPRITES.icons().s.flag) {
			
			private final GAME.Cache cache = new GAME.Cache(60);
			private int neighs;
			private int enemies;
			
			@Override
			protected double valueNext() {
				cache();
				return (1.0+neighs)/(1.0+enemies);
			}
			
			@Override
			protected double value() {
				return 1.0;
			}
			
			private void cache() {
				if (!cache.shouldAndReset())
					return;
				
				neighs = 0;
				enemies = DIP.WAR().player().size();
				
				for (FactionNPC f : FACTIONS.NPCs()) {
					if (RD.DIST().factionHasRegionBorderingPlayer(f))
						neighs ++;
				}
			}
			
			@Override
			protected boolean isActive() {
				return true;
			}
			
			@Override
			protected int getNumber() {
				cache();
				return neighs;
			}
			
			@Override
			protected void renAction() {
				selectedSet(w.UI.factions.openIs());
			}
			
			@Override
			protected void clickA() {
				w.UI.factions.open(null);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Factions);
				hov(b, DIP.ALLY());
				hov(b, DIP.VASSAL());
				hov(b, DIP.PACT());
				hov(b, DIP.TRADE());
				hov(b, DIP.WAR());
			}
			
			private void hov(GBox b, DipStance stance) {
				b.textLL(stance.name);
				b.tab(6);
				b.add(b.text().add(stance.player().size()));
				b.NL();
			}
			
		};
		addB(bigButts, b, "REGIONS");
		
		b = new UIPanelTopButtL(SPRITES.icons().s.world) {
			
			private final GAME.Cache cache = new GAME.Cache(60);
			private double worst;
			private double worstTarget;
			
			@Override
			protected double valueNext() {
				cache();
				return worstTarget;
			}
			
			@Override
			protected double value() {
				cache();
				return worst;
			}
			
			private void cache() {
				if (!cache.shouldAndReset())
					return;
				worst = 1.0;
				worstTarget = 1.0;
				for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
					Region r = FACTIONS.player().realm().region(i);
					if (!r.capitol()) {
						for (RDRace ra: RD.RACES().all) {
							worst = Math.min(ra.loyalty.get(r), worst);
							worstTarget = Math.min(worstTarget, ra.loyaltyTarget(r));
						}
					}
				}
				
			}
			
			@Override
			protected boolean isActive() {
				return FACTIONS.player().realm().regions() > 1;
			}
			
			@Override
			protected int getNumber() {
				return FACTIONS.player().realm().regions()-1;
			}
			
			@Override
			protected void renAction() {
				selectedSet(w.panels.added(w.UI.regions.playerList));
			}
			
			@Override
			protected void clickA() {
				if (w.panels.added(w.UI.regions.playerList))
					w.panels.remove(w.UI.regions.playerList);
				w.panels.add(w.UI.regions.playerList, true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Realm);
				b.text(Dic.¤¤RegionDesc);
				
				super.hoverInfoGet(text);
			}
			
		};
		addB(bigButts, b, "REGIONS");
		
		
		
		b = new UIPanelTopButtL(SPRITES.icons().s.sword) {

			
			@Override
			protected double valueNext() {
				return 1.0;
			}
			
			@Override
			protected double value() {
				return (AD.menTarget(null).faction(FACTIONS.player())+1.0)/(AD.men(null).faction(FACTIONS.player())+1.0);
			}
			
			@Override
			protected boolean isActive() {
				return AD.menTarget(null).faction(FACTIONS.player()) > 0;
			}
			
			@Override
			protected int getNumber() {
				return AD.menTarget(null).faction(FACTIONS.player());
			}
			
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().UI.armies.listIsOpen(VIEW.world().panels));
			}
			
			@Override
			protected void clickA() {
				if (VIEW.world().UI.armies.listIsOpen(VIEW.world().panels))
					VIEW.world().UI.armies.close(VIEW.world().panels);
				VIEW.world().UI.armies.openList(null, VIEW.world().panels);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Armies);
				
				
				super.hoverInfoGet(text);
			}
			
		};
		addB(bigButts, b, "ARMIES");
		
		

		
		b = new UIPanelTopButtS(SPRITES.icons().s.house) {

			
			@Override
			protected double valueNext() {
				return -1;
			}
			
			@Override
			protected double value() {
				return -1;
			}
			
			
			@Override
			protected boolean isActive() {
				return getNumber() > 0;
			}
			
			@Override
			protected int getNumber() {
				int am = 0;
				for (WHavenType r : WORLD.camps().types)
					am += WORLD.camps().camps(FACTIONS.player(), r);
				return am;
			}
			
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().panels.added(VIEW.world().UI.camps));
			}
			
			@Override
			protected void clickA() {
				if (VIEW.world().panels.added(VIEW.world().UI.camps))
					VIEW.world().panels.remove(VIEW.world().UI.camps);
				else
					VIEW.world().panels.add(VIEW.world().UI.camps, true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.add(WORLD.camps().info);
				
				
				super.hoverInfoGet(text);
			}
			
		};
		add(butts, b, "CAMPS");
		
		b = new UIPanelTopButtS(SPRITES.icons().s.wheel) {

			UICaravanList li = new UICaravanList();
			
			
			@Override
			protected double valueNext() {
				return -1;
			}
			
			@Override
			protected double value() {
				return -1;
			}
			
			
			@Override
			protected boolean isActive() {
				return true;
			}
			
			@Override
			protected int getNumber() {
				return li.all().size();
			}
			
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().panels.added(li));
			}
			
			@Override
			protected void clickA() {
				if (VIEW.world().panels.added(li))
					VIEW.world().panels.remove(li);
				else
					VIEW.world().panels.add(li, true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Inbound);
				super.hoverInfoGet(text);
			}
			
		};
		add(butts, b, "CARAVANS");

		b = new UIPanelTopButtS(SPRITES.icons().s.flags) {

			UIEmissaries li = new UIEmissaries();
			
			
			@Override
			protected double valueNext() {
				return value();
			}
			
			@Override
			protected double value() {
				int v = FACTIONS.player().emissaries.spent();
				if (v == 0)
					return 1;
				return (double)CLAMP.d(FACTIONS.player().emissaries.penaltyMul(), 0, 1);
			}
			
			
			@Override
			protected boolean isActive() {
				return FACTIONS.player().emissaries.spent() > 0 || FACTIONS.player().emissaries.produced() > 0; 
			}
			
			@Override
			protected int getNumber() {
				return FACTIONS.player().emissaries.available();
			}
			
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().panels.added(li));
			}
			
			@Override
			protected void clickA() {
				if (VIEW.world().panels.added(li))
					VIEW.world().panels.remove(li);
				else
					VIEW.world().panels.add(li, true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Emissaries.¤¤name);
				b.text(Emissaries.¤¤desc);
				super.hoverInfoGet(text);
			}
			
		};
		add(butts, b, "ENVOYS");
		
		b = new UIPanelTopButtS(SPRITES.icons().s.capitol) {

			UIAdminPanel li = new UIAdminPanel(w.panels);
			RDBuildPoint b = RD.BUILDINGS().costs.GOV;
			
			@Override
			protected double valueNext() {
				return value();
			}
			
			@Override
			protected double value() {
				return (double)(b.bo.get(FACTIONS.player())+1.0)/(b.bo.added(FACTIONS.player())+1.0);
			}
			
			
			@Override
			protected boolean isActive() {
				return b.consumed(FACTIONS.player()) > 0;
			}
			
			@Override
			protected int getNumber() {
				return (int)b.bo.get(FACTIONS.player());
			}
			
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().panels.added(li));
			}
			
			@Override
			protected void clickA() {
				if (VIEW.world().panels.added(li))
					VIEW.world().panels.remove(li);
				else
					VIEW.world().panels.add(li, true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox bb = (GBox) text;
				bb.title(b.bo.name);
				bb.text(b.bo.desc);
				super.hoverInfoGet(text);
			}
			
		};
		add(butts, b, "GOV");
		

		{

			
			

			
			butts.addRelBody(0, DIR.E, new Buttt(SPRITES.icons().m.crossair) {
				@Override
				protected void clickA() {
					VIEW.world().window.centererTile.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
				};
			}.hoverInfoSet(D.g("go to capital")));
			
			if (S.get().developer) {
				butts.addRelBody(0, DIR.E, new Buttt(SPRITES.icons().s.cog) {
					@Override
					protected void clickA() {
						VIEW.world().debug.show();
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						selectedSet(VIEW.world().debug.isActivated());
						super.render(r, ds, isActive, isSelected, isHovered);
					}
				}.hoverInfoSet(D.g("developer tools")));
			}
		}

		GuiSection s = new GuiSection();
		s.add(bigButts);
		s.addRightC(0, butts);
		
		top.addLeft(s);
		
		{
			s = new GuiSection();
			s.addRightC(0, GAME.EVENT().butt());
			s.addRightC(0, UIPanelTop.junk());
			s.addRightC(0, UIPanelTop.messages());
			s.addRightC(0, UIPanelTop.vToggle());
			s.addRightC(0, UIPanelTop.wLog());
			top.addRightRight(s);
		}
		
		
		
		new UIMinimap(top, w.uiManager, w.window, null);
	}
	
	private void add(GuiSection bb, RENDEROBJ o, String key) {
		bb.add(o, (bi/2)*80, (bi%2)*24);
		bi++;
	}
	
	private void addB(GuiSection bb, RENDEROBJ o, String key) {
		bb.addRightC(0, o);
	}
	
	private static class Buttt extends GButt.ButtPanel {

		public Buttt(SPRITE label) {
			super(label);
			setDim(32, 48);
		}
		
	}

	
}
