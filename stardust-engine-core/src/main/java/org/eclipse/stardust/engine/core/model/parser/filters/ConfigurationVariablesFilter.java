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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;

/**
 * Filter to replace/fix namespaces.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class ConfigurationVariablesFilter
{
   public static XMLStreamReader createXMLStreamReader(IConfigurationVariablesProvider filter, XMLStreamReader reader)
   {
      return new NamespaceStreamReaderDelegate(filter, reader);
   }

   ConfigurationVariablesFilter()
   {}

   private static class NamespaceStreamReaderDelegate extends StreamReaderDelegate
   {
      private IConfigurationVariablesProvider filter;

      private char[] text;
      private int diff;

      private NamespaceStreamReaderDelegate(IConfigurationVariablesProvider filter, XMLStreamReader reader)
      {
         super(reader);
         this.filter = filter;
      }

      @Override
      public int next() throws XMLStreamException
      {
         text = null;
         return super.next();
      }

      @Override
      public int nextTag() throws XMLStreamException
      {
         text = null;
         return super.nextTag();
      }

      @Override
      public String getElementText() throws XMLStreamException
      {
         return evaluateVariables(super.getElementText());
      }

      @Override
      public String getAttributeValue(String namespaceUri, String localName)
      {
         return evaluateVariables(super.getAttributeValue(namespaceUri, localName));
      }

      @Override
      public String getAttributeValue(int index)
      {
         return evaluateVariables(super.getAttributeValue(index));
      }

      @Override
      public String getText()
      {
         return evaluateVariables(super.getText());
      }

      @Override
      public int getTextCharacters(int sourceStart, char[] target, int targetStart,
            int length) throws XMLStreamException
      {
         fetchText();
         if (sourceStart >= text.length || targetStart >= target.length)
         {
            return 0;
         }
         length = Math.min(target.length - targetStart, length);
         length = Math.min(text.length - sourceStart, length);
         System.arraycopy(text, sourceStart, target, targetStart, length);
         return length;
      }

      @Override
      public char[] getTextCharacters()
      {
         fetchText();
         return text;
      }

      @Override
      public int getTextLength()
      {
         fetchText();
         return super.getTextLength() + diff;
      }

      private void fetchText()
      {
         if (text == null)
         {
            String actual = new String(super.getTextCharacters());
            String evaluated = evaluateVariables(actual);
            diff = evaluated.length() - actual.length();
            text = evaluated.toCharArray();
         }
      }

      private String evaluateVariables(String actual)
      {
         return StringUtils.isEmpty(actual) ? actual : ConfigurationVariableUtils.evaluate(filter, actual);
      }
   }
}