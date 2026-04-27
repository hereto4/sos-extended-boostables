package init.race.bio;

import settlement.entity.humanoid.Humanoid;
import settlement.stats.stat.STAT;
import snake2d.util.file.Json;
import snake2d.util.sprite.text.Str;
import util.text.Dic;

public final class Opinion {

	private static final CharSequence[] dm = new CharSequence[] { Dic.¤¤More + ": {0}" };
	private static final CharSequence[] dl = new CharSequence[] { Dic.¤¤Less + ": {0}" };

	CharSequence[] more = dm;
	CharSequence[] less = dl;

	public static final Opinion DEF = new Opinion();

	public Opinion() {

	}

	Opinion(Json morg, Json mspe, Json lorg, Json lspe, String key) {

		more = get(morg, mspe, key, dm);
		less = get(lorg, lspe, key, dl);
		
	}

	private CharSequence[] get(Json org, Json spe, String key, CharSequence[] backup) {

		Json j = org;
		if (spe != null && spe.has(key))
			j = spe;

		if (j != null && j.has(key)) {
			CharSequence[] ss = j.texts(key);
			if (ss.length > 0)
				return ss;
		}
		return backup;
	}

	public Opinion setMore(CharSequence... more) {
		if (more == null || more.length == 0)
			this.more = dm;
		else
			this.more = more;
		return this;
	}

	public Opinion setLess(CharSequence... more) {
		
		
		
		if (more == null || more.length == 0)
			this.less = dl;
		else
			this.less = more;
		
		return this;
	}

	void insert(Str prep, STAT stat, Humanoid a) {
		prep.insert(0, stat.info().name);
		BioLine.insert.set(prep, a);
	}

	public void read(Json json) {
		if (json.has("MORE"))
			setMore(BioLine.insert.check(json.texts("MORE")));
		if (json.has("LESS"))
			setLess(BioLine.insert.check(json.texts("LESS")));
		
	}
}