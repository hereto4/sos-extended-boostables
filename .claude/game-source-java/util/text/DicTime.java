package util.text;

import game.time.TIME;
import snake2d.util.sprite.text.Str;

public class DicTime {


	public static CharSequence ¤¤Day = "¤Day";
	public static CharSequence ¤¤Days = "¤Days";
	public static CharSequence ¤¤Season = "¤Season";
	public static CharSequence ¤¤Seasons = "¤Seasons";
	public static CharSequence ¤¤Year = "¤Year";
	public static CharSequence ¤¤Years = "¤Years";
	public static CharSequence ¤¤Age = "¤Age";
	public static CharSequence ¤¤Ages = "¤Ages";
	public static CharSequence ¤¤Hour = "¤Hours";
	public static CharSequence ¤¤Hours = "¤Hours";
	public static CharSequence ¤¤Minute = "¤Minute";
	public static CharSequence ¤¤Minutes = "¤Minutes";
	public static CharSequence ¤¤Second = "¤Second";
	public static CharSequence ¤¤Seconds = "¤Seconds";
	public static CharSequence ¤¤Today = "¤Today";
	private static CharSequence ¤¤now = "¤now";
	
	private static CharSequence ¤¤1OfSomething = "¤1 {0}";
	private static CharSequence ¤¤MoreOfSomething = "¤{0} {1}";

	private static CharSequence ¤¤dateFormat = "¤Day {0} of {1}, Year {2} of the {3}";
	private static CharSequence ¤¤dateFormatShort = "¤{0}/{1} - {2} {3}";
	
	private static CharSequence ¤¤2SomethingAgo = "{0}, {1} ago";
	private static CharSequence ¤¤1SomethingAgo = "{0} ago";
	
	private static CharSequence ¤¤yearsDays = "{0} years, {1} days";
	
	private static final Str f = new Str(32);
	private static final Str f2 = new Str(32);
	
	static {
		D.ts(DicTime.class);
	}
	
	public static Str setDate(Str text, int second) {
		
		int age = (second % (int)TIME.age().cycleSeconds()) / (int)TIME.age().bitSeconds();
		int year = (second % (int)TIME.years().cycleSeconds())  / (int)TIME.years().bitSeconds();
		int season = (second % (int)TIME.seasons().cycleSeconds())  / (int)TIME.seasons().bitSeconds();
		int day = (second % (int)TIME.days().cycleSeconds())  / (int)TIME.days().bitSeconds();
		text.clear().add(¤¤dateFormat);
		text.insert(0, day+1);
		text.insert(1, TIME.seasons().bitName(season));
		text.insert(2, year+1);
		text.insert(3, TIME.age().bitName(age));
		return text;
	}
	
	public static Str setDateShort(Str text, int second) {
		
		int age = (second % (int)TIME.age().cycleSeconds()) / (int)TIME.age().bitSeconds();
		int year = (second % (int)TIME.years().cycleSeconds())  / (int)TIME.years().bitSeconds();
		int season = (second % (int)TIME.seasons().cycleSeconds())  / (int)TIME.seasons().bitSeconds();
		int day = (second % (int)TIME.days().cycleSeconds())  / (int)TIME.days().bitSeconds();
		text.clear().add(¤¤dateFormatShort);
		text.insert(0, day+1);
		text.insert(1, TIME.seasons().bitName(season));
		text.insert(2, year+1);
		CharSequence aa = TIME.age().bitName(age);
		f.clear();
		f.add(Character.toUpperCase(aa.charAt(0)));
		f.add('.');
		for (int i = 1; i < aa.length(); i++) {
			if (aa.charAt(i-1) == ' ')
				f.add(Character.toUpperCase(aa.charAt(i))).add('.');
		}
		
		text.insert(3, f);
		return text;
	}
	
	public static void setTime(Str text, double second) {
		
	
		
		
		double s = second % TIME.secondsPerDay();
		int h = (int) (s / TIME.secondsPerHour());
		s = s % TIME.secondsPerHour();
		
		s = (int) (60.0*s/TIME.secondsPerHour());
		if (h < 10)
			text.add('0');
		text.add(h);
		text.add(':');
		if (s < 10)
			text.add('0');
		text.add((int)s);
	}
	

	
	
	public static Str setAgo(Str text, double seconds) {
		return setYearDay(text, seconds, ¤¤2SomethingAgo);
	}
	
	public static Str setSpanDays(Str text, double from, double to) {
		text.clear();
		int day = (int) (from / (int)TIME.days().bitSeconds());
		text.add(day).s().add('-').s();
		day = (int) (to / (int)TIME.days().bitSeconds());
		text.add(day).s();
		text.add(¤¤1SomethingAgo);
		text.insert(0, ¤¤Days);
		return text;
	}
	
	private static Str setYearDay(Str text, double seconds, CharSequence F) {
		
		int secondAgo = (int) seconds;
		int year = (secondAgo / (int)TIME.years().bitSeconds());
		secondAgo -= year*TIME.years().bitSeconds();
		int day = (secondAgo / (int)TIME.days().bitSeconds());
		
		if (year == 0 && day == 0) {
			text.clear().add(¤¤now);
			return text;
		}
			
		text.clear().add(F);
		text.insert(0, setDays(f.clear(), day));
		text.insert(1, setYears(f.clear(), year));
		return text;
	}
	
	public static Str setYearDay(Str text, double seconds) {
		int secondAgo = (int) seconds;
		int year = (secondAgo / (int)TIME.years().bitSeconds());
		secondAgo -= year*TIME.years().bitSeconds();
		int day = (secondAgo / (int)TIME.days().bitSeconds());
		text.clear().add(¤¤yearsDays);
		text.insert(0, year);
		text.insert(1, day);
		return text;
	}
	
	public static Str setYears(Str text, double years) {
		format(text, years, ¤¤Year, ¤¤Years);
		return text;
	}
	
	public static Str setYearsAgo(Str text, double years) {
		f.clear();
		setYears(f, years);
		text.clear().add(¤¤1SomethingAgo);
		text.insert(0, f);
		return text;
	}
	
	public static Str setDaysAgo(Str text, double days) {
		f.clear();
		setDays(f, days);
		text.clear().add(¤¤1SomethingAgo);
		text.insert(0, f);
		return text;
	}
	
	public static Str setDays(Str text, double days) {
		format(text, days, ¤¤Day, ¤¤Days);
		return text;
	}
	
	public static Str setHours(Str text, double hours) {
		format(text, hours, ¤¤Hour, ¤¤Hours);
		return text;
	}
	
	public static Str setMinutes(Str text, double minutes) {
		format(text, minutes, ¤¤Minute, ¤¤Minutes);
		return text;
	}
	
	public static Str setSeconds(Str text, double minutes) {
		format(text, minutes, ¤¤Second, ¤¤Seconds);
		return text;
	}
	
	
	
	private static void format(Str text, double days, CharSequence singular, CharSequence plural) {
		f2.clear();
		if (days == 1) {
			text.add(¤¤1OfSomething).insert(0, singular);
		}else if (days == (int) days) {
			text.add(¤¤MoreOfSomething).insert(0, (int) days).insert(1, plural);
		}else {
			text.add(¤¤MoreOfSomething).insert(0, days, 1).insert(1, plural);
		}

	}
}
