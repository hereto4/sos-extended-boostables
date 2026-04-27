package settlement.room.law.guard;

import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.SETT;
import settlement.overlay.RADIUS_INTER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.bit.Bit;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.COORDINATEE;
import snake2d.util.rnd.RND;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class GuardInstance extends RoomInstance implements RADIUS_INTER{
	
	static final int crimesMax = 5;
	private static final long serialVersionUID = 1L;
	final static Bit standReserved = new Bit(0b01);
	private int[] crimes = new int[crimesMax];
	byte crimeI = 0;
	
	
	protected GuardInstance(ROOM_GUARD b, TmpArea area, RoomInit init) {
		super(b, area, init);
		

		for (COORDINATE c : body()) {
			if (is(c) && SETT.ROOMS().fData.tileData.get(c) == Constructor.codeLight) {
				int off = SETT.ROOMS().fData.tileData.get(c.x()+1, c.y()) == Constructor.codeLight ? C.TILE_SIZEH-1 : 0;
				SETT.LIGHTS().torchBig(c.x(), c.y(), off);
			}
		}
		
		
	
		employees().maxSet((int)b.constructor.guards.get(this));
		employees().neededSet((int)b.constructor.guards.get(this));
		activate();
		
		blueprintI().finder.report(blueprintI().service.get(this), 1);
		
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}

	@Override
	protected void activateAction() {
		
	}

	@Override
	protected void deactivateAction() {
		
	}
	
	@Override
	protected void dispose() {
		if (crimeI < crimesMax)
			blueprintI().finder.report(blueprintI().service.get(this), -1);
		
	}

	@Override
	public ROOM_GUARD blueprintI() {
		return (ROOM_GUARD) blueprint();
	}
	
	public boolean guardSpot(COORDINATEE planTile) {
		int a = body().width()*body().height();
		int tx = body().x1() + RND.rInt(body().width());
		int ty = body().y1() + RND.rInt(body().height());
		while(a -- >= 0) {
			if (is(tx, ty) && SETT.ROOMS().fData.tileData.is(tx, ty, Constructor.codeStand)) {
				int d = SETT.ROOMS().data.get(tx, ty);
				if (!standReserved.is(d)) {
					d = standReserved.set(d);
					SETT.ROOMS().data.set(this, tx, ty, d);
					planTile.set(tx, ty);
					return true;
				}
			}
			tx++;
			if (tx >= body().x2()) {
				tx = body().x1();
				ty++;
				if (ty >= body().y2()) {
					ty = body().y1();
				}
				
			}
		}
		return false;
	}
	
	public void guardSpotReturn(int tx, int ty) {
		if (!is(tx, ty) || !SETT.ROOMS().fData.tileData.is(tx, ty, Constructor.codeStand)) {
			throw new RuntimeException(is(tx, ty) + " " + SETT.ROOMS().fData.tileData.is(tx, ty, Constructor.codeStand));
		}
		int d = SETT.ROOMS().data.get(tx, ty);
		d = standReserved.clear(d);
		SETT.ROOMS().data.set(this, tx, ty, d);
	}
	
	void reportCriminal(Humanoid a) {
		if (crimeI == crimes.length) {
			throw new RuntimeException();
		}
		for (int i = 0; i < crimeI; i++) {
			if (crimes[i] == a.id())
				return;
		}
		crimes[crimeI++] = a.id();
		if (crimeI >= crimesMax)
			blueprintI().finder.report(blueprintI().service.get(this), -1);
	}
	
	public Humanoid pollCriminal() {
		if (crimeI >= crimesMax)
			blueprintI().finder.report(blueprintI().service.get(this), 1);
		while (crimeI > 0) {
			crimeI --;
			int ai = crimes[crimeI];
			ENTITY e = SETT.ENTITIES().getByID(ai);
			if (e != null && e instanceof Humanoid && !e.isRemoved()) {
				Humanoid a = (Humanoid) e;
				if (AI.modules().isCriminal(a))
					return a;
			}
		}
		return null;
	}

	@Override
	public int radius() {
		return (int) (blueprintI().constructor.radius.get(this)*eff());
	}

	public double eff() {
		return blueprintI().upgrades().upD(this)*(1.0 - getDegrade()*0.5)*(employees().employed()/(double)employees().max());
	}
	

	@Override
	public int rx() {
		return body().cX();
	}

	@Override
	public int ry() {
		return body().cY();
	}

}
