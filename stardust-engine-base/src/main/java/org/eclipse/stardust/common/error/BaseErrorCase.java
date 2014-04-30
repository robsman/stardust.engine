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
package org.eclipse.stardust.common.error;

import java.text.MessageFormat;


/**
 * @author sauer
 * @version $Revision: $
 */
public class BaseErrorCase extends ErrorCase
{
   private static final long serialVersionUID = 1L;

   public static final Args2 BASE_INCOMPATIBLE_TYPES = newArgs2("BASE01001", BaseErrorCaseMessages.getString("BASE01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER = newArgs2("BASE01002", BaseErrorCaseMessages.getString("BASE01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_TRUE_OR_FALSE = newArgs2("BASE01003", BaseErrorCaseMessages.getString("BASE01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_DOUBLE = newArgs2("BASE01004", BaseErrorCaseMessages.getString("BASE01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_VALID_DATE = newArgs2("BASE01005", BaseErrorCaseMessages.getString("BASE01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BASE_URL_CANNOT_BE_LOADED = newArgs1("BASE01006", BaseErrorCaseMessages.getString("BASE01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BASE_PASSWORD_FILE_COULD_NOT_BE_FOUND_AT_THE_SPECIFIED_LOCATION = newArgs0("BASE01007", BaseErrorCaseMessages.getString("BASE01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BASE_PASSWORD_FILE_COULD_NOT_BE_READ = newArgs0("BASE01008", BaseErrorCaseMessages.getString("BASE01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 BASE_DATE_VALUE_FOR_OPTION_IS_NOT_IN_CORRECT_FORMAT = newArgs3("BASE01010", BaseErrorCaseMessages.getString("BASE01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BASE_INVALID_NUMERIC_ARGUMENT = newArgs0("BASE01011", BaseErrorCaseMessages.getString("BASE01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BASE_ILLEGAL_OPTION_VALUE_IS_NOT_A_LONG_VALUE = newArgs2("BASE01012", BaseErrorCaseMessages.getString("BASE01012")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BASE_YOU_HAVE_NOT_SUPPLIED_THE_CORRECT_PARAMETER_IN_PROPERTIES = newArgs2("BASE01012", BaseErrorCaseMessages.getString("BASE01012")); //$NON-NLS-1$ //$NON-NLS-2$



   private static final Object[] NONE = {};

   private final String defaultMessage;

   private final Object[] args;

   private BaseErrorCase(String id)
   {
      this(id, null);
   }

   private BaseErrorCase(String id, String defaultMessage)
   {
      this(id, defaultMessage, NONE);
   }

   private BaseErrorCase(String code, String defaultMessage, Object msgArgs[])
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
      return new Args0(errorCode, BaseErrorCaseMessages.getString(errorCode));
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
      return new Args1(errorCode, BaseErrorCaseMessages.getString(errorCode));
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
      return new Args2(errorCode, BaseErrorCaseMessages.getString(errorCode));
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

      public BaseErrorCase raise()
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

      public BaseErrorCase raise(Object arg)
      {
         return buildError(new Object[] {arg});
      }

      public BaseErrorCase raise(long arg)
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

      public BaseErrorCase raise(Object arg1, Object arg2)
      {
         return buildError(new Object[] {arg1, arg2});
      }

      public BaseErrorCase raise(long arg1, long arg2)
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

      public BaseErrorCase raise(Object arg1, Object arg2, Object arg3)
      {
         return buildError(new Object[] { arg1, arg2, arg3 });
      }

      public BaseErrorCase raise(long arg1, long arg2, long arg3)
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

      public BaseErrorCase raise(Object arg1, Object arg2, Object arg3, Object arg4)
      {
         return buildError(new Object[] { arg1, arg2, arg3, arg4 });
      }

      public BaseErrorCase raise(long arg1, long arg2, long arg3, long arg4)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3), new Long(arg4) });
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

      protected BaseErrorCase buildError(Object[] args)
      {
         return new BaseErrorCase(errorCode, defaultMessage, args);
      }
   }

   static BaseErrorCase createError(String id, String defaultMessage, Object arg0)
   {
      return new BaseErrorCase(id, defaultMessage, new Object[] {arg0});
   }

}
