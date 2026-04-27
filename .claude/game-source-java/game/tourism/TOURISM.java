package game.tourism;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.time.TIME;
import init.constant.Config;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import init.type.NEED;
import init.type.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.service.StatService;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import view.sett.IDebugPanelSett;
import view.ui.wiki.WIKI;

public final class TOURISM extends GameResource{

	final static double CREDITS = Config.sett().TOURIST_CRETIDS;
	final static int MIN_EMPLOYEES = 100;
	final static double MAX_EMPLOYEES = 1000;
	public final static int AMOUNT = Config.sett().TOURIST_PER_YEAR_MAX;
	
	
	static TOURISM self;
	
	private final Review[] reviews = new Review[32];
	private final ArrayList<Review> list = new ArrayList<>(32);
	private final LIST<Race> tourists;
	final HistoryInt history = new HistoryInt(24, TIME.seasons(), false);
	private final Updater updater;
	private final Bitmap1D permit = new Bitmap1D(RACES.all().size(), false);
	private double score = 0;
	private final ACTION wiki;
	
	private final double[] races = new double[RACES.all().size()];
	
	
	public TOURISM(){
		super("TOURISTS", false);
		self = this;
		for (int i = 0; i < reviews.length; i++)
			reviews[i] = new Review();
		
		LinkedList<Race> li = new LinkedList<>();
		for (Race r : RACES.all()) {
			if (r.tourism().occurence > 0) {
				li.add(r);
			}
		}
		tourists = new ArrayList<Race>(li);
		updater = new Updater();
		permit.setAll(true);
		wiki = WIKI.add(new Json(PATHS.RACE().text.getFolder("tourist").get("_WIKI")));
		
		IDebugPanelSett.add("TOURIST_REVIEW", new ACTION() {
			
			@Override
			public void exe() {
				Induvidual i = new Induvidual(HTYPES.TOURIST(), RACES.playable().rnd());
				service(i).cheatSetTotal(i, RND.rFloat());
				RoomInstance ii = SETT.ROOMS().INN.instancesSize() > 0 ? SETT.ROOMS().INN.getInstance(RND.rInt(SETT.ROOMS().INN.instancesSize())) : null;
				touristFinish(i, ii != null ? new Coo(ii.mX(), ii.mY()) : new Coo(-1, -1));
			}
		});
		
	}



	
	@Override
	protected void save(FilePutter file) {
		for (Review r : reviews)
			r.save(file);
		history.save(file);
		updater.save(file);
		permit.save(file);
		file.d(score);
		RACES.map().saver().save(races, file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Review r : reviews)
			r.load(file);
		history.load(file);
		updater.load(file);
		permit.load(file);
		score = file.d();
		RACES.map().loader().load(races, file, 0);
	}

	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(TOURISM.class);
		updater.update(ds);
		prof.logEnd(TOURISM.class);
	}
	
	public static RoomBlueprintIns<?> attraction(Induvidual indu) {
		return Updater.attraction(indu);
	}
	
	public static int perYear() {
		return (int) Math.ceil(self.updater.chance()*AMOUNT);
	}
	
	public static StatService service(Induvidual i) {
		
		NEED n = need(i);
		if (n == null) {
			GAME.Notify("here");
		}
		return AI.modules().needs.service(i, n, STATS.RAN().getD(i, 15));
	}
	
	public static AiPlanActivation servicePlan(Humanoid a, AIManager d) {
		
		NEED n = need(a.indu());
		return AI.modules().needs.plan(a, d, n, STATS.RAN().getD(a.indu(), 15));
	}
	
	private static NEED need(Induvidual a) {
		
		long max = 0;
		for (NEED n : NEEDS.ALLSIMPLE()) {
			if (n != NEEDS.TYPES().SHRINE && n != NEEDS.TYPES().TEMPLE && n!= NEEDS.TYPES().SKINNYDIP)
				max += (long)(1000*a.race().bvalue(n.rate));
		}
		
		max *= STATS.RAN().getD(a, 21);
		for (NEED n : NEEDS.ALLSIMPLE()) {
			if (n != NEEDS.TYPES().SHRINE && n != NEEDS.TYPES().TEMPLE && n!= NEEDS.TYPES().SKINNYDIP) {
				max -= (long)(1000*a.race().bvalue(n.rate));
				if (max <= 0) {
					return n;
				}
			}
		}
		return NEEDS.ALLSIMPLE().get(0);
	}
	
	public static int credits(Race race) {
		return (int) (race.tourism().credits*CREDITS);
	}
	
	public static LIST<Race> races(){
		return self.tourists;
	}
	
	public static HISTORY_INT history() {
		return self.history;
	}

	public static boolean permit(Race race) {
		return self.permit.get(race.index());
	}
	
	public static void permit(Race race, boolean perm) {
		self.permit.set(race.index(), perm);
	}
	
	public static double score() {
		return self.score;
	}
	
	public static void touristFinish(Induvidual tourist, COORDINATE inn) {
		if (SETT.ENTRY().beseiged()) {
			return;
		}
		
		Review v = self.reviews[self.reviews.length-1];
		for (int i = self.reviews.length-1; i > 0; i--) {
			self.reviews[i] = self.reviews[i-1];
		}
		self.reviews[0] = v;
		v.make(tourist, inn);
		
		self.score = (15*self.score + v.score)/16.0;
		self.score = CLAMP.d(self.score, 0, 1);
		
		if (SETT.ROOMS().INN.is(inn))
			SETT.ROOMS().INN.setReview(inn.x(), inn.y(), v);
		FACTIONS.player().credits().inc(v.credits, CTYPE.TOURISM);
		
		self.races[tourist.race().index()] ++;
		for (int i = 0; i < self.races.length; i++)
			self.races[i]/= 2;
		
		
	}
	
	public static LIST<Review> reviews(){
		self.list.clear();
		for (int i = 0; i < self.reviews.length; i++) {
			if (self.reviews[i].has())
				self.list.add(self.reviews[i]);
			else
				break;
		}
		return self.list;
	}
	
	public static ACTION wiki() {
		return self.wiki;
	}
	
	public static double race(Race race) {
		return TOURISM.self.races[race.index()];
	}
	
}

