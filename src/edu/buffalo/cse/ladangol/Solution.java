package edu.buffalo.cse.ladangol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Solution {

	public static void main(String[] args) {
		Table table = new Table();
		ArrayList<tFD> tFDs = new ArrayList<>();
		try {
			FileReader fr = new FileReader("Data/table.csv");
			BufferedReader bf = new BufferedReader(fr);
			String line;
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
		
		table.reduce();
		try {
			FileReader fr = new FileReader("Data/tfd");
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
		
		for(int i = 0; i<table.Cardinality(); i++){
			System.out.println(table.getTuple(i).getStringTuple());
		}
		System.out.println("Repair:");
		
		
		table.reduce();
		table = table.Factorize(tFDs);
	   // table.PntRCon(tFDs);
		//table.coalesce();
		//table.expand();
		for(int i = 0; i<table.Cardinality(); i++){
			System.out.println(table.getTuple(i).getStringTuple());
		}
		// TODO Auto-generated method stub

	}

}
