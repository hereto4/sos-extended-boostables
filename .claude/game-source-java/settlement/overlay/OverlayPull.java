package settlement.overlay;

import settlement.main.SETT;
import settlement.room.infra.logistics.MoveJob;
import settlement.room.infra.logistics.MoveJob.ROOM_MOVE_SOURCE;
import settlement.room.infra.logistics.MoveOrderPull.MoveOrderPullInstance;
import settlement.room.main.Room;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import util.colors.GCOLOR;
import util.rendering.RenderData.RenderIterator;

public final class OverlayPull extends Addable{
		
	private MoveOrderPullInstance special;

	OverlayPull(){
		super(null, null, null, null, true, false);
		exclusive = true;
	}

	public void add(MoveOrderPullInstance ins) {
		super.add();
		this.special = ins;
	}
	
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		COLOR c = COLOR.WHITE10;
		
		Room room = SETT.ROOMS().map.get(it.tx(), it.ty());
		if (room != null && room != special && room instanceof MoveJob.ROOM_MOVE_SOURCE) {
			MoveJob.ROOM_MOVE_SOURCE s = (ROOM_MOVE_SOURCE) room;
			if (s.moveCapacity().has(special.moveOrderPullAccepted())) {
				c = GCOLOR.MAP().OVERLAY_GOOD;
			}else {
				c = GCOLOR.MAP().OVERLAY_BAD;
			}
			
			
		}

		renderUnder(c, r, it);
		
	}

	
	@Override
	public void finishBelow() {
		special = null;
	}
	
}
