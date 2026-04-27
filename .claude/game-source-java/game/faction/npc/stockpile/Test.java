package game.faction.npc.stockpile;

import java.util.Arrays;

import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LinkedList;

class Test {

	ArrayListGrower<RES> ress = new ArrayListGrower<>();
	ArrayListGrower<Producer> plays = new ArrayListGrower<>();
	
	final double delta = 0.1;
	Test(){
		
		int ri = 0;
		final RES LS = ress.addReturn(new RES("LS", ri++, 1.0));
		final RES LE = ress.addReturn(new RES("LE", ri++, 1.0));
		final RES ME = ress.addReturn(new RES("ME", ri++, 1.0));
		final RES EG = ress.addReturn(new RES("EG", ri++, 1.0));
		final RES CO = ress.addReturn(new RES("CO", ri++, 1.0));

		double ls = 0.0625;
		
		plays.addReturn(new Producer("ENT", 1).add(ME, 1, 1).add(LS, ls, ls*10));
		plays.addReturn(new Producer("BAL", 1).add(ME, 0.5, 0.5).add(LS, 0.5*ls, 0.5*ls*10));
		plays.addReturn(new Producer("AUR", 2).add(ME, 0.5, 1).add(LE, 0.5, 1).add(LS, ls, ls*10));
		plays.addReturn(new Producer("ONX", 2).add(ME, 0.5, 1).add(CO, 3, 6).add(LS, ls, ls*10));
		plays.addReturn(new Producer("GLO", 1).add(ME, 0.25, 0.5).add(EG, 0.75, 1).add(LS, ls, ls*10));
		plays.addReturn(new Producer("FAR", 1).add(CO, 6, 6));
		
		
		double before[] = new double[ress.size()];
		
		for (Producer p : plays) {
			System.out.println(p.name);
			for (RES r : p.resses) {
				System.out.println("-" + r.name + ", rate: " + p.rate(r) + " (" + p.rateAI(r) + ")");
			}
		}
		
		System.out.println();
		Arrays.fill(before, 0);
		for (Producer p : plays) {
			
			for (RES r : p.resses) {
				if (before[r.index] < p.rateAI(r))
					before[r.index] = p.rateAI(r);
				
			}
			
			
		}
		for (RES d : ress) {
			System.out.println(d.name + " (" + before[d.index] + ") " + d.mr*200/before[d.index]);
		}
		
		for (Producer p : plays) {
			System.out.println(p.name);
			double tot = 0;
			for (RES r : p.resses) {
				double pr = r.mr*p.rate(r)*200/before[r.index];
				System.out.println("  -" + r.name + " " + pr + " " + p.rate(r));
				tot += pr;
			}
			System.out.println("= " + tot);
		}
		
		
	}

	public static void main(String[] args) {
		new Test();
	}
	
	private class RES {
		
		public final int index;
		public final LinkedList<Producer> producers = new LinkedList<>();
		String name;
		public final double mr;
		RES(String name, int index, double mr){
			this.name = name;
			this.index = index;
			this.mr = mr;
		}
		
	}
	
	private class Producer {
		
		public double[] rates = new double[ress.size()];
		public double[] ratesAI = new double[ress.size()];
		public LinkedList<RES> resses = new LinkedList<>();

		String name;
		
		Producer(String name, double W){
			this.name = name;
		}
		
		public Producer add(RES res, double rate, double rateAI) {
			if (resses.contains(res))
				throw new RuntimeException();
			rates[res.index] = rate;
			ratesAI[res.index] = rateAI;
			resses.add(res);
			res.producers.add(this);
			return this;
		}
		
		double rate(RES res) {
			return rates[res.index];
		}
		
		double rateAI(RES res) {
			return ratesAI[res.index];
		}
		
		
	}

	
}
