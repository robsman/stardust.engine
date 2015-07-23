/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.compatibility.ui.preferences;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.AbstractPreferenceStore;

public class ReadOnlyUiPreferenceEditor extends AbstractPreferenceEditor
{

   public ReadOnlyUiPreferenceEditor(String moduleId, String preferencesId,
         AbstractPreferenceStore prefsStore)
   {
      super(moduleId, preferencesId, prefsStore);
   }

   @Override
   public void save()
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void resetValue(String name)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, boolean value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, double value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, float value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, int value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, long value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

   @Override
   public void setValue(String name, String value)
   {
      throw new PublicException(
            BpmRuntimeError.JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES.raise());
   }

}
