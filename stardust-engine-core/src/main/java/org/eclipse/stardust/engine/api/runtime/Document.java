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

import java.util.List;

/**
 * The <code>Document</code> interface represents an existing JCR document.
 *
 * @author rsauer
 * @version $Revision$
 */
public interface Document extends DocumentInfo, Resource
{

   /**
    * Gets the size of a the document content in bytes.
    *
    * @return the size of a the document content in bytes.
    */
   long getSize();

   /**
    * Gets the id of the revision represented by this <code>Document</code> object.
    *
    * @return the id of the revision represented by this <code>Document</code> object.
    */
   String getRevisionId();

   /**
    * Gets the name of the revision represented by this <code>Document</code> object.
    *
    * @return the name of the revision represented by this <code>Document</code> object.
    */
   String getRevisionName();

   /**
    * Gets the version comment of the revision represented by this <code>Document</code> object.
    *
    * @return the version comment of the revision represented by this <code>Document</code> object.
    */
   String getRevisionComment();

   /**
    * Gets all labels assigned to the revision represented by this <code>Document</code> object.
    *
    * @return all labels assigned to the revision represented by this <code>Document</code> object.
    */
   List<String> getVersionLabels();

   /**
    * Gets the encoding of the document content.
    *
    * @return the encoding of the document content.
    */
   String getEncoding();

}
