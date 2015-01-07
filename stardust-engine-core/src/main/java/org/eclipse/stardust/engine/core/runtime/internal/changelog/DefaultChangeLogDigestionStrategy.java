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
package org.eclipse.stardust.engine.core.runtime.internal.changelog;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.ChangeLogDigester.HistoricState;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.spi.IChangeLogDigestionStrategy;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.spi.IChangeLogDigestionStrategyFactory;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DefaultChangeLogDigestionStrategy implements IChangeLogDigestionStrategy
{

   public List digestChangeLog(IActivityInstance ai, List changeLog)
   {
      HistoricState firstState = (HistoricState) changeLog.get(0);
      if (!ActivityInstanceState.Created.equals(firstState.getState()))
      {
         // there must be an open interval from the previous TX, this record causes an
         // update effectively closing the interval
         firstState.setUpdatedRecord();
      }

      List<ChangeLogDigester.HistoricState> result = CollectionUtils.newList();
      for (int i = 0; i < changeLog.size(); ++i)
      {
         ChangeLogDigester.HistoricState hs = (ChangeLogDigester.HistoricState) changeLog.get(i);

         // There must be at least two predecessor records, as the merge candidate has to
         // have a predecessor who's interval gets extended. This is really important as
         // the finally written interval's from timestamp must be equal to the
         // lastModificationTime value of the activity instance, or else the linked list
         // gets corrupted
         if (2 <= result.size())
         {
            // try to merge pure worklist assignments into previous pending state change
            // to reduce number of historic entries

            final int idxMergeCandidate = result.size() - 1;
            ChangeLogDigester.HistoricState candidateHs = result.get(idxMergeCandidate);
            ChangeLogDigester.HistoricState predecessorHs = result.get(idxMergeCandidate - 1);

            if (candidateHs.getState() == hs.getState()
                  && isSamePerformer(candidateHs, hs)
                  && !isOpenInterval(hs))
            {
               hs = new ChangeLogDigester.HistoricState(candidateHs.getFrom(), hs
                     .getUntil(), candidateHs.getState(), candidateHs.getPerformer(),
                     candidateHs.getDepartment(), candidateHs.getWorkflowUserOid());
               result.remove(idxMergeCandidate);
            }
            else if (candidateHs.isNewRecord())
            {
               if (isSuspendSequence(candidateHs, hs)
                     || isCompleteSequence(candidateHs, hs)
                     || isAbortSequence(candidateHs, hs))
               {
                  // extend predecessor interval to make sure lastModificationTime matches
                  predecessorHs.setUntil(candidateHs.getUntil());

                  hs = new ChangeLogDigester.HistoricState(hs.getFrom(), hs.getUntil(),
                        candidateHs.getState(), hs.getPerformer(), hs.getDepartment(), candidateHs.getWorkflowUserOid());
                  result.remove(idxMergeCandidate);
               }
               else if (isActivationSequence(candidateHs, hs))
               {
                  // extend predecessor interval to make sure lastModificationTime matches
                  predecessorHs.setUntil(candidateHs.getUntil());

                  hs = new ChangeLogDigester.HistoricState(hs.getFrom(), hs.getUntil(),
                        hs.getState(), candidateHs.getPerformer(), candidateHs
                        .getDepartment(), candidateHs.getWorkflowUserOid());
                  result.remove(idxMergeCandidate);
               }
            }
         }

         result.add(hs);
      }

      // strip leading CREATED and trailing TERMINATED states to save space
      for (Iterator i = result.iterator(); i.hasNext();)
      {
         ChangeLogDigester.HistoricState hs = (ChangeLogDigester.HistoricState) i.next();
         if (isFilteredState(hs.getState()))
         {
            i.remove();
         }
      }

      return result;
   }

   private static boolean isSamePerformer(ChangeLogDigester.HistoricState lhs, ChangeLogDigester.HistoricState rhs)
   {
      boolean isSame = false;

      IParticipant lhsPerformer = lhs.getPerformer();
      IParticipant rhsPerformer = rhs.getPerformer();

      if (lhsPerformer instanceof IUser)
      {
         isSame = (rhsPerformer instanceof IUser)
               && (((IUser) lhsPerformer).getOID() == ((IUser) rhsPerformer).getOID());
      }
      else if (lhsPerformer instanceof IUserGroup)
      {
         isSame = (rhsPerformer instanceof IUserGroup)
               && (((IUserGroup) lhsPerformer).getOID() == ((IUserGroup) rhsPerformer).getOID());
      }
      else if ((lhsPerformer instanceof IModelParticipant)
            && (rhsPerformer instanceof IModelParticipant))
      {
         IModelParticipant lhsMp = (IModelParticipant) lhsPerformer;
         IModelParticipant rhsMp = (IModelParticipant) rhsPerformer;

         isSame = CompareHelper.areEqual(lhsMp.getModel(), rhsMp.getModel())
               && CompareHelper.areEqual(lhsMp.getId(), rhsMp.getId());
      }
      else
      {
         isSame = CompareHelper.areEqual(lhsPerformer, rhsPerformer);
      }

      return isSame;
   }

   private static boolean isFilteredState(ActivityInstanceState state)
   {
      return ActivityInstanceState.Created.equals(state);
   }

   private boolean isOpenInterval(ChangeLogDigester.HistoricState hs)
   {
      Date until = hs.getUntil();
      return until == null || until.getTime() == 0;
   }

   private boolean isActivationSequence(ChangeLogDigester.HistoricState hs1, ChangeLogDigester.HistoricState hs2)
   {
      return ActivityInstanceState.Suspended == hs1.getState()
            && ActivityInstanceState.Application == hs2.getState()
            && hs1.getPerformer() instanceof IUser
            && isSamePerformer(hs1, hs2);
   }

   private boolean isSuspendSequence(ChangeLogDigester.HistoricState hs1, ChangeLogDigester.HistoricState hs2)
   {
      return ActivityInstanceState.Suspended == hs1.getState()
            && hs1.getState() == hs2.getState()
            && hs1.getPerformer() instanceof IUser
            && !isSamePerformer(hs1, hs2);
   }

   private boolean isCompleteSequence(ChangeLogDigester.HistoricState hs1, ChangeLogDigester.HistoricState hs2)
   {
      return ActivityInstanceState.Completed == hs1.getState()
            && hs1.getState() == hs2.getState()
            && hs1.getPerformer() instanceof IUser
            && !isSamePerformer(hs1, hs2);
   }

   private boolean isAbortSequence(ChangeLogDigester.HistoricState hs1, ChangeLogDigester.HistoricState hs2)
   {
      return ActivityInstanceState.Aborted == hs1.getState()
            && hs1.getState() == hs2.getState()
            && !isSamePerformer(hs1, hs2);
   }

   public static class Factory implements IChangeLogDigestionStrategyFactory
   {
      public IChangeLogDigestionStrategy createDigester()
      {
         return new DefaultChangeLogDigestionStrategy();
      }
   }
}
