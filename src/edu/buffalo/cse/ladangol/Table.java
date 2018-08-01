package edu.buffalo.cse.ladangol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.plaf.metal.OceanTheme;


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
		for(int i=0; i < table.size(); i++){
			Tuple temp = table.get(i);
			if(temp.getEndPoint()==switchpoint){
				temp.setEndPoint(Integer.MAX_VALUE);
			}
			
		}
		
	}
	
	public void PntRCon(ArrayList<tFD> tFDs){ 
		boolean isCons = true;
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
				//	HashMap<String, String> tfdMap = new HashMap<String, String>();
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
						if(!tfdMap.get(key.toString()).equals(value.toString())){
				
/*							System.out.println("key " + key+ " is inconsitent ");
							System.out.println("Current time point is"+ currentpoint);
							System.out.println("Value "+ value);
							System.out.println(" prev value " +  tfdMap.get(key.toString()));*/
							isConsistant = false;
							isCons = false;

						}
					}
					else{
						
						tfdMap.put(key.toString(), value.toString());
					}
				}
				if(isCons){
					//building the concrete tuple
					Tuple reptup = new Tuple(tup.getDataAttrs(), currentpoint, currentpoint+1);
					repair.add(reptup);
					
				}
				else{
					isCons=true;
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
		//I give tuples a number so S is an ArrayList<HashSet<Integer>>
		//Shouldn't I check the value toooo????
		//I am normalizing any sets of tuples that have the same key!!!
		ArrayList<HashSet<Integer>> S = new ArrayList<HashSet<Integer>>();
		Hashtable<String, ArrayList<Integer>> tFDHash = new Hashtable<String, ArrayList<Integer>>();ArrayList<Hashtable<String, ArrayList<Integer>>> mayinc = new ArrayList<Hashtable<String, ArrayList<Integer>>>();
		Hashtable<String, HashSet<String>> tFDkeyVals =new Hashtable<String, HashSet<String>>();
		for(int i= 0; i<lhstFDs.size(); i++){			
			int[] tFD = lhstFDs.get(i);
			int[] rhs = tFDs.get(i).getrhs();
			for(int j=0; j< table.size(); j++){
				StringBuilder key = new StringBuilder();
				Tuple tup = table.get(j);
				for(int k=0; k<tFD.length; k++){
					key.append(tup.getDataAttr(tFD[k]-1));
				}
				//key.append(tup.getStartPoint());
				//key.append(tup.getEndPoint());
				String keyStr = key.toString();
				StringBuilder value = new StringBuilder();
				for(int k=0; k<rhs.length; k++){
					value.append(tup.getDataAttr(rhs[k]-1));
				}
				String valuestr = value.toString();
				if(tFDHash.containsKey(keyStr)){
					ArrayList<Integer> temp = tFDHash.get(keyStr);
					temp.add(j); //J is tuple number
					tFDHash.put(keyStr, temp);
					HashSet<String> temphs = tFDkeyVals.get(keyStr);
					temphs.add(valuestr);
					tFDkeyVals.put(keyStr, temphs);
				}
				else{
					ArrayList<Integer> temp = new ArrayList<>();
					temp.add(j);
					tFDHash.put(keyStr, temp);
					HashSet<String> temphs = new HashSet<String>();
					temphs.add(valuestr);
					tFDkeyVals.put(keyStr, temphs);
					
				}
			}
			
		}
		Enumeration<String> iterdeltakeys = tFDHash.keys();
		while(iterdeltakeys.hasMoreElements()){
			String key = iterdeltakeys.nextElement();
			if(tFDkeyVals.get(key).size() == 1){
				tFDHash.remove(key);
				iterdeltakeys = tFDHash.keys();
			}
		}
/*		//Printing the number of conflicts:
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("Conflicts.csv"));
			iterdeltakeys = tFDHash.keys();
			while(iterdeltakeys.hasMoreElements()){
				String key = iterdeltakeys.nextElement();
				ArrayList<Integer> vals  = tFDHash.get(key);
				for(int i=0; i<vals.size(); i++){
					bw.write(table.get(vals.get(i)).getStringTuple());
					bw.write("\n");
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
			Collection<ArrayList<Integer>> sets = tFDHash.values();
			Iterator<ArrayList<Integer>> iter = sets.iterator();
			HashSet<Integer> hs = new HashSet<Integer>();
			HashSet<Integer> conflictupnums = new HashSet<Integer>();
			while(iter.hasNext()){		
				//This just copy the references not the actual values to S
				//When I clear hs then the values in S are being copied too
				hs.addAll(iter.next());
				S.add((HashSet<Integer>) hs.clone());
				conflictupnums.addAll(hs);
				hs.clear();
			}
			
	//	}
		//Merging the sets in S that have a fact in common
		
		int index=0;
		boolean changes = false;
		while(index <S.size()){
			changes = true;
			//HashSet<Integer> temp1 = S.get(index);
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
		
		for(int t=0; t< table.size(); t++){
			if(!conflictupnums.contains(t)){
				normalizedtbl.add(table.get(t));
			}
		}
		
		TreeSet<Integer> ts = new TreeSet<Integer>(); //Storing the sorted distinct start poingts and endpoints
	    for(index =0; index<S.size(); index++){
	    	HashSet<Integer> delta = S.get(index);
	    	ts.clear();
	        Iterator<Integer> iter2 = delta.iterator();
	        while(iter2.hasNext()){
	        	Tuple tup = table.get(iter2.next());
	        	ts.add(tup.getStartPoint());
	        	ts.add(tup.getEndPoint());
	        }
	        
	        iter2 = delta.iterator();
	        while(iter2.hasNext()){
	        	Tuple tup = table.get(iter2.next());
	        	//System.out.println("sp:" + tup.getStartPoint() + " ep: "+ tup.getEndPoint());
	        	TreeSet<Integer> tupsubset = (TreeSet<Integer>) ts.subSet(tup.getStartPoint(), true, tup.getEndPoint(), true);
                Iterator<Integer> itersub = tupsubset.iterator();
                String[] dataattr = tup.getDataAttrs();
                if(itersub.hasNext()){
                	int news = itersub.next(); int newe;
                	while(itersub.hasNext()){
                		newe = itersub.next();
                		Tuple newtup = new Tuple(dataattr, news, newe);
                		normalizedtbl.add(newtup);
                		news=newe;
                	}
                	
                	
                }
	        }
	    }
	    normalizedtbl.switchpoint = switchpoint;
		return normalizedtbl;
		
	}
	public Table IntRCon(ArrayList<tFD> tFDs){
		Table repair = new Table();
		//Building a set that contains all the subset of the facts that are in conflict.
		ArrayList<HashSet<Integer>> conflictsets = new ArrayList<HashSet<Integer>>();
		//ArrayList<HashSet<Integer>> conflictsetsTemp = new ArrayList<HashSet<Integer>>();
		//I am assuming my original instance is already factorized
		ArrayList<int[]> lhstFDs = new ArrayList<int[]>(tFDs.size());
		for(int i=0; i<tFDs.size(); i++){
			lhstFDs.add(tFDs.get(i).getlhs());
		}
		//In the TFDS i do not consider start points and end point but I have to consider them
		//as well since the instance is factorized, each set in the conflict set has the same interval
		//In order to build the sets I need to check if two tuples have the same X R(X,Y') \wedge
		//R(X,Y) --> Y = Y'
		Hashtable<String, ArrayList<Integer>> deltas = new Hashtable<String, ArrayList<Integer>>();
		Hashtable<String, HashSet<String>> tFDkeyVals =new Hashtable<String, HashSet<String>>();
		for(int i= 0; i<lhstFDs.size(); i++){			
			int[] tFD = lhstFDs.get(i);
			int[] rhs = tFDs.get(i).getrhs();
			for(int j=0; j< table.size(); j++){
				StringBuilder key = new StringBuilder();
				Tuple tup = table.get(j);
				for(int k=0; k<tFD.length; k++){
					key.append(tup.getDataAttr(tFD[k]-1));
				}
				key.append(tup.getStartPoint());
				key.append(tup.getEndPoint());
				String keyStr = key.toString();
				StringBuilder value = new StringBuilder();
				for(int k=0; k<rhs.length; k++){
					value.append(tup.getDataAttr(rhs[k]-1));
				}
				String valuestr = value.toString();
				if(deltas.containsKey(keyStr)){
					ArrayList<Integer> temp = deltas.get(keyStr);
					temp.add(j); //J is tuple number
					deltas.put(keyStr, temp);
					HashSet<String> temphs = tFDkeyVals.get(keyStr);
					temphs.add(valuestr);
					tFDkeyVals.put(keyStr, temphs);
				}
				else{
					ArrayList<Integer> temp = new ArrayList<>();
					temp.add(j);
					deltas.put(keyStr, temp);
					HashSet<String> temphs = new HashSet<String>();
					temphs.add(valuestr);
					tFDkeyVals.put(keyStr, temphs);
					
				}
			}
			
		}
		//So deltas contain sets of size 1
		//They are not in a conflict set so we remove them
/*		Iterator<ArrayList<Integer>> iterdelta = deltas.values().iterator();
		while(iterdelta.hasNext()){
			ArrayList<Integer> d = iterdelta.next();
			if(d.size() == 1){
				deltas.values().remove(d);
				iterdelta = deltas.values().iterator();
			}
		}*/
		Enumeration<String> iterdeltakeys = deltas.keys();
		while(iterdeltakeys.hasMoreElements()){
			String key = iterdeltakeys.nextElement();
			if(tFDkeyVals.get(key).size() == 1){
				deltas.remove(key);
				iterdeltakeys = deltas.keys();
			}
		}
		//There are still sets in delta that are not in conflict
		//because they have the same value.

		//If there is one tuple for the key (meaning that the ArrayList<Integer> is of size 1
		//then that is in the repair because it is not conflicting with anything
		//If Delta is of size >1 then there is a conflict 
	//	for(int i=0; i<deltas.size(); i++){
			//delta.values() is of type collection
			Collection<ArrayList<Integer>> sets = deltas.values();
			Iterator<ArrayList<Integer>> iter = sets.iterator();
			HashSet<Integer> conflictuplenums = new HashSet<Integer>();
			while(iter.hasNext()){
				ArrayList<Integer> set = iter.next();
				//if(set.size() == 1){
					//repair.add(table.get(set.get(0)));
				//}
				//else{
					HashSet<Integer> hs = new HashSet<Integer>(); 
					hs.addAll((Collection<? extends Integer>) set.clone());
					conflictuplenums.addAll(hs);
					conflictsets.add(hs);
					//hs.clear();
				//}
			}
			
		//}
		//merging conflicting sets with a common fact
		int index=0;
		boolean changes = false;
		while(index <conflictsets.size()){
			changes = true;
			//HashSet<Integer> temp1 = S.get(index);
			while(changes){
				changes = false;
				for(int j=conflictsets.size()-1; j>index; j--){
					HashSet<Integer> temp2 = conflictsets.get(j);
					if(!Collections.disjoint(conflictsets.get(index), temp2)){
						changes=true;
						conflictsets.get(index).addAll(temp2);
						conflictsets.remove(j);
						
					}
				}
			}
			index++;
		}

       //I have to add the tuples that are not in a conflict set to repair
		for(int t=0; t< table.size(); t++){
			if(!conflictuplenums.contains(t)){
				repair.add(table.get(t));
			}
		}
		//Now repair starts
		//for each \delta in the conflictsets we need to 
		for(int i=0; i<conflictsets.size(); i++){
			HashSet<Integer> delta = conflictsets.get(i);
			//int[] intdelta = delta.toArray();
			Iterator<Integer> iter2 = delta.iterator();
			Table deltafacts = new Table();
			while(iter2.hasNext()){
				deltafacts.add(table.get(iter2.next()));
			}
		  Table tableA = new Table();
		  Random rand = new Random();
		  //choosing a random fact
		  while(deltafacts.Cardinality()> 0){
			  int tuplenumber = rand.nextInt(deltafacts.Cardinality());  //Tbound is exclusive
			  Tuple tup = deltafacts.getTuple(tuplenumber);
			  //choosing a random interval
			  int endpoint = tup.getEndPoint();
			  int startpoint = tup.getStartPoint();
			  rand = new Random();
			  int news = rand.nextInt(endpoint-startpoint)+ startpoint;
			  rand = new Random();
			  int newe = rand.nextInt(endpoint-news) + (news+1);
			  deltafacts.Remove(tuplenumber);
			  if(news > startpoint){
				  Tuple fr = new Tuple(tup.getDataAttrs(), startpoint, news);
				  deltafacts.add(fr);
			  }
			  if(newe < endpoint){
				  Tuple fr = new Tuple(tup.getDataAttrs(), newe, endpoint);
				  deltafacts.add(fr);
			  }
			  Tuple newtup = new Tuple(tup.getDataAttrs(), news, newe);
			  tableA.add(newtup);
			  Table normalized = factorize2(tableA);
			  tableA = findRepair(normalized, newtup, tFDs);
			 
			  
		  }

		  
		  repair.add(tableA);
 
		}
		repair.switchpoint = switchpoint;
		return repair;
	}
	private void add(Table tableA) {
		// TODO Auto-generated method stub
		for(int i=0; i<tableA.Cardinality(); i++){
		      table.add(tableA.getTuple(i));
		}
		
	}
	public Table factorize2(Table input){   //naive normalization
/*		System.out.println("---------------------------------");
		for(int i = 0; i<input.Cardinality(); i++){
			System.out.println(input.getTuple(i).getStringTuple());
		}*/
		Table output = new Table();
		TreeSet<Integer> ts = new TreeSet<Integer>();
		
		//I will put all the start points and End points in a sorted set 
		//I use a TreeSet because I can put distinct start points and end points
		//in it sorted
		//filling the ts with start points and end points
		for(int i=0; i<input.Cardinality(); i++){
			Tuple temp =  input.getTuple(i);
			ts.add(temp.getStartPoint());
			ts.add(temp.getEndPoint());
			
		}
		for(int t=0; t<input.Cardinality(); t++){
			Tuple tup = input.getTuple(t);
			int strtpnt = tup.getStartPoint();
			int endpnt = tup.getEndPoint();
			TreeSet<Integer> tssub = (TreeSet<Integer>) ts.subSet(strtpnt, true, endpnt, true);
			Iterator<Integer> iter = tssub.iterator();
			String[] dataattr = tup.getDataAttrs();
            if(iter.hasNext()){
            	int news = iter.next(); int newe;
            	while(iter.hasNext()){
            		newe = iter.next();
            		Tuple newtup = new Tuple(dataattr, news, newe);
            		output.add(newtup);
            		news=newe;
            	}
			}
		}
/*		System.out.println("----------------normalized--------------");
		for(int i = 0; i<output.Cardinality(); i++){
			System.out.println(output.getTuple(i).getStringTuple());
		}*/
		
		return output;
	}
	public Table findRepair(Table tbl, Tuple tuple, ArrayList<tFD> tFDs){
		//ArrayList<HashSet<Integer>> conflictset = new ArrayList<HashSet<Integer>>();
		//tbl.isConsistant = true;
		if(tbl.Cardinality() == 1){
			return tbl;
		}
		Collections.shuffle(tFDs);
        List<Map<String, String>> tFDsMap = new ArrayList<Map<String, String>>(tFDs.size());
        for(int i=0; i<tFDs.size(); i++){
        	tFDsMap.add(new HashMap<String, String>());
        }
       
        //looping through the tuples
        Table repair = new Table();
       
        HashMap<String, String> tFDsKeyVal = null;
        for(int t=0; t<tbl.Cardinality(); t++){
        	Tuple tup = tbl.getTuple(t);
        	//looping through the constraints
        	boolean isCons = true;
        	for(int d= 0; d<tFDs.size(); d++){
        		
        		//for storing the X attributes and the values
        	    tFDsKeyVal = (HashMap<String, String>) tFDsMap.get(d);
        		tFD tfd = tFDs.get(d);
        		int[] lhs = tfd.getlhs();
        		StringBuilder key = new StringBuilder();
        		for(int i=0; i< lhs.length; i++){
        			key.append(tup.getDataAttr(lhs[i]-1).toLowerCase());
        		}
        		//Adding the start point and end points
        		key.append(tup.getStartPoint());
        		key.append(tup.getEndPoint());

				//Now I have the X = Key and Y= value
				//so the key value of two tuples are the same
				//we can add the tup to repair?
				//No, what about other constraints?
				//Ada, IBM, 14221, USA, [5,10)
				//Ada, IBM, 14221, Canada, [5,10)
				//just go to the next tuple
        		int[] rhs = tfd.getrhs();
				StringBuilder value = new StringBuilder();
				for(int i=0; i<rhs.length; i++){
					value.append(tup.getDataAttr(rhs[i]-1).toLowerCase());
				}
        		if(tFDsKeyVal.containsKey(key.toString())){
        			
					if(!tFDsKeyVal.get(key.toString()).equals(value)){
						isCons = false;
						//System.out.println("key = "+ key.toString() + " and the start point = " + tup.getStartPoint());
					}
        		}
        		else{
        			//System.out.println("key:"+key +" value "+ value );
        			tFDsKeyVal.put(key.toString(), value.toString());
        		}				
        	}
        	if(isCons){
        		repair.add(tup);
        	}
        
        }

		
		
		return repair;
		
	}
	public Tuple Remove(int pos){
		return table.remove(pos);
	}
	public int CountConflicts(ArrayList<tFD> tFDs){
		int num = 0;
		ArrayList<int[]> lhstFDs = new ArrayList<int[]>(tFDs.size());
		for(int i=0; i<tFDs.size(); i++){
			lhstFDs.add(tFDs.get(i).getlhs());
		}
		for(int t = 0; t< table.size(); t++){
			Tuple tup = table.get(t);
			for(int d=0; d<tFDs.size(); d++){
				int[] tFD = lhstFDs.get(d);
				int[] rhs = tFDs.get(d).getrhs();
				StringBuilder key = new StringBuilder();
				for(int k=0; k<tFD.length; k++){
					key.append(tup.getDataAttr(tFD[k]-1));
				}
				String keyStr = key.toString();
				StringBuilder value = new StringBuilder();
				for(int k=0; k<rhs.length; k++){
					value.append(tup.getDataAttr(rhs[k]-1));
				}
				String valuestr = value.toString();
				for( int t2 = t+1; t2 < table.size(); t2++){
					Tuple tupc = table.get(t2);
					key = new StringBuilder();
					for(int k=0; k<tFD.length; k++){
						key.append(tupc.getDataAttr(tFD[k]-1));
					}
					String candid = key.toString();
					if(keyStr.equals(candid)){
						value = new StringBuilder();
						for(int k=0; k<rhs.length; k++){
							value.append(tupc.getDataAttr(rhs[k]-1));
						}
						if(!valuestr.equals(value.toString())){
							if(tup.getStartPoint() <= tupc.getStartPoint() && tupc.getStartPoint() < tup.getEndPoint()){
								num++;
							}
							else if(tupc.getStartPoint() <= tup.getStartPoint() && tup.getStartPoint() < tupc.getEndPoint()){
								num++;
							}
							
						}
					}
				}
			}
		}

		

		return num;
	}
	public int countTuplesInConflict(ArrayList<tFD> tFDs){
		int num = 0;
		ArrayList<int[]> lhstFDs = new ArrayList<int[]>(tFDs.size());
		for(int i=0; i<tFDs.size(); i++){
			lhstFDs.add(tFDs.get(i).getlhs());
		}
		HashSet<Integer> tuplenum = new HashSet<Integer>();
		for(int t = 0; t< table.size(); t++){
			Tuple tup = table.get(t);
			for(int d=0; d<tFDs.size(); d++){
				int[] tFD = lhstFDs.get(d);
				int[] rhs = tFDs.get(d).getrhs();
				StringBuilder key = new StringBuilder();
				for(int k=0; k<tFD.length; k++){
					key.append(tup.getDataAttr(tFD[k]-1));
				}
				String keyStr = key.toString();
				StringBuilder value = new StringBuilder();
				for(int k=0; k<rhs.length; k++){
					value.append(tup.getDataAttr(rhs[k]-1));
				}
				String valuestr = value.toString();
				for( int t2 = t+1; t2 < table.size(); t2++){
					Tuple tupc = table.get(t2);
					key = new StringBuilder();
					for(int k=0; k<tFD.length; k++){
						key.append(tupc.getDataAttr(tFD[k]-1));
					}
					String candid = key.toString();
					if(keyStr.equals(candid)){
						value = new StringBuilder();
						for(int k=0; k<rhs.length; k++){
							value.append(tupc.getDataAttr(rhs[k]-1));
						}
						if(!valuestr.equals(value.toString())){
							if(tup.getStartPoint() <= tupc.getStartPoint() && tupc.getStartPoint() < tup.getEndPoint()){
								//num++;
								//System.out.println(table.get(t).getStringTuple());
								//System.out.println(table.get(t2).getStringTuple());
								tuplenum.add(t);
								tuplenum.add(t2);
							}
							else if(tupc.getStartPoint() <= tup.getStartPoint() && tup.getStartPoint() < tupc.getEndPoint()){
								//num++;
								//System.out.println(table.get(t).getStringTuple());
								//System.out.println(table.get(t2).getStringTuple());
								tuplenum.add(t);
								tuplenum.add(t2);
							}
							
						}
					}
				}
			}
		}
		
		return tuplenum.size();
	}
	

}
	
	


