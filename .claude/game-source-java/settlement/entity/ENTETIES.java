package settlement.entity;

import static settlement.main.SETT.ENTITIES;

import java.io.IOException;

import game.GAME;
import game.debug.Profiler;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT.SettResource;
import settlement.path.AVAILABILITY;
import settlement.path.AvailabilityListener;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.bit.Bits;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.ADDABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.IntegerStack;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;
import view.sett.ui.minimap.UIMinimapSettConfig;
import view.tool.PLACABLE;
import view.tool.PlacableSimple;

public class ENTETIES extends SettResource{
	
	public final static int MAX = 40000;
	public final static int MM = MAX + 20000;
	private final Grid grid;
	private final ENTITY[] ents = new ENTITY[MM];
	private final IntegerStack freeIndexes = new IntegerStack(MM);
	private final Bits order = new Bits(0b0000_0000_0000_0011_1111_1111_1111_1111);
	private final Bits count = new Bits(0b0111_1111_1111_1100_0000_0000_0000_0000);
	private int eLastIndex = -1;
	private final Tree<ENTITY> renderables = new Tree<ENTITY>(8000) {

		@Override
		protected boolean isGreaterThan(ENTITY current, ENTITY cmp) {
			double d = current.height()+current.physics.getZ() - (cmp.height()+cmp.physics.getZ());
			if (d > 0)
				return true;
			return false;
		}
		
	};
	private final ArrayList<ENTITY> temp = new ArrayList<ENTITY>(3000);
	private final Rec lWin = new Rec();
	
	public final MAP_BOOLEAN submerged = new SubmergedMap();
	
	public ENTETIES(){
		super("ENTETIES", true);
		grid = new Grid();
		
		freeIndexes.fill();
		eLastIndex = 0;
		
		new AvailabilityListener() {
			@Override
			protected void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
				if (!playerChange || a.player >= 0)
					return;
				
				for (ENTITY e : getAtTile(tx, ty)) {
					ResolverTile.trapped(e);
					if (!e.isRemoved() && !e.physics.MoveCheck()){
						e.physics.initMoveCheck();
						grid.remove(e);
						grid.add(e);
					}
				}
				
			}
		};
		
		IDebugPanelSett.add(remove);
		
	}
	
	@Override
	protected void save(FilePutter file) {
		
		
		freeIndexes.save(file);
		file.i(eLastIndex);
		for (int iter = 0; iter <= eLastIndex; iter++){
			ENTITY e = ents[iter];
			if (e == null) {
				file.i(0);
				continue;
			}else if (e instanceof Animal) {
				file.i(1);
			}else if (e instanceof Humanoid) {
				file.i(2);
			}else {
				throw new RuntimeException(""+e.getClass());
			}
			e.save(file);
		}
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		
		grid.clear();
		for (int iter = 0; iter <= eLastIndex; iter++){
			ents[iter] = null;
		}
		freeIndexes.load(file);
		int l = file.i();
		
		for (int iter = 0; iter <= l; iter++){
			int in = file.i();
			if (in == 0) {
				continue;
			}else if (in == 1) {
				ents[iter] = new Animal(file);
			}else if (in == 2) {
				ents[iter] = new Humanoid(file);
			}else {
				throw new IOException(""+in);
			}
			if (ents[iter] != null) {
				grid.addRaw(ents[iter]);
				if (iter > eLastIndex)
					eLastIndex = iter;
			}
		}
		
		int old = freeIndexes.size();
		int am = 0;
		eLastIndex = 0;
		freeIndexes.clear();
		
		for (int i = 0; i < MM; i++) {
			if (ents[i] != null) {
				am++;
				if (i > eLastIndex)
					eLastIndex = i;
			}else {
				freeIndexes.push(i);
			}
		}
		
		if (MM-old != am)
			GAME.Notify(""+(MM-old) + " " + am);
		

	}
	
	@Override
	protected void clear() {
		grid.clear();
		for (int iter = 0; iter <= eLastIndex; iter++){
			ents[iter] = null;
		}
		eLastIndex = -1;
		freeIndexes.clear();
		for (int i = 0; i < MM; i++) {
			freeIndexes.push(i);
		}
	}
	
	/**
	 * Add e to this handler. E will be updated and moved. Do not add an entity twice!
	 * @param e
	 */
	boolean add(ENTITY e, boolean collide){
		if (e.handlerId != -1)
			throw new RuntimeException("entity already added!");
		
		if (freeIndexes.isEmpty())
			return false;
		
		int i = freeIndexes.pop();
		
		
		int oi = order.get(i);
		if (oi > eLastIndex)
			eLastIndex = oi;
		
		int ci = count.get(i)+1;
		if (ci > count.mask)
			ci = 0;
		i = count.set(i, ci);
		ents[oi] = e;
		e.handlerId = i;
		e.physics.initMoveCheck();
		grid.add(e, collide);
		return true;
	}
	
	public void moveIntoTheTheUnknown(ENTITY e) {
		grid.remove(e);
	}
	
	public void returnFromTheTheUnknown(ENTITY e) {
		if (e.gx == -1 && e.gy == -1)
			grid.add(e, false);
		
	}
	
	/**
	 * 
	 * @return an array that you shouldn't mess with. Read only!
	 */
	public ENTITY[] getAllEnts(){
		return ents;
	}
	
	public int Imax() {
		return eLastIndex;
	}
	
	public void renderA(Renderer r, ShadowBatch shadowBatch, float ds, RECTANGLE renWindow, int offX, int offY) {
		
		shadowBatch.setHard();
		lWin.setWidth(renWindow.width()+200).setHeight(renWindow.height()+200).centerIn(renWindow);
		int offXs = (int) (offX - renWindow.x1());
		int offYs = (int) (offY - renWindow.y1());
		
		fill(lWin, renderables);
		
		if (CORE.renderer().getZoomout() >= 2) {
			while(renderables.hasMore()) {
				ENTITY e = renderables.pollSmallest();
				e.renderSimple(r, shadowBatch, ds, offXs, offYs);
			}
		}else {
			while(renderables.hasMore()) {
				ENTITY e = renderables.pollSmallest();
				e.render(r, shadowBatch, ds, offXs, offYs);
			}
		}
		
		
		shadowBatch.setSoft();
		
	}
	
	public void renderZoomed(Renderer r, ShadowBatch shadowBatch, float ds, RECTANGLE renWindow, int offX, int offY, UIMinimapSettConfig con) {
		
		
	
		for (ENTITY e : ENTITIES().getAllEnts()) {
			if (e == null)
				continue;
			
			
			int x1 = e.physics.body().cX();
			int y1 = e.physics.body().cY();
			
			
			if (!renWindow.holdsPoint(x1, y1))
				continue;
			
			DIR d = e.speed.dir();
			
			x1 -= (renWindow.x1()-offX) + C.TILE_SIZEH;
			y1 -= (renWindow.y1()-offY) + C.TILE_SIZEH;
			COLOR c = con.col(e);
			if (c != null) {
				c.bind();
				SPRITES.cons().ICO.arrows_entity.getC(d.ordinal()).render(r, x1, y1);
			}
		}
		COLOR.unbind();
		
	}
	
	/**
	 * 
	 * @param coo
	 * @param result
	 */
	public void fill(Coo coo, ADDABLE<ENTITY> result){
		grid.fill(coo.x(), coo.y(), result);
	}
	
	/**
	 * 
	 * @param area
	 * @param result
	 */
	public void fill(RECTANGLE area, ADDABLE<ENTITY> result){
		grid.fill(area, result);
	}
	
	public LIST<ENTITY> fill(RECTANGLE pixels){
		temp.clear();
		grid.fill(pixels, temp);
		return temp;
	}
	
	public LIST<ENTITY> fillTiles(RECTANGLE tiles){
		lWin.moveX1Y1(tiles.x1()<<C.T_SCROLL, tiles.y1()<<C.T_SCROLL);
		lWin.setWidth(tiles.width()<<C.T_SCROLL).setHeight(tiles.height()<<C.T_SCROLL);
		return fill(lWin);
	}
	
	public LIST<ENTITY> fillTiles(int tx, int ty, int w, int h){
		lWin.moveX1Y1(tx<<C.T_SCROLL, ty<<C.T_SCROLL);
		lWin.setWidth((w)<<C.T_SCROLL).setHeight(h<<C.T_SCROLL);
		return fill(lWin);
	}
	
	public boolean save(String p){
		throw new RuntimeException();
	}
	
	void remove(ENTITY e) {
		if (e.handlerId == -1)
			throw new RuntimeException();
		e.removeAction();
		grid.remove(e);
		ents[order.get(e.handlerId)] = null;
		freeIndexes.push(e.handlerId);
		e.handlerId = -1;
		
	}

	private int k = 0;
	private double lastDs = 0;
	@Override
	public void update(double ds, Profiler profiler){

		
		k++;
		k &= 1;
		double dds = ds;
		ds += lastDs;
		lastDs = dds;
		
		int s = k == 0 ? 0 : eLastIndex/2;
		int l = k == 0 ? eLastIndex : eLastIndex/2;
		
		for (int i = s; i <= l; i++){
			ENTITY e = ents[i];
			if (e == null)
				continue;
			e.update(ds);
			if (e.handlerId == -1) {
				continue;
			}
			grid.move(e);
		}

	}

	
	public void move(ENTITY e) {
		grid.move(e);
	}
	
	public ENTITY getAtPoint(COORDINATE coo) {
		return getAtPoint(coo.x(), coo.y());
	}
	
	public ENTITY getAtPoint(int x, int y) {
		temp.clear();
		grid.fill(x, y, temp);
		if (!temp.isEmpty())
			return temp.get(0);
		return null;
	}
	
	public Iterable<ENTITY> getAtPointL(int x, int y) {
		temp.clear();
		grid.fill(x, y, temp);
		return temp;
	}
	
	public ENTITY getArroundPoint(int x, int y) {
		temp.clear();
		
		Rec.TEMP.setDim(C.TILE_SIZE);
		Rec.TEMP.moveC(x, y);
		grid.fill(Rec.TEMP, temp);
		
		ENTITY res = null;
		while(!temp.isEmpty()) {
			ENTITY candidate = temp.removeLast();
			if (res == null || candidate.body().getDistance(Rec.TEMP) < res.body().getDistance(Rec.TEMP))
				res = candidate;
		}
		
		return res;
	}
	
	public LIST<ENTITY> getArroundPoint(int x, int y, int size) {
		temp.clear();
		
		Rec.TEMP.setDim(size);
		Rec.TEMP.moveC(x, y);
		grid.fill(Rec.TEMP, temp);
		
		
		return temp;
	}
	
	public Iterable<ENTITY> getAtTile(int tx, int ty){
		temp.clear();
		grid.fillTile(tx, ty, temp);
		return temp;
	}
	
	public ENTITY getAtTileSingle(int tx, int ty){
		return grid.getFirst(tx, ty);
	}
	
	public boolean tileIsClear(int tx, int ty) {
		temp.clear();
		grid.fill(tx, ty, temp);
		for (ENTITY e : temp){
			if (e.physics.getMass() != 0)
				return false;
		}
		return true;
	}
	
	public boolean hasAtTile(ENTITY asker, int tx, int ty) {
		ENTITY e = grid.getFirst(tx, ty);
		if (e != null && e == asker)
			e = e.next;
		return e != null;
	}
	
	public boolean hasAtTileHigher(ENTITY asker, int tx, int ty) {
		ENTITY e = grid.getFirst(tx, ty);
		while (e != null) {
			if (e != asker && e.id() > asker.id())
				return true;
			e = e.next;
		}
		return false;
	}
	
	public int amountAtTile(int tx, int ty) {
		int am = 0;
		ENTITY e = grid.getFirst(tx, ty);
		if (e != null && am < 10) {
			e = e.next;
			am++;
		}
		return am;
	}
	
	public boolean hasAtTile(int tx, int ty) {
		ENTITY e = grid.getFirst(tx, ty);
		return e != null;
	}
	
	public LIST<ENTITY> getInProximity(ENTITY e, int radius){
		temp.clear();
		grid.fill(e, radius, temp);
		return temp;
	}
	
	public ENTITY getByID(int id) {
		if (id < 0)
			return null;
		int oi = order.get(id);
		if (oi > ents.length)
			return null;
		ENTITY t = ents[oi];
		if (t == null)
			return null;
		if (t.isRemoved())
			return null;
		if (id != t.handlerId)
			return null;
		return t;
	}
	
	public int size() {
		return MAX-freeIndexes.size();
	}

	public final PLACABLE remove = new PlacableSimple("Remove Entity", "") {
		
		@Override
		public void place(int x, int y) {
			ENTITY e = ENTITIES().getAtPoint(x, y);
			if (e != null)
				e.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
		}
		
		@Override
		public CharSequence isPlacable(int x, int y) {
			return (ENTITIES().getAtPoint(x, y) != null) ? null : E;
		}
	};

	public boolean isMax() {
		return freeIndexes.isEmpty();
	}
	
}
