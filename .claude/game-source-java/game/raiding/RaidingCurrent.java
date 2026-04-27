package game.raiding;

import java.io.IOException;

import game.GAME;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import settlement.battle.invasion.InvasionListener;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import view.interrupter.IDebugPanel;
import world.WORLD;
import world.army.AD;
import world.battle.BattleListener;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.Region;

public final class RaidingCurrent implements SAVABLE {

	private enum STATE {
		WARNING, ALLY_HELP, WARNING_REJECTED, ARMY, INVADING, DEFEATED, VICTORY, STRANGENESS, ALLY_FIGHT, APPEAR_REGION
	}

	private Raider raider;
	private int currentArmy;
	private int ai;
	private double timer = 0;
	private STATE state;
	private int invadeRef;
	int iterration;
	private Coo appearCoo = new Coo();
	
	@Override
	public void save(FilePutter file) {
		if (current() != null) {
			file.bool(true);
			file.object(raider);
			file.i(currentArmy);
			file.i(ai);
			file.d(timer);
			file.i(state.ordinal());
			file.i(invadeRef);
			file.i(iterration);
			appearCoo.save(file);
		} else
			file.bool(false);

	}

	@Override
	public void load(FileGetter file) throws IOException {
		if (file.bool()) {
			raider = (Raider) file.object(true);
			currentArmy = file.i();
			ai = file.i();
			timer = file.d();
			state = STATE.values()[file.i()];
			invadeRef = file.i();
			iterration = file.i();
			appearCoo.load(file);
		}else {
			raider = null;
		}
		if (raider == null)
			clear();

	}

	public RaidingCurrent() {

		new InvasionListener() {

			@Override
			protected void weirdness(int ref) {
				if (raider != null && state == STATE.INVADING && ref == invadeRef) {
					state = STATE.STRANGENESS;
					timer = 0;
				}

			}

			@Override
			protected void victory(int lossed, int kills, int ref) {
				if (raider != null && state == STATE.INVADING && ref == invadeRef) {
					GAME.raiders().defeat(raider);
					state = STATE.DEFEATED;
					timer = 0;
				}
			}

			@Override
			protected void defeat(int lossed, int killsf, int ref) {
				if (raider != null && state == STATE.INVADING && ref == invadeRef) {
					state = STATE.VICTORY;
					timer = 0;
				}
			}

			@Override
			protected void register(WArmy a, int ref) {
				if (raider != null && a == army()) {
					currentArmy = -1;
					state = STATE.INVADING;
					invadeRef = ref;
				}
			}
		};

		new BattleListener() {

			@Override
			public void siege(Faction attacker, Region reg) {
				
			}

			@Override
			public void siege(WArmy attacker, Region reg) {
				
			}

			@Override
			public void battle(WArmy a, boolean victory, int losses, int kills, Faction against) {
				if (a == army() && !victory && state == STATE.ARMY) {
					GAME.raiders().defeat(raider);
					state = STATE.DEFEATED;
					timer = 0;
				}
			}

			@Override
			public void battle(Faction a, boolean victory, int losses, int kills, Faction against) {
				
			}
		};
		
		IDebugPanel.add("RAIDER next step", new ACTION() {
			
			@Override
			public void exe() {
				timer += TIME.secondsPerDay()*10;
			}
		});

	}

	public Raider current() {
		return raider;
	}

	public WArmy army() {
		if (raider == null)
			return null;
		if (currentArmy == -1)
			return null;
		WArmy a = WORLD.ENTITIES().armies.tryGet(currentArmy);
		if (a != null && a.added() && a.iteration() == ai) {
			return a;
		}
		return null;
	}

	private void set(Raider raider, STATE state, WArmy a) {

		if (this.raider != raider) {
			iterration++;
			raider.raids ++;
		}

		timer = 0;
		this.raider = raider;
		this.state = state;
		if (a != null) {
			currentArmy = a.armyIndex();
			ai = a.iteration();
		} else {
			currentArmy = -1;
			ai = -1;
		}
	}

	@Override
	public void clear() {
		if (raider != null) {
			if (state == STATE.INVADING) {
				SETT.INVADOR().cancel(invadeRef);
			}
			WArmy a = army();
			if (a != null)
				a.disband();
		}
		raider = null;
		timer = 0;
		currentArmy = -1;
	}

	public void raid(Raider raider) {
		clear();
		
		COORDINATE c = GAME.raiders().util.attackSpot(raider);
		if (c == null) {
			LOG.ln("no spots");
			return;
		}
		
		appearCoo.set(c);
		
		FactionNPC fa = RaidingMap.passThroughFaction(c);

		if (fa != null) {
			set(raider, STATE.ALLY_HELP, null);
			MessAlly.help(raider, fa);
			return;
		}
		
		set(raider, STATE.WARNING, null);
		new MessDemand(raider).send();
	}
	
	public void raid(Raider raider, CharSequence text) {
		clear();
		
		COORDINATE c = GAME.raiders().util.attackSpot(raider);
		appearCoo.set(c);
		
		set(raider, STATE.WARNING_REJECTED, null);
		if (text == null)
			new MessCustom(raider, ""+text).send();
	}

	boolean appear(Raider raider) {
		
		COORDINATE c = GAME.raiders().util.attackSpot(raider);
		if (c == null)
			return false;

		Region reg = WORLD.REGIONS().map.get(c);

		if (reg == null || reg.faction() != FACTIONS.player())
			return false;

		
		appear(raider, c.x(), c.y());
		return true;

	}
	

	public boolean appear(Raider raider, int wx, int wy) {

		if (this.raider != raider)
			iterration++;

		this.raider = raider;
		clear();
		Region reg = WORLD.REGIONS().map.get(wx, wy);
		if (reg == null || !isValid(wx, wy) || reg.capitol()) {
			invadeRef = raider.army.invade(wx, wy, raider.indu);
			set(raider, STATE.INVADING, null);
		} else {
			
			WArmy a = raider.army.spawn(wx, wy, raider.name);
			set(raider, STATE.ARMY, a);
			new MessArmyAppear(raider, wx, wy).send();

		}
		
		return true;
		
	}
	
	public boolean isValid(int wx, int wy) {
		Region reg = WORLD.REGIONS().map.get(wx, wy);
		if (reg == null)
			return false;
		if (reg.faction() != FACTIONS.player())
			return false;
		return true;
	}

	protected void update(double ds, Profiler prof) {
		if (raider == null) {
			return;
		}

		timer += ds;
		switch (state) {
		case ALLY_HELP:
			if (timer > TIME.secondsPerDay()) {
				FactionNPC f = RaidingMap.passThroughFaction(appearCoo);
				if (f == null) {
					set(raider, STATE.WARNING_REJECTED, null);
					new MessDemandRejected(raider).send();
				}else if ((TIME.days().bitCurrent() & 2) == 1 || !GAME.raiders().util.validCoo(appearCoo, raider)){
					MessAlly.fight(raider, f);
					clear();
				}else {
					MessAlly.letThrough(raider, f);
					set(raider, STATE.WARNING_REJECTED, null);
					timer -= 120;
				}
			}
			break;
		case ALLY_FIGHT: 
			FactionNPC f = RaidingMap.passThroughFaction(appearCoo);
			if (f == null) {
				set(raider, STATE.WARNING_REJECTED, null);
				new MessDemandRejected(raider).send();
			}
			if (timer > TIME.secondsPerDay()/2) {
				MessAlly.fight(raider, f);
				clear();
			}
			break;
		case WARNING:
			if (timer > TIME.secondsPerDay()) {
				set(raider, STATE.WARNING_REJECTED, null);
				new MessDemandRejected(raider).send();
			}
			break;
		case WARNING_REJECTED:
			if (timer > 30) {
				
				Region reg = WORLD.REGIONS().map.get(appearCoo.x(), appearCoo.y());
				if (reg == null || !isValid(appearCoo.x(), appearCoo.y()) || reg.capitol()) {
					invadeRef = raider.army.invade(appearCoo.x(), appearCoo.y(), raider.indu);
					set(raider, STATE.INVADING, null);
				}else {
					set(raider, STATE.APPEAR_REGION, null);
					new MessArmySpotted(raider, appearCoo.x(), appearCoo.y()).send();
				}
			}
			break;
		case APPEAR_REGION:
			if (timer > TIME.secondsPerDay()*3) {
				appear(raider, appearCoo.x(), appearCoo.y());
			}
			
			break;
		case ARMY:
			if (army() == null) {
				new MessGoingAway(raider).send();
				clear();
			} else {
				
				WArmy a = army();
				if(a == null) {
					new MessGoingAway(raider).send();
					clear();
					break;
				}
				AD.updateArmy(army());
				if (a.state() == WArmyState.fortifying || a.state() == WArmyState.fortified) {
					
					if (timer > 60*4) {
						new MessGoingAway(raider).send();
						clear();
					}
				}else
					timer = 0;
			}
				break;
		case DEFEATED:
			if (timer > 20) {
				GAME.raiders().defeat(raider);
				new MessDefeated(raider).send();
				clear();
			}
			break;
		case INVADING:
			if (!SETT.INVADOR().invading()) {

				new MessGoingAway(raider).send();
				clear();
			}
			break;
		case STRANGENESS:
			if (timer > 20) {
				new MessGoingAway(raider).send();
				clear();
			}
			break;
		case VICTORY:
			if (timer > 20) {
				raider.hasAttacked = true;
				new MessVictory(raider).send();
				clear();
			}
			break;
		default:
			raider = null;
			break;

		}

	}

	boolean canPay(int iteration) {
		return raider != null && (state == STATE.WARNING || state == STATE.ALLY_HELP);
	}
	
	void setAllyFight() {
		if (raider != null && (state == STATE.WARNING || state == STATE.ALLY_HELP)) {
			set(raider, STATE.ALLY_FIGHT, null);
		}
	}

}
