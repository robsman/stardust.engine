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

import org.eclipse.stardust.engine.api.runtime.Document;

public class DocumentQuery extends Query
{
   private static final long serialVersionUID = 1L;

   /**
    * Allows to define a filter on the document ID.
    */
   public static final FilterableAttribute ID = new Attribute("id");

   /**
    * Allows to define a filter on the document name.
    */
   public static final FilterableAttribute NAME = new Attribute("name");

   /**
    * Allows to define a filter on the document's content type.
    */
   public static final FilterableAttribute CONTENT_TYPE = new Attribute("contentType");

   /**
    * Allows to define a filter on the document owner.
    */
   public static final FilterableAttribute OWNER = new Attribute("owner");

   /**
    * Allows to define a filter on the document's creation date.
    */
   public static final FilterableAttribute DATE_CREATED = new Attribute("dateCreated");

   /**
    * Allows to define a filter on the document's last modification date.
    */
   public static final FilterableAttribute DATE_LAST_MODIFIED = new Attribute(
         "dateLastModified");

   /**
    * Allows to define a filter on the document's documentType id.
    */
   public static final FilterableAttribute DOCUMENT_TYPE_ID = new Attribute(
         "attributesTypeId");

   /**
    * Allows to define a filter on the document's documentType schema location.
    */
   public static final FilterableAttribute DOCUMENT_TYPE_SCHEMA_LOCATION = new Attribute(
         "attributesTypeSchemaLocation");

   /**
    * Allows to define a filter on the document content.
    * <p>
    * Please note that only some document types (like plain text, PDF, Microsoft Office
    * documents) support content indexing. Which document types are supported is implied
    * by the concrete DMS.
    */
   public static final FilterableAttribute CONTENT = new Attribute("content");

   /**
    * Allows to define a filter on the document's metadata attributes.
    * <p>
    * Supports filters on any attribute (e.g. is there any attribute containing a certain
    * text fragment) or specific attributes (is there an attribute with a given name,
    * containing a certain text fragment).
    */
   public static final MetadataFilterBuilder META_DATA = new MetadataFilterBuilder();

   // // attribute aliases to cater for use of static imports

   /**
    * Alias for {@link #ID}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_ID = ID;

   /**
    * Alias for {@link #NAME}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_NAME = NAME;

   /**
    * Alias for {@link #CONTENT_TYPE}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_CONTENT_TYPE = CONTENT_TYPE;

   /**
    * Alias for {@link #OWNER}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_OWNER = OWNER;

   /**
    * Alias for {@link #DATE_CREATED}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_CREATED = DATE_CREATED;

   /**
    * Alias for {@link #DATE_LAST_MODIFIED}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_LAST_MODIFIED = DATE_LAST_MODIFIED;

   /**
    * Alias for {@link #CONTENT}, suitable for static imports.
    */
   public static final FilterableAttribute DOC_CONTENT = CONTENT;

   /**
    * Alias for {@link #META_DATA}, suitable for static imports.
    */
   public static final MetadataFilterBuilder DOC_META_DATA = META_DATA;

   private static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] { //
               FilterTerm.class, //
               UnaryOperatorFilter.class, //
               BinaryOperatorFilter.class, //
               TernaryOperatorFilter.class, //
               CurrentPartitionFilter.class}), //
         DocumentQuery.class);

   public static final CustomOrderCriterion RELEVANCE = new CustomOrderCriterion(
         Document.class, "score");

   /**
    * Creates a query for finding all documents currently existing.
    *
    * @return The readily configured query.
    */
   public static DocumentQuery findAll()
   {
      return new DocumentQuery();
   }

   public DocumentQuery()
   {
      super(FILTER_VERIFYER);
   }

   public static final class MetadataFilterBuilder
   {
      private static final String ANY = "documentQuery:metaDataFilter:any";

      private static final String NAMED = "documentQuery:metaDataFilter:named";

      /**
       * Define a filter targeting any metadata property.
       */
      public FilterableAttribute any()
      {
         return new Attribute(ANY);
      }

      /**
       * Define a filter targeting a specific metadata property.
       */
      public FilterableAttribute withName(String propertyName)
      {
         return new Attribute(NAMED + ":" + propertyName);
      }

      public static boolean isNamed(String attribute)
      {
         if (attribute == null)
         {
            return false;
         }
         return attribute.startsWith(NAMED);
      }

      public static boolean isAny(String attribute)
      {
         return ANY.equals(attribute);
      }

      public static String getPropertyName(String attribute)
      {
         String ret = null;
         if (isNamed(attribute))
         {
            ret = attribute.substring(attribute.lastIndexOf(":") + 1);
         }
         return ret;
      }
   }

   /**
    * Process instance attribute supporting filter operations.
    * <p />
    * Not for direct use.
    *
    */
   private static final class Attribute extends FilterableAttributeImpl
   {
      private static final long serialVersionUID = 1L;

      private Attribute(String name)
      {
         super(DocumentQuery.class, name);
      }
   }

}
