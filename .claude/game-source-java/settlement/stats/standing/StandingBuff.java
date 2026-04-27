package settlement.stats.standing;

import java.io.IOException;
import java.util.Arrays;

import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import util.text.D;
import world.map.regions.Region;

public class StandingBuff {

	private static CharSequence ¤¤Emmergency = "Emergency";
	
	static {
		D.ts(StandingBuff.class);
	}
	
	public final double time = TIME.secondsPerDay()*8;
	public final double timerI = 1.0/time;
	public final double add = 1;
	private double[] timer = new double[HCLASSES.ALL().size()];
	
	
	public StandingBuff(HCLASS cl) {
		BValue v = new BValue() {
			
			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}
			
			@Override
			public double vGet(Player f) {
				return vGet(POP_CL.clP());
			}
			
			@Override
			public double vGet(Div div) {
				return 0;
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return CLAMP.d(timer[indu.hType().CLASS.index()]*timerI, 0, 1);
			}
			
			@Override
			public double vGet(Region reg) {
				return 0;
			}

			@Override
			public double vGet(PopTime t) {
				if (t.pop.cl != null)
					return CLAMP.d(timer[t.pop.cl.index()]*timerI, 0, 1);
				return 0;
			}
		};
		
		new BoosterValue(v, new BSourceInfo(¤¤Emmergency, UI.icons().s.alert), add, false).add(BOOSTABLES.BEHAVIOUR().LOYALTY);
	}
	
	void update(double ds) {
		for (int i = 0; i < HCLASSES.ALL().size(); i++) {
			timer[i] = CLAMP.d(timer[i]-ds, 0, time*4);
		}
	}
	
	public void execute(HCLASS cl, double time) {
		timer[cl.index()] = Math.max(timer[cl.index()], time);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.dsE(timer);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.dsE(timer);
		}
		
		@Override
		public void clear() {
			Arrays.fill(timer, 0.0);
		}
	};
	
	
}
