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
package org.eclipse.stardust.engine.extensions.transformation.runtime.serialization;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactoryUtils;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.eclipse.stardust.engine.extensions.transformation.format.IMessageFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * <p>
 * This class tests the <i>Message Serialization Application Type</i>
 * ({@link org.eclipse.stardust.engine.extensions.transformation.runtime.serialization.MessageSerializationApplicationInstance}).
 * </p>
 * 
 * @author nicolas.werlein
 * @version $Revision$ 
 */
public class MessageSerializationApplicationInstanceTest
{
   private static final String APP_ACCESS_POINT_ID = "<Application Access Point Id>";
   
   @Mock
   private ActivityInstance ai;
   
   private MessageSerializationApplicationInstance out;
   
   @Before
   public void setUp()
   {
      out = new MessageSerializationApplicationInstance();
      
      MockitoAnnotations.initMocks(this);
   }
   
   @Test(expected = NullPointerException.class)
   public void testBootstrapFailForNull()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.bootstrap(null);
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test
   public void testBootstrap()
   {
      /* stubbing */
      defineBootstrapMocks();
      
      /* invoking the method under test */
      out.bootstrap(ai);
      
      /* verifying */
      verifyStateAfterBootstrap();
   }

   private void defineBootstrapMocks()
   {
      final String messageFormatId = "XML";
      
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      final ModelManager modelManager = mock(ModelManager.class);
      ModelManagerFactoryUtils.initRtEnvWithModelManager(modelManager);
      final IModel model = mock(IModel.class);
      final IData data = mock(IData.class);
      final ApplicationContext appCtx = mock(ApplicationContext.class);
      final DataMapping dataMapping = mock(DataMapping.class);
      final AccessPoint accessPoint = mock(AccessPoint.class);
      final StructuredDataConverter converter = mock(StructuredDataConverter.class);
      final Document schemaDoc = mock(Document.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(modelManager.findModel(anyLong())).thenReturn(model);
      when(model.findData(anyString())).thenReturn(data);
      when(activity.getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)).thenReturn(appCtx);
      when(appCtx.getAllInDataMappings()).thenReturn(Collections.singletonList(dataMapping));
      when(appCtx.getAllOutDataMappings()).thenReturn(Collections.singletonList(dataMapping));
      when(dataMapping.getApplicationAccessPoint()).thenReturn(accessPoint);
      when(accessPoint.getId()).thenReturn(APP_ACCESS_POINT_ID);
      when(app.getAttribute(Constants.MESSAGE_FORMAT)).thenReturn(messageFormatId);
      
      out = newTestAdjustedInstance(converter, schemaDoc);
   }

   private MessageSerializationApplicationInstance newTestAdjustedInstance(final StructuredDataConverter converter, final Document schemaDoc)
   {
      return new MessageSerializationApplicationInstance()
      {
         /* package-private */ IXPathMap getXPathMap(final IData ignored)
        {
           return null;
        };
        
        /* package-private */ StructuredDataConverter newStructuredDataConverter(final IXPathMap ignored)
        {
           return converter;
        };
        
        /* package-private */ Document getSchemaDocument(final IData ignored)
        {
           return schemaDoc;
        }
      };
   }
   
   private void verifyStateAfterBootstrap()
   {
      final StructuredDataConverter actualDataConverter = (StructuredDataConverter) Reflect.getFieldValue(out, "structuredDataConverter");
      assertNotNull(actualDataConverter);

      final Document actualSchemaDoc = (Document) Reflect.getFieldValue(out, "schemaDocument");
      assertNotNull(actualSchemaDoc);
      
      final Map<String, String> actualOutputValues = (Map<String, String>) Reflect.getFieldValue(out, "outputValues");
      assertNotNull(actualOutputValues);

      final IMessageFormat actualMessageFormat = (IMessageFormat) Reflect.getFieldValue(out, "messageFormat");
      assertNotNull(actualMessageFormat);
      
      final DOMImplementation actualDomImpl = (DOMImplementation) Reflect.getFieldValue(out, "domImpl");
      assertNotNull(actualDomImpl);
      
      final List<Pair> actualInAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "inAccessPointValues");
      assertNotNull(actualInAccessPointValues);

      final Map<String, DataMapping> actualOutAccessPoints = (Map<String, DataMapping>) Reflect.getFieldValue(out, "outAccessPoints");
      assertNotNull(actualOutAccessPoints);
      assertNotNull(actualOutAccessPoints.get(APP_ACCESS_POINT_ID));
   }
   
   @Test
   public void testSetInAccessPointValueNoOverride()
   {
      final String value = "Value";
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.setInAccessPointValue(APP_ACCESS_POINT_ID, value);
      
      /* verifying */
      final List<Pair> actualInAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "inAccessPointValues");
      assertNotNull(actualInAccessPointValues);
      assertEquals(1, actualInAccessPointValues.size());
      
      assertThat(actualInAccessPointValues.get(0).getSecond(), org.hamcrest.Matchers.is(String.class));
      final String actualValue = (String) actualInAccessPointValues.get(0).getSecond();
      assertEquals(value, actualValue);
   }
   
   @Test
   public void testSetInAccessPointValueOverride()
   {
      final String initialValue = "Initial Value";
      final String newValue = "New Value";
      

      /* stubbing */
      final List<Pair> initialInAccessPointValues = newArrayList();
      initialInAccessPointValues.add(new Pair(APP_ACCESS_POINT_ID, initialValue));
      Reflect.setFieldValue(out, "inAccessPointValues", initialInAccessPointValues);
      
      /* invoking the method under test */
      out.setInAccessPointValue(APP_ACCESS_POINT_ID, newValue);
      
      /* verifying */
      final List<Pair> actualInAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "inAccessPointValues");
      assertNotNull(actualInAccessPointValues);
      assertEquals(1, actualInAccessPointValues.size());
      
      assertThat(actualInAccessPointValues.get(0).getSecond(), org.hamcrest.Matchers.is(String.class));
      final String actualValue = (String) actualInAccessPointValues.get(0).getSecond();
      assertEquals(newValue, actualValue);
   }
   
   @Test
   public void testGetOutAccessPointValueExists()
   {
      final String value = "Value";
      
      /* stubbing */
      final Map<String, String> initialOutputValues = newHashMap();
      initialOutputValues.put(APP_ACCESS_POINT_ID, value);
      Reflect.setFieldValue(out, "outputValues", initialOutputValues);
      
      /* invoking the method under test */
      final Object actualValue = out.getOutAccessPointValue(APP_ACCESS_POINT_ID);
      
      /* verifying */
      assertNotNull(actualValue);
      assertEquals(value, actualValue);
   }

   @Test
   public void testGetOutAccessPointValueDoesNotExist()
   {
      /* stubbing */
      final Map<String, String> initialOutputValues = newHashMap();
      Reflect.setFieldValue(out, "outputValues", initialOutputValues);
      
      /* invoking the method under test */
      final Object actualValue = out.getOutAccessPointValue(APP_ACCESS_POINT_ID);
      
      /* verifying */
      assertNull(actualValue);
   }

   @Test(expected = InvocationTargetException.class)
   public void testInvokeInputMessageIsEmpty() throws Exception
   {
      /* stubbing */
      final List<Pair> inAccessPointValues = newArrayList();
      inAccessPointValues.add(new Pair("", Collections.emptyMap()));
      Reflect.setFieldValue(out, "inAccessPointValues", inAccessPointValues);
      
      final StructuredDataConverter converter = mock(StructuredDataConverter.class);
      Reflect.setFieldValue(out, "structuredDataConverter", converter);
      when(converter.toDom(Collections.emptyMap(), "", true)).thenReturn(new Node[0]);
      
      /* invoking the method under test */
      out.invoke(null);
      Assert.fail("Previous statement should throw an exception.");
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test(expected = InvocationTargetException.class)
   public void testInvokeInputMessageIsNull() throws Exception
   {
      /* stubbing */
      final List<Pair> inAccessPointValues = newArrayList();
      inAccessPointValues.add(new Pair("", Collections.emptyMap()));
      Reflect.setFieldValue(out, "inAccessPointValues", inAccessPointValues);
      
      final StructuredDataConverter converter = mock(StructuredDataConverter.class);
      Reflect.setFieldValue(out, "structuredDataConverter", converter);
      when(converter.toDom(Collections.emptyMap(), "", true)).thenReturn(null);
      
      /* invoking the method under test */
      out.invoke(null);
      Assert.fail("Previous statement should throw an exception.");
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test
   public void testInvoke() throws Exception
   {
      final String outAccessPointKey = "<Key>";
      
      /* stubbing */
      final Document doc = mock(Document.class);
      out = newTestAdjustedInstance(doc);

      final List<Pair> inAccessPointValues = newArrayList();
      Reflect.setFieldValue(out, "inAccessPointValues", inAccessPointValues);
      final Map<String, DataMapping> outAccessPoints = (Map<String, DataMapping>) Reflect.getFieldValue(out, "outAccessPoints");
      outAccessPoints.put(outAccessPointKey, null);
      final Map<String, String> outputValues = newHashMap();
      Reflect.setFieldValue(out, "outputValues", outputValues);
      
      final StructuredDataConverter converter = mock(StructuredDataConverter.class);
      Reflect.setFieldValue(out, "structuredDataConverter", converter);
      final Element element = mock(Element.class);
      when(converter.toDom(null, "", true)).thenReturn(new Node[] { element });
      
      final IMessageFormat messageFormat = mock(IMessageFormat.class);
      Reflect.setFieldValue(out, "messageFormat", messageFormat);
      
      /* invoking the method under test */
      final Map<String, String> actualResult = out.invoke(Collections.singleton(outAccessPointKey));
      
      /* verifying */
      assertTrue(actualResult.containsKey(outAccessPointKey));
      final String actualValue = actualResult.get(outAccessPointKey);
      assertEquals("", actualValue);
   }
   
   private MessageSerializationApplicationInstance newTestAdjustedInstance(final Document doc)
   {
      return new MessageSerializationApplicationInstance()
      {
         @Override
         Document toW3CDocument(final org.eclipse.stardust.engine.core.struct.sxml.Document domDocument)
         {
            return doc;
         } 
      };
   }
   
   @Test
   public void testCleanupDoesNotThrowExeption()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.cleanup();
      
      /* verifying */
      /* nothing to do */
   }
}
