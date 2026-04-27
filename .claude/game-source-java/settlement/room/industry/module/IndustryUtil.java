package settlement.room.industry.module;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.Boostable;
import game.boosting.Booster;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;

public final class IndustryUtil {

	private IndustryUtil() {
		
	}
	
	public static double calcConsumptionRate(double base, Humanoid h, IndustryRate rate, RoomInstance ins, RESOURCE res) {
		return calcProductionRate(base, h, rate, rate.bonus(), ins)/res.conBoost(h.indu());
	}
	
	public static double calcConsumptionRate(double base, IndustryRate rate, RoomInstance ins, RESOURCE res) {
		double mul = 1;
		
		Boostable bonus = res.conBoost();
		
		if (bonus != null) {
			mul = bonus.get(POP_CL.clP());
			
		}
		
		return calcProductionRate(base, rate, rate.bonus(), ins)/mul;
	}
	
	public static double roomBonus(RoomInstance ins, IndustryRate rate) {
		
		double r = 1;
		r *= 0.25 + 0.75-0.75*(ins).getDegrade();
		if (rate != null) {
			for (RoomBoost b : rate.boosts())
				r *= b.get(ins);
		}
		
		return r;
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(base, h, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		r*= base;
		if (bonus != null)
			r*= bonus.get(h.indu());
		return r;
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(baseRate, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		r *= ins.employees().totEfficiency();
		
		double am = 0;
		double mul = 0;
		
		if (bonus != null) {
			for (Humanoid a : ins.employees().employees()) {
				mul += bonus.get(a.indu());
				am++;
			}
		}
		
		
		
		if (am > 0) {
			mul /= am;
		}else {
			mul = 1;
		}
		
		return r*mul*baseRate;
	}
	
	private static double[] values = new double[100];
	
	public static void hoverProductionRate(GUI_BOX text, double baseRate, IndustryRate rate, RoomInstance ins) {
		hoverProductionRate(text, baseRate, rate, rate.bonus(), ins);	
	}
	
	public static void hoverProductionRate(GUI_BOX text, double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		GBox b = (GBox) text;
		
		b.NL(4);
		
		b.textLL(Dic.¤¤Base);
		b.NL();
		b.text(Dic.¤¤Rate);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), baseRate));
		b.NL();
		
		b.text(Dic.¤¤Employees);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), ins.employees().employed()));
		b.NL();
		
		b.text(RoomEmploymentIns.¤¤Workload);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), ins.employees().efficiency()));
		b.NL();
		
		b.text(RoomEmploymentIns.¤¤Proximity);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), ins.employees().proximity()));
		b.NL();
		
		if (ins.blueprintI().employment().countInput()) {
			b.text(RoomEmploymentIns.¤¤ProximityInput);
			b.tab(6);
			b.add(GFORMAT.f1(b.text(), ins.employees().fetchProximity()));
			b.NL();
		}
		
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), baseRate*ins.employees().employed()*ins.employees().totEfficiency(), baseRate*ins.employees().employed()));
		b.NL(8);
		
		hoverBoosts(text, baseRate*ins.employees().employed(), rate, bonus, ins, ins.employees().totEfficiency());
		
		b.NL();
		
	}
	
	public static void hoverBoosts(GUI_BOX text, final double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins, double totEfficiency) {
		GBox b = (GBox) text;
		
		double mul = 1.0;
		double add = 0;
		
		int tot = 0;
		Arrays.fill(values, 0);
		for (Humanoid a : ins.employees().employees()) {

			tot ++;
			
			int vi = 0;
			
			if (STATS.WORK().EMPLOYED.get(a) == ins) {
				for (Booster s : bonus.all()) {
					if (s.isMul) {
						values[vi] += s.get(a.indu())-1.0;
					}else {
						values[vi] += s.get(a.indu());
					}
					
					vi++;
				}
				
			}
		}
		
		if (tot > 0) {
			int vi = 0;
			
			for (Booster s : bonus.all()) {
				if (s.isMul) {
					values[vi]/=tot;
					values[vi] += 1;
					mul *= values[vi];
				}else {
					values[vi]/=tot;
					add += values[vi];
				}
				
				vi++;
			}
			
			
		}
		
		
		{
			b.textLL(Dic.¤¤Multipliers);
			b.NL();
			
			if (ins.degrader(ins.mX(), ins.mY()) != null) {
				
				double v = (1.0-0.75*ins.getDegrade());
				Booster.hover(b, v, 0, UI.icons().s.degrade, true, Dic.¤¤Degrade);
				Booster.hoverSpan(b, 0.25, 1.0);
				mul *= v;
				b.NL();
			}
			
		
			b.NL();
			if (rate != null) {
				for (RoomBoost bo : rate.boosts()) {
					double bb = bo.get(ins);
					mul *= bb;
					Booster.hover(b, bb, 0, UI.icons().s.chevron(DIR.W), true, bo.info().name);
					Booster.hoverSpan(b, bo.min(), bo.max());
					b.NL();
				}
			}
			b.NL();
			int vi = 0;
			
			for (Booster bb : bonus.all()) {
				if (bb.isMul) {
					Booster.hover(b, values[vi], 0, bb.info.icon, true, bb.info.name);
					Booster.hoverSpan(b, bb.from(), bb.to());
					b.NL();
				}
				
				vi++;
				
			}
			
			b.NL(4);
			b.tab(6);
			b.add(GFORMAT.f1(b.text(), mul));
			
			b.NL(8);

		}

		{
			b.textLL(Dic.¤¤Addative);
			b.NL();
			
			int vi = 0;
			
			for (Booster bb : bonus.all()) {
				if (!bb.isMul) {
					Booster.hover(b, values[vi], 0, bb.info.icon, false, bb.info.name);
					Booster.hoverSpan(b, bb.from(), bb.to());
					b.NL();
				}
				vi++;
				
			}
			
			
			b.NL(4);
			
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), add));

			
			
		}
		

		b.NL(16);
		b.textLL(Dic.¤¤Total);
		b.tab(6);
		GText t = b.text();
		if (baseRate*totEfficiency != 1) {
			GFORMAT.f1(t, baseRate*totEfficiency);
			t.s().add('*').s();
		}
		
		GFORMAT.f1(t, mul);
		t.s().add('*').s();
		t.add('(').add('1').s().add('+').s();
		GFORMAT.f1(t, add).add(')').s().add('=').s();
		double tott = calcProductionRate(baseRate, rate, bonus, ins);
		GFORMAT.f0(t, tott, baseRate);
		b.add(t);
				
	}
	
	public static void hoverConsumptionRate(GUI_BOX text, double baseRate, IndustryRate rate, RoomInstance ins, RESOURCE res) {
		GBox b = (GBox) text;

		b.NL(4);
		b.textL(Dic.¤¤Base);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), -baseRate));
		b.NL();
		
		double rr = calcConsumptionRate(1, rate, ins, res);
		
		b.NL(4);
		b.textL(Dic.¤¤Efficiency);
		b.tab(6);
		GText t = b.text();
		t.add('*');
		b.add(GFORMAT.f1(t, rr));
		b.NL();
		
		b.NL();
		b.textL(Dic.¤¤Employees);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), ins.employees().employed()));
		b.NL();
		
		b.NL();
		b.textL(Dic.¤¤Boosts);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), 1.0/res.conBoost(POP_CL.clP())));
		b.NL();
		
		b.NL();
		b.textLL(Dic.¤¤Total);
		b.tab(6);
		b.add(b.text().add('(').add(baseRate).s().add('*').s().add(rr).s().add('*').s().add(ins.employees().employed()).add(')').s().add('/').s().add(res.conBoost(POP_CL.clP())).s().add('=').s().add(ins.employees().employed()*baseRate*rr/res.conBoost(POP_CL.clP())));
		b.NL();
		
//		b.tab(6);
//		b.add(GFORMAT.fRel(b.text(), baseRate*ins.employees().employed()*ins.employees().efficiency(), baseRate*ins.employees().employed()));
		b.NL(8);
		
	}
	
	public static void save(FilePutter p, LIST<Industry> ins) {
		p.i(ins.size());
		for (Industry i : ins)
			i.save(p);
	}
	
	public static void load(FileGetter p, LIST<Industry> ins) throws IOException {
		int am = p.i();
		if (ins.size() != am) {
			for (int i = 0; i < am; i++)
				ins.get(0).load(p);
		}else {
			for (Industry i : ins)
				i.load(p);
		}
	}
	
	public static void clear(LIST<Industry> ins) {
		for (Industry i : ins)
			i.clear();
	}
	
}
