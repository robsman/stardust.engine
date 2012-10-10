package org.eclipse.stardust.engine.cli.sysconsole.patch;

public class RuntimeOidPatch
{

   private long modelOid;

   private long incorrectRuntimeOid;

   private long fixedRuntimeOid;

   public RuntimeOidPatch(long modelOid, long incorrectRuntimeOid, long fixedRuntimeOid)
   {
      this.modelOid = modelOid;
      this.incorrectRuntimeOid = incorrectRuntimeOid;
      this.fixedRuntimeOid = fixedRuntimeOid;
   }

   public long getModelOid()
   {
      return modelOid;
   }

   public long getIncorrectRuntimeOid()
   {
      return incorrectRuntimeOid;
   }

   public long getFixedRuntimeOid()
   {
      return fixedRuntimeOid;
   }
}