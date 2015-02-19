package org.eclipse.stardust.engine.core.persistence.jdbc.extension;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Session.FilterOperation;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * <p>
 * SPI implementation of ISessionLifecycleExtension to detect and delete process data
 * marked as volatile after process completion
 * </p>
 * 
 * @author Thomas.Wolfram
 *
 */
public class VolatileDataSessionLifecycleExtension implements ISessionLifecycleExtension
{

   private static final Logger trace = LogManager.getLogger(VolatileDataSessionLifecycleExtension.class);

   @Override
   public void beforeSave(Session session)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Entering <beforeSafe> method of session");
      }

      Iterator<ProcessInstanceBean> piIter;

      piIter = session.getSessionCacheIterator(ProcessInstanceBean.class,
            TerminatedProcessInstanceFilterOperation.filterAllTerminatedPIs());

      while (piIter.hasNext())
      {
         ProcessInstanceBean pi = piIter.next();

         if (pi.isAborted() || pi.isCompleted())
         {

            Iterator<DataValueBean> dataIter = pi.getAllDataValues();

            while (dataIter.hasNext())
            {

               DataValueBean data = dataIter.next();

               if (Boolean.parseBoolean((String) data.getData().getAttribute(
                     "carnot:engine:volatile")))
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.info("Data <" + data.getData().getId()
                           + "> is volatile and will be deleted now");
                  }
                  
                  if (StructuredTypeRtUtils.isDmsType(data.getData().getType().getId())
                        || StructuredTypeRtUtils.isStructuredType(data.getData()
                              .getType()
                              .getId()))
                  {                                         
                     IXPathMap xpathMap = DataXPathMap.getXPathMap(data.getData());
                     
                     Set xPathOids = xpathMap.getAllXPathOids();                     
                     Iterator xpathOidIter = xPathOids.iterator();
                     
                     
                     while (xpathOidIter.hasNext())
                     {
                        long xPathOid = (Long) xpathOidIter.next();
                        
                        if (trace.isDebugEnabled())
                        {
                           trace.debug("XPath with OID <" + xPathOid
                                 + "> for volatile structured data <"
                                 + data.getData().getId() + ">  found, deleting content");
                        }
                        
                        Iterator<StructuredDataValueBean> dataValueIter = session.getSessionCacheIterator(
                              StructuredDataValueBean.class,
                              StructuredDataValueFilterOperation.forProcessInstance(xPathOid, pi.getOID()));
                        
                        while (dataValueIter.hasNext())
                        {
                           dataValueIter.next().delete();
                        }
                        
                     }
                     
                     Iterator<ClobDataBean> clobIterator = session.getSessionCacheIterator(
                           ClobDataBean.class,
                           ClobDataFilter.findForOid(data.getLongValue()));
                     
                     while (clobIterator.hasNext())
                     {                  
                        if (trace.isDebugEnabled())
                        {
                           trace.debug("CLOB data for volatile structured data <"
                                 + data.getData().getId() + ">  found, deleting content");
                        }
                        clobIterator.next().delete();
                     }
                                                                                                         
                  }
                  else
                  {

                     if (data.getType() == BigData.BIG_SERIALIZABLE
                           || data.getType() == BigData.BIG_STRING)
                     {
                        Iterator<LargeStringHolder> largeStringList = session.getSessionCacheIterator(
                              LargeStringHolder.class,
                              StringDataForDataValueFilterIOperation.filterForDataValueOid(data.getOID()));

                        while (largeStringList.hasNext())
                        {
                           LargeStringHolder holder = largeStringList.next();
                           if (trace.isDebugEnabled())
                           {
                              trace.debug("Large String Holder found with OID <"
                                    + holder.getOID() + ">, deleting content");
                           }
                           holder.delete();

                        }
                     }
                     data.delete();
                  }
               }

            }
         }
      }

   }

   @Override
   public void afterSave(Session session)
   {
      // TODO Auto-generated method stub

   }

   /******************** Defining FilterOperations ****************/


   private static class TerminatedProcessInstanceFilterOperation
         implements FilterOperation<ProcessInstanceBean>
   {

      @Override
      public FilterOperation.FilterResult filter(ProcessInstanceBean persistentToFilter)
      {
         final boolean isScopePi = persistentToFilter.getScopeProcessInstanceOID() == persistentToFilter.getOID();
         return (isScopePi) ? FilterResult.ADD : FilterResult.OMIT;
      }

      public static TerminatedProcessInstanceFilterOperation filterAllTerminatedPIs()
      {
         return new TerminatedProcessInstanceFilterOperation();
      }

   }

   private static class StringDataForDataValueFilterIOperation
         implements FilterOperation<LargeStringHolder>
   {

      private static long dataValueOid;

      @Override
      public org.eclipse.stardust.engine.core.persistence.Session.FilterOperation.FilterResult filter(
            LargeStringHolder persistentToFilter)
      {
         boolean isElementOfDataValue = persistentToFilter.getObjectID() == dataValueOid;
         return isElementOfDataValue ? FilterResult.ADD : FilterResult.OMIT;
      }

      public static StringDataForDataValueFilterIOperation filterForDataValueOid(long oid)
      {
         dataValueOid = oid;
         return new StringDataForDataValueFilterIOperation();
      }

   }

   private static class StructuredDataValueFilterOperation
         implements FilterOperation<StructuredDataValueBean>
   {

      private static long processInstanceOid;

      private static long xpathOid;

      @Override
      public org.eclipse.stardust.engine.core.persistence.Session.FilterOperation.FilterResult filter(
            StructuredDataValueBean persistentToFilter)
      {
         boolean isElementOfProcessInstance = persistentToFilter.getProcessInstance()
               .getOID() == processInstanceOid;
         boolean isElementOfStructuredData = persistentToFilter.xpath == xpathOid;

         return isElementOfProcessInstance && isElementOfStructuredData
               ? FilterResult.ADD
               : FilterResult.OMIT;
      }

      public static StructuredDataValueFilterOperation forProcessInstance(long dataOid,
            long oid)
      {
         processInstanceOid = oid;
         xpathOid = dataOid;
         return new StructuredDataValueFilterOperation();
      }

   }
   
   public static class StructuredDataFilter implements FilterOperation<StructuredDataBean>
   {
      private static long dataOid;
      
      public static StructuredDataFilter forData(long oid)
      {
         dataOid = oid;
         return new StructuredDataFilter();
      }
                 
      @Override
      public org.eclipse.stardust.engine.core.persistence.Session.FilterOperation.FilterResult filter(
            StructuredDataBean persistentToFilter)
      {
         boolean isElementOfData = persistentToFilter.getData() == dataOid;
         return isElementOfData ? FilterResult.ADD : FilterResult.OMIT;
      }
   }
   
   public static class ClobDataFilter implements FilterOperation<ClobDataBean>
   {
      
      private static long dataOid;
            
      public static ClobDataFilter findForOid(long oid)
      {
         dataOid = oid;
         return new ClobDataFilter();
      }
            
      @Override
      public org.eclipse.stardust.engine.core.persistence.Session.FilterOperation.FilterResult filter(
            ClobDataBean persistentToFilter)
      {
         boolean isClobOfData = persistentToFilter.getOID() == dataOid;
         return isClobOfData ? FilterResult.ADD : FilterResult.OMIT;
      }
                  
   }

}
