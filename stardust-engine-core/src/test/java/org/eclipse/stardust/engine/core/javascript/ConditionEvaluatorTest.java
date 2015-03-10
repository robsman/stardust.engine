/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.javascript;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mozilla.javascript.EcmaError;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Scripting;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.el.EvaluationError;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;
import org.eclipse.stardust.engine.core.model.beans.DataTypeBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

public class ConditionEvaluatorTest
{
   @Mock
   private ModelElement modelElement;
   @Mock
   private SymbolTable symbolTable;
   @Mock
   private IModel model;
   @Mock
   private Scripting scripting;
   
   private Map<String, Object> modelElementRuntimeAttrs = CollectionUtils.newHashMap();
   
   @Before
   public void initMockObjects()
   {
      MockitoAnnotations.initMocks(this);
      when(modelElement.getModel()).thenReturn(model);
      doAnswer(new Answer<Object>()
      {
         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            String runtimeAttribute = (String)invocation.getArguments()[0];
            return modelElementRuntimeAttrs.get(runtimeAttribute);
         }
      }).when(modelElement).getRuntimeAttribute(anyString());
      doAnswer(new Answer<Object>()
      {
         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            String runtimeAttribute = (String)invocation.getArguments()[0];
            modelElementRuntimeAttrs.put(runtimeAttribute, invocation.getArguments()[1]);
            return null;
         }
      }).when(modelElement).setRuntimeAttribute(anyString(), any());
      when(model.getScripting()).thenReturn(scripting);
   }
   
   @Test
   public void testSimpleConditions()
   {
      when(scripting.getType()).thenReturn(ConditionEvaluator.ECMASCRIPT_TYPE);
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: TRUE"));
      modelElementRuntimeAttrs.clear();
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: true"));
      modelElementRuntimeAttrs.clear();
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "true"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isEnabled(modelElement, symbolTable, "OTHERWISE"));
      modelElementRuntimeAttrs.clear();
      
      assertTrue(ConditionEvaluator.isOtherwiseEnabled(modelElement, symbolTable, "OTHERWISE"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isOtherwiseEnabled(modelElement, symbolTable, "true"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isOtherwiseEnabled(modelElement, symbolTable, "carnotEL: TRUE"));
      modelElementRuntimeAttrs.clear();
      
      when(scripting.getType()).thenReturn("");
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "TRUE"));
      modelElementRuntimeAttrs.clear();
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "true"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isEnabled(modelElement, symbolTable, "FALSE"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isEnabled(modelElement, symbolTable, "false"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isOtherwiseEnabled(modelElement, symbolTable, "false"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isOtherwiseEnabled(modelElement, symbolTable, "carnotEL: FALSE"));
      modelElementRuntimeAttrs.clear();
   }
   
   @Test
   public void testConditionsWithVariables()
   {
      when(scripting.getType()).thenReturn(ConditionEvaluator.ECMASCRIPT_TYPE);
      
      final AccessPoint data = Mockito.mock(AccessPoint.class);
      PluggableType retryType = new DataTypeBean("Retry", "Retry", false);
      retryType.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT, PrimitiveAccessPathEvaluator.class.getName());
      when(data.getType()).thenReturn(retryType);
      doAnswer(new Answer<AccessPoint>()
      {
         @Override
         public AccessPoint answer(InvocationOnMock invocation) throws Throwable
         {
            String name = (String)invocation.getArguments()[0];
            if("Retry".equals(name))
            {
               return data;
            }
            return null;
         }
      }).when(symbolTable).lookupSymbolType(anyString());
      
      doAnswer(new Answer<Object>()
      {
         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            String name = (String)invocation.getArguments()[0];
            if("Retry".equals(name))
            {
               return Boolean.TRUE;
            }
            else if("true".equals(name) || "TRUE".equals(name) || "false".equals(name) || "FALSE".equals(name))
            {
               return name;
            }
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(name), name);
         }
      }).when(symbolTable).lookupSymbol(anyString());
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: Retry = TRUE"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: Retry = FALSE"));
      modelElementRuntimeAttrs.clear();
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "Retry == true"));
      modelElementRuntimeAttrs.clear();
      assertFalse(ConditionEvaluator.isEnabled(modelElement, symbolTable, "Retry == false"));
      modelElementRuntimeAttrs.clear();
      
      assertTrue(ConditionEvaluator.isEnabled(modelElement, symbolTable, "Retry = TRUE", true));
      modelElementRuntimeAttrs.clear();
      
      boolean exceptionThrown = false;
      try
      {
         ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: Retry == TRUE");
      }
      catch(InternalException ie)
      {
         if(ie.getCause() instanceof SyntaxError)
         {
            exceptionThrown = true;
         }
      }
      assertTrue("== is not allowed in carnotEL", exceptionThrown);
      modelElementRuntimeAttrs.clear();
      exceptionThrown = false;
      try
      {
         ConditionEvaluator.isEnabled(modelElement, symbolTable, "carnotEL: NotRetry = TRUE");
      }
      catch(InternalException ie)
      {
         if(ie.getCause() instanceof EvaluationError)
         {
            exceptionThrown = true;
         }
      }
      assertTrue("EvaluationError must be thrown if an variable (in this case NotRetry) is unknown", exceptionThrown);
      modelElementRuntimeAttrs.clear();
      
      exceptionThrown = false;
      try
      {
         ConditionEvaluator.isEnabled(modelElement, symbolTable, "Retry = TRUE");
      }
      catch(PublicException pe)
      {
         if(pe.getCause() instanceof EcmaError && pe.getMessage().contains("ReferenceError: \"TRUE\" is not defined"))
         {
            exceptionThrown = true;
         }
      }
      assertTrue(exceptionThrown);
      modelElementRuntimeAttrs.clear();
   }

}
