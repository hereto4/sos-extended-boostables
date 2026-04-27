package settlement.job;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.TWIDTH;

import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.job.StateManager.State;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class Placer extends PlacableMulti{

	private final Job j;
	private final RESOURCE res;
	private final int resAmount;
	final CharSequence desc;
	
	public Placer(Job j, CharSequence desc) {
		this(j, null, 0, desc);
	}
	
	public Placer(Job j, RESOURCE res, int resAmount, CharSequence desc) {
		super(j.name,desc, j.icon);
		this.j = j;
		this.res = res;
		this.resAmount = resAmount;
		this.desc = desc;
	}
	
	@Override
	public CharSequence name() {
		return j.name;
	}
	
	@Override
	public void updateRegardless(GameWindow window, AREA selected) {
		j.doSomethingExtraRender();
		super.updateRegardless(window, selected);
	}
	
	@Override
	public final CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {

		return j.problem(tx, ty, Job.overwrite);
	}
	
	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		place(tx, ty, j);
	}
	
	static void place(int tx, int ty, Job j) {
		
		if (!IN_BOUNDS(tx, ty)) {
			return;
		}
		int i = tx+ty*TWIDTH;
		Job old = JOBS().getter.get(i);
		
		if (old == j) {
			if (!JOBS().planMode.is() && JOBS().state.is(tx, ty, State.DORMANT)) {
				JOBS().state.set(State.RESERVABLE, JOBS().getter.get(i));
			}
			return;
		}
		
		if (old != null) {
			old.cancel(tx, ty);
			PlacerDelete.place(tx, ty);
		}
		
		j.init(tx, ty);
		JOBS().set(j.index, tx, ty);
		
		if (!JOBS().planMode.is()) {
			JOBS().state.set(State.RESERVABLE, JOBS().getter.get(i));
		}else {
			JOBS().state.set(State.DORMANT, JOBS().getter.get(i));
		}
	}
	

	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA a,
			PLACER_TYPE t, boolean isPlacable, boolean areaIsPlacable) {
		SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
	}
	
	@Override
	public void placeInfo(GBox b, int okTiles, AREA a) {
		if (a.body().width() < 2 && a.body().height() < 2)
			return; 
		b.setArea(a.body());
		if (okTiles > 0 && res != null)
			b.setResource(res, resAmount*okTiles);
	}
	
	@Override
	public void hoverDesc(GBox box) {
		super.hoverDesc(box);
		if (res != null) {
			box.NL();
			box.setResource(res, resAmount);
		}
		box.NL();
		j.extraHovInfo(box);
	}

	
	@Override
	public PLACABLE getUndo() {
		return JOBS().tool_clear;
	}
	
	private static CharSequence ¤¤overwrite = "¤Overwrite";
	private static CharSequence ¤¤overdesc = "¤Overwrites other jobs or structures when placed.";
	
	static {
		D.ts(Placer.class);
	}
	
	protected final LIST<CLICKABLE> bOverwrite = new ArrayList<CLICKABLE>(new GButt.Panel(SPRITES.icons().m.overwrite) {
		
		{
			hoverInfoSet(¤¤overdesc);
			hoverTitleSet(¤¤overwrite);
		}
		
		@Override
		protected void renAction() {
			selectedSet(Job.overwrite);
		};
		
		@Override
		protected void clickA() {
			Job.overwrite = !Job.overwrite;
		};
		
	});
	
	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		return bOverwrite;
	}

	
	
}
