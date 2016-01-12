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
import java.util.List;
import java.util.Map;

/**
 * Options class that specifies how the data should be copied between two process instances.
 *
 * @author Florin.Herinean
 */
public class DataCopyOptions implements Serializable
{
   private static final long serialVersionUID = 2L;

   /**
    * The default data copy options used when no options are specified.
    */
   public static final DataCopyOptions DEFAULT = new DataCopyOptions(true, null, null, true);

   private boolean copyAllData;
   private Map<String, String> dataTranslationTable;
   private Map<String, ? extends Serializable> replacementTable;
   private boolean useHeuristics;
   private List<String> converters;

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
      this(copyAllData, dataTranslationTable, replacementTable, useHeuristics, null);
   }

   /**
    * Creates a new DataCopyOptions object with a list of custom data value converters.
    *
    * @param converters a list of data value converter class names.
    *                   The classes must be accessible to the engine and must have
    *                   a public default constructor.
    */
   public DataCopyOptions(List<String> converters)
   {
      this(false, null, null, false, converters);
   }

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
    * @param converters a list of data value converter class names.
    *                   The classes must be accessible to the engine and must have
    *                   a public default constructor.
    */
   public DataCopyOptions(boolean copyAllData, Map<String, String> dataTranslationTable,
         Map<String, ? extends Serializable> replacementTable, boolean useHeuristics,
         List<String> converters)
   {
      this.copyAllData = copyAllData;
      this.dataTranslationTable = dataTranslationTable;
      this.replacementTable = replacementTable;
      this.useHeuristics = useHeuristics;
      this.converters = converters;
   }

   /**
    * Checks if the engine should attempt to copy all compatible data.
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
    * Checks if heuristics should be applied to determine which data should be copied.
    */
   public boolean useHeuristics()
   {
      return useHeuristics;
   }

   /**
    * Retrieves the list of data value converter class names.
    *
    * @return a list of class names.
    */
   public List<String> getDataValueConverters()
   {
      return converters;
   }
}
