package game.battle.formation;

import java.io.IOException;

import init.constant.C;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class DivPositionImp implements DivPosition, SAVABLE{
	
	private int deployed = 0;	
	private final int half;
	private final int[] coos;
	private final Coo coo = new Coo();
	
	DivPositionImp(int maxMen) {
		coos = new int[maxMen*2];
		half = maxMen;		
	}
	
	@Override
	public COORDINATE tile(int i) {
		if (i >= deployed)
			return null;
		coo.set(coos[i]>>C.T_SCROLL, coos[i+half]>>C.T_SCROLL);
		return coo;
	}

	@Override
	public COORDINATE pixel(int i) {
		if (i >= deployed)
			return null;
		coo.set(coos[i], coos[i+half]);
		return coo;
	}
	
	@Override
	public int px(int i) {
		return coos[i];
	}
	
	@Override
	public int py(int i) {
		return coos[i+half];
	}
	
	@Override
	public int tx(int i) {
		return coos[i]>>C.T_SCROLL;
	}
	
	@Override
	public int ty(int i) {
		return coos[i+half]>>C.T_SCROLL;
	}
	
	public void init(int men) {
		if (men != this.deployed) {
			this.deployed = men;
		}
	}
	
	public void set(int i, int x, int y){
		coos[i] = x;
		coos[i+half] = y;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(deployed);
		file.is(coos);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		deployed = file.i();
		file.is(coos);
	}
	
	@Override
	public void clear() {
		deployed = 0;
	}

	@Override
	public int deployed() {
		return deployed;
	}

	public void copyposition(DivPosition pos) {
		this.deployed = pos.deployed();
		for (int i = 0; i < deployed; i++) {
			coos[i] = pos.px(i);
			coos[i+half] = pos.py(i);
		}
	}
	
}
