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
package org.eclipse.stardust.engine.api.query;

/**
 * Specifies how to search for linked process instances.
 * 
 * @author Roland.Stamm
 * @version $Revision: $
 */
public enum LinkDirection
{
   /**
    * finds process instances that are linked to the specified one.
    */
   TO,
   
   /**
    * finds process instances that are linked from the specified one.
    */
   FROM,
   
   /**
    * finds process instances that either are linked to or linked from the specified one.
    */
   TO_FROM;
}
