package edu.buffalo.cse.ladangol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class Table {
	private ArrayList<Tuple> table;
	private int switchpoint;
	private ArrayList<Integer> infintindexes;
	private boolean isConsistant;
	public Table(){
		table = new ArrayList<>();
		switchpoint = Integer.MIN_VALUE;
	}
	public Table(int card){
		table = new ArrayList<>(card);
		switchpoint = Integer.MIN_VALUE;
	}
	public void add(Tuple tup){
		table.add(tup);
	}

	public int Cardinality(){
		return table.size();
	}
	public Tuple getTuple(int index){
		return table.get(index);
	}
	public void reduce(){
		
		findSwitchPoint();
		//Now reduce by e^* = switchpoint
		for(int i=0; i < infintindexes.size(); i++){
			Tuple temp = table.get(infintindexes.get(i));
			temp.setEndPoint(switchpoint+1);
		}
	}
	private void findSwitchPoint() {
		//First finding the switchpoint
				infintindexes = new ArrayList<Integer>();
				for(int i=0; i<table.size(); i++){
					Tuple tup = table.get(i);
					if(tup.isEndPointInfinite()){
						infintindexes.add(i);
						if(tup.getStartPoint() > switchpoint){
							switchpoint = tup.getStartPoint();
						}
					}
					else if(tup.getEndPoint() > switchpoint){
						switchpoint = tup.getEndPoint();
					}
						
				}
		
	}

	public void coalesce(){
		HashMap<String, ArrayList<Interval<Integer, Integer>>> cotbl = new HashMap<String, ArrayList<Interval<Integer,Integer>>>();
		//building the hash : tuples with the same data attributes are going to one key
		for(int i=0; i<table.size(); i++){
			Tuple tup = table.get(i);
			String[] dataatt = tup.getDataAttrs();
			//I have to learb how key can be String[] later
			String key = convertStringArrayToString(dataatt, ";");
			Interval<Integer, Integer> inv = Interval.of(tup.getStartPoint(), tup.getEndPoint());
			if(cotbl.containsKey(key)){
				ArrayList<Interval<Integer, Integer>> value = cotbl.get(key);
				value.add(inv);
				cotbl.put(key,value);				
			}
			else{
				ArrayList<Interval<Integer, Integer>> value = new ArrayList<Interval<Integer, Integer>>();
				value.add(inv);
				cotbl.put(key, value);
			}
			
				
	   }
	  // Now we need to sort each List of each key
		//Iterate over the hashmap and sort, then coalece 
		ArrayList<Tuple> tbl = new ArrayList<>();
		 Set<String> keySet = cotbl.keySet();
		 Iterator<String> keySetIterator = keySet.iterator();
		 while (keySetIterator.hasNext()) {
		    String key = keySetIterator.next();
		    ArrayList<Interval<Integer, Integer>> value = cotbl.get(key);
		    
		    Collections.sort(value, new IntervalComparator());
		    Iterator<Interval<Integer, Integer>> iter = value.iterator();
		    Interval<Integer, Integer> current = iter.next();
		    int startpoint = (Integer) current.sp;
		    Interval<Integer, Integer> next =null;
		    String[] data = key.split(";");
		    while(iter.hasNext())
		    {
		    	next = iter.next();
		    	if(!current.ep.equals(next.sp)){
		    		
		    		Tuple tup= new Tuple(data, startpoint,current.ep);
		    		tbl.add(tup);
		            startpoint = next.sp;
		    	}
		  
		   		current = next;
		    }
		    int endpoint;
		    if(next == null){
		    	endpoint = current.ep;
		    }
		    else{
		    	endpoint = (int) next.ep;
		    }
		    
		    Tuple tup = new Tuple(data,startpoint, endpoint);
		    tbl.add(tup);
		 }
		 table.clear();
		 table.addAll(tbl);
	}
	public void expand(){
		for(int i=0; i < infintindexes.size(); i++){
			Tuple temp = table.get(infintindexes.get(i));
			temp.setEndPoint(Integer.MAX_VALUE);
		}
		
	}
	public void PntRCon(ArrayList<tFD> tFDs){ 
		ArrayList<Tuple> repair = new ArrayList<Tuple>();
		Collections.shuffle(table);
		//Collections.shuffle(instance);
		Collections.shuffle(tFDs);
		List<Map<String, String>> tFDsMaps = new ArrayList<Map<String, String>>(tFDs.size());
		for (int i=0; i< tFDs.size(); i++){
			tFDsMaps.add(new HashMap<String, String>());
		}
		for(int t=0; t < table.size(); t++ ){
			Tuple tup = table.get(t);
			for(int currentpoint = tup.getStartPoint(); currentpoint < tup.getEndPoint(); currentpoint++){
				for(int d = 0; d <tFDs.size(); d++){
					HashMap<String, String> tfdMap = (HashMap<String, String>) tFDsMaps.get(d);
					tFD tfd = tFDs.get(d);
					int[] lhs = tfd.getlhs();
					StringBuilder key = new StringBuilder();
					for(int i = 0; i< lhs.length; i++){
						//lhs[i]-1  because use start from index 1
						key.append(tup.getDataAttr(lhs[i]-1).toLowerCase());
					}
					key.append(currentpoint); //adding the temporal attribute
					int[] rhs = tfd.getrhs();
					StringBuilder value = new StringBuilder();
					for(int i=0; i<rhs.length; i++){
						value.append(tup.getDataAttr(rhs[i]-1).toLowerCase());
					}
					//So if I do not put this bloody .toString it never understands they are equal
					//That is why types are super important. 
					if(tfdMap.containsKey(key.toString())){
						if(tfdMap.get(key.toString()).equals(value.toString())){
							//Data base still consistant
							//building the concrete tuple
							Tuple reptup = new Tuple(tup.getDataAttrs(), currentpoint, currentpoint+1);
							repair.add(reptup);

						}
						else{
							isConsistant = false;
						}
					}
					else{
						
						tfdMap.put(key.toString(), value.toString());
						Tuple reptup = new Tuple(tup.getDataAttrs(), currentpoint, currentpoint+1);
						repair.add(reptup);
					}
				}
			}
		}
		table.clear();
		table.addAll(repair);

			
}
	private static String convertStringArrayToString(String[] strArr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String str : strArr) 
			sb.append(str).append(delimiter);
		return sb.substring(0, sb.length() - 1);
	}
	public Table Factorize(ArrayList<tFD> tFDs){
		ArrayList<int[]> lhstFDs = new ArrayList<int[]>(tFDs.size());
		for(int i=0; i<tFDs.size(); i++){
			lhstFDs.add(tFDs.get(i).getlhs());
		}
		//Finding the sets of facts that have to be normalized together
		
		ArrayList<HashSet<Integer>> S = new ArrayList<HashSet<Integer>>();
		ArrayList<Hashtable<String, ArrayList<Integer>>> mayinc = new ArrayList<Hashtable<String, ArrayList<Integer>>>();
		for(int i= 0; i<lhstFDs.size(); i++){
			Hashtable<String, ArrayList<Integer>> tFDHash = new Hashtable<String, ArrayList<Integer>>();
			int[] tFD = lhstFDs.get(i);
			for(int j=0; j< table.size(); j++){
				StringBuilder key = new StringBuilder();
				Tuple tup = table.get(j);
				for(int k=0; k<tFD.length; k++){
					key.append(tup.getDataAttr(tFD[k]-1));
				}
				String keyStr = key.toString();
				if(tFDHash.containsKey(keyStr)){
					ArrayList<Integer> temp = tFDHash.get(keyStr);
					temp.add(j); //J is tuple number
					tFDHash.put(keyStr, temp);
				}
				else
				{
					ArrayList<Integer> temp = new ArrayList<>();
					temp.add(j);
					tFDHash.put(keyStr, temp);
				}
			}
			mayinc.add(tFDHash);
		
				
				
				
			}
		
		for(int i=0; i<mayinc.size(); i++){
			Hashtable<String, ArrayList<Integer>> tFDHash = mayinc.get(i);
			Collection<ArrayList<Integer>> sets = tFDHash.values();
			Iterator<ArrayList<Integer>> iter = sets.iterator();
			HashSet<Integer> hs = new HashSet<Integer>();
			while(iter.hasNext()){		
				//This just copy the references not the actual values to S
				//When I clear hs then the values in S are being copied too
/*				hs.addAll(iter.next());
				S.add(hs);  
				hs.clear();*/
				hs.addAll(iter.next());
				S.add((HashSet<Integer>) hs.clone());
				hs.clear();
			}
			
		}
		//Merging the sets that have a fact in common
		
		int index=0;
		boolean changes = false;
		while(index <S.size()){
			changes = true;
			HashSet<Integer> temp1 = S.get(index);
			while(changes){
				changes = false;
				for(int j=S.size()-1; j>index; j--){
					HashSet<Integer> temp2 = S.get(j);
					if(!Collections.disjoint(S.get(index), temp2)){
						changes=true;
						S.get(index).addAll(temp2);
						S.remove(j);
						
					}
				}
			}
			index++;
		}
		Table normalizedtbl = new Table();
		TreeSet<Integer> ts = new TreeSet<Integer>(); //Storing the sorted distinct start poingts and endpoints
	    for(index =0; index<S.size(); index++){
	    	HashSet<Integer> delta = S.get(index);
	    	ts.clear();
	        Iterator<Integer> iter = delta.iterator();
	        while(iter.hasNext()){
	        	Tuple tup = table.get(iter.next());
	        	ts.add(tup.getStartPoint());
	        	ts.add(tup.getEndPoint());
	        }
	        
	        iter = delta.iterator();
	        while(iter.hasNext()){
	        	Tuple tup = table.get(iter.next());
	        	TreeSet<Integer> tupsubset = (TreeSet<Integer>) ts.subSet(tup.getStartPoint(), true, tup.getEndPoint(), true);
                Iterator<Integer> itersub = tupsubset.iterator();
                if(itersub.hasNext()){
                	int news = itersub.next(); int newe;
                	while(itersub.hasNext()){
                		newe = itersub.next();
                		Tuple newtup = new Tuple(tup.getDataAttrs(), news, newe);
                		normalizedtbl.add(newtup);
                		news=newe;
                	}
                	
                	
                }
	        }
	    }
	
		return normalizedtbl;
	}
	

}
	
	


