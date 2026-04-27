package init.type;

import settlement.main.SETT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.MAPPED;
import util.text.D;
import util.text.Dic;

public final class DISEASE implements MAPPED{

	private final int index;
	public final INFO info;
	public final String key;
	
	public final double infectRate;
	public final double incubationDays;
	public final double fatalityRate;
	private final double[] coccurence = new double[CLIMATES.ALL().size()];
	private final double[] toccurence = new double[TERRAINS.ALL().size()];
	public final int length;
	public final COLOR color;
	public final boolean epidemic;
	public final boolean regular;
	
	private static CharSequence ¤¤Spread = "Spread";
	private static CharSequence ¤¤Occurrence = "Occurrence";
	private static CharSequence ¤¤Incubation = "Incubation Days";
	private static CharSequence ¤¤Length = "Infection Days";
	private static CharSequence ¤¤Lethality = "lethality";
	
	static {
		D.ts(DISEASE.class);
	}

	
	DISEASE(LISTE<DISEASE> all, String key, Json data, Json text) {
		index = all.add(this);
		this.key = key;
		info = new INFO(text);
		
		infectRate = data.d("SPREAD", 0, 1);
		incubationDays = data.i("INCUBATION_DAYS", 1, 100);
		fatalityRate = data.d("FATALITY_RATE", 0, 1.0);
		CLIMATES.MAP().readFill("OCCURRENCE_CLIMATE", coccurence, data, 0.00000, 100000);
		TERRAINS.MAP().readFill("OCCURRENCE_TERRAIN", toccurence, data, 0.00000, 100000);
		length = data.i("INFECTION_DAYS", 1, 100);
		epidemic = data.bool("EPIDEMIC");
		regular = data.bool("REGULAR");
		color = new ColorImp(data).shade(2.0);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return key;
	}
	
	
	public void hover(GUI_BOX text) {
		GBox b = (GBox) text;
		info.hover(b);
		

		{
			double occ = 0;
			
			b.NL();
			b.textLL(¤¤Occurrence);
			b.NL();
			int tt = 0;
			
			for (TERRAIN t : TERRAINS.ALL()) {
				occ += toccurence[t.index()]*SETT.WORLD_AREA().info.get(t).getD();
				if (tt > 6) {
					tt = 0;
					b.NL();
				}
				b.add(t.icon());
				b.add(GFORMAT.f0(b.text(), toccurence[t.index()]));
			}
			b.NL();
			CLIMATE climate = SETT.ENV().climate();
			occ *= coccurence[climate.index()];
			b.textLL(CLIMATES.INFO().name);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), coccurence[climate.index()]));
			b.NL();
			b.textSLL(Dic.¤¤Total);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), occ));
			
			b.sep();
			
		}

		
		b.NL();
		b.textLL(¤¤Spread);
		b.tab(6);
		b.add(GFORMAT.percInv(b.text(), infectRate));
		b.NL();
		
		b.textLL(¤¤Incubation);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), incubationDays, 1));
		b.NL();
		
		b.textLL(¤¤Length);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), length, 1));
		b.NL();
		
		b.NL(8);
		b.textLL(¤¤Lethality);
		b.tab(6);
		b.add(GFORMAT.percInv(b.text(), fatalityRate));
		
		b.sep();
		
	}
	
	public double occurence() {
		double occ = 0;
		
		for (TERRAIN t : TERRAINS.ALL()) {
			occ = Math.max(occ, toccurence[t.index()]*SETT.WORLD_AREA().info.get(t).getD());
		}
		CLIMATE climate = SETT.ENV().climate();
		occ *= coccurence[climate.index()];
		return occ;
	}

	@Override
	public String key() {
		return key;
	}

}
