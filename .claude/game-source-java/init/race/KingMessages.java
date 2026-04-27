package init.race;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.paths.PATHS;
import settlement.stats.Induvidual;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import util.text.INSERT;
import util.text.Inserter;
import view.interrupter.IDebugPanel;

public class KingMessages {

	private final LinkedList<Message> all = new LinkedList<>();
	
	public final Message GREETING;
	public final Message STANCE_WARNING;
	public final Message STANCE_DOWN;
	public final Message STANCE_UP;
	public final Message VASSAL_BREAK;
	public final Message VASSAL_BREAK_REQ;
	public final Message PEACE;
	public final Message PEACE_GOOD;
	public final Message PEACE_BAD;
	public final Message DEMAND;
	

	
	
	
	public static KingMessages make(Json data, ExpandInit init) {
		
		String key = data.value("KING_FILE");
		if (!init.kmessagess.containsKey(key)) {
			KingMessages m =  new KingMessages(new Json(PATHS.RACE().text.getFolder("king").get(key)));
			init.kmessagess.put(key,m);
			IDebugPanel.add("King message test: " + key, new ACTION() {
				
				@Override
				public void exe() {
					FactionNPC f = FACTIONS.NPCs().rnd();
					KingMessages m = f.court().king().roy().induvidual.race().kingMessage();
					
					for (Message me : m.all) {
						LOG.ln(me.key);
						for (int i = 0; i < me.all.length; i++) {
							LOG.ln(me.get(f, i));
						}
						LOG.ln();
						
					}
				}
				
			});
		}
		
		return init.kmessagess.get(key);
	}
	
	public KingMessages(Json j) {
		
		GREETING = new Message(j, "GREETING");
		STANCE_UP = new Message(j, "STANCE_UP");
		STANCE_DOWN = new Message(j, "STANCE_DOWN");
		STANCE_WARNING = new Message(j, "STANCE_WARNING");
		VASSAL_BREAK  = new Message(j, "VASSAL_BREAK");
		VASSAL_BREAK_REQ  = new Message(j, "VASSAL_BREAK_REQ");
		PEACE_GOOD = new Message(j, "PEACE_GOOD");
		PEACE_BAD = new Message(j, "PEACE_BAD");
		PEACE = new Message(j, "PEACE");
		DEMAND = new Message(j, "DEMAND");
		
	}
	

	final static Inserter<FactionNPC> insert = new Inserter<>();
	
	static {
		
		
		insert.join(INSERT.faction, new GETTER_TRANS<FactionNPC, Faction>(){

			@Override
			public Faction get(FactionNPC f) {
				return f;
			}
			
		});
		
		insert.join(new Inserter<Faction>(INSERT.faction, "PLAYER_"), new GETTER_TRANS<FactionNPC, Faction>(){

			@Override
			public Faction get(FactionNPC f) {
				return FACTIONS.player();
			}
			
		});
		
		
		
		insert.join(INSERT.player, new GETTER_TRANS<FactionNPC, Integer>(){

			@Override
			public Integer get(FactionNPC f) {
				return RND.rInt();
			}
			
		});
		
		insert.join(INSERT.indu, new GETTER_TRANS<FactionNPC, Induvidual>(){

			@Override
			public Induvidual get(FactionNPC f) {
				return f.court().king().roy().induvidual;
			}
			
		});
	}
	
	
	private static final Str  TMP = new Str(250);
	
	public class Message {
		
		private final CharSequence[] all;
		public final String key;
		
		Message(Json j, String key){
			all = insert.check(j.texts(key));
			KingMessages.this.all.add(this);
			this.key = key;
		}
		
		public CharSequence get(FactionNPC f) {
			return get(f, RND.rInt(all.length));
		}
		
		private CharSequence get(FactionNPC f, int mi) {
			TMP.clear();
			TMP.add(all[mi]);
			
			insert.set(TMP, f);
			
			return TMP;
		}
		
		

		
	}

}
