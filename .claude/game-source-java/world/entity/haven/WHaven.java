package world.entity.haven;

import java.io.IOException;

import game.faction.Faction;
import init.constant.C;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.entity.WEntity;
import world.map.regions.Region;

public class WHaven extends WEntity{
	
	private int ti = 0;
	private double size;
	private int ran;
	public final Str name = new Str(16);
	
	public WHaven() {
		super(24*C.SCALE, 24*C.SCALE);		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(ti);
		file.d(size);
		file.i(ran);
		name.save(file);
	}
	
	@Override
	protected WEntity load(FileGetter file) throws IOException {
		ti = file.i();
		ti = CLAMP.i(ti, 0, WORLD.camps().types.size()-1);
		size = file.d();
		ran = file.i();
		name.load(file);
		return this;
	}
	
	@Override
	protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
		WHavenType t = type();
		
		int off = (t.sheet.size()-24*C.SCALE)/2;
		
		int ran = (int) ((this.ran&0b011)*8);
		ran += (ran >> 2) & 1;
		int size = (int) (Math.ceil(this.size*3))*2;
		t.cMask.bind();
		t.sheet.render(r, ran+size, x-off, y-off);
		s.setHeight(4).setDistance2Ground(0);
		t.sheet.render(s, ran+size, x-off, y-off);
		
		COLOR.unbind();
	}

	public void add(int tx, int ty, WHavenType type, double size, CharSequence name) {
		this.ti = type.index();
		this.ran = RND.rInt();
		this.name.clear().add(name);
		this.size = size;
		body().moveC(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH);
		
		add();
	}
	
	public WHavenType type() {
		return constructor().types.get(ti);
	}
	
	public int pop() {
		return (int) Math.ceil(type().popFrom + (type().popTo-type().popFrom)*size);
	}
	
	public double replenish() {
		return  type().replenishMin + (type().replenishMax-type().replenishMin)*size;
	}
	
	@Override
	protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected WHavens constructor() {
		return WORLD.ENTITIES().havens;
	}
	
	public void delete() {
		super.remove();
	}
	
	@Override
	protected void addAction() {
		Region r = WORLD.REGIONS().map.get(ctx(), cty());
		if (r != null)
			WORLD.ENTITIES().havens.setDirty(r.faction());
		super.addAction();
	}
	
	@Override
	protected void removeAction() {
		Region r = WORLD.REGIONS().map.get(ctx(), cty());
		if (r != null)
			WORLD.ENTITIES().havens.setDirty(r.faction());
		if (!constructor().free.isFull())
			constructor().free.push(this);
	}

	@Override
	public Faction faction() {
		Region r = WORLD.REGIONS().map.get(ctx(), cty()); 
		if (r != null)
			return r.faction();
		return null;
	}
	

}
