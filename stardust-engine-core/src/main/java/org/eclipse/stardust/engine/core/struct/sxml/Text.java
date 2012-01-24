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
package org.eclipse.stardust.engine.core.struct.sxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML text
 *
 * @author robert.sauer
 */
public class Text extends LeafNode
{
   private String text;

   /**
    * Creates a new text node having the given value.
    */
   public Text(String text)
   {
      if (null == text)
      {
         text = "";
      }

      this.text = text;
   }

   @Override
   public Text copy()
   {
      return new Text(getValue());
   }

   @Override
   public String toXML()
   {
      return text;
   }

   /**
    * Sets a new value for this text node.
    */
   public void setValue(String value)
   {
      if (null == value)
      {
         value = "";
      }

      this.text = value;
   }

   @Override
   public String getValue()
   {
      return text;
   }

   @Override
   void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException
   {
      xmlWriter.writeCharacters(text);
   }
}
