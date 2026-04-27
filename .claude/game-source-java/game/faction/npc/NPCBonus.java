package game.faction.npc;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE_O;
import view.interrupter.IDebugPanel;
import world.region.pop.RDRace;

public final class NPCBonus extends NPCResource implements DOUBLE_O<Boostable>{

	private final int MM = 128-1;
	private final double[] bos = new double[128];

	public final FactionNPC faction;
	
	public NPCBonus(FactionNPC faction, LISTE<NPCResource> all) {
		super(all);
		randomize();
		this.faction = faction;
		if (faction.index() == 5) {
			IDebugPanel.add("Faction bonus max test", new ACTION() {
				
				@Override
				public void exe() {
					
					double[] back = new double[128];
					for (int i = 0; i < bos.length; i++) {
						back[i] = bos[i];
						bos[i] = 1.0;
					}
					
					for (Boostable b : BOOSTING.ALL()) {
						LOG.ln(b.key + " " + b.get(faction));
					}
					
					for (int i = 0; i < bos.length; i++) {
						bos[i] = back[i];
					}
				}
			});
		}
		
	}
	
	@Override
	public double getD(Boostable bo) {
		
		return get(bo.index());
	}
	
	public void clear() {
		for (int i = 0; i < bos.length; i++) {
			bos[i] = 0.0;
		}
	}
	
	public double get(int ran) {
		int ii = ran&MM;
		if (faction.court().king() == null || faction.court().king().roy() == null)
			return bos[ii]*0.5;
		double c = (0.5 + 0.25 * BOOSTABLES.NOBLE().COMPETANCE.get(faction.court().king().roy().induvidual));
		return CLAMP.d(bos[ii]*c, 0, 1);
	}
	
	public void randomize() {
		
		for (int i = 0; i < bos.length; i++) {
			bos[i] = 0.1 + 0.9*i/(bos.length-1);
			bos[i] = CLAMP.d(bos[i], 0, 1);
		}
		
		for (int i = 0; i < bos.length; i++) {
			double d = bos[i];
			int k = RND.rInt(bos.length);
			bos[i] = bos[k];
			bos[k] = d;
		}
		
	}

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.ds(bos);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.ds(bos);
			}
			
			@Override
			public void clear() {
				
			}
		};
	}

	@Override
	protected void update(FactionNPC faction, double seconds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean fromScratch) {
		randomize();
	}

	
	
}
