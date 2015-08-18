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

import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;

/**
 * TODO:
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ModelParticipantInfo extends ParticipantInfo
{
   /**
    * Public constant for the Administrator role.
    */
   public static final ModelParticipantInfo ADMINISTRATOR = new ModelParticipantInfo()
   {
      private static final long serialVersionUID = 1L;
      
      private static final String TO_STRING = "ModelParticipant: " + PredefinedConstants.ADMINISTRATOR_ROLE;

      public boolean definesDepartmentScope()
      {
         return false;
      }

      public DepartmentInfo getDepartment()
      {
         return null;
      }

      public long getRuntimeElementOID()
      {
         return 0;
      }

      public boolean isDepartmentScoped()
      {
         return false;
      }

      public String getId()
      {
         return PredefinedConstants.ADMINISTRATOR_ROLE;
      }

      public String getName()
      {
         // TODO: (fh) i18n ?
         return PredefinedConstants.ADMINISTRATOR_ROLE;
      }

      @Override
      public String toString()
      {
         return TO_STRING;
      }

      public String getQualifiedId()
      {
         return getId();
      }
   };
   
   /**
    * Gets the runtime OID of the model element.
    * <p>
    * Contrary to the element OID, runtime element OIDs are guaranteed to be stable over
    * model versions for model elements of same type and identical fully qualified IDs.
    * </p>
    * 
    * <p>
    * The fully qualified ID of a model element consists of the concatenation of the fully
    * qualified element ID of its parent element, if existent, and the element ID.
    * </p>
    * 
    * @return the runtime model element OID
    * 
    * @see ModelElement#getElementOID()
    */
   long getRuntimeElementOID();
   
   /**
    * Returns true if model participant is modeled to support departments, either directly or inherited from the parent.
    *   
    * @return true if this model participant supports department scoping, otherwise false.
    */
   boolean isDepartmentScoped();

   /**
    * Returns true if the model participant is modeled to support creation of its own departments.
    *   
    * @return true if this model participant supports creation of departments, otherwise false.
    */
   boolean definesDepartmentScope();

   /**
    * Returns the department associated with this model participant or null if the participant has no department association.<br>
    * This method will return Department.DEFAULT instead of null if this object was obtained from
    * Department.DEFAULT.getScopedParticipant(ModelParticipant).
    * 
    * @return The department info.
    */
   DepartmentInfo getDepartment();
}