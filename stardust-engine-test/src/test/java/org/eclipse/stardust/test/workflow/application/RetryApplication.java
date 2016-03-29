package org.eclipse.stardust.test.workflow.application;

import java.lang.reflect.InvocationTargetException;

public class RetryApplication 
{
	public static int counter = 0;
	
	public static void setCounter(int counter)
   {
      RetryApplication.counter = counter;
   }

   public RetryApplication()
	{
		
	}

	public static void callInvoke() throws InvocationTargetException
	{
		counter++;		
		
		throw new InvocationTargetException(null);		
	}
	
	public static int getCounter()
	{
		return counter;		
	}
}