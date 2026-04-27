package settlement.battle.invasion;


import java.io.IOException;

import game.battle.util.DivGeneration;
import init.constant.Config;
import init.resources.STOCKPILE.StockpileImp;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;

public final class InvasionSpec {


	public final StockpileImp loot = new StockpileImp();
	public final ArrayList<DivGeneration> divs = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	public final int[] artillery = new int[SETT.ROOMS().ARTILLERY.size()];
	public int wx,wy = -1;
	public int fi = -1;
	boolean canBeAttacked = true;
	int ref = -1;
	
	public InvasionSpec() {

	}
	
	public InvasionSpec(FileGetter f) throws IOException {
		loot.load(f);
		int am = f.i();
		for (int i = 0; i < am; i++)
			divs.add(new DivGeneration(f));
		f.isE(artillery);
		wx = f.i();
		wy = f.i();
		fi = f.i();
		canBeAttacked = f.bool();
		ref = f.i();
	}

	public void save(FilePutter file) {
		loot.save(file);
		file.i(divs.size());
		for (DivGeneration g : divs)
			g.save(file);
		file.isE(artillery);
		file.i(wx);
		file.i(wy);
		file.i(fi);
		file.bool(canBeAttacked);
		file.i(ref);
	}

	
}
