package view.sett.ui.subject;

import init.race.bio.BioLine;
import init.sprite.UI.UI;
import init.type.HTYPES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;

final class SInfoDesc extends GuiSection{
	
	private final AInfo ss;
	private Str str = new Str(1024);
	private int lines = 0;
	private final int width = SInfo.width-48;
	
	private int[] starts = new int[128];
	private int[] ends = new int[128];
	private COLOR[] cols = new COLOR[128];
	
	private final Font font = UI.FONT().M;
	
	private final LIST<Str> impr = new ArrayList<Str>(
			new Str(64),
			new Str(64),
			new Str(64),
			new Str(64)
			);
	
	
	public SInfoDesc(AInfo ss, int height) {
		this.ss = ss;
		
		for (int i = 0; i < cols.length; i++)
			cols[i] = COLOR.WHITE100;
		GTableBuilder b = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return lines;
			}
		};
		
		int li = (height-10)/font.height();
		b.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(width, font.height()) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						int s = starts[ier.get()];
						int e = ends[ier.get()];
						cols[ier.get()].bind();
						font.render(r, str, body().x1(), body().y1(), s, e, 1.0);
						COLOR.unbind();
					}
				};
			}
		});
		
		body().setDim(16, 1);
		
		add(b.create(li, false), 16, 0);
		
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		if (ss.a == null)
			return;
		
		str.clear();
		
		int end = 0;
		lines = 0;
		
		if (ss.a.indu().hType().player && ss.a.indu().hType() != HTYPES.CHILD()) {
			
			str.add(ss.a.race().bio().opinionTitle(ss.a));
			end = setColor(end, COLOR.WHITE100);
			str.NL();
			
			
			ss.a.race().bio().opinions(impr, ss.a);
			for (CharSequence s : impr) {
				if (s.length() > 0) {
					str.s(4).add('-').s();
					str.add(s);
					str.NL();
				}
			}
			end = setColor(end, GCOLOR.T().WARNING);
			str.NL();
		}
		
		
		boolean nl = false;
		for (BioLine d : ss.a.race().bio().lines()) {
			
			CharSequence s = d.get(ss.a);
			if (s != null) {
				if (nl)
					for (int i = 0; i < 4; i++)
						str.s();

				str.add(s);
				nl = d.nl();
				if (nl)
					str.NL();
				else
					str.s();
			}
		}
		end = setColor(end, COLOR.WHITE100);
		str.NL();
		

		super.render(r, ds);
	}
	
	private int setColor(int end, COLOR color) {
		while(end < str.length() && lines < starts.length) {
			int start = font.getStartIndex(str, end);
			end = font.getEndIndex(str, start, width);
			starts[lines] = start;
			ends[lines] = end;
			lines++;
			cols[lines] = color;
		}
		return end;
	}

	
}
