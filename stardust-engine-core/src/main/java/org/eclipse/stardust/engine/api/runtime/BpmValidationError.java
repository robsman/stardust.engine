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
package org.eclipse.stardust.engine.api.runtime;

import java.text.MessageFormat;

import org.eclipse.stardust.common.error.ErrorCase;


/**
 * @author pielmann
 * @version $Revision: $
 */
public class BpmValidationError extends ErrorCase
{

   private static final long serialVersionUID = 1L;


   //Process Definiton related
   public static final Args0 PD_NO_START_ACTIVITY = newArgs0("PD01001", BpmRuntimeErrorMessages.getString("PD01001"));
   public static final Args1 PD_DUPLICATE_ID = newArgs1("PD01002", BpmRuntimeErrorMessages.getString("PD01002"));
   public static final Args2 PD_ID_EXCEEDS_LENGTH = newArgs2("PD01003", BpmRuntimeErrorMessages.getString("PD01003"));
   public static final Args1 PD_FORMAL_PARAMETER_NO_DATA_SET = newArgs1("PD01004", BpmRuntimeErrorMessages.getString("PD01004"));
   public static final Args2 PD_DUPLICATE_TRANSITION_SAME_SOURCE_OR_TARGET = newArgs2("PD01005", BpmRuntimeErrorMessages.getString("PD01005"));
   public static final Args2 PD_MULTIPLE_START_ACTIVYTIES = newArgs2("PD01006", BpmRuntimeErrorMessages.getString("PD01006"));
   public static final Args1 PD_NO_ACTIVITIES_DEFINED = newArgs1("PD01007", BpmRuntimeErrorMessages.getString("PD01007"));
   public static final Args1 PD_PROCESS_INTERFACE_NOT_RESOLVED = newArgs1("PD01008", BpmRuntimeErrorMessages.getString("PD01008"));

   //ApplicationType related
   public static final Args1 APP_TYPE_NO_LONGER_SUPPORTED = newArgs1("APP01001", BpmRuntimeErrorMessages.getString("APP01001"));

   //Actions related
   public static final Args0 ACT_NO_DATA_DEFINED = newArgs0("ACT01001", BpmRuntimeErrorMessages.getString("ACT01001"));
   public static final Args0 ACT_NO_ACCESS_POINT_DEFINED = newArgs0("ACT01002", BpmRuntimeErrorMessages.getString("ACT01002"));
   public static final Args0 ACT_NO_PROCESS_SELECTED = newArgs0("ACT01003", BpmRuntimeErrorMessages.getString("ACT01003"));


   private static final Object[] NONE = {};

   private final String defaultMessage;

   private final Object[] args;

   private BpmValidationError(String id)
   {
      this(id, null);
   }

   private BpmValidationError(String id, String defaultMessage)
   {
      this(id, defaultMessage, NONE);
   }

   private BpmValidationError(String code, String defaultMessage, Object msgArgs[])
   {
      super(code);

      this.defaultMessage = defaultMessage;
      this.args = msgArgs;
   }

   public String getDefaultMessage()
   {
      return defaultMessage;
   }

   public Object[] getMessageArgs()
   {
      return args;
   }

   public String toString()
   {
      return getId() + " - " + MessageFormat.format(getDefaultMessage(), args); //$NON-NLS-1$
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args0 newArgs0(String errorCode)
   {
      return new Args0(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args0 newArgs0(String errorCode, String defaultMessage)
   {
      return new Args0(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args1 newArgs1(String errorCode)
   {
      return new Args1(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args1 newArgs1(String errorCode, String defaultMessage)
   {
      return new Args1(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args2 newArgs2(String errorCode)
   {
      return new Args2(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args2 newArgs2(String errorCode, String defaultMessage)
   {
      return new Args2(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args3 newArgs3(String errorCode, String defaultMessage)
   {
      return new Args3(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args4 newArgs4(String errorCode, String defaultMessage)
   {
      return new Args4(errorCode, defaultMessage);
   }

   public static class Args0 extends AbstractErrorFactory
   {
      private Args0(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise()
      {
         return buildError(NONE);
      }
   }

   public static class Args1 extends AbstractErrorFactory
   {
      private Args1(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg)
      {
         return buildError(new Object[] {arg});
      }

      public BpmValidationError raise(long arg)
      {
         return buildError(new Object[] {new Long(arg)});
      }
   }

   public static class Args2 extends AbstractErrorFactory
   {
      private Args2(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2)
      {
         return buildError(new Object[] {arg1, arg2});
      }

      public BpmValidationError raise(long arg1, long arg2)
      {
         return buildError(new Object[] {new Long(arg1), new Long(arg2)});
      }
   }

   public static class Args3 extends AbstractErrorFactory
   {
      private Args3(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2, Object arg3)
      {
         return buildError(new Object[] { arg1, arg2, arg3 });
      }

      public BpmValidationError raise(long arg1, long arg2, long arg3)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3) });
      }
   }

   public static class Args4 extends AbstractErrorFactory
   {
      private Args4(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2, Object arg3, Object arg4)
      {
         return buildError(new Object[] { arg1, arg2, arg3, arg4 });
      }

      public BpmValidationError raise(long arg1, long arg2, long arg3, long arg4)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3), new Long(arg4) });
      }
   }

   public static Args newArgs(String errorCode)
   {
      return new Args(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
   }

   public static class Args extends AbstractErrorFactory
   {
      private Args(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object ... arg)
      {
         return buildError(arg);
      }
   }

   static abstract class AbstractErrorFactory
   {
      private final String errorCode;

      private final String defaultMessage;

      protected AbstractErrorFactory(String errorCode, String defaultMessage)
      {
         this.errorCode = errorCode;
         this.defaultMessage = defaultMessage;
      }

      protected BpmValidationError buildError(Object[] args)
      {
         return new BpmValidationError(errorCode, defaultMessage, args);
      }
   }

   static BpmValidationError createError(String id, String defaultMessage, Object arg0)
   {
      return new BpmValidationError(id, defaultMessage, new Object[] {arg0});
   }

}
