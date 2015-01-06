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
package org.eclipse.stardust.engine.extensions.ejb.data;

import java.util.Map;

/**
 * @author fherinean
 * @version $Revision$
 */
public interface IEntityPKEvaluator
{
   /**
    * Obtains the PK from an entity bean access point.
    *
    * @param attributes The access point definition's attributes.
    * @param value The base value to be used for PK inspection.
    * @return The obtained PK, or <code>null</code> if no strategy was successful.
    */
   Object getEntityBeanPK(Map attributes, Object value);

   Object findEntityByPK(Map attributes, Object pk);
}
