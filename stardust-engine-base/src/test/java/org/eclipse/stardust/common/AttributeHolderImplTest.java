/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class AttributeHolderImplTest
{

   @Test
   public void testGetRuntimeAttribute()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object value = new Object();
      attr.setRuntimeAttribute("1", value);
      
      // Act
      Object oldValue = attr.getRuntimeAttribute("1");
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(oldValue, is(sameInstance(value)));
   }

   @Test
   public void testGetRuntimeAttributeFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      
      // Act
      Object oldValue = attr.getRuntimeAttribute("1");
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(oldValue, is(nullValue()));
   }

   @Test
   public void testGetRuntimeAttributeWithWrongType()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      attr.setRuntimeAttribute("1", Boolean.TRUE);
      Integer oldValue = null;
      
      // Act
      try
      {
         oldValue = attr.getRuntimeAttribute("1");
         fail("Requesting a runtime attribute with the wrong type should throw an exception");
      }
      catch(Exception e)
      {
         assertThat(oldValue, is(nullValue()));
         assertThat(attr.<Boolean>getRuntimeAttribute("1"), is(sameInstance(Boolean.TRUE)));
      }
   }

   @Test
   public void testSetNewRuntimeAttributeInEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object value = new Object();
      
      // Act
      Object oldValue = attr.setRuntimeAttribute("1", value);
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(attr.getRuntimeAttribute("1"), is(sameInstance(value)));
      assertThat(oldValue, is(nullValue()));
   }

   @Test
   public void testSetNewRuntimeAttributeInChain()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object value1 = new Object();
      Object value2 = new Object();
      
      // Act
      Object oldValue1 = attr.setRuntimeAttribute("1", value1);
      Object oldValue2 = attr.setRuntimeAttribute("2", value2);
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(attr.getRuntimeAttribute("1"), is(sameInstance(value1)));
      assertThat(attr.getRuntimeAttribute("2"), is(sameInstance(value2)));
      assertThat(oldValue1, is(nullValue()));
      assertThat(oldValue2, is(nullValue()));
   }

   @Test
   public void testOverwriteRuntimeAttribute()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object overwittenValue = new Object();
      attr.setRuntimeAttribute("1", overwittenValue);
      Object newValue = new Object();
      
      // Act
      Object oldValue = attr.setRuntimeAttribute("1", newValue);
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(attr.getRuntimeAttribute("1"), is(sameInstance(newValue)));
      assertThat(oldValue, is(sameInstance(overwittenValue)));
   }

   @Test
   public void testOverwriteRuntimeAttributeWithNull()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object overwittenValue = new Object();
      attr.setRuntimeAttribute("1", overwittenValue);
      
      // Act
      Object oldValue = attr.setRuntimeAttribute("1", null);
      
      // Assert
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
      assertThat(attr.getRuntimeAttribute("1"), is(nullValue()));
      assertThat(oldValue, is(sameInstance(overwittenValue)));
   }

   @Test
   public void testGetAttributeFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      
      // Act
      Object value = attr.getAttribute("1");
      
      // Assert
      assertThat(value, is(nullValue()));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetAttributeFromNonEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      
      // Act
      Object value = attr.getAttribute("1");
      
      // Assert
      assertThat(value, is(nullValue()));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetAttributeFromAttributeHolder()
   {
      // Arrange
      Object value = new Object();
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", value));
      
      // Act
      Object valueFromAttrHolder = attr.getAttribute("1");
      
      // Assert
      assertThat(valueFromAttrHolder, is(sameInstance(value)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(1)));
      assertThat(allAttributes.containsValue(value), is(true));
   }

   @Test
   public void testSetAllAttributesInEmptyAttributeHolder()
   {
      TestAttributeHolder attr = createTestAttributeHolder();
      Object value1 = new Object();
      Object value2 = new Object();
      
      // Act
      Map<String, Object> myAttributes = new HashMap<String, Object>();
      myAttributes.put("1", value1);
      myAttributes.put("2", value2);
      attr.setAllAttributes(myAttributes);
      
      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      assertThat(attr.getAttribute("1"), is(sameInstance(value1)));
      assertThat(attr.getAttribute("2"), is(sameInstance(value2)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(2)));
   }

   @Test
   public void testSetAllAttributesInNonEmptyAttributeHolder()
   {
      Object value1 = new Object();
      Object value2 = new Object();
      Object value3 = new Object();
      Object value4 = new Object();
      Object value5 = new Object();
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", value1),
            attr("2", value2));
      
      // Act
      Map<String, Object> myAttributes = new HashMap<String, Object>();
      myAttributes.put("1", value5);
      myAttributes.put("3", value3);
      myAttributes.put("4", value4);
      attr.setAllAttributes(myAttributes);
      
      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      assertThat(attr.getAttribute("1"), is(sameInstance(value5)));
      assertThat(attr.getAttribute("3"), is(sameInstance(value3)));
      assertThat(attr.getAttribute("4"), is(sameInstance(value4)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(3)));
   }
   
   @Test
   public void testSetNullForAllAttributesInEmptyAttributeHolder()
   {
      TestAttributeHolder attr = createTestAttributeHolder();
      
      // Act
      attr.setAllAttributes(null);
      
      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testSetNullForAllAttributesInNonEmptyAttributeHolder()
   {
      Object value1 = new Object();
      Object value2 = new Object();
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", value1),
            attr("2", value2));
      
      // Act
      attr.setAllAttributes(null);
      
      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testGetAllAttributes()
   {
      // Arrange
      Object value1 = new Object();
      Object value2 = new Object();
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", value1),
            attr("2", value2));
      
      // Act
      Map<String, Object> allAttributes = attr.getAllAttributes();
      
      // Assert
      assertThat(allAttributes.size(), is(equalTo(2)));
      assertThat(allAttributes.keySet(), contains("1", "2"));
      assertThat(allAttributes.values(), contains(value1, value2));
   }
   
   @Test
   public void testGetAllAttributesFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      
      // Act
      Map<String, Object> allAttributes = attr.getAllAttributes();
      
      // Assert
      assertThat(allAttributes.size(), is(equalTo(0)));
      assertThat(allAttributes, is(sameInstance(Collections.<String, Object>emptyMap())));
   }

   @Test
   public void testSetOneAttribute()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object attrValue = new Object();
      
      // Act
      attr.setAttribute("1", attrValue);
      
      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      assertThat(attr.getAttribute("1"), is(sameInstance(attrValue)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(1)));
   }

   @Test
   public void testSetTwoAttributes()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
      Object attrValue1 = new Object();
      Object attrValue2 = new Object();

      // Act
      attr.setAttribute("1", attrValue1);
      attr.setAttribute("2", attrValue2);

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      assertThat(attr.getAttribute("1"), is(sameInstance(attrValue1)));
      assertThat(attr.getAttribute("2"), is(sameInstance(attrValue2)));
      
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetBooleanFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
         
      // Act
      boolean value = attr.getBooleanAttribute("1");

      // Assert
      assertThat(value, is(equalTo(false)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetBooleanFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", true),
            attr("2", false));
      
      // Act
      boolean bValue1 = attr.getBooleanAttribute("1");
      boolean bValue2 = attr.getBooleanAttribute("2");
      
      // Assert
      assertThat(bValue1, is(equalTo(true)));
      assertThat(bValue2, is(equalTo(false)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetNonBooleanFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()));
      boolean bValue = false;
      
      // Act
      try
      {
         bValue = attr.getBooleanAttribute("1");
         // Assert
         fail("Non boolean value should throw an exception");
      }
      catch(ClassCastException e)
      {
         assertThat(bValue, is(equalTo(false)));         
         assertThat(attr.getAllAttributes().size(), is(equalTo(1)));         
      }
   }

   @Test
   public void testGetIntegerFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
         
      // Act
      int value = attr.getIntegerAttribute("1");

      // Assert
      assertThat(value, is(equalTo(0)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetIntegerFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", Integer.valueOf(50)),
            attr("2", Integer.valueOf(60)));
      
      // Act
      int iValue1 = attr.getIntegerAttribute("1");
      int iValue2 = attr.getIntegerAttribute("2");
      
      // Assert
      assertThat(iValue1, is(equalTo(50)));
      assertThat(iValue2, is(equalTo(60)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetNonIntegerFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()));
      int iValue = -1;
      
      // Act
      try
      {
         iValue = attr.getIntegerAttribute("1");
         // Assert
         fail("Non integer value should throw an exception");
      }
      catch(ClassCastException e)
      {
         assertThat(iValue, is(equalTo(-1)));         
         assertThat(attr.getAllAttributes().size(), is(equalTo(1)));         
      }
   }

   @Test
   public void testGetFloatFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
         
      // Act
      float value = attr.getFloatAttribute("1");

      // Assert
      assertThat(value, is(equalTo(0f)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetFloatFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", Float.valueOf(1.50f)),
            attr("2", Float.valueOf(1.60f)));
      
      // Act
      float fValue1 = attr.getFloatAttribute("1");
      float fValue2 = attr.getFloatAttribute("2");
      
      // Assert
      assertThat(fValue1, is(equalTo(1.50f)));
      assertThat(fValue2, is(equalTo(1.60f)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetNonFloatFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()));
      float fValue = -1.0f;
      
      // Act
      try
      {
         fValue = attr.getFloatAttribute("1");
         // Assert
         fail("Non float value should throw an exception");
      }
      catch(ClassCastException e)
      {
         assertThat(fValue, is(equalTo(-1.0f)));         
         assertThat(attr.getAllAttributes().size(), is(equalTo(1)));         
      }
   }

   @Test
   public void testGetLongFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
         
      // Act
      long value = attr.getLongAttribute("1");

      // Assert
      assertThat(value, is(equalTo(0l)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetLongFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", Long.valueOf(Long.MAX_VALUE)),
            attr("2", Long.valueOf(Long.MIN_VALUE)));
      
      // Act
      long lValue1 = attr.getLongAttribute("1");
      long lValue2 = attr.getLongAttribute("2");
      
      // Assert
      assertThat(lValue1, is(equalTo(Long.MAX_VALUE)));
      assertThat(lValue2, is(equalTo(Long.MIN_VALUE)));
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetNonLongFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()));
      long lValue = Long.MIN_VALUE;
      
      // Act
      try
      {
         lValue = attr.getLongAttribute("1");
         // Assert
         fail("Non long value should throw an exception");
      }
      catch(ClassCastException e)
      {
         assertThat(lValue, is(equalTo(Long.MIN_VALUE)));
         assertThat(attr.getAllAttributes().size(), is(equalTo(1)));
      }
   }

   @Test
   public void testGetStringFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();
         
      // Act
      String value = attr.getStringAttribute("1");

      // Assert
      assertThat(value, is(nullValue()));
      assertThat(attr.getAllAttributes().size(), is(equalTo(0)));
   }

   @Test
   public void testGetStringFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", "abc"),
            attr("2", "def"));
      
      // Act
      String sValue1 = attr.getStringAttribute("1");
      String sValue2 = attr.getStringAttribute("2");
      
      // Assert
      assertThat(sValue1, is(equalTo("abc")));
      assertThat(sValue2, is(equalTo("def")));
      assertThat(attr.getAllAttributes().size(), is(equalTo(2)));
   }

   @Test
   public void testGetNonStringFromAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()));
      String sValue = "abc";
      
      // Act
      try
      {
         sValue = attr.getStringAttribute("1");
         // Assert
         fail("Non String value should throw an exception");
      }
      catch(ClassCastException e)
      {
         assertThat(sValue, is(equalTo("abc")));
         assertThat(attr.getAllAttributes().size(), is(equalTo(1)));
      }
   }

   @Test
   public void testRemoveAllAttributesFromNonEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()),
            attr("2", new Object()),
            attr("3", new Object()));

      // Act
      attr.removeAllAttributes();

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testRemoveAllAttributesFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();

      // Act
      attr.removeAllAttributes();

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testRemoveAttributeFromNonEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()),
            attr("2", new Object()),
            attr("3", new Object()));

      // Act
      attr.removeAttribute("2");

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(2)));
      assertThat(allAttributes.keySet(), contains("1", "3"));
   }

   @Test
   public void testRemoveNonExistingAttributeFromNonEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()),
            attr("2", new Object()),
            attr("3", new Object()));

      // Act
      attr.removeAttribute("4");

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(3)));
      assertThat(allAttributes.keySet(), contains("1", "2", "3"));
   }

   @Test
   public void testRemoveNonExistingAttributeFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();

      // Act
      attr.removeAttribute("4");

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testRemoveAttributeFromEmptyAttributeHolder()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder();

      // Act
      attr.removeAttribute("1");

      // Assert
      assertThat(attr.isModified(), is(equalTo(true)));
      Map<String, Object> allAttributes = attr.getAllAttributes();
      assertThat(allAttributes.size(), is(equalTo(0)));
   }

   @Test
   public void testRemoveAttributeFromFromAllAttributesMap()
   {
      // Arrange
      TestAttributeHolder attr = createTestAttributeHolder(
            attr("1", new Object()),
            attr("2", new Object()),
            attr("3", new Object()));

      // Act
      Map<String, Object> allAttributes = attr.getAllAttributes();
      try
      {
         allAttributes.remove("1");
         // Assert
         fail("Direct modification of the attribute map should not be allowed");
      }
      catch(UnsupportedOperationException e)
      {
         assertThat(allAttributes.size(), is(equalTo(3)));
         assertThat(allAttributes.keySet(), contains("1", "2", "3"));
      }
   }
   
   private static Attribute attr(String name, Object value)
   {
      return new Attribute(name, value);
   }
   
   private TestAttributeHolder createTestAttributeHolder(Attribute... attributes)
   {
      TestAttributeHolder attr = new TestAttributeHolder();
      if(attributes.length > 0)
      {
         for (Pair<String, Object> attribute : attributes)
         {
            attr.setAttribute(attribute.getFirst(), attribute.getSecond());
            assertThat(attr.getAllAttributes().keySet().contains(attribute.getFirst()), 
                  is(equalTo(true)));
         }
      }
      attr.setUnmodified();
      assertThat(attr.isModified(), is(equalTo(false)));
      return attr;
   }

   static class TestAttributeHolder extends AttributeHolderImpl
   {
      boolean modified = false;
      
      @Override
      public void markModified()
      {
         modified = true;
      }
      
      public void setUnmodified()
      {
         modified = false;
      }
      
      public boolean isModified()
      {
         return modified;
      }
   }
   
   @SuppressWarnings("serial")
   static class Attribute extends Pair<String, Object>
   {
      public Attribute(String first, Object second)
      {
         super(first, second);
      }
      
   }
}
