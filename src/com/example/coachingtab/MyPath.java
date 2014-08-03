package com.example.coachingtab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.widget.Toast;

public class MyPath {
	private static final String TAG = "MyPath";
	/*Player's move*/
	//private List<Path> paths = new ArrayList<Path>();
	private Path path = new Path();
	int    	count_down = 0;
	Paint  	paint = new Paint();
	private float prevX;
	private float prevY;
	private float prevprevX;
	private float prevprevY;
	private CoachingTab activity;
	
	public MyPath() {
		paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(7);
		paint.setStyle(Paint.Style.STROKE);
	}
	
	public MyPath(CoachingTab activity) {
		this.activity = activity;
		paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(7);
		paint.setStyle(Paint.Style.STROKE);
	}
	
	public void DashPaint() {
		paint.setPathEffect(new DashPathEffect(new float[] {50, 50}, 0));
	}
	
	public void UpdateByFirstMove (float x1, float y1){
		path.moveTo (x1, y1);
		prevX = x1;
		prevY = y1;
		/* give path full color*/
		count_down = 50;
	}
	
	public void UpdateByMove (float x1, float y1){
		if (path.isEmpty()){
			UpdateByFirstMove(x1,y1);
		}/*
		float dX = prevX- prevprevX;
		float dY = prevY- prevprevY;
		float momentum = (float)0.5;
		float tX = prevX + momentum * dX;
		float tY = prevY + momentum * dY;
		prevprevX = prevX;
		prevprevY = prevY;
		prevX = x1;
		prevY = y1;
		path.quadTo(tX, tY,x1, y1);*/
		path.lineTo(x1,y1);
	}
	
	public void onDraw (Canvas canvas,  boolean continue_count_down){
		if ((count_down>0) && (!path.isEmpty())){/* check if there's a non-empty path */
			paint.setAlpha(count_down*5);
			canvas.drawPath(path, paint);
			if (continue_count_down == true)
			{
				if (count_down>20){
					
					count_down--;
				}else
				{
					count_down = 0;
					path.reset ();
				}
			}
		}
	}
	public void reset(){
		path.reset();
	}
	
	public Paint getPaint() {
		return paint;
	}

	public boolean IsEmpty() {
		return path.isEmpty();
	}
}
