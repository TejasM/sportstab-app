package com.example.coachingtab;

import android.graphics.PointF;

public class ScreenPoint {
	private int ID = -1;
	private MyPointF down;
	private MyPointF up;
	public int spriteIndex[] = new int[GameLoopThread.MAX_NUM_PLAYERS + 1];

	public ScreenPoint() {
		for (int i = 0; i < GameLoopThread.MAX_NUM_PLAYERS + 1; i++){
			spriteIndex[i] = -1;
		}
		down = new MyPointF(0,0);
		up = new MyPointF(0,0);
	}
	
	public void addIndex(int index){
		for (int i = 0; i < GameLoopThread.MAX_NUM_PLAYERS; i++){
			if (spriteIndex[i] == index){
				return;
			}
			if (spriteIndex[i] == -1){
				spriteIndex[i] = index;
				return;
			}
		}
	}
	
	public void setID(int id){
		ID = id;
	}
	
	public void setDown(MyPointF down){
		this.down = down;
	}
	
	public void setUp (MyPointF up){
		this.up = up;
	}
	
	public int getID(){
		return ID;
	}
	
	public MyPointF getDown(){
		return down;
	}
	
	public MyPointF getUp(){
		return up;
	}
	
	/*public int[] getSpriteIndex(){
		return spriteIndex;
	}*/
}
