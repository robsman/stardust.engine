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

import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.runtime.beans.IWorkItem;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemAdapter;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;


/**
 * Evaluates ActivityInstanceQueries based on entries in workitem table.
 * Should only be used for retrieving worklists.
 *
 * @author stephan.born
 * @version $Revision: $
 */
public class WorkItemQueryEvaluator extends RuntimeInstanceQueryEvaluator
{
   public WorkItemQueryEvaluator(ActivityInstanceQuery query, EvaluationContext context)
   {
      super(query, WorkItemBean.class, context);
   }
   
   @Override
   public ResultIterator executeFetch()
   {
      return new WorkItemRSI(super.executeFetch());
   }

   /**
    * Wraps ResultsetIterator which would return elements of type WorkItem. These elements
    * are wrapped by an adapter which implements IActivityInstance.
    * 
    * @author stephan.born
    * @version $Revision: $
    */
   private static final class WorkItemRSI implements ResultIterator
   {
      private final ResultIterator delegate;

      public WorkItemRSI(ResultIterator delegate)
      {
         super();
         this.delegate = delegate;
      }

      public void close()
      {
         delegate.close();
      }

      public int getMaxSize()
      {
         return delegate.getMaxSize();
      }

      public int getStartIndex()
      {
         return delegate.getStartIndex();
      }

      public long getTotalCount() throws UnsupportedOperationException
      {
         return delegate.getTotalCount();
      }

      public boolean hasMore()
      {
         return delegate.hasMore();
      }

      public boolean hasNext()
      {
         return delegate.hasNext();
      }

      public boolean hasTotalCount()
      {
         return delegate.hasTotalCount();
      }

      public Object next()
      {
         return new WorkItemAdapter((IWorkItem) delegate.next());
      }

      public void remove()
      {
         delegate.remove();
      }
   }
}
