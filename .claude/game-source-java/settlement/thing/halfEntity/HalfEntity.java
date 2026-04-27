package settlement.thing.halfEntity;

import java.io.IOException;

import init.constant.C;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public abstract class HalfEntity implements BODY_HOLDER{
	
	//int managerArrayIndex;
	final Rec hitBox;
	HalfEntity renderNext;
	short gridX,gridY;
	int index = -1;
	
	public HalfEntity(int hitBoxWidth, int hitBoxHeight) {
		hitBox = new Rec(0, hitBoxWidth, 0, hitBoxHeight);
	}
	
	protected abstract void save(FilePutter file);
	
	protected abstract HalfEntity load(FileGetter file) throws IOException;
	
	@Override
	public final RECTANGLEE body(){
		return hitBox;
	}
	
	protected final void add() {
		renderNext = null;
		SETT.HALFENTS().add(this);
		addAction();
	}
	
	protected final void remove() {
		SETT.HALFENTS().remove(this);
		
		removeAction();
	}
	
	public final boolean added() {
		return index != -1;
	}
	
	public abstract void hoverInfo(GBox box);
	
	protected void renderBelow(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
	}
	
	protected abstract void render(Renderer r, ShadowBatch s, float ds, int x, int y);

	protected void renderAbove(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
	}
	
	
	protected void addAction() {
		
	};
	protected void removeAction() {
		
	};
	
	protected abstract void update(double ds);
	
	
	public int getZ(){
		return 0;
	}
	

	
	public int index() {
		return index;
	}
	
	protected abstract Factory<? extends HalfEntity> constructor();
	
	public int ctx() {
		return body().cX()>>C.T_SCROLL;
	}
	
	public int cty() {
		return body().cY()>>C.T_SCROLL;
	}
	
}
