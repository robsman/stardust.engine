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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.dto.DeputyDetails;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.UserInfo;

@XmlRootElement(name="d")
@XmlType(propOrder={"user", "from", "to"})
class DeputyBean
{
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

   @XmlElement(name="u")
   long user;
   
   @XmlElement(name="f")
   @XmlJavaTypeAdapter(DateAdapter.class)
   Date from;
   
   @XmlElement(name="t")
   @XmlJavaTypeAdapter(DateAdapter.class)
   Date to;
   
   DeputyBean()
   {
      // required default constructor
   }
   
   DeputyBean(long oid, Date fromDate, Date toDate)
   {
      user = oid;
      from = fromDate;
      to = toDate;
   }

   public Deputy createDeputyDetails(UserInfo deputyUser)
   {
      UserInfo userInfo = DetailsFactory.create(UserBean.findByOid(user));
      return new DeputyDetails(userInfo, deputyUser, from, to);
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
         throw new InternalException("Unable to parse deputy info from '" + value + "'.", e);
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
   
   public static void main(String[] args)
   {
      System.out.println(new DeputyBean(12345, new Date(), null));
   }
}
