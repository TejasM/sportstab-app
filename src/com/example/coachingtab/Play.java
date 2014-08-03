package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class Play implements java.io.Serializable{/* each step starts with a touch down */
	/**
	 * 
	 */
	private static final long serialVersionUID = -3608029479525739852L;
	private String ID;  /* used to identify this play*/
	public List<Step> steps = new ArrayList<Step>();
	public Tags tags;
	private int curr_step;
	private int view_orientation;
	public List<Sprite> players = new ArrayList<Sprite>();
	
	public Play(int background_index, List<Sprite> sprites, GameView view, CoachingTab act) {
		curr_step = 0;
		view_orientation = background_index;
		for (int i = 0; i < sprites.size(); i++){
			players.add(new Sprite(sprites.get(i)));
		}
	}
	
	public void setOrientation(int i){
		view_orientation = i;
	}
	
	public void setTags(Tags t) {
		tags = t;
	}
	
	public int getOrientation(){
		return view_orientation;
	}
	public Step CurrStep(){
		return steps.get(curr_step);
	}
	
	public String getID(){
		return ID;
	}
	
	public void setID(String id){
		if (id.contentEquals("")){
			ID = "Default";
		}else{
			ID = id;
		}
	}
	
	public void addStep(List<Sprite> sprites, GameView view){
		steps.add(new Step(sprites, view));
		curr_step++;
	}
	/********
	 * 
	 * @return true if step is not incremented b/c it's the last step
	 */
	public boolean IncrementStep(){
		if (curr_step < steps.size() - 1){
			curr_step++;
			return false;
		}else return true;
			
	}
	
	/********
	 * 
	 * @return true if step is not decremented b/c it's the first step
	 */
	public boolean decrementStep(){
		if (curr_step > 0){
			curr_step--;
			return false;
		}else return true;
			
	}
	
	public int getCurrStep(){
		return curr_step;
	}

	public void setCurrStep(int i) {
		curr_step = i;
	}

	public void storeMatrix() {
		for (int i = 0; i < steps.size(); i++){
			steps.get(i).storeMatrixData();
		}
		
	}

	public void restoreMatrix() {
		for (int i = 0; i < steps.size(); i++){
			steps.get(i).restoreMatrixData();
		}
		
	}
	
	public void scaleXY(float scaleX, float scaleY) {
		for (int i=0; i<this.CurrStep().playerMove.size();i++){
			OneMove m = this.CurrStep().playerMove.get(i);
			for (int j=0; j<m.points.size();j++){
				MyPointF p = m.points.get(j);
				p.scaleXY(scaleX,scaleY);
			}
			this.CurrStep().initialPositions.get(i).scaleXY(scaleX, scaleY);
			Sprite p = this.players.get(i);
			p.setR(p.getR()*scaleX);
		}
		
	}
}