package org.apache.servicemix.wsn.push;

public class Counter
{
	private int counter;
	
	public void add()
	{
		counter++;
	}
	
	public void set(int k)
	{
		counter=k;
	}
	
	public int get()
	{
		return counter;
	}
}