package util.text;

import game.GAME;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.data.GETTER_TRANS;

public class Inserter<T> {

	public final ArrayListGrower<II> all = new ArrayListGrower<>();
	private final KeyMap.CharMap<II> map = new KeyMap.CharMap<II>();
	
	public Inserter() {

	}
	
	public Inserter(Inserter<T> ii, String prefix) {
		for (II i : ii.all) {
			new II(prefix + i.key) {

				
				@Override
				public void set(T t, Str str) {
					if (t == null)
						return;
					i.set(t, str);
				}
			
			};
		}
		
	}
	
	public abstract class II extends StrInserter<T>{

		public II(String key){
			super(key);
			all.add(this);
			map.put(key, this);
		}
		
		@Override
		public abstract void set(T t, Str str);
		
	}
	
	private static int ranI;
	private static long ran = RND.rInt();
	public static void setRandom(long ran) {
		ranI = 0;
		Inserter.ran = ran;
	}
	
	public int ran() {
		ran = ran >> ranI;
		
		ranI += 4;
		if (ranI >= 64)
			ranI = 0;
		return (int) (ran&0x0F);
	}
	
	private static boolean ww = false;
	
	public void check(CharSequence str) {
		
		for (int ii = 0; ii < 100; ii++) {
			CharSequence s = Str.getInsert(str, ii);
			if (s == null)
				break;
			if(map.containsKey(s))
				continue;
			GAME.WarnLight("missing insert: " + s + ", in text: " + str);
			if (!ww) {
				ww = true;
				String v = "Available:" + System.lineSeparator();
				v+= map.keysString();
				GAME.Warn(v);
			}
		}
		
		
		
	}
	
	public CharSequence[] check(CharSequence[] str) {
		for (CharSequence cc : str)
			check(cc);
		return str;
	}
	
	public <K> Inserter<T> join(Inserter<K> in, GETTER_TRANS<T, K> trans){
		
		for (Inserter<K>.II ii : in.all) {
			join(ii, trans);
		}
		return this;
	}
	
	public <K> Inserter<T> join(Inserter<K>.II ii, GETTER_TRANS<T, K> trans){
		new II(ii.key) {

			@Override
			public void set(T t, Str str) {
				ii.set(trans.get(t), str);
			}
		};
		return this;
	}
	
	public Inserter<T> join(Inserter<T> in){
		
		for (Inserter<T>.II ii : in.all) {
			join(ii);
		}
		return this;
	}
	
	public Inserter<T> join(Inserter<T>.II ii){
		map.put(ii.key, ii);
		all.add(ii);
		return this;
	}
	
	public void set(Str str, T i) {
		
		if (i == null)
			return;
		
		for (int ii = 0; ii < 100; ii++) {
			CharSequence s = Str.getInsert(str, ii);
			if (s == null)
				break;
			II iii = map.get(s);
			if (iii == null)
				continue;
			try {
				iii.insert(i, str);
			}catch(Exception e) {
				throw new RuntimeException("problems with insert " + iii.key, e);
			}
			
			ii--;
			
		}
	}
}
