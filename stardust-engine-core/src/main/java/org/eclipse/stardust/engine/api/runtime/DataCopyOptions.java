/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Map;

/**
 * Options class that specifies how the data should be copied between two process instances. 
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DataCopyOptions implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The default data copy options used when no options are specified. 
    */
   public static final DataCopyOptions DEFAULT = new DataCopyOptions(true, null, null, true);

   /**
    * If true, the engine will attempt to copy all compatible data.
    */
   private boolean copyAllData;
   
   /**
    * Map that specifies renamed data objects. The key is the data id of the new process instance,
    * the value is the data id in the originating process.
    */
   private Map<String, String> dataTranslationTable;
   
   /**
    * Map that specifies new values for specific data objects.
    */
   private Map<String, ? extends Serializable> replacementTable;
   
   /**
    * If true and copyAllData is true, it will attempt to copy those data that are used in data paths and data mappings
    * of directly contained activities.
    */
   private boolean useHeuristics;

   /**
    * Creates a new DataCopyOptions object with the specified parameters.
    * 
    * @param copyAllData if true, it will attempt to copy all data
    *                    from the source process instance to the target process instance.
    * @param dataTranslationTable a Map that indicates that the values for the specified
    *                    data should be taken from another data object. The keys are the
    *                    IDs of the target data and the values are the IDs of the source
    *                    data objects.
    * @param replacementTable a Map that specifies concrete values for target data. The
    *                    keys are the IDs of the target data. 
    * @param useHeuristics if true then the engine will attempt to auto determine which
    *                    data must be copied by investigating the data mappings and data
    *                    paths of the target process instance.
    */
   public DataCopyOptions(boolean copyAllData, Map<String, String> dataTranslationTable,
         Map<String, ? extends Serializable> replacementTable, boolean useHeuristics)
   {
      this.copyAllData = copyAllData;
      this.dataTranslationTable = dataTranslationTable;
      this.replacementTable = replacementTable;
      this.useHeuristics = useHeuristics;
   }

   /**
    * Gets if the engine should attempt to copy all compatible data.
    * 
    * @return true if the engine should attempt to copy all compatible data.
    */
   public boolean copyAllData()
   {
      return copyAllData;
   }

   /**
    * Gets the Map that specifies from where the data values should be retrieved.
    * 
    * @return a Map with data IDs. The keys are representing the IDs of the data in the
    *         target process instance while the values are representing the IDs of the
    *         data in the source process instance.
    */
   public Map<String, String> getDataTranslationTable()
   {
      return dataTranslationTable;
   }

   /**
    * Gets the Map with concrete data values.
    * 
    * @return a Map with ID/value pairs.
    */
   public Map<String, ? extends Serializable> getReplacementTable()
   {
      return replacementTable;
   }

   /**
    * Gets if heuristics should be applied to determine which data should be copied.
    * 
    * @return true if the engine should auto determine which data should be copied between the process instances.
    */
   public boolean useHeuristics()
   {
      return useHeuristics;
   }
}
