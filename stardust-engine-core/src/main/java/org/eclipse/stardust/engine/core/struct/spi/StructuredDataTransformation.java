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
package org.eclipse.stardust.engine.core.struct.spi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;


/**
 * Parses access path and determines the kind of transformation
 * defined by the access path (ex. <code>DOM(my/specific/xpath)</code>)
 */
public class StructuredDataTransformation
{
   private static final Pattern TRANSFORMATION_PATTERN = Pattern.compile("(DOM) *\\((.*)\\)");

   private StructDataTransformerKey type;

   private String xPath;

   public static StructuredDataTransformation valueOf(String outPath)
   {
      return valueOf(outPath, null);
   }
   
   public static StructuredDataTransformation valueOf(String outPath, AccessPoint targetAccessPointDefinition)
   {
      StructuredDataTransformation result = getExplicitTransformation(outPath);
      
      if ( !result.needsTransformation() && targetAccessPointDefinition != null)
      {
         // if no explicit transformation is set via the XPath, look if the transformation attribute is set
         String transformationAttribute = (String) targetAccessPointDefinition.getAttribute(StructuredDataConstants.TRANSFORMATION_ATT);
         if (StructDataTransformerKey.DOM.equals(transformationAttribute))
         {
            result = new StructuredDataTransformation(StructDataTransformerKey.Dom, outPath);
         }
         else if(StructDataTransformerKey.BEAN.equals(transformationAttribute))
         {
            result = new StructuredDataTransformation(StructDataTransformerKey.Bean, outPath);
         }
      }
      // no changes
      return result;
   }

   private static StructuredDataTransformation getExplicitTransformation(String outPath)
   {
      if (StringUtils.isEmpty(outPath))
      {
         return new StructuredDataTransformation(StructDataTransformerKey.None, outPath);
      }
      Matcher matcher = TRANSFORMATION_PATTERN.matcher(outPath);

      while (matcher.find())
      {
         String extractedType = matcher.group(1);
         String extractedXPath = matcher.group(2);
         if (StructDataTransformerKey.DOM.equals(extractedType))
         {
            return new StructuredDataTransformation(StructDataTransformerKey.Dom, extractedXPath);
         }
         else
         { 
            throw new RuntimeException("Transformation '"+extractedType+"' from expression '"+outPath+"' is not supported");
         }
      }
      return new StructuredDataTransformation(StructDataTransformerKey.None, outPath);
   }

   public StructuredDataTransformation(StructDataTransformerKey type, String xPath)
   {
      this.type = type;
      this.xPath = xPath;
   }

   public boolean isToDOM()
   {
      return StructDataTransformerKey.Dom == type;
   }

   public boolean isToBean()
   {
      return StructDataTransformerKey.Bean == type;
   }

   public boolean needsTransformation()
   {
      if (StructDataTransformerKey.None == type)
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
   public String getXPath()
   {
      return xPath;
   }
   
   public StructDataTransformerKey getType()
   {
      return type;
   }

}

