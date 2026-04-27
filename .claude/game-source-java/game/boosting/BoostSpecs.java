package game.boosting;

import java.util.Comparator;

import game.GAME;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.BOOLEANO;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;

public class BoostSpecs {

	private final ArrayListGrower<BoostSpec> all = new ArrayListGrower<>();
	final boolean connect;
	
	public static final String MUL = ">MUL";
	public static final String ADD = ">ADD";

	private PromiseList li = null;
	public final BSourceInfo info;


	public BoostSpecs(BSourceInfo info, boolean connect) {
		this.connect = connect;
		this.info = info;
		
	}
	
	public BoostSpecs(CharSequence sourceName, SPRITE icon, boolean connect) {
		this.connect = connect;
		info = new BSourceInfo(sourceName, icon);
		
	}
	
	public void push(BoostSpec boost) {
		all.add(boost);
		if (connect)
			boost.boostable.addFactor(boost);
	}
	
	public void push(Booster factor, String targetKey, Object path){
		push(targetKey, factor, path.toString(), false);
		
	}
	
	public BoostSpec push(Booster factor, Boostable target) {
		return push(factor, target, null);
	}
	
	public void push(Boostable target, double value, boolean isMul) {
		Booster w = new BoosterValue(BValue.VALUE1, info, value, isMul);
		BoostSpec boost = new BoostSpec(w, target, null);
		all.add(boost);
		if (connect)
			target.addFactor(boost);
	}
	
	public BoostSpec push(Booster factor, Boostable target, CharSequence append) {
		BoostSpec boost = new BoostSpec(factor, target, append);
		all.add(boost);
		if (connect)
			target.addFactor(boost);
		return boost;
	}
	
	public boolean remove(Boostable target) {
		boolean ret = false;
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).boostable == target) {
				all.remove(all.get(i));
				
				ret = true;
				i--;
			}
		}
		return ret;
	}
	
	public void remove(BoostSpec target) {
		if (all.remove(target)) {
			if (connect) {
				target.boostable.removeFactor(target);
			}
		}
	}
	
	public void replace(int oldI, Booster nnew, Boostable target) {
		BoostSpec boost = new BoostSpec(nnew, target, null);
		all.replace(oldI, boost);
	}
	
	public boolean removeFirst(Boostable target) {
		boolean ret = false;
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).boostable == target) {
				all.remove(all.get(i));
				ret = true;
				i--;
			}
		}
		return ret;
	}
	
	public void pushWeak(Boostable target, double value, boolean isMul, Object path) {
		Booster w = new BoosterValue(BValue.VALUE1, info, value, isMul);
		push(target.key, w, path.toString(), true);
		
	}
	
	public void pushPromise(Boostable target, BValue pValue, double value, boolean isMul){
		if (pValue == null)
			pValue = BValue.VALUE1;
		Booster bb = new BoosterValue(pValue, info, value, isMul);
		push(target.key, bb, "", false);
	}

	
	public void read(Json json, BValue pValue){
		read(BOOSTING.KEY, json, pValue);
	}
	
	public void read(String key, Json json, BValue pValue){
		read(key, json, pValue, null);
	}
	
	public void read(String key, Json json, BValue pValue, CharSequence append){
		read(key, json, pValue, append, true);
	}
	
	public void read(String key, Json json, BValue pValue, CharSequence append, boolean allowMul, String... notallowed){
		
		if (!json.has(key)) {
			return;
		
		}
		
		json = json.json(key);
		
		if (pValue == null)
			pValue = BValue.VALUE1;

		
		for (String k : json.keys()) {
			
			boolean isMul = false;
			double value = json.d(k);
			String path = json.path() + ", line" + json.line(k);
			
			if (k.endsWith(MUL)) {
				if (!allowMul) {
					json.error("Only ADD is allowed in this context", k);
				}
				isMul = true;
				k = k.substring(0, k.length()-MUL.length());
			}else if (k.endsWith(ADD)) {
				k = k.substring(0, k.length()-ADD.length());
			}else {
				json.error("Malformed value. Must be a string that ends with " + ADD + " (addition), or " + MUL + (" (Multiplication)"), k);
			}
			
			for (String na : notallowed) {
				if (na.equalsIgnoreCase(k))
					json.error("This key is special in this context and not allowed!", k);
			}
			BSourceInfo i = info;
			if (append != null)
				i = new BSourceInfo(i.name, append, i.icon);
			Booster bb = new BoosterValue(pValue, i, value, isMul);
			
			push(k, bb, path, false);
			
		}
	}
	

	
	public LIST<BoostSpec> all(){
		return all;
	}
	
	private static int htab = 7;
	
	
	public void hover(GUI_BOX text, BOOSTABLE_O t) {
		hover(text, t, Dic.¤¤Effects);
	}
	
	public void hover(GUI_BOX text, BOOSTABLE_O t, CharSequence name) {
		hover(text, t, name, -1);
	}
	
	public void hover(GUI_BOX text, BOOSTABLE_O t, int catMask) {
		hover(text, t, Dic.¤¤Effects, catMask);
		
	}
	
	public void hover(GUI_BOX text, BOOSTABLE_O t, CharSequence name, int catMask) {
		GBox b = (GBox) text;
		if (name != null)
			b.textLL(name);
		b.NL();
		
		for (int si = 0; si < all().size(); si++) {
			BoostSpec s = all().get(si);
			if ((s.boostable.cat.typeMask & catMask) != 0 && hover(b, s, s.get(t), 0)) {
				b.tab(htab+2);
				b.add(GFORMAT.iOrF(b.text(), s.booster.from()).color(COLOR.WHITE65));
				GText te = b.text();
				te.color(COLOR.WHITE65);
				te.add('<').add('>');
				b.add(te);
				b.add(GFORMAT.iOrF(b.text(), s.booster.to()).color(COLOR.WHITE65));
				b.NL();
			}
			
			
		}
		
		
	}

	public void hover(GUI_BOX text, double input, int catMask) {
		hover(text, input, Dic.¤¤Effects, catMask);
	}
	
//	private static final Str fuckString = new Str(128); 
	
	public void hover(GUI_BOX text, double input, CharSequence name, int catMask) {
//		boolean multiple = false;
		if (all().size() <= 0)
			return;
//		fuckString.clear();
//		for (int bi = 0; bi < all.size(); bi++) {
//			BoostSpec l = all.get(bi);
//			if ((l.boostable.cat.typeMask & catMask) != 0) {
//				if (fuckString.containsText(l.boostable.cat.name)) {
//					multiple = true;
//					break;
//				}
//				fuckString.add(l.boostable.cat.name);
//			}
//		}
//		
//		
//		
//		if (multiple) {
//			fuckString.clear();
//			for (int bi = 0; bi < all.size(); bi++) {
//				BoostSpec l = all.get(bi);
//				if ((l.boostable.cat.typeMask & catMask) != 0) {
//					if (!fuckString.containsText(l.boostable.cat.name)) {
//						hover(text, input, name, l.boostable.cat.filter, catMask);
//						fuckString.add(l.boostable.cat.name);
//					}
//				}
//			}
//			return;
//		}
		
		hover(text, input, name, null, catMask);
		
		
		
	}
	
	public void hoverDetailed(GUI_BOX text, double input, CharSequence name, BOOLEANO<BoostSpec> filter, int catMask) {
		GBox b = (GBox) text;
		if (name != null)
			b.textLL(name);
		b.NL();
		
		for (BoostSpec l : all()) {
			if (filter != null && !filter.is(l))
				continue;
			double d = l.booster.getValue(input);
			if ((l.boostable.cat.typeMask & catMask) != 0 && hover(b, l, d, 0)) {
				
				b.tab(9);
				GText t = b.text();
				t.add('(');
				t.add(l.booster.getValue(0), 2);
				t.s().add('>').s();
				t.add(l.booster.getValue(1.0), 2);
				t.add(')');
				b.add(t);
				b.NL();
			}
		}
		b.NL();
	}
	
	public void hover(GUI_BOX text, double input, CharSequence name, BOOLEANO<BoostSpec> filter, int catMask) {
		
		if (all().size() <= 0)
			return;
			
		GBox b = (GBox) text;
		if (name != null)
			b.textLL(name);
		b.NL();
		int tab = 0;
		
		
		
		for (BoostSpec l : all()) {
			
			double d = l.booster.getValue(input);
			if ((filter == null || filter.is(l)) && (l.boostable.cat.typeMask & catMask) != 0 && hover(b, l, d, tab)) {
				if (tab >= 1) {
					tab = 0;
					b.NL();
				}else {
					tab++;;
				}
			}
		}
		b.NL();
	}
	
	public boolean hover(GBox b, BoostSpec l, double d, int tab) {
		
		if (l.boostable.name == null || l.boostable.name.length() == 0)
			return false;
		
		COLOR c = GCOLOR.T().INACTIVE;
		
		GText t = b.text();
		
		
		if (l.booster.isMul) {
			if (d < 1)
				c = GCOLOR.T().IBAD;
			else if (d > 1)
				c = GCOLOR.T().IGOOD;
			d -= 1;
			GFORMAT.percInc(t, d);
		}else {
			if (d < 0)
				c = GCOLOR.T().IBAD;
			else if (d > 0)
				c = GCOLOR.T().IGOOD;
			if (d == (int) d)
				GFORMAT.iIncr(t, (int)d);
			else
				GFORMAT.f0(t, d);
		}
		
		b.tab(tab*(htab+2));
		b.add(l.boostable.icon.small);
		b.add(b.text().color(c).add(l.tName));
		b.tab(tab*(htab+2)+htab);
		
		t.color(c);
		b.add(t);
		return true;
	}

	private void push(String key, Booster factor, String path, boolean isWeak) {
		if (li == null) {
			li = new PromiseList(this);
			BOOSTING.waiting.add(li);
		}
		li.push(key, factor, path, isWeak);
		
	}

	
	private static class PromiseList implements ACTION{
		
		public final LinkedList<Promise> all = new LinkedList<>();
		public final BoostSpecs coll;
		
		
		PromiseList(BoostSpecs coll){
			this.coll = coll;
		}

		@Override
		public void exe() {

			for (Promise p : this.all) {
				
				LIST<Boostable> bos = BOOSTING.MAP().get(p.key);
				

				if (bos.size()==0) {
					String m = p.path + System.lineSeparator() + "no BOOSTABLE " + " named : " + p.key;
					if (BOOSTING.hasErrored) {
						LOG.ln(m);
					}else {
						BOOSTING.hasErrored = true;
						GAME.Warn(m + System.lineSeparator() + "Available:" + System.lineSeparator() + BOOSTING.available());
					}
					continue;
				}
				
				boolean isWeak = bos.size() > 1;
				
				for (Boostable bo : bos) {
					add(p, bo, isWeak);
				}
				
			}

			ArrayList<BoostSpec> rr = new ArrayList<BoostSpec>(coll.all);
			rr.sort(new Comparator<BoostSpec>() {
				
				@Override
				public int compare(BoostSpec o1, BoostSpec o2) {
					int ii = (""+o1.boostable.cat.name).compareTo(""+o2.boostable.cat.name); 
					if (ii == 0) {
						return (""+o1.boostable.name).compareTo(""+o2.boostable.name);
					}
					return ii;
				}
			});
			coll.all.clear();
			coll.all.add(rr);
			coll.li = null;
		}
		
		private void add(Promise p, Boostable b, boolean isWeak) {
			BoostSpec boost = new BoostSpec(p.factor, b, null);
			for (BoostSpec bb : coll.all) {
				if (boost.isSameAs(bb)) {
					if (isWeak)
						return;
					coll.remove(bb);
					if (coll.connect) {
						bb.boostable.removeFactor(bb);
					}
					break;
				}
			}
			
			coll.push(boost);
		}
		
		private void push(String key, Booster factor, String path, boolean isWeak) {
			
//			BSourceInfo info = coll.info;
//			if (append != null)
//				info = new BSourceInfo(coll.info.name, append, coll.info.icon);
			
			Promise p = new Promise(key, factor, path, isWeak);
			for (Promise pp : all) {
				if (pp.key == key && pp.factor.isMul == factor.isMul)
					all.remove(pp);
			}
			all.add(p);
		}

		
		private static class Promise{
			
			public final String key;
			public final String path;
			public final Booster factor;
			
			Promise(String key, Booster factor, String path, boolean isWeak){
				this.key = key;
				this.factor = factor;
				this.path = path;
			//	this.info = info;
			}
			
		}
		
	}


	public double max(Boostable bo) {
		double m = 0;
		for (BoostSpec s : all())
			if (s.boostable == bo)
				m = Math.max(m, s.booster.max());
		return m;
	}
	
	
	
}
