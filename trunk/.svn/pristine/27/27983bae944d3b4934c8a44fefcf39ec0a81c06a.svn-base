package com.example.coachingtab;

import java.util.List;

public class Event implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6556095391223612315L;
	public int timepoint = -1;
	public int event_type = -1;
	/** for type: pass **/
	public int passTo = -1;
	public int passFrom = -1;
	/** for type: screen **/
	public int screenPlayerIndex = -1;
	public MyPointF screenDown = null;
	public MyPointF screenUp = null;
	/** for type: done screen **/
	public int doneScreenPlayerIndex = -1;
	public MyPointF doneScreenDown = null;
	public MyPointF doneScreenUp = null;
	
	public Event(int timepoint, int type) {
		this.timepoint = timepoint;
		this.event_type = type;
	}
	
	public void setPass( int target,int from){
		this.passTo = target;
		this.passFrom = from;
	}
	public void setScreen(int who, MyPointF down, MyPointF up){
		this.screenPlayerIndex = who;
		this.screenDown = down;
		this.screenUp = up;
	}
	public void setDoneScreen(int who, List<MyPointF> pts){
		this.doneScreenPlayerIndex = who;
		this.doneScreenDown = pts.get(0);
		this.doneScreenUp = pts.get(1);
	}
}
