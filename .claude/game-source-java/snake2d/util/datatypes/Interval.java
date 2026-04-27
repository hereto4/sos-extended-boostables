package snake2d.util.datatypes;

public class Interval {

	private int[] x1;
	private int[] x2;
	
	public Interval(){
	}
	
	public void add(int a, int b){
		
		if (x1 == null){
			x1 = new int[]{a};
			x2 = new int[]{b};
			return;
		}
		int[] newX1 = new int[x1.length+1];
		int[] newX2 = new int[x2.length+1];
		int count = 0;
		
		for (int i = 0; i < newX1.length-1; i++){
			if ((a <= x2[i] && b >= x1[i])){
				a = a < x1[i] ? a : x1[i];
				b = b > x2[i] ? b : x2[i];
				continue;
			}
			newX1[count] = x1[i];
			newX2[count] = x2[i];
			count++;
		}
		
		newX1[count] = a;
		newX2[count] = b;
		
		x1 = new int[count+1];
		x2 = new int[count+1];
		
		for (int i = 0; i < x1.length; i++){
			x1[i] = newX1[i];
			x2[i] = newX2[i];
		}
		
	}
	
	public boolean holds(int x){
		
		if (x1 == null)
			return false;
		
		for (int i = 0; i < x1.length; i++){
			if (x >= x1[i] && x < x2[i])
				return true;
		}
		return false;
		
		
	}

	public boolean isEmpty() {
		return x1 == null;
	}
	
	public void clear(){
		x1 = null;
	}
	
}
