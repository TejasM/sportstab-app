package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.MotionEvent;

public class Sprite implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5337841439724840342L;
	public static final int FREE = 0;
	public static final int BLOCK = 1;
	public static final int LOCK = 2;
	private static final String TAG = "Sprite";
	//private static final int ALIVE = 5;
	private boolean moving;
	private static final int NUM_PATHS = 10;
	private transient GameView gameView;
	private transient EditView editView;
	private transient ButtonView buttonView;
	private float x = 50;
	private float y = 50;
	private float lastX;
	private float lastY;
	private transient Bitmap bmp;
	private transient Bitmap block;
	private transient Bitmap baseBmp;
	private transient Bitmap baseBlock;
	private float r;
	private float[] baseR = new float[GameView.BACKGROUND_NUM];/* what it is initially, (view.background.getWidth()/10)/2 */
	private float[] baseBlockWidth = new float[GameView.BACKGROUND_NUM];
	private float[] baseBlockHeight = new float[GameView.BACKGROUND_NUM];
	private MyPointF delta_down_ref = new MyPointF(0, 0); /* = (the old x,y) - (the first down) */
	/*Player's move*/
	private transient List<MyPath> paths;
	int cur_path = 0;
	/*ball handling */
	private transient Ball ball;
	private boolean hasBall = false;

	private int state;
	private float angle;
	private transient Matrix matrix;

	private int team_color;
	private String name;

	private boolean stuck;
	private float step = 15;
	private transient float[] squeezePoints = new float[4];

	private transient CoachingTab activity;

	private MyPointF[] pos_backup = new MyPointF[GameView.BACKGROUND_NUM];
	private int[] state_backup = new int[GameView.BACKGROUND_NUM];
	private transient Matrix[] matrix_backup = new Matrix[GameView.BACKGROUND_NUM];

	private float[] matrix_data = new float[9];
	private float[] matrix_backup0_data = new float[9];
	private float[] matrix_backup1_data = new float[9];

	private MyPointF screenDown;
	private MyPointF screenUp;

	private Typeface font;

	/*public Sprite(float x, float y, float r){
		this.x = x;
		this.y = y;
		this.r = r;
	}*/

	public Sprite(Sprite copy) {	/* this is only to construct a storable copy of sprite */
		x = copy.getX();
		y = copy.getY();
		r = copy.getR();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			baseR[i] = copy.baseR[i];
			baseBlockWidth[i] = copy.baseBlockWidth[i];
			baseBlockHeight[i] = copy.baseBlockHeight[i];
			pos_backup[i] = new MyPointF(copy.pos_backup[i].x, copy.pos_backup[i].y);
			state_backup[i] = copy.state_backup[i];

		}
		hasBall = copy.hasBall;
		state = copy.state;
		team_color = copy.team_color;
		name = copy.name;
		moving = false;

		copy.matrix.getValues(matrix_data);
		copy.matrix_backup[0].getValues(matrix_backup0_data);
		copy.matrix_backup[1].getValues(matrix_backup1_data);

	}

	public Sprite(Sprite copy, GameView gameView, CoachingTab activity, Bitmap baseBmp, Bitmap baseBlock) {	/* when selecting play, update the sprites with the saved ones */
		x = copy.getX();
		y = copy.getY();
		r = copy.getR();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			baseR[i] = copy.baseR[i];
			baseBlockWidth[i] = copy.baseBlockWidth[i];
			baseBlockHeight[i] = copy.baseBlockHeight[i];
			pos_backup[i] = new MyPointF(copy.pos_backup[i].x, copy.pos_backup[i].y);
			state_backup[i] = copy.state_backup[i];
			matrix_backup[i] = new Matrix();

		}
		hasBall = copy.hasBall;
		state = copy.state;
		team_color = copy.team_color;
		name = copy.name;
		moving = false;

		matrix = new Matrix();
		matrix.setValues(copy.matrix_data);
		matrix_backup[0].setValues(copy.matrix_backup0_data);
		matrix_backup[1].setValues(copy.matrix_backup1_data);

		this.gameView = gameView;
		this.activity = activity;

		ball = gameView.thread.ball;
		this.baseBmp = Bitmap.createBitmap(baseBmp);
		this.baseBlock = Bitmap.createBitmap(baseBlock);
		paths = new ArrayList<MyPath>();
		/* alloc all the paths */
		for (int i = NUM_PATHS - 1; i >= 0; i--) {
			paths.add(new MyPath(activity));
		}

		this.resizePlayer(r * 100 / baseR[gameView.background_index]);


	}

	public Sprite(GameView gameView, Bitmap bmp, CoachingTab activity) {
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			pos_backup[i] = new MyPointF(0, 0);
			state_backup[i] = FREE;
			matrix_backup[i] = new Matrix();
		}

		this.activity = activity;
		this.gameView = gameView;
		this.bmp = bmp;
		this.baseBmp = bmp;
		this.r = bmp.getWidth() / 2;
		this.x = -100;
		this.y = -100;
		this.state = FREE;
		this.matrix = new Matrix();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			this.baseR[i] = (gameView.background[i].getWidth() / 10) / 2;
			this.baseBlockWidth[i] = gameView.background[i].getWidth() / 5;
			this.baseBlockHeight[i] = gameView.background[i].getWidth() / 12;
		}
		createBlock(false); // Not button


		paths = new ArrayList<MyPath>();
		/* alloc all the paths */
		for (int i = NUM_PATHS - 1; i >= 0; i--) {
			paths.add(new MyPath(activity));
		}
		moving = false;
	}

	public Sprite(ButtonView buttonView, Bitmap bmp, CoachingTab activity, Typeface font) {
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			pos_backup[i] = new MyPointF(0, 0);
			state_backup[i] = FREE;
			matrix_backup[i] = new Matrix();
		}

		this.activity = activity;
		this.buttonView = buttonView;
		this.gameView = activity.getGameView();
		this.bmp = bmp;
		this.baseBmp = bmp;
		this.font = font;
		this.r = bmp.getWidth() / 2;
		this.x = -100;
		this.y = -100;
		this.state = FREE;
		this.matrix = new Matrix();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			this.baseR[i] = (gameView.background[i].getWidth() / 10) / 2;
			this.baseBlockWidth[i] = gameView.background[i].getWidth() / 5;
			this.baseBlockHeight[i] = gameView.background[i].getWidth() / 12;
		}
		createBlock(true); // isButton


		paths = new ArrayList<MyPath>();
		/* alloc all the paths */
		for (int i = NUM_PATHS - 1; i >= 0; i--) {
			paths.add(new MyPath(activity));
		}
		moving = false;
	}

	public Sprite(EditView editView, Bitmap bmp, CoachingTab activity) {
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			pos_backup[i] = new MyPointF(0, 0);
			state_backup[i] = FREE;
			matrix_backup[i] = new Matrix();
		}

		this.activity = activity;
		this.editView = editView;
		this.gameView = activity.getGameView();
		this.bmp = bmp;
		this.baseBmp = bmp;
		this.r = bmp.getWidth() / 2;
		this.x = -100;
		this.y = -100;
		this.state = FREE;
		this.matrix = new Matrix();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			this.baseR[i] = (gameView.background[i].getWidth() / 10) / 2;
			this.baseBlockWidth[i] = gameView.background[i].getWidth() / 5;
			this.baseBlockHeight[i] = gameView.background[i].getWidth() / 12;
		}
		createBlock(false); // Not button


		paths = new ArrayList<MyPath>();
		/* alloc all the paths */
		for (int i = NUM_PATHS - 1; i >= 0; i--) {
			paths.add(new MyPath(activity));
		}
		moving = false;
	}

	public void initBall(Ball ball) {
		this.ball = ball;
	}

	public void setBall(boolean has) {
		hasBall = has;
		if (has)
			ball.updateByPlayer(x, y);
	}

	public Bitmap getBitmap() {
		return bmp;
	}

	public Bitmap getBaseBitmap() {
		return baseBmp;
	}

	public Bitmap getBlockBitmap() {
		return block;
	}

	public Bitmap getBaseBlockBitmap() {
		return baseBlock;
	}

	public boolean getHasBall() {
		return hasBall;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getR() {
		return r;
	}

	public boolean getMoving() {
		return moving;
	}

	public float getBaseR() {
		return baseR[gameView.background_index];
	}

	public float getWidth() {
		if (state != BLOCK) {
			return r;
		} else {
			return block.getWidth();
		}
	}

	public float getHeight() {
		if (state != BLOCK) {
			return r;
		} else {
			return block.getHeight();
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTeamColor() {
		return team_color;
	}

	public void setStuck(boolean s) {
		stuck = s;
	}

	public void setTeamColor(int color) {
		team_color = color;

		for (int x = 0; x < bmp.getWidth(); x++) {
			for (int y = 0; y < bmp.getHeight(); y++) {
				double dXSquared = Math.pow(x - bmp.getWidth() / 2, 2);
				double dYSquared = Math.pow(y - bmp.getWidth() / 2, 2);
				double dR = Math.sqrt(dYSquared + dXSquared);
				if (/*(bmp.getWidth()/2 * 0.8 < dR) &&*/ (dR < bmp.getWidth() / 2)) {
					bmp.setPixel(x, y, color);
				}
			}
		}
		if (name != null)
			this.setName(name, false);//otherwise the name is overwritten by the new color
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(Matrix m) {
		matrix = new Matrix(m);
	}

	public String getName() {
		return name;
	}

	public void setName(String n, boolean erase) {
		name = n;
		if (erase)
			eraseName();
		paintName();
	}

	public int determineMaxTextSize(String str, float maxWidth) {
		if ((str == null) || (str.trim().length() == 0))
			return 0;
		int size = 0;
		if (str.length() < 2) {
			str = "a";
		}
		Paint paint = new Paint();
		Rect bounds = new Rect();

		do {
			paint.setTextSize(++size);
			paint.getTextBounds(str, 0, str.length(), bounds);
			//} while(paint.measureText(str) < maxWidth * 0.8); /* here the 0.8 is just so that the text are a bit smaller */
		} while ((bounds.height() < maxWidth * 0.8) && (bounds.width() < maxWidth * 0.8));
		return size;
	}

	public void eraseName() {
		/* erasing the previous name and shit */
		/*Canvas canvas = new Canvas(bmp);
		Paint paint1 = new Paint();
		paint1.setColor(Color.WHITE);
		canvas.drawCircle((float)bmp.getWidth()/2, (float)bmp.getWidth()/2, (float)(bmp.getWidth()/2 * 0.8), paint1);*/
	}

	public void paintName() {
		/* drawing out the new name */
		Canvas c = new Canvas(bmp);
		if (buttonView != null) {
			bmp.eraseColor(Color.TRANSPARENT);
		}
		Paint paint = new Paint();
		if (buttonView != null) {
			paint.setColor(Color.rgb(215, 108, 34));
		} else {
			paint.setColor(Color.BLACK);
		}
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(determineMaxTextSize(name, (float) (bmp.getWidth() * 0.6)));
		Rect bounds = new Rect();
		paint.setTypeface(font);
		paint.getTextBounds("a", 0, "a".length(), bounds);
		if (name != null)
			c.drawText(name, bmp.getWidth() / 2, bmp.getWidth() / 2 + bounds.height() / 2, paint);
	}

	public void initByCourt(float[] x1, float[] y1) {
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			pos_backup[i].x = x1[i];
			pos_backup[i].y = y1[i];
		}
	}

	public void initButtonMatrix() {
		this.matrix.reset();
		this.matrix.preTranslate((this.x - this.baseBmp.getWidth() / 2), (float) (this.y - this.baseBmp.getHeight() / 2));
	}

	public void initButtonMatrixByCourt() {
		//set matrix of button disabled state to the same as its (X,Y)
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			matrix_backup[i].preTranslate((pos_backup[i].x - baseBlockWidth[i] / 2), (float) (pos_backup[i].y - baseBlockHeight[i] / 2));
		}
	}

	public void setPosBackup(float x, float y, int index) {
		pos_backup[index].x = x;
		pos_backup[index].y = y;
	}

	public void setPosByCourt(int index) {
		x = pos_backup[index].x;
		y = pos_backup[index].y;
	}

	public void setMatrixByCourt(int index) {
		matrix = matrix_backup[index];
	}

	public void init(float x1, float y1) {
		x = x1;
		y = y1;
	}

	public void simpleUpdate(float x1, float y1) {
		x = x1;
		y = y1;
	}

	public void update(float x1, float y1) {
		lastX = x;
		lastY = y;
		/* Updating the center coordinates of bitmap */
		if (!moving)/* First being moved */ {
			Log.d(TAG, "update (): First move!");
			/* move the first empty Path in paths to the current center of the player */
			for (int i = NUM_PATHS - 1; i >= 0; i--) {
				MyPath path = paths.get(i);
				if (path.IsEmpty()) {
					path.UpdateByFirstMove(x, y);
					cur_path = i;
					break;
				}
			}
			if (gameView.getRecorder().replay) {/* we just pretend that the 'finger' is placed at the center of the player*/
				SetMoveRef(0, 0);
				x = x1;
				y = y1;
			} else {
				SetMoveRef(x - x1, y - y1);
			}
		} else /*In the middle of moving*/ {
			if ((stuck) && playerTryingToSqueezeThru((x1 + delta_down_ref.x), (y1 + delta_down_ref.y))) { /* too big of a leap, you're going thru player or something */
				return;
			} else {
				x = (x1 + delta_down_ref.x);
				y = (y1 + delta_down_ref.y);
				//down_ref.x = x1;
				//down_ref.y = y1;
				/* update the current path */
				stuck = false;
			}
		}
		/* done updating */
		/* update ball if this guy has it */


	}

	public boolean justCrossed(float x) {
		return (this.x - x) * (this.lastX - x) < 0;
	}

	public void updateBall() {
		ball.updateByPlayer(x, y);
	}

	private boolean playerTryingToSqueezeThru(float newX, float newY) {
		float x1 = squeezePoints[0];
		float y1 = squeezePoints[1];
		float x2 = squeezePoints[2];
		float y2 = squeezePoints[3];
		double arg = Math.atan((y2 - y1) / (x2 - x1));
		Matrix m = new Matrix();
		float degree = (float) (180 * arg / Math.PI);
		m.setRotate(0 - degree, x1, y1);
		//m.invert(m);
		float l[] = {x, y, newX, newY};
		m.mapPoints(l);
		if (((l[1] - y1) * (l[3] - y1)) > 0)/* only if one is one each side of the midline will make the product negative */
			return false;
		else
			return true;
	}

	public void updatePath() {
		paths.get(cur_path).UpdateByMove(x, y);
	}

	public void onDraw(Canvas canvas) {
		/* draw player */
		if (state != BLOCK) {
			canvas.drawBitmap(bmp, (float) (x - r), (float) (y - r), null);
		} else {
			//matrix.preTranslate((x - block.getWidth()/2),(float)(y - block.getHeight()/2));
			//matrix.postRotate((float) (360 * (Math.PI - angle) / Math.PI), x, y);
			canvas.drawBitmap(block, matrix, null);
			//matrix.reset();
		}
	}

	@SuppressLint("WrongCall")
	public void onDrawPath(Canvas canvas, boolean count_down) {
		/* draw all the non-empty paths associated with this player */
		for (int i = NUM_PATHS - 1; i >= 0; i--) {
			MyPath path = paths.get(i);
			if (i != cur_path)
				path.onDraw(canvas, true);
			else
				path.onDraw(canvas, !moving && count_down);
		}
	}

	private void playerDoneMoving() {
		Log.d(TAG, "playerDoneMoving ():");
		SetMoveRef(0, 0);
		moving = false;
	}

	public boolean isTouched(float x2, float y2) {
		float dX = this.x - x2;
		float dY = this.y - y2;
		double curDist = Math.sqrt(dX * dX + dY * dY);
		return curDist < (this.r * 1.5);//Jackson: the x1.5 is just the make the touchbox a lil bigger, i might want to adjust according to different devices.
	}

	public void positionBasedOnArg(Sprite s1) {
		double reference = (double) ((this.r + s1.getR()) * gameView.playerSizeModifier);
		positionBasedOnArgHelp(s1.getX(), s1.getY(), reference);
	}

	private void positionBasedOnArgHelp(float x1, float y1, double reference) {
		float dX = lastX - x1;
		float dY = lastY - y1;
		double curDist = Math.sqrt(dX * dX + dY * dY);
		if (curDist != 0) {
			float x2 = (float) (dX * (reference / curDist)) + x1;
			float y2 = (float) (dY * (reference / curDist)) + y1;

			float arg = (float) Math.atan((y2 - y1) / (x2 - x1));
			Matrix m = new Matrix();
			m.postRotate((float) (180 * arg / Math.PI), x1, y1);
			m.invert(m);
			float l[] = {x, y, x2, y2};
			m.mapPoints(l);
			float case1 = l[3] + 5;
			float case2 = l[3] - 5;
			m.invert(m);
			float dX1, dY1, x3, y3;
			double curDist1;
			if (Math.abs(case1 - l[1]) < Math.abs(case2 - l[1])) {
				dX1 = l[2] - x1;
				dY1 = case1 - y1;
			} else {
				dX1 = l[2] - x1;
				dY1 = case2 - y1;

			}
			curDist1 = Math.sqrt(dX1 * dX1 + dY1 * dY1);
			if (curDist1 != 0) {
				x3 = (float) (dX1 * (reference / curDist1)) + x1;
				y3 = (float) (dY1 * (reference / curDist1)) + y1;
				float l1[] = {x3, y3};
				m.mapPoints(l1);
				x = l1[0];
				y = l1[1];
			}

			/*
			boolean positive = ((y2 - y1) > 0);
			float arg = (float) Math.atan((y2 - y1)/(x2 - x1));
			float case1 = arg++;
			float case2 = arg--;
			float x3, y3, x4, y4;
			if (positive){
				x3 = (float)(Math.cos(case1)*(reference))+x1;
				y3 = (float)(Math.sin(case1)*(reference))+y1;				
				x4 = (float)(Math.cos(case2)*(reference))+x1;
				y4 = (float)(Math.sin(case2)*(reference))+y1;				
			}else{
				x3 = (float)(0 - Math.cos(case1)*(reference))+x1;
				y3 = (float)(0 - Math.sin(case1)*(reference))+y1;
				x4 = (float)(0 - Math.cos(case2)*(reference))+x1;
				y4 = (float)(0 - Math.sin(case2)*(reference))+y1;
			}
			if ((Math.pow(x3 - x, 2) + Math.pow(y3 - y, 2)) < (Math.pow(x4 - x, 2) + Math.pow(y4 - y, 2))){
				x = x3;
				y = y3;
			}else{
				x = x4;
				y = y4;
			}
			 */

		}
		/*if (curDist!=0){
			float x2 = (float)((double)dX*(reference/curDist))+x1;
			float y2 = (float)((double)dY*(reference/curDist))+y1;
			if ((Math.abs(x2 - lastX) > step)||(Math.abs(y2 - lastY) > step)){/* too big of a leap, you're going thru player or something 
				x = lastX;
				y = lastY;
			}
			else{
				this.x = x2;
				this.y = y2;
				stuck = false;
			}
		}*/
	}

	private double heron(float s1, float s2, float s3) {
		float s = (s1 + s2 + s3) / 2;
		return Math.sqrt(s * (s - s1) * (s - s2) * (s - s3));
	}

	public void positionBasedOnArgHelp(float x1, float y1, float c1, float x2, float y2, float c2) {
		/* the second set is always gonna be the block for now, or does it matter? */
		double dD = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		double newdR = heron((c1 + r) * gameView.playerSizeModifier, (c2 + r) * gameView.playerSizeModifier, (float) dD) * 2 / dD;
		double dR = Math.sqrt(Math.pow(((c1 + r) * gameView.playerSizeModifier), 2) - newdR * newdR);
		double refX = dR / dD * (x2 - x1) + x1;
		double dX = Math.abs(x1 - refX);
		double refY = dR / dD * (y2 - y1) + y1;
		double dY = Math.abs(y1 - refY);
		//double dR   = Math.sqrt(dX*dX+dY*dY);
		//double newdR = Math.sqrt(Math.pow(s1.getR()+s2.getR(),2)-dR*dR);
		double newdX = dY * (newdR / dR);
		double newdY = dX * (newdR / dR);
		double newX, newY, newX1, newY1;
		Log.d(TAG, "dD: " + dD + "newdR: " + newdR + "dR" + dR + "refX" + refX + "dX" + dX + "refY" + refY + "dY" + dY + "newdX" + newdX + "newdY" + newdY);
		boolean down_right = ((x1 - x2 > 0) != (y1 - y2 > 0));
		if (down_right) {
			newX = refX + newdX;
			newX1 = refX - newdX;
			newY = refY + newdY;
			newY1 = refY - newdY;
		} else {
			newX = refX + newdX;
			newX1 = refX - newdX;
			newY = refY - newdY;
			newY1 = refY + newdY;
		}
		if (Math.pow(newX - this.x, 2) + Math.pow(newY - this.y, 2) < Math.pow(newX1 - this.x, 2) + Math.pow(newY1 - this.y, 2)) {
			if ((Math.abs(newX - lastX) > step) || (Math.abs(newY - lastY) > step)) {/* too big of a leap, you're going thru player or something */
				x = lastX;
				y = lastY;
			} else {
				this.x = (int) newX;
				this.y = (int) newY;
				//stuck = false;
			}
		} else {
			if ((Math.abs(newX1 - lastX) > step) || (Math.abs(newY1 - lastY) > step)) { /* too big of a leap, you're going thru player or something */
				x = lastX;
				y = lastY;
			} else {
				this.x = (int) newX1;
				this.y = (int) newY1;
				//stuck = false;
			}
		}
		/* take down these two points */
		squeezePoints[0] = x1;
		squeezePoints[1] = y1;
		squeezePoints[2] = x2;
		squeezePoints[3] = y2;
	}

	public void positionBasedOnArg(Sprite s1, Sprite s2) {
		positionBasedOnArgHelp(s1.getX(), s1.getY(), s1.getR(), s2.getX(), s2.getY(), s2.getR());
		return;
	}

	public void SetMoveRef(float x1, float y1) {
		/* mark the reference point */
		delta_down_ref.x = x1;
		delta_down_ref.y = y1;
		/* start moving */
		moving = true;
	}

	public void TouchUpCback() {
		playerDoneMoving();
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		for (int x = 0; x < bmp.getWidth(); x++) {
			for (int y = 0; y < bmp.getHeight(); y++) {
				double dXSquared = Math.pow(x - bmp.getWidth() / 2, 2);
				double dYSquared = Math.pow(y - bmp.getWidth() / 2, 2);
				double dR = Math.sqrt(dYSquared + dXSquared);
				if ((bmp.getWidth() / 2 * 0.6 < dR) && (dR < bmp.getWidth() / 2 * 0.8)) {
					bmp.setPixel(x, y, Color.MAGENTA);
				}
			}
		}
		return true;
	}

	public void doneScreen() {
		if (state == BLOCK) {
			this.state = FREE;
			matrix.reset();
		}
	}

	public List<MyPointF> doneScreenGetData() {
		List<MyPointF> pts = new ArrayList<MyPointF>();
		if (state == BLOCK) {
			this.state = FREE;
			matrix.reset();
			pts.add(this.screenDown);
			pts.add(this.screenUp);
		}
		return pts;
	}

	public boolean onScreen(MyPointF down, MyPointF up) {
		this.screenDown = down;
		this.screenUp = up;
		Log.d(TAG, "onScreen():");
		if ((state == FREE) && (!hasBall) && (!down.equals(up))) {
			this.state = BLOCK;

			angle = (float) Math.atan((up.y - down.y) / (up.x - down.x));
			matrix.preTranslate((x - block.getWidth() / 2), (float) (y - block.getHeight() / 2));
			matrix.postRotate((float) (180 * angle / Math.PI), x, y);
		}

		return true;
	}

	private void createBlock(boolean isButton) {
		if (isButton) {
			block = Bitmap.createBitmap(bmp);
			Canvas canvas = new Canvas(block);
			//white base
			canvas.drawColor(Color.TRANSPARENT);
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			//canvas.drawColor(Color.rgb(239, 239, 235));
			//black "X"
//			Paint paint = new Paint();
//			paint.setColor(Color.BLACK);
//			paint.setStyle(Paint.Style.FILL_AND_STROKE);
//			paint.setTextAlign(Align.CENTER);
//			paint.setTextSize(determineMaxTextSize("X", (float) (block.getWidth() * 0.7)));
//			Rect bounds = new Rect();
//			paint.getTextBounds("a", 0, "a".length(), bounds);
			//canvas.drawText("X", block.getWidth() / 2, block.getWidth() / 2 + bounds.height() / 2, paint);
			//transparent
			//			for (int x = 0; x < block.getWidth(); x++) {
			//				for (int y = 0; y < block.getHeight(); y++) {
			//					int color = block.getPixel(x, y);
			//					int red = Color.red(color);
			//					int blue = Color.blue(color);
			//					int green = Color.green(color);
			//					int alpha = Color.alpha(color);
			//					color = Color.argb(alpha / 2, red, green, blue);
			//					block.setPixel(x, y, color);
			//				}
			//			}


		} else {
			// TODO: This is stupid, I should instead use a base View class
			// and then use polymorphism or something
			Bitmap bmp = null;
			if (editView != null) {
				bmp = BitmapFactory.decodeResource(editView.getResources(), R.drawable.block);
				block = Bitmap.createScaledBitmap(bmp, (int) baseBlockWidth[gameView.background_index], (int) baseBlockHeight[gameView.background_index], true);
			} else if (buttonView != null) {
				bmp = BitmapFactory.decodeResource(buttonView.getResources(), R.drawable.block);
				block = Bitmap.createScaledBitmap(bmp, (int) baseBlockWidth[gameView.background_index], (int) baseBlockHeight[gameView.background_index], true);
			} else {
				bmp = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.block);
				block = Bitmap.createScaledBitmap(bmp, (int) baseBlockWidth[gameView.background_index], (int) baseBlockHeight[gameView.background_index], true);
			}
		}
		baseBlock = block;

	}

	/**
	 * *************************
	 * Plase input as percentage!!!!! for example...if u want a factor of 1...input 100
	 *
	 * @param factor
	 */
	public void resizePlayer(float factor) {
		Bitmap tmp_bmp = Bitmap.createScaledBitmap(baseBmp, (int) ((baseR[gameView.background_index] * 2 * factor) / 100), (int) ((baseR[gameView.background_index] * 2 * factor) / 100), true);
		Bitmap tmp_block = Bitmap.createScaledBitmap(baseBlock, (int) ((baseBlockWidth[gameView.background_index] * factor) / 100), (int) ((baseBlockHeight[gameView.background_index] * factor) / 100), true);
		this.r = tmp_bmp.getWidth() / 2;
		bmp = tmp_bmp;
		block = tmp_block;

		this.setTeamColor(team_color);
		this.setName(name, false);
	}

	public void resetPaths() {
		for (MyPath path : paths) {
			path.reset();
		}
	}

	public void scaleXY(float scaleX, float scaleY) {
		x = x * scaleX;
		y = y * scaleY;
	}

	/**
	 * multiply by the 3 input factors
	 *
	 * @param scaleX: eg. (newXdime/oldXdim)
	 * @param scaleY: eg. (newYdim/oldYdim)
	 * @param scaleR: a ratio (ie. new_r/old_r)
	 */
	public void scale(float scaleX, float scaleY, float scaleR) {
		scaleXY(scaleX, scaleY);
		resizePlayer(100 * scaleR*r / baseR[gameView.background_index]);
	}

	public void updateByCourtOrientation(int background_index) {
		/* first store the current position to the previous orientation*/
		if (background_index == GameView.FULLCOURT) {
			state_backup[GameView.HALFCOURT] = state;
			state = state_backup[GameView.FULLCOURT];

			pos_backup[GameView.HALFCOURT].x = x;
			pos_backup[GameView.HALFCOURT].y = y;
			x = pos_backup[GameView.FULLCOURT].x;
			y = pos_backup[GameView.FULLCOURT].y;
			resizePlayer(100 * r / baseR[GameView.HALFCOURT]);
		} else {
			state_backup[GameView.FULLCOURT] = state;
			state = state_backup[GameView.HALFCOURT];

			pos_backup[GameView.FULLCOURT].x = x;
			pos_backup[GameView.FULLCOURT].y = y;
			x = pos_backup[GameView.HALFCOURT].x;
			y = pos_backup[GameView.HALFCOURT].y;
			resizePlayer(100 * r / baseR[GameView.FULLCOURT]);
		}
		setMatrixByCourt(background_index);
		resetPaths();
		if (hasBall) {
			updateBall();
		}
	}

	public float getXByCourt(int index) {
		return pos_backup[index].x;
	}

	public float getYByCourt(int index) {
		return pos_backup[index].y;
	}

	public void ButtonDisable() {
		state = BLOCK;
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			state_backup[i] = BLOCK;
		}

	}

	public void ButtonEnable() {
		state = FREE;
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++) {
			state_backup[i] = FREE;
		}
	}

	public void setR(float r) {
		this.r = r;
	}

}
