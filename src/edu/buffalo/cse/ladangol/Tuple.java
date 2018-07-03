package edu.buffalo.cse.ladangol;

import java.util.ArrayList;

import javax.xml.bind.TypeConstraintException;

public class Tuple {
	private String[] dataatt;
	private int startpoint;
	private int endpoint;
	public Tuple(String[] tup)
	{
		/*
		 * At First we do not know what is the switchpoint until we see all the data
		 * So temporary we pot -1 as the endpoint of tuples with Infty then we change
		 * it to switch point.
		 */
			//start and end points are natural numbers +0
		int len = tup.length;
		dataatt = new String[len-2];
		for(int i=0; i< len-2 ; i++){
			dataatt[i] = tup[i].trim();
		}
		startpoint = Integer.parseInt(tup[len-2].trim());

		if(tup[len-1].trim().toLowerCase().equals("inf")){
			endpoint = Integer.MAX_VALUE;
		}
		else{
			endpoint = Integer.parseInt(tup[len-1].trim());
		}
	
	}
	public Tuple(String[] dataAttrs, int startp, int endp){
		dataatt = dataAttrs;
		startpoint = startp;
		endpoint = endp;
	}
	public String getStringTuple(){
		StringBuilder tup = new StringBuilder();
		int len = dataatt.length;
		for(int i = 0; i< len; i++){
			tup.append(dataatt[i]+", ");
		}
		tup.append(String.valueOf(startpoint) +", ");
		if(isEndPointInfinite()){
			tup.append("Inf");
		}
		else{
			tup.append(String.valueOf(endpoint));
		}
		
		return tup.toString();
	}
	public int getStartPoint(){
		return startpoint;
	}
	public int getEndPoint(){
		return endpoint;
			
		}
	public boolean isEndPointInfinite(){
		return (endpoint == Integer.MAX_VALUE) ? true : false;
	}
	public void setEndPoint(int newend){
		endpoint = newend;
	}
	public String getDataAttr(int position){
		return dataatt[position];
	}
	public String[] getDataAttrs(){
		return dataatt;
	}
	public int dataAttSize(){
		return dataatt.length;
	}
	public boolean hasEqualDataAttr(Tuple tup){
		for(int i=0; i<dataAttSize(); i++){
			if(!dataatt[i].equals(tup.getDataAttr(i))){
				return false;
			}
		}
		return true;
	}
	
}
  
	
	

