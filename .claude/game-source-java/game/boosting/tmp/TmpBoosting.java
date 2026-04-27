package game.boosting.tmp;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.GAME.GameResource;
import game.boosting.BoostSpec;
import game.boosting.Booster;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.type.POP_CL;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public class TmpBoosting extends GameResource{

	private static CharSequence ¤¤regs = "Affected Regions";
	private static CharSequence ¤¤groups = "Affected Groups";
	
	static {
		D.ts(TmpBoosting.class);
	}
	
	static ArrayListGrower<TmpBoostSpec> allTmp;
	static KeyMap<TmpBoostSpec> allMap;
	
	private final ArrayListGrower<TmpBoostable<?>> all = new ArrayListGrower<TmpBoostable<?>>();
	public final TmpBoostable<Region> regions = new TmpBoostable<Region>(all, WREGIONS.MAX, this);
	public final TmpBoostable<POP_CL> popcl = new TmpBoostable<POP_CL>(all, POP_CL.ALL().size(), this);
	public final TmpBoostable<Faction> factions = new TmpBoostable<Faction>(all, FACTIONS.MAX(), this);
	
	private final ArrayListGrower<TmpBoostSpec> specs;
	final Data[] datas = new Data[all.get(all.size()-1).startIndex+all.get(all.size()-1).max];
	
	public TmpBoosting(GAME game){
		super("TEMP_EVENTS");
		allTmp = new ArrayListGrower<TmpBoostSpec>();
		allMap = new KeyMap<TmpBoostSpec>();
		specs = allTmp;
		GAME.addOnViewInit(new ACTION() {
			
			@Override
			public void exe() {
				
				allTmp = null;
				
				for (int i = 0; i < datas.length; i++) {
					datas[i] = new Data(specs.size());
				}
				
				KeyMap<ArrayListGrower<BoostSpec>> map = new KeyMap<ArrayListGrower<BoostSpec>>();
				
				for (TmpBoostSpec ts : specs) {
					for (BoostSpec s : ts.spec.all()) {
						String k = s.boostable.key + s.booster.isMul;
						if (!map.containsKey(k)) {
							map.put(k, new ArrayListGrower<BoostSpec>());
						}
						map.get(k).add(s);
					}
				}
				
				for (LIST<BoostSpec> bos : map.all()) {
					boolean isMul = bos.get(0).booster.isMul;
					double min = isMul ? 1 : 0;
					double max = min;
					
					for (BoostSpec s : bos) {
						Booster b = s.booster;
						if (isMul) {
							if (b.to() < 1)
								min *= b.to();
							else
								max *= b.to();
						}else {
							if (b.to()<0)
								min += b.to();
							else
								max += b.to();
						}
					}
					
					new TBooster(bos.get(0).boostable, min, max, isMul);
					
				}
				
			}
		});
		
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void save(FilePutter file) {
		
		file.i(specs.size());
		for (TmpBoostSpec s : specs) {
			file.chars(s.key);
		}
		
		file.i(datas.length);
		for (Data d : datas)
			d.save(file);
		
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		
		
		int am = file.i();
		int[] look = new int[am];
		Arrays.fill(look, -1);
		for (int i = 0; i < am; i++) {
			String k = file.chars();
			if (allMap.containsKey(k)) {
				look[i] = allMap.get(k).index;
			}
		}
		am = file.i();
		for (int i = 0; i < am; i++) {
			if (i < datas.length)
				datas[i].load(file, look);
			else
				new Data(specs.size()).load(file, look);
		}
		
		
		
	}
	
	public LIST<TmpBoostSpec> specs(){
		return specs;
	}
	
	public void hover(GBox b, Faction f) {

		for (TmpBoostSpec s : GAME.BOOST().specs()) {
			boolean faction = factions.is(f, s);
			int regs = 0;
			for (int ri = 0; ri < f.realm().regions(); ri++) {
				if (regions.is(f.realm().region(ri), s))
					regs++;
			}
			boolean pops = false;
			if (f == FACTIONS.player()) {
				for (POP_CL cl : POP_CL.ALL()) {
					if (popcl.is(cl, s)) {
						pops = true;
						break;
					}
				}
			}
			
			
			if (!faction && regs == 0 && !pops)
				continue;
			
			b.add(s.icon);
			b.textLL(s.name);
			b.NL();
			b.text(s.desc);
			b.NL(8);
			
			for (BoostSpec ss : s.spec.all()) {
				b.add(ss.boostable.icon);
				b.textL(ss.boostable.name);
				b.tab(7);
				GText t = b.text();
				if (ss.booster.isMul) {
					t.add('*');
					GFORMAT.f1(t, ss.booster.to());
				}else {
					GFORMAT.f0(t, ss.booster.to());
				}
				b.add(t);
				b.NL();
			}
			
			b.NL(4);
			
			if (regs > 0) {
				b.textLL(¤¤regs);
				b.add(GFORMAT.i(b.text(), regs));
				b.NL();
			}
			
			if (pops) {
				b.textLL(¤¤groups);
				for (POP_CL cl : POP_CL.ALL()) {
					if (cl.race != null && cl.cl != null && popcl.is(cl, s)) {
						b.add(cl.race.appearance().iconBig);
						b.rewind(8);
						if (cl.cl.iconSmall() == null)
							continue;
						b.add(cl.cl.iconSmall());
						b.space();
					}
				}
			}
			
			b.sep();
			
		}	
	}
	
}
