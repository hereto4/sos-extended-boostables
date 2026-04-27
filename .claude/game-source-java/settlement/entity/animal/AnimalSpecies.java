package settlement.entity.animal;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import init.constant.C;
import init.paths.PATHS;
import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.info.INFO;
import util.keymap.MAPPED;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerUtil;

public class AnimalSpecies extends INFO implements MAPPED{
	
	public static final int SIZE = 24*C.SCALE;
	private final double massMin;
	private final double heightOverGround;
	private final double acceleration;
	public final int hitboxSize;
	private final int spriteOff;
	public final Icon icon;
	private final int index;
	public final boolean caravanable;
	public final COLOR color;
	private final LIST<RESOURCE> resources;
	public final RBIT rBit;
	private final double[] resAmounts;
	private final double[] climates;
	private final double[] terrains;
	private final String key;
	public final double[] damage = new double[BOOSTABLES.BATTLE().DAMAGES.size()];
	
	public final boolean pack;
	public final boolean grazes;
	public final COLOR blood = new ColorImp(127, 15, 15);
	
	public final double momTreshold;
	public final double momTresholdFly;
	public final double caveLiving;
	
	public final double danger;
	
	public final SoundRace sound;
	public final TILE_SHEET sheet;
	
	AnimalSpecies(String key, int index, Json data, Json text, KeyMap<TILE_SHEET> sprites) throws IOException{
		super(text, null);
		this.key = key;
		this.index = index;
		icon = SPRITES.icons().get(data);
		caravanable = data.bool("CARAVAN");
		massMin = data.i("MASS", 1, 500); 
		acceleration = data.i("SPEED", 1, 31)*C.TILE_SIZE;
		heightOverGround = data.i("HEIGHT", 0, 50);
		hitboxSize = 11*C.SCALE;
		spriteOff = (24*C.SCALE - hitboxSize)/2;
		color = new ColorImp(data);
		resources = RESOURCES.map().readMany(data);
		resAmounts = data.ds("RESOURCE_AMOUNT", resources.size());
		
		RBITImp bb = new RBITImp();
		for (RESOURCE res : resources) {
			bb.or(res);
		}
		this.rBit = bb;
		
		BOOSTABLES.BATTLE().DAMAGE_COLL . new KJson(data) {
			
			@Override
			protected void process(BDamage s, Json j, String key, boolean isWeak) {
				damage[s.index()] = j.d(key, 0, 10000);
			}
		};
		

		CLIMATES.MAP();
		climates = CLIMATES.MAP().readFill(data, 1);
		terrains = TERRAINS.MAP().readFill(data, 1);
		pack = data.bool("PACK");
		grazes = data.bool("GRAZES");
		danger = data.d("DANGER", 0, 1);
		momTreshold = acceleration*massMin*1.5;
		momTresholdFly = acceleration*massMin*2.0;
		caveLiving = data.d("LIVES_IN_CAVES", 0, 1);
		
		sound = AUDIO.race("ANIMAL_CALL_" + key);
		String sKey = data.value("SPRITE");
		TILE_SHEET sheet;
		if (sprites.containsKey(sKey)) {
			sheet = sprites.get(sKey);
		}else {
			new ComposerThings.IInit(PATHS.SPRITE().getFolder("animal").get(sKey), 132, 366);
			
			sheet = new ComposerThings.ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 2, 12, d.s24);
					for (int i = 0; i < 12; i++) {
						s.singles.setSkip(i * 2, 2).paste(3, true);
					}
					return d.s24.saveGame();
				}
			}.get();
		}
		this.sheet = sheet;
	}

	public double occurence(CLIMATE c) {
		return climates[c.index()];
	}
	
	public double occurence(TERRAIN t) {
		return terrains[t.index()];
	}
	
	public double mass() {
		return massMin;
	}

	public double heightOverGround() {
		return heightOverGround;
	}

	public double acceleration() {
		return acceleration;
	}

	public int hitBoxSize() {
		return hitboxSize;
	}

	public int spriteOff() {
		return spriteOff;
	}

	@Override
	public int index() {
		return index;
	}
	
	public LIST<RESOURCE> resources(){
		return resources;
	}
	
	public int resAmount(int ri, double weight) {
		return (int)Math.ceil(resAmounts[ri]*weight*0.3);
	}

	@Override
	public String key() {
		return key;
	}
	
}
