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
package org.eclipse.stardust.engine.core.model.beans;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.MethodDescriptor;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.IApplicationType;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaApplicationInstance;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationContextValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;


/**
 * @author mgille
 */
public class ApplicationBean extends IdentifiableElementBean
      implements IApplication, Serializable
{
   static final String NAME_SPACE = "Application::";

   static final String IS_INTERACTIVE_ATT = "Interactive";
   private boolean isInteractive;

   private IApplicationType applicationType = null;

   private final Link persistentAccessPoints = new Link(this, "Access Points");

   private final AccessPointJanitor accessPoints = new AccessPointJanitor(persistentAccessPoints);

   private Link contexts = new Link(this, "Contexts");

   ApplicationBean()
   {
   }

   public ApplicationBean(String id, String name, String description)
   {
      super(id, name);

      setDescription(description);

      this.isInteractive = true;
   }

   public String toString()
   {
      return "Application: " + getName();
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the application.
    */
   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies);
      checkId(inconsistencies);

      // check for unique Id
      IApplication app = ((IModel) getModel()).findApplication(getId());
      if (app != null && app != this)
      {
         inconsistencies.add(new Inconsistency("Duplicate ID for application '" +
               getName() + "'.", this, Inconsistency.ERROR));
      }

      if (getType() != null)
      {
         ApplicationValidator validator = (ApplicationValidator) ValidatorUtils.getValidator(getType(), this, inconsistencies);
         if (null != validator)
         {
            if (validator instanceof ApplicationValidatorEx)
            {
               inconsistencies.addAll(((ApplicationValidatorEx) validator).validate(this));
            }
            else
            {
               Collection c = validator.validate(getAllAttributes(), getType().getAllAttributes(), getAllAccessPoints());
               for (Iterator i = c.iterator(); i.hasNext();)
               {
                  Inconsistency x = (Inconsistency) i.next();
                  inconsistencies.add(new Inconsistency(x.getMessage(), this, x.getSeverity()));
               }
            }
         }

      }
      for (Iterator iterator = contexts.iterator(); iterator.hasNext();)
      {
         IApplicationContext context = (IApplicationContext) iterator.next();
         ApplicationContextValidator validator = (ApplicationContextValidator) ValidatorUtils.getValidator(context.getType(), this, inconsistencies, !isInteractive());
         if (null != validator)
         {
            Collection problems = validator.validate(context.getAllAttributes(), context.getAllAccessPoints());
            for (Iterator i = problems.iterator(); i.hasNext();)
            {
               Inconsistency x = (Inconsistency) i.next();
               inconsistencies.add(new Inconsistency(x.getMessage(), this, x.getSeverity()));
            }
         }
      }
   }

   public PluggableType getType()
   {
      return applicationType;
   }

   public void setApplicationType(IApplicationType type)
   {
      this.applicationType = type;
   }

   public void removeFromContexts(IApplicationContext ctx)
   {
      contexts.remove(ctx);
   }

   public void removeContext(String id)
   {
      contexts.remove(id);
   }

   public IApplicationContext createContext(String id, int elementOID)
   {
      if (findContext(id) != null)
      {
         throw new PublicException("Context with id '" + id + "' already exists.");
      }
      IApplicationContext context = new ApplicationContextBean(id, false);
      contexts.add(context);
      context.register(elementOID);
      return context;
   }

   public void addToContexts(IApplicationContext context)
   {
      contexts.add(context);
   }

   public Iterator getAllContexts()
   {
      return contexts.iterator();
   }

   private AccessPointJanitor getAccessPointLink()
   {
      return accessPoints;
   }

   public AccessPoint findAccessPoint(String id)
   {
      return findAccessPoint(id, null);
   }

   public AccessPoint findAccessPoint(String id, Direction direction)
   {
      //strip down the full(generic) method name to a simple one, so later checks will find
      //the access point
      Object applicationInstanceClassName 
         = applicationType.getAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT); 
      if(PlainJavaApplicationInstance.class.getName().equals(applicationInstanceClassName))
      {
         MethodDescriptor md = Reflect.describeEncodedMethod(id);
         if(md != null)
         {
            id = md.toString();
         }
      }
       
      return getAccessPointLink().findAccessPoint(id, direction);
   }

   public AccessPoint createAccessPoint(String id, String name, Direction direction,
         IDataType type, int elementOID)
   {
      IAccessPoint result = new AccessPointBean(id, name, direction);
      addToPersistentAccessPoints(result);
      result.register(elementOID);
      result.setDataType(type);
      return result;
   }

   public Iterator getAllAccessPoints()
   {
      return getAccessPointLink().iterator();
   }

   public void addToPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.add(accessPoint);
      accessPoints.setDirty();
   }

   public boolean isSynchronous()
   {
      String async = (String) getAttribute(PredefinedConstants.ASYNCHRONOUS_ATT);
      if (async == null)
      {
         return ((IApplicationType) getType()).isSynchronous();
      }
      else
      {
         return !Boolean.valueOf(async).booleanValue();
      }
   }

   public void removeFromAccessPoints(AccessPoint accessPoint)
   {
      getAccessPointLink().remove(accessPoint);
   }

   public void removeFromPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.remove(accessPoint);
      accessPoints.setDirty();
   }

   public Iterator getAllInAccessPoints()
   {
      return getAccessPointLink().getAllInAccessPoints();
   }

   public Iterator getAllOutAccessPoints()
   {
      return getAccessPointLink().getAllOutAccessPoints();
   }

   public String getProviderClass()
   {
      return getType().getStringAttribute(
            PredefinedConstants.ACCESSPOINT_PROVIDER_ATT);
   }

   public void markModified()
   {
      super.markModified();
      getAccessPointLink().setDirty();
   }

   public void addIntrinsicAccessPoint(AccessPoint ap)
   {
      getAccessPointLink().addIntrinsicAccessPoint(ap);
   }

   public Iterator getAllPersistentAccessPoints()
   {
      return persistentAccessPoints.iterator();
   }

   public boolean isInteractive()
   {
      return isInteractive;
   }

   public void setInteractive(boolean interactive)
   {
      this.isInteractive = interactive;
      if (interactive)
      {
         applicationType = null;
      }
   }

   public IApplicationContext findContext(String id)
   {
      return (IApplicationContext) contexts.findById(id);
   }

   public void removeAllContexts()
   {
      contexts.clear();
   }
}
