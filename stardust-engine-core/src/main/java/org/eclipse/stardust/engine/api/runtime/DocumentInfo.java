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

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;

/**
 * The <code>DocumentInfo</code> keeps information common to
 * both existing and not yet existing JCR documents.
 *
 * @author sauer
 * @version $Revision$
 */
public interface DocumentInfo extends ResourceInfo
{

   /**
    * Gets the content type of the JCR document (e.g. "application/octet-stream").
    *
    * @return the content type of the JCR document.
    */
   String getContentType();

   /**
    * Sets the content type of the JCR document.
    *
    * @param type the content type of the JCR document.
    */
   void setContentType(String type);

   /**
    * @return
    */
   DocumentType getDocumentType();

   /**
    * @param documentType
    */
   void setDocumentType(DocumentType documentType);


   /**
    * @return
    */
   DocumentAnnotations getDocumentAnnotations();

   /**
    * @param documentAnnotations
    */
   void setDocumentAnnotations(DocumentAnnotations documentAnnotations);

}
