package settlement.stats.colls;

import java.io.IOException;

import game.GAME;
import init.race.Race;
import init.race.appearence.RType;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATInduOnly;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;

public final class StatsAppearance extends StatCollection {

	public final INT_OE<Induvidual> name;
	public final INT_OE<Induvidual> customName;
	public final INT_OE<Induvidual> gender;
	public final LIST<INT_OE<Induvidual>> all;
	public final INT_OE<Induvidual> favo;
	
	public final STAT dead;
	

	
	public StatsAppearance(StatsInit init){
		super(init, "APPEARANCE", "", "");
		name = init.count.new DataShort("APPEARENCE_NAME");
		customName = init.count.new DataBit("APPEARANCE_NAMEC");
		gender = init.count.new DataNibble("APPEARENCE_GENDER");
		
		
		init.savers.put("APPEARENCE_SCRAP", new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.i(allNames.size());
				for (Str s : allNames)
					s.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				int am = file.i();
				allNames.clearSoft();
				for (int i = 0; i < am; i++) {
					Str s = new Str(32);
					s.load(file);
					allNames.add(s);
				}
			}
			
			@Override
			public void clear() {
				//allNames.clearSoft();
				
			}
		});
		
		dead = new STATInduOnly("DEAD", init, init.count.new DataBit("APPEARENCE_DEAD"));
		
		LinkedList<INT_OE<Induvidual>> tt = new LinkedList<>();
		tt.add(name);
		tt.add(customName);
		tt.add(gender);
		favo = init.count.new DataBit("FFAVOURITE");
		tt.add(favo);
		
		all = new ArrayList<>(
				tt);
		
		init.copier.add(all);
		
		init.onConstruct.add(new StatInitable() {
			
			@Override
			public void init(Induvidual h) {
				double ri = RND.rFloat(h.race().appearance().tMax);
				int gi = 0;
				for (RType t : h.race().appearance().types) {
					ri -= t.spec.occurrence;
					if (ri <= 0) {
						gi = CLAMP.i(gi, 0, h.race().appearance().types.size()-1);
						gender.set(h, gi);
						int n =  (RND.rInt(t.names.firstNames.size()&0x0FF) << 8);
						n |= RND.rInt(t.names.lastNames.size()&0x0FF);
						name.set(h, n);
						break;
					}
					gi++;	
				}
			}
		});
	}
	
	private RType get(Induvidual i) {
		if (i.hType() == HTYPES.CHILD())
			return i.race().appearance().child;
		return i.race().appearance().types.getC(gender.get(i));
	}

	public COLOR colorSkin(Induvidual i) {
		COLOR a = get(i).spec.skin.get(i, false);
		COLOR col = STATS.DISEASE().color(i);
		if (col != null)
			return colorAdd(a, col);
		col = GAME.EVENT().color(i);
		if (col != null)
			return colorAdd(a, col);
		return a;
	}
	
	private final ColorImp color = new ColorImp();
	private double ci = 1.0/127.0;
	
	private COLOR colorAdd(COLOR ca, COLOR cb) {
		int r = ca.red()&0x0FF;
		int r2 = cb.red()&0x0FF;
		if (r2 > 127)
			r += r2-127;
		else
			r *= r2*ci;
		if (r > 0x0FF)
			r = 0x0FF;
		
		int g = ca.green()&0x0FF;
		int g2 = cb.green()&0x0FF;
		if (g2 > 127)
			g += g2-127;
		else
			g *= g2*ci;
		if (g > 0x0FF)
			g = 0x0FF;
		
		int b = ca.blue()&0x0FF;
		int b2 = cb.blue()&0x0FF;
		if (b2 > 127)
			b += b2-127;
		else
			b *= b2*ci;
		if (b > 0x0FF)
			b = 0x0FF;
		
		color.set(r, g, b);
		return color;
	}
	
//	private COLOR colorAdd(COLOR a, COLOR b) {
//		int r = a.red()+b.red();
//		int g = a.green()+b.green();
//		int bl = a.blue()+b.blue();
//		color.set(r&0x0FF, g&0x0FF, bl&0x0FF);
//		return color;
//	}
	
	public COLOR colorClothes(Induvidual i) {
		if (i.hType().hostile) {
			return clothesEnemy[(int) (STATS.RAN().get(i, 0, 3) & 0x07)];
		}
		if (i.hType() == HTYPES.SLAVE() || i.hType() == HTYPES.PRISONER())
			return clothesSlave[(int) (STATS.RAN().get(i, 3, 3) & 0x07)];
		if (i.hType() == HTYPES.NOBILITY()) {
			return clothesNoble[0];
		}
		if (i.hType() == HTYPES.TOURIST()) {
			return clothesTourist[(int) (STATS.RAN().get(i, 3, 4) & 0x0F)];
		}
		return i.race().appearance().colors.clothes(STATS.EQUIP().CLOTHES.stat().indu().get(i), (int) ((STATS.RAN().get(i, 6, 3)) & 0x07));
	}
	
	private final static COLOR[] clothesSlave = new COLOR[] {
		new ColorImp(114, 67, 36).shade(0.5),
		new ColorImp(114+5, 67, 36).shade(0.5),
		new ColorImp(114, 67+5, 36).shade(0.5),
		new ColorImp(114, 67, 36+5).shade(0.5),
		new ColorImp(114+5, 67+5, 36).shade(0.5),
		new ColorImp(114, 67+5, 36+5).shade(0.5),
		new ColorImp(114+5, 67, 36+5).shade(0.5),
		new ColorImp(114+5, 67+5, 36+5).shade(0.5),};

	private final static COLOR[] clothesTourist = COLOR.interpolate(new ColorImp(80,80, 127), new ColorImp(40, 40, 62), 16);
	
	public final static COLOR[] clothesEnemy = new COLOR[] {
		new ColorImp(20, 20, 20),
		new ColorImp(20+5, 20, 20),
		new ColorImp(20, 20+5, 20),
		new ColorImp(20, 20, 20+5),
		new ColorImp(20+5, 20+5, 20),
		new ColorImp(20, 20+5, 20+5),
		new ColorImp(20+5, 20, 20+5),
		new ColorImp(20+10, 20, 20),};
	
	public final static COLOR[] clothesNoble = new COLOR[] {
		new ColorImp(84, 0, 127),
		};
	
//	public COLOR colorArmour(Induvidual i) {
//		if (i.hType() == HTYPE.ENEMY) {
//			return armorEnemy[(int) (i.randomness & 0x07)];
//		}
//		return i.race().appearance().colors.armour(STATS.EQUIP().BATTLEGEAR.stat().indu().get(i), (int) ((i.randomness>>9) & 0x07));
//	}
	
	public COLOR colorLegs(Induvidual i) {
		return get(i).spec.leg.get(i, false);
	}
	
	private final Str ss = new Str(32);
	
	public CharSequence name(Induvidual i) {
		return name(i.race(), i.hType(), gender.get(i), name.get(i), customName.get(i));
	}
	
	public CharSequence name(Race r, HTYPE t, int gender, int name, int custom) {
		if (custom != 0) {
			return allNames.get(name);
		}
		int first = name >>> 8;
		int last = name & 0x0FF;
		ss.clear();
		ss.add(r.appearance().types.getC(gender).names.firstNames.getC(first));
		ss.s();
		ss.add(r.appearance().types.getC(gender).names.lastNames.getC(last));
		return ss;
	}
	
	public CharSequence nameFirst(Induvidual i) {
		if (customName.get(i) != 0) {
			return allNames.get(name.get(i));
		}
		int first = name.get(i) >>> 8;
		return i.race().appearance().types.getC(gender.get(i)).names.firstNames.getC(first);
	}
	
	public CharSequence nameLast(Induvidual i) {
		if (customName.get(i) != 0) {
			return allNames.get(name.get(i));
		}
		int last = name.get(i) & 0x0FF;
		return i.race().appearance().types.getC(gender.get(i)).names.lastNames.getC(last);
	}
	
	public Str customName(Induvidual i) {
		if (customName.get(i) != 0) {
			return allNames.get(name.get(i));
		}
		
		int k = 0;
		if (allNames.size() >= 0x0FFFF) {
			k = RND.rInt(0x0FFFF);
		}else {
			k = allNames.size();
			Str s = new Str(32);
			s.add(name(i));
			allNames.add(s);
		}
		
		customName.set(i, 1);
		name.set(i, k);
		return allNames.get(k);
	}
	
	public void portraitRender(SPRITE_RENDERER r, Induvidual a, int x, int y, int scale) {
		
		a.race().appearance().types.getC(gender.get(a)).portrait.render(r, x, y, a, scale);
		
	}

	
	private ArrayListResize<Str> allNames = new ArrayListResize<>(1024, 0x0FFFF);
	
}
