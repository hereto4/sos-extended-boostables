package settlement.battle.invasion;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.constant.Config;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.battle.invasion.SpotMaker.InvasionSpot;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import settlement.stats.equip.Equip;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.message.MessageText;
import world.army.AD;

final class Invasion {

	private static CharSequence ¤¤Bombardment = "¤Bombardment";
	private static CharSequence ¤¤BombardmentD = "¤The enemy has started bombarding us to clear a path. There is nothing we can do but take cover until they are done.";
	
	private static CharSequence ¤¤Deployment = "¤Deployment";
	private static CharSequence ¤¤DeploymentD = "¤The enemy are deploying their troops. May the gods help us! Quickly enable the battle view and counter them. They must not reach the throne!";
	
	private static CharSequence ¤¤Retreat = "¤Retreat!";
	private static CharSequence ¤¤RetreatD = "¤Enemy forces are weary of fighting and have retreated!";

	private static CharSequence ¤¤LooseD = "¤It's over! The enemy forces have reached the throne, and have taken control of the city. They have sacked your treasury for {0} {1}, and collected {2}% of your warehouse stock.";
	private static CharSequence ¤¤LooseDFaction = "¤The faction of {0} sends its regards. Now that you've bent the knee, they hope you've learned your lesson, and you are now at peace.";
	private static CharSequence ¤¤Victory = "¤Our men have prevailed and our foe is beaten. Rejoice! The {0} survivors can be turned into prisoners, and need a stockade to stay in. Do you accept them? Declining will have the remaining enemies chased down and killed.";
	
	static {
		D.ts(Invasion.class);
	}
	
	public final InvasionSpec spec;
	private STATE state = null;
	private int artillery;
	private double timer = 0;
	final InvasionSpot spot;
	private ArrayListShort activeDivs = new ArrayListShort(Config.battle().DIVISIONS_PER_ARMY);
	public boolean victory;
	private static final IntChecker check = new IntChecker(Config.battle().DIVISIONS_PER_BATTLE);
	
	private final int eDeaths;
	private final int pLosses;
	
	private enum STATE {
		WARNING,
		BOMBARD_TEST,
		BOMBARD,
		PLACEART,
		DEPLOYING,
		FIGHTING,
		WAIT_FOR_REPLY,
		DONE,
	}

	Invasion(FileGetter f) throws IOException{
		spec = new InvasionSpec(f);
		artillery = f.i();
		state = STATE.values()[f.i()];
		timer = f.d();
		spot = new InvasionSpot(f);
		activeDivs.load(f);
		victory = f.bool();
		eDeaths = f.i();
		pLosses = f.i();
	}
	
	public void save(FilePutter file) {
		spec.save(file);
		file.i(artillery);
		file.i(state.ordinal());
		file.d(timer);
		spot.save(file);
		activeDivs.save(file);
		file.bool(victory);
		file.i(eDeaths);
		file.i(pLosses);
		
	}
	
	Invasion(InvasionSpec spec){
		eDeaths = STATS.POP().COUNT.leaves().get(CAUSE_LEAVES.SLAYED().index()).statistics(HCLASSES.OTHER()).get(null);
		pLosses = STATS.POP().COUNT.leaves().get(CAUSE_LEAVES.SLAYED().index()).statistics(HCLASSES.CITIZEN()).get(null);
		this.spec = spec;
		
		FactionNPC f = spec.fi < 0 ? null : (FactionNPC) FACTIONS.getByIndex(spec.fi);
		
		if (f != null)
			FACTIONS.otherFactionSet(f);
		else if (FACTIONS.NPCs().size() > 0)
			FACTIONS.otherFactionSet(FACTIONS.NPCs().rnd());
		
		
		int men = 0;
		
		for (DivGeneration d : spec.divs)
			men += d.indus.length;
		if (men == 0)
			GAME.Error("no men!");
		
		artillery = (int) Math.ceil(men/200.0);
		
		spot = SpotMaker.get(men, spec.wx, spec.wy);
		
		state = STATE.WARNING;
		timer = 0;
		
		GAME.count().INVASIONS.inc(1);
		
		new Prompt(spec, spot.dir.perpendicular()).send();
		
	}
	

	
	public boolean update(double ds) {
		timer += ds;
		
		if (spot.body.width()*spot.body.height() < 8) {
			GAME.Warn(""+spot.body);
			for (InvasionListener li : InvasionListener.all)
				li.weirdness(spec.ref);
			return false;
		}
		switch(state) {
			case WARNING:
				if (!spec.canBeAttacked)
					return false;
				if (timer > TIME.secondsPerDay()/8 && spot.launchProj()) {
					CharSequence title = ¤¤Bombardment;
					CharSequence text = ¤¤BombardmentD;
					new MessageText(title, text).send();
					VIEW.s().getWindow().centererTile.set(spot.body.cX(), spot.body.cY());
					state = STATE.BOMBARD;
					timer = 0;
				}else if (timer > TIME.secondsPerDay()/2) {
					launch();
				}
				break;
			case BOMBARD:
				if (!spec.canBeAttacked)
					return false;
				timer += ds*artillery*0.25;
				while(timer >= 1) {
					timer -= RND.rFloat();
					if (!spot.launchProj()) {
						launch();
						break;
					}
				}
				break;
			case PLACEART:
				if (timer < 40)
					return true;
				deploy();
				break;
			case DEPLOYING:
				if (spec.divs.size() == 0) {
					timer = 0;
					state = STATE.FIGHTING;
				}else {
					while(timer > 1) {
	
						Div d = DivDeployer.deploy(spec.divs, spot);
						if (d != null)
							activeDivs.add(d.index());
						timer -= 1;
						if (spot.size < 8) {
							activeDivs.clear();
						}
					}
				}
				
				
				
				break;
			case FIGHTING:
				if (!fight()) {
					state = victory ? STATE.WAIT_FOR_REPLY : STATE.DONE;
					return true;
				}
				if (timer > TIME.secondsPerDay()*5) {
					state = STATE.DONE;
					remove();
					CharSequence title = ¤¤Retreat;
					CharSequence text = ¤¤RetreatD;
					new MessageText(title, text).send();
					for (InvasionListener ll : InvasionListener.all) {
						ll.weirdness(spec.ref);
					}
					return false;
				}
				break;
			case WAIT_FOR_REPLY:
				return STATS.POP().pop(HTYPES.ENEMY()) > 0;
			default:
				return false;
				
			
		}
		return true;
	}
	
	private void launch() {
		spec.canBeAttacked = false;
		deploy();
		timer = 0;
		if (ArtilleryPlacer.placeArt(spec.divs, spot, artillery)) {
			state = STATE.PLACEART;
		}
		
		CharSequence title = ¤¤Deployment;
		CharSequence text = ¤¤DeploymentD;
		VIEW.s().getWindow().centererTile.set(spot.body.cX(), spot.body.cY());
		
		new MessageText(title, text).send();
	}
	
	private void deploy() {
		GAME.ARMIES().factors.init(GAME.ARMIES().enemy(), 1.0);
		
		state = STATE.DEPLOYING;
	}
	
	void fastForward() {
		LOG.ln(state);
		
		switch(state) {
			case WARNING:
				if (!spec.canBeAttacked)
					return;
				timer = TIME.secondsPerDay()/8;
				break;
			case BOMBARD:
				launch();
			case PLACEART:
				timer = 40;
				break;
			case DEPLOYING:
				break;
			case FIGHTING:
				for (int ei = 0; ei < SETT.ENTITIES().getAllEnts().length; ei++) {
					ENTITY e = SETT.ENTITIES().getAllEnts()[ei];
					if (e instanceof Humanoid) {
						Humanoid h = (Humanoid) e;
						if (h.indu().hType().hostile) {
							h.kill(false, CAUSE_LEAVES.SLAYED());
							ei--;
						}
					}
				}
				break;
			case WAIT_FOR_REPLY:
				return;
			default:
				return;
				
			
		}
	}
	
	boolean fight() {

		for (int i = 0; i < activeDivs.size(); i++) {
			Div d = GAME.ARMIES().division((short) activeDivs.get(i));
			if (!d.active() && d.menNrOf() == 0) {
				activeDivs.remove(i);
				i--;
			}
		}

		if (activeDivs.size() == 0) {
			
			int am = 0;
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid h = (Humanoid) e;
					if (SETT.PATH().reachability.is(h.tc()) && h.indu().hType() == HTYPES.ENEMY() && STATS.BATTLE().ROUTING.indu().get(h.indu()) !=0) {
						am++;
						for (Equip eq : STATS.EQUIP().allE()) {
							int eam = eq.get(h.indu());
							
							if (eam > 0) {
								SETT.THINGS().resources.create(h.tc(), eq.resource, eam);
								eq.set(h.indu(), 0);
							}
						}
					}
				}
			}
			
			String m = "" + Str.TMP.clear().add(¤¤Victory).insert(0, am);
			
			ACTION yes = new ACTION() {
				
				@Override
				public void exe() {
					for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
						if (e instanceof Humanoid) {
							Humanoid h = (Humanoid) e;
							if (SETT.PATH().reachability.is(h.tc()) && h.indu().hType() == HTYPES.ENEMY() && STATS.BATTLE().ROUTING.indu().get(h.indu()) !=0) {
								
								
								
								h.HTypeSet(HTYPES.PRISONER(), null, null);
								STATS.BATTLE().ROUTING.indu().set(h.indu(), 0);
							}
						}
					}
					state = STATE.DONE;
				}
			};
			
			ACTION no = new ACTION() {
				
				@Override
				public void exe() {
					
					state = STATE.DONE;
				}
			};
			VIEW.inters().yesNo.activate(m, yes, no, false);
			resolve(true);
			return false;
		}else if (GAME.ARMIES().enemy().men() > 0){
			COORDINATE c = THRONE.coo();
			for (int x = c.x()-1; x < c.x()+2; x++) {
				for (int y = c.y()-1; y < c.y()+2; y++) {
					for (ENTITY e : SETT.ENTITIES().getAtTile(x, y)){
						if (e instanceof Humanoid) {
							if (((Humanoid)e).indu().hType() == HTYPES.ENEMY()  && ((Humanoid)e).division() != null) {
								victory = false;
								loose();
								resolve(false);
								return false;
							}
						}
					}
				}
			}	
		}
		return true;
		
	}
	
	private void resolve(boolean victory) {
		this.victory = victory;
		int eDeaths = STATS.POP().COUNT.leaves().get(CAUSE_LEAVES.SLAYED().index()).statistics(HCLASSES.OTHER()).get(null)-this.eDeaths;
		int pLosses = STATS.POP().COUNT.leaves().get(CAUSE_LEAVES.SLAYED().index()).statistics(HCLASSES.CITIZEN()).get(null)-this.pLosses;
		
		if (victory) {
			for (InvasionListener ll : InvasionListener.all) {
				ll.victory(pLosses, eDeaths, spec.ref);
			}
			GAME.count().INVASIONS_WON.inc(1);
		}else {
			for (InvasionListener ll : InvasionListener.all) {
				ll.defeat(pLosses, eDeaths, spec.ref);
			}
			GAME.count().INVASIONS_LOST.inc(1);
		}
		
		AD.stats().report(FACTIONS.player(), victory, pLosses, eDeaths);
	}
	
	private void remove() {
		
		check.init();
		for (int i = 0; i < activeDivs.size(); i++) {
			int di = activeDivs.get(i);
			check.isSetAndSet(di);
		}
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.division() != null && check.isSet(h.division().index())) {
					h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
			}
		}
	}

	
	private void loose() {
		
		double am = 0.25 + 0.05*RND.rInt(6);
		
		
		int creds = FACTIONS.player().credits().credits() > 0 ? (int) (FACTIONS.player().credits().credits()*0.75) : 0;
		FACTIONS.player().credits().inc(-creds, CTYPE.MISC);
		
		CharSequence t1 = "" + Str.TMP.clear().add(¤¤LooseD).insert(0, creds).insert(1, Dic.¤¤Currs).insert(2, (int)Math.ceil(100*am));
		
		MessageText m = new MessageText(Dic.¤¤Defeat, t1);
		
		FactionNPC f = spec.fi < 0 ? null : (FactionNPC) FACTIONS.getByIndex(spec.fi);
		
		
		if (f != null && f.isActive()) {
			
			
			m.paragraph(Str.TMP.clear().add(¤¤LooseDFaction).insert(0, f.name));
			
			ROPINIONS.STANCE().setNewStance(f, DIP.VASSAL(), false);
			
		}
		
		remove();

		RESOURCE.remove(am, RBIT.ALL, RTYPE.SPOILS);
		
		m.send();
	}

	public Faction invador() {
		FactionNPC f = spec.fi < 0 ? null : (FactionNPC) FACTIONS.getByIndex(spec.fi);
		
		if (f != null) {
			return f;
		}
		return FACTIONS.NPCs().get(1);
	}

	

	
	
	
}
