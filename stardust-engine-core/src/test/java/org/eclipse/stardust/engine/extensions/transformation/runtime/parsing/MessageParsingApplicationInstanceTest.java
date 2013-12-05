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
package org.eclipse.stardust.engine.extensions.transformation.runtime.parsing;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactoryUtils;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.eclipse.stardust.engine.extensions.transformation.format.IMessageFormat;
import org.eclipse.stardust.engine.extensions.transformation.format.XMLMessageFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;

/**
 * <p>
 * This class tests the <i>Message Parsing Application Type</i>
 * ({@link org.eclipse.stardust.engine.extensions.transformation.runtime.parsing.MessageParsingApplicationInstance}).
 * </p>
 * 
 * @author nicolas.werlein
 * @version $Revision$ 
 */
public class MessageParsingApplicationInstanceTest
{
   @Mock
   private ActivityInstance ai;
   
   @Mock
   private IModel model;
   
   @Mock
   private DataMapping dataMapping;
   
   private MessageParsingApplicationInstance out;
   
   @Before
   public void setUp()
   {
      out = new MessageParsingApplicationInstance();
      
      MockitoAnnotations.initMocks(this);
   }
   
   @Test(expected = NullPointerException.class)
   public void testBootstrapFailForNull()
   {
      /* stubbing */
      final ModelManager modelManager = mock(ModelManager.class);
      ModelManagerFactoryUtils.initRtEnvWithModelManager(modelManager);
      
      /* invoking the method under test */
      out.bootstrap(null);
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test
   public void testBootstrap()
   {
      final String accessPointId = "<Access Point ID>";
      final String schema = "<Schema>";
      
      /* stubbing */
      defineBootstrapMocks(accessPointId, schema);
      
      /* invoking the method under test */
      out.bootstrap(ai);
      
      /* verifying */
      final IModel actualModel = (IModel) Reflect.getFieldValue(out, "model");
      assertEquals(model, actualModel);
      final Map<String, DataMapping> actualOutAccessPoints = (Map<String, DataMapping>) Reflect.getFieldValue(out, "outAccessPoints");
      assertEquals(dataMapping, actualOutAccessPoints.get(accessPointId));
      final Map<?, ?> actualOutputValues = (Map<?, ?>) Reflect.getFieldValue(out, "outputValues");
      assertTrue(actualOutputValues.isEmpty());
      final IMessageFormat actualMessageFormat = (IMessageFormat) Reflect.getFieldValue(out, "messageFormat");
      assertThat(actualMessageFormat, instanceOf(XMLMessageFormat.class));
      final String actualSchema = (String) Reflect.getFieldValue(out, "schema");
      assertEquals(schema, actualSchema);
   }
   
   @Test
   public void testSetInAccessPointValue()
   {
      final String name = "<Name>";
      final String value = "<Value>";
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.setInAccessPointValue(name, value);
      
      /* verifying */
      final List<Pair> actualInAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "inAccessPointValues");
      final Pair actualPair = actualInAccessPointValues.get(0);
      assertEquals(new Pair(name, value), actualPair);
   }
   
   @Test
   public void testSetInAccessPointValueOverride()
   {
      final String name = "<Name>";
      final String value1 = "<Value 1>";
      final String value2 = "<Value 2>";
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.setInAccessPointValue(name, value1);
      out.setInAccessPointValue(name, value2);
      
      /* verifying */
      final List<Pair> actualInAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "inAccessPointValues");
      final Pair actualPair = actualInAccessPointValues.get(0);
      assertEquals(new Pair(name, value2), actualPair);
   }
   
   @Test
   public void testGetOutAccessPointValue()
   {
      final String name = "<Name>";
      final String value = "<Value>";
      
      Reflect.setFieldValue(out, "outputValues", Collections.singletonMap(name, value));
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final Object actualValue = out.getOutAccessPointValue(name);
      
      /* verifying */
      assertEquals(value, actualValue);
   }
   
   @Test
   public void testInvoke() throws Exception
   {
      /* stubbing */
      final StructuredDataConverter converter = mock(StructuredDataConverter.class);
      final org.eclipse.stardust.engine.core.struct.sxml.Document doc = mock(org.eclipse.stardust.engine.core.struct.sxml.Document.class);
      out = newTestAdjustedInstance(doc, converter);
      
      injectInAccessPointValuesIntoOUT();
      final String outAccessPoint = "<Out Data Type>";
      injectOutAccessPointsIntoOUT(outAccessPoint);
      Reflect.setFieldValue(out, "outputValues", CollectionUtils.newHashMap());
      final Map<String, Object> outputValue = createOutputValue();
      
      final IModel model = mock(IModel.class);
      Reflect.setFieldValue(out, "model", model);
      final IMessageFormat messageFormat = mock(IMessageFormat.class);
      Reflect.setFieldValue(out, "messageFormat", messageFormat);
      
      when(converter.toCollection(any(Element.class), any(String.class), Matchers.anyBoolean())).thenReturn(outputValue);
      
      /* invoking the method under test */
      final Map<String, Map<String, Object>> result = out.invoke(Collections.singleton(outAccessPoint));
      
      /* verifying */
      Mockito.verify(messageFormat).parse(any(Reader.class), any(Document.class));
      final Map<String, Object> actualOutputValue = result.get(outAccessPoint);
      assertEquals(outputValue, actualOutputValue);
   }

   private MessageParsingApplicationInstance newTestAdjustedInstance(final org.eclipse.stardust.engine.core.struct.sxml.Document doc, final StructuredDataConverter converter)
   {
      return new MessageParsingApplicationInstance()
      {
         @Override
         /* package-private */ Document getSchemaDocument(final IData ignored)
         {
            return null;
         }
         
         @Override
         /* package-private */ org.eclipse.stardust.engine.core.struct.sxml.Document fromW3CDocument(final Document ignored)
         {
            return doc;
         }
         
         @Override
         /* package-private */ IXPathMap getXPathMap(final IData ignored)
         {
            return null;
         }
         
         @Override
         /* package-private */ StructuredDataConverter newStructuredDataConverter(final IXPathMap ignored)
         {
            return converter;
         }
      };
   }
   
   @Test
   public void testCleanupDoesNotThrowException()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.cleanup();
      
      /* verifying */
      /* nothing to do */
   }
   
   private void defineBootstrapMocks(final String accessPointId, final String schema)
   {
      final String messageFormatId = "XML";
      
      final ModelManager modelManager = mock(ModelManager.class);
      ModelManagerFactoryUtils.initRtEnvWithModelManager(modelManager);
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      final ApplicationContext appCtx = mock(ApplicationContext.class);
      final List<DataMapping> outDataMappings = Collections.singletonList(dataMapping);
      final AccessPoint accessPoint = mock(AccessPoint.class);

      when(modelManager.findModel(anyLong())).thenReturn(model);
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(activity.getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)).thenReturn(appCtx);
      when(appCtx.getAllOutDataMappings()).thenReturn(outDataMappings);
      when(dataMapping.getApplicationAccessPoint()).thenReturn(accessPoint);
      when(accessPoint.getId()).thenReturn(accessPointId);
      when(app.getAttribute(Constants.MESSAGE_FORMAT)).thenReturn(messageFormatId);
      when(app.getAttribute(Constants.FORMAT_MODEL_FILE_PATH)).thenReturn(schema);

   }
   
   private void injectOutAccessPointsIntoOUT(final String outAccessPoint)
   {
      final DataMapping dataMapping = new TestDataMapping("<Data ID>");
      final Map<String, DataMapping> outAccessPoints = newHashMap();
      outAccessPoints.put(outAccessPoint, dataMapping);
      Reflect.setFieldValue(out, "outAccessPoints", outAccessPoints);
   }

   private void injectInAccessPointValuesIntoOUT()
   {
      final String inAccessPoint = "<Input Access Point>";
      final String inValue = "<XML Value to be parsed>";
      final List<Pair> inAccessPointValues = newArrayList();
      inAccessPointValues.add(new Pair(inAccessPoint, inValue));
      Reflect.setFieldValue(out, "inAccessPointValues", inAccessPointValues);
   }
   
   private Map<String, Object> createOutputValue()
   {
      final Map<String, Object> result = newHashMap();
      result.put("<Key 1>", "<Value 1>");
      result.put("<Key 2>", "<Value 2>");
      return result;
   }
   
   private static final class TestDataMapping implements DataMapping
   {
      private static final long serialVersionUID = 1L;

      private final String dataId;
      
      public TestDataMapping(final String dataId)
      {
         if (dataId == null)
         {
            throw new NullPointerException("Data ID must not be null.");
         }
         if ("".equals(dataId))
         {
            throw new IllegalArgumentException("Data ID must not be empty.");
         }
         
         this.dataId = dataId;
      }

      public String getDataId()
      {
         return dataId;
      }

      public String getId()
      {
         throw new UnsupportedOperationException();
      }
      
      public short getPartitionOID()
      {
         throw new UnsupportedOperationException();
      }

      public String getPartitionId()
      {
         throw new UnsupportedOperationException();
      }

      public String getDescription()
      {
         throw new UnsupportedOperationException();
      }

      public String getName()
      {
         throw new UnsupportedOperationException();
      }

      public int getModelOID()
      {
         throw new UnsupportedOperationException();
      }

      public int getElementOID()
      {
         throw new UnsupportedOperationException();
      }

      public Map getAllAttributes()
      {
         throw new UnsupportedOperationException();
      }

      public Object getAttribute(String name)
      {
         throw new UnsupportedOperationException();
      }

      public String getNamespace()
      {
         throw new UnsupportedOperationException();
      }

      public String getQualifiedId()
      {
         throw new UnsupportedOperationException();
      }

      public String getApplicationPath()
      {
         throw new UnsupportedOperationException();
      }

      public AccessPoint getApplicationAccessPoint()
      {
         throw new UnsupportedOperationException();
      }

      public Class getMappedType()
      {
         throw new UnsupportedOperationException();
      }

      public Direction getDirection()
      {
         throw new UnsupportedOperationException();
      }


      public String getDataPath()
      {
         throw new UnsupportedOperationException();
      }
   }
}
