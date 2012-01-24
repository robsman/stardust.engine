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
 * This exception is thrown by the engine in any case where an error with the
 * intial configuration of the system.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class ConfigurationException extends Exception
{
   private static final long serialVersionUID = -5188805240482648300L;
   
   /**
    * Default constructor only calls the constructor of the super class
    */
   public ConfigurationException()
   {
      super();
   }

   /**
    * This constructor accepts a message as argument, only calls the
    * appropriate constructor of the super class.
    *
    * @param message  a message that is carried with this exception
    */
   public ConfigurationException(String message)
   {
      super(message);
   }
}
