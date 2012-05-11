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
package org.eclipse.stardust.engine.core.pojo.app;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.constants.PlainJavaConstants;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.reflect.ResolvedCtor;
import org.eclipse.stardust.common.reflect.ResolvedMethod;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ApplicationInvocationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.StatelessSynchronousApplicationInstance;


/**
 * ApplicationInstance implementation for the PlainJavaApplicationInstance
 * 
 * @author jmahmood, ubirkemeyer
 * @version $Revision$
 */
public class PlainJavaApplicationInstance
      implements StatelessSynchronousApplicationInstance
{
   public static final Logger trace = LogManager
         .getLogger(PlainJavaApplicationInstance.class);
   
   private static final String CACHED_CLASS = PlainJavaApplicationInstance.class.getName()
         + ".CachedClass";

   private static final String CACHED_CTOR = PlainJavaApplicationInstance.class.getName()
         + ".CachedCtor";
   
   private static final String CACHED_METHOD = PlainJavaApplicationInstance.class.getName()
         + ".CachedMethod";

   /**
    * Initializes the plain java application instance. Retrieves the application, the type
    * of application and data mappings from the activity instance.
    * 
    * @param activityInstance
    */
   public ApplicationInvocationContext bootstrap(ActivityInstance activityInstance)
   {
      InvocationContext c = new InvocationContext(activityInstance);
      
      IActivity activity = null;
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      if (null != modelManager)
      {
         activity = modelManager.findActivity(activityInstance.getModelOID(),
               activityInstance.getActivity().getRuntimeElementOID());
      }

      if (null != activity)
      {
         // TODO
         c.application = activity.getApplication();
         
         c.theType = (Class) activity.getApplication().getRuntimeAttribute(CACHED_CLASS);
         c.ctor = (ResolvedCtor) activity.getApplication().getRuntimeAttribute(CACHED_CTOR);
         c.method = (ResolvedMethod) activity.getApplication().getRuntimeAttribute(CACHED_METHOD);
      }

      if (null == c.theType)
      {
         c.theType = Reflect.getClassFromClassName((String) c.application
               .getAttribute(PredefinedConstants.CLASS_NAME_ATT));
         
         if (null != activity)
         {
            activity.getApplication().setRuntimeAttribute(CACHED_CLASS, c.theType);
         }
      }

      if (null == c.ctor)
      {
         c.ctor = new ResolvedCtor(
               Reflect.decodeConstructor(c.theType,
                     (String) c.application.getAttribute(PredefinedConstants.CONSTRUCTOR_NAME_ATT)));

         if (null != activity)
         {
            activity.getApplication().setRuntimeAttribute(CACHED_CTOR, c.ctor);
         }
      }
      
      if (null == c.method)
      {
         c.method = new ResolvedMethod(Reflect.decodeMethod(c.theType,
               (String) c.application.getAttribute(PredefinedConstants.METHOD_NAME_ATT)));

         if (null != activity)
         {
            activity.getApplication().setRuntimeAttribute(CACHED_METHOD, c.method);
         }
      }
      
      final List allOutDataMappings = activityInstance.getActivity()
            .getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)
            .getAllOutDataMappings();
      for (int i = 0; i < allOutDataMappings.size(); ++i)
      {
         DataMapping mapping = (DataMapping) allOutDataMappings.get(i);

         c.outDataMappingOrder.add(mapping.getApplicationAccessPoint().getId());
      }
      
      // create instance right now, if the default ctor is used and thus no ctor argument
      // mappings might exist
      
      if ((null != c.ctor) && (0 == c.ctor.argTypes.length))
      {
         createObject(c);
      }
      
      return c;
   }

   /**
    * Adds a new pair with an access point name and value. If a pair with the specified
    * name already exists, the old value will be replaced with the new one.
    * 
    * @param name
    *           The name of the access point.
    * @param value
    *           The value of the access point.
    */
   public void setInAccessPointValue(ApplicationInvocationContext c, String name,
         Object value)
   {
      // erase java generics from the name
      StringBuffer sb = new StringBuffer(name);
      int right = 0;
      int counter = 0;
      int left = 0;
      while (right < sb.length())
      {
         char charAt = sb.charAt(right);
         if (charAt == '<')
         {
            if (counter == 0)
            {
               left = right;
            }
            counter++;
         }
         else if (charAt == '>')
         {
            counter--;
            if (counter == 0)
            {
               sb.delete(left, right + 1);
               right = left;
               continue;
            }
         }
         right++;
      }
      name = sb.toString();
      
      Pair param = findAccessPointValue(((InvocationContext) c), name);
      if (null != param)
      {
         ((InvocationContext) c).accessPointValues.remove(param);
      }
      ((InvocationContext) c).accessPointValues.add(new Pair(name, value));
   }

   /**
    * Returns the value of the access point with the given name.
    * 
    * @param name
    *           The name of the access point.
    * @return The value of the access point.
    */
   public Object getOutAccessPointValue(ApplicationInvocationContext c, String name)
   {
      // hint: no returnValue access-point here allowed because the getOutAccessPointValue
      // method is only for processing in data mappings.
      try
      {
         return doGetOutAccessPointValue(((InvocationContext) c), name, false);
      }
      catch (InvocationTargetException e)
      {
         throw new InternalException(e.getMessage(), e.getTargetException());
      }
   }

   public void cleanup(ApplicationInvocationContext c)
   {}

   /**
    * Callback used by the CARNOT engine when the corresponding activity instance is run.
    * Invokes the method of the application.
    * 
    * @param outDataTypes
    *           A set of AccessPointBean names to be expected as return values. This is
    *           filled by the CARNOT engine and is an optimization hint to prevent the
    *           application instance to evaluate all possible OUT AccessPoints.
    * @return A map with the provided AccessPointBean names as keys and the values at this
    *         access points as values.
    * @throws InvocationTargetException
    *            Any exception thrown during execution of the application is delivered via
    *            this exception.
    */
   public Map invoke(ApplicationInvocationContext c, Set outDataTypes)
         throws InvocationTargetException
   {
      final InvocationContext pojoContext = (InvocationContext) c;

      // reset from possible previous execution
      pojoContext.lastReturnValue = null;

      // only create instance if it was not already created during bootstrap
      if (null == pojoContext.theObject)
      {
         createObject(pojoContext);
      }

      doSetInAccessPointValues(pojoContext);

      Object[] parameters = new Object[pojoContext.method.argTypes.length];

      for (int i = 0; i < pojoContext.method.argTypes.length; i++)
      {
         Pair param = findAccessPointValue(pojoContext, pojoContext.method.argNames[i]);
         try
         {
            parameters[i] = Reflect.castValue((null != param) ? param.getSecond() : null,
                  pojoContext.method.argTypes[i]);
         }
         catch (InvalidValueException e)
         {
            throw new InvocationTargetException(e, "Failed to apply IN parameter '"
                  + pojoContext.method.argNames[i] + "' for java method '"
                  + Reflect.encodeMethod(pojoContext.method.self) + "'.");
         }
      }

      try
      {
         pojoContext.lastReturnValue = pojoContext.method.self.invoke(
               pojoContext.theObject, parameters);
      }
      catch (InvocationTargetException e)
      {
         trace.warn("Failed to invoke Java Bean method '"
               + Reflect.encodeMethod(pojoContext.method.self) + "'.",
               e.getTargetException());
         throw e;
      }
      catch (Exception e)
      {
         trace.warn("Failed to invoke session-bean method '"
               + Reflect.encodeMethod(pojoContext.method.self) + "'.", e);
         throw new InvocationTargetException(e, "Failed to invoke session bean method "
               + Reflect.encodeMethod(pojoContext.method.self) + ".");
      }

      return doGetOutAccessPointValues(pojoContext, outDataTypes);
   }

   /**
    * Instantiates the actual session-bean.
    */
   private void createObject(InvocationContext pojoContext)
   {
      Object[] createParameters = new Object[pojoContext.ctor.argTypes.length];

      for (int i = 0; i < pojoContext.ctor.argTypes.length; i++)
      {
         String humanName = Reflect.getHumanReadableClassName(pojoContext.ctor.argTypes[i]);
         String paramName = humanName.toLowerCase().charAt(0)
               + PlainJavaConstants.CONSTRUCTOR_PARAMETER_PREFIX + (i + 1);
         Pair param = findAccessPointValue(pojoContext, paramName);
         createParameters[i] = (null != param) ? param.getSecond() : null;
      }
      try
      {
         pojoContext.theObject = pojoContext.ctor.self.newInstance(createParameters);
      }
      catch (Exception x)
      {
         throw new PublicException("Cannot create object.", x);
      }
   }

   /**
    * Sets in access point values invoking the setter methods.
    * 
    * @throws InvocationTargetException
    *            Any exception thrown during execution of the application is delivered via
    *            this exception.
    */
   private void doSetInAccessPointValues(InvocationContext pojoContext) throws InvocationTargetException
   {
      for (int i = 0; i < pojoContext.accessPointValues.size(); ++i)
      {
         Pair entry = (Pair) pojoContext.accessPointValues.get(i);
         
         String name = (String) entry.getFirst();
         Object value = entry.getSecond();

         final AccessPoint inAccessPoint = pojoContext.application.findAccessPoint(name);
         if (null == inAccessPoint)
         {
            throw new PublicException("Access point '" + name + "' does not exist, "
                  + pojoContext.theType);
         }

         if (JavaAccessPointType.METHOD.equals(inAccessPoint.getAttribute(PredefinedConstants.FLAVOR_ATT)))
         {
            // must be a Java setter

            try
            {
               JavaDataTypeUtils.evaluateSetter(inAccessPoint, pojoContext.theObject,
                     value);
            }
            catch (InvocationTargetException e)
            {
               trace.warn("Failed to invoke setter '" + name + "'.", e
                     .getTargetException());
               throw e;
            }
            catch (Exception e)
            {
               trace.warn("", e);
               throw new InvocationTargetException(e, "Failed setting session-bean in "
                     + "access-point '" + name + "'.");
            }
         }
      }
   }

   /**
    * Returns all out access points of the activity application context with the
    * corresponding values in the out-data-mapping order.
    * 
    * @param outDataTypes
    *           The out access points of the activity application context.
    * @return A map with the access point name as key and the access point value as value.
    * @throws InvocationTargetException
    *            Any exception thrown during execution of the application is delivered via
    *            this exception.
    */
   private Map doGetOutAccessPointValues(InvocationContext c, Set outDataTypes)
         throws InvocationTargetException
   {
      Map result = null;

      for (int i = 0; i < c.outDataMappingOrder.size(); ++i)
      {
         String name = (String) c.outDataMappingOrder.get(i);
         if (outDataTypes.contains(name))
         {
            if (null == result)
            {
               result = CollectionUtils.newMap();
            }

            result.put(name, doGetOutAccessPointValue(c, name, true));
         }
      }
      return result;
   }

   /**
    * If a return value is allowed, the stored return value of the last method invocation
    * is returned. Otherwise invokes the getter method to retrieve the return value.
    * 
    * @param name
    *           The name of the access point.
    * @param allowReturnValue
    * @return The value of the access point with the given name.
    * @throws InvocationTargetException
    *            Any exception thrown during execution of the application is delivered via
    *            this exception.
    */
   private Object doGetOutAccessPointValue(InvocationContext c, String name,
         boolean allowReturnValue) throws InvocationTargetException
   {
      AccessPoint outAp = c.application.findAccessPoint(name);

      Object characteristics = outAp.getAttribute(PredefinedConstants.FLAVOR_ATT);

      if (allowReturnValue && JavaAccessPointType.RETURN_VALUE.equals(characteristics))
      {
         return c.lastReturnValue;
      }
      else if (JavaAccessPointType.METHOD.equals(characteristics))
      {
         // MUST be getter
         
         try
         {
            return JavaDataTypeUtils.evaluateGetter(outAp, c.theObject);
         }
         catch (InvocationTargetException e)
         {
            trace.warn("Failed to invoke getter '" + name + "'.", e.getTargetException());
            throw e;
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new InvocationTargetException(e, "Failed retrieving session-bean out "
                  + "access-point '" + name + "'.");
         }
      }
      else
      {
         throw new InternalException("Unknown characteristics '" + characteristics
               + "' for access point '" + name + "'.");
      }
   }

   /**
    * Returns a pair of the given access point name and the corresponding value.
    * 
    * @param name
    *           The access point name.
    * @return The pair with name and value. Null if no access point pair could be found.
    */
   private Pair findAccessPointValue(InvocationContext c, String name)
   {
      Pair result = null;
      for (int i = 0; i < c.accessPointValues.size(); ++i)
      {
         Pair entry = (Pair) c.accessPointValues.get(i);
         if (name.equals(entry.getFirst()))
         {
            result = entry;
            break;
         }
      }
      return result;
   }
   
   private static class InvocationContext extends ApplicationInvocationContext
   {
      public InvocationContext(ActivityInstance ai)
      {
         super(ai);
      }

      private List accessPointValues = CollectionUtils.newList();

      private List outDataMappingOrder = CollectionUtils.newList();

      private IApplication application;

      private Class theType;
      
      private ResolvedCtor ctor;
      
      private ResolvedMethod method;

      private Object theObject;

      private Object lastReturnValue;
   }
}
