package snake2d;



/**
 * Will anyone ever read this? If you do you're in luck. this is the single greatest feat in programming 
 * history. Here before you is a black and white three unlike anything you've ever seen.
 * It's directly stolen from oracle, but all fuzz has been stripped out, making it 50% faster.
 * 
 * 
 * @author mail__000
 *
 */
class RBTileTree{

    private PathTile root = null;
    private int size = 0;

    public RBTileTree() {

    }

    public int size() {
        return size;
    }
    
    public void put(PathTile key) {
        PathTile t = root;
        if (t == null) {
            initTile(key, null);
        	root = key;
            size = 1;
            return;
        }
        int cmp;
        PathTile parent;
        
        do {
            parent = t;
            cmp = key.compareTo(t);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else{
            	throw new RuntimeException("shitstorm");
            }
            
        } while (t != null);

        initTile(key, parent);
        if (cmp < 0)
            parent.left = key;
        else
            parent.right = key;
        fixAfterInsertion(key);
        size++;
        return;
    }

    private void initTile(PathTile t, PathTile parent){
    	t.left = null;
    	t.right = null;
        t.parent = parent;
        t.color = RBTileTree.BLACK;
    }
    
    public PathTile pollSmallest(){
    	PathTile t = getFirstEntry();
    	deleteEntry(t);
    	return t;
    }
    
    public PathTile pollGreatest(){
    	PathTile t = getLastEntry();
    	deleteEntry(t);
    	return t;
    }
    
    public PathTile smallest() {
        return getFirstEntry();
    }

    public PathTile greatest() {
        return getLastEntry();
    }
    
    public void remove(PathTile p) {
        if (p == null)
            return;
        deleteEntry(p);
    }

    public void clear() {
        size = 0;
        root = null;
    }

    // Red-black mechanics

    private static final boolean RED   = false;
    private static final boolean BLACK = true;
    
    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    private final PathTile getFirstEntry() {
        PathTile p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }

    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */

    private  final PathTile getLastEntry() {
    	PathTile p = root;
    	if (p != null)
    		while (p.right != null)
    			p = p.right;
    	return p;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    private PathTile successor(PathTile t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            PathTile p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            PathTile p = t.parent;
            PathTile ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     *
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version.  Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null.  They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */

    private static boolean colorOf(PathTile p) {
        return (p == null ? BLACK : p.color);
    }

    private static PathTile parentOf(PathTile p) {
        return (p == null ? null: p.parent);
    }

    private static void setColor(PathTile p, boolean c) {
        if (p != null)
            p.color = c;
    }

    private static  PathTile leftOf(PathTile p) {
        return (p == null) ? null: p.left;
    }

    private static PathTile rightOf(PathTile p) {
        return (p == null) ? null: p.right;
    }

    /** From CLR */
    private void rotateLeft(PathTile p) {
        if (p != null) {
        	PathTile r = p.right;
            p.right = r.left;
            if (r.left != null)
                r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null)
                root = r;
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            r.left = p;
            p.parent = r;
        }
    }

    /** From CLR */
    private void rotateRight(PathTile p) {
        if (p != null) {
        	PathTile l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;
        }
    }

    /** From CLR */
    private void fixAfterInsertion(PathTile x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            	PathTile y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
            	PathTile y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(PathTile p) {
        
        size--;

        if (size == 0){
        	root = null;
        	return;
        }
        	
        
        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
        	
        	PathTile s = successor (p);
        	switchLoc(s, p);
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        PathTile replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null)
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            // Fix replacement
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
        	if (p.color == BLACK)
                fixAfterDeletion(p);

            if (p.parent != null) {
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }

    private void switchLoc(PathTile a, PathTile b){
    	
    	//color
    	boolean ac = a.color;
    	a.color = b.color;
    	b.color = ac;
    	
    	if (a.parent == b && b != null){
    		PathTile lc = a.left;
    		PathTile rc = a.right;
    		
    		if (b.left == a){
    			a.left = b;
    			a.right = b.right;
    			a.right.parent = a;
    		}else{
    			a.right = b;
    			a.left = b.left;
    			a.left.parent = a;
    		}
    		
    		a.parent = b.parent;
    		
    		if (a.parent != null){
	    		if (a.parent.left == b)
	    			a.parent.left = a;
	    		else
	    			a.parent.right = a;
    		}
    		
    		b.parent = a;
    		
    		b.left = lc;
    		if (lc != null)
    			lc.parent = b;
    		b.right = rc;
    		if (rc != null)
    			rc.parent = b;
    	}else if(b.parent == a && b != null){
    		throw new RuntimeException("should not happen!");
    	}else{
        	//parent
        	if (b.parent != null){
        		if (b.parent.left == b){
        			b.parent.left = a;
        		}else{
        			b.parent.right = a;
        		}
        	}
        	if (a.parent != null){
        		if (a.parent.left == a){
        			a.parent.left = b;
        		}else{
        			a.parent.right = b;
        		}
        		
        	}
        	PathTile ap = a.parent;
        	a.parent = b.parent;
        	b.parent = ap;
        	
        	//childrens parents
        	if (a.left != null)
        		a.left.parent = b;
        	if (a.right != null)
        		a.right.parent = b;
        	if (b.left != null)
        		b.left.parent = a;
        	if (b.right != null)
        		b.right.parent = a;
        	
        	//children
        	PathTile al = a.left;
        	PathTile ar = a.right;
        	a.left = b.left;
        	a.right = b.right;
        	b.left = al;
        	b.right = ar;
    	}
    	
    	if (a == root){
    		root = b;
    	}else if (b == root){
    		root= a;
    	}

    }
    
    /** From CLR */
    private void fixAfterDeletion(PathTile x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
            	PathTile sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib))  == BLACK &&
                    colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
            	PathTile sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

}