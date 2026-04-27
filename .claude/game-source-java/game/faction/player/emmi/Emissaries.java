package game.faction.player.emmi;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.Faction.FactionActivityListener;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.message.MessageText;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

public class Emissaries {

	public static CharSequence ¤¤name = "Emissary Points";
	public static CharSequence ¤¤desc = "Emissaries are used to influence foreign courts, or to increase support in regions outside of your realm. Emissaries are trained in an embassy, and can be assigned in the faction panel or the region panel.";
	private static CharSequence ¤¤low = "Emissary points Low";
	private static CharSequence ¤¤lowD = "We do no longer employ as many emissaries as are needed. As a result, all our diplomatic missions will suffer a penalty. We should cancel a few missions so that there is no shortage.";
	
	
	private static CharSequence ¤¤support = "Gather Support";
	private static CharSequence ¤¤supportD = "Gathers support in a region, so that it will be more inclined to be ruled by you in the future.";
	
	private static CharSequence ¤¤assasinate = "Assassinate";
	private static CharSequence ¤¤assasinateD = "Assassinate a royalty. Has a small chance of succeeding and failed attempts will decrease the royalty's opinion of you severely.";
	
	private static CharSequence ¤¤flatter = "Flatter";
	private static CharSequence ¤¤flatterD = "Flattering a royalty will increase their opinion of you and your faction.";
	
	private static CharSequence ¤¤sab = "Sabotage";
	private static CharSequence ¤¤sabD = "Sabotage and insult a royalty to decrease their opinion of you.";
	
	
	
	private int mDay = -60;
	static {
		D.ts(Emissaries.class);
	}
	
	private double penalty = 1;
	
	public final EmiTypeReg assimilate = new EmiTypeReg(
			UI.icons().s.fist.createColored(new ColorImp(40, 40, 120)), 
			¤¤support, ¤¤supportD) {

				@Override
				public void formatValue(GText t, Region v) {
					GFORMAT.perc(t, RD.OWNER().affiliation.getD(v));
				}		
	};
	
	
	public final EmiTypeRoy assasinate = new EmiTypeRoy(
			UI.icons().s.death.createColored(new ColorImp(120, 20, 20)), 
			¤¤assasinate, ¤¤assasinateD) {

				@Override
				public void formatValue(GText t, Royalty v) {
					GFORMAT.perc(t, ROPINIONS.EMMI().assasinationValue(v));
				}

		
	};
//	public final EmiTypeRoy favour  = new EmiTypeRoy(UI.icons().s.star.createColored(new ColorImp(20, 120, 20)), ¤¤favour, ¤¤favourD);
	public final EmiTypeRoy flatter  = new EmiTypeRoy(
			UI.icons().s.heart.createColored(new ColorImp(120, 40, 120)), 
			¤¤flatter, ¤¤flatterD) {

				@Override
				public void formatValue(GText t, Royalty v) {
					GFORMAT.f0(t, ROPINIONS.EMMI().flatteryCurrent(v));
					t.s().add('>').s();
					GFORMAT.f0(t, ROPINIONS.EMMI().flatteryValue(v));
				}
	};
	
	public final EmiTypeRoy sabotage  = new EmiTypeRoy(
			UI.icons().s.cog.createColored(new ColorImp(120, 40, 120)), 
			¤¤sab, ¤¤sabD) {

				@Override
				public void formatValue(GText t, Royalty v) {
					GFORMAT.f0(t, ROPINIONS.EMMI().sabotage(v));
					t.s().add('>').s();
					GFORMAT.f0(t, ROPINIONS.EMMI().sabotageValue(v));
				}
	};
	
	public final LIST<EmiTypeRoy> roys = new ArrayList<>(assasinate, flatter, sabotage);
	public final LIST<EmiTypeReg> regs = new ArrayList<>(assimilate);
	public final LIST<EmiType<?>> all = new ArrayList<>(assimilate, assasinate, flatter, sabotage);


	public Emissaries() {
		new FactionActivityListener() {
			
			@Override
			public void remove(FactionNPC ff) {
				for (EmiTypeRoy t : roys)
					t.clear(ff);
			}
			
			@Override
			public void add(FactionNPC f) {
				// TODO Auto-generated method stub
				
			}
		};
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player()) {
					for (EmiTypeReg t : regs)
						t.set(reg, 0);
				}
			}
		};
		
		new NPCCourt.RoyaltyEventListener() {
			
			@Override
			public void change(int successionI, Royalty old, Royalty nn) {
				if (old != null)
					assasinate.set(old, 0);
				if (successionI == 0)
					return;
				if (successionI == 0 && nn != null) {
					int o = old == null ? 0 : flatter.get(old);
					int n = flatter.get(nn);
					o = Math.max(o, n);
					flatter.set(successionI, o);
				}
				
			}
		};
		
		for (EmiType<?> t : all) {
			double max = -1000000;
			final double maxI = -1.0/max;
			BValue v = new BValue.BValueFaction(BOOSTABLES.CIVICS().DIPLOMACY) {
				
				@Override
				public double vGet(Player f) {
					return t.total()*maxI;
				}

				@Override
				public double vGet(FactionNPC f) {
					return 0;
				}
			};
			
			new BoosterValue(v, new BSourceInfo(t.name, t.icon), 0, -1000000, false).add(BOOSTABLES.CIVICS().DIPLOMACY);
		}
		
		IDebugPanel.add("diplomacy + 10000", new ACTION() {

			@Override
			public void exe() {
				BValue v = new BValue.BValuePlayerOnly() {
					
					@Override
					public double vGet(Player f) {
						return 1;
					}

					@Override
					public double vGet(FactionNPC f) {
						return 0;
					}
				};
				
				new BoosterValue(v, new BSourceInfo("cheat", UI.icons().s.admin), 0, 10000, false).add(BOOSTABLES.CIVICS().DIPLOMACY);
			}
			
		});
		
	}

	private final IUpdater upRoy = new IUpdater(FACTIONS.MAX()*NPCCourt.MAX, 120) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			Faction f = FACTIONS.getByIndex(i/NPCCourt.MAX);
			
			if (f instanceof FactionNPC) {
				FactionNPC ff = (FactionNPC) f;
				if (ff == null || !ff.isActive() || ff.court().all().get(i%NPCCourt.MAX) == null) {
					for (EmiTypeRoy t : roys)
						t.set(i, 0);
				}
			}	
		}
		
	};
	
	private final IUpdater upReg = new IUpdater(WREGIONS.MAX, 120) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			Region reg = WORLD.REGIONS().getByIndex(i);
			if (reg == null || !reg.active() || reg.faction() == FACTIONS.player()) {
				for (EmiTypeReg t : regs)
					t.set(reg, 0);
			}
		}
		
	};
	
	private int viewI = -1;
	
	public double penaltyMul() {
		if (VIEW.RI() != viewI) {
			viewI = VIEW.RI();
			double am = BOOSTABLES.CIVICS().DIPLOMACY.get(FACTIONS.player());
			if (am < 0) {
				am = Math.floor(am);
				int tot = 0;
				for (EmiType<?> t : all) {
					tot += t.total();
				}
				
				penalty = -am/(tot);
				penalty = CLAMP.d(penalty, 0, 1);
				penalty = 1.0-penalty;
			}else {
				penalty = 1;
			}
			
		}
		return penalty;
	}
	
	public int available() {
		return (int) (BOOSTABLES.CIVICS().DIPLOMACY.get(FACTIONS.player()));
	}
	
	public int produced() {
		return (int) (BOOSTABLES.CIVICS().DIPLOMACY.get(FACTIONS.player())) + spent();
	}
	
	public int spent() {
		int am = 0;
		for (EmiType<?> t : all)
			am += t.total();
		return am;
	}
	
	public int spent(FactionNPC f) {
		int am = 0;
		for (EmiTypeRoy t : roys)
			am += t.total(f);
		return am;
	}
	
	public void update(double ds) {
		upReg.update(ds);
		upRoy.update(ds);
		
		if (penaltyMul() < 1 && Math.abs(TIME.days().bitsSinceStart()-mDay) > 10) {
			new MessageText(¤¤low).paragraph(¤¤lowD).send();
			mDay = TIME.days().bitsSinceStart();
		}
	}
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(mDay);
			for (EmiType<?> t : all)
				t.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			mDay = file.i();
			for (EmiType<?> t : all)
				t.load(file);	
		}
		
		@Override
		public void clear() {
			mDay = -60;
			for (EmiType<?> t : all)
				t.clear();	
		}
	};
	
	
	
}
