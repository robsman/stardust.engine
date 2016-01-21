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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.eclipse.stardust.vfs.MetaDataLocation;


public class DocumentXPathQueryFilterEvaluator implements FilterEvaluationVisitor
{

   private DocumentXPathQueryTermFactory xPathQueryTermFactory;

   public DocumentXPathQueryFilterEvaluator(MetaDataLocation metaDataLocation)
   {
      if (metaDataLocation == null)
      {
         this.xPathQueryTermFactory = new DocumentXPathQueryTermFactory(MetaDataLocation.LOCAL);
      }
      else
      {
         this.xPathQueryTermFactory = new DocumentXPathQueryTermFactory(metaDataLocation);
      }
   }

   public String buildFilterTerm(Query query)
   {
      if (query instanceof DocumentQuery)
      {
         String xPathQuery = visit(query.getFilter(), null);

         if (!isEmpty(xPathQuery))
         {
            StringBuffer buf = new StringBuffer();
            buf.append("[");
            buf.append(xPathQuery);
            buf.append("]");
            xPathQuery = buf.toString();
         }
         return xPathQuery;
      }
      else
      {
         throw new UnsupportedOperationException("Only supports " + DocumentQuery.class);
      }
   }

   public String visit(FilterTerm filter, Object context)
   {
      String operator = FilterTerm.AND.equals(filter.getKind()) ? "and" : "or";

      StringBuffer andTerm = new StringBuffer();
      for (Object part : filter.getParts())
      {
         FilterCriterion criterion = (FilterCriterion) part;
         String andPart = (String) criterion.accept(this, context);

         if (andTerm.length() == 0)
         {
            andTerm.append("(");
            andTerm.append(andPart);
         }
         else
         {
            andTerm.append(" " + operator + " " + andPart);
         }
      }
      if (andTerm.length() != 0)
      {
         andTerm.append(")");
      }
      return andTerm.toString();
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      return xPathQueryTermFactory.buildFilterTerm(filter.getAttribute(),
            filter.getOperator());
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      return xPathQueryTermFactory.buildFilterTerm(filter.getAttribute(),
            filter.getOperator(), filter.getValue());
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      return xPathQueryTermFactory.buildFilterTerm(filter.getAttribute(),
            filter.getOperator(), filter.getValue());
   }

   public Object visit(RootProcessInstanceFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }
   
   public Object visit(ProcessDefinitionFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      return "";
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(DocumentFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

}
