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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;

/**
 * @author mgille
 * @version $Revision$
 */
public class RoleBean extends ModelParticipantBean implements IRole
{
   private static final long serialVersionUID = 1L;

   private int cardinality = Unknown.INT;
   
   private List teams = null;

   RoleBean()
   {
   }

   public RoleBean(String id, String name, String description)
   {
      super(id, name, description);

      cardinality = Unknown.INT;
   }

   /**
    *
    */
   public String toString()
   {
      return "Role: " + getName();
   }

   /**
    * Gets the cardinality of this role.
    */
   public int getCardinality()
   {
      return cardinality;
   }

   /**
    * Sets the cardinality of this role.
    */
   public void setCardinality(int cardinality)
   {
      this.cardinality = cardinality;
   }
   
   public Iterator getAllTeams()
   {
      return teams == null ? Collections.emptyList().iterator() : teams.iterator();
   }

   public Iterator getAllClientOrganizations()
   {
      return super.getAllOrganizations();
   }

   public Iterator getAllOrganizations()
   {
      return new SplicingIterator(getAllTeams(), getAllClientOrganizations());
   }

   public IOrganization findOrganization(String id)
   {
      IOrganization result = (IOrganization) ModelUtils.findById(teams, id);
      
      return (null != result) ? result : super.findOrganization(id);
   }

   /**
    * Checks, wether the participant <code>participant</code> is equal to
    * this role.
    * <p/>
    * It is used to determine, whether this object is allowed to start processes
    * whose starting participant equals <tt>participant</tt>.
    */
   public boolean isAuthorized(IModelParticipant participant)
   {
      Assert.isNotNull(participant);
      return isAuthorized(participant, ModelManagerFactory.getCurrent());
   }
   
   private boolean isAuthorized(IModelParticipant participant, ModelManager manager)
   {
      return participant == this || manager.getRuntimeOid(participant) == manager.getRuntimeOid(this);
   }

   public boolean isAuthorized(IUser user)
   {
      if (user == null)
      {
         return false;
      }

      final ModelManager manager = ModelManagerFactory.getCurrent();
      return UserUtils.isAuthorized(user, new Predicate<IModelParticipant>()
      {
         @Override
         public boolean accept(IModelParticipant participant)
         {
            return isAuthorized(participant, manager);
         }
      });
   }

   public boolean isAuthorized(IUserGroup userGroup)
   {
      // TODO (sb): provide a decent implementation for authorization checking
      boolean returnValue = false;
      
      return returnValue;      
   }
   
   public Iterator getAllParticipants()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public void addToTeams(IOrganization org)
   {
      if (teams == null)
      {
         teams = CollectionUtils.newList();
      }
      teams.add(org);
   }
}