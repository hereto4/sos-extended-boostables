package world.battle;

import java.io.IOException;

import game.debug.Profiler;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import view.main.VIEW;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.battle.Side.Conflict;
import world.entity.army.WArmy;
import world.map.regions.Region;

public final class WBattles extends WorldResource{

	public static final double retreatPenalty = 0.4;
	
	private final PRegAttack regAttack;
	private final PFieldBattle poller;
	private final PSiege siege;
	
	public WBattles() {
		super("battles", "BATTLES");
		
		Util u = new Util();
		Conflict c = new Conflict();
		
		Resolver pro = new Resolver();
		regAttack = new PRegAttack(c, pro, u);
		poller = new PFieldBattle(c, pro, u);
		siege = new PSiege(u, c, pro);
		new Tests();
	}
	

	
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			regAttack.save(file);
			poller.save(file);
			siege.save(file);;
		}

		@Override
		public void load(FileGetter file) throws IOException {
			regAttack.load(file);
			poller.load(file);
			siege.load(file);
		}
		
		@Override
		public void clear() {
			regAttack.clear();
			poller.clear();
			siege.clear();
		}
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}

	@Override
	public void update(double ds, Profiler prof) {
		prof.logStart(this);
		siege.update(ds);
		prof.logEnd(this);
	}
	
	public void poll() {
		
		int death = 0;
		while(canPoll()) {
			if (!regAttack.poll())
				break;
			if (death++ > 1000)
				throw new RuntimeException();
		}
		
		death = 0;
		while(canPoll()) {
			if (!poller.poll())
				break;
			if (death++ > 1000)
				throw new RuntimeException();
		}
		
		death = 0;
		while(canPoll()) {
			if (!siege.poll())
				break;
			if (death++ > 1000)
				throw new RuntimeException();
		}
		
	}
	

	private boolean canPoll() {
		return !VIEW.b().isActive() && !VIEW.world().UI.battle.isBusty();
	}

	public double besigedTime(Region reg) {
		return siege.besigedTime(reg);
	}
	
	public boolean besiged(Region reg) {
		return siege.besiged(reg);
	}
	
	public void besige(WArmy a, Region reg) {
		siege.besige(a, reg);
	}

	public void regAttack(Region reg, WArmy a) {
		regAttack.regAttack(reg, a);
		report(a);
	}

	public void report(WArmy a) {
		regAttack.register(a);
		poller.register(a);
		siege.register(a);
		
	}

}
