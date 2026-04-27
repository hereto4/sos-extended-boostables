package settlement.entity.humanoid;

import settlement.entity.ECollision;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.ai.main.AIManager;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum HPoll {
	
	DEFENCE_SKILL,
	PARRY_SKILL,
	WILL_COLLIDE_WITH,
	COLLIDES,
	SCARE_ANIMAL_NOT,
	BATTLE_READY,
	IMPACT_DAMAGE,
	WORKING,
	IS_ENEMY,
	IS_SLAVE_READY_FOR_UPRISING,
	CAN_INTERRACT,
	;
	
	public static final LIST<HPoll> all = new ArrayList<HPoll>(values());
	
	private static HPollData poll = new HPollData();
	public static final class HPollData {
		
		public HPoll type;
		public double adx;
		public double ady;
		public double facingDot;
		public ENTITY other;
		public ECollision colli;
		public ECollision damage;
		public boolean isEnemy;
		
		private HPollData() {
			
		}
	}
	
	public static final class Handler {
		
		private Handler() {
			
		}
		
		public static boolean scaresAnimal(Humanoid a) {
			poll.type = SCARE_ANIMAL_NOT;
			return a.ai.poll(a, poll) == 0;
		}
		
		public static void collideDamage(Humanoid a, AIManager ai, ECollision coll, ECollision damage) {
			boolean isEnemy = isEnemy(a, coll.other);
			poll.type = IMPACT_DAMAGE;
			poll.colli = coll;
			poll.damage = damage;
			poll.isEnemy = isEnemy;
			
			a.ai.poll(a, poll);
		}

		static boolean collides(Humanoid a, AIManager ai, ENTITY o) {
			poll.other = o;
			poll.type = COLLIDES;
			
			return ai.poll(a, poll) == 1;
		}
		
		static boolean willCollideWith(Humanoid a, AIManager ai, ENTITY other) {
			poll.type = WILL_COLLIDE_WITH;
			poll.other = other;
			
			return ai.poll(a, poll) == 1;
		}
		
		public static double defenseSkill(Humanoid a, double faceDot, double adx, double ady) {
			poll.facingDot = faceDot;
			poll.adx = adx;
			poll.ady = ady;
			poll.type = DEFENCE_SKILL;
			return a.ai.poll(a, poll);
		}
		
		public static double parrySkill(Humanoid a, double dot, double adx, double ady) {
			poll.facingDot = dot;
			poll.adx = adx;
			poll.ady = ady;
			poll.type = PARRY_SKILL;
			return a.ai.poll(a, poll);
		}
		
		public static boolean works(Humanoid a) {
			poll.type = WORKING;
			return a.ai.poll(a, poll) == 1;
		}
		
		public static int isSlaveReadyForUprising(Humanoid a) {
			poll.type = IS_SLAVE_READY_FOR_UPRISING;
			return (int)a.ai.poll(a, poll);
		}
		
		public static boolean isEnemy(Humanoid a, ENTITY other) {
			poll.type = IS_ENEMY;
			poll.other = other;
			return a.ai.poll(a, poll) == 1;
		}
		
		public static boolean canInterract(Humanoid a, ENTITY other) {
			poll.type = CAN_INTERRACT;
			poll.other = other;
			return a.ai.poll(a, poll) == 1;
		}
		
		
	}
	

	
}
