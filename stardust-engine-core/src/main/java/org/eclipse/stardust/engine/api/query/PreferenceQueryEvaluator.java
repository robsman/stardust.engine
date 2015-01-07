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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.preferences.ParsedPreferenceQuery;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;


public class PreferenceQueryEvaluator implements FilterEvaluationVisitor
{
   private PreferenceScope scope;

   private String moduleId;

   private String preferencesId;

   private String realmId;

   private String userId;

   public PreferenceQueryEvaluator(PreferenceQuery query)
   {
      scope = null;
      moduleId = "*";
      preferencesId = "*";
      realmId = "*";
      userId = "*";

      visit(query.getFilter(), null);
   }

   public ParsedPreferenceQuery getParsedQuery()
   {
      return new ParsedPreferenceQuery(this.scope,this.moduleId,this.preferencesId,this.realmId, this.userId);
   }

   public Object visit(FilterTerm filter, Object context)
   {
      for (Object part : filter.getParts())
      {
         FilterCriterion criterion = (FilterCriterion) part;
         if ( !(criterion instanceof FilterTerm))
         {
            criterion.accept(this, context);
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Usage of nested FilterTerms is not supported");
         }
      }
      return null;
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      // TODO Auto-generated method stub

      throw new UnsupportedOperationException("Unary filtes are not supported");
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      Binary op = filter.getOperator();

      if (op.equals(Binary.LIKE) || op.equals(Binary.IS_EQUAL))
      {
         String attribute = filter.getAttribute();

         String value = (filter.getValue() == null) ? null : filter.getValue().toString();

         if (PreferenceQuery.MODULE_ID.getAttributeName().equals(attribute))
         {
            this.moduleId = value;
         }
         else if (PreferenceQuery.PREFERENCES_ID.getAttributeName().equals(attribute))
         {
            this.preferencesId = value;
         }
         else if (PreferenceQuery.REALM_ID.getAttributeName().equals(attribute))
         {
            this.realmId = value;
         }
         else if (PreferenceQuery.USER_ID.getAttributeName().equals(attribute))
         {
            this.userId = value;
         }
         else if (PreferenceQuery.SCOPE.getAttributeName().equals(attribute))
         {
            this.scope = getScopeFromId(value);
         }

      }
      else
      {
         throw new UnsupportedOperationException(
               "Only Binary like or isEqual operators are supported.");
      }
      return null;
   }

   private PreferenceScope getScopeFromId(String value)
   {
      if (PreferenceScope.DEFAULT.name().equals(value))
      {
         return PreferenceScope.DEFAULT;
      }
      else if (PreferenceScope.PARTITION.name().equals(value))
      {
         return PreferenceScope.PARTITION;
      }
      else if (PreferenceScope.REALM.name().equals(value))
      {
         return PreferenceScope.REALM;
      }
      else if (PreferenceScope.USER.name().equals(value))
      {
         return PreferenceScope.USER;
      }
      return null;
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessDefinitionFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public Object visit(DocumentFilter filter, Object context)
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   public PreferenceScope getScope()
   {
      return scope;
   }

   public String getModuleId()
   {
      return moduleId;
   }

   public String getPreferencesId()
   {
      return preferencesId;
   }

   public String getRealmId()
   {
      return realmId;
   }

   public String getUserId()
   {
      return userId;
   }

}
