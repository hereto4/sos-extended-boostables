package world.entity.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import init.race.RACES;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import util.text.Dic;
import view.tool.PlacableSingle;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.army.AD;
import world.army.WDivRegional;
import world.entity.WEntity;
import world.entity.WEntityConstructor;

public final class WArmyConstructor extends WEntityConstructor<WArmy> {

	private final Stack<WArmy> free = new Stack<>(512);
	private WArmy[] all = new WArmy[128];
	private int amount = 0;
	final WArmySprite sprite = new WArmySprite();
	private boolean tmpStop;
	public static final int MAX = 1024;
	
	public WArmyConstructor(LISTE<WEntityConstructor<? extends WEntity>> all) throws IOException{
		super(all, true);
		IDebugPanelWorld.add(new PlacableSingle("ArmyBig") {
			
			@Override
			public void placeFirst(int tx, int ty) {
				if (!FACTIONS.player().armies().canCreate())
					return;
				WArmy e = create(tx, ty, FACTIONS.player());
				for (int i = 0; i <= 100; i++) {
					
					
					WDivRegional d = AD.regional().create(RACES.all().rnd(),  0.25 + 0.75*(1.0-(1-RND.rFloatP(2))), e);
					d.randomize(RND.rFloat(), RND.rFloat());
					d.menSet(d.menTarget());
				}
				AD.supplies().fillAll(e);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.PATH().map.is.is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSingle("ArmySmall") {
			
			@Override
			public void placeFirst(int tx, int ty) {
				WArmy e = create(tx, ty, FACTIONS.player());
				for (int i = 0; i <= 1; i++) {
					
					
					WDivRegional d = AD.regional().create(RACES.all().rnd(),  0.25 + 0.75*(1.0-(1-RND.rFloatP(2))), e);
					d.randomize(RND.rFloat(), RND.rFloat());
					d.menSet(d.menTarget());
				}
				AD.supplies().fillAll(e);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.PATH().map.is.is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSingle("ArmyEnemyBig") {
			
			@Override
			public void placeFirst(int tx, int ty) {
				WArmy e = create(tx, ty, null);
				AD.factionSet(e, null);
				for (int i = 0; i <= 100; i++) {
					
					WDivRegional d = AD.regional().create(RACES.all().rnd(), 0.25 + 0.75*(1.0-(1-RND.rFloatP(2))), e);
					d.randomize(RND.rFloat(), RND.rFloat());
					d.menSet(d.menTarget());
				}
				AD.supplies().fillAll(e);
				AD.updateArmy(e);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.PATH().map.is.is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSingle("ArmyEnemySmall") {
			
			@Override
			public void placeFirst(int tx, int ty) {
				WArmy e = create(tx, ty, null);

				
				
				WDivRegional d = AD.regional().create(RACES.all().rnd(),  0.25 + 0.75*(1.0-(1-RND.rFloatP(2))), e);
				d.randomize(RND.rFloat(), RND.rFloat());
				d.menSet(d.menTarget());
				AD.supplies().fillAll(e);
				AD.updateArmy(e);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.PATH().map.is.is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSingle("ArmyEnemyFaction") {
			
			@Override
			public void placeFirst(int tx, int ty) {
				Faction f = FACTIONS.NPCs().get(0);
				if (f.capitolRegion() == null)
					return;
				WArmy e = create(tx, ty, f);
				for (int i = 0; i <= 50; i++) {
					
					
					WDivRegional d = AD.regional().create(RACES.all().rnd(),  0.25 + 0.75*(1.0-(1-RND.rFloatP(2))), e);
					d.randomize(RND.rFloat(), RND.rFloat());
					d.menSet(d.menTarget());
				}
				AD.supplies().fillAll(e);
				DIP.WAR().set(f, FACTIONS.player());
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.PATH().map.is.is(tx, ty) ? null : E;
			}
		});
		
	}

	@Override
	protected WArmy create() {
		if (!free.isEmpty())
			return free.pop();
		return new WArmy();
	}
	
	public boolean tmpStop() {
		return tmpStop;
	}
	
	public int armies() {
		return amount;
	}
	
	public int max() {
		return all.length;
	}
	
	public WArmy get(int index) {
		return all[index];
	}
	
	public WArmy tryGet(int index) {
		if (index < 0 || index > all.length)
			return null;
		return all[index];
	}
	
	public boolean canCreate() {
		return amount < MAX && WORLD.ENTITIES().canAdd(fast);
	}
	
	/**
	 * Don't use this, use via faction, or WArmies
	 * @param tx
	 * @param ty
	 * @param f
	 * @return
	 */
	public WArmy create(int tx, int ty, Faction f) {
		
		if (!canCreate()) {
			throw new RuntimeException("too many armies on the map!" + " " + amount + " " + WORLD.ENTITIES().allFast().size());
		}
		
		for (int i = 0; i < all.length; i++) {
			if (all[i] == null) {
				return add(create(), i, tx, ty, f);
			}
		}
		
		int nsize = all.length + 64;
		if (nsize > Short.MAX_VALUE)
			nsize = Short.MAX_VALUE;
		if (nsize <= all.length)
			throw new RuntimeException();
		
		WArmy[] newAll = new WArmy[nsize];
		for (int i = 0; i < all.length; i++) {
			newAll[i] = all[i];
		}
		int i = all.length;
		all = newAll;
		return add(create(), i, tx, ty, f);
	}
	
	public void ret(WArmy wArmyEntity) {
		all[wArmyEntity.index] = null;
		if (!free.isFull())
			free.push(wArmyEntity);
		
		
		
	}

	
	private WArmy add(WArmy a, int index, int tx, int ty, Faction f) {
		a.index = (short) index;
		all[index] = a;
		a.init(tx, ty, f);
		if (!a.added())
			throw new RuntimeException();
		amount ++;
		
		a.name.clear();
		if (f != null) {
			a.name.add(Dic.¤¤Army).s().add(f.armies().all().size());
		}else {
			a.name.add(Dic.¤¤Army);
		}
		return a;
	}
	
	WArmy load(WArmy a) {
		
		if(a.index > all.length) {
			WArmy[] nn = all;
			while(a.index > nn.length)
				nn = new WArmy[nn.length+64];
			for (int i = 0; i < all.length; i++)
				nn[i] = all[i];
			all = nn;
		}
		all[a.index] = a;
		amount ++;
		return a;
	}
	
	
	@Override
	protected void clear() {
		all = new WArmy[128];
		amount = 0;
	}


}
