package settlement.stats.colls;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.main.SETT;
import settlement.room.spirit.grave.GraveData;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import settlement.stats.util.StatHoverer;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.keymap.RMapInt.RMapIntTwo;
import util.text.D;
import util.text.Dic;

public class StatsBurial extends StatCollection{

	private final LIST<StatGrave> graves;
	public STAT DESECRATION;
	
	private final LIST<STAT> others;
	
	private static CharSequence ¤¤more = "¤We want better prospects of being buried in a {0}.";
	private static CharSequence ¤¤less = "¤We do not wish to be buried in a {0}.";
	private static CharSequence ¤¤name = "Burial";
	private static CharSequence ¤¤desc = "Stats related to afterlife.";
	
	
	static {
		D.ts(StatsBurial.class);
	}
	
	public StatsBurial(StatsInit init) {
		super(init, "BURIAL", ¤¤name, ¤¤desc);
		
		ArrayList<StatGrave> graves = new ArrayList<StatGrave>(SETT.ROOMS().GRAVES.size());
		for (GraveData.GRAVE_DATA_HOLDER h : SETT.ROOMS().GRAVES) {
			StatInfo in = new StatInfo(h.graveData().blueprint().info.names, h.graveData().blueprint().info.desc);
			in.setOpinion(¤¤more, ¤¤less);
			
			graves.add(new StatGrave(h.graveData(), init, in));			
		}
		this.graves = graves;
		
		
		
		
		DESECRATION = new STATImp("DESECRATION", init) {

			@Override
			protected int getDD(HCLASS s, Race r) {
				double am = 0;
				for (StatGrave g : graves)
					am += g.grave().disturbance.getD();
				return (int) (CLAMP.d(am, 0, 1)*pdivider(s, r, 0));
			}
		};

		
		ArrayList<STAT> others = new ArrayList<>(all().size());
		for (STAT s : all()) {
			if (s instanceof StatGrave)
				continue;
			others.add(s);
		}
		this.others = new ArrayList<>(others);
		
		
		init.upers.add(new GraveUpdater());
		
	}
	
	public LIST<STAT> others(){
		return others;
	}
	
	public LIST<StatGrave> graves(){
		return graves;
	}
	
	private class GraveUpdater implements StatUpdatable{
		
		private int[] available = new int[graves.size()];
		private int[] needed = new int[graves.size()];
		
		@Override
		public void update(double ds) {
			
			for (StatGrave gr : graves) {
				available[gr.gIndex()] = gr.grave().total.get(null)*100;
			}
			
			for (HCLASS c : HCLASSES.ALL()) {
				if (!c.player)
					continue;
				
				for (StatGrave gr : graves) {
					needed[gr.gIndex()] = 0;
				}
				
				for (Race r : RACES.all()) {
					for (StatGrave gr : r.service().GRAVES.get(c.index())) {
						if (gr.grave().permission().get(c, r)) {
							needed[gr.gIndex()] += STATS.POP().POP.data(c).get(r);
						}
					}
				}
				
				for (Race r : RACES.all()) {
					for (StatGrave gr : r.service().GRAVES.get(c.index())) {
						if (gr.grave().permission().get(c, r)) {
							gr.access.get(c).set(r, 0);
							
							int av = available[gr.gIndex()];
							int needed = this.needed[gr.gIndex()];
							if (av == 0) {
								gr.access.get(c).setD(r, 0);
							}else {
								double d = av/(double)(needed+1);
								gr.access.get(c).setD(r,  CLAMP.d(d, 0, 1));
							}
							
						}else {
							gr.access.get(c).setD(r, 0);
						}
					}
				}
				
			}
			
		}
	}
	
	public static class StatGrave extends STATImp {

		private GraveData h;
		private RMapIntTwo<HCLASS, Race> access = new RMapIntTwo<>(HCLASSES.MAP(), RACES.map());
		
		StatGrave(GraveData h, StatsInit init, StatInfo info) {
			super(h.blueprint().key, "BURR_" + h.blueprint().key, init, info);
			standing = new StatStanding(this, 0, h.standingDef());
			this.h = h;
			
			
			init.savers.put("BURR_ACCESS_" + h.blueprint().key, access);
			info().icon = h.blueprint().icon.resized(Icon.S);
		}

		@Override
		protected int getDD(HCLASS s, Race r) {
			
			return (int) (access.get(s).getD(r) * h.get(s).value.getD(r)*pdivider(s, r, 0));
			
			//return h.get(s).value.getD(r);
		}
		
		public GraveData grave() {
			return h;
		}
		
		int gIndex() {
			return index()-STATS.BURIAL().graves.get(0).index();
		}
		
		
		@Override
		public void hover(GUI_BOX text, HCLASS cl, Race type) {
			GBox b = (GBox) text;
			b.title(info().name);
			GraveData da = grave();

			b.textLL(Dic.¤¤Access);
			b.add(GFORMAT.perc(b.text(), access.get(cl).getD(type)));
			
			
			b.textLL(da.respect.info().name);
			b.add(GFORMAT.perc(b.text(), da.respect.getD(null)));
			b.NL().text(da.respect.info().desc);
			b.NL(4);
			b.textLL(da.get(cl).burried.info().name);
			b.add(GFORMAT.iofkInv(b.text(), (int)da.get(cl).burried.getD(type), (int)da.get(cl).burried.getD(type)+ (int)da.get(cl).failed.getD(type) ));
			
			b.NL().text(da.get(cl).burried.info().desc);
			b.sep();
			StatHoverer.hover(text, this, cl, type);
		}
		
		@Override
		public void hover(GUI_BOX text, Induvidual indu) {
			hover(text, indu.clas(), indu.race());
		}
		
	}
	
}
