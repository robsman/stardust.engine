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
package org.eclipse.stardust.engine.core.compatibility.extensions.dms;

import java.util.Collection;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface DocumentSet extends java.util.List/*<org.eclipse.stardust.engine.api.runtime.Document>*/
{
   int getSize();
   
   void addDocument(Document doc);
   
   void addDocuments(DocumentSet docs);
   
   Document getFirstDocument();
   
   Document getDocument(String id);
   
   Document getDocument(int idx);
   
   Collection getDocuments();
   
   void updateDocument(Document doc);
   
   void updateDocuments(DocumentSet docs);
   
   void removeDocument(String id);
   
   void removeDocument(Document doc);

   void removeDocuments(DocumentSet docs);
   
}
