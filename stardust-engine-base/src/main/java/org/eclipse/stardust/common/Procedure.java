/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.IJobManager;


/**
 * Abstract base class for first order procedures.
 * 
 * @author rsauer
 * @version $Revision$
 * 
 * @see Function
 * @see IJobManager
 */
public abstract class Procedure<T> implements ICallable<T>, Action<T>
{
   
   protected abstract void invoke();
   
   final public T execute()
   {
      return call();
   }

   public T call() throws PublicException
   {
      invoke();
      
      return null;
   }

}
