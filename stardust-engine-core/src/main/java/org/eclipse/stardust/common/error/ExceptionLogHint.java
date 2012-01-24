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

package org.eclipse.stardust.common.error;

/**
 * Subclasses of {@link ApplicationException} may implement this interface. If logging
 * is performed by {@link ApplicationException} then these hints may be taken into account.
 *  
 * @author Stephan.Born
 *
 */
public interface ExceptionLogHint
{
   /**
    * Constructor of {@link ApplicationException} might log the original Throwable.
    * This hint determines if this logging will be performed.
    *   
    * @return true: log exception on creation. Otherwise false will be returned. 
    */
   boolean getInitialLogging();
}
