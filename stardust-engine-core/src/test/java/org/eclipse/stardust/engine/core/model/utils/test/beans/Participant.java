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
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */
public class Participant extends IdentifiableElementBean
{
   private static final long serialVersionUID = 3813184685610689151L;

   List organizations = CollectionUtils.newList();
   private String id;

   Participant() {}
   public Participant(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public ImageIcon getIcon()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public Iterator getAllOrganizations()
   {
      return ModelUtils.iterator(organizations);
   }

   public Organization findOrganization(String id)
   {
      return (Organization) ModelUtils.findById(organizations, id);
   }

   public long getOrganizationsCount()
   {
      return ModelUtils.size(organizations);
   }
}
