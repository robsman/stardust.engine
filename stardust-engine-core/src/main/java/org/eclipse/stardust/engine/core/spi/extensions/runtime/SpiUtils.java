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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.DataTypeDetails;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.extensions.data.AccessPathEvaluatorAdapter;
import org.eclipse.stardust.engine.core.extensions.data.DefaultDataFilterExtension;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidatorAdapter;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;


/**
 * Utility methods for the CARNOT SPI interface
 */

public class SpiUtils
{

   private static final String KEY_CACHED_STATELESS_APP_INSTANCES = SpiUtils.class.getName()
         + ".CachedStatelessApplicationInstances";

   private static final String KEY_CACHED_STATELESS_EVALUATORS = SpiUtils.class.getName()
         + ".CachedStatelessEvaluators";
   
   private static final String KEY_CACHED_STATELESS_VALIDATORS = SpiUtils.class.getName()
         + ".CachedStatelessValidators";


   public static ApplicationInstance createApplicationInstance(String className)
   {
      if (StringUtils.isEmpty(className))
      {
         return null;
      }
      
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap cachedStatelessAppInstances = (ConcurrentHashMap) globals.get(KEY_CACHED_STATELESS_APP_INSTANCES);
      
      StatelessApplicationInstance sharedInstance = (null != cachedStatelessAppInstances)
            ? (StatelessApplicationInstance) cachedStatelessAppInstances.get(className)
            : null;

            
      ApplicationInstance result = null;
      if (sharedInstance instanceof StatelessSynchronousApplicationInstance)
      {
         result = new StatelessSynchronousApplicationBinding(
               (StatelessSynchronousApplicationInstance) sharedInstance);
      }
      else if (sharedInstance instanceof StatelessAsynchronousApplicationInstance)
      {
         result = new StatelessAsynchronousApplicationBinding(
               (StatelessAsynchronousApplicationInstance) sharedInstance);
      }
      else
      {
         Object rawInstance = Reflect.createInstance(className);
         if (rawInstance instanceof StatelessApplicationInstance)
         {
            if (rawInstance instanceof StatelessSynchronousApplicationInstance)
            {
               // new-style implementation
               result = new StatelessSynchronousApplicationBinding(
                     (StatelessSynchronousApplicationInstance) rawInstance);
            }
            else if (rawInstance instanceof StatelessAsynchronousApplicationInstance)
            {
               // new-style implementation
               result = new StatelessAsynchronousApplicationBinding(
                     (StatelessAsynchronousApplicationInstance) rawInstance);
            }
            
            if (null == cachedStatelessAppInstances)
            {
               cachedStatelessAppInstances = (ConcurrentHashMap) globals.initializeIfAbsent(
                     KEY_CACHED_STATELESS_APP_INSTANCES, new ValueProvider()
                     {
                        public Object getValue()
                        {
                           return new ConcurrentHashMap();
                        }
                     });
            }
            
            cachedStatelessAppInstances.putIfAbsent(className, rawInstance);
         }

         if (null == result)
         {
            result = (ApplicationInstance) rawInstance;
         }
      }

      return result;
   }
   
   public static ExtendedDataValidator createExtendedDataValidator(String validatorClass)
   {
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap cachedStatelessValidators = (ConcurrentHashMap) globals
            .get(KEY_CACHED_STATELESS_VALIDATORS);
      
      ExtendedDataValidator validator = (null != cachedStatelessValidators)
            ? (ExtendedDataValidator) cachedStatelessValidators.get(validatorClass)
            : null;

      if (null == validator)
      {
         Object rawValidator = Reflect.createInstance(validatorClass);

         if (rawValidator instanceof ExtendedDataValidator)
         {
            // new-style implementation
            validator = (ExtendedDataValidator) rawValidator;
         }
         else if (rawValidator instanceof DataValidator)
         {
            // wrap the old-style implementation for backward compatibility
            validator = new DataValidatorAdapter((DataValidator) rawValidator);
         }

         if ((validator instanceof Stateless) && ((Stateless) validator).isStateless())
         {
            if (null == cachedStatelessValidators)
            {
               cachedStatelessValidators = (ConcurrentHashMap) globals
                     .initializeIfAbsent(KEY_CACHED_STATELESS_VALIDATORS,
                           new ValueProvider()
                           {
                              public Object getValue()
                              {
                                 return new ConcurrentHashMap();
                              }
                           });
            }

            cachedStatelessValidators.putIfAbsent(validatorClass, validator);
         }
      }
      
      return validator;
   }
   
   /**
    * Instantiate an implementation of the {@link ExtendedAccessPathEvaluator} interface.
    * If the class implements the old-style {@link AccessPathEvaluator} interface, an
    * {@link AccessPathEvaluatorAdapter} adapter will be created to wrap the old
    * implementation.
    * 
    * @param className
    *           class to instantiate, must have a public default constructor
    * @return implementation of the ExtendedAccessPathEvaluator
    */
   public static ExtendedAccessPathEvaluator createExtendedAccessPathEvaluator(
         String className)
   {
      if (StringUtils.isEmpty(className))
      {
         return null;
      }
      
      final GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap cachedStatelessEvaluators = (ConcurrentHashMap) globals.get(KEY_CACHED_STATELESS_EVALUATORS);
      
      ExtendedAccessPathEvaluator evaluator = (null != cachedStatelessEvaluators)
            ? (ExtendedAccessPathEvaluator) cachedStatelessEvaluators.get(className)
            : null;
            
      if (null == evaluator)
      {
         Object rawEvaluator = Reflect.createInstance(className);
         if (rawEvaluator instanceof ExtendedAccessPathEvaluator)
         {
            // new-style implementation
            evaluator = (ExtendedAccessPathEvaluator) rawEvaluator;
         }
         else if (rawEvaluator instanceof AccessPathEvaluator)
         {
            // wrap the old-style implementation for backward compatibility
            evaluator = new AccessPathEvaluatorAdapter((AccessPathEvaluator) rawEvaluator);
         }
         
         if ((evaluator instanceof Stateless) && ((Stateless) evaluator).isStateless())
         {
            if (null == cachedStatelessEvaluators)
            {
               cachedStatelessEvaluators = (ConcurrentHashMap) globals.initializeIfAbsent(
                     KEY_CACHED_STATELESS_EVALUATORS, new ValueProvider()
                     {
                        public Object getValue()
                        {
                           return new ConcurrentHashMap();
                        }
                     });
            }
            
            cachedStatelessEvaluators.putIfAbsent(className, evaluator);
         }
      }

      return evaluator;
   }

   /**
    * Instantiate an implementation of the {@link ExtendedAccessPathEvaluator} interface.
    * 
    * @param type
    *           to retrieve the class name from
    * @return implementation of the ExtendedAccessPathEvaluator
    * @see SpiUtils.createExtendedAccessPathEvaluator(String className)
    */
   public static ExtendedAccessPathEvaluator createExtendedAccessPathEvaluator(
         PluggableType type)
   {
      String accessPathEvaluatorClass = type.getStringAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT);
      return createExtendedAccessPathEvaluator(accessPathEvaluatorClass);
   }
   
   /**
    * Instantiates an {@link DataFilterExtension} for specified data. The first data definition 
    * that is exists is used for instantiation.
    * 
    * @param allDataForId all data definitions for a given data id stored in a map with runtimeOID as key
    * @return implementation of the DataFilterExtension
    */
   public static DataFilterExtension createDataFilterExtension(
         Map<Long, IData> allDataForId)
   {
      if (allDataForId.isEmpty())
      {
         // data could not be found, return the DefaultDataFilterExtension to guarantee the 
         // old behavior regarding using non-existing data in queries 
         return new DefaultDataFilterExtension();
      }
      else
      {
         IData data = (IData) allDataForId.values().iterator().next();
         DataTypeDetails dataTypeDetails = (DataTypeDetails) DetailsFactory.create(
               data.getType(), IDataType.class, DataTypeDetails.class);
         return (DataFilterExtension) Reflect.createInstance(dataTypeDetails
               .getDataFilterExtensionClass());

      }
   }

}
