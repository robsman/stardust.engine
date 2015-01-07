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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;

/**
 * Data transport class that bundles a value and an IN evaluation path.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DataFragmentValue implements Serializable
{
   private static final long serialVersionUID = 1L;

   String path;
   Object value;
   
   public DataFragmentValue(String path, Object value)
   {
      this.path = path;
      this.value = value;
   }
}
