/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.parser.filters;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Filter to replace/fix namespaces.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class NamespaceFilter extends XMLFilterImpl
{
   private Map<String, String> replacedNamespaces = new HashMap<String, String>();

   /**
    * Constructs a new namespace filter.
    * 
    * @param parent the parent reader. Must not be null.
    */
   public NamespaceFilter(XMLReader parent)
   {
      super(parent);
   }

   /**
    * Specifies a replacement namespace uri.<br>
    * <br>
    * Example:
    * <code>setReplacement("http://www.wfmc.org/2002/XPDL1.0",
    *       "http://www.wfmc.org/2008/XPDL2.1")</code>
    * will specify that all occurrences of the XPDL 1.0 namespace uri should be replaced with the XPDL 2.1 namespace uri.<br>
    * <br>
    * This method can be used to specify more than one replacement.
    * <br>
    * @param namespaceUri the namespace to be replaced.
    * @param replacementUri the replacement uri.
    */
   public void addReplacement(String namespaceUri, String replacementUri)
   {
      replacedNamespaces.put(namespaceUri, replacementUri);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
   {
      super.startElement(replace(uri), localName, qName, atts);
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      super.endElement(replace(uri), localName, qName);
   }

   @Override
   public void startPrefixMapping(String prefix, String uri) throws SAXException
   {
      super.startPrefixMapping(prefix, replace(uri));
   }
   
   private String replace(String uri)
   {
      String replacement = replacedNamespaces.get(uri);
      return replacement == null ? uri : replacement;
   }
}