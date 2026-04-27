package settlement.entity.humanoid.ai.main;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.save.Savable;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;

public final class AI {
	
	private static AI s;
	private final AISTATES STATES;
	private final AIEventListeners listeners;
	private final AISUBS SUBS;
	private final AIPlans plans;
	private final AIModules modules;
	private final AIPLAN first;
	private final AIData data = new AIData();
	private final KeyMap<AIElement> map = new KeyMap<AI.AIElement>();
	private final ArrayListGrower<AIElement> all = new ArrayListGrower<>();
	private int[] loadOrder = null;
	
	private AI() {
		AI.s = this;
		STATES = new AISTATES();
		listeners = new AIEventListeners();
		SUBS = new AISUBS();
		plans = new AIPlans();
		modules = new AIModules();
		first = new AIPLAN.PLANRES("planFirst") {
			
			@Override
			protected AISubActivation init(Humanoid a, AIManager d) {
				return resumer.set(a, d);
			}
			
			private final Resumer resumer = new Resumer("standing") {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					return AI.SUBS().STAND.activateTime(a, d, 1);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return true;
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					// TODO Auto-generated method stub
					
				}
			};
		};
		
		GAME.saver().addSpecialSaver(new Savable("HAI") {
			
			@Override
			public void save(FilePutter file) {
				file.i(all.size());
				for (AIElement e : all)
					file.chars(e.key);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				int am = file.i();
				loadOrder = new int[am];
				Arrays.fill(loadOrder, -1);
				for (int i = 0; i < am; i++) {
					String k = file.chars();
					if (map.containsKey(k)) {
						loadOrder[i] = map.get(k).index;
					}
				}
				
			}

		});
		
	}
	
	static AIElement load(int i) {
		if (i < 0 || i >= s.loadOrder.length || s.loadOrder[i] == -1)
			return null;
		return s.all.get(s.loadOrder[i]);
	}
	
	static int save(AIElement e) {
		return e != null ? e.index : -1;
	}
	
//	public static LIST<AIElement> ALL(){
//		return s.all;
//	}
	
	public static final AISTATES STATES() {
		return s.STATES;
	}

	public static final AISUBS SUBS() {
		return s.SUBS;
	}
	public static final AIPlans plans() {
		return s.plans;
	}
	public static final AIModules modules() {
		return s.modules;
	}
	public static final AIEventListeners listeners() {
		return s.listeners;
	}
	
	
	public static final AIPLAN first() {
		return s.first;
	}

	public static void init() {
		new AI();
	}
	
//	public static AIElement get(int index) {
//		
//		return s.all.get(index);
//	}
	
	public static AIData data(){
		return s.data;
	}
	
	public static AIData.AIDataSuspender suspender(String key){
		return s.data.new AIDataSuspender(key);
	}
	
	public static AIDataBit bit(String key){
		return s.data.new AIDataBit(key);
	}
	
	public static class AIElement {
		
		final String className;
		final int index;
		public final String key;
		{
			String cn =  this.getClass().getName();
			String[] ss = cn.split("\\.");
			className = ss[ss.length-1];
			
		
		}
		protected AIElement(String key) {
			AI.s.map.put(key, this);
			index = AI.s.all.add(this);
			this.key = key;
		}
		
		
	} 
	
}
