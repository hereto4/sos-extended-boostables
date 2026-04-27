package game.battle.util;

import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

class BattleFightingTest {

	/**
	 * 0.3% end damage is nice
	 */
	
	
	private static double CHANCE = 100;
	private static double CHANCE_MIN = 1.0/CHANCE;
	private static double CHANCE_SPAN = CHANCE-CHANCE_MIN;

	public static void main(String[] args) {

		RND.rInt();
		
		Attack a = new Attack();
		Defence d = new Defence();

		a.offence = 1;
		a.aim = 5;
		a.damage = 1;

		d.defence = 1;
		d.parry = 1;
		d.armour = 1;
		
		d.armourDir = 1;

		perform("normal", a, d);

		test();

	}

	private static void perform(String name, Attack a, Defence d) {

		System.out.println(name);
		a.print();
		d.print();

		Count c = new Count();
		double amount = 1000000;

		for (int i = 0; i < amount; i++) {

			double hit = a.offence / ((d.defence) * (CHANCE_MIN + RND.rFloat() * CHANCE_SPAN));

			if (hit > RND.rFloat()) {
				c.hits++;
				double damage = a.damage;
				double block = a.aim / ((d.parry) * (CHANCE_MIN + RND.rFloat() * CHANCE_SPAN));

				if (block > RND.rFloat()) {
					c.bypass++;
				} else {
					damage /= (d.armourDir);
				}

				double r = damage / ((d.armour) * (CHANCE_MIN + RND.rFloat() * CHANCE_SPAN));

				if (r > RND.rFloat()) {
					c.impacts ++;
					c.damage += r;
					if (r > 1)
						c.deaths++;
				}
			}

		}

		c.print(amount);

		average(a, d);

	}

	static double getDamage(Attack a, Defence d) {

		double hit = averageOver1(a.offence, d.defence, CHANCE_MIN, CHANCE_SPAN);

		hit = CLAMP.d(hit, 0, 1);

		double unblocked = averageOver1(a.aim, d.parry, CHANCE_MIN, CHANCE_SPAN);
		unblocked = CLAMP.d(unblocked, 0, 1);

		double damageBlocked = (unblocked)*hit * averageAddative(a.damage, d.armour, CHANCE_MIN, CHANCE_SPAN);
		double damageUnblocked = (1.0 - unblocked)*hit * averageAddative(a.damage/(d.armourDir), d.armour, CHANCE_MIN, CHANCE_SPAN);
		
		double damage = damageBlocked + damageUnblocked;
		return damage;

	}
	
	private static void average(Attack a, Defence d) {

		Count c = new Count();

		double hit = averageOver1(a.offence, d.defence, CHANCE_MIN, CHANCE_SPAN);

		hit = CLAMP.d(hit, 0, 1);
		c.hits  += hit * 1000;

		double unblocked = averageOver1(a.aim, d.parry, CHANCE_MIN, CHANCE_SPAN);
		unblocked = CLAMP.d(unblocked, 0, 1);
		c.bypass += hit * 1000 * unblocked;

		double damageBlocked = (unblocked)*hit * averageAddative(a.damage, d.armour, CHANCE_MIN, CHANCE_SPAN);
		double damageUnblocked = (1.0 - unblocked)*hit * averageAddative(a.damage/(d.armourDir), d.armour, CHANCE_MIN, CHANCE_SPAN);
		
		double damage = damageBlocked + damageUnblocked;

		c.damage += 1000 * (damage);

		c.print(1000);

	}

	private static double averageAddative(double A, double B, double DAMAGE_MIN, double DAMAGE_SPAN) {
		double C = A / B;
		double m = DAMAGE_MIN;
		double s = DAMAGE_SPAN;
		double h = m + s;

		if (C <= m) {
			return C * C / (s * m * h);
		} else if (C >= h) {
			return C * Math.log(h / m) / s;
		} else {
			return (C * Math.log(C / m) + C - C * C / h) / s;
		}
	}

	private static double averageOver1(double A, double B, double DAMAGE_MIN, double DAMAGE_SPAN) {
		double C = A / B;
		double m = DAMAGE_MIN;
		double s = DAMAGE_SPAN;
		double h = m + s;
		return (C <= m) ? C * Math.log(h / m) / s : (C >= h) ? 1.0 : ((C - m) + C * Math.log(h / C)) / s;
	}

	private static void test() {

		double amount = 1000000;

		double DAMAGE_MIN = 1.0 / 100;
		double DAMAGE_SPAN = 100 - DAMAGE_MIN;

		double A = 1;
		double B = 1;

		double totA = 0;
	    for (int i = 0; i < amount; i++) {
	        double hit = A / ((B + 1) * (DAMAGE_MIN + RND.rFloat() * DAMAGE_SPAN));
	        if (hit > RND.rFloat()) totA++;
	    }
	    System.out.println("A (sim): " + totA / amount);
	    System.out.println("A (ana): " + averageOver1(A, B + 1, DAMAGE_MIN, DAMAGE_SPAN));

		double totB = 0;
		for (int i = 0; i < amount; i++) {
			double hit = A / (B * (DAMAGE_MIN + RND.rFloat() * DAMAGE_SPAN));
			if (hit > RND.rFloat())
				totB += hit;
		}
		System.out.println("B (sim): " + totB / amount);
		System.out.println("B (ana): " + averageAddative(A, B, DAMAGE_MIN, DAMAGE_SPAN));

	}

	static class Attack {

		public double offence = 1;
		public double aim = 5;
		public double damage = 1;

		public void print() {
			System.out.println("attacker");
			System.out.println("  offence   " + offence);
			System.out.println("  aim   " + aim);
			System.out.println("  damage  " + damage);
		}

	}

	static class Defence {

		public double defence = 1;
		public double parry = 1;
		public double armourDir = 1;
		public double armour = 1;

		public void print() {
			System.out.println("defender");
			System.out.println("  defence   " + defence);
			System.out.println("  parry   " + parry);
			System.out.println("  shield  " + armourDir);
			System.out.println("  armour  " + armour);
		}

	}

	private static class Count {

		public double hits = 0;
		public double bypass = 0;
		public double impacts = 0;
		public double deaths = 0;
		public double damage = 0;

		public void print(double amount) {

			System.out.println("result");
			System.out.println("  Hitrate  " + (int) (1000 * (hits / amount)) / 10.0 + "%");
			System.out.println("  Bypass   " + (int) (1000 * (bypass / hits)) / 10.0 + "%");
			System.out.println("  impacts   " + (int) (1000 * (impacts / hits)) / 10.0 + "%");
			System.out.println("     damage   " + (int) (1000 * (damage / hits)) / 10.0 + "%");
			System.out.println("     deaths   " + (int) (1000 * (deaths / hits)) / 10.0 + "%");

			System.out.println("  tot damage   " + (int) (10000 * (damage / amount)) / 100.0 + "%");
			System.out.println("  tot deaths   " + (int) (10000 * (deaths / amount)) / 100.0 + "%");
		}

	}

}
