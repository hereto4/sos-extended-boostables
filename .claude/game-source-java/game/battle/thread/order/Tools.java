package game.battle.thread.order;

import java.util.Arrays;

import game.battle.formation.DivDeployer;
import game.battle.formation.DivFormation;
import game.battle.thread.order.BattleOrderUpdater.PlanData;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.CircleCooIterator;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;

class Tools {

	public final PathUtilOnline pather = new PathUtilOnline(SETT.TWIDTH);
	public final DivDeployer deployer = new DivDeployer(pather);
	public final PathCost pathCost = new PathCost();
	public final CircleCooIterator circle = new CircleCooIterator(25, pather.getFlooder());
	
	public final ToolMover mover = new ToolMover(pather);
	public final ToolsDiv div = new ToolsDiv(this);
	public final ToolsWalk walk = new ToolsWalk(this);
	public final Arranger arranger = new Arranger();
	public final Columns columns = new Columns();
	
	Tools(PlanData[] all){
		
	}
	
	public int[] arrageFromFront(DivFormation f) {
		return arranger.getArrangedPointsForward(f);
	}
	
	
	public LIST<Pos> getPosColumnSort(DivFormation f) {
		return columns.sortByColumnRow(f);
	}
	
	public LIST<Pos> getPosRowsSort(DivFormation f) {
		return columns.sortByRow(f);
	}
	
	private static class Arranger {
		
		private final Tree<Point> tree = new Tree<Point>(Config.battle().MEN_PER_DIVISION) {
			
			@Override
			protected boolean isGreaterThan(Point current, Point cmp) {
				return current.value > cmp.value;
			}
		};
		private final Point[] points = new Point[Config.battle().MEN_PER_DIVISION];
		private int[] arranged = new int[Config.battle().MEN_PER_DIVISION];
		
		private Arranger() {
			for (int i = 0; i < points.length; i++)
				points[i] = new Point();
		}
		
		public int[] getArrangedPointsForward(DivFormation f) {
			tree.clear();
			Arrays.fill(arranged,0);
			double lineX1 = f.start().x();
			double lineY1 = f.start().y();
			double lineDirX = f.dx();
			double lineDirY = f.dy();
			
			for (int i = 0; i < f.deployed(); i++) {
				Point p = points[i];
				p.index = i;
				p.value = calculateDistanceToLine(f.px(i), f.py(i), lineX1, lineY1, lineDirX, lineDirY);
				tree.add(p);
			}
			
			int i = 0;
			while(tree.hasMore()) {
				Point p = tree.pollSmallest();
				arranged[i++] = p.index;
			}
			return arranged;
			
		}
		
	    private static double calculateDistanceToLine(double pointX, double pointY, 
	            double lineX1, double lineY1, 
	            double lineDirX, double lineDirY) {
			// Line equation coefficients (Ax + By + C = 0)
			double A = lineDirY; // Coefficient of x
			double B = -lineDirX; // Coefficient of y
			double C = -(A * lineX1 + B * lineY1); // Constant term
			
			// Distance formula
			return Math.abs(A * pointX + B * pointY + C) / Math.sqrt(A * A + B * B);
	    }
	    
	    private static class Point {
	    	
	    	int index;
	    	double value;
	    	
	    }
		
	}
	
	private static class Columns {
		
		private final VectorImp vec = new VectorImp();
		private final ArrayList<Pos> all = new ArrayList<Pos>(Config.battle().MEN_PER_DIVISION);
		private final ArrayList<Pos> res = new ArrayList<Pos>(Config.battle().MEN_PER_DIVISION);
		
		private final Tree<Pos> tree = new Tree<Pos>(Config.battle().MEN_PER_DIVISION) {
			
			@Override
			protected boolean isGreaterThan(Pos current, Pos cmp) {
				return current.value > cmp.value;
			}
		};
		
		private Columns() {
			while(all.hasRoom())
				all.add(new Pos(all.size()));
			
		}
		
		
		private ArrayList<Pos> cols(DivFormation f){
			res.clearSloppy();
			
			
			vec.set(f.dx(), f.dy());
			vec.rotate90().rotate90().rotate90();
			double dx = vec.nX();
			double dy = vec.nY();
			
			double minRow = Double.MAX_VALUE;
			double minCol = Double.MAX_VALUE;
			for (int i = 0; i < f.deployed(); i++) {
							
				Pos p = all.get(i);
				double px = f.px(i);
				double py = f.py(i);
				
				double x = px;
				double y = py;
				
				p.rowI = (int) Math.round(dx*x +dy*y);
				p.columnI = (int) Math.round(-dy*x + dx*y);
				minRow = Math.min(p.rowI, minRow);
				minCol = Math.min(minCol, p.columnI);
				res.add(p);
			
			}
			for (Pos p : res) {
				p.columnI -= minCol;
				p.rowI -= minRow;
				
			}
			
			return res;
		}
		
		public ArrayList<Pos> sortByRow(DivFormation f){
			cols(f);
			tree.clear();
			for (Pos p : res) {
				p.value = -p.rowI;
				tree.add(p);
			}
			res.clearSloppy();
			while(tree.hasMore()) {
				res.add(tree.pollGreatest());
			}
			return res;
			
		}
		
		public ArrayList<Pos> sortByColumnRow(DivFormation f){
			cols(f);
			tree.clear();
			for (Pos p : res) {
				p.value = p.columnI*C.TILE_SIZE*200 + p.rowI;
				tree.add(p);
			}
			res.clearSloppy();
			while(tree.hasMore()) {
				res.add(tree.pollGreatest());
			}
			return res;
			
		}
		
	}
	
	static class Pos {
		
		public int columnI;
		public int rowI;
		public final int pos;
		private double value;
		
		private Pos(int i) {
			this.pos = i;
		}
	}
	
}
