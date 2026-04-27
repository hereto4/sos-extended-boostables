package game.time;

import snake2d.util.file.Json;
import snake2d.util.misc.Numbers;
import util.text.DicTime;

public abstract class TIMECYCLE{

	private final int bits;
	private final double secondsPerbit;
	private final double secondsPerCycle;
	private double partOfBit;
	private double partIfBitCircular;
	private final CharSequence postFix;
	private final CharSequence names;
	private int bitsSinceStart;
	private int bitsOfCycle;
	private double bitsOfDay;
	private double bitsOfSeason;
	private double bitsOfYear;
	private double secondOfBit;
	
	
	TIMECYCLE(int seconds, int amount, CharSequence postFix, CharSequence names) {
		secondsPerbit = seconds;
		this.bits = amount;
		
		secondsPerCycle = secondsPerbit*amount;
		this.postFix = postFix;
		this.names = names;
	}
	
	void update(double currentSecond) {
		bitsSinceStart = (int) (currentSecond/secondsPerbit);
		secondOfBit = currentSecond%secondsPerbit;
		bitsOfCycle = bitsSinceStart % bits;
		
		partOfBit = (currentSecond%secondsPerbit)/secondsPerbit;
		if (partOfBit <= 0.5)
			partIfBitCircular = partOfBit*2.0;
		else
			partIfBitCircular = 1.0 - (partOfBit-0.5)*2;
		
		bitsOfDay = (currentSecond%TIME.days().bitSeconds())/secondsPerbit;
		bitsOfSeason = (currentSecond%TIME.seasons().bitSeconds())/secondsPerbit;
		bitsOfYear = (currentSecond%TIME.years().bitSeconds())/secondsPerbit;
	}
	
	/**
	 * 
	 * @return part of the progression of the current bit. 0->1 
	 */
	public final double bitPartOf() {
		return partOfBit;
	}
	
	/**
	 * 
	 * @return circular part of progression of current bit 0->1->0
	 */
	public final double bitPartOfC() {
		return partIfBitCircular;
	}
	
	/**
	 * 
	 * @return the length of a bit in seconds
	 */
	public double bitSeconds() {
		return secondsPerbit;
	}
	
	public double secondOfBit() {
		return secondOfBit;
	}
	
	/**
	 * 
	 * @return amount o bits in a cycle
	 */
	public int bitsPerCycle() {
		return bits;
	}
	
	public double bitConversion(TIMECYCLE toBits) {
		return secondsPerbit / toBits.secondsPerbit;
	}
	
	/**
	 * 
	 * @return nr of bits that have passed since year 0, age 0
	 */
	public int bitsSinceStart() {
		return bitsSinceStart;
	}
	
	/**
	 * 
	 * @return the index of the current bit in this cycle
	 */
	public int bitCurrent() {
		return bitsOfCycle;
	}

	public String bitNameCurrent() {
		return bitName(bitsOfCycle);
	}
	
	public double bitOfDay() {
		return bitsOfDay;
	}
	
	public double bitOfSeason() {
		return bitsOfSeason;
	}
	
	public double bitOfYear() {
		return bitsOfYear;
	}
	
	public abstract String bitName(int bit);
	
	
	public CharSequence cycleName() {
		return postFix;
	}
	
	public CharSequence cycleNames() {
		return names;
	}
	
	
	
	/**
	 * 
	 * @return total seconds per cycle
	 */
	public double cycleSeconds() {
		return secondsPerCycle;
	}
	
	public static final class Hours extends TIMECYCLE{
		
		private final String[] names;
		
		Hours(int seconds, int amount) {
			super(seconds, amount, "hour", "hours");
			names = new String[amount];
			for (int i = 0; i < amount; i++)
				names[i] = Numbers.getSuffix(i+1);
		}

		@Override
		public String bitName(int bit) {
			return names[bit];
		}
		
	}
	
	public static final class Days extends TIMECYCLE{
		
		private final String[] names;
		public final double dayShiftStart = 0.25;
		public final double dayShiftEnd = 0.75;
		private boolean dayShift;
		private double partOfShift;
		
		Days(int seconds, int amount) {
			super(seconds, amount, DicTime.¤¤Day, DicTime.¤¤Days);
			names = new String[amount];
			for (int i = 0; i < amount; i++)
				names[i] = Numbers.getSuffix(i+1);
		}
		
		@Override
		protected void update(double currentSecond) {
			super.update(currentSecond);
			dayShift = bitPartOf() >= dayShiftStart && bitPartOf() < dayShiftEnd;
			if (dayShift) {
				partOfShift = (bitPartOf() - dayShiftStart)*2.0;
			}else if(bitPartOf() < dayShiftStart){
				partOfShift = 0.5 + bitPartOf()*2.0;
			}else {
				partOfShift = (bitPartOf()-dayShiftEnd)*2.0;
			}
		}
		
		public boolean dayShift() {
			return dayShift;
		}
		
		public boolean isNightShift() {
			return !dayShift;
		}
		
		public double shiftPartOf() {
			return partOfShift;
		}

		@Override
		public String bitName(int bit) {
			return names[bit];
		}
		
	}
	
	public static final class Years extends TIMECYCLE{
		
		private final String[] names;
		
		Years(int seconds, int amount) {
			super(seconds, amount, DicTime.¤¤Year, DicTime.¤¤Years);
			names = new String[amount];
			for (int i = 0; i < amount; i++)
				names[i] = Numbers.getSuffix(i+1);
		}

		@Override
		public String bitName(int bit) {
			return names[bit];
		}
		
	}
	
	public static final class Ages extends TIMECYCLE{
		
		private final String[] names;

		public Ages(int seconds, Json jData, Json jText) {
			super(seconds, jData.i("AGES"), DicTime.¤¤Age, DicTime.¤¤Ages);
			names = jText.texts("AGES", jData.i("AGES", 1, 1000), 1000);
		}

		@Override
		public String bitName(int bit) {
			return names[bit];
		}
	}
	
}
