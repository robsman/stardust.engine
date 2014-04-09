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

import static org.eclipse.stardust.vfs.VfsUtils.NS_PREFIX_VFS;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_ATTRIBUTES;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_NAME;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_OWNER;

import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.query.DocumentQuery.MetadataFilterBuilder;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.vfs.MetaDataLocation;


public class DocumentXPathQueryOrderEvaluator implements OrderEvaluationVisitor
{
   private String localMetaDataAttribute;

   public DocumentXPathQueryOrderEvaluator(MetaDataLocation metaDataLocation)
   {
      if (MetaDataLocation.LOCAL.equals(metaDataLocation))
      {
         localMetaDataAttribute = addVfsPrefix("metaData/");
      }
      else
      {
         localMetaDataAttribute = "";
      }
   }

   public String buildOrderTerm(Query query)
   {
      String orderTerm = "";
      OrderCriteria criteria = query.getOrderCriteria();
      if (criteria != null)
      {
         List<OrderCriterion> list = criteria.getCriteria();
         if (list != null)
         {
            // only first element
            if (list.iterator().hasNext())
            {
               orderTerm = " order by "
                     + (String) list.iterator().next().accept(this, null);
            }
         }
      }
      return orderTerm;
   }

   public String visit(OrderCriteria order, Object context)
   {
      String orderTerm = "";
      List<OrderCriterion> list = order.getCriteria();
      if (list != null)
      {
         // only first element
         if (list.iterator().hasNext())
         {
            orderTerm = (String) list.iterator().next().accept(this, null);
         }
      }
      return orderTerm;
   }

   public String visit(AttributeOrder order, Object context)
   {
      String sortDir = order.isAscending() ? " ascending" : " descending";
      String attribute = order.getAttributeName();

      if (DocumentQuery.DATE_CREATED.getAttributeName().equals(attribute))
      {
         return "@jcr:created" + sortDir;
      }
      else if (DocumentQuery.DATE_LAST_MODIFIED.getAttributeName().equals(attribute))
      {
         return "jcr:content/@jcr:lastModified" + sortDir;
      }
      else if (DocumentQuery.CONTENT_TYPE.getAttributeName().equals(attribute))
      {
         return "jcr:content/@jcr:mimeType" + sortDir;
      }
      else if (DocumentQuery.ID.getAttributeName().equals(attribute))
      {
         return "@jcr:uuid" + sortDir;
      }
      else if (DocumentQuery.NAME.getAttributeName().equals(attribute))
      {
         return localMetaDataAttribute + "@" + addVfsPrefix(VFS_NAME) + sortDir;

      }
      else if (DocumentQuery.OWNER.getAttributeName().equals(attribute))
      {
         return localMetaDataAttribute + addVfsPrefix(VFS_ATTRIBUTES) + "/@"
               + addVfsPrefix(VFS_OWNER) + sortDir;
      }
      else if (MetadataFilterBuilder.isNamed(attribute))
      {
         return localMetaDataAttribute + addVfsPrefix(VFS_ATTRIBUTES) + "/@"
               + addVfsPrefix(MetadataFilterBuilder.getPropertyName(attribute)) + sortDir;
      }
      else if (DocumentQuery.DOCUMENT_TYPE_ID.getAttributeName().equals(attribute))
      {
         return localMetaDataAttribute + DocumentXPathQueryAttributes.ATTRIBUTES_TYPE_ID + sortDir;

      }
      else if (DocumentQuery.DOCUMENT_TYPE_SCHEMA_LOCATION.getAttributeName().equals(attribute))
      {
         return localMetaDataAttribute + DocumentXPathQueryAttributes.ATTRIBUTES_TYPE_SCHEMA_LOCATION + sortDir;

      }
		throw new PublicException(
				BpmRuntimeError.QUERY_ATTRIBUTE_NOT_SUPPORTED_FOR_ORDER_TERM
						.raise());
   }

   public String visit(DataOrder order, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public String visit(CustomOrderCriterion order, Object context)
   {
      String sortDir = order.isAscending() ? " ascending" : " descending";
      if (DocumentQuery.RELEVANCE.getFieldName().equals(order.getFieldName()))
      {
         return "@jcr:score" + sortDir;
      }

      return "";
   }

   private String addVfsPrefix(String string)
   {
      return NS_PREFIX_VFS + ":" + string;
   }

}
