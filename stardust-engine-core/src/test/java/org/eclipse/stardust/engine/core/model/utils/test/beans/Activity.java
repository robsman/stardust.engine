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
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */
public class Activity extends IdentifiableElementBean
{
   private static final long serialVersionUID = 5824346798927264109L;

   String id;
   List inTransitions = null;
   List outTransitions = null;
   Application application = null;
   Connections dataMappings  = new Connections(this, "Data Mappings", null, "dataMappings");

   Activity() {}

   public Activity(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public ImageIcon getIcon()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public Iterator getAllInTransitions()
   {
      if (inTransitions == null)
      {
         return Collections.emptyList().iterator();
      }
      return inTransitions.iterator();
   }

   public Iterator getAllOutTransitions()
   {
      if (outTransitions == null)
      {
         return Collections.emptyList().iterator();
      }
      return outTransitions.iterator();
   }

   public void setApplication(Application application)
   {
      this.application = application;
   }

   public Transition findInTransition(String id)
   {
      return (Transition) findById(inTransitions, id);
   }

   public Transition findOutTransition(String id)
   {
      return (Transition) findById(outTransitions, id);
   }

   private Transition findById(List transitions, String id)
   {
      return (Transition) ModelUtils.findById(transitions, id);
   }

   public long getOutTransitionsCount()
   {
      return outTransitions == null ? 0 : outTransitions.size();
   }

   public long getInTransitionsCount()
   {
      return inTransitions == null ? 0 : inTransitions.size();
   }

   public DataMapping createDataMapping(String id, Data data)
   {
      DataMapping result = new DataMapping(id, this, data);
      dataMappings.add(result);
      result.register(0);
      return result;
   }

   public long getDataMappingsCount()
   {
      return dataMappings.size();
   }

   public Application getApplication()
   {
      return application;
   }
}
