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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;


/**
 * @author mgille
 * @version $Revision$
 */
public class OrganizationBean extends ModelParticipantBean
      implements IOrganization
{
   private static final long serialVersionUID = 1L;
   
   private List participants = null;

   private IRole teamLead = null;

   OrganizationBean()
   {
   }

   public OrganizationBean(String id, String name, String description)
   {
      super(id, name, description);
   }

   public String toString()
   {
      return "Organization: " + getName();
   }

   public IRole getTeamLead()
   {
      return teamLead;
   }

   public void setTeamLead(IRole teamLead)
   {
      this.teamLead = teamLead;
      teamLead.addToTeams(this);
   }

   /**
    * @param participant Participant to be added.
    */
   public void addToParticipants(IModelParticipant participant)
   {
      if (participants == null)
      {
         participants = CollectionUtils.newList();
      }
      participants.add(participant);
      participant.addToOrganizations(this);
   }

   public Iterator getAllParticipants()
   {
      final Set teamLeads = (null != getTeamLead())
            ? Collections.singleton(getTeamLead())
            : Collections.EMPTY_SET;
      Collection participants = this.participants == null
            ? Collections.emptyList()
            : this.participants;
      return new SplicingIterator(teamLeads.iterator(), participants.iterator());
   }

   public int getCardinality()
   {
      return Unknown.INT;
   }

   public void addToSubOrganizations(IOrganization organization)
   {
      if (organization == this)
      {
         throw new PublicException("An organization definition \"" + getId()
               + "\" cannot be its own suborganization/superorganization.");
      }

      if (isDirectOrIndirectSubOrganizationOf(organization))
      {
         throw new PublicException("The organization \"" + getId()
               + "\" is already a direct or indirect suborganization of organization \""
               + organization.getId() + "\". Cyclic references are illegal.");
      }

      addToParticipants(organization);
   }

   public Iterator getSubOrganizations()
   {
      return new FilteringIterator(getAllParticipants(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return o instanceof IOrganization;
         }
      });
   }

   /**
    * Checks, wether this organization is a direct or indirect suborganization of the
    * organization <tt>testOrganization</tt>.
    */
   public boolean isDirectOrIndirectSubOrganizationOf(IOrganization testOrganization)
   {
      for (Iterator i = testOrganization.getSubOrganizations(); i.hasNext();)
      {
         IOrganization subOrganization = (IOrganization) i.next();

         if (subOrganization.equals(this)
               || isDirectOrIndirectSubOrganizationOf(subOrganization))
         {
            return true;
         }
      }
      return false;
   }

   public IModelParticipant findParticipant(String id)
   {
      if ((null != getTeamLead()) && CompareHelper.areEqual(id, getTeamLead().getId()))
      {
         return getTeamLead();
      }
      else
      {
         return (IModelParticipant) ModelUtils.findById(participants, id);
      }
   }

   public boolean isAuthorized(IModelParticipant participant)
   {
      return isAuthorized(participant, ModelManagerFactory.getCurrent());
   }
   
   private boolean isAuthorized(IModelParticipant participant, ModelManager manager)
   {
      if (participant == null)
      {
         return false;
      }

      if (participant == this || participant instanceof IOrganization
            && manager.getRuntimeOid(participant) == manager.getRuntimeOid(this))
      {
         return true;
      }
      
      if ((null != getTeamLead()) && getTeamLead().isAuthorized(participant))
      {
         return true;
      }

      for (Iterator i = getAllParticipants(); i.hasNext();)
      {
         if (((IModelParticipant) i.next()).isAuthorized(participant))
         {
            return true;
         }
      }

      return false;
   }

   // @todo (france, ub): invert dependency: This introduces a dependency to the runtime package
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
}
