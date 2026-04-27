package game.battle.div;

import java.io.IOException;

import game.GAME;
import game.battle.Army;
import game.battle.ArmyDiv;
import game.battle.formation.DivFormation;
import game.battle.formation.DivPositionCopyable;
import game.battle.setting.BattleSettings;
import game.battle.setting.DivSettings;
import game.battle.thread.order.BattleOrder;
import game.battle.thread.order.BattleOrders;
import game.battle.thread.position.DivCentre;
import game.battle.thread.status.BattleStatus;
import game.battle.thread.status.DivStatus;
import game.battle.thread.trajectory.BattleTrajectories;
import game.battle.util.DIV_SIMPLE;
import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.HDivStat;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import util.gui.misc.GBox;
import view.main.VIEW;

public final class Div extends ArmyDiv implements BOOSTABLE_O, DIV_SIMPLE{

	private final short index;
	private final short armyIndex;
	private final Army army;
	final DivMen men;
	public final DivTargets targets;
	
	private final DivPositionCopyable current;
	
	public final DivInfo info;
	public final DivReporter reporter = new DivReporter();

	
	public Div(ArrayList<Div> all, ArrayList<Div> armyAll, Army army){
		this.index = (short) all.add(this);
		armyIndex = (short) armyAll.add(this);
		this.army = army;
		men = new DivMen();
		
		current = new DivPositionCopyable();
		targets = new DivTargets(this);
		info = new DivInfo(this);
	}
	
	@Override
	public Race race() {
		return info.race();
	}
	
	@Override
	public int men() {
		return men.men();
	}
	
	public int menPrevious() {
		return GAME.ARMIES().prevMen(this);
	}
	
	@Override
	protected void save(FilePutter file) {
		men.save(file);
		current.save(file);
		targets.saver().save(file);
		info.saver.save(file);
		reporter.unreachablem.save(file);
		file.s(reporter.unreachable);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		men.load(file);
		current.load(file);
		targets.saver().load(file);
		info.saver.load(file);
		reporter.unreachablem.load(file);
		reporter.unreachable = file.s();
		army.men.recount();
	}
	
	@Override
	protected void clear() {
		men.clear();
		current.clear();
		targets.saver().clear();
		info.saver.clear();
		reporter.unreachablem.clear();
		reporter.unreachable = 0;
		settings().clear();
	
	}
	
	public short index() {
		return index;
	}
	
	public short indexArmy() {
		return armyIndex;
	}
	
	public Army army() {
		return army;
	}
	
	public boolean player() {
		return army ==GAME.ARMIES().player();
	}
	
	public Army armyEnemy() {
		return GAME.ARMIES().armies().getC(army.index()+1);
	}
	
	public int menNrOf() {
		return men.men();
	}
	
	public int deployed() {
		return position().deployed();
	}
	
	public DIR dir() {
			
		return position().dir();
	}

	
	public Faction faction() {
		if (army() == GAME.ARMIES().player())
			return FACTIONS.player();
		return FACTIONS.otherFaction();
	}
	private static final Coo tmp = new Coo();
	
	
	public final class DivReporter extends HDivStat{
		
		private final Bitmap1D unreachablem = new Bitmap1D(Config.battle().MEN_PER_DIVISION, false);
		private short unreachable;
		
		private DivReporter() {
			
		}
		
		public boolean posHas(Humanoid a) {
			return position().deployed() >=  men.getSpot(a.divSpot());
		}
		
		public COORDINATE getTile(Humanoid a) {
			COORDINATE c = position().tile(men.getSpot(a.divSpot()));
			if (c == null)
				return a.tc();
			return c;
		}
		
		public COORDINATE getPixel(Humanoid a) {
			COORDINATE c = position().pixel(men.getSpot(a.divSpot()));
			if (c == null) {
				tmp.set(a.body().cX(), a.body().cY());
				return tmp;
			}
			return c;
		}
		
		public RECTANGLE body() {
			return position().body();
		}
		
		public COORDINATE getDestTile(Humanoid a) {
			COORDINATE c = position().centreTile();
			if (c == null)
				return a.tc();
			return c;
		}
		
		public void reportPosition(short spot, int x, int y) {
			current.set(men.getSpot(spot), x, y);
		}
		
		@Override
		public short signUpAndGetPosition(int x, int y, Race r) {
			if (menNrOf() == 0) {
				info.raceSet(r);
				GAME.ARMIES().factors.init(Div.this);
			}
			army.men.recount();
			short sp = men.getNewSpot();
			reportPosition(sp, x, y);
			current.init(men.men());
			
			return sp;
		}
		
		@Override
		public void returnPosition(short pos) {
			army.men.recount();
			men.returnSpot(pos);
			if (men.men() == 0)
				settings().musteringSet(false);
			reportReachable(pos, true);
			current.init(men.men());
		}
		
		private void reportReachable(int spot, boolean reachable) {
			if (unreachablem.get(spot) == true)
				unreachable --;
			unreachablem.set(spot, !reachable);
			if (!reachable)
				unreachable ++;
		}
		
		public void reportReachable(Humanoid a, boolean reachable) {
			
			int spot = positionSpot(a);
			reportReachable(spot, reachable);
		}
		
		public boolean reachable(int i) {
			return !unreachablem.get(i);
		}
		
		public int unreachable() {
			return unreachable;
		}
		
		public int positionSpot(Humanoid h) {
			int i = STATS.BATTLE().position(h.indu());
			return men.getSpot(i);
		}
		
		public int positionSpot(int ui) {
			return men.spotTranslate(ui);
		}
		
	}
	
	public void hoverInfo(GBox text) {
		VIEW.UI().div.battle.hover(text, this);
	}
	
	public DivPositionCopyable current() {
		return current;
	}

	public void debug() {
		String s = System.lineSeparator();
		String res = "";
		res += "Div: " + index + " " + armyIndex + s;
		res += "Men: " + menNrOf() + " Deployed:" + deployed() + s;
		res += "Current: " + current.deployed() + " " + " " + s;
		
		GAME.Notify(res);
	}

	public boolean active() {
		return menNrOf() > 0 && settings().mustering();
	}
	
	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}
	
	public DivCentre centre() {
		return GAME.BATTLE_THREADS().centres.centre(index);
	}
	
	public DivStatus status() {
		return BattleStatus.status(this);
	}
	
	public Trajectory traj(Humanoid h) {
		return BattleTrajectories.request(h, this);
	}
	
	public DivFormation position() {
		return BattleOrders.next(this);
	}
	
	public BattleOrder order() {
		return BattleOrders.get(this);
	}
	
	public double morale() {
		return GAME.ARMIES().factors.morale(this);
	}

	public DivSettings settings() {
		return BattleSettings.get(this);
	}
	
}
