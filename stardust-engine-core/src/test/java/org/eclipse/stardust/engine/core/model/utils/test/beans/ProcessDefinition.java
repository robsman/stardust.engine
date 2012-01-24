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

import java.util.Iterator;

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */
public class ProcessDefinition extends IdentifiableElementBean
{
   private static final long serialVersionUID = -5245017539798524752L;

   Link activities = new Link(this, "ACT");
   Connections transitions = new Connections(this, "TRANS", "outTransitions", "inTransitions");

   private String id;

   ProcessDefinition() {}
   public ProcessDefinition(String id)
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

   public Iterator getAllTransitions()
   {
      return transitions.iterator();
   }

   public Transition createTransition(String id, Activity a1, Activity a2)
   {
      Transition result = new Transition(id, a1, a2);
      transitions.add(result);
      result.register(0);

      return result;
   }

   public Activity createActivity(String id)
   {
      Activity result = new Activity(id);
      activities.add(result);
      result.register(0);
      return result;
   }

   public Activity findActivity(String id)
   {
      return (Activity) activities.findById(id);
   }

   public Transition findTransition(String id)
   {
      return (Transition) transitions.findById(id);
   }

   public long getTransitionsCount()
   {
      return transitions.size();
   }

   public void addToTransitions(Transition transition)
   {
      transitions.add(transition);
   }

   public void addToActivities(Activity activity)
   {
      activities.add(activity);
   }
}
