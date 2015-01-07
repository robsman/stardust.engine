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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * Container for the raw deployment data, i.e. the content of the model file.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DeploymentElement implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The byte array containing the model definition in xpdl format.
    */
   private byte[] content;
   
   /**
    * Creates a new deployment element.
    * 
    * @param content the content of the deployment element in binary form.
    */
   public DeploymentElement(byte[] content)
   {
      this.content = content;
   }

   /**
    * Retrieves the content of the deployment element.
    * 
    * @return the raw deployment data.
    */
   public byte[] getContent()
   {
      return content;
   }
}
