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

import java.io.IOException;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Filter to stop xml parsing when specific tags are encountered.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class StopFilter extends XMLFilterImpl
{
   private Map<String, Set<String>> stopConditions = new HashMap<String, Set<String>>();
   
   private Stack<Tag> tags = new Stack<Tag>();

   /**
    * Constructs a new stop filter.
    * 
    * @param parent the parent reader. Must not be null.
    */
   public StopFilter(XMLReader parent)
   {
      super(parent);
   }

   /**
    * Specifies the tags that will stop the parsing.
    * 
    * @param namespaceUri the tags namespace.
    * @param localNames an array of local names.
    */
   public void addStopCondition(String namespaceUri, String... localNames)
   {
      Set<String> conditions = stopConditions.get(namespaceUri);
      if (conditions == null)
      {
         conditions = new HashSet<String>(localNames.length);
         stopConditions.put(namespaceUri, conditions);
      }
      for (String condition : localNames)
      {
         conditions.add(condition);
      }
   }
   
   @Override
   public void parse(InputSource input) throws SAXException, IOException
   {
      try
      {
         super.parse(input);
      }
      catch (StopException stop)
      {
         // end parsing gracefully by emulating the end events.
         while (!tags.isEmpty())
         {
            Tag tag = tags.peek();
            endElement(tag.uri, tag.localName, tag.qName);
         }
         endDocument();
      }
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
   {
      // check stop condition
      Set<String> conditions = stopConditions.get(uri);
      if (conditions != null && conditions.contains(localName))
      {
         throw new StopException();
      }
      // record the tags for gracefully stopping
      tags.push(new Tag(uri, localName, qName));

      super.startElement(uri, localName, qName, atts);
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      super.endElement(uri, localName, qName);
      
      // remove the tag record as it was successfully processed
      tags.pop();
   }

   /**
    * Marker exception.
    */
   private static class StopException extends SAXException
   {
      private static final long serialVersionUID = 1L;
   }
   
   /**
    * Simple container class for the element identification.
    * 
    * @author Florin.Herinean
    * @version $Revision: $
    */
   private static class Tag
   {
      /**
       * The namespace uri.
       */
      private String uri;
      
      /**
       * The element local name.
       */
      private String localName;
      
      /**
       * The qualified (prefixed) element name.
       */
      private String qName;

      /**
       * Convenient constructor.
       */
      private Tag(String uri, String localName, String qName)
      {
         this.uri = uri;
         this.localName = localName;
         this.qName = qName;
      }
   }
}