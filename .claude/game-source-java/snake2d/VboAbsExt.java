package snake2d;

abstract class VboAbsExt extends VboAbs{


    
    protected int count = 0;
    
	protected int[] vFrom = new int[255];
	protected int[] vTo = new int[255];
	protected int current = 0;
    
	VboAbsExt(int type, int maxElements, VboAttribute... attributes){
		super(type, maxElements, attributes);
		

		
	}
    
	@Override
    void clear(){
    	super.clear();
    	current = 0;
    	count = 0;
    }
	


}
