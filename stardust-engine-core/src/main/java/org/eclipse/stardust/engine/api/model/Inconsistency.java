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
package org.eclipse.stardust.engine.api.model;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.Nameable;
import org.eclipse.stardust.engine.core.runtime.ValidationErrorMessageProvider;


/**
 * The <code>Inconsistency</code> class provides information about a model inconsistency.
 * Inconsistencies are of two types: errors and warnings. When an error inconsistency is
 * issued, the model is unable to work (models with errors cannot be deployed). A warning
 * inconsistency implies that the specific workflow operation may fail.
 */
public class Inconsistency implements Serializable
{
   private static final long serialVersionUID = 2L;

   /**
    * Specifies a warning inconsistency.
    */
   public static final int WARNING = 0;

   /**
    * Specifies an error inconsistency.
    */
   public static final int ERROR = 1;

   private int severity;
   private String message;
   private int sourceElementOID;
   private String sourceElementId;
   private String sourceElementName;
   private ErrorCase error;

   /**
    * Constructs an Inconsistency.
    *
    * @param message inconsistency message.
    * @param severity the severity: WARNING or ERROR.
    * @deprecated
    */
   public Inconsistency(String message, int severity)
   {
      this(message, null, severity);
   }

   /**
    * Constructs an Inconsistency for a specific model element.
    *
    * @param message          inconsistency message.
    * @param sourceElementOID the OID of the inconsistent model element.
    * @param severity         the severity: WARNING or ERROR.
    * @deprecated
    */
   public Inconsistency(String message, int sourceElementOID, int severity)
   {
      this(message, null, severity);
      this.sourceElementOID = sourceElementOID;
   }

   /**
    * Constructs an Inconsistency for a specific model element.
    *
    * @param message          inconsistency message.
    * @param sourceElement    the inconsistent model element.
    * @param severity         the severity: WARNING or ERROR.
    */
   public Inconsistency(String message, ModelElement sourceElement, int severity)
   {
      this.message = message;
      this.severity = severity;
      if (sourceElement != null)
      {
         sourceElementOID = sourceElement.getElementOID();
         if (sourceElement instanceof Identifiable)
         {
            sourceElementId = ((Identifiable) sourceElement).getId();
         }
         if (sourceElement instanceof Nameable)
         {
            sourceElementName = ((Nameable) sourceElement).getName();
         }
      }
   }

   public Inconsistency(ErrorCase error, String message, ModelElement sourceElement,
         int severity)
   {
      this(message, sourceElement, severity);
      this.error = error;
   }

   public Inconsistency(ErrorCase error, ModelElement sourceElement, int severity)
   {
      this.severity = severity;
      if (sourceElement != null)
      {
         sourceElementOID = sourceElement.getElementOID();
         if (sourceElement instanceof Identifiable)
         {
            sourceElementId = ((Identifiable) sourceElement).getId();
         }
         if (sourceElement instanceof Nameable)
         {
            sourceElementName = ((Nameable) sourceElement).getName();
         }
      }
      this.error = error;
      if (error != null)
      {
         ValidationErrorMessageProvider provider = new ValidationErrorMessageProvider();
         message = provider.getErrorMessage(error, null, Locale.ENGLISH);
      }
   }

   public Inconsistency(ErrorCase error, int severity)
   {
      this(error, null, severity);
   }

   public Inconsistency(int severity, ModelElement sourceElement, String pattern, Object ... arguments)
   {
      this(MessageFormat.format(pattern, arguments), sourceElement, severity);
   }

   /**
    * Returns the element OID of the inconsistent model element (activity, role etc.).
    *
    * @return the element OID of the source.
    */
   public int getSourceElementOID()
   {
      return sourceElementOID;
   }

   /**
    * Returns the ID of the inconsistent model element (activity, role etc.).
    *
    * @return the ID of the source.
    */
   public String getSourceElementId()
   {
      return sourceElementId;
   }

   /**
    * Returns the Name of the inconsistent model element (activity, role etc.).
    *
    * @return the name of the source.
    */
   public String getSourceElementName()
   {
      return sourceElementName;
   }

   /**
    * Gets the severity of the inconsistency.
    *
    * @return the severity.
    */
   public int getSeverity()
   {
      return severity;
   }

   /**
    * Gets the message contained in the inconsistency.
    *
    * @return the inconsistency message.
    */
   public String getMessage()
   {
      return message;
   }

   /**
    * Gets the errorID contained in the inconsistency.
    *
    * @return the error id
    */
   public String getErrorID()
   {
      if (error != null)
      {
         return error.getId();
      }
      return null;
   }

   public ErrorCase getError()
   {
      return error;
   }

   public String toString()
   {
      return Long.toString(sourceElementOID) + " : " + message;
   }
}
