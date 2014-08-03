package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PathDashPathEffect;
import android.graphics.PointF;
import android.util.Log;

public class Ball {
	private static final String TAG = "Ball";
	private static final int ALIVE = 5;
	private static final int NUM_PATHS = 10;
	private GameView gameView;
	private Bitmap bmp;
	private float x;
	private float y;
	private float r;
	private boolean beingPassed;
	/*ball's move*/
	private List<MyPath> paths;
	int 	cur_path = 0;
	int		time_to_still = 0;
	private boolean hided=false;
	
	public boolean getHided() {
		return hided;
	}

	public void setHided(boolean hided) {
		this.hided = hided;
	}

	public void setBeingPassed(boolean b){
		beingPassed = b;
	}

	public Ball(GameView gameView, Bitmap bmp) {
		this.gameView = gameView;
		this.bmp = bmp;
		this.r = bmp.getWidth()/2;
		this.beingPassed = false;
		
		paths = new ArrayList<MyPath>();
		/* alloc all the paths */		
		for (int i = NUM_PATHS - 1; i >= 0; i--){
			paths.add(new MyPath ());
		}
	}
	
	public void updateByPlayer(float x1, float y1){
		x = x1;
		y = y1;
	}
	/***************************
	 * 
	 * @param dest
	 * @return true if we've reached dest
	 * 		   false if we still need to update by pass
	 */
	public boolean updateByPass(Sprite dest){
		float Step = 50; //this is how many pixels this step it's gonna span,, temporary...later depends on the passer's passing skill
		float dX, dY, refdX, refdY, refStep;
		boolean retval;
		if (!beingPassed)/* first update when being passed */
		{
			beingPassed = true;
			/* move the first empty Path in paths to the current center of the player */
			for (int i = NUM_PATHS - 1; i >= 0; i--){
				MyPath path = paths.get (i);
				if (path.IsEmpty ())
				{
					path.UpdateByFirstMove (x, y);
					cur_path = i;
					break;
				}
			}
		}

		refdX = dest.getX() - x;
		refdY = dest.getY() - y;
		refStep = (float) Math.sqrt(Math.pow(refdX, 2) + Math.pow(refdY, 2));
		dX = Step * (refdX/refStep);
		dY = Step * (refdY/refStep);
		if (Step > refStep)/* then we are stepping too much */
		{
			beingPassed = false;
			x = dest.getX();
			y = dest.getY();
			retval = true;
		}
		else
		{
			x += dX;
			y += dY;
			retval = false;
		}
		/* update the current path */
		paths.get (cur_path).UpdateByMove (x, y);
		return retval;
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
		//resizePlayer(100 * scaleR); //WARNING: need to resize the ball
		resizeByCourt();
	}
	public float passCompleteness(Sprite from, Sprite dest){
		float totaldX, totaldY, totalDist, curdX, curdY, curDist;
		totaldX = dest.getX() - from.getX();
		totaldY = dest.getY() - from.getY();
		totalDist = (float) Math.sqrt(Math.pow(totaldX, 2) + Math.pow(totaldY, 2));
		
		curdX = from.getX() - x;
		curdY = from.getY() - y;
		curDist = (float) Math.sqrt(Math.pow(curdX, 2) + Math.pow(curdY, 2));
		return curDist/totalDist;
	}
	@SuppressLint("WrongCall")
	public void onDrawPath(Canvas canvas){
		/* draw all the non-empty paths associated with this player */
		for (int i = NUM_PATHS - 1; i >= 0; i--){
			MyPath path = paths.get (i);
			if (i != cur_path)
				path.onDraw (canvas, true);
			else
				path.DashPaint();
				path.onDraw (canvas, !beingPassed);
		}
	}
	

	public void onDraw(Canvas canvas){
		/* draw all the non-empty paths associated with this player */
		/*for (int i = NUM_PATHS - 1; i >= 0; i--){
			MyPath path = paths.get (i);
			if (i != cur_path)
				path.onDraw (canvas, false);
			else
				path.onDraw (canvas, (time_to_still > 0));
		}
		*/
		/* draw ball */
		if (this.hided==false){
			canvas.drawBitmap(bmp, (float)(x - r),(float)(y -r), null);
		}
		/* decrement ALIVE to see if player is still moving */
		/*if (time_to_still > 0)
		{
			time_to_still--;
			if (time_to_still == 0)
				this.playerDoneMoving();
		}*/
	}
	
	private void playerDoneMoving() {
		Log.d (TAG, "playerDoneMoving ():");
		TouchUpCback();
	}
	
	public void TouchUpCback (){
	}
	
	public void resetPaths(){
		for (MyPath path : paths){
			path.reset();
		}
	}
	public void resizeByCourt(){
		float newWidth = gameView.background[gameView.background_index].getWidth()/12;		
		Bitmap tmp_bmp = Bitmap.createScaledBitmap(bmp, (int) newWidth, (int) newWidth, true);
		this.r = tmp_bmp.getWidth()/2;
		bmp = tmp_bmp;
		resetPaths();
	}
}
