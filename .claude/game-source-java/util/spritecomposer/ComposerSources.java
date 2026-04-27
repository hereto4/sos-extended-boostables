package util.spritecomposer;

import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import util.spritecomposer.ComposerDests.Dest;

public final class ComposerSources {

	public final House house;
	public final Full full;
	public final House house2;
	public final Singles singles;
	public final Full full2;
	public final FullCombo combo = new FullCombo() {

		@Override
		public Source setSkip(int maxAmount, int skipfirst) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	ComposerSources() {

		full = new Full(6);
		singles = new Singles();
		full2 = new Full(3);

		house = new House(new Body(2, 2)) {
			@Override
			void setSize(int size) {
				this.size = size;
				final int m = 2;
				offX[0] = 2 * m + 3 * size;
				offX[1] = 2 * m + 3 * size;
				offX[2] = 0;
				offX[3] = 0;
				offX[4] = 2 * m + 3 * size;
				offX[5] = 2 * m + 3 * size;
				offX[6] = 0;
				offX[7] = 0;
				offX[8] = 0 + 2 * size;
				offX[9] = 0 + 2 * size;
				offX[10] = 0 + 1 * size;
				offX[11] = 0 + 1 * size;
				offX[12] = 0 + 2 * size;
				offX[13] = 0 + 2 * size;
				offX[14] = 0 + 1 * size;
				offX[15] = 0 + 1 * size;

				offY[0] = 2 * m + 3 * size;
				offY[1] = 0 + 2 * size;
				offY[2] = 2 * m + 3 * size;
				offY[3] = 0 + 2 * size;
				offY[4] = 0;
				offY[5] = 0 + 1 * size;
				offY[6] = 0;
				offY[7] = 0 + 1 * size;
				offY[8] = 2 * m + 3 * size;
				offY[9] = 0 + 2 * size;
				offY[10] = 2 * m + 3 * size;
				offY[11] = 0 + 2 * size;
				offY[12] = 0;
				offY[13] = 0 + 1 * size;
				offY[14] = 0;
				offY[15] = 0 + 1 * size;
				width = m * 4 + 4 * size;
				height = m * 4 + 4 * size;
				body.init(0, 0, width, height, 1, 1);
			}
		};

		house2 = new House(new Body(4, 4)) {
			@Override
			void setSize(int size) {
				this.size = size;
				int m = 4;
				int m2 = 2;

				offX[0] = 0;
				offX[1] = 0 * (size + m2);
				offX[2] = 0 * (size + m2);
				offX[3] = 0 * (size + m2);
				offX[4] = 4 * (size + m2);
				offX[5] = 3 * (size + m2);
				offX[6] = 2 * (size + m2);
				offX[7] = 1 * (size + m2);
				offX[8] = 4 * (size + m2);
				offX[9] = 2 * (size + m2);
				offX[10] = 1 * (size + m2);
				offX[11] = 3 * (size + m2);
				offX[12] = 4 * (size + m2);
				offX[13] = 1 * (size + m2);
				offX[14] = 3 * (size + m2);
				offX[15] = 2 * (size + m2);

				offY[0] = 0;
				offY[1] = 1 * (size + m);
				offY[2] = 2 * (size + m);
				offY[3] = 0 * (size + m);
				offY[4] = 2 * (size + m);
				offY[5] = 1 * (size + m);
				offY[6] = 1 * (size + m);
				offY[7] = 2 * (size + m);
				offY[8] = 1 * (size + m);
				offY[9] = 0 * (size + m);
				offY[10] = 1 * (size + m);
				offY[11] = 0 * (size + m);
				offY[12] = 0 * (size + m);
				offY[13] = 0 * (size + m);
				offY[14] = 2 * (size + m);
				offY[15] = 2 * (size + m);
				width = m * 2 + m2 * 4 + 5 * size;
				height = m * 4 + 3 * size;
				body.init(0, 0, width, height, 1, 1);
			}
		};
	}

	public static abstract class Source implements BODY_HOLDER {

		// public abstract boolean next();

		int size;

		Source() {
		}

		abstract int height();

		abstract int width();

		abstract int x1();

		abstract int y1();

		public COLOR sample() {
			return ComposerThings.IColorSampler.save(Resources.c.sampleSource(x1(), y1()));
		}

	}

	private static class Body implements BODY_HOLDER {

		private final Rec body = new Rec();
		private final Rec allBody = new Rec();
		private final int marginLeft, marginTop;
		private int bodiesX, bodiesY;

		Body(int marginLeft, int marginTop) {
			this.marginLeft = marginLeft;
			this.marginTop = marginTop;
		}

		void init(int x, int y, int width, int height, int nrWidth, int nrHeight) {
			body.set(x, x + width, y, y + height);
			bodiesX = nrWidth;
			bodiesY = nrHeight;
			allBody.set(x, x + body.width() * nrWidth, y, y + body.height() * nrHeight);
			assert nrWidth > 0 && nrHeight > 0 && marginLeft >= 0 && marginTop >= 0 && x >= 0 && y >= 0 && width > 0
					&& height > 0;
		}

		void set(int nr) {
			int x = nr % bodiesX;
			int y = nr / bodiesX;
			if (y >= bodiesY)
				throw new RuntimeException("cant set var higher");
			body.moveX1Y1(allBody.x1() + x * body.width(), allBody.y1() + y * body.height());
		}

		int getStartX() {
			return body.x1() + marginLeft;
		}

		int getStartY() {
			return body.y1() + marginTop;
		}

		@Override
		public RECTANGLE body() {
			return allBody;
		}

	}

	public static abstract class Imp extends Source {

		public abstract boolean next();

		public abstract Source setVar(int var);

		public abstract Source setSkip(int maxAmount, int skipfirst);

		protected int size;
		private ComposerDests.Dest dest;

		public void paste(int dupRot, boolean setNext) {

			int i = 0;

			paste(setNext);

			for (int rot = 1; rot <= dupRot; rot++) {
				setRot(rot);
				do {
					Resources.c.copy(this);
					Resources.c.pasteRotated(dest, rot);
					dest.jump(1);
					i++;
				} while (!next());
			}

			if (!setNext) {
				dest.jump(-i);
			}
		}

		public void paste(boolean setNext) {
			int i = 0;

			setRot(0);
			do {
				Resources.c.copy(this);
				Resources.c.paste(dest);
				dest.jump(1);
				i++;
			} while (!next());

			if (!setNext) {
				dest.jump(-i);
			}

		}
		
		public void pasteOverBackground(boolean setNext, double blend) {
			int i = 0;

			setRot(0);
			do {
				Resources.c.copy(this);
				Resources.c.paste(dest, blend);
				dest.jump(1);
				i++;
			} while (!next());

			if (!setNext) {
				dest.jump(-i);
			}

		}

		public void pasteRotated(int rotation, boolean setNext) {

			if (rotation == 0) {
				paste(setNext);
				return;
			}
			
			int i = 0;

			setRot(rotation);
			do {
				Resources.c.copy(this);
				Resources.c.pasteRotated(dest, rotation);
				dest.jump(1);
				i++;
			} while (!next());

			if (!setNext) {
				dest.jump(-i);
			}
		}

		public void pasteStenciled(Imp stencil, int dupRot) {

			stencil.setRot(0);
			do {
				Resources.c.blendWithBackground(dest, stencil, this);
				Resources.c.paste(dest);
				dest.jump(1);
				next();
			} while (!stencil.next());

			for (int rot = 1; rot <= dupRot; rot++) {
				stencil.setRot(rot);
				do {
					Resources.c.blendWithBackground(dest, stencil, this);
					Resources.c.pasteRotated(dest, rot);
					dest.jump(1);
					next();
				} while (!stencil.next());
			}

		}

		public void pasteNormal(int dupRot, boolean setNext) {

			int i = 0;

			setRot(0);
			do {
				Resources.c.copy(this);
				Resources.c.pasteNormalOnly(dest, 0);
				dest.jump(1);
				i++;
			} while (!next());

			for (int rot = 1; rot <= dupRot; rot++) {
				setRot(rot);
				do {
					Resources.c.copy(this);
					Resources.c.pasteNormalOnly(dest, rot);
					dest.jump(1);
					i++;
				} while (!next());
			}

			if (!setNext) {
				dest.jump(-i);
			}
		}

		public void pasteEdges(boolean setNext) {
			dest.jump(1);
			Resources.c.copy(this);

			for (int i = 1; i < 16; i++) {

				if ((i & 0b0001) > 0)
					Resources.c.paste(dest);
				if ((i & 0b0010) > 0)
					Resources.c.pasteRotated(dest, 1);
				if ((i & 0b0100) > 0)
					Resources.c.pasteRotated(dest, 2);
				if ((i & 0b01000) > 0)
					Resources.c.pasteRotated(dest, 3);
				dest.jump(1);
			}
			if (!setNext) {
				dest.jump(-16);
			}
		}

		void setDest(ComposerDests.Dest dest) {
			this.dest = dest;
			this.size = dest.size();
		}

		abstract void setRot(int rot);

		@Override
		abstract int height();

		@Override
		abstract int width();

		@Override
		abstract int x1();

		@Override
		abstract int y1();

		public final void debug() {
			LOG.ln(x1() + " " + y1());
		}

	}

	public static class Full extends Imp {

		private int tilesX = 8;
		private final int m;
		private int tileStart, tileEnd;
		private int tileCurrent;
		private int offX, offY;
		private int width;
		private int tilesY;

		private final Body body;
		private int size = 0;

		private Full(int m) {
			this.m = m;
			body = new Body(m, m);
		}

		private void setSize(int size) {
			this.size = size;
			body.init(0, 0, width, m * 2, 1, 1);
		}

		public Full init(int x, int y, int width, int height, int tilesX, int tilesY, ComposerDests.Tile dest) {
			setSize(dest.size());
			this.tilesX = tilesX;
			this.width = m * 2 + tilesX * size;
			body.init(x, y, this.width, (tilesY * size + 2 * m), width, height);
			setVar(0);
			setSkip(0, tilesY * tilesX);
			this.tilesY = tilesY;
			setDest(dest);
			tileCurrent = 0;
			tileStart = 0;
			tileEnd = tilesX * tilesY;
			calc();
			return this;
		}

		@Override
		public Full setSkip(int maxAmount, int skipfirst) {
			tileStart = skipfirst;
			tileEnd = tileStart + maxAmount;
			assert tileEnd <= tilesX * tilesY && tileEnd > tileStart;
			tileCurrent = tileStart;
			calc();
			return this;
		}

		public Full setNextSingle() {
			setSkip(1, tileStart + 1);
			return this;
		}

		@Override
		public Full setVar(int var) {
			body.set(var);
			calc();
			return this;
		}

		private void calc() {
			offX = tileCurrent % tilesX;
			offY = tileCurrent / tilesX;
			offX *= size;
			offY *= size;
			offX += body.getStartX();
			offY += body.getStartY();
		}

		@Override
		public boolean next() {

			tileCurrent++;

			if (tileCurrent >= tileEnd) {
				tileCurrent = tileStart;
				calc();
				return true;
			}
			calc();
			return false;
		}

		@Override
		public RECTANGLE body() {
			return body.body();
		}

		@Override
		void setRot(int rot) {

		}

		@Override
		int height() {
			return size;
		}

		@Override
		int width() {
			return size;
		}

		@Override
		int x1() {
			return offX;
		}

		@Override
		int y1() {
			return offY;
		}

	}
	
//	private static class Full2 extends Imp {
//
//		private int tilesX = 8;
//		private final static int m = 3;
//		private int tileStart, tileEnd;
//		private int tileCurrent;
//		private int offX, offY;
//		private int width;
//		private int tilesY;
//
//		private final Body body = new Body(m, m);
//		private int size = 0;
//
//		private Full2() {
//
//		}
//
//		private void setSize(int size) {
//			this.size = size;
//			body.init(0, 0, width, m * 2, 1, 1);
//		}
//
//		public Full2 init(int x, int y, int width, int height, int tilesX, int tilesY, ComposerDests.Tile dest) {
//			setSize(dest.size());
//			this.tilesX = tilesX;
//			this.width = m * 2 + tilesX * size;
//			body.init(x, y, this.width, (tilesY * size + 2 * m), width, height);
//			setVar(0);
//			setSkip(0, tilesY * tilesX);
//			this.tilesY = tilesY;
//			setDest(dest);
//			tileCurrent = 0;
//			tileStart = 0;
//			tileEnd = tilesX * tilesY;
//			calc();
//			return this;
//		}
//
//		@Override
//		public Full2 setSkip(int maxAmount, int skipfirst) {
//			tileStart = skipfirst;
//			tileEnd = tileStart + maxAmount;
//			assert tileEnd <= tilesX * tilesY && tileEnd > tileStart;
//			tileCurrent = tileStart;
//			calc();
//			return this;
//		}
//
//		public Full2 setNextSingle() {
//			setSkip(1, tileStart + 1);
//			return this;
//		}
//
//		@Override
//		public Full2 setVar(int var) {
//			body.set(var);
//			calc();
//			return this;
//		}
//
//		private void calc() {
//			offX = tileCurrent % tilesX;
//			offY = tileCurrent / tilesX;
//			offX *= size;
//			offY *= size;
//			offX += body.getStartX();
//			offY += body.getStartY();
//		}
//
//		@Override
//		public boolean next() {
//
//			tileCurrent++;
//
//			if (tileCurrent >= tileEnd) {
//				tileCurrent = tileStart;
//				calc();
//				return true;
//			}
//			calc();
//			return false;
//		}
//
//		@Override
//		public RECTANGLE body() {
//			return body.body();
//		}
//
//		@Override
//		void setRot(int rot) {
//
//		}
//
//		@Override
//		int height() {
//			return size;
//		}
//
//		@Override
//		int width() {
//			return size;
//		}
//
//		@Override
//		int x1() {
//			return offX;
//		}
//
//		@Override
//		int y1() {
//			return offY;
//		}
//
//	}

	public static abstract class House extends Imp {

		protected final int[] offX = new int[16];
		protected final int[] offY = new int[16];
		protected int sx = 0, sy = 0;
		protected int tCurrent = 0;
		protected int tEnd = 16, tStart = 0;
		protected int rot = 0;
		protected int rotM = 0;

		protected int width, height;
		protected final Body body;

		protected int size = 0;

		private House(Body b) {
			this.body = b;
		}

		abstract void setSize(int size);

		public House init(int x, int y, int housesX, int housesY, Dest dest) {
			setSize(dest.size());
			body.init(x, y, width, height, housesX, housesY);
			setDest(dest);
			setVar(0);
			setSkip(0, 16);
			setRot(0);
			return this;
		}

		@Override
		public House setSkip(int start, int amount) {
			tStart = start;
			tEnd = tStart + amount;
			tCurrent = tStart;
			assert tStart >= 0 && tEnd <= 16 && tStart < tEnd;
			setRot(0);
			return this;
		}

		@Override
		public House setVar(int var) {
			body.set(var);
			setRot(0);
			tCurrent = tStart;
			rot = 0;
			rotM = 0;
			return this;
		}

		@Override
		public boolean next() {
			tCurrent++;
			if (tCurrent >= tEnd) {
				tCurrent = tStart;
				return true;
			}
			rotate();
			return false;
		}

		@Override
		void setRot(int rot) {
			this.rot = rot;
			rotate();
		}

		private void rotate() {
			rotM = tCurrent;
			for (int rotI = 0; rotI < rot; rotI++) {
				if ((rotM & 0b0001) == 0b0001)
					rotM = (rotM >> 1) | 0b1000;
				else
					rotM = rotM >> 1;
			}
			sx = body.getStartX() + offX[rotM];
			sy = body.getStartY() + offY[rotM];
		}

		@Override
		int height() {
			return size;
		}

		@Override
		int width() {
			return size;
		}

		@Override
		int x1() {
			return sx;
		}

		@Override
		int y1() {
			return sy;
		}

		@Override
		public RECTANGLE body() {
			return body.body();
		}

	}
	
	public static class Singles extends Imp {

		private int tilesX = 4;
		private final static int m = 6;
		private int pixelX = 0, pixelY = 0;
		private int tileStart, tileEnd;
		private int tileCurrent;

		private final Body body = new Body(m, m);

		private int width;
		private int tilesY;

		private Singles() {

		}

		@Override
		public Singles setVar(int var) {
			body.set(var);
			setSkip(0, tilesY * tilesX);
			calc();
			return this;
		}

		@Override
		public Singles setSkip(int start, int amount) {
			tileStart = start;
			tileEnd = tileStart + amount;
			tileCurrent = tileStart;
			calc();
			return this;
		}

		public Singles init(int x, int y, int width, int height, int tilesX, int tilesY, Dest dest) {
			this.tilesX = tilesX;
			this.tilesY = tilesY;
			setDest(dest);
			this.width = m + tilesX * (size + m);
			int h = m + tilesY * (size + m);
			body.init(x, y, this.width, h, width, height);
			setVar(0);
			return this;
		}

		private void calc() {
			int tx = tileCurrent % tilesX;
			int ty = tileCurrent / tilesX;
			pixelX = body.getStartX() + tx * (size + m);
			pixelY = body.getStartY() + ty * (size + m);
		}

		@Override
		public boolean next() {

			tileCurrent++;

			if (tileCurrent >= tileEnd) {
				tileCurrent = tileStart;
				calc();
				return true;
			}
			calc();
			return false;
		}

		@Override
		void setRot(int rot) {

		}

		@Override
		int height() {
			return size;
		}

		@Override
		int width() {
			return size;
		}

		@Override
		int x1() {
			return pixelX;
		}

		@Override
		int y1() {
			return pixelY;
		}

		@Override
		public RECTANGLE body() {
			return body.body();
		}

	}

	public static class FullCombo extends Imp {

		int tx;
		int ty;
		private int comboWidth;
		private int comboHeight;

		private int sx = 0, sy = 0;
		private int rot = 0;

		private final Body body = new Body(m, m);
		private final static int m = 6;

		private FullCombo() {
		}

		public FullCombo init(int x, int y, int combosX, int combosY, int comboSize, Dest dest) {

			setDest(dest);
			this.comboWidth = comboSize;
			this.comboHeight = comboSize;
			int width = 2 * m + comboSize * (this.size);
			body.init(x, y, width, width, combosX, combosY);
			setVar(0);
			tx = 0;
			ty = 0;
			setRot(0);
			return this;
		}

		public FullCombo init(int x, int y, int combosX, int combosY, int width, int height, Dest dest) {

			setDest(dest);
			this.comboWidth = width;
			this.comboHeight = height;
			body.init(x, y, 2 * m + width * (this.size), 2 * m + height * (this.size), combosX, combosY);
			setVar(0);
			tx = 0;
			ty = 0;
			setRot(0);
			return this;
		}

		@Override
		public FullCombo setVar(int var) {
			body.set(var);
			setRot(0);
			tx = 0;
			ty = 0;
			rot = 0;
			return this;
		}

		@Override
		public boolean next() {
			tx++;
			if (tx >= comboWidth) {
				tx = 0;
				ty++;
				if (ty >= comboHeight) {
					ty = 0;
					return true;
				}
			}
			rotate();
			return false;
		}

		@Override
		void setRot(int rot) {
			this.rot = rot;
			rotate();
		}

		private void rotate() {

			int x = tx;
			int y = ty;

			if ((rot & 0b01) > 0) {

				int i = x + y * comboWidth;

				x = i % comboHeight;
				y = i / comboHeight;

				int oldX = x;
				x = y;
				y = comboHeight - oldX - 1;

			}
			
			if ((rot & 0b10)>0) {
				
				y = comboHeight-y-1;
				x = comboWidth-x-1;
				
			}
			
			sx = body.getStartX() + x * size;
			sy = body.getStartY() + y * size;

		}

		@Override
		int height() {
			return size;
		}

		@Override
		int width() {
			return size;
		}

		@Override
		int x1() {
			return sx;
		}

		@Override
		int y1() {
			return sy;
		}

		@Override
		public RECTANGLE body() {
			return body.body();
		}

		@Override
		public Source setSkip(int maxAmount, int skipfirst) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
