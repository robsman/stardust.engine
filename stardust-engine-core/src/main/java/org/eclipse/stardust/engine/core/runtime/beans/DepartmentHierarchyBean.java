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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * @author Florin.Herinean
 */
public class DepartmentHierarchyBean extends PersistentBean implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(DepartmentHierarchyBean.class);

   public static final String FIELD__SUPERDEPARTMENT = "superDepartment";
   public static final String FIELD__SUBDEPARTMENT = "subDepartment";

   public static final FieldRef FR__SUPERDEPARTMENT = new FieldRef(DepartmentHierarchyBean.class, FIELD__SUPERDEPARTMENT);
   public static final FieldRef FR__SUBDEPARTMENT = new FieldRef(DepartmentHierarchyBean.class, FIELD__SUBDEPARTMENT);

   public static final String TABLE_NAME = "department_hierarchy";
   public static final String DEFAULT_ALIAS = "dptmh";

   public static final String[] PK_FIELD = new String[] {FIELD__SUPERDEPARTMENT, FIELD__SUBDEPARTMENT};
   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] department_hier_idx1_UNIQUE_INDEX = new String[]{FIELD__SUPERDEPARTMENT, FIELD__SUBDEPARTMENT};
   public static final String[] department_hier_idx2_UNIQUE_INDEX = new String[]{FIELD__SUBDEPARTMENT, FIELD__SUPERDEPARTMENT};

   private long superDepartment;
   public static final String superDepartment_EAGER_FETCH = Boolean.FALSE.toString();
   public static final String superDepartment_MANDATORY = Boolean.TRUE.toString();

   private long subDepartment;
   public static final String subDepartment_EAGER_FETCH = Boolean.FALSE.toString();
   public static final String subDepartment_MANDATORY = Boolean.TRUE.toString();

   public static List<Long> findAllSubDepartments(long department)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      QueryDescriptor piDescriptor = QueryDescriptor.from(DepartmentHierarchyBean.class)
            .select(DepartmentHierarchyBean.FR__SUBDEPARTMENT)
            .where(Predicates.isEqual(DepartmentHierarchyBean.FR__SUPERDEPARTMENT, department));      
      ResultSet resultSet = session.executeQuery(piDescriptor);
      try
      {
         List<Long> result = CollectionUtils.newList();
         while (resultSet.next())
         {
            result.add(resultSet.getLong(1));
         }
         return result;
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         QueryUtils.closeResultSet(resultSet);
         throw new PublicException(e);
      }
   }

   public static Iterator<IDepartment> findAllSubDepartments(IDepartment department)
   {
      PredicateTerm departmentPredicate = Predicates.isEqual(DepartmentHierarchyBean.FR__SUPERDEPARTMENT, department.getOID());

      QueryExtension queryExtension = new QueryExtension()
         .addJoin(new Join(DepartmentHierarchyBean.class)
            .on(DepartmentBean.FR__OID, DepartmentHierarchyBean.FIELD__SUBDEPARTMENT)
            .where(departmentPredicate));

      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      return session.<IDepartment,DepartmentBean>getIterator(DepartmentBean.class, queryExtension);
   }

   public static List<Long> findAllSuperDepartments(long department)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      QueryDescriptor piDescriptor = QueryDescriptor.from(DepartmentHierarchyBean.class)
            .select(DepartmentHierarchyBean.FR__SUPERDEPARTMENT)
            .where(Predicates.isEqual(DepartmentHierarchyBean.FR__SUBDEPARTMENT, department));      
      ResultSet resultSet = session.executeQuery(piDescriptor);
      try
      {
         List<Long> result = CollectionUtils.newList();
         while (resultSet.next())
         {
            result.add(resultSet.getLong(1));
         }
         return result;
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         QueryUtils.closeResultSet(resultSet);
         throw new PublicException(e);
      }
   }

   public DepartmentHierarchyBean()
   {
   }

   public DepartmentHierarchyBean(long superDepartment, long subDepartment)
   {
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(//
            DepartmentHierarchyBean.class, QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__SUPERDEPARTMENT, superDepartment),//
                  Predicates.isEqual(FR__SUBDEPARTMENT, subDepartment)))))
      {
         throw new PublicException("Department hierarchy entry for '" + superDepartment
               + "' and '" + subDepartment + "' already exists.");
      }

      this.superDepartment = superDepartment;
      this.subDepartment = subDepartment;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public String toString()
   {
      return "Domain hierarchy entry: " + superDepartment + ", " + subDepartment;
   }

   public IDepartment getSuperDepartment()
   {
      fetch();
      return DepartmentBean.findByOID(superDepartment);
   }
   
   public IDepartment getSubDepartment()
   {
      fetch();
      return DepartmentBean.findByOID(subDepartment);
   }
}

