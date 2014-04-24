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
package org.eclipse.stardust.engine.core.struct;

import java.util.Set;

/**
 * Maps XPaths to their OIDs and back
 */
public interface IXPathMap
{

   /**
    * @param xPath
    * @return true if xPath is contained in this xPathMap
    */
   public boolean containsXPath(String xPath);

   /**
    * Return XPath for its OID
    * @param xPathOID
    * @return
    */
   public TypedXPath getXPath(long xPathOID);
   
   /**
    * Return TypedXPath for its String XPath
    * @param xPath
    * @return
    */
   public TypedXPath getXPath(String xPath);

   /**
    * Return OID for an XPath or null if no such XPath is registered
    * @param xPath
    * @return
    */
   public Long getXPathOID(String xPath);
   
   /**
    * Return all XPaths defined for this data
    * @return all XPaths (Set of {@link TypedXPath})
    */
   public Set /*<TypedXPath>*/ getAllXPaths ();

   /**
    * Returns root XPath OID
    * @return root XPath OID
    */
   public Long getRootXPathOID();
   
   /**
    * Returns OIDs of all XPaths contained in this XPath map 
    * @return Set of Long values
    */
   public Set /* <Long> */ getAllXPathOids();

   /**
    * Returns root XPath
    * @return root XPath
    */
   public TypedXPath getRootXPath();
}
