package game.events.killer;

import game.GAME;
import init.paths.PATHS;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.gui.misc.GButt;
import util.text.Dic;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;

final class Messenger {

	private final Insert[] ins;
	
	private final M mFirst;
	private final M mSecond;
	private final M mAgain;
	private final M mSuspect;
	private final M mSuspectFail;
	private final M mSuspectSuccess;
	private final M mColdCase;

	public Messenger() {
		
		Json js = new Json(PATHS.TEXT_MISC().getFolder("serialKiller").get("_INFO"));
		mFirst = new M(js.json("FIRST"));
		mSecond = new M(js.json("SECOND"));
		mAgain = new M(js.json("AGAIN"));
		mSuspect = new M(js.json("SUSPECT"));
		mSuspectFail = new M(js.json("SUSPECT_FAIL"));
		mSuspectSuccess = new M(js.json("CAUGHT"));
		mColdCase = new M(js.json("COLD"));
		
		
		ins = new Insert[] {
			new Insert("VICTIM_NAME") {
				@Override
				public void set(Data t, Str str) {
					str.add(STATS.APPEARANCE().name(t.victim.indu()));
				}
			},
			new Insert("VICTIM_AGE") {
				@Override
				public void set(Data t, Str str) {
					str.add((int)Math.ceil(STATS.POP().age.years.getD(t.victim.indu())));
				}
			},
			new Insert("VICTIM_RACE") {
				@Override
				public void set(Data t, Str str) {
					str.add(t.race.info.name);
				}
			},
			new Insert("KILLER_SIGNATURE") {
				@Override
				public void set(Data t, Str str) {
					str.add(t.type.method);
				}
			},
			new Insert("KILLER_ALIAS") {
				@Override
				public void set(Data t, Str str) {
					str.add(t.type.name);
				}
			},
			new Insert("KILLER_NAME") {
				@Override
				public void set(Data t, Str str) {
					str.add(STATS.APPEARANCE().name(t.killer.indu()));
				}
			},
			new Insert("SUSPECT_NAME") {
				@Override
				public void set(Data t, Str str) {
					CharSequence s = t.suspect != null ? STATS.APPEARANCE().name(t.suspect.indu()) : Dic.empty;
					str.add(s);
				}
			},
		};
		
		
	}
	
	public void murder(Data data) {
		
		M m = mFirst;
		if (data.murders == 2)
			m = mSecond;
		else if (data.murders > 2)
			m = mAgain;
		
		Str s = Str.TMP;
		s.clear().add(m.body);
		
		for (Insert i : ins) {
			while (i.insert(data, s))
				;
		}
		
		
		
		MessageText mm = new MessageText(m.title).paragraph(s);
		s.clear();
		s.add('"').add(data.type.messages[data.murders-1]).add('"').NL().s().s().s().s().add('-').add(data.type.name);
		mm.paragraph(s);
		mm.send();
		
	}
	
	public void murderSuspect(Data data) {
		
		M m = mFirst;
		if (data.murders == 2)
			m = mSecond;
		else if (data.murders > 2)
			m = mAgain;
		
		Str s = Str.TMP;
		s.clear().add(m.body);
		
		for (Insert i : ins) {
			while (i.insert(data, s))
				;
		}
		
		String mess1 = "" + s;
		s.clear();
		s.add('"').add(data.type.messages[data.murders]).add('"').NL().s().s().s().s().add('-').add(data.type.name);
		String mess2 = ""+s;
		
		s.clear();
		s.add(mSuspect.body);
		for (Insert i : ins) {
			while (i.insert(data, s));
		}
		
		new mSuspect(m.title, mess1, mess2, ""+s, data.suspect.id(), data.murders, data.killer.id()).send();
		
	}
	
	public static class mSuspect extends MessageSection {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String mess1;
		private final String mess2;
		private final String quest;
		private final int supsect;
		private final int murders;
		private final int killer;
		
		mSuspect(CharSequence title, CharSequence mess1, CharSequence mess2, CharSequence quest, int suspect, int murders, int killer){
			super(title);
			this.mess1 = ""+mess1;
			this.mess2 = ""+mess2;
			this.quest = ""+quest;
			this.supsect = suspect;
			this.murders = murders;
			this.killer = killer;
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(mess1);
			paragraph(mess2);
			paragraph(quest);
			
			section.addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤Yes) {
				@Override
				protected void clickA() {
					if (murders == GAME.events().killer.murders() && GAME.events().killer.theKiller() != null && GAME.events().killer.theKiller().id() == killer)
						GAME.events().killer.setSuspect(supsect);
					close();
				}
				@Override
				protected void renAction() {
					activeSet(GAME.events().killer.suspect()== -1 && murders == GAME.events().killer.murders() && GAME.events().killer.theKiller() != null && GAME.events().killer.theKiller().id() == killer);
				}
			});
			
			section.addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤No) {
				@Override
				protected void clickA() {
					close();
				}
				@Override
				protected void renAction() {
					activeSet(GAME.events().killer.suspect()== -1 && murders == GAME.events().killer.murders() && GAME.events().killer.theKiller() != null && GAME.events().killer.theKiller().id() == killer);
				}
			});
		}
		
	}
	
	
	
	
	public void over(Data data) {
		
		M m = mColdCase;
		Str s = Str.TMP;
		s.clear().add(m.body);
		
		for (Insert i : ins) {
			i.insert(data, s);
		}
		
		new MessageText(m.title).paragraph(s).send();
		
	}
	
	public void caught(Data data) {
		
		M m = mSuspectSuccess;
		Str s = Str.TMP;
		s.clear().add(m.body);
		
		for (Insert i : ins) {
			i.insert(data, s);
		}
		
		new MessageText(m.title).paragraph(s).send();
		
	}
	
	public void fail(Data data) {
		
		M m = mSuspectFail;
		Str s = Str.TMP;
		s.clear().add(m.body);
		
		for (Insert i : ins) {
			i.insert(data, s);
		}
		
		new MessageText(m.title).paragraph(s).send();
		
	}
	
	static class Data {
		
		public int murders;
		public Humanoid suspect;
		public Humanoid killer;
		public Corpse victim;
		public Race race;
		public KillerType type;
		
	}
	
	private static abstract class Insert extends StrInserter<Data>{

		public Insert(String key) {
			super(key);
		}
		
	}
	
	private static class M {

		public final CharSequence title;
		public final CharSequence body;
		
		public M(Json json) {
			title = json.text("TITLE");
			body = json.text("BODY");
		}
		
	}
	
}
