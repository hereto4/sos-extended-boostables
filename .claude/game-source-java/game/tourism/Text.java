package game.tourism;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.service.StatService;
import snake2d.util.MATH;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import util.text.INSERT;
import util.text.Inserter;

final class Text {
	

	
	public static final Inserter<InsertData> insert = new Inserter<InsertData>();
	static {
		
		insert.join(INSERT.indu, new GETTER_TRANS<InsertData, Induvidual>(){

			@Override
			public Induvidual get(InsertData f) {
				return f.i;
			}
			
		});
		
		insert.join(INSERT.faction, new GETTER_TRANS<InsertData, Faction>(){

			@Override
			public Faction get(InsertData f) {
				return FACTIONS.player();
			}
			
		});
		
		insert.join(INSERT.player, new GETTER_TRANS<InsertData, Integer>(){

			@Override
			public Integer get(InsertData f) {
				return STATS.RAN().get(f.i, 0);
			}
			
		});
		
		insert.new II("ATTRACTION") {
			
			@Override
			public void set(InsertData t, Str str) {
				RoomBlueprintImp att = TOURISM.attraction(t.i);
				str.add(att.info.name);
			}
		};
		
		insert.new II("ATTRACTIONS") {
			
			@Override
			public void set(InsertData t, Str str) {
				RoomBlueprintImp att = TOURISM.attraction(t.i);
				str.add(att.info.names);
			}
		};
		
		insert.new II("ATTRACTION_EMPLOYEE") {
			
			@Override
			public void set(InsertData t, Str str) {
				RoomBlueprintImp att = TOURISM.attraction(t.i);
				str.add(att.employment().title);
			}
		};
		
		insert.new II("SERVICE") {
			
			@Override
			public void set(InsertData t, Str str) {
				StatService s = TOURISM.service(t.i);
				str.add(s.name);
			}
		};
		
		insert.new II("INN_NAME") {
			
			@Override
			public void set(InsertData t, Str str) {
				RoomInstance ins = SETT.ROOMS().INN.getter.get(t.inn);
				if (ins != null)
					str.add(ins.name());
				
				
				
			}
		};
		
		insert.new II("INN_HOST") {
			
			@Override
			public void set(InsertData t, Str str) {
				RoomInstance ins = SETT.ROOMS().INN.getter.get(t.inn);
				if (ins == null)
					return;

				if (ins.employees().employed() > 0){
					int e = (int) (ins.employees().employed()*RND.rFloat());
					for (Humanoid a : ins.employees().employees()) {
						if (e-- <= 0) {
							str.add(STATS.APPEARANCE().name(a.indu()));
							return;
						}
					}
				}
				str.add(STATS.APPEARANCE().name(RACES.all().rnd(), HTYPES.SUBJECT(), 0, RND.rInt(), 0));
				
			}
		};
		
	}
	
	final static class InsertData {
		
		public double rating;
		public Induvidual i;
		public COORDINATE inn;
	}
	
	public static final InsertData dd = new InsertData();
	
	public final Entry rating;
	public final Entry attraction;
	public final Entry service;
	public final Entry inn;
	
	private static final Str str = new Str(128);
	
	public Text(Json json){
		rating = new Entry(0, json, "RATING");
		attraction = new Entry(1, json, "ATTRACTION");
		service = new Entry(2, json, "SERVICE");
		inn = new Entry(3, json, "INN");
	}
	
	static class Entry {
		
		private CharSequence[][] chars = new CharSequence[3][];
		private final int scroll;
		
		Entry(int index, Json json, String key){
			scroll = index*8;
			if (json != null) {
				json = json.json(key);
				chars[0] = insert.check(json.texts("BAD"));
				chars[1] = insert.check(json.texts("OK"));
				chars[2] = insert.check(json.texts("GOOD"));
			}else {
				chars[0] = new CharSequence[] {""};
				chars[1] = new CharSequence[] {""};
				chars[2] = new CharSequence[] {""};
			}
		}
		
		public Str get(InsertData data) {
			int ri = (int) Math.round((data.rating*2 - 0.25));
			ri = CLAMP.i(ri, 0, 2);
			int r = (int) (STATS.RAN().get(data.i, scroll));
			str.clear().add(chars[ri][MATH.mod(r, chars[ri].length)]);
			insert.set(str, data);
			return str;	
		}
		
	}
	
	
	
}