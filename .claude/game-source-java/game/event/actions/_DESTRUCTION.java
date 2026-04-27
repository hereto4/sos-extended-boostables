package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;

final class _DESTRUCTION extends EventActionConstructor{


	
	
	_DESTRUCTION() {
		super("DESTRUCTION");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}

	
	public final class Imp extends EventAction  {
		
		public final double death;
		public final double destruction;
		public final double degrade;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			death = data.dTry("DEATH", 0, 1000, 0);
			destruction = data.dTry("DESTRUCTION", 0, 1000, 0);
			degrade= data.dTry("DEGRADE", 0, 1000, 0);
			data.checkUnused();
		}
		

		@Override
		public void setContext(Event event, EContext data) {
			dacc = 0;
			kacc = 0;
			accd = 0;
		}
		

		double dacc;
		double kacc;
		double accd;
		
		@Override
		public void update(Event event, EContext e, double ds, double second) {

			
			dacc += ds*destruction;
			while (dacc > 0) {
				dacc-= 1;
				int tx = RND.rInt(SETT.TWIDTH);
				int ty = RND.rInt(SETT.THEIGHT);
				if (SETT.TERRAIN().get(tx, ty) instanceof TBuilding.BuildingComponent)
					GAME.ARMIES().map.breakIt(tx, ty);
				Room r = SETT.ROOMS().map.get(tx, ty);
				if (r != null && r.destroyTileCan(tx, ty))
					r.destroyTile(tx, ty);
			}
			kacc += ds*death;
			while (kacc > 0) {
				kacc-= 1;
				int tx = RND.rInt(SETT.TWIDTH);
				int ty = RND.rInt(SETT.THEIGHT);
				
				for (ENTITY ent : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (ent instanceof Humanoid)
						_SUBJECTS_KILL.slap(e, ent, 1.0, CAUSE_LEAVES.SLAYED());
				}
				
			}
			accd += ds*degrade;
			while (accd > 0) {
				accd-= 1;
				int tx = RND.rInt(SETT.TWIDTH);
				int ty = RND.rInt(SETT.THEIGHT);
				SETT.MAINTENANCE().vandalise(tx, ty);
				
			}
		}
		
	}




	
}
