/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.RootElementBean;



/**
 * @author ubirkemeyer
 * @version $Revision: 6165 $
 */
public class Model extends RootElementBean
{
   private static final long serialVersionUID = -692553268777037265L;

   Link processDefinitions = new Link(this, "PD");
   Link applications = new Link(this, "APP");
   Link data = new Link(this, "DATA");
   Link participants = new Link(this, "PT");
   private Link diagrams = new Link(this, "DIAG");

   public Model() {}

   public String getId()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public String getName()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public ImageIcon getIcon()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public Application createApplication(String id)
   {
      Application result = new Application(id);
      applications.add(result);
      result.register(0);
      return result;
   }

   public ProcessDefinition createProcessDefinition(String id)
   {
      ProcessDefinition result = new ProcessDefinition(id);
      processDefinitions.add(result);
      result.register(0);
      return result;
   }

   public ProcessDefinition findProcessDefinition(String id)
   {
      return (ProcessDefinition) processDefinitions.findById(id);
   }

   public Role createRole(String id)
   {
      Role result = new Role(id);
      participants.add(result);
      result.register(0);
      return result;
   }

   public Organization createOrganization(String id)
   {
      Organization result = new Organization(id);
      participants.add(result);
      result.register(0);
      return result;
   }

   public Participant findParticipant(String id)
   {
      return (Participant) participants.findById(id);
   }

   public long getParticipantCount()
   {
      return participants.size();
   }

   public Data createData(String id)
   {
      Data result = new Data(id);
      data.add(result);
      result.register(0);
      return result;
   }

   public Data findData(String id)
   {
      return (Data) data.findById(id);
   }

   public Diagram createDiagram(String id)
   {
      Diagram result =  new Diagram(id);
      diagrams.add(result);
      result.register(0);
      return result;
   }

   public Diagram findDiagram(String id)
   {
      return (Diagram) diagrams.findById(id);
   }

   public Application findApplication(String id)
   {
      return (Application) applications.findById(id);
   }

   public void addToApplications(Application application)
   {
      applications.add(application);
   }
}
