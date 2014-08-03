package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class EditBar {
	static final int MID = 0;
	static final int START = 1;
	static final int END = 2;
	static final int LINE = 3;
	static final int BALL = 4;
	static final int BLOCK = 5;
	static final int MOVE = 6;
	static final int PARENF = 7;
	static final int PARENB = 8;
	static final int RUNBALL = 9;
	static final int STARTOR = 10;
	static final int MIDOR = 11;
	static final int ENDOR = 12;
	
	static final int BMP_SMALL = 0;
	static final int BMP_BIG = 1;

	private Bitmap player_icon;
	private List<Bitmap> bmps = new ArrayList<Bitmap>();
	private List<Bitmap> bmps_small = new ArrayList<Bitmap>();
	private List<Bitmap> bmps_big = new ArrayList<Bitmap>();
	private CoachingTab activity;
	private EditView view;
	private float x;
	private float y;
	private int height;
	private float down_x = -1;
	private float curr_x = -1;
	private int selectedEventIndex = -1;
	public List<EditFragment> fragments = new ArrayList<EditFragment>();
	public List<EditFragment> movements = new ArrayList<EditFragment>();
	public List<EditFragment> possessions = new ArrayList<EditFragment>();
	public List<EditFragment> screens = new ArrayList<EditFragment>();

	public EditBar(EditView view, CoachingTab activity) {

		this.activity = activity;
		this.view = view;

		//this.height = this.bmps.get(0).getHeight();

	}
	public void setBitmaps(int index){
		if (index == EditBar.BMP_SMALL){
			this.bmps = this.bmps_small;
		}
		else if (index == EditBar.BMP_BIG){
			this.bmps = this.bmps_big;
		}
		this.height = this.bmps.get(0).getHeight();
	}
	public void setBitmapsSmall(List<Bitmap> bmps){
		this.bmps_small = bmps;
	}
	public void setBitmapsBig(List<Bitmap> bmps){
		this.bmps_big = bmps;
	}
	public void setPlayerIcon (Bitmap b){
		this.player_icon = b;
	}
	public void setXY(float x, float y){
		this.x = x;
		this.y = y;
	}
	public int getHeight(){
		return height;
	}
	public boolean isTouched(float x,float y){
		boolean ret = (this.y - y) * (this.y+this.height-y) < 0;
		if (ret){//if bar is touched, figure out which event 
			for (int i = 0; i<fragments.size();i++){
				float start = (float)(fragments.get(i).getStart()*view.getWidth());
				float end = (float)(fragments.get(i).getEnd()*view.getWidth());
				if ((end - x)*(start-x)<0){
					this.selectedEventIndex = i;
					break;
				}
			}
		}
		if (ret&&(this.selectedEventIndex==-1)){
			System.out.println("wtf");
		}
		return ret;
	}
	public int whichEventInListFromSelectedFrag(List<EditFragment> list){
		EditFragment test = this.fragments.get(selectedEventIndex);
		for (int i =0;i<list.size();i++){
			EditFragment m = list.get(i);
			if ((m.getStart()<=test.getStart())&&(m.getEnd()>=test.getEnd())){
				return i;
			}
		}
		return -1;
	}
	public int prevEventInArray(int i, List<EditFragment> list, int event){
		while (i>0){
			i--;
			if (list.get(i).getType()==event){
				return i;
			}
		}
		return -1;
	}
	public int nextEventInArray(int i, List<EditFragment> list, int event){
		while (i<list.size()-1){
			i++;
			if (list.get(i).getType()==event){
				return i;
			}
		}
		return -1;
	}
	public boolean overlapped(int i, float dx, List<EditFragment> list, int event){
		int p = this.prevEventInArray(i, list, event);
		int n = this.nextEventInArray(i, list, event);
		if (p!=-1){
			EditFragment prev = list.get(p);
			if (list.get(i).getStart()+dx<=prev.getEnd()){ //overlapping with the previous/next movement
				return true;
			}
		}
		if (n!=-1){
			EditFragment next = list.get(n);
			if (list.get(i).getEnd()+dx>=next.getStart()){
				return true;
			}
		}
		return false;
	}
	public void updateCurrX(float x){
		if (this.down_x == -1){//first update
			this.down_x = x;
			this.curr_x = down_x;
		}else{
			float prev_x = this.curr_x;
			this.curr_x = x;
			//now check if this guy is gonna overlap the meaningful event before or after him
			float dx = this.percentDX();
			if (this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_MOVE){
				int m = whichEventInListFromSelectedFrag(this.movements);
				EditFragment move = this.movements.get(m);
				if ((move.getStart()+dx<=0)||(move.getEnd()+dx>=1)){
					this.curr_x = prev_x;
					return;
				}
				if (move.getType()==EditBars.IDLE){//selected IDLE
					this.curr_x = prev_x;
					return;
				}
				if (overlapped(m, dx, this.movements, EditBars.MOVE)==true){//overlapped with prev or next movement
					this.curr_x = prev_x;
					return;
				}
				//now also check if we're overlaping with screens
				if (overlapped(this.selectedEventIndex, dx, this.fragments, EditBars.BLACK)==true){
					this.curr_x = prev_x;
					return;
				}
			}else if(this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_BALL){
				List<EditFragment> list = this.view.editbars.possessionByPlayer;
				int p = whichEventInListFromSelectedFrag(list);
				if ((list.get(p).getStart()+dx<=0)||(list.get(p).getStart()+dx>=1)){//out of screen
					this.curr_x = prev_x;
					return;
				}
				int lastIndex = this.view.editbars.prevMeaningfulPossessionIndex(p);// usually lastIndex == p-2
				//2 conditions
				//1. backward in time: can't move before last pass
				if (list.get(p).getStart()+dx<list.get(lastIndex).getStart()){
					this.curr_x = prev_x;
					return;
				}
				//2. foward in time: can't move past this event's end time
				if (list.get(p).getStart()+dx>list.get(p).getEnd()){
					this.curr_x = prev_x;
					return;
				}
			}else if (this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_SCREEN){
				EditFragment e = this.fragments.get(selectedEventIndex);
				if ((e.getStart()+dx<=0)||(e.getEnd()+dx>=1)){//out of screen
					this.curr_x = prev_x;
					return;
				}
				//2 conditions
				//1. interferes with movements
				if (fragmentOverlapWithList(e, this.movements, EditBars.MOVE)==true){
					this.curr_x = prev_x;
					return;
				}
				//2. interfacers with possessions
				if (fragmentOverlapWithList(e, this.possessions, EditBars.BALL_IDLE)==true){
					this.curr_x = prev_x;
					return;
				}
			}
			else{//not in any of the edit mode, can't move anything
				this.curr_x = prev_x;
				return;
			}
		}
	}
	public boolean fragmentOverlapWithList(EditFragment e, List<EditFragment> list, int event){
		float dx = this.percentDX();
		int m = whichEventInListFromSelectedFrag(list);
		int prev = prevEventInArray(m, list, event);
		int next = nextEventInArray(m, list, event);
		if ((prev!=-1)&&(e.getStart()+dx<=list.get(prev).getEnd())){
			return true;
		}
		if ((next!=-1)&&(e.getEnd()+dx>=list.get(next).getStart())){
			return true;
		}
		return false;
	}
	private float percentDX(){
		float dx = this.curr_x - this.down_x;
		float ref = (float)view.getWidth();
		return dx = dx/ref;
	}
	@SuppressWarnings("unused")
	public void finishMoving(int index){


		Step step = activity.getGameView().getRecorder().getCurrPlay().steps.get(0);
		// render the fragments
		float dx = this.percentDX();
		EditFragment e = this.fragments.get(selectedEventIndex);
		OneMove move = step.playerMove.get(index);
		int dx_index = (int) (dx*move.points.size());

		int size = move.points.size();

		float epsilon = (float)0.05;
		if (this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_MOVE){
			List<List<MyPointF>> motions = new ArrayList<List<MyPointF>>();
			List<MyPointF> m = new ArrayList<MyPointF>();
			int k = whichEventInListFromSelectedFrag(this.movements);
			EditFragment movement = this.movements.get(k);
			int start_index = (int)(movement.getStart() * (float)move.points.size());
			int end_index = (int)(movement.getEnd() * (float)move.points.size());
			for (int j=start_index;j<end_index;j++){
				m.add(new MyPointF(move.points.get(j)));
			}
			motions.add(m);
			int new_start_index =start_index+dx_index;
			int new_end_index = end_index + dx_index;
			for (int j = new_start_index;j<new_end_index;j++){
				int i = j-new_start_index;
				move.points.get(j).x = motions.get(0).get(i).x;
				move.points.get(j).y = motions.get(0).get(i).y;
			}
			//pad the next or prev event with where this guy started/finished
			MyPointF startPos = move.points.get(start_index);
			if (end_index>=move.points.size()) end_index = move.points.size()-1;
			MyPointF endPos = move.points.get(end_index);
			for (int j = start_index; j < new_start_index;j++){
				move.points.get(j).x = startPos.x;
				move.points.get(j).y = startPos.y;
			}
			for (int j = new_end_index; j < end_index;j++){
				move.points.get(j).x = endPos.x;
				move.points.get(j).y = endPos.y;
			}
		}else if(this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_BALL){
			int passer_id = -1; 
			int old_timepoint = -1;
			int new_timepoint = -1;
			for (int i = 0; i < step.events.size();i++){
				Event event = step.events.get(i);
				float percent_time =((float)event.timepoint/(float)size);

				//need to find out which event we're editing in the original events array
				if (event.passTo==index){//matches the receiver of the pass
					if ((e.getStart()>=percent_time-epsilon)&&(e.getStart()<=percent_time+epsilon)){//this event happens around the same time as the one we're editting
						old_timepoint = event.timepoint;
						event.timepoint=(int)((percent_time+dx)*(float)size);
						new_timepoint = event.timepoint;
						passer_id = event.passFrom;

					}
				}
			}
			//now, update the hasBall data struct 
			//first find out how many 'ball-in-the-air' we have WARNING: this might actually change if the position of the players are also changed, but i'm just not gonna worry about it now
			int count = 1;
			for (int i = old_timepoint + 1;step.whoHasBall.get(i)==-1;i++){
				count++;
			}
			if (new_timepoint < old_timepoint){
				int i;
				for (i = new_timepoint; i < new_timepoint+count;i++){
					step.whoHasBall.remove(i);
					step.whoHasBall.add(i,-1);
				}
				for (;i<=old_timepoint+count;i++){
					step.whoHasBall.remove(i);
					step.whoHasBall.add(i,index);
				}
			}else{
				int i;
				for (i = old_timepoint; i<new_timepoint;i++){
					step.whoHasBall.remove(i);
					step.whoHasBall.add(i,passer_id);
				}
				if (i!=-1){//i==-1 when there was no pass to begin with
					for (;i<=new_timepoint+count;i++){
						step.whoHasBall.remove(i);
						step.whoHasBall.add(i,-1);
					}
				}
			}
		}else if (this.view.editbars.getEditEventMode()==ButtonView.BTN_EDIT_SCREEN){
			for (int i = 0; i < step.events.size();i++){
				Event event = step.events.get(i);
				float percent_time =((float)event.timepoint/(float)size);
				//need to find out which event we're editing in the original events array
				if (event.screenPlayerIndex==index){//event happens on the same person & same type SCREEN
					if ((e.getStart()>=percent_time-epsilon)&&(e.getStart()<=percent_time+epsilon)){//this event happens around the same time as the one we're editting
						event.timepoint=(int)((percent_time+dx)*(float)size);
					}
				}
				//done screen event
				if ((event.doneScreenPlayerIndex==index)){
					if ((e.getEnd()>=percent_time-epsilon)&&(e.getEnd()<=percent_time+epsilon)){
						event.timepoint=(int)((percent_time+dx)*(float)size);
					}
				}
			}
		}
		this.view.editbars.render(activity.getGameView().getRecorder().getCurrPlay());
		//cleanup
		this.down_x = -1;
		this.selectedEventIndex = -1;
	}

	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas){	
		Rect src = new Rect(0,0,bmps.get(LINE).getWidth(),height);
		Rect dst = new Rect((int)x,(int)y, (int)x+view.getWidth(),(int)y+height);
		canvas.drawBitmap(this.bmps.get(LINE), src, dst, null);
		synchronized(this.view.getHolder()){
			if (fragments.size()==0){
				//canvas.drawBitmap(idle, x,y, null);
			}else{//WARNING: Synchronization problem!

				for (int i = 0; i<fragments.size();i++){
					drawFragment(canvas, i);
				}
				drawPass(canvas);
				if ((this.selectedEventIndex!=-1)&&(this.selectedEventIndex<this.fragments.size())){//WARNING: the second check is one of those bad checks that hide crashes :(
					int i = this.selectedEventIndex;
					int start = (int)(fragments.get(i).getStart()*view.getWidth());
					int end = (int)(fragments.get(i).getEnd()*view.getWidth());
					float dx = this.curr_x - this.down_x;
					canvas.drawBitmap(this.bmps.get(PARENF), dx+start-this.bmps.get(PARENF).getWidth()/2, (int)this.y, null);
					if ((fragments.get(i).getType()!=EditBars.BALL_IDLE)&&(fragments.get(i).getType()!=EditBars.BALL_MOVE))
						canvas.drawBitmap(this.bmps.get(PARENB), dx+end-this.bmps.get(PARENB).getWidth()/2, (int)this.y, null);
				}
			}
		}
		src = new Rect(0,0,player_icon.getWidth(),player_icon.getHeight());
		dst = new Rect((int)x,(int)y, (int)x+height,(int)y+height);
		canvas.drawBitmap(player_icon, src, dst, null);
	}
	private void drawPass(Canvas canvas){
		Bitmap bmp = this.bmps.get(BALL);
		for (int i = 0; i<this.possessions.size();i++){
			EditFragment p = this.possessions.get(i);
			if (i!=0){
				int start =(int) (p.getStart()*view.getWidth());
				canvas.drawBitmap(bmp, start-bmp.getWidth()/2,this.y, null);
			}
			if (i!=this.possessions.size()-1){
				int end = (int)(p.getEnd()*view.getWidth());
				canvas.drawBitmap(bmp, end-bmp.getWidth()/2, this.y, null);
			}
		}
	}
	private void drawFragment(Canvas canvas, int i){
		int offset = 0;
		if ((fragments.get(i).getType()==EditBars.IDLE)){
			return;
		}else{//WARNING Synchronization problem: fragments might be changed by render
			Bitmap s,m,e;
			if (fragments.get(i).getType()==EditBars.BALL_IDLE){
				offset = this.bmps.get(MID).getHeight()/4;
			}
			if ((fragments.get(i).getType()==EditBars.BALL_IDLE)||(fragments.get(i).getType()==EditBars.BALL_MOVE)){
				s = this.bmps.get(STARTOR);
				m = this.bmps.get(MIDOR);
				e = this.bmps.get(ENDOR);
			}else{
				s = this.bmps.get(START);
				m = this.bmps.get(MID);
				e = this.bmps.get(END);
			}
			int start = (int)(fragments.get(i).getStart()*view.getWidth());
			int end = (int)(fragments.get(i).getEnd()*view.getWidth()); 
			int length = end - start;
			Rect start_src, start_dst, end_src, end_dst, mid_src, mid_dst, src, dst;
			if (length < this.bmps.get(END).getWidth()*2){//fragment too small
				start_src = new Rect(0,0,length/2,height);
				start_dst = new Rect((int)x+start,(int)y+offset, (int)x+start+length/2,(int)y+height-offset);
				end_src = new Rect(this.bmps.get(END).getWidth()-length/2, 0, this.bmps.get(END).getWidth(),height);
				end_dst = new Rect((int)x+start+length/2, (int)y+offset, (int)x+end, (int)y+height-offset);

			}else{
				start_src = new Rect(0,0,this.bmps.get(START).getWidth(), this.height);
				start_dst = new Rect((int)x+start, (int)y+offset, (int)x+start+this.bmps.get(START).getWidth(), (int)y+this.height-offset);
				end_src = new Rect(0,0,this.bmps.get(END).getWidth(), this.height);
				end_dst = new Rect((int)x+end-this.bmps.get(END).getWidth(),(int)y+offset, (int)x+end, (int)y+this.height-offset);
				mid_src = new Rect(0, 0, this.bmps.get(MID).getWidth(), height);
				mid_dst = new Rect((int)x+start+this.bmps.get(START).getWidth(), (int)y+offset, (int)x+end-this.bmps.get(END).getWidth(), (int)y+height-offset);
				canvas.drawBitmap(m, mid_src, mid_dst,null);
			}
			canvas.drawBitmap(s, start_src, start_dst, null);
			canvas.drawBitmap(e, end_src, end_dst,null);
			//draw the symbol
			Bitmap bmp=null;
			int type = this.fragments.get(i).getType();
			if (type==EditBars.BLACK){
				bmp = this.bmps.get(BLOCK);
			}
			if (type==EditBars.MOVE){
				bmp = this.bmps.get(MOVE);
			}
			if (type==EditBars.BALL_MOVE){
				bmp = this.bmps.get(RUNBALL);
			}
			if (type==EditBars.BALL_IDLE){
				//bmp = this.bmps.get(BALL);
			}
			if (bmp!=null){
				src = new Rect(0,0,bmp.getWidth(),bmp.getHeight());
				int mid = (int)this.x+start+length/2;
				dst = new Rect(mid-bmp.getWidth()/2,(int)y,mid+bmp.getWidth()/2,(int)y+height);
				canvas.drawBitmap(bmp,src,dst,null);
			}

		}
	}

}
