package settlement.entity;

import game.boosting.BOOSTABLES;
import init.type.CAUSE_LEAVE;
import snake2d.LOG;

public class ECollision {
	public ENTITY other;
	public CAUSE_LEAVE leave;
	public double damagetileStrength;
	public double[] damage = new double[BOOSTABLES.BATTLE().DAMAGES.size()];
	public double tileMomentum;
	/**
	 * The x direction from other entity to you.
	 */
	public double norX;
	/**
	 * The y direction from other entity to you.
	 */
	public double norY;
	/**
	 * the dot product 0-1 of the direction of collision and your speeds direction before the collision
	 */
	public double dirDot;
	public double dirDotOther;
	public boolean speedHasChanged;

	public void debug() {
		LOG.ln("other " + other);
		LOG.ln("leave " + leave);
		LOG.ln("mom " + tileMomentum);
		LOG.ln("strength " + damagetileStrength);
		for (int i = 0; i < damage.length; i++) {
			LOG.ln(BOOSTABLES.BATTLE().DAMAGES.get(i).key  + " " + damage[i]);
			
		}
		LOG.ln("dir " + dirDot);
	}
	
}