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
 * Container class for the deployment options.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class LinkingOptions implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The deployment comment.
    */
   private String comment;
   
   public String getComment()
   {
      return comment;
   }
   
   public void setComment(String comment)
   {
      this.comment = comment;
   }
}
