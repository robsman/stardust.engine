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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.spi.ITypeNameResolver;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;



/**
 * Various getter methods returning common metamodel information.
 * Extension of the java.lang.reflect package.
 */
public class Reflect
{
   private static final Logger trace = LogManager.getLogger(Reflect.class);

   private static Map singletons = new HashMap();

   /**
    * Retrieves a class object from an abbreviated class name. The lookup rules are:
    *
    * <ul>
    *    <li>an empty name or <code>String</code> gets translated to {@link String}</li>
    *    <li><code>boolean</code>, <code>char</code>, <code>byte</code>,
    *        <code>short</code>, <code>int</code>, <code>long</code>, <code>float</code>,
    *        <code>double</code> are translated to their appropriate wrapper classes</li>
    *    <li><code>Money</code> gets translated to {@link Money}</li>
    *    <li><code>Calendar</code> gets translated to {@link Calendar}</li>
    *    <li><code>Timestamp</code> gets translated to {@link Date}</li>
    *    <li>If no match is found a normal lookup is done.</li>
    * </ul>
    *
    * @param className the name of the class to retrieve
    * @return The retrieved class, <code>null</code> if no such class can be found.
    *
    * @see #getClassFromClassName(java.lang.String)
    */
   public static Class getClassFromAbbreviatedName(String className)
   {
      if (StringUtils.isEmpty(className) || "String".equals(className))
      {
         return String.class;
      }
      else if ("boolean".equals(className))
      {
         return Boolean.class;
      }
      else if ("char".equals(className))
      {
         return Character.class;
      }
      else if ("byte".equals(className))
      {
         return Byte.class;
      }
      else if ("short".equals(className))
      {
         return Short.class;
      }
      else if ("int".equals(className))
      {
         return Integer.class;
      }
      else if ("long".equals(className))
      {
         return Long.class;
      }
      else if ("float".equals(className))
      {
         return Float.class;
      }
      else if ("double".equals(className))
      {
         return Double.class;
      }
      else if ("Money".equals(className))
      {
         return Money.class;
      }
      else if ("Calendar".equals(className))
      {
         return Calendar.class;
      }
      else if ("Timestamp".equals(className))
      {
         return Date.class;
      }
      else if ("Period".equals(className))
      {
         return Period.class;
      }
      return getClassFromClassName(className);
   }


   public static String getAbbreviatedName(Class clazz)
   {
      if (clazz == String.class)
      {
         return "";
      }
      else if (clazz == Boolean.class || clazz == Boolean.TYPE)
      {
         return "boolean";
      }
      else if (clazz == Character.class || clazz == Character.TYPE)
      {
         return "char";
      }
      else if (clazz == Byte.class || clazz == Byte.TYPE)
      {
         return "byte";
      }
      else if (clazz == Short.class || clazz == Short.TYPE)
      {
         return "short";
      }
      else if (clazz == Integer.class || clazz == Integer.TYPE)
      {
         return "int";
      }
      else if (clazz == Long.class || clazz == Long.TYPE)
      {
         return "long";
      }
      else if (clazz == Float.class || clazz == Float.TYPE)
      {
         return "float";
      }
      else if (clazz == Double.class || clazz == Double.TYPE)
      {
         return "double";
      }
      else if (clazz == Money.class)
      {
         return "Money";
      }
      else if (clazz == Calendar.class)
      {
         return "Calendar";
      }
      else if (clazz == Date.class)
      {
         return "Timestamp";
      }
      else if (clazz == Period.class)
      {
         return "Period";
      }
      return clazz.getName();
   }

   /**
    * Retrieves a class object from the class name. For "normal" Java classes,
    * the return value is the result of <code>Class.forName()</code>. For
    * primitive types <code>int</code>, <code>float</code>, ... their pseudo
    * class object e.g. <code>Integer.TYPE</code> is returned.
    *
    * @throws InternalException If the lookup fails.
    *
    * @see #getClassFromAbbreviatedName(java.lang.String)
    */
   public static Class getClassFromClassName(String className)
   {
      return getClassFromClassName(className, true);
   }

   /**
    * Retrieves a class object from the class name. For "normal" Java classes,
    * the return value is the result of <code>Class.forName()</code>. For
    * primitive types <code>int</code>, <code>float</code>, ... their pseudo
    * class object e.g. <code>Integer.TYPE</code> is returned.
    * If <code>isMandatory</code> is set to false then <code>null</code> will
    * be returned instead of throwing an <code>InternalException</code>.
    *
    * @throws InternalException If the lookup fails and lenientLookup is enabled.
    *
    * @see #getClassFromAbbreviatedName(java.lang.String)
    */
   public static Class getClassFromClassName(String className, boolean isMandatory)
   {
      Class clazz = null;
      if ( !StringUtils.isEmpty(className))
      {
         List<ITypeNameResolver> typeNameResolvers = ExtensionProviderUtils.getExtensionProviders(ITypeNameResolver.class);
         for (int i = 0; i < typeNameResolvers.size(); ++i)
         {
            String resolvedType = typeNameResolvers.get(i).resolveTypeName(className);
            if (null != resolvedType)
            {
               className = resolvedType;
               break;
            }
         }
         
         className = getRawClassName(className);
         try
         {
            if (className.equals(Boolean.TYPE.getName()))
            {
               return Boolean.TYPE;
            }
            else if (className.equals(Character.TYPE.getName()))
            {
               return Character.TYPE;
            }
            else if (className.equals(Byte.TYPE.getName()))
            {
               return Byte.TYPE;
            }
            else if (className.equals(Short.TYPE.getName()))
            {
               return Short.TYPE;
            }
            else if (className.equals(Integer.TYPE.getName()))
            {
               return Integer.TYPE;
            }
            else if (className.equals(Long.TYPE.getName()))
            {
               return Long.TYPE;
            }
            else if (className.equals(Float.TYPE.getName()))
            {
               return Float.TYPE;
            }
            else if (className.equals(Double.TYPE.getName()))
            {
               return Double.TYPE;
            }
            else if (className.equals(Void.TYPE.getName()))
            {
               return Void.TYPE;
            }
            else
            {
               ClassLoader classLoader = getContextClassLoader();
               if (null != classLoader)
               {
                  clazz = Class.forName(className, true, classLoader);
               }
               else
               {
                  clazz = Class.forName(className);
               }
            }
         }
         catch (Exception x)
         {
            if (isMandatory)
            {
               throw new InternalException("Cannot retrieve class from class name '"
                     + className + "'.", x);
            }
         }
      }
      else
      {
         if (isMandatory)
         {
            // @todo (france, ub): throwing an exception here is experimental
            throw new InternalException("Empty class name.");
         }
      }

      return clazz;
   }

   public static Object createInstance(String className, ClassLoader loader)
   {
      try
      {
         className = getRawClassName(className);
         Class clazz = Class.forName(className, true, loader);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException("Cannot instantiate class '" + className + "'.", e);
      }
   }


   private static String getRawClassName(String className)
   {
      // strip type parameters info if present
      int ix = className.indexOf('<');
      if (ix > 0)
      {
         className = className.substring(0, ix);
      }
      return className;
   }

   public static Object createInstance(String className)
   {
      return createInstance(className, null, null);
   }

   public static Object createInstance(String className, Class[] argTypes, Object[] args)
   {
      Class clazz = Reflect.getClassFromClassName(className);

      return createInstance(clazz, argTypes, args);
   }

   public static Object createInstance(Class clazz, Class[] argTypes, Object[] args)
   {
      try
      {
         if (null == argTypes)
         {
            return clazz.newInstance();
         }
         else
         {
            Constructor ctor = clazz.getConstructor(argTypes);
            return ctor.newInstance(args);
         }
      }
      catch (Exception e)
      {
         throw new InternalException("Cannot instantiate class '" + clazz.getName()
               + "'.", e);
      }
   }

   /**
    * Returns always the same instance of the specified type
    */
   public static Object getInstance(String className)
   {
      Object instance = null;
      if (!StringUtils.isEmpty(className))
      {
         className = getRawClassName(className);
         instance = singletons.get(className);
         if (null == instance)
         {
            instance = createInstance(className);
            singletons.put(className, instance);
         }
      }
      return instance;
   }

   public static Class getWrapperClassFromPrimitiveClassName(Class primitiveClass)
   {
      Class wrapperClass = null;
      if ((null != primitiveClass) &&  isPrimitive(primitiveClass))
      {
         final String className = primitiveClass.getName();

         if (Boolean.TYPE.getName().equals(className))
         {
            wrapperClass = Boolean.class;
         }
         else if (Character.TYPE.getName().equals(className))
         {
            wrapperClass = Character.class;
         }
         else if (Byte.TYPE.getName().equals(className))
         {
            wrapperClass = Byte.class;
         }
         else if (Short.TYPE.getName().equals(className))
         {
            wrapperClass = Short.class;
         }
         else if (Integer.TYPE.getName().equals(className))
         {
            wrapperClass = Integer.class;
         }
         else if (Long.TYPE.getName().equals(className))
         {
            wrapperClass = Long.class;
         }
         else if (Float.TYPE.getName().equals(className))
         {
            wrapperClass = Float.class;
         }
         else if (Double.TYPE.getName().equals(className))
         {
            wrapperClass = Double.class;
         }
      }
      return wrapperClass;
   }

   /**
    *
    */
   public static boolean isAssignable(Class sourceClass, Class destinationClass)
   {
      try
      {
         if (isPrimitive(sourceClass))
         {
            sourceClass = getWrapperClassFromPrimitiveClassName(sourceClass);
         }

         if (isPrimitive(destinationClass))
         {
            destinationClass = getWrapperClassFromPrimitiveClassName(destinationClass);
         }

         return sourceClass.isAssignableFrom(destinationClass);
      }
      catch (Exception e)
      {
         trace.warn("", e);
         return false;
      }
   }

   public static boolean isPrimitive(Class clazz)
   {
      return (Boolean.TYPE == clazz) || (Byte.TYPE == clazz) || (Character.TYPE == clazz)
            || (Short.TYPE == clazz) || (Integer.TYPE == clazz) || (Long.TYPE == clazz)
            || (Float.TYPE == clazz) || (Double.TYPE == clazz) || (Void.TYPE == clazz);
   }

   /**
    * collects all methods starting with "get", having a corresponding "set"
    * method, having a primtive or string return type and taking no parameters
    */
   public static List collectGetSetMethods(Class clazz)
   {
      List foundGetSetMethods = new ArrayList();

      Method[] allPublicMethods = clazz.getMethods();

      for (int i = 0; i < allPublicMethods.length; i++)
      {
         Method getMethod = allPublicMethods[i];
         String getMethodName = getMethod.getName();

         if (Modifier.isStatic(getMethod.getModifiers()))
         {
            continue;
         }

         if (getMethod.getParameterTypes().length == 0
               //		 && ! (void.class == getMethod.getReturnType())
               && !(Void.TYPE == getMethod.getReturnType())
               //		 && ! "void".equals(getMethod.getReturnType().getDescription())
               && getMethodName.startsWith("get")
               && !("getPersistor".equals(getMethodName)))
         {
            // Check for corresponding "set" method

            String setMethodName = "s" + getMethodName.substring(1);

            Class[] args = {getMethod.getReturnType()};

            try
            {
               Method setMethod = clazz.getMethod(setMethodName, args);

               Method[] getSetPair = {getMethod, setMethod};
               foundGetSetMethods.add(getSetPair);
            }
            catch (java.lang.NoSuchMethodException exception)
            {
            }
         }
      }
      return foundGetSetMethods;
   }

   /**
    * Retrieves the class name in java syntax.
    */
   public static String getHumanReadableClassName(Class type)
   {
      return getHumanReadableClassName(type, false);
   }

   /**
    * Retrieves the class name in java syntax.
    */
   public static String getHumanReadableClassName(Class type, boolean fullyQualified)
   {
      if (type == null)
      {
         return null;
      }
      String className;
      if (type.isArray())
      {
         className = type.getComponentType().getName() + "[]";
      }
      else
      {
         className = type.getName();
      }
      int splitIndex = className.lastIndexOf('.');
      if (!fullyQualified && (-1 != splitIndex))
      {
         return className.substring(splitIndex + 1);
      }
      else
      {
         return className;
      }
   }

   /**
    * Returns a string <code>method(parametertype1, parametertype2, ...)</code>.
    * Return type is omitted because it is not needed for decoding.
    */
   public static String encodeMethod(Method method)
   {
      Assert.isNotNull(method, "Method is not null.");

      return MethodDescriptor.encodeMethod(method.getName(), method.getParameterTypes());
   }

   /**
    * Returns the Method object from the CARNOT internal stringified representation
    *
    * @return the Method object found
    * @throws InternalException if no match was found.
    */
   public static Method decodeMethod(Class type, String encodedMethod)
         throws InternalException
   {
      if (StringUtils.isEmpty(encodedMethod))
      {
         throw new InternalException("Encoded method is empty.");
      }

      MethodDescriptor descriptor = describeEncodedMethod(encodedMethod);
      String name = descriptor.getName();
      Class[] args = descriptor.getArgumentTypeArray();
      try
      {
         Method method = null;
         int currentMatch = 0;
         Method[] methods = type.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
            Method mtd = methods[i];
            Class[] params = mtd.getParameterTypes();
            if (mtd.getName().equals(name) && params.length == args.length)
            {
               int match = match(params, args);
               if (match >= 0)
               {
                  if (method == null || match < currentMatch)
                  {
                     method = mtd;
                     currentMatch = match;
                     if (match == 0)
                     {
                        break;
                     }
                  }
               }
            }
         }

//         Method method = type.getMethod(name, args);

//       no need for searching in interfaces. If the method is defined in an
//       interface, then it must be public in the implementing class
/*         if (!method.isAccessible())
         {
            Class[] interfaces = type.getInterfaces();
            for (int i = 0; i < interfaces.length; i++)
            {
               try
               {
                  Method intfMethod = interfaces[i].getMethod(name, args);
                  if (intfMethod != null)
                  {
                     return intfMethod;
                  }
               }
               catch (Exception ex)
               {
                  // go to the next interface
               }
            }
            // fall back to original method
         }*/
         return method;
      }
      catch (Exception e)
      {
         throw new InternalException("Method '" + descriptor + "' in '"
               + type + "' cannot be found or accessed.", e);
      }
   }

   private static int match(Class[] params, Class[] args)
   {
      int match = 0;
      for (int i = 0; i < params.length; i++)
      {
         if (params[i].equals(Object.class))
         {
            if (!args[i].equals(Object.class))
            {
               match++;
            }
         }
         else if (!params[i].equals(args[i]))
         {
            return -1;
         }
      }
      return match;
   }


   public static Constructor decodeConstructor(Class type, String constructorName)
   {
      if (StringUtils.isEmpty(constructorName))
      {
         throw new InternalException("Encoded method is empty.");
      }

      MethodDescriptor descriptor = describeEncodedMethod(constructorName);
      String name = descriptor.getName();
      String cname = type.getName();
      int ix = cname.lastIndexOf('.');
      if (ix >= 0)
      {
         cname = cname.substring(ix + 1);
      }
      if (!name.equals(cname))
      {
         throw new InternalException("Constructor name doesn't match class name.");
      }
      Class[] args = descriptor.getArgumentTypeArray();
      try
      {
         Constructor constructor = null;
         int currentMatch = 0;
         Constructor[] constructors = type.getConstructors();
         for (int i = 0; i < constructors.length; i++)
         {
            Constructor ctor = constructors[i];
            Class[] params = ctor.getParameterTypes();
            // constructors have no names
            if (/*ctor.getName().equals(name) &&*/ params.length == args.length)
            {
               int match = match(params, args);
               if (match >= 0)
               {
                  if (constructor == null || match < currentMatch)
                  {
                     constructor = ctor;
                     currentMatch = match;
                     if (match == 0)
                     {
                        break;
                     }
                  }
               }
            }
         }
/*         Constructor ctor = type.getConstructor(descriptor.getArgumentTypeArray());
         if (!descriptor.getName().equals(
               describeEncodedMethod((String) Reflect.encodeConstructor(ctor)).getName()))
         {
            throw new InternalException("The constructor '" + constructorName
                  + "' for class '" + type.getName() + "' does not exist.");
         }*/
         return constructor;
      }
      catch (Exception e)
      {
         throw new InternalException("The constructor '" + descriptor + "' for class '"
               + type.getName() + "' cannot be found or accessed.");
      }
   }

   /**
    * @param method
    * @return
    */
   public static String getSortableMethodName(Method method)
   {
      StringBuffer result = new StringBuffer();

      result.append(method.getName() + "(");

      Class[] parameters = method.getParameterTypes();

      for (int i = 0; i < parameters.length; i++)
      {
         result.append(getHumanReadableClassName(parameters[i]));

         if (i  != parameters.length - 1)
         {
            result.append(", ");
         }
      }

      result.append(") : ");
      result.append(getHumanReadableClassName(method.getReturnType()));

      return result.toString();
   }

   /**
    * Converts a String representation of an object to an object:
    * <ul>
    * <li>Java primitive types are accepted.</li>
    * <li>Other CARNOT primitive types (Calendar, Timestamp, Money) are accepted.</li>
    * <li>Abbreviated class names for primitive types are accepted.</li>
    * <li>Derivatives of StringKey and IntKey are accepted.</li>
    * <li>All types with a constructor accepting a single String argument are accepted.</li>
    * </ul>
    */
   public static Object convertStringToObject(String classname, String value)
   {
      try
      {
         Class type = Reflect.getClassFromAbbreviatedName(classname);

         if (null == value)
         {
            return null;
         }
         else if (String.class.isAssignableFrom(type))
         {
            return value;
         }
         else if (StringKey.class.isAssignableFrom(type))
         {
            return StringKey.getKey(type, value);
         }
         else if (IntKey.class.isAssignableFrom(type))
         {
            return IntKey.getKey(type, Integer.parseInt(value));
         }
         else if (Character.class == type)
         {
            if (StringUtils.isEmpty(value))
            {
               return new Character(' ');
            }
            return new Character(value.charAt(0));
         }
         else if (Date.class == type)
         {
            return DateUtils.getNoninteractiveDateFormat().parse(value);
         }
         else if (Calendar.class == type)
         {
            Calendar result = Calendar.getInstance();
            result.setTime(DateUtils.getNoninteractiveDateFormat().parse(value));
            return result;
         }
         else
         {
            Constructor constructor = type.getConstructor(new Class[]{String.class});
            return constructor.newInstance(new Object[]{value});
         }
      }
      catch (Exception x)
      {
         throw new InternalException("Conversion from String impossible for class '"
               + classname + "', value: '" + value + "'.", x);
      }
   }

   public static String convertObjectToString(Object value)
   {
      if (value == null)
      {
         return "";
      }
      if (value instanceof IntKey)
      {
         return Integer.toString(((IntKey) value).getValue());
      }
      else if (value instanceof Calendar)
      {
         return DateUtils.getNoninteractiveDateFormat().format(((Calendar)value).getTime());
      }
      else if (value instanceof Date)
      {
         return DateUtils.getNoninteractiveDateFormat().format((Date)value);
      }
      return value.toString();

   }



   public static String getSortableConstructorName(Constructor constructor)
   {
      StringBuffer result = new StringBuffer();

      result.append(getHumanReadableClassName(constructor.getDeclaringClass()) + "(");

      Class[] parameters = constructor.getParameterTypes();

      for (int i = 0; i < parameters.length; i++)
      {

         result.append(getHumanReadableClassName(parameters[i]));

         if (i != parameters.length - 1)
         {
            result.append(", ");
         }
      }

      result.append(")");
      return result.toString();
   }

   public static Object encodeConstructor(Constructor constructor)
   {
      Assert.isNotNull(constructor, "Method is not null.");

      return MethodDescriptor.encodeMethod(
            getHumanReadableClassName(constructor.getDeclaringClass()),
            constructor.getParameterTypes());
   }

   public static Collection getAnnotatedFields(Class clazz)
   {
      List result;

      if (clazz == null)
      {
         result = Collections.EMPTY_LIST;
      }
      else
      {
         result = new ArrayList();

         Field[] fields = clazz.getDeclaredFields();

         for (int i = 0; i < fields.length; i++)
         {
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers()) ||
                Modifier.isTransient(field.getModifiers()))
            {
               // static & transient fields are skipped
               continue;
            }
            AnnotatedField annotatedField = null;
            String annotationPrefix = getAnnotationPrefix(field.getName());
            for (int j = 0; j < fields.length; j++)
            {
               Field annotationCandidate = fields[j];
               if (annotationCandidate.getName().startsWith(annotationPrefix)
                     && Modifier.isStatic(annotationCandidate.getModifiers()))
               {
                  if (annotatedField == null)
                  {
                     annotatedField = new AnnotatedField(field);
                     result.add(annotatedField);
                  }
                  try
                  {
                     annotationCandidate.setAccessible(true);
                     annotatedField.addAnnotation(annotationCandidate.getName().substring(annotationPrefix.length()),
                           annotationCandidate.get(null));
                  }
                  catch (Exception e)
                  {
                     throw new InternalException(e);
                  }
               }
            }
         }

         result.addAll(getAnnotatedFields(clazz.getSuperclass()));
      }
      return result;
   }

   private static String getAnnotationPrefix(String fieldName)
   {
      StringBuffer prefix = new StringBuffer();
      for (int i = 0; i < fieldName.length(); i++)
      {
         char c = fieldName.charAt(i);
         if (!Character.isLetter(c))
         {
            prefix.append(c);
         }
         else if (Character.isLowerCase(c))
         {
            prefix.append(Character.toUpperCase(c));
         }
         else
         {
            prefix.append('_');
            prefix.append(Character.toUpperCase(c));
         }
      }
      prefix.append('_');
      return prefix.toString();
   }

   public static Field getField(Class clazz, String name)
   {
      Field field = null;

      if (null != clazz)
      {
         try
         {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
         }
         catch (NoSuchFieldException e)
         {
            // ignore, bubble up to super class
         }
         catch (SecurityException e)
         {
            throw new InternalException(e);
         }
         if (null == field)
         {
            field = getField(clazz.getSuperclass(), name);
         }
      }
      return field;
   }

   public static Object getStaticFieldValue(Class clazz, String name)
   {
      Object value = null;

      Field field = getField(clazz, name);
      if (field != null && Modifier.isStatic(field.getModifiers()))
      {
         field.setAccessible(true);
         try
         {
            value = field.get(null);
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
      return value;
   }

   public static Object getFieldValue(Object instance, String name)
   {
      Field field = getField(instance.getClass(), name);
      if (null != field)
      {
         field.setAccessible(true);
         try
         {
            return field.get(instance);
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
      else
      {
         throw new InternalException("Field '" + name + "' for '"
               + instance.getClass().getName() + "' not found");
      }
   }

   public static void setFieldValue(Object instance, String name, Object value)
   {
      Field field = getField(instance.getClass(), name);
      if (null != field)
      {
         field.setAccessible(true);
         try
         {
            field.set(instance, value);
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
      else
      {
         throw new InternalException("Field '" + name + "' for '"
               + instance.getClass().getName() + "' not found");
      }
   }

   public static Collection getFields(Class type)
   {
      List result;

      if (type == null)
      {
         result = Collections.EMPTY_LIST;
      }
      else
      {
         result = new ArrayList();

         Field[] fields = type.getDeclaredFields();

         for (int i = 0; i < fields.length; i++)
         {
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers()))
            {
               continue;
            }
            field.setAccessible(true);
            result.add(field);
         }

         result.addAll(getFields(type.getSuperclass()));
      }

      return result;
   }

   public static Method getSetterMethod(Class type, String name, Class parameterType)
   {
      Method result = null;

      Method[] methods = type.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         if (method.getName().equals(name))
         {
            Class[] parameters = method.getParameterTypes();
            if (1 == parameters.length)
            {
               if (parameterType == null || isAssignable(parameters[0], parameterType))
               {
                  result = methods[i];
                  break;
               }
            }
         }
      }
      return result;
   }

   public static MethodDescriptor describeEncodedMethod(String encodedMethod)
   {
      if (StringUtils.isEmpty(encodedMethod))
      {
         return null;
      }
      int lparenIndex = encodedMethod.indexOf('(');
      final int rparenIndex;

      if (-1 == lparenIndex)
      {
         lparenIndex = encodedMethod.length();
         rparenIndex = lparenIndex + 1;
      }
      else
      {
         rparenIndex = encodedMethod.indexOf(')', lparenIndex);

         if (-1 == rparenIndex)
         {
            throw new InternalException("Syntax error: missing terminating ')' after '(' "
                  + "in encoded method '" + encodedMethod + "'");
         }
      }

      MethodDescriptor method;
      if ((encodedMethod.length() == lparenIndex) || (lparenIndex + 1 == rparenIndex))
      {
         method = new MethodDescriptor(encodedMethod.substring(0, lparenIndex));
      }
      else
      {
         String parameterString = encodedMethod.substring(lparenIndex + 1, rparenIndex);
         List argumentTypes = new ArrayList();

         if (trace.isDebugEnabled())
         {
            trace.debug("Parsing method parameter list encoded as '" + parameterString
                  + "'.");
         }

         Iterator classNamesIter = StringUtils.split(parameterString, ",");

         if (parameterString.indexOf('<') > 0)
         {
            List classNames = getRawParamNames(parameterString);
            classNamesIter = classNames.iterator();
         }

         for (Iterator i = classNamesIter; i.hasNext();)
         {
            String className = ((String) i.next()).trim();

            try
            {
               argumentTypes.add(getClassFromClassName(className));
            }
            catch (InternalException e)
            {
               throw new InternalException("Class '" + className
                     + "' for parameter not found (" +encodedMethod +").", e);
            }
         }

         String methodName = encodedMethod.substring(0, lparenIndex);
         method = new MethodDescriptor(methodName, argumentTypes);
      }

      return method;
   }

   private static List getRawParamNames(String parameterString)
   {
      boolean endOfMtd = false;
      List /* <String> */classNames = new ArrayList();
      int lsplitIdx = 0;
      int rsplitIdx = parameterString.indexOf(',');
      if (rsplitIdx < 0)
      {
         rsplitIdx = parameterString.length();
      }

      while (!endOfMtd)
      {
         endOfMtd = rsplitIdx == parameterString.length();
         String subString = parameterString.substring(lsplitIdx, rsplitIdx);
         int charCount1 = 0;
         int charCount2 = 0;
         int idx = subString.indexOf('<');
         while (idx > 0)
         {
            charCount1++;
            idx = subString.indexOf('<', idx + 1);
         }
         idx = subString.indexOf('>');
         while (idx > 0)
         {
            charCount2++;
            idx = subString.indexOf('>', idx + 1);
         }
         if (charCount1 == charCount2)
         {
            classNames.add(subString);
            lsplitIdx = rsplitIdx + 1;
            rsplitIdx = parameterString.indexOf(',', rsplitIdx + 1);
         }
         else
         {
            rsplitIdx = parameterString.indexOf(',', rsplitIdx + 1);
         }
         if (rsplitIdx < 0)
         {
            rsplitIdx = parameterString.length();
         }
      }
      return classNames;
   }

   public static Object castValue(Object value, Class targetType)
         throws InvalidValueException
   {
      Object result;
      if (null == value)
      {
         if (isAssignable(byte.class, targetType))
         {
            result = new Byte((byte) 0);
         }
         else if (isAssignable(short.class, targetType))
         {
            result = new Short((short) 0);
         }
         else if (isAssignable(int.class, targetType))
         {
            result = new Integer(0);
         }
         else if (isAssignable(long.class, targetType))
         {
            result = new Long(0);
         }
         else if (isAssignable(float.class, targetType))
         {
            result = new Float(0);
         }
         else if (isAssignable(double.class, targetType))
         {
            result = new Double(0);
         }
         else
         {
            result = value;
         }
      }
      else if (isAssignable(targetType, value.getClass()))
      {
         result = value;
      }
      else if ((value instanceof Number) && isAssignable(Byte.class, targetType))
      {
         result = new Byte(((Number) value).byteValue());
      }
      else if ((value instanceof Number) && isAssignable(Short.class, targetType))
      {
         result = new Short(((Number) value).shortValue());
      }
      else if ((value instanceof Number) && isAssignable(Integer.class, targetType))
      {
         result = new Integer(((Number) value).intValue());
      }
      else if ((value instanceof Number) && isAssignable(Long.class, targetType))
      {
         result = new Long(((Number) value).longValue());
      }
      else if ((value instanceof Number) && isAssignable(Float.class, targetType))
      {
         result = new Float(((Number) value).floatValue());
      }
      else if ((value instanceof Number) && isAssignable(Double.class, targetType))
      {
         result = new Double(((Number) value).doubleValue());
      }
      else
      {
         throw new InvalidValueException(
               BpmRuntimeError.BPMRT_GENERAL_INCOMPATIBLE_TYPE.raise(value.getClass(),
                     targetType));
      }
      return result;
   }

   /**
    * @return the context classloader. May be null if it is not set or usage of
    *         this classloader is disabled by configuration (default behavior).
    */
   public static ClassLoader getContextClassLoader()
   {
      ClassLoader classLoader = null;

      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.USE_CONTEXT_CLASSLOADER, false))
      {
         classLoader = Thread.currentThread().getContextClassLoader();
      }

      return classLoader;
   }

   private Reflect()
   {
      // utility class
   }
}
