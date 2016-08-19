/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

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