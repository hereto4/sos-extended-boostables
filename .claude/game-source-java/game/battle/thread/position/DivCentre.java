package game.battle.thread.position;

import java.io.IOException;

import init.constant.C;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public final class DivCentre implements SAVABLE{

	int cx,cy;
	int cxSoft,cySoft;
	int squareCX, squareCY;
	short inPosition;
	
	//dir faceEnemyDirection (the best way to face the enemy
	// faceEnemyWidth
	
	public DivCentre() {
		
	}
	
	@Override
	public void save(FilePutter file) {
		file.s(inPosition);
		file.i(cx).i(cy);
		file.i(cxSoft).i(cySoft);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		inPosition = file.s();
		cx = file.i();
		cy = file.i();
		cxSoft = file.i();
		cySoft = file.i();
		squareCX = file.i();
		squareCY = file.i();
	}

	@Override
	public void clear() {
		cx = -1;
		cy = -1;
		cxSoft = -1;
		cySoft = -1;
		inPosition = 0;
		squareCX = -1;
		squareCY = -1;
	}
	
	public int cUnitX() {
		return cx;
	}

	public int cUnitY() {
		return cy;
	}
	
	/**
	 * 
	 * @return current positions centre pixel. -1 if invalid
	 */
	public int cX() {
		return cxSoft;
	}

	/**
	 * 
	 * @return current positions centre pixel. -1 if invalid
	 */
	public int cY() {
		return cySoft;
	}
	
	public int inFormation() {
		return inPosition; 
	}

	public int squareCX() {
		return squareCX;
	}

	public int squareCY() {
		return squareCY;
	}
	
	public int ctX() {
		return cxSoft/C.TILE_SIZE;
	}

	/**
	 * 
	 * @return current positions centre pixel. -1 if invalid
	 */
	public int ctY() {
		return cySoft/C.TILE_SIZE;
	}
	
}
