package com.example.coachingtab;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
 
public class PlayBookAdapter extends BaseAdapter {
    private Context mContext;
    private CoachingTab activity;
 
    // Keep all Images in array
    public Integer[] mThumbIds = {
            /*R.drawable.pic_1, R.drawable.pic_2,
            R.drawable.pic_3, R.drawable.pic_4*/
    };
 
    // Constructor
    public PlayBookAdapter(Context c, CoachingTab act){
        mContext = c;
        activity = act;
    }
 
    @Override
    public int getCount() {
       // return mThumbIds.length;
    	//return activity.getGameView().thread.sprites.size();
    	return activity.getPlayBookCount();
    }
 
    @Override
    public Object getItem(int position) {
        //return mThumbIds[position];
    	//return activity.getGameView().thread.sprites.get(position);
    	
    	return activity.getPlayBookObject(position);
    }
 
    @Override
    public long getItemId(int position) {
        return 0;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*ImageView imageView = new ImageView(mContext);
        //imageView.setImageResource(mThumbIds[position]);
        //imageView.setImageBitmap(activity.getGameView().thread.sprites.get(position).getBitmap());
        imageView.setImageBitmap(activity.getPlayBookObject(position));
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new GridView.LayoutParams(700, 700));
        return imageView;*/
    	TextView tView = new TextView(mContext);
		activity.getGameView().getRecorder().updatePlayBook();
    	tView.setText(activity.getGameView().getRecorder().getCatalog().get(position));
    	tView.setTextSize((float)(activity.getGameView().getHeight() * 0.02));
    	/*tView.setBackgroundColor(Color.BLACK);
    	tView.setTextColor(Color.WHITE);
    	tView.setShadowLayer(100, 10, 10, Color.WHITE);*/
    	return tView;
    }
 
}
