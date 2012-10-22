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
package org.eclipse.stardust.engine.core.struct.beans;

import java.util.Iterator;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;


/**
 * Describes an xpath (used for structured data values). List of xpaths belongs to 
 * every structured data definition 
 */
public class StructuredDataBean extends IdentifiablePersistentBean implements IXPath
{
   private static final String SPACE = " ";

   private static final long serialVersionUID = -2780072696011628151L;
   
   private static final Logger trace = LogManager.getLogger(StructuredDataBean.class);

   /**
    * Providing this instance will result in default initialization.  
    */
   public static final Object USE_DEFAULT_INITIAL_VALUE = new Object();

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__DATA = "data";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__XPATH = "xpath";

   public static final FieldRef FR__OID = new FieldRef(StructuredDataBean.class, FIELD__OID);
   public static final FieldRef FR__DATA = new FieldRef(StructuredDataBean.class, FIELD__DATA);
   public static final FieldRef FR__MODEL = new FieldRef(StructuredDataBean.class, FIELD__MODEL);
   public static final FieldRef FR__XPATH = new FieldRef(StructuredDataBean.class, FIELD__XPATH);

   public static final String TABLE_NAME = "structured_data";
   public static final String DEFAULT_ALIAS = "sd";

   public static final String[] PK_FIELD = new String[] {FIELD__OID, FIELD__MODEL};
  
   public static final String[] struct_data_idx1_UNIQUE_INDEX = new String[] {FIELD__OID, FIELD__MODEL};
   public static final String[] struct_data_idx2_INDEX = new String[] {FIELD__XPATH};

   private String xpath;
   private long data;
   private long model;
   
   public static final int xpath_COLUMN_LENGTH = 200;

   private transient String fullLengthXPath = null;
   
   public static Iterator<StructuredDataBean> findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            StructuredDataBean.class,
            new QueryExtension() //
                  .addJoin(new Join(ModelPersistorBean.class) //
                        .on(StructuredDataBean.FR__MODEL, ModelPersistorBean.FIELD__OID) //
                        .where(
                              Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                                    partitionOid))));
   }
   
   public static StructuredDataBean findByOid(long rtOid, long modelOid)
   {
      return (StructuredDataBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
                  StructuredDataBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OID, rtOid),
                  Predicates.isEqual(FR__MODEL, modelOid))));
   }

   public StructuredDataBean()
   {
   }

   public StructuredDataBean(long rtOid, long data, long model, String xpath)
   {
      setOID(rtOid);
      this.data = data;
      this.model = model;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (xpath.length() > xpath_COLUMN_LENGTH)
      {
         // overflow to clob_data table, look for existing entry (equal oids mean equal xpaths)
         ClobDataBean clobDataBean = ClobDataBean.find(rtOid, StructuredDataBean.class);
         if (null == clobDataBean)
         {
            // no entry yet, create it
            clobDataBean = new ClobDataBean(rtOid, StructuredDataBean.class, xpath);
            session.cluster(clobDataBean);
         }
         else
         {
            Assert.condition(xpath.equals(clobDataBean.getStringValue()));
         }
         this.fullLengthXPath = xpath;
         this.xpath = xpath.substring(0, xpath_COLUMN_LENGTH);
      }
      else
      {
         this.fullLengthXPath = xpath;
         this.xpath = xpath;
      }
      
      session.cluster(this);

      if (trace.isDebugEnabled())
      {
         trace.debug("XPath entry created for xPath '" + xpath + "'.");
      }
   }

   public boolean hasOverflow()
   {
      if (getXPath().length() > xpath_COLUMN_LENGTH)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   public String getXPath()
   {
      if (this.fullLengthXPath == null)
      {
         // lazy init fullLengthXPath 
         if (this.xpath != null && this.xpath.length() == xpath_COLUMN_LENGTH)
         {
            // long xpath: there should be an entry in clob_data 
            ClobDataBean clobDataBean = ClobDataBean.find(this.getOID(), StructuredDataBean.class);
            if (null == clobDataBean)
            {
               // the value length is exactly on the limit -> no entry in clob_data
               this.fullLengthXPath = this.xpath;
            }
            else
            {
               this.fullLengthXPath = clobDataBean.getStringValue();
            }
         }
         else
         {
            // short xpath
            this.fullLengthXPath = this.xpath;
         }
      }
      
      if (this.fullLengthXPath == null || SPACE.equals(this.fullLengthXPath))
      {
         // sometimes databases return null (Oracle)
         // or a single space (Sybase)
         // for inserted empty strings 
         return "";
      }
      return this.fullLengthXPath;
   }
 
   public long getData()
   {
      return this.data;
   }
   
   public long getModel()
   {
      return model;
   }

   public long getParent()
   {
      return getData();
   }

   public String getId()
   {
      return getXPath();
   }
}
