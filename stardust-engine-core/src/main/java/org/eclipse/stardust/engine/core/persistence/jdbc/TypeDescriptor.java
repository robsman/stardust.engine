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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.PersistentVector;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractProperty;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * An instance of <tt>TypeDescriptor</tt> provides an efficient mapping between
 * Java classes and SQL tables.
 * <p>
 * It caches the persistent fields and the fields representing links.
 * <p>
 * All other other parts of the persistence manager are not aware of the
 * persistent-capable Java types or SQL tables.
 */
public class TypeDescriptor extends TableDescriptor implements ITypeDescriptor
{
   private static final Logger trace = LogManager.getLogger(TypeDescriptor.class);
   
   private static final String TABLE_NAME_ANNOTATION = "TABLE_NAME";
   private static final String DEFAULT_ALIAS_ANNOTATION = "DEFAULT_ALIAS";
   private static final String LOCK_TABLE_NAME_ANNOTATION = "LOCK_TABLE_NAME";
   private static final String LOCK_INDEX_NAME_ANNOTATION = "LOCK_INDEX_NAME";
   private static final String PK_FIELD_ANNOTATION = "PK_FIELD";
   private static final String PK_SEQUENCE_ANNOTATION = "PK_SEQUENCE";
   private static final String TRY_DEFERRED_INSERT_ANNOTATION = "TRY_DEFERRED_INSERT";

   private final String IDX_ANNOTATION_SUFFIX = "_INDEX";
   private final String UNIQUE_IDX_ANNOTATION_SUFFIX = "_UNIQUE" + IDX_ANNOTATION_SUFFIX;

   private static final String COLUMN_LENGTH_ANNOTATION_SUFFIX = "_COLUMN_LENGTH";
   
   private static final String LOADER_ANNOTATION = "LOADER";
   
   private static final String SECRET_ANNOTATION = "_SECRET";
   
   private static final String USE_LITERALS_SUFFIX = "_USE_LITERALS";

   private final Class type;
   private final String tableName;
   private final String defaultAlias;
   
   private final String lockTableName;
   private final String lockIndexName;
   private final ITableDescriptor tdLockTable;
   private boolean tryDeferredInsert;

   private final Field[] pkFields;
   private final String pkSequence;

   private List<FieldDescriptor> persistentFields = CollectionUtils.newArrayList();
   private List<LinkDescriptor> links = CollectionUtils.newArrayList();
   private List<IndexDescriptor> indexes = CollectionUtils.newArrayList();
   private List<LinkDescriptor> parents = Collections.emptyList();
   private List<PersistentVectorDescriptor> persistentVectors = CollectionUtils.newArrayList();
   private List<String> literalFields = CollectionUtils.newArrayList();

   private final Method encryptKeyGetterMethod;
   private final Method decryptKeyGetterMethod;

   private Loader loader;

   public static TypeDescriptor get(final Class type)
   {
      final TypeDescriptorRegistry tdRegistry = TypeDescriptorRegistry.current();
      
      TypeDescriptor result = tdRegistry.getDescriptor(type);

      if (null == result)
      {
         trace.warn("Persistent type not known at engine initialization: " + type);
         
         result = new TypeDescriptor(type);
      }

      return result;
   }

   /**
    * Maps a Java class name to a valid SQL table name by taking the uppercase
    * version of the pure class name, e.g. <tt>PERSON</tt> for
    * <tt>ag.carnot.person.Person</tt>.
    */
   public static String getTableName(Class type)
   {
      String result = (String) Reflect.getStaticFieldValue(type, TABLE_NAME_ANNOTATION);
      if (result != null)
      {
         return result;
      }
      return Reflect.getHumanReadableClassName(type).toUpperCase();
   }

   public static String getLockTableName(Class type)
   {
      String lockTarget = (String) Reflect.getStaticFieldValue(type,
            LOCK_TABLE_NAME_ANNOTATION);

      if (StringUtils.isEmpty(lockTarget))
      {
         lockTarget = getTableName(type);
      }

      return lockTarget;
   }

   public static String getLockIndexName(Class type)
   {
      String lockTargetIndex = (String) Reflect.getStaticFieldValue(type,
            LOCK_INDEX_NAME_ANNOTATION);

      return lockTargetIndex;
   }

   /**
    *
    */
   public static Method getEncryptKeyGetterMethod(Class type)
   {
      String methodName = (String) readAnnotation(type, null, "ENCRYPT_KEY_GETTER_METHOD");

      if (methodName == null)
      {
         return null;
      }

      try
      {
         Method method = type.getMethod(methodName);

         if (method.getReturnType() != String.class)
         {
            throw new InternalException(
                  "Encryption key getter method " + methodName + "() returns "
                  + method.getReturnType().getName() + " instead of java.lang.String.");
         }
         return method;
      }
      catch (Exception x)
      {
         trace.warn("", x);
         throw new InternalException(
               "Failed to lookup for encryption key getter method " + methodName + "().");
      }
   }

   /**
    *
    */
   public static Method getDecryptKeyGetterMethod(Class type)
   {
      String methodName = (String) readAnnotation(type, null, "DECRYPT_KEY_GETTER_METHOD");

      if (methodName == null)
      {
         return null;
      }

      try
      {
         Method method = type.getMethod(methodName);

         if (method.getReturnType() != String.class)
         {
            throw new InternalException(
                  "Decryption key getter method " + methodName + "() returns "
                  + method.getReturnType().getName() + " instead of java.lang.String.");
         }
         return method;
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed to lookup for decryption key getter method " + methodName + "().");
      }
   }
   
   private static boolean getTryDeferredInsert(Class type)
   {
      Boolean b = (Boolean) Reflect.getStaticFieldValue(type, TRY_DEFERRED_INSERT_ANNOTATION);
      if (b == null)
      {
         return false;
      }
      else
      {
         return b.booleanValue();
      }
   }

   /**
    * Retrieves an annotation for a persistent field, such as
    * <tt>employees_TABLE_NAME</tt> or the entire class, such as
    * <tt>TABLE_NAME</tt>.
    */
   private static Object readAnnotation(Class type, String fieldName, String annotation)
   {
      String annotationFieldName = null;
      if (fieldName == null)
      {
         annotationFieldName = annotation;
      }
      else
      {
         annotationFieldName = fieldName + annotation;
      }

      Field annotationField = null;
      try
      {
         annotationField = type.getDeclaredField(annotationFieldName);
      }
      catch (Exception x)
      {
         // Ignored
      }

      if (annotationField == null)
      {
         return null;
      }

      if (!Modifier.isStatic(annotationField.getModifiers()))
      {
         throw new InternalException(
               "Field '" + annotationFieldName + "' is not static.");
      }

      if (!Modifier.isFinal(annotationField.getModifiers()))
      {
         throw new InternalException(
               "Field '" + annotationFieldName + "' is not final.");
      }

      annotationField.setAccessible(true);

      Object annotationValue;
      try
      {
         annotationValue = annotationField.get(null);
      }
      catch (Exception x)
      {
         throw new InternalException("Failed to get value of annotation '" + annotationFieldName + "'.", x);
      }

      if (annotationValue == null)
      {
         throw new InternalException("The value of the annotation '" + annotationFieldName + "' is null.");
      }

      return annotationValue;
   }

   /**
    */
   private static Field getField(Class type, String fieldName)
   {
      if (type.getSuperclass() != null)
      {
         Field result = getField(type.getSuperclass(), fieldName);
         if (result != null)
         {
            return result;
         }
      }

      try
      {
         return type.getDeclaredField(fieldName);
      }
      catch (Exception x)
      {
         return null;
      }
   }

   /**
    * Retrieves the primary key field(s) using the information in
    * the static member <tt>PK_FIELD</tt>.
    */
   private static Field[] getPKFields(Class type)
   {
      String[] pkFieldString = null;

      try
      {
         Object pkAnnotation = readAnnotation(type, "", PK_FIELD_ANNOTATION);
         if (pkAnnotation instanceof String)
         {
            pkFieldString = new String[] {(String) pkAnnotation};
         }
         else if (pkAnnotation instanceof String[])
         {
            pkFieldString = (String[]) pkAnnotation;
         }
      }
      catch (InternalException x)
      {
         // Ignore
      }

      if (pkFieldString == null)
      {
         throw new InternalException("No annotation for PK in class " + type.getName());
      }

      Field[] result = new Field[pkFieldString.length];
      for (int i = 0; i < pkFieldString.length; i++ )
      {
         result[i] = getField(type, pkFieldString[i].trim());
         if (null == result[i])
         {
            throw new InternalException("Unable to find specified primary key field '"
                  + pkFieldString[i] + "'.");
         }
         result[i].setAccessible(true);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Found PK field " + pkFieldString + " for type " + type.getName());
      }

      return result;
   }

   /**
    *
    */
   public static boolean canBeMappedToColumnType(Class type)
   {
      return (type == Integer.TYPE
            || type == Long.TYPE
            || type == Long.class
            || type == Float.TYPE
            || type == Double.TYPE
            || type == Double.class
            || type == String.class
            || type == java.sql.Date.class
            || type == java.util.Date.class);
   }

   public TypeDescriptor(Class type)
   {
      this(null, type);
   }
   
   public TypeDescriptor(String schemaName, Class type)
   {
      super(schemaName);
      
      this.type = type;

      this.tableName = getTableName(type);
      this.defaultAlias = (String) Reflect.getStaticFieldValue(type,
            DEFAULT_ALIAS_ANNOTATION);
      this.lockTableName = getLockTableName(type);
      this.lockIndexName = getLockIndexName(type);
      this.tdLockTable = isDistinctLockTableName() ? new LockTableDescriptor(this) : null;
      this.tryDeferredInsert = getTryDeferredInsert(type); 
      
      Class loaderClass = (Class) Reflect.getStaticFieldValue(type, LOADER_ANNOTATION);
      if (loaderClass != null)
      {
         try
         {
            loader = (Loader) loaderClass.newInstance();
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }

      this.decryptKeyGetterMethod = getDecryptKeyGetterMethod(type);
      this.encryptKeyGetterMethod = getEncryptKeyGetterMethod(type);

      if (trace.isDebugEnabled())
      {
         trace.debug("Mapping type '" + type.getName() + "' to table '" + tableName
               + "'.");
      }

      inspectFields(type);

      // initializing the PK

      this.pkFields = getPKFields(type);
      
      if (trace.isDebugEnabled())
      {
         if (1 == pkFields.length)
         {
            trace.debug("Using field <" + pkFields[0].getName() + "> as primary key.");
         }
         else
         {
            trace.debug("Using fields <"
                  + StringUtils.join(new TransformingIterator<Field, String>(Arrays.asList(pkFields)
                        .iterator(), new Functor<Field, String>()
                  {
                     public String execute(Field source)
                     {
                        return source.getName();
                     }
                  }), ", ") + "> as primary key.");
         }
      }

      this.pkSequence = (String) readAnnotation(type, "", PK_SEQUENCE_ANNOTATION);
   }

   public boolean isTryDeferredInsert()
   {
      return tryDeferredInsert;
   }

   /**
    * Retrieves the field column index.
    */
   public int getColumnIndex(String fieldName)
   {
      Assert.isNotNull(fieldName);

      for (int n = 0; n < persistentFields.size(); ++n)
      {
         FieldDescriptor field = (FieldDescriptor) persistentFields.get(n);
         if (field.getField().getName().equals(fieldName))
         {
            return n;
         }
      }

      throw new InternalException("Cannot lookup column index for field '"
            + fieldName + "'.");
   }

   /**
    * Retrieves the field column index.
    */
   public int getFieldColumnIndex(Field rhsField)
   {
      Assert.isNotNull(rhsField);

      for (int n = 0; n < persistentFields.size(); ++n)
      {
         FieldDescriptor field = (FieldDescriptor) persistentFields.get(n);
         if (field.getField().equals(rhsField))
         {
            return n;
         }
      }

      throw new InternalException("Cannot lookup column index for field '"
            + rhsField.getName() + "'.");
   }

   /**
    *
    */
   private void inspectFields(Class type)
   {
      // Inspect the fields of the superclass

      if (type.getSuperclass() != null)
      {
         inspectFields(type.getSuperclass());
      }

      trace.debug("Inspecting class " + type.getName());

      // Inspect the fields of the class itself

      Field[] fields = type.getDeclaredFields();

      for (int i = 0; i < fields.length; ++i)
      {
         final Field field = fields[i];
         field.setAccessible(true);

         if (Modifier.isTransient(field.getModifiers()))
         {
            continue;
         }

         if (Modifier.isStatic(field.getModifiers()))
         {
            if (field.getName().endsWith(IDX_ANNOTATION_SUFFIX))
            {
               if (!field.getType().equals(String[].class))
               {
                  throw new InternalException("Index annotation '" + field.getName()
                        + "' is not of type String[].");
               }

               // is it even a unique index?

               String decoratedName = field.getName();
               String annotationSuffix = IDX_ANNOTATION_SUFFIX;
               boolean isUnique = false;
               if (decoratedName.endsWith(UNIQUE_IDX_ANNOTATION_SUFFIX))
               {
                  annotationSuffix = UNIQUE_IDX_ANNOTATION_SUFFIX;
                  isUnique = true;
               }

               try
               {
                  String name = decoratedName.substring(0, decoratedName.indexOf(
                        annotationSuffix));

                  indexes.add(new IndexDescriptor(name, (String[]) field.get(null),
                        isUnique));
               }
               catch (Exception x)
               {
                  throw new InternalException(
                        "Cannot retrieve index annotation from field '"
                        + field.getName() + "'. ", x);
               }
            }
            else if (field.getName().endsWith(USE_LITERALS_SUFFIX))
            {
               if (!field.getType().equals(boolean.class))
               {
                  throw new InternalException("Literals annotation '" + field.getName()
                        + "' is not of type boolean.");
               }

               String decoratedName = field.getName();
               try
               {
                  String name = decoratedName.substring(0, decoratedName.indexOf(
                        USE_LITERALS_SUFFIX));
                  if(field.getBoolean(Boolean.FALSE))
                  {
                     literalFields.add(name);
                  }
               }
               catch (Exception x)
               {
                  throw new InternalException(
                        "Cannot retrieve literals annotation from field '"
                        + field.getName() + "'. ", x);
               }
            }
         }
         else if (canBeMappedToColumnType(field.getType()))
         {
            Integer columnLength = getColumnLength(type, field);
            String _wrapFunction = (String) readAnnotation(type, field.getName(),
                  "_ENCRYPT_FUNCTION");
            String _unwrapFunction = (String) readAnnotation(type, field.getName(),
                  "_DECRYPT_FUNCTION");
            
            Boolean secret = (Boolean) readAnnotation(type, field.getName(),
                  SECRET_ANNOTATION);

            field.setAccessible(true);

            if (columnLength == null)
            {
               columnLength = new Integer(0);
            }
            persistentFields.add(new FieldDescriptor(field, columnLength.intValue(), 
                  _wrapFunction, _unwrapFunction, Boolean.TRUE.equals(secret)));
         }
         else if (Persistent.class.isAssignableFrom(field.getType()))
         {
            field.setAccessible(true);

            Class targetType = field.getType();
            Field[] pkFields = getPKFields(targetType);
            
            Assert.condition(1 == pkFields.length, "Linked target type " + targetType
                  + " has more than one PK field.");
            
            Integer fkFieldLength = getColumnLength(targetType, pkFields[0]); 
            if (fkFieldLength == null)
            {
               fkFieldLength = new Integer(0);
            }
            String registrarName = (String) readAnnotation(type, field.getName(), "_REGISTRAR");
            Method registrar = null;
            if (registrarName != null)
            {
               registrar = Reflect.getSetterMethod(targetType, registrarName, null);
               if (registrar == null)
               {
                  throw new InternalException("Registrar method '" + registrarName
                        + "' not found on '" + targetType + "'.");
               }
            }
            
            String isMandatory = (String) readAnnotation(type, field.getName(),
                  "_MANDATORY");

            String isEagerFetchable = (String) readAnnotation(type, field.getName(),
                  "_EAGER_FETCH");

            LinkDescriptor link = new LinkDescriptor(field, fkFieldLength.intValue(),
                  pkFields[0], targetType, registrar, StringUtils.getBoolean(isMandatory,
                        true), StringUtils.getBoolean(isEagerFetchable, false));
            links.add(link);
            if (null != registrar)
            {
               if (parents.isEmpty())
               {
                  this.parents = CollectionUtils.newArrayList();
               }
               
               parents.add(link);
            }
         }
         else if (PersistentVector.class.isAssignableFrom(field.getType()))
         {
            // Retrieve table name

            String vectorTableName = (String) readAnnotation(type, field.getName(),
                  "_TABLE_NAME");

            if (vectorTableName == null)
            {
               throw new InternalException("Mandatory annotation " + field.getName()
                     + "_TABLE_NAME not specified.");
            }

            // Retrieve type

            String vectorClassName = (String) readAnnotation(type, field.getName(), "_CLASS");

            if (vectorClassName == null)
            {
               throw new InternalException("Mandatory annotation " + field.getName()
                     + "_CLASS not specified.");
            }

            Class vectorClass = null;

            try
            {
               vectorClass = Reflect.getClassFromClassName(vectorClassName);
            }
            catch (Exception x)
            {
               throw new InternalException("Cannot lookup class '" + vectorClassName
                     + "' of persistent vector '" + field.getName() + "'.", x);
            }

            // Retrieve other role

            String vectorOtherRole = (String) readAnnotation(type, field.getName(),
                  "_OTHER_ROLE");

            if (vectorOtherRole == null)
            {
               throw new InternalException("Mandatory annotation " + field.getName()
                     + "_OTHER_ROLE not specified.");
            }

            field.setAccessible(true);

            persistentVectors.add(new PersistentVectorDescriptor(
                  field, vectorClass, vectorOtherRole));
         }
         else if (PersistenceController.class.isAssignableFrom(field.getType()))
         {
            // We ignore PersistenceController objects
         }
         else
         {
            throw new InternalException("Non-primitive, non-static type '"
                  + field.getType().getName() + "' cannot be mapped.");
         }
      }
      
      if ( !parents.isEmpty())
      {
         this.parents = Collections.unmodifiableList(parents);
      }
   }

   /**
    * Returns the table name which is associated to the specific type described by this 
    * TypeDescriptor instance.
    * 
    * @return The instance data table name.
    */
   public String getTableName()
   {
      return tableName;
   }

   public String getTableAlias()
   {
      return StringUtils.isEmpty(defaultAlias) ? null : defaultAlias;
   }

   /**
    * Returns the table name used for locking which is associated to the specific type
    * described by this TypeDescriptor instance. This name can be the same as the one
    * returned by {@link #getTableName()}. In case that special handling is necessary
    * when both names are distinct the method {@link #isDistinctLockTableName()} should
    * be called.
    * 
    * @return The locking table name.
    */
   public String getLockTableName()
   {
      return lockTableName;
   }
   
   /**
    * 
    * @return The index name for the corresponding locking table.
    */
   public String getLockIndexName()
   {
      return lockIndexName;
   }
   
   public ITableDescriptor getLockTableDescriptor()
   {
      return isDistinctLockTableName() ? tdLockTable : this;
   }

   /**
    * This method returns true in case that the locking table name differs from
    * the data table name.
    *   
    * @return true when {@link #getLockTableName()} not equal to {@link #getTableName()}
    */
   public boolean isDistinctLockTableName()
   {
      return !StringUtils.isEmpty(getLockTableName())
            && !getLockTableName().equals(getTableName());
   }

   public Class getType()
   {
      return type;
   }

   /**
    *
    */
   public int getIndexCount()
   {
      return indexes.size();
   }

   /**
    * Obtains, whether a sequence should be created for this class an
    * single-columns are created from it.
    */
   public boolean requiresPKCreation()
   {
      return (null != pkSequence) && (null != pkFields) && (1 <= pkFields.length);
   }

   /**
    *
    */
   public String getEncryptKey()
   {
      try
      {
         return (String) encryptKeyGetterMethod.invoke(null);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed to invoke getter method for encryption key.", x);
      }
   }

   /**
    *
    */
   public String getDecryptKey()
   {
      try
      {
         return (String) decryptKeyGetterMethod.invoke(null);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed to invoke getter method for decryption key.", x);
      }
   }

   /**
    *
    */
   public int getLinkIndex(String linkName)
   {
      int n;

      for (n = 0; n < links.size(); ++n)
      {
         LinkDescriptor descriptor = (LinkDescriptor) links.get(n);
         if (descriptor.getField().getName().equals(linkName))
         {
            return n;
         }
      }

      throw new InternalException("Unknown link name '" + linkName + "'.");
   }

   public Class getVectorType(String vectorName)
   {
      PersistentVectorDescriptor descriptor = getPersistentVector(vectorName);
      if (descriptor == null)
      {
         throw new InternalException("Unknown vector name '" + vectorName + "'.");
      }
      return descriptor.getType();
   }

   public boolean hasPkSequence()
   {
      return pkSequence != null;
   }
   
   public boolean hasField(String fieldName)
   {
      for (Iterator<FieldDescriptor> i = getPersistentFields().iterator(); i.hasNext();)
      {
         FieldDescriptor descriptor = (FieldDescriptor) i.next();
         if (descriptor.getField().getName().equalsIgnoreCase(fieldName))
         {
            return true;
         }
      }

      for (Iterator<LinkDescriptor> i = getLinks().iterator(); i.hasNext();)
      {
         LinkDescriptor descriptor = i.next();
         if (descriptor.getField().getName().equalsIgnoreCase(fieldName))
         {
            return true;
         }
      }

      return false;
   }

   public String getPkSequence()
   {
      return pkSequence;
   }

   public Loader getLoader()
   {
      return loader;
   }

   public List<FieldDescriptor> getPersistentFields()
   {
      return persistentFields;
   }

   public List<LinkDescriptor> getLinks()
   {
      return links;
   }

   public IndexDescriptor getIndexDescriptor(int n)
   {
      return (IndexDescriptor) indexes.get(n);
   }

   /**
    * @deprecated Callers must support multiple PK fields to be safe.
    */
   public Field getPkField()
   {
      return pkFields[0];
   }

   public boolean isPkField(Field field)
   {
      boolean result = false;
      for (int i = 0; i < pkFields.length; i++ )
      {
         if (pkFields[i].equals(field))
         {
            result = true;
            break;
         }
      }
      return result;
   }

   public Field[] getPkFields()
   {
      return pkFields;
   }

   public List<PersistentVectorDescriptor> getPersistentVectors()
   {
      return persistentVectors;
   }

   /**
    * Returns the PK of a PersistenceController in a format suitable as a map key.
    * Identities are guaranteed to be unique per given PK. 
    */
   public Object getIdentityKey(Persistent persistent)
   {
      if (persistent == null)
      {
         return null;
      }
      try
      {
         Field[] pkFields = getPkFields();
         if ((1 == pkFields.length)
               && (Long.class.isAssignableFrom(pkFields[0].getType())
                     || Long.TYPE.isAssignableFrom(pkFields[0].getType())
                     || IdentifiablePersistent.class.isAssignableFrom(pkFields[0].getType())))
         {
            Object pkValue = pkFields[0].get(persistent);
            
            if (pkValue instanceof Long)
            {
               return pkValue;
            }
            else if (pkValue instanceof Persistent)
            {
               final Persistent resolvedLink = (Persistent) pkValue;
               return ((DefaultPersistenceController) resolvedLink.getPersistenceController()).getTypeDescriptor()
                     .getIdentityKey(resolvedLink);
            }
            else
            {
               throw new InternalException("Unsupported PK value: " + pkValue);
            }
            
         }
         else
         {
            //Object[] result = new Object[pkFields.length];
            CompositeKey result = createKey(pkFields.length);
            for (int i = 0; i < pkFields.length; i++ )
            {
               Object pkValue = pkFields[i].get(persistent);
               if (pkValue instanceof Persistent)
               {
                  final Persistent resolvedLink = (Persistent) pkValue;
                  pkValue = ((DefaultPersistenceController) resolvedLink.getPersistenceController()).getTypeDescriptor()
                        .getIdentityKey(resolvedLink);
               }

               //result[i] = pkValue;
               result.setKey(i, pkValue);
            }

            return result;
         }
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Returns the PK of a PersistenceController in a format suitable as a map key.
    * Identities are guaranteed to be unique per given PK. 
    */
   public Object getIdentityKey(Object pkValue)
   {
      if (null == pkValue)
      {
         return null;
      }
      try
      {
         Field[] pkFields = getPkFields();
         if ((1 == pkFields.length)
               && (Long.class.isAssignableFrom(pkFields[0].getType())
                     || Long.TYPE.isAssignableFrom(pkFields[0].getType())
                     || IdentifiablePersistent.class.isAssignableFrom(pkFields[0].getType())))
         {
            if (pkValue instanceof Long)
            {
               return pkValue;
            }
            else
            {
               return ((Object[]) pkValue)[0];
            }
         }
         else if (pkValue instanceof Object[])
         {
            return createKey((Object[]) pkValue);
         }
         else
         {
            throw new InternalException("Unsupported primary key for type " + getType());
         }
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Returns the OID of a PersistenceController as a concatenation of its table name
    * and its stringified primary key values.
    */
   public String getFullOIDFromPK(Object pk)
   {
      if (pk == null)
      {
         return null;
      }

      StringBuffer buffer = new StringBuffer();

      buffer.append(tableName);

      buffer.append("'");
      buffer.append(pk);

      return buffer.toString();
   }

   public LinkDescriptor getLink(String name)
   {
      for (int n = 0; n < links.size(); ++n)
      {
         LinkDescriptor link = (LinkDescriptor) links.get(n);
         if (link.getField().getName().equals(name))
         {
            return link;
         }
      }
       return null;
   }

   public PersistentVectorDescriptor getPersistentVector(String name)
   {
      for (int n = 0; n < persistentVectors.size(); ++n)
      {
         PersistentVectorDescriptor persistentVector = (PersistentVectorDescriptor) persistentVectors.get(n);
         if (persistentVector.getField().getName().equals(name))
         {
            return persistentVector;
         }
      }
       return null;
   }

   public LinkDescriptor getLink(int linkIdx)
   {
      if ((0 <= linkIdx) && (linkIdx < links.size()))
      {
         return (LinkDescriptor) links.get(linkIdx);
      }
      else
      {
         return null;
      }
   }

   public PersistentVectorDescriptor getPersistentVector(int vectorIdx)
   {
      if ((0 <= vectorIdx) && (vectorIdx < persistentVectors.size()))
      {
         return (PersistentVectorDescriptor) persistentVectors.get(vectorIdx);
      }
      else
      {
         return null;
      }
   }

   public int getLinkIdx(String name)
   {
      for (int n = 0; n < links.size(); ++n)
      {
         LinkDescriptor link = (LinkDescriptor) links.get(n);
         if (link.getField().getName().equals(name))
         {
            return n;
         }
      }
      return -1;
   }

   public int getPersistentVectorIdx(String name)
   {
      for (int n = 0; n < persistentVectors.size(); ++n)
      {
         PersistentVectorDescriptor persistentVector = (PersistentVectorDescriptor) persistentVectors.get(n);
         if (persistentVector.getField().getName().equals(name))
         {
            return n;
         }
      }
      return -1;
   }

   public List<LinkDescriptor> getParents()
   {
      return parents;
   }

   public FieldDescriptor getPersistentField(int index)
   {
      return (FieldDescriptor) persistentFields.get(index);
   }
   
   public FieldDescriptor getPersistentField(Field field)
   {
      int index = getFieldColumnIndex(field);
      return (FieldDescriptor) persistentFields.get(index);
   }

   public FieldDescriptor getPersistentField(String fieldName)
   {
      int index = getColumnIndex(fieldName);
      return (FieldDescriptor) persistentFields.get(index);
   }

   /**
    * Read column length from annotation. This value will be taken as default value
    * if no property <code>Carnot.Db.ColumnLength.&lt;table-name&gt;.&lt;column-name&gt;</code>
    * is set. Otherwise the properties value will be used.
    * <br/><br/>
    * Be aware: Overwriting column length by property is restricted to subclasses of {@link AbstractProperty}.
    *  
    * @param type 
    * @param field
    * @return
    */
   private Integer getColumnLength(Class type, final Field field)
   {
      String fieldName = field.getName();
      Integer columnLength = (Integer) readAnnotation(type, fieldName,
            COLUMN_LENGTH_ANNOTATION_SUFFIX);

      // At the moment this is restricted to AbstractProperty sub classes.
      // TODO: Remove this dependency class AbstractProperty.
      if (AbstractProperty.class.isAssignableFrom(type))
      {
         String propName = KernelTweakingProperties.STRING_COLUMN_LENGTH_PREFIX
               + tableName + "." + fieldName;

         if (null != columnLength)
         {
            columnLength = new Integer(Parameters.instance().getInteger(propName,
                  columnLength.intValue()));
         }
         else
         {
            int integer = Parameters.instance().getInteger(propName, Integer.MIN_VALUE);
            if (Integer.MIN_VALUE != integer)
            {
               columnLength = new Integer(integer);
            }
         }
      }

      return columnLength;
   }

   public boolean isLiteralField(String fieldName)
   {
      return literalFields.contains(fieldName);
   }
   
   static CompositeKey createKey(int length)
   {
      return length == 2 ? new CompositeKey2() : new CompositeKeyN(length);
   }

   static CompositeKey createKey(Object[] pkValue)
   {
      CompositeKey composite = createKey(pkValue.length);
      for (int i = 0; i < pkValue.length; i++)
      {
         composite.setKey(i, pkValue[i]);
      }
      return composite;
   }

   static interface CompositeKey extends Comparable
   {
      void setKey(int i, Object pkValue);
   }
   
   private static class CompositeKeyN implements CompositeKey
   {
      private Comparable[] keys;

      private CompositeKeyN(int length)
      {
         keys = new Comparable[length];
      }

      public void setKey(int i, Object pkValue)
      {
         keys[i] = pkValue instanceof Comparable ? (Comparable) pkValue : String.valueOf(pkValue);
      }

      @Override
      public int hashCode()
      {
         return Arrays.hashCode(keys) + 31;
      }

      @Override
      public boolean equals(Object obj)
      {
         return compareTo(obj) == 0;
      }

      public int compareTo(Object o)
      {
         int result = 1;
         if (o instanceof CompositeKeyN)
         {
            Comparable[] other = ((CompositeKeyN) o).keys;
            result = keys.length - other.length;
            if (result == 0)
            {
               for (int i = 0; i < keys.length; i++)
               {
                  result = keys[i].compareTo(other[i]);
                  if (result != 0)
                  {
                     break;
                  }
               }
            }
         }
         return result;
      }
   }

   private static class CompositeKey2 implements CompositeKey
   {
      private Comparable key0;
      private Comparable key1;
      
      private CompositeKey2()
      {
      }
      
      public void setKey(int i, Object pkValue)
      {
         Comparable c = pkValue instanceof Comparable ? (Comparable) pkValue : String.valueOf(pkValue);
         if (i == 0)
         {
            key0 = c;
         }
         else
         {
            key1 = c;
         }
      }

      @Override
      public int hashCode()
      {
         return 31 * (31 + key0.hashCode()) + key1.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         return compareTo(obj) == 0;
      }

      public int compareTo(Object o)
      {
         int result = -1;
         if (o instanceof CompositeKey2)
         {
            CompositeKey2 other = (CompositeKey2) o;
            result = key0.compareTo(other.key0);
            if (result == 0)
            {
               result = key1.compareTo(other.key1);
            }
         }
         return result;
      }
   }
}