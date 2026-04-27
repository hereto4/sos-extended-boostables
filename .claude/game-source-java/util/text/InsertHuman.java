package util.text;

import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;

final class InsertHuman extends Inserter<Humanoid>{

	InsertHuman(){
		super();
		new II("TITLE") {
			@Override
			public void set(Humanoid t, Str str) {
				str.add(t.title());
			}
		}; 
		new II("WEIGHT") {
			@Override
			public void set(Humanoid a, Str str) {
				str.add(a.physics.getMass(), 1);
			}
		}; 
		new II("HEIGHT") {
			@Override
			public void set(Humanoid a, Str str) {
				str.add(a.physics.getHeight(), 1);
			}
		}; 
		new II("LOC") {
			@Override
			public void set(Humanoid b, Str str) {
				if (b != null) {
					DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, b.tc().x(), b.tc().y());
					if (COORDINATE.tileDistance(SETT.TWIDTH/2, SETT.THEIGHT/2, b.tc().x(), b.tc().y()) < 150)
						d = DIR.C;
					str.add(Dic.get(d));
				}
			}
		};
		
		join(new InsertIndu(), new GETTER_TRANS<Humanoid, Induvidual>(){

			@Override
			public Induvidual get(Humanoid f) {
				if (f == null)
					return null;
				return f.indu();
			}
			
		});
	}
	
}