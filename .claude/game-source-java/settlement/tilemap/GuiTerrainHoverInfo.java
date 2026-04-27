package settlement.tilemap;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.MAINTENANCE;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.SETT;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;

public final class GuiTerrainHoverInfo{

	private static CharSequence ¤¤Degrade= "¤Degrade:";
	private static CharSequence ¤¤Strength= "¤Strength:";
	private static CharSequence ¤¤Border= "¤This is a static entry point to your city. Keep this clear and reachable.";
	
	static {
		D.ts(GuiTerrainHoverInfo.class);
	}
	
	private GuiTerrainHoverInfo() {
		
	}
	
	

	
	public static void add(GBox box, int tx, int ty) {
		GText t;
		
		SETT.TILE_MAP().ground.hover(box, tx, ty);
		
		if(!TERRAIN().NADA.is(tx, ty)) {
			TERRAIN().get(tx, ty).hoverInfo(box, tx, ty);
			double st = GAME.ARMIES().map.strength.get(tx, ty)/C.TILE_SIZE;
			if (st > 0) {
				box.NL();
				box.textL(¤¤Strength);
				box.add(GFORMAT.f0(box.text(), st));
			}
			box.sep();
		};
		

		
		if (FLOOR().getter.is(tx, ty)) {
			t = box.text();
			t.lablify().add(FLOOR().getter.get(tx, ty).name());
			box.add(t);
			box.add(box.text().add(¤¤Degrade));
			box.add(GFORMAT.percInv(box.text(), FLOOR().degrade.get(tx, ty)));

			if (MAINTENANCE().isser.is(tx, ty)) {
				box.add(SPRITES.icons().s.hammer);
			}
			box.sep();
		}
		

		
		
		if (SETT.ENTRY().points.map.is(tx, ty)) {
			box.add(box.text().normalify2().add(¤¤Border));
			box.sep();
		}
		
	}

}
