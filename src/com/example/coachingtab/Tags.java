package com.example.coachingtab;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import android.content.Context;

public class Tags implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6788548159841912034L;
	List<String> tags = new ArrayList<String>();
	List<Integer> IDs = new ArrayList<Integer>();
	List<Boolean> states = new ArrayList<Boolean>();
}
