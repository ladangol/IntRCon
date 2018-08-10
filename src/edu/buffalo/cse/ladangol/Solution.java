package edu.buffalo.cse.ladangol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Solution {

	public static void main(String[] args) {
		
		//for(int r = 0; r < 20; r++){
			Table table = new Table();
			ArrayList<tFD> tFDs = new ArrayList<>();
			String filename = "table4";
		
			try {
				FileReader fr = new FileReader("Data/"+filename+".csv");
				BufferedReader bf = new BufferedReader(fr);
				String line;
				//If you need to skip the header
				//
				//bf.readLine();  //skipping the header
				///////////////
				///////
				///////////////
	
				while((line =bf.readLine())!= null){
					String[] values = line.split(",");
					for(int i=0; i<values.length; i++){
						values[i] = values[i].toLowerCase();
					}
					Tuple tuple = new Tuple(values);
					table.add(tuple);		
				}
				bf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//table.reduce();
			
			try {
				
				FileReader fr = new FileReader("Data/tFD2");
				BufferedReader bf = new BufferedReader(fr);
				String line;
				while((line =bf.readLine())!= null){
					String lhsStr = line.substring(0, line.indexOf("-"));
					String[] lhsStrArr = lhsStr.split(",");
				    String rhsStr = line.substring(line.indexOf(">")+1);
				    String[] rhsStrArr = rhsStr.split(",");
					tFDs.add(new tFD(lhsStrArr, rhsStrArr));
				}
				
				bf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
/*			for(int i = 0; i<table.Cardinality(); i++){
				System.out.println(table.getTuple(i).getStringTuple());
			}*/
			System.out.println("Repair:");
			
			
			
			//System.out.println(table.Cardinality());
		//	long startTime = System.nanoTime();
			table.reduce();
			
			//If you are running IntRCon: run one of the normalization algorithms
		 //   table = table.Normalize(tFDs);     //This is smarter normalization
			//table = table.NaiNormalize(table);   // This is naive Normalization
		     
			//System.out.println("after fact: " + table.Cardinality());
			//long startTime = System.nanoTime();
			
			//Point-based Repair Construction
		    table.PntRCon(tFDs);
		     
		   //Interval-based Repair Construction
		 	//table = table.IntRCon(tFDs);
			
		     table.coalesce();
			
		//	 
		  //  System.out.println(table.Cardinality());
			
			//If you wanna see number of conflicts run the following lines:
		//	System.out.println("Number of conflicts is " + table.CountConflicts(tFDs));
	//		System.out.println("Number of Tuples in Conflict is " + table.countTuplesInConflict(tFDs));
																																																																					
		 //  table.expand();
		 //    long stopTime = System.nanoTime();
			
	
		//	long elapsedtime = stopTime-startTime;
	    //	System.out.println("Time passed = "+ (TimeUnit.MILLISECONDS.convert(elapsedtime, TimeUnit.NANOSECONDS)));
			// TODO Auto-generated method stub
		/*	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			//If you want to print the repair in the console uncomment the following loop
			
			for(int i = 0; i<table.Cardinality(); i++){
				System.out.println(table.getTuple(i).getStringTuple());
			}
			
			//If you want to write the repair to a file, uncomment the following block: 
			
			/*try{
				BufferedWriter bw = new BufferedWriter(new FileWriter("Data2/repairs/RepairOf"+filename+".csv"));
			     
				int num=0;
				for(int i = 0; i<table.Cardinality()-1; i++){
					bw.write(table.getTuple(i).getStringTuple());
					bw.write("\n");
					//System.out.println(table.getTuple(i).getStringTuple());
					num++;
				}
				bw.write(table.getTuple(table.Cardinality()-1).getStringTuple());
				System.out.println(num+1);
				bw.flush();
			}
			catch (Exception e) {
				// TODO: handle exception
			}*/
	//	}// end of for (r = 0)


	}

}
