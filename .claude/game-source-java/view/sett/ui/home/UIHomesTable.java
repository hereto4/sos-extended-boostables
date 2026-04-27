package view.sett.ui.home;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.type.HCLASSES;
import init.type.HGROUP;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

final class UIHomesTable extends GuiSection {

	private static CharSequence ¤¤Housed = "¤Housed";
	private static CharSequence ¤¤HousedD = "¤Subjects that have a home. There might be a small delay between building new houses and having people move in.";
	private static CharSequence ¤¤Homeless = "¤Homeless";
	private static CharSequence ¤¤HomelessD = "¤Subjects that have looked for housing, yet have not found one. Oddjobbers will search for houses across the whole map. Employed people will search in the vicinity of their workplace.";
	private static CharSequence ¤¤HousingTotal = "¤Total Housing";
	private static CharSequence ¤¤HousingAvailable = "¤Available Housing";
	private static CharSequence ¤¤HousingAvailableD = "¤Available Housing of this type across the map. Note that these houses might be beyond the reach of employed people.";
	private static CharSequence ¤¤ClickToGoToFirstHomeless = "¤Click to go to a subject that has trouble finding a home.";
	private static CharSequence ¤¤FurnishClick = "¤Click to manage furnishing.";

	Humanoid subject;
	private int hi = 0;

	static {
		D.ts(UIHomesTable.class);
	}

	public UIHomesTable(int HEIGHT) {

		Data housed = new Data(¤¤Housed, ¤¤HousedD) {

			@Override
			GText format(GText t, HGROUP h) {
				return GFORMAT.i(t, STATS.HOME().GETTER.stat().data(h.type).get(h.race));
			}
		};

		Data homeless = new Data(¤¤Homeless, ¤¤HomelessD) {

			@Override
			GText format(GText t, HGROUP h) {
				int am = STATS.HOME().GETTER.hasSearched.data(h.type).get(h.race);
				GFORMAT.i(t, am);
				if (am > 0)
					t.errorify();
				return t;
			}
		};

		Data available = new Data(¤¤HousingAvailable, ¤¤HousingAvailableD) {

			@Override
			GText format(GText t, HGROUP h) {

				int am = SETT.ROOMS().HOME.total(h) - SETT.ROOMS().HOME.used(h);

			
				return GFORMAT.i(t, am);
			}
		};

		Data total = new Data(¤¤HousingTotal, "") {

			@Override
			GText format(GText t, HGROUP h) {
				int am = SETT.ROOMS().HOME.total(h);

				
				return GFORMAT.i(t, am);
			}
		};

		Data furnishing = new Data(STATS.HOME().materials.info().name, STATS.HOME().materials.info().desc) {

			@Override
			GText format(GText t, HGROUP h) {
				return GFORMAT.perc(t, STATS.HOME().materials.data(h.type).getD(h.race));
			}
		};

		GTableBuilder bu = new GTableBuilder() {

			@Override
			public int nrOFEntries() {
				return HGROUP.all().size();
			}

			private void hover(GBox box, HGROUP h, Data d) {
				box.textL(d.name);
				box.tab(5);
				box.add(d.format(box.text(), h));
				box.NL();
				box.text(d.desc);
				box.NL(4);
			}

			@Override
			public void hoverInfo(int index, GBox box) {
				HGROUP h = HGROUP.all().get(index);
				box.title(h.name);

				hover(box, h, homeless);
				hover(box, h, housed);
				hover(box, h, available);
				hover(box, h, total);
				hover(box, h, furnishing);

			}

			@Override
			public boolean activeIs(int index) {
				HGROUP h = HGROUP.all().get(index);
				return STATS.POP().POP.data(h.type).get(h.race) > 0;
			}
		};
		GRowBuilder b;

		int size = 90;

		b = new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new HOVERABLE.Sprite(Icon.M + 4) {
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						HGROUP h = HGROUP.all().get(ier.get());
						h.icon.render(r, body.x1() + 2, body().y1() + 2);
					}
				};
			}
		};
		bu.column(null, 48, b);

		bu.column(homeless.name, size, row(homeless));
		bu.column(housed.name, size, row(housed));

		b = new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {

				GStat a = new GStat() {

					@Override
					public void update(GText text) {

						HGROUP h = HGROUP.all().get(ier.get());
						available.format(text, h);
					}
				};
				GStat b = new GStat() {

					@Override
					public void update(GText text) {
						text.add('(');
						HGROUP h = HGROUP.all().get(ier.get());
						total.format(text, h);
						text.add(')');
					}
				};

				return new RENDEROBJ.RenderImp(20, b.height()) {

					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						a.render(r, body().x1(), body().y1());
						b.render(r, body().x1() + 60, body().y1());
					}
				};
			}
		};
		bu.column(Dic.¤¤Available, size + 40, b);

		b = new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {

				GuiSection s = new GuiSection();

				GStat a = new GStat() {

					@Override
					public void update(GText text) {

						HGROUP h = HGROUP.all().get(ier.get());
						furnishing.format(text, h);
					}
				};

				GButt b = new GButt.Glow(SPRITES.icons().s.cog) {

					@Override
					protected void clickA() {
						HGROUP h = HGROUP.all().get(ier.get());

						if (h.type == HCLASSES.CITIZEN()) {
							VIEW.s().ui.standing.openAccess(h.race);
						}

						super.clickA();
					}

					@Override
					public void hoverInfoGet(GUI_BOX text) {

						HGROUP h = HGROUP.all().get(ier.get());
						if (h.type != HCLASSES.NOBLE())
							text.text(¤¤FurnishClick);
						text.NL(8);

						super.hoverInfoGet(text);
					}

				};

				s.add(b);
				s.addRightC(2, a.r());

				return s;
			}
		};
		bu.column(furnishing.name, size + 20, b);

		b = new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {

				return new GButt.ButtPanel(SPRITES.icons().s.arrow_right) {

					@Override
					protected void clickA() {
						HGROUP h = HGROUP.all().get(ier.get());
						search(h);
						super.clickA();
					}

					@Override
					protected void renAction() {
						HGROUP h = HGROUP.all().get(ier.get());
						activeSet(STATS.HOME().GETTER.hasSearched.data(h.type).get(h.race) > 0);
					}

					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.text(¤¤ClickToGoToFirstHomeless);
						text.NL();
					}

				};

			}
		};
		bu.column(null, 48, b);

		add(bu.createHeight(HEIGHT, true));

	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (subject != null) {
			VIEW.s().getWindow().centerer.set(subject.body().cX(), subject.body().cY());
			SETT.OVERLAY().add(subject);
		}
	}

	private void search(HGROUP t) {

		ENTITY[] ee = SETT.ENTITIES().getAllEnts();

		for (int i = 0; i < ee.length; i++) {
			if (hi >= ee.length)
				hi = 0;
			ENTITY e = SETT.ENTITIES().getAllEnts()[hi];
			hi++;
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;

				if (STATS.HOME().GETTER.hasSearched.indu().get(h.indu()) == 0)
					continue;

				if (t == HGROUP.get(h)) {
					subject = h;
					return;
				}
			}

		}

	}

	private abstract static class Data {

		final CharSequence name;
		final CharSequence desc;

		Data(CharSequence name, CharSequence desc) {
			this.name = name;
			this.desc = desc;
		}

		abstract GText format(GText t, HGROUP h);

	}

	private static GRowBuilder row(Data data) {
		return new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {

					@Override
					public void update(GText text) {
						HGROUP h = HGROUP.all().get(ier.get());
						data.format(text, h);

					}
				}.r(DIR.NW);
			}
		};

	}

}
