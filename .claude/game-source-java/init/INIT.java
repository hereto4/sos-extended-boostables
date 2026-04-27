package init;

import java.io.IOException;

import game.boosting.BOOSTING;
import game.save.Savable;
import init.constant.Config;
import init.race.RACES;
import init.religion.RELIGIONS;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.structure.STRUCTURES;
import init.tech.TECHS;
import init.type.TYPEINIT;
import init.value.GVALUES;
import snake2d.CORE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public class INIT {

	private static ArrayListGrower<Savable> savers = new ArrayListGrower<Savable>();
	
	private final ArrayListGrower<InitResource> resses = new ArrayListGrower<INIT.InitResource>();
	
	public INIT() throws IOException{
		savers.clear();
		
		new Config(this);
		CORE.checkIn();
		new UI(this);
		CORE.checkIn();
		new GVALUES(this);
		CORE.checkIn();
		new BOOSTING(this);
		CORE.checkIn();
		new RACES();
		CORE.checkIn();
		new TYPEINIT(this);
		CORE.checkIn();
		new RESOURCES(this);
		CORE.checkIn();
		new RELIGIONS(this);
		CORE.checkIn();
		new TECHS();
		CORE.checkIn();
		new STRUCTURES(this);
		
	}
	
	public static void addSaver(Savable s) {
		savers.add(s);
	}
	
	public LIST<Savable> finish() throws IOException{

		
		
		for (InitResource ii : resses) {
			ii.finishSetup();
		}
		
		return savers;
	}
	
	public static class InitResource {
		
		protected InitResource(INIT init) {
			init.resses.add(this);
		}
		
		protected void finishSetup() throws IOException{
			
		}
		
	}
	
	public static interface AfterInit {
		
		public void exe() throws IOException;
		
	}
	
}
