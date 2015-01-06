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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.sxml.Attribute;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Text;


/**
 * For structured data, creates XML representation out of IStructuredDataValue instances 
 * This class is used only for old (IPP 4.5) structured data that have no correspodning CLOB document
 */
public class StructuredDataReader
{
   public static final String PRP_THREAD_LOCAL_DOM_BUILDER = StructuredDataReader.class.getName() + ".DomBuilder";

   private IXPathMap xPathMap;

   public StructuredDataReader(IXPathMap xPathMap)
   {
      this.xPathMap = xPathMap;
   }

   public Document read(Set /*<IStructuredDataValue>*/ entries) throws ParserConfigurationException,
         FactoryConfigurationError
   {
      Element rootElement = StructuredDataXPathUtils.createElement(xPathMap.getRootXPath(), true);
      Document document = new Document(rootElement);

      IStructuredDataValue rootEntry = StructuredDataReader.findRootEntry(entries);

      this.fill(rootElement, this.xPathMap.getRootXPath(), rootEntry, entries);

      rootElement.addAttribute(new Attribute(IStructuredDataValue.STRUCTURED_DATA_NAMESPACE_PREFIX + ":"
                  + IStructuredDataValue.ENTRY_OID_ATTRIBUTE_NAME, 
                  IStructuredDataValue.STRUCTURED_DATA_NAMESPACE, rootEntry.getOID()+""));

      return document;
   }

   private List findElementEntries(IStructuredDataValue parentEntry, Set entries)
   {
      List elementEntries = new LinkedList();

      for (Iterator i = entries.iterator(); i.hasNext();)
      {
         IStructuredDataValue entry = (IStructuredDataValue) i.next();
         if (entry.isElement() == true && (parentEntry.getOID() == entry.getParentOID()))
         {
            elementEntries.add(entry);
         }
      }

      Collections.sort(elementEntries, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            IStructuredDataValue e1 = (IStructuredDataValue) o1;
            IStructuredDataValue e2 = (IStructuredDataValue) o2;
            return e1.getEntryKey().compareTo(e2.getEntryKey());
         }
      });

      return elementEntries;
   }

   private void addElements(Element element, TypedXPath parentXPath, List elementEntries, Set /*<IStructuredDataValue>*/ entries)
   {
      for (Iterator i = elementEntries.iterator(); i.hasNext();)
      {
         IStructuredDataValue entry = (IStructuredDataValue) i.next();
         TypedXPath xPath = (TypedXPath) xPathMap.getXPath(entry.getXPathOID());
         Element childElement = new Element(
               StructuredDataXPathUtils.getLastXPathPart(xPath.getXPath()));
         childElement.addAttribute(new Attribute(IStructuredDataValue.STRUCTURED_DATA_NAMESPACE_PREFIX + ":"
                     + IStructuredDataValue.ENTRY_OID_ATTRIBUTE_NAME, 
                     IStructuredDataValue.STRUCTURED_DATA_NAMESPACE, entry.getOID()+""));

         element.appendChild(childElement);
         this.fill(childElement, xPath, entry, entries);
      }
   }

   private void fill(Element parentElement, TypedXPath parentXPath, IStructuredDataValue parentEntry, Set entries)
   {
      this.addElements(parentElement, parentXPath, this.findElementEntries(parentEntry, entries),
            entries);
      if (parentEntry.getValue() != null)
      {
         String stringValue = StructuredDataValueFactory.convertToString(parentXPath.getType(), parentXPath.getXsdElementName(), parentEntry.getValue());
         parentElement.appendChild(new Text(stringValue));
      }
   }

   /**
    * Search for root entry in the Map 
    * @param entries
    * @return 
    */
   public static IStructuredDataValue findRootEntry(Set entries)
   {
      for (Iterator i = entries.iterator(); i.hasNext(); ) 
      {
         IStructuredDataValue entry = (IStructuredDataValue)i.next();
         if (entry.isRootEntry()) 
         {
            return entry;
         }
      }
      throw new RuntimeException("No root entry found");
   }

}
