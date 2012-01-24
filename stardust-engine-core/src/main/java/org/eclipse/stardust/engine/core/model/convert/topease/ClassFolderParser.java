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
package org.eclipse.stardust.engine.core.model.convert.topease;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class ClassFolderParser extends RecursiveFolderParser
{
   Map loadItem(Node teClass, Package parent)
   {
      String id;
      String[] idNameDescr;
      ClassWrapper wrapper;
      HashMap retVal;

      id = SimpleTaskUtil.getId(teClass);
      idNameDescr = SimpleTaskUtil.getIdentNameDescr(teClass);
      wrapper = new ClassWrapper(idNameDescr[0], idNameDescr[1], idNameDescr[2], parent);
      retVal = new HashMap();
      retVal.put(id, wrapper);
      return retVal;
   }

   String getRootFolderName()
   {
      return "Class";
   }

   String getItemName()
   {
      return "TEClass";
   }

   String getContentType()
   {
      return "TEClass";
   }
}
