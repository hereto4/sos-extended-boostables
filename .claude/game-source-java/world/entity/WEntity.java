package world.entity;

import static world.WORLD.ENTITIES;

import java.io.IOException;

import game.faction.Faction;
import init.constant.C;
import snake2d.Renderer;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.rendering.ShadowBatch;

public abstract class WEntity implements BODY_HOLDER{
	
	//int managerArrayIndex;
	final Rec hitBox;
	WEntity renderNext;
	WEntity regionNext;
	short gridX,gridY;
	short regionI = -1;
	int index = -1;
	
	public WEntity(int hitBoxWidth, int hitBoxHeight) {
		hitBox = new Rec(0, hitBoxWidth, 0, hitBoxHeight);
	}
	
	protected abstract void save(FilePutter file);
	
	protected abstract WEntity load(FileGetter file) throws IOException;

	@Override
	public final RECTANGLEE body(){
		return hitBox;
	}
	
	protected final void add() {
		renderNext = null;
		regionNext = null;
		regionI = -1;
		ENTITIES().add(this);
		addAction();
	}
	
	protected final void remove() {
		ENTITIES().remove(this);
		removeAction();
	}
	
	public final boolean added() {
		return index != -1;
	}
	
	public final int index() {
		return index;
	}
	
	protected void renderGround(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
	}
	protected abstract void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y);
	protected abstract void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y);
	protected void handleFow() {
		
	}
	
	protected void addAction() {
		
	};
	protected void removeAction() {
		
	};
	
	protected abstract void update(double ds);
	
	
	public int getZ(){
		return 0;
	}
	
	protected abstract WEntityConstructor<? extends WEntity> constructor();
	
	public short ctx() {
		return (short) (body().cX()>>C.T_SCROLL);
	}
	
	public short cty() {
		return (short) (body().cY()>>C.T_SCROLL);
	}
	
	
	public Faction faction() {
		return null;
	}
	
	public world.map.pathing.WPath path() {
		return null;
	}
	
}
