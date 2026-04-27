package settlement.stats.equip;

import java.io.IOException;

import init.paths.PATH;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.equip.EquipBattle.HumanSprite;
import settlement.stats.stat.StatCollection;
import snake2d.Renderer;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.RMAP;
import util.rendering.ShadowBatch;
import util.text.D;

public final class StatsEquip extends StatCollection {

	private final ArrayList<Equip> all;
	private final ArrayList<EquipBattle> military;
	private final LIST<EquipBattle> military_all;
	private final ArrayList<EquipRange> ammo;
	private final ArrayList<EquipCivic> civic;
	private final ArrayListGrower<EquipBattle.HumanSprite> sprites = new ArrayListGrower<EquipBattle.HumanSprite>();
	public final ArrayListGrower<EquipBattle> mounts = new ArrayListGrower<EquipBattle>();
	
	private static CharSequence ¤¤name = "Equipment";
	private static CharSequence ¤¤desc = "Having your subjects equip certain items can boost them in different ways. It can also improve happiness amongst them.";
	static CharSequence ¤¤Level = "¤{0} Level";
	static CharSequence ¤¤Target = "¤{0} Target";
	static CharSequence ¤¤Level_desc = "¤The target number of items each individual should equip.";
	public static CharSequence ¤¤Wear = "¤Wear-out rate per item and year:";

	
	public final Equip CLOTHES;
	public final RMAP<EquipBattle> militaryColl;
	public final RMAP<Equip> collAll;
	static {
		D.ts(StatsEquip.class);
	}
	
	public StatsEquip(StatsInit init) throws IOException {
		super(init, "EQUIP", ¤¤name, ¤¤desc);
		
		LinkedList<Equip> all = new LinkedList<>();
		
		PATH data = init.pd.getFolder("equip");
		
		{
			LinkedList<EquipCivic> tmp = new LinkedList<>();
			PATH d = data.getFolder("civic");
			this.CLOTHES = new EquipCivic("_CLOTHES", d, all, tmp, init);
			for (String k : d.getFiles()) {
				new EquipCivic(k, d, all, tmp, init);
			}
			this.civic = new ArrayList<>(tmp);
		}
		
		LinkedList<EquipBattle> mil = new LinkedList<>();
		KeyMap<TILE_SHEET> sprite = new KeyMap<TILE_SHEET>();
		{
			LinkedList<EquipBattle> tmp = new LinkedList<>();
			PATH d = data.getFolder("battle");
			for (String k : d.getFiles()) {
				
				EquipBattle e = new EquipBattle("BATTLE", k, d, all, mil, init, sprite);
				tmp.add(e);
			}
			this.military = new ArrayList<>(tmp);
		}
		
		{
			LinkedList<EquipRange> tmp = new LinkedList<>();
			PATH d = data.getFolder("ranged");
			for (String k : d.getFiles()) {
				new EquipRange(k, d, all, tmp, mil, init, sprite);
			}
			this.ammo = new ArrayList<>(tmp);
		}
		
		this.military_all = new ArrayList<EquipBattle>(mil);
		KeyMap<EquipBattle> map = new KeyMap<EquipBattle>();
		for (EquipBattle mm : military_all)
			map.put(mm.eKey(), mm);
		
		militaryColl = new RMAP<EquipBattle>("EQUIPMENT", military_all);
		
		this.all = new ArrayList<>(all);
		
		collAll = new RMAP<Equip>("EQUIPMENT", this.all);
		
		D.t(this);

		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				for (Equip t : all) {
					t.update16(h, updateI, updateI, day);
				}
			}
		});
		
		init.onArrival.add(new StatInitable() {
			
			@Override
			public void init(Induvidual h) {
				for (Equip t : all) {
					t.set(h, t.arrivalAmount);
				}
			}
		});
		
		for (EquipBattle e : military_all) {
			if (e.sprite != null)
				sprites.add(e.sprite);
		}
		
		for (EquipBattle e : military_all) {
			if (e.mount != null)
				mounts.add(e);
		}
		
	}
	
	public void drop(Humanoid h) {
		
		for (Equip e : all) {
			int a = Math.round(e.stat().indu().get(h.indu())*RND.rFloat());
			if (a > 0) {
				SETT.THINGS().resources.create(h.physics.tileC(), e.resource(), a);
			}
		}
	}

	public LIST<Equip> allE() {
		return all;
	}
	
	public LIST<EquipCivic> civics() {
		return civic;
	}
	
	public LIST<EquipBattle> BATTLE_MELEE() {
		return military;
	}
	
	public LIST<EquipRange> RANGED() {
		return ammo;
	}
	
	public LIST<EquipBattle> BATTLE_ALL() {
		return military_all;
	}
	
	public void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch shadow, double forward, int x, int y) {
		
		for (HumanSprite s : sprites) {
			s.render(a, r, dir, forward, x, y, shadow);
		}
		
	}

}
