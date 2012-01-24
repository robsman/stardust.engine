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

/**
 * Visitor allowing to transfer copies of filter criteria between queries.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class WorklistFilterCopier extends FilterCopier
{
   /**
    * Creates copies of filter terms.
    * 
    * @see #FilterCopier(FilterVerifier)
    * @see #visit(FilterTerm, Object)
    */
   public WorklistFilterCopier()
   {
      this(null);
   }

   /**
    * Creates copies of filter terms, but using the <code>targetVerifier</code> for the
    * copy made.
    * 
    * @param targetVerifier
    *            The filter targetVerifier to be used by the copy made.
    * 
    * @see #FilterCopier()
    * @see #visit(FilterTerm, Object)
    */
   public WorklistFilterCopier(FilterVerifier targetVerifier)
   {
      super(targetVerifier);
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      UnaryOperatorFilter result = filter;
      
      if (WorklistQuery.class.equals(filter.getScope()))
      {
         result = new UnaryOperatorFilterImpl(ActivityInstanceQuery.class, filter
               .getOperator(), filter.getAttribute());
         
         if (filter instanceof IAttributeJoinDescriptor)
         {
            IAttributeJoinDescriptor joinDesc = (IAttributeJoinDescriptor) filter;
            result = ReferenceAttribute.createUnaryOperatorFilter(result, joinDesc);
         }
      }
      
      return result;
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      BinaryOperatorFilter result = filter;
      
      if (WorklistQuery.class.equals(filter.getScope()))
      {
         result = new BinaryOperatorFilterImpl(ActivityInstanceQuery.class,
               filter.getOperator(), filter.getAttribute(), filter.getValue());
         
         if (filter instanceof IAttributeJoinDescriptor)
         {
            IAttributeJoinDescriptor joinDesc = (IAttributeJoinDescriptor) filter;
            result = ReferenceAttribute.createBinaryOperatorFilter(result, joinDesc);
         }
      }
      
      return result;
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      TernaryOperatorFilter result = filter;
      
      if (WorklistQuery.class.equals(filter.getScope()))
      {
         result = new TernaryOperatorFilterImpl(ActivityInstanceQuery.class,
               filter.getOperator(), filter.getAttribute(), filter.getValue());
         
         if (filter instanceof IAttributeJoinDescriptor)
         {
            IAttributeJoinDescriptor joinDesc = (IAttributeJoinDescriptor) filter;
            result = ReferenceAttribute.createTernaryOperatorFilter(result, joinDesc);
         }
      }
      
      return result;
   }

}
