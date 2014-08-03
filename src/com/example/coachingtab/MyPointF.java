package com.example.coachingtab;

import android.graphics.PointF;

public class MyPointF implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float x;
	public float y;
	
	public MyPointF(float x, float y){
		this.x = x;
		this.y = y;
	}
	public MyPointF(MyPointF p){
		this.x = p.x;
		this.y = p.y;
	}
	public void scaleXY(float scaleX, float scaleY) {
		this.x = this.x * scaleX;
		this.y = this.y * scaleY;
	}
}
