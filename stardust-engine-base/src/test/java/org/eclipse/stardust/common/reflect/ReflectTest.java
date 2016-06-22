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
package org.eclipse.stardust.common.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.stardust.common.reflect.GenericClass;
import org.eclipse.stardust.common.reflect.MethodDescriptor;
import org.eclipse.stardust.common.reflect.Reflect;

import junit.framework.TestCase;


/**
 * @author ubirkemeyer
 * @version $Revision: 30357 $
 */
public class ReflectTest extends TestCase
{
   public void testJava5()
   {
      String stringClassName = String.class.getName();
      String rawClassName = GenericClass.class.getName();
      String parameterizedClassName = rawClassName + '<' + stringClassName + '>';
      Class<?> clazz = Reflect.getClassFromClassName(parameterizedClassName);
      assertEquals(GenericClass.class, clazz);
      Object object = Reflect.createInstance(parameterizedClassName);
      assertNotNull(object);
      assertEquals(GenericClass.class, object.getClass());

      @SuppressWarnings("unchecked")
      List<Method[]> methodsList = Reflect.collectGetSetMethods(clazz);

      assertEquals(1, methodsList.size());
      Method[] methods = (Method[]) methodsList.get(0);
      assertEquals(2, methods.length);
      String encodedMethod = "setValue(java.lang.String)";
      try
      {
         Method method = Reflect.decodeMethod(clazz, encodedMethod);
         System.out.println(method);
         assertNotNull(method);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         assertTrue(ex == null);
      }
      String encodedConstructor1 = "GenericClass()";
      String encodedConstructor2 = "GenericClass(java.lang.String)";
      try
      {
         Constructor<?> constructor = Reflect.decodeConstructor(clazz, encodedConstructor1);
         System.out.println(constructor);
         assertNotNull(constructor);
         constructor = Reflect.decodeConstructor(clazz, encodedConstructor2);
         System.out.println(constructor);
         assertNotNull(constructor);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         assertTrue(ex == null);
      }
   }
   
   public void testJava5describeEncodedMethodTypeParams1()
   {
      String encodedMethod = "notifyCustomer(java.util.Map<java.lang.String,java.lang.Object>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[0].getName());
      assertNotNull(method);
   }

   public void testJava5describeEncodedMethodTypeParams2()
   {
      String encodedMethod = "notifyCustomer(java.util.Map<java.lang.String,java.lang.Object>, java.util.List<java.lang.String>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[0].getName());
      assertEquals("java.util.List", method.getArgumentTypeArray()[1].getName());
      assertNotNull(method);
   }

   public void testJava5describeEncodedMethodTypeParams3()
   {
      String encodedMethod = "notifyCustomer(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[0].getName());
      assertNotNull(method);
   }

   public void testJava5describeEncodedMethodTypeParams4()
   {
      String encodedMethod = "notifyCustomer(java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.util.List<java.lang.String>>>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[0].getName());
      assertNotNull(method);
   }

   public void testJava5describeEncodedMethodTypeParams5()
   {
      String encodedMethod = "notifyCustomer(java.util.Map<java.lang.String>,java.util.Map<java.lang.String,java.util.List<java.lang.String>>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[0].getName());
      assertEquals("java.util.Map", method.getArgumentTypeArray()[1].getName());
      assertNotNull(method);
   }

   public void testJava5describeEncodedMethodTypeParams6()
   {
      String encodedMethod = "notifyCustomer(java.util.List<java.lang.String>)";
      MethodDescriptor method = Reflect.describeEncodedMethod(encodedMethod);
      assertEquals("notifyCustomer", method.getName());
      assertEquals("java.util.List", method.getArgumentTypeArray()[0].getName());
      assertNotNull(method);
   }
   
   public void testGetHumanReadableClassName()
   {
      assertEquals(Reflect.getHumanReadableClassName(char[].class), "char[]");
      assertEquals(Reflect.getHumanReadableClassName(Class[].class), "Class[]");
      assertEquals(Reflect.getHumanReadableClassName(Object[].class), "Object[]");
      assertEquals(Reflect.getHumanReadableClassName(AClass.class), "ReflectTest$AClass");
      assertEquals(Reflect.getHumanReadableClassName(AClass[].class), "ReflectTest$AClass[]");
      assertEquals(Reflect.getHumanReadableClassName(char.class), "char");
      assertEquals(Reflect.getHumanReadableClassName(Character.class), "Character");
   }

   private class AClass
   {
   }
}
