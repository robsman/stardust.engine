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
package org.eclipse.stardust.engine.extensions.ejb.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.ejb.SessionBeanConstants;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.app.SessionBean20ApplicationInstance;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.data.Entity20PKEvaluator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EntityBeanEvaluator implements AccessPathEvaluator, Stateless
{
   private static final Logger trace = LogManager.getLogger(EntityBeanEvaluator.class);

   private static final String ENTITY_3_0_PK_EVALUATOR =
      "org.eclipse.stardust.engine.extensions.ejb.ejb3.data.Entity30PKEvaluator";

   public boolean isStateless()
   {
      return true;
   }

   public Object evaluate(Map attribs, Object accessPoint, String outPath)
   {
      Object bean = findEntityByPK(attribs, getEntityBeanPK(attribs, accessPoint));

      Object value;
      try
      {
         value = JavaDataTypeUtils.evaluate(outPath, bean);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException("Failed reding entity bean attribute.",
               e.getTargetException());
      }

      return value;      
   }

   public Object evaluate(Map attribs, Object accessPoint, String inPath, Object value)
   {
      // @todo (france, ub): this snippet was moved from workflowserviceimpl
      // it has to be double checked
      // snip -->
      if (value instanceof Handle)
      {
         try
         {
            value = ((Handle) value).getEJBObject();
         }
         catch (java.rmi.RemoteException e)
         {
            throw new PublicException("Failed translating Entity Bean handle to bean "
                  + "reference.", e.detail);
         }
      }
      // <--  snip

      if (StringUtils.isEmpty(inPath))
      {
         // assuming to replace the whole entity bean

         return getEntityBeanPK(attribs, value);
      }
      else
      {
         Object bean = findEntityByPK(attribs, getEntityBeanPK(attribs, accessPoint));

         try
         {
            JavaDataTypeUtils.evaluate(inPath, bean, value);
         }
         catch (InvocationTargetException e)
         {
            throw new PublicException("Failed setting entity bean attribute.",
                  e.getTargetException());
         }

         return UNMODIFIED_HANDLE;
      }
   }

   public Object createInitialValue(Map data)
   {
      return null;
   }

   public Object createDefaultValue(Map attributes)
   {
      return null;
   }

   /**
    * Obtains the PK from an entity bean access point.
    *
    * @param attributes The access point definition's attributes.
    * @param value The base value to be used for PK inspection.
    * @return The obtained PK, or <code>null</code> if no strategy was successful.
    */
   private Object getEntityBeanPK(Map attributes, Object value)
   {
      IEntityPKEvaluator evaluator = getPKEvaluator(attributes);
      return evaluator.getEntityBeanPK(attributes, value);
   }

   private Object findEntityByPK(Map attributes, Object pk)
   {
      IEntityPKEvaluator evaluator = getPKEvaluator(attributes);
      return evaluator.findEntityByPK(attributes, pk);
   }

   private IEntityPKEvaluator getPKEvaluator(Map attributes)
   {
      IEntityPKEvaluator evaluator = null;
      String style = (String) attributes.get(EntityBeanConstants.VERSION_ATT);
      if (EntityBeanConstants.VERSION_3_X.equals(style))
      {
         try
         {
            Class clz = Reflect.getClassFromClassName(ENTITY_3_0_PK_EVALUATOR);
            evaluator = (IEntityPKEvaluator) clz.newInstance();
         }
         catch (Throwable t)
         {
            trace.debug("Failed to create Entity 3.0 PK evaluator.", t);
         }
      }
      if (evaluator == null)
      {
         evaluator = new Entity20PKEvaluator();
      }
      trace.debug("Created PK evaluator: " + evaluator);
      return evaluator;
   }
}
