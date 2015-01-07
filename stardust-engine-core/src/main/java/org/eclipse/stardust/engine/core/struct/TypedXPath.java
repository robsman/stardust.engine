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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator;


/**
 * Represents an XPath expression with a type
 */
public class TypedXPath implements Serializable
{

   private static final long serialVersionUID = 1L;

   /**
    * String XPath (without indexes)
    */
   private final String xPath;

   /**
    * BigData constant describing the type of the data stored
    * at this XPath. BigData.NULL is returned for complexTypes
    * and Lists
    */
   private final int type;

   /**
    * true if the XPath references an element with maxOccurs>1
    */
   private final boolean isList;

   /**
    * Ordering integer to keep the right order of elements. Is needed
    * to restore DOM from Collections (since java.util.Map has no
    * defined order for keys)
    */
   private final int orderKey;

   /**
    * Name of the xsd element (without namespace or prefix)
    *
    * TODO combine name, ns into one field of type QName
    */
   private final String xsdElementName;

   /**
    * Namespace of the xsd element
    */
   private final String xsdElementNs;

   /**
    * Name of the xsd type (without namespace or prefix)
    *
    * TODO combine name, ns into one field of type QName
    */
   private final String xsdTypeName;

   /**
    * Namespace of the xsd type
    */
   private final String xsdTypeNs;

   /**
    * Annotations applied to this XPath during modeling
    */
   private final XPathAnnotations xPathAnnotations;

   /**
    * Precomiled evaluator for this xPath (lazily initialized)
    */
   private XPathEvaluator namespaceAwareCompiledXPath;

   /**
    * Precomiled evaluator for this xPath (lazily initialized)
    */
   private XPathEvaluator notNamespaceAwareCompiledXPath;


   private TypedXPath parentXPath;

   private List<TypedXPath> childXPaths = CollectionUtils.newArrayList();

   private List /* <Object> */ enumerationValues;

   private boolean isAttribute;

   private boolean wildcards;

   public static class Builder
   {
      private final TypedXPath parentXPath;
      private final int orderKey;
      private final String xPath;
      private final int type;

      private boolean isList = false;
      private String xsdElementName;
      private String xsdElementNs;
      private String xsdTypeName;
      private String xsdTypeNs;
      private XPathAnnotations xPathAnnotations = XPathAnnotations.DEFAULT_ANNOTATIONS;
      private List /* <Object> */ enumerationValues = Collections.EMPTY_LIST;

      public Builder(TypedXPath parentXPath, int orderKey, String xPath, int type)
      {
         this.parentXPath = parentXPath;
         this.orderKey = orderKey;
         this.xPath = xPath;
         this.type = type;
      }

      public void setList(boolean isList)
      {
         this.isList = isList;
      }

      public Builder xsdElementName(String xsdElementName)
      {
         this.xsdElementName = xsdElementName;
         return this;
      }

      public Builder setXsdElementNs(String xsdElementNs)
      {
         this.xsdElementNs = xsdElementNs;
         return this;
      }

      public Builder xsdTypeName(String xsdTypeName)
      {
         this.xsdTypeName = xsdTypeName;
         return this;
      }

      public Builder xsdTypeNs(String xsdTypeNs)
      {
         this.xsdTypeNs = xsdTypeNs;
         return this;
      }

      public Builder xPathAnnotations(XPathAnnotations pathAnnotations)
      {
         xPathAnnotations = pathAnnotations;
         return this;
      }

      public Builder enumerationValues(List enumerationValues)
      {
         this.enumerationValues = enumerationValues;
         return this;
      }

      public TypedXPath build()
      {
         return new TypedXPath(this);
      }

   }

   private TypedXPath(Builder builder)
   {
      this.orderKey = builder.orderKey;
      this.xPath = builder.xPath;
      this.type = builder.type;
      this.isList = builder.isList;
      this.xsdElementName = builder.xsdElementName;
      this.xsdElementNs = builder.xsdElementNs;
      this.xsdTypeName = builder.xsdTypeName;
      this.xsdTypeNs = builder.xsdTypeNs;
      this.xPathAnnotations = builder.xPathAnnotations;
      this.parentXPath = builder.parentXPath;
      if (this.parentXPath != null)
      {
         // if not root, register itself at parent
         this.parentXPath.addChildXPath(this);
      }
      this.enumerationValues = builder.enumerationValues;
   }

   protected TypedXPath(TypedXPath typedXPath)
   {
      this.orderKey = typedXPath.orderKey;
      this.xPath = typedXPath.xPath;
      this.type = typedXPath.type;
      this.isList = typedXPath.isList;
      this.xsdElementName = typedXPath.xsdElementName;
      this.xsdElementNs = typedXPath.xsdElementNs;
      this.xsdTypeName = typedXPath.xsdTypeName;
      this.xsdTypeNs = typedXPath.xsdTypeNs;
      this.xPathAnnotations = typedXPath.xPathAnnotations;
      this.parentXPath = typedXPath.parentXPath;
      this.namespaceAwareCompiledXPath = typedXPath.namespaceAwareCompiledXPath;
      this.notNamespaceAwareCompiledXPath = typedXPath.notNamespaceAwareCompiledXPath;
      this.childXPaths = CollectionUtils.copyList(typedXPath.childXPaths);
      this.enumerationValues = CollectionUtils.copyList(typedXPath.enumerationValues);
   }

   public TypedXPath(TypedXPath parentXPath, int orderKey, String xPath,
         String xsdTypeName, String xsdTypeNs, int type, boolean isList,
         XPathAnnotations xPathAnnotations)
   {
      this(parentXPath, orderKey, xPath, false, null, null, xsdTypeName, xsdTypeNs, type,
            isList, xPathAnnotations, Collections.EMPTY_LIST);
   }

   public TypedXPath(TypedXPath parentXPath, int orderKey, String xPath, //
         boolean isAttribute, String xsdElementName, String xsdElementNs, //
         String xsdTypeName, String xsdTypeNs, //
         int type, boolean isList, XPathAnnotations xPathAnnotations, List /*<Object*/ enumerationValues)
   {
      this.orderKey = orderKey;
      this.xPath = xPath;
      this.type = type;
      this.isList = isList;
      this.isAttribute = isAttribute;
      this.xsdElementName = xsdElementName;
      this.xsdElementNs = xsdElementNs;
      this.xsdTypeName = xsdTypeName;
      this.xsdTypeNs = xsdTypeNs;
      this.xPathAnnotations = xPathAnnotations;
      this.parentXPath = parentXPath;
      if (this.parentXPath != null)
      {
         // if not root, register itself at parent
         this.parentXPath.addChildXPath(this);
      }
      this.enumerationValues = enumerationValues;
   }

   /**
    * Gets the parent XPath of this XPath
    * @return parent XPath or null if this XPath represents the root XPath
    */
   public TypedXPath getParentXPath()
   {
      return this.parentXPath;
   }

   private void addChildXPath(TypedXPath typedXPath)
   {
      this.childXPaths.add(typedXPath);
   }

   public List<TypedXPath> getChildXPaths()
   {
      return this.childXPaths;
   }

   public TypedXPath getChildXPath(String name)
   {
      for (TypedXPath childXPath : childXPaths)
      {
         String propertyName = StructuredDataXPathUtils.getLastXPathPart(childXPath.xPath);
         if (CompareHelper.areEqual(name, propertyName))
         {
            return childXPath;
         }
      }
      return null;
   }

   public XPathEvaluator getCompiledXPath(boolean namespaceAware)
   {
      // lazy initialization
      if (namespaceAware)
      {
         if (this.namespaceAwareCompiledXPath == null)
         {
            if ("".equals(xPath))
            {
               this.namespaceAwareCompiledXPath = StructuredDataXPathUtils.createXPathEvaluator("/", StructuredDataXPathUtils.findRootXPath(this), namespaceAware);
            }
            else
            {
               this.namespaceAwareCompiledXPath = StructuredDataXPathUtils.createXPathEvaluator(xPath, StructuredDataXPathUtils.findRootXPath(this), namespaceAware);
            }
         }
         return this.namespaceAwareCompiledXPath;
      }
      else
      {
         if (this.notNamespaceAwareCompiledXPath == null)
         {
            if ("".equals(xPath))
            {
               this.notNamespaceAwareCompiledXPath = StructuredDataXPathUtils.createXPathEvaluator("/", StructuredDataXPathUtils.findRootXPath(this), namespaceAware);
            }
            else
            {
               this.notNamespaceAwareCompiledXPath = StructuredDataXPathUtils.createXPathEvaluator(xPath, StructuredDataXPathUtils.findRootXPath(this), namespaceAware);
            }
         }
         return this.notNamespaceAwareCompiledXPath;
     }
   }

   /**
    * Gets the name of the xsd type (without namespace or prefix)
    * @return name of the xsd type (without namespace or prefix)
    */
   public String getXsdElementName()
   {
      return xsdElementName;
   }

   /**
    * Gets the namespace URI of the XSD type
    * @return namespace URI of the XSD type
    */
   public String getXsdElementNs()
   {
      return xsdElementNs;
   }

   /**
    * Gets the name of the xsd type (without namespace or prefix)
    * @return name of the xsd type (without namespace or prefix)
    */
   public String getXsdTypeName()
   {
      return xsdTypeName;
   }

   /**
    * Gets the namespace URI of the XSD type
    * @return namespace URI of the XSD type
    */
   public String getXsdTypeNs()
   {
      return xsdTypeNs;
   }

   public boolean isList()
   {
      return this.isList;
   }

   public String getXPath()
   {
      return xPath;
   }

   public int getType()
   {
      return type;
   }

   public int getOrderKey()
   {
      return this.orderKey;
   }

   public XPathAnnotations getAnnotations()
   {
      return this.xPathAnnotations;
   }

   public boolean isEnumeration()
   {
      if (this.enumerationValues.isEmpty())
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public List /*<Object>*/ getEnumerationValues()
   {
      return Collections.unmodifiableList(this.enumerationValues);
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((xPath == null) ? 0 : xPath.hashCode());
      return result;
   }

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final TypedXPath other = (TypedXPath) obj;
      if (xPath == null)
      {
         if (other.xPath != null)
            return false;
      }
      else if ( !xPath.equals(other.xPath))
         return false;
      return true;
   }

   public boolean isAttribute()
   {
      return isAttribute;
   }

   public String getId()
   {
      String id = xsdElementName;
      if (id == null)
      {
         id = xsdTypeName;
      }
      if (isAttribute)
      {
         id = "@" + id;
      }
      return id;
   }

   public String toString()
   {
      return xPath;
   }

   /**
    * For internal use only.
    */
   public void enableWildcards()
   {
      wildcards = true;
   }

   public boolean hasWildcards()
   {
      return wildcards;
   }
}
