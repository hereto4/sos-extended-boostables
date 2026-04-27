package game.battle.factors;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.GameDisposable;
import game.battle.Armies;
import game.battle.Army;
import game.battle.div.Div;
import game.battle.thread.status.DivStatus;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.BUtil;
import game.boosting.BoostSpec;
import game.boosting.BoosterAbs;
import game.save.Savable;
import game.time.TIME;
import init.constant.Config;
import settlement.entity.humanoid.Humanoid;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.updating.IUpdater;

public class DivFactors {
	
	static final ArrayListGrower<DivFactor> all = new ArrayListGrower<DivFactor>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}

	private final ArrayListGrower<DataDiv> datas = new ArrayListGrower<DataDiv>();

	final DataA supplies = new DataA();
	

	final DataDiv casulties = new DataDiv();
	final DataDiv routing = new DataDiv();
	final DataDiv projectiles = new DataDiv();
	final DataDiv weariness = new DataDiv();
	final DataDiv kills = new DataDiv();
	
	private final DataDiv morale = new DataDiv();
	private final DataDiv valueFactors = new DataDiv();
	private final ArrayListGrower<BoosterAbs<BOOSTABLE_O>> boosters = new ArrayListGrower<BoosterAbs<BOOSTABLE_O>>();
	private final double fMax;
	
	private final IUpdater updater = new IUpdater(Config.battle().DIVISIONS_PER_BATTLE, 1.0) {

		private double speed = Config.battle().MORALE_HOLDOUT/(120);
		
		private final ArrayListGrower<DataDiv> player = new ArrayListGrower<DataDiv>();
		{
			player.add(casulties);
			player.add(routing);
		}

		
		@Override
		protected void update(int i, double ds) {
			Div div = GAME.ARMIES().division((short) i);
			if (div.men() == 0 || (GAME.ARMIES().enemy().men() == 0 && !div.army().player())) {
				for (DataDiv d : datas) {
					if (d != kills)
						d.setD(div, 0);
				}
			}else {

				if (GAME.ARMIES().enemy().men() == 0){
					{
						double w = weariness.getD(div);
						w -= ds*speed;
						w = CLAMP.d(w, 0, 10000000);
						weariness.setD(div, w);
						if (weariness.getD(div) == 0) {
							kills.setD(div, 0);
						}
					}
					
					for (DataDiv d : player) {
						double am = d.getD(div) - 10.0*ds*TIME.secondsPerDayI();
						am = CLAMP.d(am, 0, 10000);
						d.setD(div, am);
					}
				}else {
					DivStatus s = div.status();
					double w = weariness.getD(div);
					
					double cc = (double)s.engagements()/div.men();
					if (cc > 0) {
						w += ds*cc*speed;
					}
					w = CLAMP.d(w, 0, 10000000);
					weariness.setD(div, w);
				}
				
				
				
				double d = projectiles.getD(div);
				d -= div.men()*ds/10.0;
				d = CLAMP.d(d, 0, div.men()*4);
				projectiles.setD(div, d);
				set(div, ds);
			}
			
			
		}
		
	};
	

	

	
	public DivFactors(Armies a){
		
		new Init(this);
		
		GAME.saver().add(new Savable("BATTLE_DIV_FACTORS") {
			
			@Override
			protected void save(FilePutter file) {
				for (DataDiv d : datas)
					d.save(file);
				supplies.save(file);
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				for (DataDiv d : datas)
					d.load(file);
				supplies.load(file);
			}
			
			@Override
			protected void loadFail(){
				for (DataDiv d : datas) {
					d.clear();
				}
				supplies.clear();
			}
		});
		
		for (DivFactor f : all) {
			for (BoostSpec s : f.specs.all()) {
				boosters.add(s.booster);
			}
		}
		fMax = BUtil.max(boosters, Div.class, 1);
		
	}
	
	private void set(Div div, double ds) {
		
		morale.setD(div, BOOSTABLES.BATTLE().MORALE.get(div)-1);
		double m = BUtil.value(boosters, div)/fMax;
		valueFactors.setD(div, m);
	}
	
	public void init(Army a, double supplies) {
		for (DataDiv d : datas) {
			d.clear(a);
		}
		this.supplies.setD(a, supplies-0.5);
		for (Div div : a.divisions()) {
			morale.setD(div, BOOSTABLES.BATTLE().MORALE.get(div)-1);
			double m = BUtil.value(boosters, div)/fMax;
			valueFactors.setD(div, m);
		}
	}
	
	public void init(Div div) {
		for (DataDiv d : datas)
			d.setD(div, 0);
		morale.setD(div, BOOSTABLES.BATTLE().MORALE.get(div)-1);
		double m = BUtil.value(boosters, div)/fMax;
		valueFactors.setD(div, m);
	}
	
	public void update(double ds) {
		updater.update(ds);
	}
	
	public double morale(Div div) {
		return morale.getD(div)+1;
	}
	
	public double morale(Army as) {
		int m = as.men();
		if (m == 0)
			return 1;
		return morale.army.getD(as)/m + 1;
	}
	
	public double valueCurrent(Div div) {
		return valueFactors.getD(div);
	}
	

	public boolean shouldRun(Div div) {
		return projectiles.getD(div) > (div.menNrOf()>>1);
	}
	
	public LIST<DivFactor> all(){
		return all;
	}
	
	class DataDiv implements DOUBLE_OE<Div>, SAVABLE {

		private final double[] data = new double[Config.battle().DIVISIONS_PER_BATTLE];
		private final long[] dataa = new long[2];
		
		DataDiv(){
			datas.add(this);
		}
		
		@Override
		public double getD(Div t) {
			return data[t.index()];
		}

		@Override
		public void save(FilePutter file) {
			file.ds(data);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.ds(data);
			Arrays.fill(dataa, 0l);
			for (int ai = 0; ai < 2; ai++) {
				for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
					dataa[ai] += (int)(data[ai*Config.battle().DIVISIONS_PER_ARMY+di]*100);
				}
			}
		}

		@Override
		public void clear() {
			Arrays.fill(data, 0);
			Arrays.fill(dataa, 0l);
		}
		
		void clear(Army a){
			for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
				data[a.index()*Config.battle().DIVISIONS_PER_ARMY+di] = 0;
			}
			dataa[a.index()] = 0;
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			dataa[t.army().index()] -= (int)(data[t.index()]*100);
			data[t.index()] = d;
			dataa[t.army().index()] += (int)(data[t.index()]*100);
			return this;
		}
		
		
		
		public final DOUBLE_O<Army> army = new DOUBLE_O<Army>() {

			@Override
			public double getD(Army t) {
				return dataa[t.index()]/100.0;
			}
		
		};
		
	}
	
	class DataA implements DOUBLE_OE<Army>, SAVABLE {
		private final double[] dataa = new double[2];
		
		@Override
		public double getD(Army t) {
			return dataa[t.index()]/100.0;
		}

		@Override
		public void save(FilePutter file) {
			file.ds(dataa);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.ds(dataa);
			
		}

		@Override
		public void clear() {
			Arrays.fill(dataa, 0);
		}

		@Override
		public DOUBLE_OE<Army> setD(Army t, double d) {
			dataa[t.index()] = d;
			return this;
		}
		
	}

	public double casulties(Army enemy) {
		return casulties.army.getD(enemy);
	}

	public double projectiles(Div div) {
		return projectiles.getD(div);
	}

	public void reportCasulty(Div division) {
		casulties.incD(division, 1);
	}
	
	public void reportRout(Div division) {
		routing.incD(division, 1);
	}

	public double casulties(Div div) {
		return casulties.getD(div);
	}

	public void reportProjectile(Div division) {
		projectiles.incD(division, 1);
	}
	
	public void reportKill(Humanoid a) {
		Div d = a.division();
		if (d != null) {
			kills.incD(d, 1);
		}
	}
	
	public int kills(Div div) {
		return (int) kills.getD(div);
	}
	
}
