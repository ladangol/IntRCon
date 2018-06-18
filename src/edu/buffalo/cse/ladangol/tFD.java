package edu.buffalo.cse.ladangol;

public class tFD {
	private int[] lhs;
	private int[] rhs;
	public tFD(String[] lhsStr, String[] rhsStr){
		lhs = new int[lhsStr.length];
		rhs = new int[rhsStr.length];
		for(int i = 0; i< lhsStr.length; i++){
			lhs[i] = Integer.parseInt(lhsStr[i].trim());
		}
		for(int j=0; j< rhsStr.length; j++){
			rhs[j] = Integer.parseInt(rhsStr[j].trim());
		}
	}
	public int[] getlhs(){
		return lhs;
	}
	public int[] getrhs(){
		return rhs;
	}

}
