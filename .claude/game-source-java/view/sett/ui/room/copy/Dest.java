package view.sett.ui.room.copy;

import settlement.main.SETT;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEAN;

final class Dest implements MAP_BOOLEAN, BODY_HOLDER{

	private final Source source;
	private int cx,cy;
	private Coo sourceCoo = new Coo();
	private int rot = 0;
	private Rec body = new Rec();
	
	Dest(Source source){
		this.source = source;
	}
	
	void init(int cx, int cy, int rot) {
		body.clear();
		this.rot = rot;
		this.cx = cx;
		this.cy = cy;
		
		int width = source.area().width();
		int height = source.area().height();
		
		for (int i = 0; i < this.rot; i++) {
			int ox = width;
			width = height;
			height = ox;
		}
		body.setDim(width+1, height+1);
		body.moveC(cx, cy);
	}

	@Override
	public boolean is(int tile) {
		return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
	}

	@Override
	public boolean is(int tx, int ty) {
		return source.is(transform(tx, ty));
	}
	
	public boolean sourceIs(int tx, int ty) {
		return source.is(tx, ty);
	}
	
	public boolean blocking(int tx, int ty) {
		return SETT.PLACA().solidityWill.is(transform(tx, ty));
	}
	
	public COORDINATE transform(int tx, int ty) {
		
		int dx = tx-cx;
		int dy = ty-cy;
		
		for (int i = 0; i < rot; i++) {
			int ox = dx;
			dx = dy;
			dy = -ox;
		}
		
		dx = source.area().cX() + dx;
		dy = source.area().cY() + dy;
		
		sourceCoo.set(dx, dy);
		return sourceCoo;
	}

	@Override
	public RECTANGLE body() {
		return body;
	}
	
	public int rot() {
		return rot;
	}

	
	
}
