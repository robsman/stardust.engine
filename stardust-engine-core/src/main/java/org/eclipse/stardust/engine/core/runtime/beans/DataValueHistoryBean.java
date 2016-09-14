package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.model.beans.DataBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.ForeignKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class DataValueHistoryBean extends IdentifiablePersistentBean
      implements IDataValue, BigData
{

   private static final Logger trace = LogManager.getLogger(DataValueHistoryBean.class);

   /**
    * 
    */
   private static final long serialVersionUID = -799003414551246757L;

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;

   public static final String FIELD__MODEL = "model";

   public static final String FIELD__DATA = "data";

   public static final String FIELD__PROCESS_INSTANCE = "processInstance";

   public static final String FIELD__TYPE_KEY = "type_key";

   public static final String FIELD__STRING_VALUE = "string_value";

   public static final String FIELD__NUMBER_VALUE = "number_value";

   public static final String FIELD__DOUBLE_VALUE = "double_value";

   public static final String FIELD__MOD_TIMESTAMP = "mod_timestamp";

   public static final String FIELD__MOD_USER = "mod_user";

   public static final String FIELD__MOD_ACTIVITY_INSTANCE = "mod_ai_instance";

   public static final FieldRef FR__OID = new FieldRef(DataValueHistoryBean.class,
         FIELD__OID);

   public static final FieldRef FR__MODEL = new FieldRef(DataValueHistoryBean.class,
         FIELD__MODEL);

   public static final FieldRef FR__DATA = new FieldRef(DataValueHistoryBean.class,
         FIELD__DATA);

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(
         DataValueHistoryBean.class, FIELD__PROCESS_INSTANCE);

   public static final FieldRef FR__TYPE_KEY = new FieldRef(DataValueHistoryBean.class,
         FIELD__TYPE_KEY);

   public static final FieldRef FR__STRING_VALUE = new FieldRef(
         DataValueHistoryBean.class, FIELD__STRING_VALUE);

   public static final FieldRef FR__NUMBER_VALUE = new FieldRef(
         DataValueHistoryBean.class, FIELD__NUMBER_VALUE);

   public static final FieldRef FR__DOUBLE_VALUE = new FieldRef(
         DataValueHistoryBean.class, FIELD__DOUBLE_VALUE);

   public static final FieldRef FR__MOD_TIMESTAMP = new FieldRef(
         DataValueHistoryBean.class, FIELD__MOD_TIMESTAMP);

   public static final FieldRef FR__MOD_USER = new FieldRef(DataValueHistoryBean.class,
         FIELD__MOD_USER);

   public static final FieldRef FR__MOD_ACTIVITY_INSTANCE = new FieldRef(
         DataValueHistoryBean.class, FIELD__MOD_ACTIVITY_INSTANCE);

   public static final String TABLE_NAME = "data_value_history";

   public static final String DEFAULT_ALIAS = "dvh";

   public static final String LOCK_TABLE_NAME = "data_value_history_lck";

   public static final String LOCK_INDEX_NAME = "data_value_history_lck_idx";

   public static final String PK_FIELD = FIELD__OID;

   public static final String PK_SEQUENCE = "data_value_history_seq";

   @ForeignKey(modelElement = ModelBean.class)
   public long model;

   @ForeignKey(modelElement = DataBean.class)
   public long data;
   
   @ForeignKey(persistentElement = ProcessInstanceBean.class)
   public ProcessInstanceBean processInstance;

   public String string_value;

   public long number_value;

   public double double_value;

   public long mod_timestamp;

   public long mod_user;

   public long mod_activity_instance;

   public int type_key = BigData.NULL;

   private transient BigDataHandler dataHandler;

   public DataValueHistoryBean()
   {
      dataHandler = new LargeStringHolderBigDataHandler(this);
   }

   public DataValueHistoryBean(DataValueBean dataValueBean,
         DataMappingContext mappingContext)
   {
      this.data = dataValueBean.data;
      this.processInstance = dataValueBean.processInstance;
      this.model = dataValueBean.model;

      this.double_value = dataValueBean.double_value;
      this.number_value = dataValueBean.number_value;
      this.type_key = dataValueBean.getType();

      this.string_value = dataValueBean.string_value;

      this.mod_timestamp = TimestampProviderUtils.getTimeStampValue();
      this.mod_user = SecurityProperties.getUserOID();

      if (mappingContext != null)
      {
         this.mod_activity_instance = mappingContext.getActivityInstance().getOID();
      }

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);

      if (dataValueBean.getType() == BIG_STRING
            || dataValueBean.getType() == BIG_SERIALIZABLE)
      {

         dataHandler = new LargeStringHolderBigDataHandler(this);
         dataHandler.write(dataValueBean.getValue(), false);
      }

   }

   @Override
   public Object getValue()
   {
      return dataHandler.read();
   }

   @Override
   public void setShortStringValue(String value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.string_value, value))
      {
         markModified(FIELD__STRING_VALUE);
         string_value = value;
      }
   }

   @Override
   public void setLongValue(long value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.number_value, value))
      {
         markModified(FIELD__NUMBER_VALUE);
         number_value = value;
      }
   }

   @Override
   public void setDoubleValue(double value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.double_value, value))
      {
         markModified(FIELD__DOUBLE_VALUE);
         double_value = value;
      }
   }

   @Override
   public int getType()
   {
      fetch();
      return this.type_key;
   }

   @Override
   public long getLongValue()
   {
      fetch();
      return this.number_value;
   }

   @Override
   public void setType(int type)
   {
      fetch();
      if (this.type_key != type)
      {
         markModified(FIELD__TYPE_KEY);
         type_key = type;
      }
   }

   @Override
   public String getShortStringValue()
   {
      fetch();
      return string_value == null && type_key == BigData.STRING ? "" : string_value;
   }

   @Override
   public double getDoubleValue()
   {
      fetch();
      return double_value;
   }

   @Override
   public int getShortStringColumnLength()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public IData getData()
   {
      fetch();
      return ModelManagerFactory.getCurrent().findData(model, data);
   }

   @Override
   public void setValue(Object value, boolean forceRefresh)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Serializable getSerializedValue()
   {
      if (dataHandler != null)
      {
         return (Serializable) dataHandler.read();
      }
      return null;
   }

   @Override
   public void refresh()
   {
      dataHandler.refresh();
   }

   @Override
   public IProcessInstance getProcessInstance()
   {
      fetchLink(FIELD__PROCESS_INSTANCE);
      return processInstance;
   }

   public long getModificationTimestamp()
   {
      fetch();
      return mod_timestamp;
   }

   public long getModificatingUser()
   {
      fetch();
      return mod_user;
   }

   public long getModificatingActivityInstance()
   {
      fetch();
      return mod_activity_instance;
   }

}
