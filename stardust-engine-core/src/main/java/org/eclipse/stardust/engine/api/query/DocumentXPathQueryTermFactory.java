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
package org.eclipse.stardust.engine.api.query;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.query.DocumentQuery.MetadataFilterBuilder;
import org.eclipse.stardust.engine.core.persistence.Operator;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.persistence.Operator.Ternary;
import org.eclipse.stardust.engine.core.persistence.Operator.Unary;
import org.eclipse.stardust.engine.core.thirdparty.encoding.Text;

import org.eclipse.stardust.vfs.MetaDataLocation;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsOperations;


import org.eclipse.stardust.vfs.MetaDataLocation;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsOperations;

public class DocumentXPathQueryTermFactory
{
   private final String localMetaDataAttribute;

   private DateFormat dateFormat;

   public DocumentXPathQueryTermFactory(MetaDataLocation metaDataLocation)
   {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

      if (MetaDataLocation.LOCAL.equals(metaDataLocation))
      {
         localMetaDataAttribute = DocumentXPathQueryAttributes.VFS_META_DATA;
      }
      else
      {
         localMetaDataAttribute = "";
      }
   }

   public String buildFilterTerm(String attribute, Unary operator)
   {
      String jcrAttribute = inferJcrAttribute(attribute, operator);
      String jcrOperator = toJcr(operator);

      StringBuffer buf = new StringBuffer();

      buf.append(jcrOperator).append(jcrAttribute);

      if (needsJcrFunction(operator))
      {
         buf.append(")");
      }

      return buf.toString();
   }

   public String buildFilterTerm(String attribute, Binary operator, Object value)
   {
      String jcrAttribute = "";
      String jcrOperator = "";
      String jcrValue = "";
      boolean isFunctionSyntax = needsJcrFunction(operator);

      if (DocumentQuery.DATE_CREATED.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.DATE_CREATED;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrDateTime(value);
      }
      else if (DocumentQuery.DATE_LAST_MODIFIED.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.DATE_LAST_MODIFIED;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrDateTime(value);
      }
      else if (DocumentQuery.CONTENT.getAttributeName().equals(attribute))
      {
         if ( Binary.LIKE.equals(operator))
         {
            jcrAttribute = DocumentXPathQueryAttributes.CONTENT;
         }
         else
         {
            jcrAttribute = DocumentXPathQueryAttributes.CONTENT_DATA;
         }
         jcrOperator = toJcr(operator, true);
         jcrValue = toJcrString(value);
      }
      else if (DocumentQuery.CONTENT_TYPE.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.CONTENT_TYPE;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }
      else if (DocumentQuery.ID.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.ID;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(stripUuidPrefix(value));
      }
      else if (DocumentQuery.NAME.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.NAME;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }
      else if (DocumentQuery.DOCUMENT_TYPE_ID.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.ATTRIBUTES_TYPE_ID;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }
      else if (DocumentQuery.DOCUMENT_TYPE_SCHEMA_LOCATION.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.ATTRIBUTES_TYPE_SCHEMA_LOCATION;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }
      else if (DocumentQuery.OWNER.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.OWNER;
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }
      else if (MetadataFilterBuilder.isAny(attribute))
      {
         if ( !Binary.LIKE.equals(operator))
         {
            throw new PublicException(
                  "DocumentQuery.META_DATA.any() only supports the LIKE operator.");
         }

         // jackrabbit only supports . in conjunction with jcr:contains() (instead of @*
         // and all operators)
         jcrAttribute = localMetaDataAttribute
               + DocumentXPathQueryAttributes.META_DATA_ANY;
         jcrOperator = toJcr(Binary.LIKE, true);
         isFunctionSyntax = true;
         jcrValue = toJcrString(value);

         // jcrAttribute = localMetaDataAttribute + "vfs:attributes//@*";
         // jcrOperator = toJcr(operator, false);
         // jcrValue = toJcrString(value);
      }
      else if (MetadataFilterBuilder.isNamed(attribute))
      {
         jcrAttribute = localMetaDataAttribute
               + DocumentXPathQueryAttributes.META_DATA_NAMED
               + MetadataFilterBuilder.getPropertyName(attribute);
         jcrOperator = toJcr(operator, false);
         jcrValue = toJcrString(value);
      }

      if (Binary.LIKE.equals(operator))
      {
         jcrValue = jcrValue.replace("*", "%");
      }

      StringBuffer buf = new StringBuffer();

      if ( !isFunctionSyntax)
      {
         buf.append(jcrAttribute).append(" ").append(jcrOperator).append(" ").append(
               jcrValue);
      }
      else
      {
         buf.append(jcrOperator)
               .append(jcrAttribute)
               .append(", ")
               .append(jcrValue)
               .append(")");
      }

      return buf.toString();
   }

   public String buildFilterTerm(String attribute, Ternary operator, Object value)
   {
      String jcrOperatorGE = "";
      String jcrOperatorLE = "";
      String jcrValue1 = "";
      String jcrValue2 = "";
      String jcrAttribute = inferJcrAttribute(attribute, operator);

      if (Ternary.BETWEEN.equals(operator))
      {
         jcrOperatorGE = toJcr(Binary.GREATER_OR_EQUAL, false);
         jcrOperatorLE = toJcr(Binary.LESS_OR_EQUAL, false);

         if (value instanceof Pair)
         {
            if (isDateValueAttribute(attribute))
            {
               jcrValue1 = toJcrDateTime(((Pair) value).getFirst());
               jcrValue2 = toJcrDateTime(((Pair) value).getSecond());
            }
            else
            {
               jcrValue1 = toJcrString(((Pair) value).getFirst());
               jcrValue2 = toJcrString(((Pair) value).getSecond());
            }
         }
      }
      else
      {
         throw new PublicException("Operator not supported: " + operator);
      }

      StringBuffer buf = new StringBuffer();

      buf.append("(")
            .append(jcrAttribute)
            .append(" ")
            .append(jcrOperatorGE)
            .append(" ")
            .append(jcrValue1);
      buf.append(" and ").append(jcrAttribute).append(" ").append(jcrOperatorLE).append(
            " ").append(jcrValue2).append(")");

      return buf.toString();
   }

   private boolean isDateValueAttribute(String attribute)
   {
      if (DocumentQuery.DATE_CREATED.getAttributeName().equals(attribute))
      {
         return true;
      }
      else if (DocumentQuery.DATE_LAST_MODIFIED.getAttributeName().equals(attribute))
      {
         return true;
      }
      return false;
   }

   private String inferJcrAttribute(String attribute, Operator operator)
   {
      String jcrAttribute;
      if (DocumentQuery.DATE_CREATED.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.DATE_CREATED;
      }
      else if (DocumentQuery.DATE_LAST_MODIFIED.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.DATE_LAST_MODIFIED;
      }
      else if (DocumentQuery.CONTENT.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.CONTENT_DATA;
      }
      else if (DocumentQuery.CONTENT_TYPE.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.CONTENT_TYPE;
      }
      else if (DocumentQuery.ID.getAttributeName().equals(attribute))
      {
         jcrAttribute = DocumentXPathQueryAttributes.ID;
      }
      else if (DocumentQuery.NAME.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.NAME;
      }
      else if (DocumentQuery.OWNER.getAttributeName().equals(attribute))
      {
         jcrAttribute = localMetaDataAttribute + DocumentXPathQueryAttributes.OWNER;
      }
      else if (MetadataFilterBuilder.isAny(attribute))
      {
         if ( !Binary.LIKE.equals(operator))
         {
            throw new PublicException(
                  "Attribute META_DATA.any() only supports the LIKE operator.");
         }
         // jackrabbit only supports . in conjunction with jcr:contains() (instead of @*
         // and all operators)
         jcrAttribute = localMetaDataAttribute
               + DocumentXPathQueryAttributes.META_DATA_ANY;

         // jcrAttribute = localMetaDataAttribute + "vfs:attributes//@*";
      }
      else if (MetadataFilterBuilder.isNamed(attribute))
      {
         jcrAttribute = localMetaDataAttribute
               + DocumentXPathQueryAttributes.META_DATA_NAMED
               + MetadataFilterBuilder.getPropertyName(attribute);
      }
      else
      {
         throw new PublicException("Attribute not supported");
      }

      return jcrAttribute;
   }

   private boolean needsContainsSyntax(Operator operator)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   private boolean needsJcrFunction(Operator operator)
   {
      if (Binary.LIKE.equals(operator))
      {
         return true;
      }
      else if (Unary.IS_NULL.equals(operator))
      {
         return true;
      }
      return false;
   }

   private String stripUuidPrefix(final Object value)
   {
      String jcrString = null;
      if (value instanceof String)
      {
         jcrString = (String) value;
      }

      String ret = null;
      if (jcrString != null)
      {
         ret = jcrString.replace(JcrVfsOperations.PREFIX_JCR_UUID, "").replace(
               JcrVfsOperations.PREFIX_JCR_REVISION, "");
      }

      if (ret == null)
      {
         ret = jcrString;
      }

      return ret;
   }

   private String escapeString(String string)
   {
      return Text.escapeIllegalXpathSearchChars(string).replaceAll("'", "''");
   }

   private String toJcrString(Object value)
   {
      if (value instanceof String)
      {
         return "'" + escapeString((String) value) + "'";
      }
      else if (value instanceof Boolean)
      {
         return (String) "'" + ((Boolean) value).toString() + "'";
      }
      else if (value instanceof Number)
      {
         return value.toString();
      }
      return "'" + escapeString(value.toString()) + "'";
   }

   private String toJcrDateTime(Object value)
   {
      if (value instanceof Long)
      {
         Date date = new Date((Long) value);
         String time = dateFormat.format(date);
         return "xs:dateTime('" + time + "')";
      }
      else if (value instanceof Date)
      {
         Date date = (Date) value;
         String time = dateFormat.format(date);
         return "xs:dateTime('" + time + "')";
      }
      else if (value instanceof String)
      {
         return "xs:dateTime('" + value + "')";
      }
      // Calendar cal = Calendar.getInstance();
      // cal.setTime(date);
      // String time = DatatypeConverter.printDateTime(cal);
      throw new PublicException(
            "Only the long or string representation of a date is supported. "
                  + dateFormat.getNumberFormat());
   }

   private String toJcr(Unary operator)
   {
      String ret;
      if (Unary.IS_NULL.equals(operator))
      {
         ret = "not(";
      }
      else if (Binary.IS_NOT_NULL.equals(operator))
      {
         ret = "";
      }
      else
      {
         throw new PublicException("Operator not supported: " + operator);
      }
      return ret;
   }

   private String toJcr(Binary operator, boolean isContent)
   {
      String ret;
      if (Binary.LESS_OR_EQUAL.equals(operator))
      {
         ret = "le";
      }
      else if (Binary.LESS_THAN.equals(operator))
      {
         ret = "lt";
      }
      else if (Binary.IS_EQUAL.equals(operator))
      {
         ret = "eq";
      }
      else if (Binary.GREATER_THAN.equals(operator))
      {
         ret = "gt";
      }
      else if (Binary.GREATER_OR_EQUAL.equals(operator))
      {
         ret = "ge";
      }
      else if (Binary.NOT_EQUAL.equals(operator))
      {
         ret = "ne";
      }
      else if (Binary.LIKE.equals(operator))
      {
         if (isContent)
         {
            ret = "jcr:contains(";
         }
         else
         {
            ret = "jcr:like(";
         }
      }
      else
      {
         throw new PublicException("Operator not supported: " + operator);
      }
      return ret;
   }

}
