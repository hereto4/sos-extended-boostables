package settlement.entry;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import init.sprite.UI.UI;
import settlement.entry.EntryPoints.EntryPoint;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayCooShort;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.regions.Region;

final class Updater implements SAVABLE{

	private static CharSequence ¤¤open = "Capital Open!";
	private static CharSequence ¤¤openD = "Your capital is now open for immigrants and trade.";
	private static CharSequence ¤¤closed = "Capital Closed!";
	private static CharSequence ¤¤closedD = "Your throne can not be reached from the outside world. As a consequence, no immigration or trade can occur. Clear a path from the throne to an country road as soon as possible.";
	
	
	private static CharSequence ¤¤mTitle = "City Isolated!";
	private static CharSequence ¤¤mDesc = "One or several of our city's entry points have been blocked off, and as a result, outsiders will have a problem reaching us. This will have many negative consequences and should be fixed as quickly as possible.";
	
	static {
		D.ts(Updater.class);
	}
	
	private double checkTimer = 0;
	private boolean isClosed = false;
	private boolean besieged;
	private double besigeTime = 0;

	public void update(double ds, EntryPoints points) {
		
		if (VIEW.b().isActive())
			return;
		
		if (FACTIONS.player().capitolRegion() == null)
			return;
		
		int oldReach = points.reachable().size();
		
		points.update();
		
		
		
		if (oldReach > points.reachable().size()) {
			new Mess(points);
		}
		
		if (!SETT.INVADOR().invading()) {
			
			
			checkTimer += ds;
			if (checkTimer > 5) {
				checkTimer -= 5;
				Region c = FACTIONS.player().capitolRegion();
				
				besieged = false;
				
				if (SETT.INVADOR().invading()) {
					besieged = true;
				}else {
					for (WEntity e : WORLD.ENTITIES().fillTiles(c.cx()-3, c.cx()+3, c.cy()-3, c.cy()+3)) {
						if (e instanceof WArmy) {
							WArmy a = (WArmy) e;
							if (DIP.WAR().is(a.faction(), FACTIONS.player()) && a.besieging(FACTIONS.player().capitolRegion())) {
								besieged = true;
								break;
							}
						}
					}
				}
				
				
				
				
				
				
				if (besieged)
					isClosed = true;
				else if (isClosed && points.hasAny()){
					isClosed = false;
					if (!VIEW.b().isActive())
						new MessageText(¤¤open, ¤¤openD).send();
				}else if (!isClosed && !points.hasAny()) {
					if (!VIEW.b().isActive())
						new MessageText(¤¤closed, ¤¤closedD).send();
					isClosed = true;
				}
			}
			
			

		}
		
		
		
		if (besieged)
			besigeTime += ds;
		else
			besigeTime = 0;
		
	}

	
	@Override
	public void save(FilePutter file) {

		file.bool(isClosed);
		file.d(checkTimer);
		file.bool(besieged);
		file.d(besigeTime);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		isClosed = file.bool();
		checkTimer = file.d();
		besieged = file.bool();
		besigeTime = file.d();
	}
	
	@Override
	public void clear() {
		isClosed = false;
		checkTimer = 0;
		besieged = false;
		besigeTime = 0;
	}
	
	public boolean isClosed() {
		return isClosed;
	}
	
	public boolean beseiged() {
		return besieged;
	}
	
	public double besigeTime() {
		return besigeTime;
	}
	
	private static class Mess extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ArrayCooShort coos;
		
		public Mess(EntryPoints po) {
			super(¤¤mTitle);			
			coos = new ArrayCooShort(po.active().size()-po.reachable().size());
			for (EntryPoint p : po.active()) {
				if (!p.reachable()) {
					coos.get().set(p.coo());
					coos.inc();
				}
			}
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(¤¤mDesc);
			
			coos.set(0);
			
			while(true) {
				section.addRelBody(8, DIR.S, new GButt.ButtPanel(UI.icons().m.crossair){
					
					final int tx = coos.get().x();
					final int ty = coos.get().y();
					
					@Override
					protected void clickA() {
						VIEW.s().activate();
						VIEW.s().getWindow().centerAtTile(tx, ty);
					};
					
				}.pad(20, 2));
				
				if (!coos.hasNext())
					return;
				coos.next();
				
			}
				
				
			
		}
		
	}
	
}
