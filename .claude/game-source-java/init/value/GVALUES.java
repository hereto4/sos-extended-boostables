package init.value;

import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.royalty.Royalty;
import init.INIT;
import init.INIT.InitResource;
import settlement.stats.Induvidual;
import snake2d.util.misc.ACTION;
import util.data.GETTER;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.util.UIValues;
import world.map.regions.Region;

public class GVALUES extends InitResource{

	public static final String KEY = "VALUE";
	public static final GValueCat<Induvidual> INDU = new GValueCat<Induvidual>("HUMAN");
	public static final GValueCat<Region> REGION = new GValueCat<Region>("REGION");
	public static final GValueCat<Faction> FACTION = new GValueCat<Faction>("FACTION");
	public static final GValueCat<Royalty> ROYALTY = new GValueCat<Royalty>("ROYALTY");

	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				INDU.clear();
				REGION.clear();
				FACTION.clear();
				ROYALTY.clear();
			}
		};
	}
	
	public GVALUES(INIT init) {
		super(init);
		INDU.clear();
		REGION.clear();
		FACTION.clear();
		ROYALTY.clear();
		
		
		
	}
	
	@Override
	protected void finishSetup() {
		GValuesInit.init();
		INDU.init();
		REGION.init();
		FACTION.init();
		ROYALTY.init();
		IDebugPanel.add("values", new ACTION() {
			
			@Override
			public void exe() {
				GETTER<Faction> g = new GETTER<Faction>() {

					@Override
					public Faction get() {
						return FACTIONS.player();
					}
					
				};
				VIEW.inters().popup.show(new UIValues<Faction>(FACTION, g), null);
				
			}
		});
	}
	

	
}
