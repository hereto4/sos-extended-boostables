package game.faction;

import java.io.IOException;

import game.time.TIMECYCLE;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.statistics.HISTORY_COLLECTION;
import util.statistics.HistoryResource;
import util.text.D;
import util.text.Dic;

public abstract class FResources extends FactionResource{
	
	private static CharSequence ¤¤worn = "¤Furniture";
	private static CharSequence ¤¤theft = "¤Theft";
	
	static {
		D.ts(FResources.class);
	}
	
	private final HistoryResource[] all = new HistoryResource[RTYPE.all.size()*2+1];
	public final TIMECYCLE time;
	
	public FResources(int saved, TIMECYCLE time){
		for (int i = 0; i < all.length; i++)
			all[i] = new HistoryResource(saved, time, false);
		this.time = time;
	}

	
	
	
	public abstract int getAvailable(RESOURCE t);

	public HISTORY_COLLECTION<RESOURCE> in(RTYPE t){
		return all[t.ordinal()];
	}
	
	public HISTORY_COLLECTION<RESOURCE> out(RTYPE t){
		return all[RTYPE.all.size() + t.ordinal()];
	}
	
	public HISTORY_COLLECTION<RESOURCE> total(){
		return all[all.length-1];
	}
	
	public void inc(RESOURCE res, RTYPE type, int am) {
		if (am > 0)
			all[type.ordinal()].inc(res, am);
		else
			all[RTYPE.all.size() + type.ordinal()].inc(res, -am);
		all[all.length-1].inc(res, am);
	}
	
	public void dec(RESOURCE res, RTYPE type, int am) {
		inc(res, type, -am);
	}

	@Override
	protected void save(FilePutter file) {
		for (HistoryResource rr : all)
			rr.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (HistoryResource rr : all)
			rr.load(file);
	}

	@Override
	public void clear() {
		for (HistoryResource i : all) {
			i.clear();
		}
	}

	@Override
	protected void update(double ds, Faction f) {
		// TODO Auto-generated method stub
		
	}
	
	public static enum RTYPE {
		
		PRODUCED(Dic.¤¤Production),
		CONSUMED(Dic.¤¤Consumed),
		TRADE(Dic.¤¤Trade),
		TAX(Dic.¤¤taxes),
		CONSTRUCTION(Dic.¤¤construction),
		FURNISH(¤¤worn),
		EQUIPPED(Dic.¤¤Equipped),
		MAINTENANCE(Dic.¤¤Maintenance),
		SPOILAGE(Dic.¤¤Spoilage),
		ARMY_SUPPLY(Dic.¤¤Supplies + ": " + Dic.¤¤Armies),
		SPOILS(Dic.¤¤Battle + ": "+ Dic.¤¤Spoils),
		DIPLOMACY(Dic.¤¤Diplomacy),
		THEFT(¤¤theft),
		
		;
		
		public static final LIST<RTYPE> all = new ArrayList<FResources.RTYPE>(values());
		
		public final CharSequence name;
		
		private RTYPE(CharSequence name) {
			this.name = name;
		}
	}
	
}
