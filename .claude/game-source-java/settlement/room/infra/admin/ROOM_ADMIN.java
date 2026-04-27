package settlement.room.infra.admin;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import settlement.main.SETT;
import settlement.path.finders.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.interrupter.IDebugPanel;
import view.sett.ui.room.UIRoomModule;
import view.ui.message.MessageSection;

public final class ROOM_ADMIN extends RoomBlueprintIns<AdminInstance> implements INDUSTRY_HASER {

	public final static String type = "ADMIN";
	public final AdminData data;
	public final BoostSpecs boosts;
	final Job job;
	
	final Industry industry;
	final Constructor constructor;
	final LIST<Industry> indus;

	public final double POP_MIN;
	public final double POP_BREAK;
	public final double INCREASE_POW;
	
	private boolean hasSentMess = false;
	private final CharSequence sBoost;
	private final CharSequence sMess;
	
	final double BOOST_FROM;
	final double BOOST_TO;
	
	boolean automate;
	boolean missingPaper = false;
	boolean missingPaperNext = false;
	private int upI = 0;
	
	public ROOM_ADMIN(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		job = new Job(this);
		
		constructor = new Constructor(this, init);
		pushBo(init.data(), type, true);
		data = new AdminData(employmentExtra(), init.data(), bonus());
		sBoost = init.text().text("BOOST_NAME");
		sMess = init.text().text("UNLOCK_MESS");
		
		POP_MIN = init.data().i("POP_MIN");
		
		INCREASE_POW = init.data().d("INCREASE_POW", 0, 10);
		
		
		cache = 0;

		industry = new Industry(this, init.data(), bonus());
		industry.roomBoosts.add(constructor.efficiency);
		boosts = new BoostSpecs(sBoost, iconBig(), true);
		
		Json jboost = init.data();
		BOOST_FROM = init.data().d("BOOST_FROM", 0, 1);
		BOOST_TO = init.data().d("BOOST_TO", BOOST_FROM, 1000);

		POP_BREAK = POP_MIN - Math.pow(POP_MIN - POP_MIN/BOOST_TO, 1.0/INCREASE_POW);
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
	
				for (Boostable bo : BOOSTING.MAP().readMany("BOOSTING", jboost)) {
					Booster b = new Bo(BOOST_FROM, BOOST_TO, true);
					boosts.push(b, bo);
				}
				
				
			}
		});
		
		IDebugPanel.add("admin + 100000", new ACTION() {
			
			@Override
			public void exe() {
				
				data.inc(100000);
			}
		});
		
		indus = new ArrayList<>(industry);
		employment().countInputSet();
	}
	
	@Override
	protected void saveP(FilePutter f){
		data.save(f);
		industry.save(f);
		f.bool(hasSentMess);
		f.bool(automate);
		f.bool(missingPaper);
		f.bool(missingPaperNext);
		f.i(upI);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		data.load(f);
		industry.load(f);
		hasSentMess = f.bool();
		automate = f.bool();
		missingPaper = f.bool();
		missingPaperNext = f.bool();
		upI = f.i();
		cacheI = -1;
	}
	
	@Override
	protected void clearP() {
		this.data.clear();
		industry.clear();
		hasSentMess = false;
		automate = true;
		missingPaper = false;
		missingPaperNext = false;
		upI = 0;
		cacheI = -1;
	}
	

	private final class Bo extends Booster implements BValue.BValuePlayerOnly {

		private final double from;
		private final double to;
		
		public Bo(double from, double to, boolean isMul) {
			super(new BSourceInfo(sBoost, icon), isMul);
			this.from = from;
			this.to = to;
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

		@Override
		public double getValue(double input) {
			return input;
		}

		@Override
		protected double pget(BOOSTABLE_O o) {
			return o.boostableValue(this);
		}

		@Override
		public double vGet(Player f) {
			return CLAMP.d(value(), from, to);
		}

		@Override
		public double vGet(FactionNPC f) {
			return 1;
		}


		
	}
	
	
	private int cacheI = -1;
	private double cache = 0;
	
	public double value() {
		if (cacheI == GAME.updateI())
			return cache;
		cacheI = GAME.updateI();
		
		
		double workers = number();
		if (workers < POP_MIN) {
			cache = 1;
			return 1;
		}
		
		workers -= POP_BREAK;
		workers = Math.pow(workers, INCREASE_POW);
		
		cache = Math.max(data.projection(), data.value()) - workers;
		cache /= POP_MIN;
		cache += BOOST_TO;
		
		cache = (int)(cache*100)/100.0;
		return cache;
	}
	
	
	public double needed() {
		double workers = number()-POP_BREAK;
		
		if (workers <= 0)
			return 0;
		return Math.pow(workers, INCREASE_POW);
	}
	
	private int number() {
		return (STATS.WORK().EMPLOYED.stat().data(null).get(null)-employment().employed());
	}
	
	
	public int employeesNeeded() {
		double am = needed();
		if (am > 0)
			return (int) Math.ceil(am/data.perEmployee());
		return 0;
	}
	
	@Override
	protected void update(double ds) {
		data.update();
		if (!hasSentMess && reqs.passes(FACTIONS.player())) {
			hasSentMess = true;
			new Mess(this).send();
		}
		
		if (upI >= instancesSize()) {
			missingPaper = missingPaperNext;
			missingPaperNext = false;
			upI = 0;
			return;
		}
		
		AdminInstance ins = getInstance(upI);
		upI++;
		if (!ins.jobs.isSearching()) {
			missingPaper = true;
			missingPaperNext = true;
		}
		
		if (automate && !missingPaper) {
			int min = employeesNeeded();
			min += Math.ceil(employeesNeeded()*0.05);
			if (employeesNeeded() > employmentExtra().target.get()) {
				if (ins.employees().needed() == ins.employees().employed() && ins.employees().employed() < ins.employees().max())
					ins.employees().neededSet(ins.employees().employed()+1);
			}else if (min < employmentExtra().employed()) {
				if (ins.employees().employed() > 0) {
					ins.employees().neededSet(ins.employees().employed()-1);
				}
			}
		}
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}


	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	void automate() {
		automate = !automate;
		
	}
	
	private static class Mess extends MessageSection {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int ai;
		
		Mess(ROOM_ADMIN a){
			super(a.sBoost);
			ai = a.typeIndex();
			
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(SETT.ROOMS().ADMINS.get(ai).sMess);
			section.addRelBody(8, DIR.N, SETT.ROOMS().ADMINS.get(ai).iconBig().huge);
			
		}
		
	}
	
}
