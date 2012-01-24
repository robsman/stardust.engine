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
package org.eclipse.stardust.engine.cli.console;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.IllegalUsageException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.cli.common.DepartmentClientUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class UserCommand extends ConsoleCommand
{
   protected static final String REALM = "realm";
   protected static final String ACCOUNT = "account";
   protected static final String FIRST_NAME = "firstname";
   protected static final String LAST_NAME = "lastname";
   protected static final String DESCRIPTION = "description";
   protected static final String PASSWORD = "password";
   protected static final String VALIDFROM = "validfrom";
   protected static final String VALIDTO = "validto";
   protected static final String EMAIL = "email";

   protected Options argTypes = new Options();

   protected UserCommand()
   {
      argTypes.register("-" + REALM, "-sr", REALM, "The security realm of the user.",
            true);
      argTypes.register("-account", "-a", ACCOUNT, "Account name of the user.",
            true);
      argTypes.register("-firstname", "-f", FIRST_NAME, "First name of the user.", true);
      argTypes.register("-lastname", "-l", LAST_NAME, "Last name of the user.", true);
      argTypes.register("-email", "-e", EMAIL, "E-Mail address of the user.", true);
      argTypes.register("-description", "-d", DESCRIPTION,
            "An optional description of the user.", true);
      argTypes.register("-password", "-p", PASSWORD, "The password of the user.", true);
      argTypes.register("-validfrom", "-v", VALIDFROM, "'Valid from' time of the user.", true);
      argTypes.register("-validto", "-t", VALIDTO, "'Valid to' time of the user.", true);

      argTypes.addMandatoryRule(ACCOUNT);
   }

   public Options getOptions()
   {
      return argTypes;
   }
   
   protected List<GrantHolder> toGrants(String grantString)
   {
      List<GrantHolder> result = CollectionUtils.newList();
      StringTokenizer tkr = new StringTokenizer(grantString, ",");
      while (tkr.hasMoreTokens())
      {
    	 String departmentPathOrOid = null;
         DepartmentInfo department = null;
         String t = tkr.nextToken().trim();

         int departmentPartIndex = t.lastIndexOf("@");
         if(departmentPartIndex != -1) {
        	 departmentPathOrOid = t.substring(departmentPartIndex + 1);
        	 t = t.substring(0, departmentPartIndex);
         } 
         department = getDepartmentFromOidOrPath(departmentPathOrOid, t);
         result.add(new GrantHolder(department, t));
      }
      return result;
   }

   protected void addGrants(String grantString, User user)
   {
      List grants = toGrants(grantString);
      for (Iterator i = grants.iterator(); i.hasNext();)
      {
         GrantHolder holder = (GrantHolder) i.next();
         user.addGrant(holder.getParticipant());
      }
   }

   protected void removeGrants(String grantString, User user)
   {
      List grants = toGrants(grantString);
      for (Iterator i = grants.iterator(); i.hasNext();)
      {
         GrantHolder holder = (GrantHolder) i.next();
         user.removeGrant(holder.getParticipant());
      }
   }

   protected class GrantHolder
   {
      private String id;
      private String qualifiedId;
      private DepartmentInfo department;

      public GrantHolder(DepartmentInfo department, String id)
      {
         this.department = department;
         this.qualifiedId = id;
         this.id = QName.valueOf(id).getLocalPart();
      }

      public ModelParticipantInfo getParticipant()
      {
         return new ModelParticipantImpl(id, qualifiedId, department);
      }
   }
   
   private static class ModelParticipantImpl implements QualifiedModelParticipantInfo
   {
      private static final long serialVersionUID = 2L;

      private String id;
      private String qualifiedId;
      private DepartmentInfo department;

      private ModelParticipantImpl(String id, String qualifiedId, DepartmentInfo department)
      {
         this.id = id;
         this.qualifiedId = qualifiedId;
         this.department = department;
      }

      public long getRuntimeElementOID()
      {
         return 0;
      }

      public String getId()
      {
         return id;
      }

      public String getQualifiedId()
      {
         return qualifiedId;
      }

      public String getName()
      {
         return null;
      }
      
      public boolean definesDepartmentScope()
      {
         return false;
      }

      public DepartmentInfo getDepartment()
      {
         return department;
      }

      public boolean isDepartmentScoped()
      {
         return false;
      }
   }
   
   private DepartmentInfo getDepartmentFromOidOrPath(String departmentPathOrOid,
         String participantId)
   {
      DepartmentInfo department = null;
      DepartmentClientUtils dh = DepartmentClientUtils.getInstance(globalOptions);

      ServiceFactory factory = ServiceFactoryLocator.get(globalOptions);
      WorkflowService ws = factory.getWorkflowService();      
      
      String namespace = null;
      if (participantId.startsWith("{"))
      {
         QName qname = QName.valueOf(participantId);
         namespace = qname.getNamespaceURI();
         participantId = qname.getLocalPart();
      }               
      
      Model model = null;
      if (namespace != null)
      {      
          Models models = factory.getQueryService().getModels(DeployedModelQuery.findActiveForId(namespace));    	  
          if(models.getSize() > 0)
          {
             
             // model oid
        	 DeployedModelDescriptionDetails details = (DeployedModelDescriptionDetails) models.get(0);
        	 model = factory.getQueryService().getModel(details.getModelOID());
          }
      }
      else
      {
    	  model = ws.getModel();
      }
      
      if (model == null)
      {
         throw new IllegalUsageException("invalid participant provided: " + participantId);
      }
      
      Participant participant = model.getParticipant(participantId);
      if (participant == null)
      {
         throw new IllegalUsageException("invalid participant provided: " + participantId);
      }

      List<Organization> scopedHierachy = dh.getOrganizationHierarchy(participant, true);
      // pathorid specified
      if (!StringUtils.isEmpty(departmentPathOrOid))
      {
         // try to extract the oid
         try
         {
            int departmentOID = Integer.parseInt(departmentPathOrOid);
            department = new DepartmentInfoDetails(departmentOID, null, null, 0);
         }
         catch (NumberFormatException e)
         {
            // try to find the department via the path
            try
            {
               List<String> departmentPath = dh.parseDepartmentPath(departmentPathOrOid);
               List<Department> departmentHierachy = dh.getDepartmentHierarchy(
                     departmentPath, scopedHierachy);
               if (!departmentHierachy.isEmpty())
               {
                  int lastElementIndex = departmentHierachy.size() - 1;
                  department = departmentHierachy.get(lastElementIndex);
               }
            }
            catch (ObjectNotFoundException notFoundException)
            {
               throw new IllegalUsageException("Cant resolve department path for: " + "'"
                     + departmentPathOrOid + "'" + " and participant: " + "'"
                     + participantId + "'" + ", grants won't be created.");
            }
         }

      }
      if (department == null && !scopedHierachy.isEmpty())
      {
         department = Department.DEFAULT;
      }
      return department;
   }
}