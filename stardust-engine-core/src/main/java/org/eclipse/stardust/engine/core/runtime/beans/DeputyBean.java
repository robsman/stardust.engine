/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.dto.DeputyDetails;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Internal class to help with creation and parsing of the deputy information.
 * 
 * The element names are intentionally 1 character long and no namespace and XML declaration
 * to keep the serialized version as small as possible.
 * 
 * Top element ("d") must have no attributes and the user element ("u") must be the first child,
 * i.e. the serialized form must have the format "&lt;d&gt;&lt;u&gt;user-oid&lt;/u&gt;...other
 * children...&lt;/d&gt;".
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
@XmlRootElement(name = "d")
@XmlType(propOrder = { "user", "from", "to", "grants" })
public class DeputyBean
{
   public static class GrantBean
   {
      @XmlElement(name = "p")
      public long participant;

      @XmlElement(name = "d")
      public long department;

      public GrantBean()
      {
         super();
      }

      public GrantBean(long participant, long department)
      {
         super();
         this.participant = participant;
         this.department = department;
      }
   }

   private static class DateAdapter extends XmlAdapter<Long, Date>
   {
      @Override
      public Date unmarshal(Long v) throws Exception
      {
         return v == null ? null : new Date(v);
      }

      @Override
      public Long marshal(Date v) throws Exception
      {
         return v == null ? null : v.getTime();
      }
   }

   @XmlElement(name = "u")
   public long user;

   @XmlElement(name = "f")
   @XmlJavaTypeAdapter(DateAdapter.class)
   public Date from;

   @XmlElement(name = "t")
   @XmlJavaTypeAdapter(DateAdapter.class)
   public Date to;

   @XmlElement(name = "g")
   public List<GrantBean> grants;

   // required default constructor
   DeputyBean()
   {
   }

   DeputyBean(long oid, Date fromDate, Date toDate, Set<ModelParticipantInfo> participants)
   {
      user = oid;
      from = fromDate;
      to = toDate;
      grants = CollectionUtils.newArrayList(participants.size());

      ModelManager current = ModelManagerFactory.getCurrent();
      for (ModelParticipantInfo participant : participants)
      {
         String qualifiedId = participant instanceof QualifiedModelParticipantInfo ? ((QualifiedModelParticipantInfo) participant)
               .getQualifiedId() : participant.getId();

         Iterator<IModelParticipant> participantsForID = current
               .getParticipantsForID(qualifiedId);
         if (participantsForID.hasNext())
         {
            IModelParticipant modelParticipant = participantsForID.next();
            IDepartment department = DepartmentUtils.getDepartment(participant
                  .getDepartment());
            this.grants.add(new GrantBean(current.getRuntimeOid(modelParticipant),
                  department == null ? 0 : department.getOID()));
         }
      }
   }

   public Deputy createDeputyDetails(UserInfo deputyUser)
   {
      UserInfo userInfo = DetailsFactory.create(UserBean.findByOid(user));
      Set<ModelParticipantInfo> grants = CollectionUtils.newHashSet();
      if (this.grants != null)
      {
         for (GrantBean grantBean : this.grants)
         {
            ModelParticipantInfo participantInfo = (ModelParticipantInfo) DepartmentUtils
                  .getParticipantInfo(PerformerType.ModelParticipant,
                        grantBean.participant, grantBean.department,
                        PredefinedConstants.ANY_MODEL);
            grants.add(participantInfo);
         }
      }
      return new DeputyDetails(userInfo, deputyUser, from, to, grants);
   }

   public String toString()
   {
      try
      {
         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FRAGMENT, true);
         StringWriter writer = new StringWriter();
         m.marshal(this, writer);
         return writer.toString();
      }
      catch (JAXBException e)
      {
         throw new InternalException("Unable to serialize deputy info.", e);
      }
   }

   static DeputyBean fromString(String value)
   {
      try
      {
         Unmarshaller u = context.createUnmarshaller();
         return (DeputyBean) u.unmarshal(new StringReader(value));
      }
      catch (JAXBException e)
      {
         throw new InternalException("Unable to parse deputy info from '" + value + "'.",
               e);
      }
   }

   private static final JAXBContext context = initContext();

   private static JAXBContext initContext()
   {
      try
      {
         return JAXBContext.newInstance(DeputyBean.class);
      }
      catch (JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   public boolean isActive()
   {
      return isActive(TimestampProviderUtils.getTimeStamp());
   }

   public boolean isActive(Date now)
   {
      return (from == null || now.compareTo(from) >= 0)
            && (to == null || now.compareTo(to) <= 0);
   }

   public boolean isExpired()
   {
      return isExpired(TimestampProviderUtils.getTimeStamp());
   }

   public boolean isExpired(Date now)
   {
      return to != null && now.after(to);
   }

   /*public static void main(String[] args)
   {
      System.out.println(new DeputyBean(12345, TimestampProviderUtils.getTimeStamp(), null));
   }*/
}
