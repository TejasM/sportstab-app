package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

public class OneMove implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6178505403189827541L;
	public List<MyPointF> points = new ArrayList<MyPointF>();
	private float distance = 0;
	
	public void addPointF(MyPointF pt){
		if (points.size() > 0){
			float dX = points.get(points.size() - 1).x - pt.x;
			float dY = points.get(points.size() - 1).y - pt.y;
			distance += (float) Math.sqrt(dX * dX + dY * dY);	
		} 
		points.add(pt);
	}
	
	public float getDistance(){
		return distance;
	}
	
	
}