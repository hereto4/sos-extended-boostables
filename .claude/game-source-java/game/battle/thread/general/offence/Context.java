package game.battle.thread.general.offence;

import java.io.IOException;

import init.constant.Config;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitmap2D;
import util.data.DOUBLE.DoubleImp;
import util.data.INT.IntImp;

class Context implements SAVABLE{
	
	public final Bitmap2D blob = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final Bitmap2D block = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final ContextLines lines = new ContextLines();
	public IntImp checkI = new IntImp();
	public final DoubleImp value = new DoubleImp();
	public Bitmap1D deployedToLine = new Bitmap1D(Config.battle().DIVISIONS_PER_ARMY, false);
	public int[] distsToLine = new int[Config.battle().DIVISIONS_PER_ARMY];
	public int[] distsFromLineToBlob = new int[Config.battle().DIVISIONS_PER_ARMY];
	public int[] trickedDivs = new int[Config.battle().DIVISIONS_PER_ARMY];
	public double flanking = RND.rFloat()*50;
	public final UtilDivMap map = new UtilDivMap();
	
	Context(){

	}


	@Override
	public void save(FilePutter file) {
		blob.save(file);
		block.save(file);
		lines.save(file);
		checkI.save(file);
		value.save(file);
		deployedToLine.save(file);
		file.isE(distsToLine);
		file.isE(distsFromLineToBlob);
		file.isE(trickedDivs);
		
	}


	@Override
	public void load(FileGetter file) throws IOException {
		blob.load(file);
		block.load(file);
		lines.load(file);
		checkI.load(file);
		value.load(file);
		deployedToLine.load(file);
		file.isE(distsToLine);
		file.isE(distsFromLineToBlob);
		file.isE(trickedDivs);
	}


	@Override
	public void clear() {
		flanking = RND.rFloat()*50;
		lines.clear();
	}
	
	
	
}