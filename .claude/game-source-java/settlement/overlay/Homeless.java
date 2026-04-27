package settlement.overlay;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.house.HomeInstance;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import util.GUTIL;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

final class Homeless extends Addable{

	
	
	Homeless(String key, CharSequence name, CharSequence desc) {
		super(UI.icons().m.building, key, name, desc, true, false);
		exclusive = true;
	}

	@Override
	public void initBelow(RenderData data) {
		
		GUTIL.flooder().init(this);
		
		iter.iterate();
	};
	
	private final EntityIterator.Humans iter = new EntityIterator.Humans() {
		
		@Override
		protected boolean processAndShouldBreakH(Humanoid a, int ie) {
			if (a.indu().clas().player) {
				if (STATS.HOME().GETTER.hasSearched.indu().get(a.indu()) == 1) {
					RoomInstance r = STATS.WORK().EMPLOYED.get(a.indu());
					if (r != null) {
						if (!GUTIL.flooder().hasBeenPushed(r.mX(), r.mY())) {
							GUTIL.flooder().close(r.mX(), r.mY(), ((RoomInstance)r).employees().employed());
							GUTIL.flooder().setValue2(r.mX(), r.mY(), 1);
						}else {
							GUTIL.flooder().setValue2(r.mX(), r.mY(), GUTIL.flooder().getValue2(r.mX(), r.mY()));
						}
					}else {
						GUTIL.flooder().close(a.tc().x(), a.tc().y(), 0);
					}
				}
			}
			return false;
		}
	};
	
	@Override
	public void finishBelow() {
		GUTIL.flooder().done();
	};
	
	@Override
	public boolean render(Renderer r, RenderIterator it) {
		return false;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		Room room = SETT.ROOMS().map.get(it.tx(), it.ty());
		
		if (room != null) {
			int mx = room.mX(it.tx(), it.ty());
			int my = room.mY(it.tx(), it.ty());
			if (GUTIL.flooder().hasBeenPushed(mx, my) && GUTIL.flooder().getValue(mx, my) > 1) {
				int tot = (int) GUTIL.flooder().getValue(mx, my);
				int home = (int) GUTIL.flooder().getValue2(mx, my);
				if (home == tot)
					GCOLOR.MAP().BAD.bind();
				else
					GCOLOR.MAP().SOSO.bind();
				
			}else if (room.blueprint().employment() != null){
				GCOLOR.MAP().OVERLAY_GOOD.bind();
			}else {
				HomeInstance h = SETT.ROOMS().HOME.getter.get(it.tx(), it.ty());
				if (h != null) {
					ColorImp.TMP.interpolate(COLOR.WHITE100, GCOLOR.MAP().BETTER, (double)h.occupants()/h.occupantsMax());
					ColorImp.TMP.bind();
				}else {
					COLOR.WHITE10.bind();
				}
				
			}
			
			SPRITES.cons().BIG.filled.render(r, 0, it.x(), it.y());
			
		}else if (SETT.PATH().getAvailability(it.tx(), it.ty()).player > 0) {
			COLOR.WHITE10.bind();
			SPRITES.cons().BIG.filled.render(r, 0, it.x(), it.y());
		}

	}
	
}
