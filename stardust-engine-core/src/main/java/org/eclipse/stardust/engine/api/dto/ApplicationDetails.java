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
package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IApplication;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ApplicationDetails extends ModelElementDetails implements Application
{
   private static final long serialVersionUID = 2L;
   
   private Map typeAttributes;
   private AccessPointDetailsEvaluator apEvaluator;
   
   public ApplicationDetails(IApplication application)
   {
      super(application);
      typeAttributes = application.getType().getAllAttributes();
      
      apEvaluator = new AccessPointDetailsEvaluator(application, application
            .isInteractive(), getAllAttributes(), getAllTypeAttributes());
   }

   public List getAllAccessPoints()
   {
      return Collections.unmodifiableList(apEvaluator.getAccessPoints());
   }

   public org.eclipse.stardust.engine.api.model.AccessPoint getAccessPoint(String id)
   {
      for (Iterator i = apEvaluator.getAccessPoints().iterator(); i.hasNext();)
      {
         org.eclipse.stardust.engine.api.model.AccessPoint point = (org.eclipse.stardust.engine.api.model.AccessPoint) i.next();
         if (point.getId().equals(id))
         {
            return point;
         }
      }
      return null;
   }

   public Map getAllTypeAttributes()
   {
      return typeAttributes;
   }

   public Object getTypeAttribute(String name)
   {
      return typeAttributes.get(name);
   }
}
