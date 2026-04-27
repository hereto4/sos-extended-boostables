package view.sett.ui.room;

import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.WGROUP;
import init.type.WGROUP.HTypeBits;
import init.type.WGROUP.HTypeBitsImp;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.GUTIL;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.rendering.ShadowBatch;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class UIRoomRaceAssign extends PlacableMulti{

	private static CharSequence ¤¤name = "Assign work groups";
	private static CharSequence ¤¤desc = "Work groups let you favour the configuration of different species in specific rooms. This assignment will only work if priorities are set up in such a way that the room type currently has workers of the selected group.";
	private static CharSequence ¤¤prob = "Must be placed on a workplaces.";
	private static CharSequence ¤¤prob2 = "Some or all of the selected groups are not employed in the current room type, and the setting will have no effect.";
	private static CharSequence ¤¤everyone = "Set priority for everyone";
	private static CharSequence ¤¤none = "Set priority for none";
	private static CharSequence ¤¤permission = "Set priority for:";
	private static CharSequence ¤¤permissionAll = "Set priority for All:";
	
	
	static {
		D.ts(UIRoomRaceAssign.class);
	}
	
	private final HTypeBitsImp data = new HTypeBitsImp(false);

	private final LIST<CLICKABLE> butts;
	
	public UIRoomRaceAssign() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.citizen);
		
		GuiSection sec = new GuiSection();
		sec.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			
			@Override
			protected void clickA() {
				data.setEveryone();
			}
			
		}.hoverInfoSet(¤¤everyone));
		sec.addRightC(0, new GButt.ButtPanel(HCLASSES.CITIZEN().icon()) {
			
			@Override
			protected void clickA() {
				data.clear();
				for (Race r : RACES.all())
					data.set(WGROUP.get(HTYPES.SUBJECT(), r));
			}
			
		}.hoverInfoSet(¤¤permissionAll + " " + HCLASSES.CITIZEN().names));
		sec.addRightC(0, new GButt.ButtPanel(HCLASSES.SLAVE().icon()) {
			
			@Override
			protected void clickA() {
				data.clear();
				for (Race r : RACES.all())
					data.set(WGROUP.get(HTYPES.SLAVE(), r));
			}
			
		}.hoverInfoSet(¤¤permissionAll + " " + HCLASSES.SLAVE().names));
		sec.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			
			@Override
			protected void clickA() {
				data.clear();
			}
			
		}.hoverInfoSet(¤¤none));
		
		GuiSection s = new GuiSection();
		for (WGROUP t : WGROUP.all()) {
			CLICKABLE bb = new GButt.ButtPanel(t.icon) {
				
				@Override
				protected void clickA() {
					
					if (selectedIs())
						data.clear(t);
					else
						data.set(t);
				}
				
				@Override
				protected void renAction() {
					selectedSet(data.is(t));
				}
			}.hoverInfoSet(¤¤permission + " " + t.name);
			
			if (s.getLastX2() > 500) {
				bb.body().moveX1(0).moveY1(s.body().y2());
				s.add(bb);
			}else
				s.addRightC(0, bb);
			
		}
		
		sec.addRelBody(8, DIR.S, s);
		
		butts = new ArrayList<CLICKABLE>(sec);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		RoomInstance ins = SETT.ROOMS().map.instance.get(tx, ty);
		if (ins != null && ins.blueprintI().employment() != null)
			return null;
		return ¤¤prob;
	}

	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		RoomInstance ins = SETT.ROOMS().map.instance.get(tx, ty);
		if (ins != null && ins.blueprintI().employment() != null) {
			ins.employees().prefferedSet(data);
		}
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		RoomInstance ins = SETT.ROOMS().map.instance.get(fromX, fromY);
		return ins != null && ins.is(toX, toY);
	}
	
	@Override
	public void placeInfo(GBox b, int oktiles, AREA a) {
		super.placeInfo(b, oktiles, a);
		b.NL();
		
		for (COORDINATE c : a.body()) {
			if (a.is(c)) {
				
				
				RoomInstance ins = SETT.ROOMS().map.instance.get(c.x(), c.y());
				if (ins == null || !c.isSameAs(ins.mX(), ins.mY()) || ins.blueprintI().employmentExtra() == null)
					continue;
				
				
				for (WGROUP g : WGROUP.all()) {
					if (data.is(g) && ins.blueprintI().employment().employed(g) <= 0) {
						GText t = b.text();
						t.warnify().add(¤¤prob2);
						b.add(t);
						return;
					}
				}
				
			}
			
		}
		
	}

	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		
		ren.add();		
		return butts;
	}
	
	
	private final ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
		
		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
			
			RenderIterator it = data.onScreenTiles();
			GUTIL.filler().init(this);
			while(it.has()) {
				if (!GUTIL.filler().isFilled(it.tx(), it.ty())) {
					RoomInstance ins = SETT.ROOMS().map.instance.get(it.tx(), it.ty());
					
					if (ins != null && ins.blueprintI().employment() != null) {
						
						int x = it.tx();
						int y = it.ty();
						GUTIL.filler().fill(x, y);
						int cx = ins.body().cX();
						int cy = ins.body().cY();
						int shortest = Integer.MAX_VALUE;
						
						while(GUTIL.filler().hasMore()) {
							COORDINATE c = GUTIL.filler().poll();
							int dist = (c.x()-cx)&Integer.MAX_VALUE;
							dist += (c.y()-cy)&Integer.MAX_VALUE;
							if (dist < shortest) {
								shortest = dist;
								x = c.x();
								y = c.y();
							}
							
							for (int di = 0; di < DIR.ORTHO.size(); di++) {
								DIR d = DIR.ORTHO.get(di);
								if (ins.is(c, d))
									GUTIL.filler().fill(c, d);
							}
						}
						
						cx*= C.TILE_SIZE;
						cy*= C.TILE_SIZE;
						cx = data.transformGX(cx);
						cy = data.transformGY(cy);
						render(r, ins, cx, cy);
						
						
					}
				}
				
				
				
				it.next();
			}
			GUTIL.filler().done();
			remove();
		}
		
		private final ArrayList<WGROUP> rens = new ArrayList<>(WGROUP.all().size());
		
		private void render(Renderer r, RoomInstance h, int cx, int cy) {
			

			
			HTypeBits t = h.employees().preffered();
			int am = 0;
			WGROUP single = null;
			for (WGROUP hh : WGROUP.all()) {
				if (t.is(hh)) {
					single = hh;
					am++;
				}
			}
			
			if (am == WGROUP.all().size()) {
				renderSingle(r, cx, cy, UI.icons().m.questionmark);
			}else if (am == 0) {
				renderSingle(r, cx, cy, UI.icons().m.cancel);
			}else if (am == 1) {
				renderSingle(r, cx, cy, h, single);
			}else if (am < WGROUP.all().size()/2) {
				rens.clearSloppy();
				for (WGROUP hh : WGROUP.all()) {
					if (t.is(hh)) {
						rens.add(hh);
					}
				}
				renderMany(r, cx, cy, h, false);
			}else {
				rens.clearSloppy();
				for (WGROUP hh : WGROUP.all()) {
					if (!t.is(hh)) {
						rens.add(hh);
					}
				}
				if (rens.size() > 0)
					renderMany(r, cx, cy, h, true);
			}
		}
		
		private void renderSingle(Renderer r, int cx, int cy, SPRITE icon) {
			int w = icon.width()*C.SCALE;
			int h = icon.height()*C.SCALE;
			int x1 = cx-w/2;
			int y1 = cy-h/2;
			icon.render(r, x1, x1+w, y1, y1+h);
		}
		
		private void renderSingle(Renderer r, int cx, int cy, RoomInstance house, WGROUP g) {
			SPRITE icon = g.icon;
			int w = icon.width()*C.SCALE;
			int h = icon.height()*C.SCALE;
			int x1 = cx-w/2;
			int y1 = cy-h/2;
			icon.render(r, x1, x1+w, y1, y1+h);
			if (house.blueprintI().employment().employed(g) <= 0) {
				GCOLOR.UI().BAD.hovered.bind();
				int w2 = 16*C.SCALE;
				UI.icons().s.alert.render(r, x1, x1+w2, y1-w2/2, y1+w2/2);
				COLOR.unbind();
			}
		}
		
		private void renderMany(Renderer r, int cx, int cy, RoomInstance house, boolean anti) {
			
			int width = house.body().width()*C.TILE_SIZE;
			int w = 24*C.SCALE/2;
			int h = rens.get(0).icon.height()*C.SCALE/2;
			
			int dx = (width/rens.size());
			dx = CLAMP.i(dx, 1, w);
			
			int x1 = cx - dx*rens.size()/2;
			int y1 = cy - h;
			
			for (WGROUP t : rens) {
				t.icon.render(r, x1, x1+w, y1, y1+h);
				
				if (anti)
					UI.icons().m.anti.render(r, x1, x1+w, y1, y1+h);
				if (house.blueprintI().employment().employed(t) <= 0) {
					GCOLOR.UI().BAD.hovered.bind();
					int w2 = 16*C.SCALE/2;
					UI.icons().s.alert.render(r, x1, x1+w2, y1-w2/2, y1+w2/2);
					COLOR.unbind();
				}
				
				x1 += dx;
			}
		}
	};
	
}
