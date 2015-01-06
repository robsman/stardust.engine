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
package org.eclipse.stardust.engine.core.model.removethis;

import javax.swing.JFrame;
import javax.swing.JComponent;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.cli.common.DeployedModelsView;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractStatefulDialog;


public class AuditTrailModelsDialog extends AbstractStatefulDialog
{
   private static AuditTrailModelsDialog instance;
   private DeployedModelsView view;

   AuditTrailModelsDialog(JFrame parent)
   {
      super(parent);
   }

   public JComponent createContent()
   {
      return view = new DeployedModelsView(DeployedModelsView.SELECTION_MODE);
   }

   public void validateSettings() throws ValidationException
   {
      DeployedModelsView.ModelTemplate template = view.getSelectedModel();
      if (template == null)
      {
         throw new ValidationException("No model has been selected.", false);
      }
   }

   private void setData(ServiceFactory service)
   {
      view.setData(service.getQueryService().getAllModelDescriptions());
   }

   public static boolean showDialog(ServiceFactory sf, JFrame parent)
   {
      if (instance == null)
      {
         instance = new AuditTrailModelsDialog(parent);
      }

      instance.setData(sf);
      return showDialog("Models in Audit Trail", instance, parent);
   }

   public static int getOIDForSelectedModel()
   {
      return instance.view.getSelectedModel().getModelOID();
   }
}
