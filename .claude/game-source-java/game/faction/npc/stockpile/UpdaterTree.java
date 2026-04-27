package game.faction.npc.stockpile;

import java.util.Arrays;
import java.util.LinkedList;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.industry.module.FlatIndustries.FlatIndustry;
import settlement.room.industry.module.IndustryResource;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;

final class UpdaterTree {

	private final TreeRes[] allO = new TreeRes[RESOURCES.ALL().size()];
	private final ArrayListGrower<ResIns> allIns = new ArrayListGrower<>();
	private double[] bo = new double[SETT.ROOMS().industries.flat.all().size()];

	public UpdaterTree() {

		for (RESOURCE res : RESOURCES.ALL()) {
			allO[res.index()] = new TreeRes(res);

		}

		for (int fi = 0; fi < SETT.ROOMS().industries.flat.all().size(); fi++) {
			FlatIndustry ins = SETT.ROOMS().industries.flat.all().get(fi);

			for (IndustryResource o : ins.industry.outs()) {
				ResIns p = new ResIns(o, ins, allO, o.AI, o.AIRate);
				allO[o.resource.index()].producers.add(p);

			}

		}

		for (TreeRes o : allO) {
			for (ResIns ii : o.producers) {
				allIns.add(ii);
			}
		}

		for (ResIns ii : allIns) {
			for (TreeRes o : ii.inputs) {
				o.consumers.add(new ResOut(ii, o.res));
			}
		}

		GuiSection prices = new Prices();

		IDebugPanel.add("prices", new ACTION() {

			@Override
			public void exe() {
				VIEW.inters().section.activate(prices);
			}
		});

		// print();

	}

	public void update(FactionNPC f) {
		for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

			double b = 1;
			if (ins.industry.reg() != null) {
				b = 0;
				for (int i = 0; i < f.realm().regions(); i++) {
					Region reg = f.realm().region(i);
					b += RD.PROSPECT().getAi(ins.industry, reg);
				}
				b /= f.realm().regions();
			}
			bo[ins.index] = b * ins.industry.aiBonus(f, ins);
			
			

		}
		
		for (ResIns ii : allIns) {
			ii.clear();
			ii.prodSpeedBonus = 1.0 / (ii.rate * bo[ii.ins.index]);
		}

		for (ResIns ii : allIns) {
			ii.prodSpeedTot = getManhours(f, ii);

		}

	}

	private double getManhours(FactionNPC faction, ResIns o) {

		if (o.inputs.size() == 0)
			return o.prodSpeedBonus;

		double d = o.prodSpeedBonus;

		for (int oi = 0; oi < o.inputs.size(); oi++) {
			
			double inRate = o.ins.industry.ins().get(oi).rate / o.ins.industry.ins().get(oi).resource.conBoost(faction);
			double outRate = o.rate;

			double am = inRate / outRate;

			d += am * getManhours(faction, o.inputs.get(oi));
		}

		return d;
	}

	private double getManhours(FactionNPC faction, TreeRes o) {

		double m = Double.MAX_VALUE;
		double amMul = faction.stockpile.res(o.res.index()).amMul();
		for (int i = 0; i < o.producers.size(); i++) {
			ResIns in = o.producers.get(i);
			m = Math.min(m, getManhours(faction, in)*amMul);
		}
		
		return m;

	}

//	private void print() {
//		for (Res o : allO) {
//			printBack(o, 0);
//		}
//		System.out.println();
//		for (Res o : allO) {
//			printForward(o, 0);
//		}
//	}
//	
//	private void printBack(Res o, int s) {
//		for (int i = 0; i < s; i++)
//			System.out.print(" ");
//		System.out.println(o.res);
//		for (int ii = 0; ii < o.producers.size(); ii++) {
//			for (int i = 0; i < s; i++)
//				System.out.print(" ");
//			System.out.print(" - ");
//			ResIns i = o.producers.get(ii);
//			System.out.print(i.ins.blue.key);
//			System.out.println();
//			for (int iii = 0; iii < i.inputs.size(); iii++) {
//				printBack(i.inputs.get(iii), s+2);
//			}
//		}
//	}
//	
//	private void printForward(Res o, int s) {
//		for (int i = 0; i < s; i++)
//			System.out.print(" ");
//		System.out.println(o.res);
//		for (int ii = 0; ii < o.consumers.size(); ii++) {
//			for (int i = 0; i < s; i++)
//				System.out.print(" ");
//			System.out.print(" - ");
//			ResOut i = o.consumers.get(ii);
//			System.out.print(i.ins.ins.blue.key);
//			System.out.println();
//			printForward(o(i.ins.out.resource), s+2);
//		}
//	}

	public TreeRes o(RESOURCE res) {
		return allO[res.index()];
	}

	public TreeRes o(int resI) {
		return allO[resI];
	}

	public static class TreeRes {
		public final RESOURCE res;
		public final ArrayListGrower<ResIns> producers = new ArrayListGrower<>();
		public final ArrayListGrower<ResOut> consumers = new ArrayListGrower<>();

		TreeRes(RESOURCE res) {
			this.res = res;
		}

		private void debug(Str s, int inc) {
			s.add(LOG.WS(3 * inc) + res).NL();
			for (int i = 0; i < producers.size(); i++) {
				ResIns ins = producers.get(i);
				ins.debug(s, inc + 1);
			}
		}

		private boolean check(TreeRes o) {
			if (o == this)
				return false;
			for (int i = 0; i < producers.size(); i++) {
				if (!producers.get(i).check(o))
					return false;
			}
			return true;

		}

	}

	public static class ResIns {

		public final IndustryResource out;
		public final FlatIndustry ins;
		public final ArrayList<TreeRes> inputs;

		public final double rate;
		public final double rateSpeed;
		public double rateTot;
		public double prodSpeedBonus;
		public double prodSpeedTot;

		ResIns(IndustryResource out, FlatIndustry ins, TreeRes[] all, double rate, double rateSpeed) {
			this.ins = ins;
			this.out = out;
			inputs = new ArrayList<TreeRes>(ins.industry.ins().size());
			for (IndustryResource r : ins.industry.ins()) {
				inputs.add(all[r.resource.index()]);
			}

			this.rate = rate;
			this.rateSpeed = rateSpeed;
			clear();
		}

		private void clear() {
			prodSpeedBonus = 1.0 / rate;
			prodSpeedTot = 1.0 / rate;
		}

		private boolean check(TreeRes o) {
			for (int i = 0; i < inputs.size(); i++)
				if (!inputs.get(i).check(o))
					return false;
			return true;
		}

		private void debug(Str s, int inc) {
			s.add(LOG.WS(3 * inc)).add(ins.blue.toString()).NL();
			for (int i = 0; i < inputs.size(); i++) {
				TreeRes ins = inputs.get(i);
				ins.debug(s, inc + 1);
			}
		}

	}

	public static class ResOut {

		public final ResIns ins;
		public final double part;
		public final double am;

		ResOut(ResIns ins, RESOURCE inres) {
			IndustryResource in = null;
			this.ins = ins;
			double tot = 0;
			for (IndustryResource ii : ins.ins.industry.ins()) {
				tot += ii.rate;
				if (ii.resource == inres)
					in = ii;
			}
			am = in.rate;
			part = in.rate / tot;
		}

	}

	class Prices extends GuiSection {

		private double[] bo = new double[SETT.ROOMS().industries.flat.all().size()];
		private double[] bo2 = new double[SETT.ROOMS().industries.flat.all().size()];
		private double[] boMi = new double[SETT.ROOMS().industries.flat.all().size()];
		private double[] boMa = new double[SETT.ROOMS().industries.flat.all().size()];

		Prices() {

			for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

				bo[ins.index] = 1.0;
			}

			for (ResIns ii : allIns) {
				ii.clear();
				ii.prodSpeedBonus = 1.0 / (ii.rate * bo[ii.ins.index]);
			}

			for (ResIns ii : allIns) {
				ii.prodSpeedTot = getManhoursV(ii);

			}

			LinkedList<RENDEROBJ> rows = new LinkedList<>();

			for (TreeRes o : allO) {

				for (ResIns ii : o.producers) {
					GuiSection s = new GuiSection() {

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;
							b.title(ii.ins.name);
							for (IndustryResource i : ii.ins.industry.ins()) {
								b.add(i.resource.icon());
								b.add(b.text().add(i.rate));
							}
							b.add(UI.icons().s.arrow_right);
							b.add(ii.out.resource.icon());
							b.add(b.text().add(ii.out.rate));
							b.NL();
							b.add(b.text().add(SETT.ROOMS().industries.vanillaRate(ii.out.resource)));
							b.NL();

						}

					};
					rows.add(s);
					final double pr = ii.prodSpeedTot;
					s.add(ii.ins.icon, 0, 0);
					s.add(ii.out.resource.icon(), 24, 0);
					int x1 = 32;
					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f(text, ii.prodSpeedTot);
						}

					}, x1 += 64);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f1(text, bo[ii.ins.index]);
						}
					}, x1 += 64);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f1(text, boMi[ii.ins.index]);
							text.add('-');
							GFORMAT.f1(text, boMa[ii.ins.index]);
							text.normalify();
						}
					}, x1 += 64);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f1(text, bo2[ii.ins.index]);
						}
					}, x1 += 120);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f1(text, bo[ii.ins.index] * bo2[ii.ins.index]);
						}
					}, x1 += 64);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.i(text,
									(long) (FACTIONS.PRICE().get(ii.out.resource) / ii.out.resource.priceMulDef));
						}
					}, x1 += 64);

					s.addCentredY(new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.i(text, (long) (FACTIONS.PRICE().get(ii.out.resource)
									/ (pr * ii.out.resource.priceMulDef)));
						}
					}, x1 += 80);

					s.body().setWidth(x1 + 100);

				}
			}

			add(new GScrollRows(rows, C.HEIGHT() - 64).view());
			body().centerIn(C.DIM());
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			Arrays.fill(bo, 0);
			Arrays.fill(bo2, 0);
			Arrays.fill(boMa, 0);
			Arrays.fill(boMi, Double.MAX_VALUE);
			for (FactionNPC f : FACTIONS.NPCs()) {
				for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

					double b = 0;
					for (int i = 0; i < f.realm().regions(); i++) {
						Region reg = f.realm().region(i);
						b += RD.PROSPECT().getAi(ins.industry, reg);
					}
					b /= f.realm().regions();
					boMa[ins.index] = Math.max(b, boMa[ins.index]);
					boMi[ins.index] = Math.min(b, boMi[ins.index]);
					bo[ins.index] += b;
					bo2[ins.index] += ins.industry.aiBonus(f, ins);
				}
			}

			for (FlatIndustry ins : SETT.ROOMS().industries.flat.all()) {

				bo[ins.index] /= FACTIONS.NPCs().size();
				bo2[ins.index] /= FACTIONS.NPCs().size();

			}

			COLOR.BLACK.render(r, body(), 16);
			super.render(r, ds);
		}

		private double getManhoursV(ResIns o) {

			if (o.inputs.size() == 0)
				return o.prodSpeedBonus;

			double d = o.prodSpeedBonus;

			for (int oi = 0; oi < o.inputs.size(); oi++) {
				double inRate = o.ins.industry.ins().get(oi).rate;
				double outRate = o.rate;
				double am = inRate / outRate;
				
				d += am * getManhoursV(o.inputs.get(oi));
			}

			return d;
		}

		private double getManhoursV(TreeRes o) {

			double m = Double.MAX_VALUE;

			for (ResIns in : o.producers) {
				m = Math.min(m, getManhoursV(in));
			}

			return m;

		}

	}

}
