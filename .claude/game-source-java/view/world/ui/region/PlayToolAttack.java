package view.world.ui.region;

import game.faction.Faction;
import game.faction.diplomacy.DIP;
import init.sprite.SPRITES;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.tool.PlacableSimpleTile;
import view.tool.ToolConfig;
import view.tool.ToolManager;
import world.WORLD;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

abstract class PlayToolAttack extends PlacableSimpleTile {

	private static CharSequence ¤¤noSoldiers = "¤Region has no soldiers to attack with.";
	private static CharSequence ¤¤noRange = "¤Can't use garrison to attack outside of the region borders.";
	private static CharSequence ¤¤def = "¤Select an army within your regions' borders to attack.";
	private static CharSequence ¤¤question = "¤Are you sure you wish to declare war on {0} and attack {1} with your garrison?";
	private static CharSequence ¤¤besiged = "¤All exits are blocked. Can only sally out and attack the besieging army.";
	
	static {
		D.ts(PlayToolAttack.class);
	}
	
	private Region reg;
	
	private final ToolManager tools;
	private final boolean dismissable;
	
	
	private WArmy aa;
	private ACTION attack = new ACTION() {

		@Override
		public void exe() {
			DIP.WAR().set(reg.faction(), aa.faction());
			WORLD.BATTLES().regAttack(reg, aa);
			
		}
		
	};
	
	public PlayToolAttack(ToolManager tools) {
		this(tools, false);
	}
	
	public PlayToolAttack(ToolManager tools, boolean dismissable) {
		super(Dic.¤¤Attack, "");
		this.tools = tools;
		this.dismissable = dismissable;
	}

	void add(Region reg) {
		this.reg = reg;
		tools.place(this, config);
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty) {
		

		
		if (RD.MILITARY().garrison.get(reg) == 0 || RD.MILITARY().divisions(reg).size() == 0) {
			return ¤¤noSoldiers;
		}
		
		
		WArmy ok = null;
		CharSequence prob = ¤¤def;
		
		for (WArmy a : WORLD.ENTITIES().armies.fillTile(tx, ty)) {
			if (a.faction() != reg.faction()) {
				if (WORLD.BATTLES().besiged(reg) && !a.besieging(reg)) {
					prob = ¤¤besiged;
				}else if (!reg.is(a.ctx(), a.cty()) && !a.besieging(reg))
					prob = ¤¤noRange;
				else {
					ok = a;
					WORLD.OVERLAY().hoverEntity(a);
					VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
				}
			}
		}
		
		if (ok == null)
			return prob;
		return null;
	}
	
	
	@Override
	public void place(int tx, int ty) {
		
		for (WArmy a : WORLD.ENTITIES().armies.fillTile(tx, ty)) {
			if (a.faction() != reg.faction()) {
				if (WORLD.BATTLES().besiged(reg) && !a.besieging(reg)) {
					;
				}else if (!reg.is(a.ctx(), a.cty()) && !a.besieging(reg))
					;
				else {
					if (!DIP.WAR().is(a.faction(), reg.faction())) {
						aa = a;
						Str.TMP.clear().add(¤¤question).insert(0, Faction.name(aa.faction())).insert(1, aa.name);
						VIEW.inters().yesNo.activate(Str.TMP, attack, ACTION.NOP, dismissable);
						return;
					}else {
						WORLD.BATTLES().regAttack(reg, a);
						return;
					}
				}
			}
		}
//		
//		for (WArmy a : WORLD.ENTITIES().armies.fillTile(tx, ty)) {
//			if (a.faction() != reg.faction()) {
//				WORLD.OVERLAY().hoverEntity(a);
//				
//				if (!FACTIONS.DIP().war.is(a.faction(), reg.faction())) {
//					aa = a;
//					Str.TMP.clear().add(¤¤question).insert(0, Faction.name(aa.faction())).insert(1, aa.name);
//					VIEW.inters().yesNo.activate(Str.TMP, attack, ACTION.NOP, dismissable);
//				}else {
//					WORLD.BATTLES().regAttack(reg, a);
//				}
//				
//				
//			}
//		}
//		
//		for (WEntity e : WORLD.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
//			if (e instanceof WArmy && e.faction() != reg.faction()) {
//				WORLD.BATTLES().regAttack(reg, (WArmy)e);
//				return;
//			}
//		}
		
	}
	
	final ToolConfig config = new ToolConfig() {
		
		@Override
		public void deactivateAction() {

		};
		
		@Override
		public void update(boolean UIHovered) {
			if (!added())
				tools.place(null, null, false);
			else if (dismissable) {
				WORLD.OVERLAY().hover(reg);
			}
		};
		
		@Override
		public boolean back() {
			return dismissable;
		};
	
	};
	
	abstract boolean added();

}
