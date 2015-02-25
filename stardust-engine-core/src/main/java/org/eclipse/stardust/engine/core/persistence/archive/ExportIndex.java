package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ExportIndex implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   @Expose
   private final List<Long> processInstanceOids;
   @Expose
   private final Map<Long, List<Long>> rootProcessToSubProcesses;
   private final List<Integer> processLengths;
   
   public ExportIndex()
   {
      this.processInstanceOids = new ArrayList<Long>();
      this.processLengths = new ArrayList<Integer>();
      this.rootProcessToSubProcesses = new HashMap<Long, List<Long>>();
   }
   
   public ExportIndex(List<Long> processInstanceOids, List<Integer> processLengths,
         Map<Long, List<Long>> rootProcessToSubProcesses)
   {
      this.processInstanceOids = processInstanceOids;
      this.rootProcessToSubProcesses = rootProcessToSubProcesses;
      this.processLengths = processLengths;
   }
   
   public List<Long> getProcessInstanceOids()
   {
      return processInstanceOids;
   }
   public List<Integer> getProcessLengths()
   {
      return processLengths;
   }

   public Map<Long, List<Long>> getRootProcessToSubProcesses()
   {
      return rootProcessToSubProcesses;
   }

}
