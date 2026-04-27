package view.sett.ui.bottom;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.value.Lock;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.subview.GameWindow;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class RoomUpgrader extends PlacableMulti{

	private static CharSequence ¤¤name = "Room upgrade";
	private static CharSequence ¤¤desc = "Upgrade the rooms that can be upgraded.";
	
	private static CharSequence ¤¤UPGRADE_MAX_REACHED = "¤Maximally Upgraded.";
	private static CharSequence ¤¤RESOURCES = "¤Not enough resources.";
	private static CharSequence ¤¤noSelect = "¤No rooms selected that can be upgraded.";
	private int[] resources = new int[RESOURCES.ALL().size()];
	private CharSequence error;
	static {
		D.ts(RoomUpgrader.class);
	}
	
	private final double iunprocessed = -2;
	private final double ierror = -1;
	private final double iok = 0;
	private boolean any = false;
	
	public RoomUpgrader() {
		super(¤¤name, ¤¤desc, UI.icons().l.upgrade);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		Room rr = SETT.ROOMS().map.get(tx, ty);
		if (rr == null)
			return E;
		return null;
		
	}
	
	@Override
	public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
		for (COORDINATE c : area.body()) {
			int tx = c.x();
			int ty = c.y();
			if (area.is(tx, ty)) {
				Room rr = SETT.ROOMS().map.get(tx, ty);
				if (rr == null)
					continue;
				GUTIL.flooder().setValue2(rr.mX(tx, ty),rr.mY(tx, ty), iunprocessed);				
			}
		}
		Arrays.fill(resources, 0);
		error = null;
		any = false;
		boolean room = false;
		outer:
		for (COORDINATE c : area.body()) {
			if (area.is(c)) {
				int tx = c.x();
				int ty = c.y();
				Room rr = SETT.ROOMS().map.get(tx, ty);
				if (rr == null)
					continue;
				int mx = rr.mX(tx, ty);
				int my = rr.mY(tx, ty);
				
				if (GUTIL.flooder().getValue2(mx, my) != iunprocessed)
					continue;
				GUTIL.flooder().setValue2(mx,my, ierror);
				
				CharSequence e = canUpgrade(rr, mx, my);
				if (e != null) {
					if (error == null) {
						error = e;
					}
					continue;
				}
				
				boolean can = true;
				room = true;
				for (int ri = 0; ri < resources(rr); ri++) {
					
					int am = resAm(rr, tx, ty, ri);
					RESOURCE res = res(rr, ri);
					resources[res.index()] += am;
					
					if (SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res) < resources[res.index()]) {
						can = false;
					}else {
						
					}
					
					
					
				}
				if (!can) {
					if (error == null) {
						error = ¤¤RESOURCES;
						
					}
					continue outer;
				}
				
				any = true;
				GUTIL.flooder().setValue2(mx,my, iok);
				
				
						
			}
		}
		
		if (!room && error == null)
			error = ¤¤noSelect;;
		
		return null;
	}
	
	private RESOURCE res(Room r, int ri) {
		return r.constructor().resource(ri);
	}
	
	private int resources(Room r) {
		return r.constructor().resources();
	}
	
	private int resAm(Room rr, int tx, int ty, int ri) {
		int current = rr.upgrade(tx, ty);
		return rr.resAmount(ri, current+1) - rr.resAmount(ri, current);
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
			PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
		
		isPlacable = false;
		areaIsPlacable = true;
		Room rr = SETT.ROOMS().map.get(tx, ty);
		if (rr != null) {
			int mx = rr.mX(tx, ty);
			int my = rr.mY(tx, ty);
			if (GUTIL.flooder().getValue2(mx, my) == iok)
				isPlacable = true;
			else if (error == ¤¤noSelect)
				areaIsPlacable = false;
		}
		
		if (!isPlacable)
			GCOLOR.MAP().BAD.bind();
		else if (!areaIsPlacable)
			GCOLOR.MAP().SOSO.bind();
		else
			GCOLOR.MAP().BEST.bind();
		
		super.renderPlaceHolder(r, mask, x, y, tx, ty, area, type, isPlacable, areaIsPlacable);
	}
	
	@Override
	public void placeInfo(GBox box, int oktiles, AREA area) {		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (resources[res.index()] > 0) {
				box.add(res.icon());
				box.add(GFORMAT.iofk(box.text(), resources[res.index()], SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)));
				box.NL();
			}
		}
		if (!any)
			box.error(error);
		else if (error != null) {
			box.add(box.text().warnify().add(error));
		}
	}

	@Override
	public void updateRegardless(GameWindow window, AREA selected) {
		Arrays.fill(resources, 0);
		super.updateRegardless(window, selected);
	}
	
	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		Room rr = SETT.ROOMS().map.get(tx, ty);
		if (rr == null)
			return;
		if (rr.mX(tx, ty) != tx || rr.mY(tx, ty) != ty)
			return;
		if (canUpgrade(rr, tx, ty) != null)
			return;

		int current = rr.upgrade(tx, ty);

		for (int ri = 0; ri < resources(rr); ri++) {
			
			int am = resAm(rr, tx, ty, ri);
			RESOURCE res = res(rr, ri);
			
			if (SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res) < am) {
				return;
			}
			
		}

		for (int ri = 0; ri < resources(rr); ri++) {
			
			int am = resAm(rr, tx, ty, ri);
			RESOURCE res = res(rr, ri);
			
			res.remove(am, RTYPE.CONSTRUCTION);
		}

		rr.upgradeSet(tx, ty, current+1);
		
	}
	
	
	
	private CharSequence canUpgrade(Room rr, int tx, int ty) {
		if (!(rr.blueprint() instanceof RoomBlueprintImp))
			return E;
		RoomBlueprintImp b = (RoomBlueprintImp) rr.blueprint();
		if (b.upgrades().max() == 0)
			return ¤¤UPGRADE_MAX_REACHED;
		int current = rr.upgrade(tx, ty);
		if (current >= b.upgrades().max())
			return ¤¤UPGRADE_MAX_REACHED;
		for (Lock<Faction> r : b.upgrades().requires(current+1).all()) {
			if (!r.unlocker.inUnlocked(FACTIONS.player())) {
				return Str.TMP.clear().add(Dic.¤¤Requires).add(':').s().add(r.unlocker.name);
			}
		}
		return null;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		Room rr = SETT.ROOMS().map.get(fromX, fromY);
		if (rr == null)
			return false;
		if (!rr.isSame(fromX, fromY, toX, toY))
			return false;
		if (rr.blueprint() instanceof RoomBlueprintImp) {
			RoomBlueprintImp b = (RoomBlueprintImp) rr.blueprint();
			if (b.upgrades().max() == 0)
				return false;
			int current = rr.upgrade(fromX, fromY);
			if (current >= b.upgrades().max())
				return false;
			return true;
		}
		return false;
		
	}
	

}
