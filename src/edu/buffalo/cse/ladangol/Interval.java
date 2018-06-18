package edu.buffalo.cse.ladangol;

import java.security.InvalidParameterException;

//This code is taken from http://www.techiedelight.com/implement-pair-class-java/
//but I did some changes

public class Interval<S, E>{
	
	public final S sp;   	// startpoint of an interval
	public final E ep;  	// endpoint of an interval
	
	// Constructs a new Pair with specified values
	private Interval(S start, E end)
	{
		this.sp = start;
		this.ep = end;
	}
	
	@Override
	// Checks specified object is "equal to" current object or not
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
	
		if (o == null || getClass() != o.getClass())
			return false;
	
		Interval<?, ?> pair = (Interval<?, ?>) o;
	
		// call equals() method of the underlying objects
		if (!sp.equals(pair.sp))
			return false;
		return ep.equals(pair.ep);
	}
	
	@Override
	// Computes hash code for an object to support hash tables
	public int hashCode()
	{
		// use hash codes of the underlying objects
		return 31 * sp.hashCode() + ep.hashCode();
	}
	
	@Override
	public String toString()
	{
		return "[" + sp + ", " + ep + ")";
	}
	
	// Factory method for creating a Typed Pair instance
	public static <S, E> Interval <S, E> of(S a, E b)
	{
		// calls private constructor
		return new Interval<>(a, b);
	}
	public int compare(Object o){
		if (this == o)
			return 0;

		if (o == null || getClass() != o.getClass())
			throw new InvalidParameterException("Class of parameters are different");

		Interval<?, ?> pair = (Interval<?, ?>) o;
		if(sp instanceof Integer && pair.sp instanceof Integer){
			if(Integer.compare((Integer)sp, (Integer)(pair.sp))==0){			
					return Integer.compare((Integer)ep, (Integer)(pair.ep));
	
			}
			else
				return Integer.compare((Integer)sp, (Integer)(pair.sp));
			
		}
		else
		   throw new InvalidParameterException("Cannot handle in this version!");
	}
	
}
