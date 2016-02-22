package org.eclipse.stardust.engine.core.benchmark;

public class ConditionParameter
{
   private static final String DATA_TYPE_KEY = "data";
   
   private static final String ATTRIBUTE_TYPE_KEY  = "attribute";
   
   public enum ParameterType {DATA, ATTRIBUTE};
   
   private ParameterType type;
   private String parameterId;
   private String dataPath;
   
   public ConditionParameter (ParameterType type, String parameterId, String dataPath)
   {
      this.type = type;
      this.parameterId = parameterId;
      this.dataPath = dataPath;
   }

   public ParameterType getType()
   {
      return type;
   }

   public String getParameterId()
   {
      return parameterId;
   }

   public String getDataPath()
   {
      return dataPath;
   }
   
   public static ParameterType evaluateType(String typeString)
   {
      if (typeString.equals(DATA_TYPE_KEY))
      {
         return ParameterType.DATA;
      }
      else if (typeString.equals(ATTRIBUTE_TYPE_KEY))
      {
         return ParameterType.ATTRIBUTE;
      }
      return null;
   }
}
