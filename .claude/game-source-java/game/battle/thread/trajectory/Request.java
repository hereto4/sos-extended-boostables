package game.battle.thread.trajectory;

import java.io.IOException;
import java.util.Arrays;

import game.battle.div.Div;
import init.constant.Config;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.equip.EquipRange;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

final class Request implements SAVABLE{

	private short ammo = -1;
	private final float[] refs = new float[Config.battle().MEN_PER_DIVISION];
	private final int[] pixels = new int[Config.battle().MEN_PER_DIVISION*2];
	private final byte[] counts = new byte[Config.battle().MEN_PER_DIVISION]; 
	public Request() {
		Arrays.fill(refs, Float.NaN);
		Arrays.fill(pixels, -1);
	}
	
	public boolean request(int pos, Humanoid h, Div div) {
		
		EquipRange a = div.settings().ammo();
		if (a == null) {
			ammo = -1;
			return false;
		}
		
		counts[pos] = 0;
		
		if (a.tIndex != ammo) {
			ammo = a.tIndex;
			set(pos, (float) a.ref(h.indu()), h);
			return false;
		}
		
		int pi = pos*2;
		if (pixels[pi] != h.body().cX() || pixels[pi+1] != h.body().cY()) {
			set(pos, (float) a.ref(h.indu()), h);
			return false;
		}
		
		float ref =  (float) a.ref(h.indu());
		
		if (ref != refs[pos]) {
			set(pos, (float) a.ref(h.indu()), h);
			return false;
		}
		
		return true;
		
	}
	
	private void set(int pos, float ref, Humanoid h) {
		refs[pos] =  (float) ref;
		int pi = pos*2;
		pixels[pi] = h.body().cX();
		pixels[pi+1] = h.body().cY();
	}
	
	@Override
	public void save(FilePutter file) {
		file.s(ammo);
		file.fs(refs);
		file.is(pixels);
		file.bs(counts);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		ammo = file.s();
		file.fs(refs);
		file.is(pixels);
		file.bs(counts);
	}
	
	@Override
	public void clear() {
		ammo = -1;
		Arrays.fill(refs, 0);
		Arrays.fill(pixels, 0);
		Arrays.fill(counts, (byte)121);
	}
	
	public float ref(int i) {
		return refs[i];
	}
	
	public int x(int i) {
		return pixels[i*2];
	}
	
	public int y(int i) {
		return pixels[i*2+1];
	}
	
	public EquipRange ammo() {
		if (ammo == -1)
			return null;
		return STATS.EQUIP().RANGED().get(ammo);
	}
	
	public boolean count(int i) {
		if (counts[i] > 120)
			return false;
		counts[i] ++;
		return true;
	}
	
}
