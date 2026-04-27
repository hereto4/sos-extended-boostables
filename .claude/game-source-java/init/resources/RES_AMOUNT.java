package init.resources;

import java.io.Serializable;

import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public interface RES_AMOUNT {

	public RESOURCE resource();
	
	public int amount();
	
	public static LIST<RES_AMOUNT> make(Json json){
		LinkedList<RES_AMOUNT> li = new LinkedList<>();
		for (String k : json.keys()) {
			RESOURCE r = RESOURCES.map().tryGet(k);
			if (r != null) {
				li.add(new Imp(r, json.i(k)));
			}
		}
		return new ArrayList<RES_AMOUNT>(li);
	}
	
	public class Imp implements RES_AMOUNT, Serializable{

		private static final long serialVersionUID = 1L;
		private byte cIndex;
		private int amount;
		
		public Imp(){
			this((RESOURCE)null, 0);
		}
		
		public Imp(RESOURCE c){
			this(c, 0);
		}
		
		public Imp(RESOURCE c, int amount){
			if (c != null)
				cIndex = (byte) c.bIndex();
			this.amount = amount;
		}
		
		public Imp(RES_AMOUNT wa, float factor){
			cIndex = (byte) wa.resource().bIndex();
			this.amount = (int) (wa.amount()*factor);
		}
		
		@Override
		public RESOURCE resource(){
			return RESOURCES.ALL().get(cIndex);
		}
		
		@Override
		public int amount(){
			return amount;
		}
		
		public void add(int amount){
			this.amount += amount;
		}
		
		public void set(int amount) {
			this.amount = amount;
		}
		
		public Imp setResource(RESOURCE res) {
			this.cIndex = res.bIndex();
			return this;
		}
		
		public Imp setResource(RESOURCE res, int amount) {
			this.cIndex = res.bIndex();
			this.amount = amount;
			return this;
		}
		
	}
	
	public class Abs implements RES_AMOUNT{

		private final byte cIndex;
		private final int amount;
		
		public Abs(RESOURCE c, int amount){
			cIndex = (byte) c.bIndex();
			this.amount = amount;
		}
		
		@Override
		public RESOURCE resource(){
			return RESOURCES.ALL().get(cIndex);
		}
		
		@Override
		public int amount(){
			return amount;
		}
		
	}
	
}
