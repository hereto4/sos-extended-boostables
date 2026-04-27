package view.world.ui.army;

import game.GAME;
import game.boosting.BHoverer;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;

class ArmyInfo extends GuiSection{


	private static CharSequence ¤¤MoraleDesc = "Morale is gained by keeping the army well supplied and by winning battles. Morale affects your army's performance on the battlefield.";
	private static CharSequence ¤¤HealthDesc = "Health is gained by keeping the army well supplied. Poor health will lead to desertion.";
	
	private static CharSequence ¤¤CreditsD = "The amount of money needed to upkeep this army daily.";
	static {
		D.ts(ArmyInfo.class);
	}
	
	public static GuiSection info(GETTER<WArmy> army) {
		GuiSection ss = new GuiSection();
		if (false) {
			//have some kind of employment swapper. Matching best man for best div
		}
		ss.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, AD.men(null).get(army.get()));
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Soldiers);
				b.add(GFORMAT.iofkInv(b.text(), AD.men(null).get(army.get()), AD.menTarget(null).get(army.get())));
				
			};
			
		}.hh(SPRITES.icons().s.human));
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				army.get().state().info(army.get(), text);
				text.lablifySub();
			}
		}.r(DIR.NW));
		
		ss.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, AD.supplies().health(army.get()));
			}
		}.hh(SPRITES.icons().s.pluses).hoverTitleSet(Dic.¤¤Health).hoverInfoSet(¤¤HealthDesc), 0, ss.body().y2()+2);
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.percInc(text, AD.morale(army.get()), 0);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Morale);
				b.text(¤¤MoraleDesc);
				b.sep();
				BHoverer.hoverDetailed(b, AD.moraleFactors(), army.get(), Dic.¤¤Factors, 1, true);
			};
			
		}.hh(SPRITES.icons().s.standard));
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)GAME.battle().power.get(army.get()));
				if (S.get().developer) {
					text.s();
					GFORMAT.i(text, (int)AD.power().get(army.get()));
				}
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				//GAME.battle().power2.hover(army.get(), b);
			};
			
		}.hh(SPRITES.icons().s.fist));
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				double needed = 0;
				double total = 0;
				for (ADSupply s : AD.supplies().all) {
					needed += s.current().get(army.get());
					total += s.targetAmount(army.get());
				}
				if (total == 0)
					needed = 1;
				else
					needed/=total;
				GFORMAT.perc(text, needed);
			}
		}.hh(SPRITES.icons().s.storage).hoverTitleSet(Dic.¤¤Supplies).hoverInfoSet(Dic.¤¤SuppliesD));
		
		

		
		ss.body().incrW(64);
		
		return ss;
	}
	
	public static GuiSection supplies(GETTER<WArmy> army){
		GuiSection s = new GuiSection();
		
		int i = 0;
		
		for (ADSupply su : AD.supplies().all) {
			RENDEROBJ g = supply(army, su);
			
			s.add(g, (i%4)*(g.body().width()+16), (i/4)*(g.body().height()+4));
			i++;
		}
		
		RECTANGLE ee = s.getLast();
		
		s.add(new GStat() {
		
			@Override
			public void update(GText text) {
				GFORMAT.i(text, AD.supplies().credits().get(army.get()));
			}
			
			
		}.hh(SPRITES.icons().s.money.resized(Icon.M)).hoverInfoSet(¤¤CreditsD), (i%4)*(ee.width()+16), (i/4)*(ee.height()+4));
		i++;
		
		return s;
	}
	
	private static RENDEROBJ supply(GETTER<WArmy> army, ADSupply su) {
		
		int w = 60;
		int h = 14;
		
		SPRITE s = new SPRITE.Imp(w, h) {
			
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (su.targetAmount(army.get()) == 0) {
					GMeter.render(r, GMeter.C_GREEN_DARK, 0, X1, X2, Y1, Y2);
					return ;
				}
				
				double now = (double)su.minimumAmount(army.get())/su.targetAmount(army.get());
				double needed = (double)su.current().get(army.get())/su.targetAmount(army.get());
				
				if (su.current().get(army.get()) >= su.minimumAmount(army.get()))
					GMeter.render(r, GMeter.C_BLUE, needed, X1, X2, Y1, Y2);
				else
					GMeter.render(r, GMeter.C_REDORANGE, needed, X1, X2, Y1, Y2);
				
				X1 += 3 + now*(X2-X1-6);
				
				GCOLOR.UI().border().render(r, X1-1, X1+1, Y1, Y2);
				
				if (!SETT.ROOMS().SUPPLY.has(su.res))
					UI.icons().s.alert.render(r, X2-8, Y1-2);

			}
		};
		
		
		RENDEROBJ o = new GHeader.HeaderHorizontal(su.res.icon(), s) {

			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				su.hover(b, army.get());
			}
		};
		
		if (S.get().developer) {
			
			GuiSection ss = new GuiSection() {
				
				STRING_RECIEVER rec = new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						try {
							double d = Double.parseDouble(""+string);
							su.current().set(army.get(), (int) (su.targetAmount(army.get())*d));
						}catch(Exception e) {
							
						}
						
						
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().input.requestInput(rec, "set");
					super.clickA();
				}
			};
			ss.add(o);
			return ss;
		}else {
			return o;
		}
		
		
	}
	
}
