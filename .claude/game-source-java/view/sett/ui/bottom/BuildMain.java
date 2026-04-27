package view.sett.ui.bottom;

import static settlement.main.SETT.ROOMS;

import game.faction.FACTIONS;
import init.sprite.UI.UI;
import settlement.job.Job;
import settlement.job.JobBuildFence;
import settlement.job.JobBuildRoad;
import settlement.job.JobBuildStructure;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.category.RoomCategorySub;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.table.GScrollRows;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.tool.PLACABLE;

final class BuildMain {

	private final Inter inter;
	private static final int BUTTH = 44;
	private static final int BUTTM = 8;
	private static final int HEIGHT = BUTTH*BUTTM;
	private final UIRoomPlacer placer;
	private final KeyMap<BParenter> map = new KeyMap<>();
	private KeyMap<Boolean> himap = new KeyMap<Boolean>();
	
	
	private static CharSequence ¤¤Build = "¤Build:";
	private static CharSequence ¤¤Fences = "¤Fences";
	private static CharSequence ¤¤Roads = "¤Roads";
	private static CharSequence ¤¤move = "¤Move";
	private static CharSequence ¤¤Construct = "¤Construct";
	private static CharSequence ¤¤jobs = "Jobs";
	private static CharSequence ¤¤jblue = "and click to place one of your saved blueprint.";
	static {
		D.ts(BuildMain.class);
	}
	
	
	BuildMain(Inter inter, UIRoomPlacer placer){
		this.inter = inter;
		this.placer = placer;
		
	}
	
	public void hilight(String key) {
		if (map.get(key) != null)
			map.get(key).higlight();
		else {
			if (himap.containsKey(key))
				return;
			himap.put(key, Boolean.TRUE);
			LOG.ln("no: " + key);
			LOG.ln(map.keysString());
		}
	}
	
	public GuiSection create() {
		GuiSection s = new GuiSection();
		
		
		
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_AGRIULTURE,
			};
			BPanel p = create(s, UI.icons().l.agri, SETT.ROOMS().CATS.MAIN_AGRIULTURE.name);
			append(p, cats);
		}
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_INDUSTRY
			};
			BPanel p = create(s, UI.icons().l.work, SETT.ROOMS().CATS.MAIN_INDUSTRY.name);
			append(p, cats);
		}
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_SERVICE,
			};
			BPanel p = create(s, UI.icons().l.service, SETT.ROOMS().CATS.MAIN_SERVICE.name);
			append(p, cats);
		}
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_INFRA,
			};
			BPanel p = create(s, SETT.ROOMS().CATS.MAIN_INFRA.icon, SETT.ROOMS().CATS.MAIN_INFRA.name);
			append(p, cats, SETT.ROOMS().CATS.MAIN_INFRA.misc, SETT.ROOMS().CATS.DECOR);
		}

		{

			
			
			BPanel p = create(s, UI.icons().l.infra, ¤¤Construct);
			
			
			
			{
				ACTION a = new ACTION() {
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().uiManager.disturb();
						VIEW.s().tools.place(ROOMS().THRONE.placer);
					}
				};
				String name = ¤¤move + " " + ROOMS().THRONE.info.name;
				
				new BAction(p, "MOVE_THRONE", ROOMS().THRONE.icon(), name, name, a) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						ROOMS().THRONE.placer.hoverDesc((GBox) text);
						super.hoverInfoGet(text);
					}
				};
			}
			
			for (RoomBlueprintImp r : SETT.ROOMS().CATS.MAIN_INFRA.misc.rooms())
				new BRoom(p, r);
			
			{
			

				
				INFO i = new INFO(¤¤Fences, ""+ ¤¤Build + " " + ¤¤Fences);
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						Job j = JobBuildFence.getPlacable();
						if (j != null) {
							VIEW.inters().popup.close();
							VIEW.s().uiManager.disturb();
							VIEW.s().tools.place(j.placer(), j.config());
						}
					}
				};
				new BAction(p, "FENCES", SETT.JOBS().fences.get(0).placer().getIcon(), i.name, i.desc, a);
				
				i = new INFO(¤¤Roads, ""+ ¤¤Build + " " + ¤¤Roads);
				a = new ACTION() {
					
					@Override
					public void exe() {
						Job j = JobBuildRoad.getPlacable();
						if (j != null) {
							VIEW.inters().popup.close();
							VIEW.s().uiManager.disturb();
							VIEW.s().tools.place(j.placer(), j.config());
						}
					}
				};
				new BAction(p, "ROADS", SETT.JOBS().roads.all.get(0).placer().getIcon(), i.name, i.desc, a);
				
				i = new INFO(Dic.¤¤Structures, ""+ ¤¤Build + " " + Dic.¤¤Structures);
				a = new ACTION() {
					
					@Override
					public void exe() {
						Job j = JobBuildStructure.getPlacable();
						if (j != null) {
							VIEW.inters().popup.close();
							VIEW.s().uiManager.disturb();
							VIEW.s().tools.place(j.placer(), j.config());
						}
					}
				};
				new BAction(p, "STRUCTURES", SETT.JOBS().build_structure.get(0).combo.getIcon(), i.name, i.desc, a);
				
				RoomCategorySub cat = SETT.ROOMS().CATS.DECOR;
				ACTION ac = new ACTION() {
					
					@Override
					public void exe() {
						for (RoomBlueprintImp b : cat.rooms())
							if (b.reqs.passes(FACTIONS.player())) {
								VIEW.inters().popup.close();
								VIEW.s().uiManager.disturb();
								placer.init(b, cat);
								return;
							}
					}
				};
				new BAction(p, "DECOR", cat.icon(), cat.name(), ""+ ¤¤Build + " " + cat.name(), ac);
				
				for (RoomBlueprintImp b : cat.rooms()) {
					
					CLICKABLE c = new B(null, b.iconBig(), b.info.name) {
						@Override
						protected void clickA() {
							VIEW.inters().popup.close();
							VIEW.s().uiManager.disturb();
							if (KEYS.MAIN().MOD.isPressed() && VIEW.s().misc.prints.has(b)) {
								VIEW.s().misc.prints.open(b);
								
							}else
								placer.init(b, cat);
							return;
						}
						
						@Override
						protected void renAction() {
							activeSet(b.reqs.passes(FACTIONS.player()));
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							UIRoomBuild.hoverRoomBuild(b, text);
							
						}
						
					};
					SearchToolPanel.add(c, b.info.name, b.info.desc);
					
				}
				
				
				LinkedList<Job> li = new LinkedList<>();
				li.add(SETT.JOBS().build_fort.build_stairs);
				li.add(SETT.JOBS().build_fort.all);
				i = new INFO(Dic.¤¤Fortifications, ""+ ¤¤Build + " " + Dic.¤¤Fortifications);
				a = new ACTION() {
					
					@Override
					public void exe() {
						Job j = SETT.JOBS().build_fort.getPlacable();
						if (j != null) {
							VIEW.inters().popup.close();
							VIEW.s().uiManager.disturb();
							
							
							VIEW.s().tools.place(j.placer(), j.config());
						}
					}
				};
				new BAction(p, "FORTIFICATION", SETT.TERRAIN().FORTIFICATIONS.all().get(0).tile.getIcon(), i.name, i.desc, a);
				

			}
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
					
			};
			append(p, cats);
		}
		

		
		{

			
			
			BPanel p = create(s, UI.icons().l.jobs, ¤¤jobs);
			
			
			
			{
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().tools.place(SETT.JOBS().clearss.food.placer());
					}
				};
				new BAction(p, "JOB_FORRAGE", SETT.JOBS().clearss.food.placer().getIcon(),SETT.JOBS().clearss.food.placer().name(), SETT.JOBS().clearss.food.placer().desc(), a);
			}
			
			{
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().tools.place(SETT.JOBS().clearss.hunt);
					}
				};
				new BAction(p, "JOB_HUNT", SETT.JOBS().clearss.hunt.getIcon(),SETT.JOBS().clearss.hunt.name(), SETT.JOBS().clearss.hunt.desc(), a);
			}
			
			{
				
				int id = 0;
				

				for (PLACABLE pp : SETT.JOBS().clearss.placers) {
					if (pp == SETT.JOBS().clearss.returnwater.placer() || pp == SETT.JOBS().clearss.caveFill.placer())
						continue;
					new BAction(p, "JOB_CLEAR_" + id, pp, "");
					id++;
				}
				
			}
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				
			};
			append(p, cats);
		}
		
		ArrayListGrower<Job> jobs = new ArrayListGrower<>();
		jobs.add(SETT.JOBS().fences);
		for (JobBuildStructure st : SETT.JOBS().build_structure)
			jobs.add(st.wall);
	
		for (JobBuildRoad r : SETT.JOBS().roads.all)
			jobs.add(r);
		jobs.add(SETT.JOBS().build_fort.all);
		jobs.add(SETT.JOBS().build_fort.build_stairs);
		
		for (Job j : jobs) {
			BButt b = new BButt(j.placer().getIcon(), j.placer().name()) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.close();
					VIEW.s().uiManager.disturb();
					VIEW.s().tools.place(j.placer(), j.config());
				}
			};
			SearchToolPanel.add(b, j.placer().name() , j.placer().desc());
		}
		
		
		return s;
		
	}
	
	private BPanel create(GuiSection mainS, SPRITE icon, CharSequence label) {
		BPanel panel = new BPanel();
		BMain main = new BMain(panel, icon, label);
		panel.daddy = main;
		mainS.addRightC(0, main);
		return panel;
	}
	
	private void append(BPanel panel, RoomCategoryMain[] cats, RoomCategorySub... ignore) {

		
		LinkedList<RoomBlueprintImp> misc = new LinkedList<>();
		
		oo:
		for (RoomCategoryMain m : cats) {
			ouer:
			for (RoomCategorySub ss : m.subs) {
				for (RoomCategorySub so : ignore)
					if (so == ss)
						continue ouer;
			}
			for (RoomCategorySub so : ignore) {
				if (so == m.misc)
					break oo;
			}
					
			for (RoomBlueprintImp b : m.misc.rooms()) {
				misc.add(b);
			}
		}
		
		if (misc.size() > 0) {
			for (RoomBlueprintImp b : misc) {
				new BRoom(panel, b);
			}
		}
		
		for (RoomCategoryMain m : cats) {
			ouer:
			for (RoomCategorySub ss : m.subs) {
				for (RoomCategorySub so : ignore)
					if (so == ss)
						continue ouer;
				
				append(panel, ss);
			}
		}
		
		
	}
	
	private BPanel append(BPanel panel, RoomCategorySub ss) {

		BPanel exp = new BPanel();
		exp.daddy = new BExp(panel, ss.icon(), ss.name(), exp);
		
		for (RoomBlueprintImp b : ss.rooms()) {
			new BRoom(exp, b);
		}
		return exp;
	}
	
	interface BParenter {
		
		public BParenter parent();
		public CLICKABLE cl();
		public void higlight();
	}
	
	private class BPanel implements BParenter{
		
		private BParenter daddy;
		private GuiSection section;
		final ArrayListGrower<B> butts = new ArrayListGrower<>();
		
		
		
		@Override
		public CLICKABLE cl() {
			if (section == null) {
				section = new SPanel();
				section.body().setHeight(HEIGHT);
				section.body().setWidth(1);
				CLICKABLE bbs;
				
				if (butts.size() > BUTTM) {
					LinkedList<CLICKABLE> rows = new LinkedList<>();
					for (B b : butts)
						rows.add(b.cl());
					bbs = new GScrollRows(rows, HEIGHT).view();
				}else {
					GuiSection bb = new GuiSection();
					for (B b : butts) {
						bb.addDown(0, b.cl());
					}
					bbs = bb;
				}
				
				
				
				int dy = HEIGHT;
				if (daddy instanceof BExp) {
					BPanel pp = ((B) daddy).daddy;
					dy = daddy.cl().body().y1()-pp.section.body().y1();
				}
				
				bbs.body().moveCY(dy);
				
				if (bbs.body().y2() > section.body().y2())
					bbs.body().moveY2(section.body().y2());
				
				if (bbs.body().y1() < section.body().y1())
					bbs.body().moveY1(section.body().y1());
				
				section.add(bbs);
				section.pad(3, 8);
				
			}
			
			return section;
		}

		@Override
		public BParenter parent() {
			return daddy;
		}

		@Override
		public void higlight() {
			daddy.higlight();
		}
	}
	
	private class B extends BButt implements BParenter {

		private final BPanel daddy;
		private boolean con;
		
		public B(BPanel panel, SPRITE icon, CharSequence label) {
			super(icon, label);
			if (panel != null) {
				this.daddy = panel;
				daddy.butts.add(this);
			}else {
				daddy = null;
			}
			
		}

		@Override
		public BParenter parent() {
			return daddy;
		}

		@Override
		public CLICKABLE cl() {
			return this;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
			if (con) {
				COLOR.RED2RED.renderFrame(r, body, 0, 3);
				con = false;
			}
		}

		@Override
		public void higlight() {
			con = true;
			daddy.higlight();
		}
		
		
	}
	
	private class BMain extends GButt.ButtPanel implements BParenter {

		private final BPanel pop;
		private boolean con;
		
		public BMain(BPanel panel, SPRITE icon, CharSequence label) {
			super(icon);
			hoverTitleSet(label);
			this.pop = panel;
			pad(6, 0);
		}

		@Override
		public BParenter parent() {
			return null;
		}

		@Override
		public CLICKABLE cl() {
			return this;
		}
		
		@Override
		protected void clickA() {
			inter.set(this, pop.cl());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
			if (con) {
				COLOR.RED2RED.renderFrame(r, body, 0, 3);
				con = false;
			}
		}

		@Override
		public void higlight() {
			con = true;
		}
		
	}
	
	private final class BExp extends B implements BParenter {

		private final BPanel exp;
		
		public BExp(BPanel panel, SPRITE icon, CharSequence label, BPanel exp) {
			super(panel, icon, label);
			this.exp = exp;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isHovered |= inter.exp == exp.cl();
			super.render(r, ds, isActive, isSelected, isHovered);
			UI.icons().m.arrow_right.renderCY(r, body().x2()-32, body.cY());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				inter.exp(this, exp.cl());
			}
			return super.hover(mCoo);
		}
		
	}
	
	private class BRoom extends  B {

		private final RoomBlueprintImp room;
		private final CLICKABLE wrap;
		
		public BRoom(BPanel panel, RoomBlueprintImp room) {
			super(panel, room.icon, room.info.name);
			this.room = room;
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					if (!room.reqs.passes(FACTIONS.player()))
						return;
					
					inter.hide();
					if (KEYS.MAIN().MOD.isPressed() && VIEW.s().misc.prints.has(room)) {
						VIEW.s().misc.prints.open(room);
						
					}else
						placer.init(room, -1,-1);
					return;
					
					
					
				}
			};
			String key = "BUILD_" + room.key;
			wrap = KeyButt.wrap(a, this, KEYS.SETT(), key, room.info.name, ¤¤Build + " " + room.info.name);
			map.put(key, this);
			clickActionSet(a);
			SearchToolPanel.add(wrap, room.info.name, room.info.desc);
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			UIRoomBuild.hoverRoomBuild(room, text);
			if (VIEW.s().misc.prints.has(room)) {
				GBox b = (GBox) text;
				
				b.text(KEYS.MAIN().MOD.repr());
				b.text(¤¤jblue);
				b.sep();
			}
		}
		
		@Override
		protected void renAction() {
			activeSet(room.reqs.passes(FACTIONS.player()));
		}
		
		@Override
		public CLICKABLE cl() {
			return wrap;
		}
		
		@Override
		protected void clickA() {
			
		}
		
	}
	
	private class BAction extends  B {

		private final CLICKABLE wrap;
		
		public BAction(BPanel panel, String key, SPRITE icon, CharSequence name, CharSequence desc, ACTION action) {
			super(panel, icon, name);
			key = "ACTION_" + key;
			wrap = KeyButt.wrap(action, this, KEYS.SETT(), key, name, desc);
			map.put(key, this);
			clickActionSet(action);
			SearchToolPanel.add(wrap, name, desc);
		}
		
//		public BAction(BPanel panel, String key, Job job) {
//			this(panel, key, job.placer(), job.placer().desc);
//			
//		}
		
		public BAction(BPanel panel, String key, PLACABLE place, CharSequence desc) {
			super(panel, place.getIcon(), place.name());
			key = "ACTION_" + key;
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().uiManager.disturb();
					VIEW.s().tools.place(place);
				}
			};
			wrap = KeyButt.wrap(a, this, KEYS.SETT(), key, place.name(), desc);
			map.put(key, this);
			clickActionSet(a);
			SearchToolPanel.add(wrap, place.name(), desc);
		}
		
		@Override
		public CLICKABLE cl() {
			return wrap;
		}
		
		@Override
		protected void clickA() {
			
		}
		
	}
	
}
