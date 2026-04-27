package settlement.stats.standing;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.info.INFO;
import util.statistics.HistoryInt;
import util.text.D;
import view.sett.IDebugPanelSett;

public final class StandingSlave extends Standing{

	{D.t(this);}

	private double cur;
	private double pow;
	private double timer;
	private final static double upI = 4;
	
	public final Boostable target = BOOSTABLES.BEHAVIOUR().SUBMISSION;

	
	
	public final HistoryInt current = new SlaveThing( 
			D.g("Submission", "Submission"),
			D.g("SubmissionD", "The submission of your slaves. Low submission can lead to uprisings. Submission is gained by providing fulfillment for your slaves and keeping their numbers down relative to your citizen population. Different species also have different submission properties.")
			);
	
	private final SlaveFactor fullfillment = new SlaveFactor( 
			2, 
			D.g("Fulfillment", "Fulfillment"),
			D.g("FulfillmentD", "Slaves wants some comfort, same as the next person. In order to keep your slaves submissive, you must provide some basic services for them.")
			);

	private final static HCLASS cl = HCLASSES.SLAVE();
	
	StandingSlave(){

		BValue v = new BValue.BValuePlayerOnly() {
			
			@Override
			public double vGet(Player f) {
				double num = 1 + STATS.POP().POP.data(cl).get(null);
				num *= 4;
				double cit = 1 + STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null);
				double d = num/((cit+num));
				d = Math.pow(d, 0.25);
				return d;
			}

			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}
		};
		new BoosterValue(v, new BSourceInfo(D.g("Numbers"), UI.icons().s.human), 0, true).add(target);
		
		v = new BValue.BValuePlayerOnly() {
			
			@Override
			public double vGet(Player f) {
				return fullfillment.getD();
			}

			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}
		};
		new BoosterValue(v, new BSourceInfo(fullfillment.info().name, UI.icons().s.heart), 0, 8, false).add(target);
		
		v = new BValue.BValuePlayerOnly() {
			
			@Override
			public double vGet(Player f) {
				return CLAMP.d((double)GAME.ARMIES().player().men()/(STATS.POP().POP.data(cl).get(null)*3+1.0), 0, 1);
			}

			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}
		};
		
		new BoosterValue(v, new BSourceInfo(D.g("Army", "Army"), UI.icons().s.sword), 0, 2, false).add(target);
		
		
		IDebugPanelSett.add("submission++", new ACTION() {
			
			@Override
			public void exe() {
				current.incD(0.25);
			}
		});
		
	}
	
	void init() {
		timer = -upI;
		update(0);
		cur = target.get(POP_CL.clP(HCLASSES.SLAVE()));
		
		if (STATS.POP().POP.data(cl).get(null) == 0){
			cur = 1.0;
		}
		
		current.setD(cur);
	}
	
	void update(double ds) {
		
		timer -= ds;
		if (timer < 0){
			timer += upI;
		}else{
			return;
		}
		
		
		update();
		
		if (STATS.POP().POP.data(cl).get(null) == 0) {
			current.set((int)(100));
			return;
		}
			
		
		double tar = target.get(POP_CL.clP(cl));
		int t = (int) (tar*100);
		int c = (int) (cur*100);
		double d = t-c;
		
		d *= upI/(100.0*TIME.secondsPerDay());

		cur = cur+d;
		if (d < 0 && cur < tar)
			cur = tar;
		else if(d > 0 && cur > tar)
			cur = tar;
		
		cur = CLAMP.d(cur, 0, 1);
		current.set((int)(100*cur));
		
		pow = Math.pow(cur, 0.5);
	}
	
	public void update() {
		
		
		
		{
			double ful = 0;
			double max = 0;
			double def = 0;
			
			for (STAT s : STATS.all()) {
				ful += s.standing().get(cl, null);
				max += s.standing().max(cl, null);
				def +=  s.standing().def(cl, null);
			}
			if (ful < def) {
				ful = ful/def;
			}else {
				ful-= def;
				max-= def;
				ful /= max;
				ful = Math.pow(ful, 1.5);
				
			}
			
			fullfillment.setD(ful);
			
		}
		
	}
	
	public double numbersMul(double slaves, double citizens) {
		double po = slaves + (citizens+1);
		slaves /= po;
		slaves = 1.0-slaves;
		return slaves;
	}
	
	@Override
	void save(FilePutter file) {
		file.d(cur);
		current.save(file);
		fullfillment.save(file);
		
	}
	
	@Override
	void load(FileGetter file) throws IOException {
		cur = file.d();
		current.load(file);
		fullfillment.load(file);
		
	}
	
	@Override
	void clear() {
		cur = 0;
		timer = 0;
		current.clear();
		fullfillment.clear();
	}

	@Override
	public double current(Induvidual a) {
		
		return target.get(a);
	}

	@Override
	public double current() {
		return current.getD();
	}
	
	public double currentPow() {
		return pow;
	}

	@Override
	public double target() {
		return target.get(POP_CL.clP(cl));
	}
	
	@Override
	public INFO info() {
		return current.info();
	}
	
	private class SlaveThing extends HistoryInt {

		public SlaveThing(CharSequence name, CharSequence desc) {
			super(name, desc, STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 100;
		}
		
	}
	
	public class SlaveFactor extends HistoryInt {
		
		private SlaveFactor(double weight, CharSequence name, CharSequence desc) {
			super(name, desc, STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		@Override
		public double getD(int fromZero) {
			return get(fromZero)/1000.0;
		}
		
		@Override
		public DOUBLE_MUTABLE setD(double d) {
			super.set((int)(d*1000));
			return this;
		}
		
	}




	
}
