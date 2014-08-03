package com.example.coachingtab;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

public class SpriteData {
	public float x;
	public float y;
	public Bitmap bmp;
	public Bitmap block;
	public Bitmap baseBmp;
	public Bitmap baseBlock;
	public float r;
	public float[] baseR = new float[GameView.BACKGROUND_NUM];
	public float[] baseBlockWidth = new float[GameView.BACKGROUND_NUM];
	public float[] baseBlockHeight = new float[GameView.BACKGROUND_NUM];
	

	public PointF[] pos_backup = new PointF[GameView.BACKGROUND_NUM];
	public int[] state_backup = new int[GameView.BACKGROUND_NUM];
	public Matrix[] matrix_backup = new Matrix[GameView.BACKGROUND_NUM];
	
	public boolean hasBall = false;

	public int state;
	public Matrix matrix;

	public int	team_color;
	public String name;
	
	public SpriteData(Sprite copy){
		x = copy.getX();
		y = copy.getY();
		bmp = Bitmap.createBitmap(copy.getBitmap());
		block = Bitmap.createBitmap(copy.getBlockBitmap());
		baseBmp = Bitmap.createBitmap(copy.getBaseBitmap());
		baseBlock = Bitmap.createBitmap(copy.getBaseBlockBitmap());
		r = copy.getR();
		for (int i = 0; i < GameView.BACKGROUND_NUM; i++){
			//baseR[i] = copy.getBaseR()[i];
		}
		
	}
}
