package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class ExportIndex implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   @Expose
   private final List<Long> processInstanceOids;
   private final List<Integer> processLengths;
   
   public ExportIndex()
   {
      this.processInstanceOids = new ArrayList<Long>();
      this.processLengths = new ArrayList<Integer>();
   }
   
   public ExportIndex(List<Long> processInstanceOids, List<Integer> processLengths)
   {
      this.processInstanceOids = processInstanceOids;
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

}
