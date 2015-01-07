package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.Util.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.CreateDocumentException;
import org.eclipse.stardust.engine.extensions.camel.util.CamelDmsUtils;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;

public class CamelMessageHelper
{

   private final static String[] legacyAccessPoints = new String[] {"oParam1", "mParam2", "body", "header"};

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static Object[] setInDataAccessPoints(Method method, Application application, List accessPointValues)
   {

      Object anObject[] = new Object[method.getParameterTypes().length];
      Class aClass[] = method.getParameterTypes();

      Iterator<AccessPoint> accessPoints = application.getAllAccessPoints().iterator();

      Map<String, Object> inHeaders = new HashMap<String, Object>();
      Map<String, Object> outHeaders = new HashMap<String, Object>();
      StringBuilder accessPointIds = new StringBuilder();

      while (accessPoints.hasNext())
      {
         AccessPoint ap = accessPoints.next();

         if ((ap.getDirection().equals(Direction.IN_OUT) || ap.getDirection().equals(Direction.IN))
               && isValidAccessPoint(application, ap))
         {
            Object bodyOutAP = getBodyInAccessPoint(application) != null
                  ? getBodyInAccessPoint(application)
                  : "oParam1";

            CamelMessageLocation location = ap.getId().equals(bodyOutAP) || bodyOutAP == null
                  ? CamelMessageLocation.BODY
                  : CamelMessageLocation.HEADER;

            if (CamelMessageLocation.BODY.equals(location))
            {
               Object pair = findAccessPointValue(bodyOutAP, accessPointValues);

               if (ap.getId().equals("mParam2"))
               {
                  anObject[1] = Reflect.castValue(null == pair ? null : ((Pair) pair).getSecond(), aClass[1]);
               }
               else
               {
                  anObject[0] = Reflect.castValue(null == pair ? null : ((Pair) pair).getSecond(), aClass[0]);
               }
            }
            else if (CamelMessageLocation.HEADER.equals(location))
            {
               Object tempPair = findAccessPointValue(ap.getId(), accessPointValues);

               if (tempPair != null)
               {
                  inHeaders.put(((Pair) tempPair).getFirst().toString(), ((Pair) tempPair).getSecond());
               }
            }
         }
         else if ((ap.getDirection().equals(Direction.IN_OUT) || ap.getDirection().equals(Direction.OUT))
               && isValidAccessPoint(application, ap))
         {
            Object bodyOutAP = getBodyOutAccessPoint(application) != null
                  ? getBodyOutAccessPoint(application)
                  : "returnValue";
            CamelMessageLocation location = ap.getId().equals(bodyOutAP) || bodyOutAP == null
                  ? CamelMessageLocation.BODY
                  : CamelMessageLocation.HEADER;

            if (CamelMessageLocation.HEADER.equals(location))
            {
               outHeaders.put(ap.getId(), null);
               accessPointIds.append(ap.getId());
               accessPointIds.append(",");
            }

         }
      }
      Map<String, Object> headers = new HashMap<String, Object>();
      if (!inHeaders.isEmpty())
      {
         headers.putAll(inHeaders);
      }

      if (!outHeaders.isEmpty())
      {
         String accessPointIdString = null;
         accessPointIdString = (accessPointIds.substring(0, accessPointIds.length() - 1)).toString();
         outHeaders.put(CamelConstants.CAT_HEADERS_OUT_ACCESS_POINT, accessPointIdString);
         for (String key : outHeaders.keySet())
            if (!headers.containsKey(key))
               headers.put(key, outHeaders.get(key));
      }

      anObject[1] = Reflect.castValue(headers, aClass[1]);
      return anObject;
   }

   @SuppressWarnings("unchecked")
   public static Map<String, Object> getOutDataAccessPoints(Message message, ActivityInstance ai)
   {

      Map<String, Object> map = new HashMap<String, Object>();

      Application application = ai.getActivity().getApplication();

      if (application.getAllAccessPoints() != null)
      {
         Iterator<AccessPoint> accessPoints = application.getAllAccessPoints().iterator();

         while (accessPoints.hasNext())
         {
            AccessPoint ap = accessPoints.next();

            if ((ap.getDirection().equals(Direction.IN_OUT) || ap.getDirection().equals(Direction.OUT))
                  && isValidAccessPoint(application, ap))
            {
                     Object bodyOutAP;
                     CamelMessageLocation location;
                     if(getInvocationPattern(application)!=null && getInvocationType(application)!=null){
                        bodyOutAP= getBodyOutAccessPoint(application) != null? getBodyOutAccessPoint(application):null;
                     }else{//old behavior
                        bodyOutAP= getBodyOutAccessPoint(application) != null? getBodyOutAccessPoint(application):"returnValue";
                     }
                 if(bodyOutAP==null)
                    location=   CamelMessageLocation.HEADER;
                 else
                    location = ap.getId().equals(bodyOutAP) || bodyOutAP == null
                     ? CamelMessageLocation.BODY
                     : CamelMessageLocation.HEADER;
               if(ap.getAccessPathEvaluatorClass().equals(CamelConstants.VFS_DOCUMENT_ACCESS_PATHE_EVALUATOR_CLASS)
                     && !CamelMessageLocation.BODY.equals(location))
               {
                  map.put(ap.getId(), getDocumentOutDataAccessPoint(message, ai, ap));
               }
               else if (CamelMessageLocation.BODY.equals(location))
               {
                  map.put((String) bodyOutAP, message.getBody());
               }
               else if (CamelMessageLocation.HEADER.equals(location))
               {
                  map.put(ap.getId(), message.getHeader(ap.getId()));
               }

            }
         }
      }

      return map;
   }

   @SuppressWarnings("unchecked")
   public static Map<String, Object> getOutDataMappings(Message message, ActivityInstance ai)
   {

      Map<String, Object> map = new HashMap<String, Object>();

      ApplicationContext context = getActivityInstanceApplicationContext(ai) != null
            ? getActivityInstanceApplicationContext(ai)
            : getActivityInstanceDefaultContext(ai);
      if (context != null)
      {
         Iterator<DataMapping> outDataMappings = context.getAllOutDataMappings().iterator();

         Application application = ai.getActivity().getApplication();
         if(application != null)
         {
            while (outDataMappings.hasNext())
            {
               DataMapping mapping = outDataMappings.next();

               AccessPoint accessPoint = mapping.getApplicationAccessPoint();

               if (isValidAccessPoint(ai.getActivity().getApplication(), accessPoint))
               {
                  Object bodyOutAP = getBodyOutAccessPoint(application) != null
                        ? getBodyOutAccessPoint(application)
                        : "returnValue";

                  CamelMessageLocation location = accessPoint.getId().equals(bodyOutAP) || bodyOutAP == null
                        ? CamelMessageLocation.BODY
                        : CamelMessageLocation.HEADER;

                  if((accessPoint.getDirection().equals(Direction.IN_OUT) || accessPoint.getDirection().equals(Direction.OUT))
                        && isValidAccessPoint(application, accessPoint) && !CamelMessageLocation.BODY.equals(location)
                        && accessPoint.getAccessPathEvaluatorClass().equals(CamelConstants.VFS_DOCUMENT_ACCESS_PATHE_EVALUATOR_CLASS))
                  {
                     map.put(mapping.getId(), getDocumentOutDataAccessPoint(message, ai, accessPoint));
                  }
                  else if (CamelMessageLocation.BODY.equals(location))
                  {
                     map.put(mapping.getId(), message.getBody());
                  }
                  else if (CamelMessageLocation.HEADER.equals(location))
                  {
                     map.put(mapping.getId(), message.getHeader(mapping.getId()));
                  }
               }
            }
         }
         
      }
      return map;
   }

   @SuppressWarnings("rawtypes")
   public static Object findAccessPointValue(Object ap, List<Pair<String, Object>> accessPoints)
   {
      Pair pair = null;

      for (Iterator<Pair<String, Object>> _iterator = accessPoints.iterator(); _iterator.hasNext();)
      {
         Pair pair1 = _iterator.next();
         if (ap.equals(pair1.getFirst()))
         {
            pair = pair1;
            break;
         }
      }

      return pair;
   }

   private static boolean isValidAccessPoint(Application application, AccessPoint ap)
   {

      boolean supportsMultipleAccessPoints = false;
      if (getSupportMultipleAccessPointAttribute(application) != null)
      {
         supportsMultipleAccessPoints = (Boolean) getSupportMultipleAccessPointAttribute(application);
      }

      if (supportsMultipleAccessPoints)
      {
         for (int i = 0; i < legacyAccessPoints.length; i++)
         {
            if (ap.getId().equals(legacyAccessPoints[i]))
            {
               return false;
            }
         }

      }
      else
      {
         for (int i = 0; i < legacyAccessPoints.length; i++)
         {
            if (ap.getId().equals(legacyAccessPoints[i]))
            {
               return true;
            }
         }
      }

      return true;
   }

   public static boolean supportsMultipleAccessPoints(Application application)
   {
      if (getSupportMultipleAccessPointAttribute(application) != null)
      {
         return (Boolean) getSupportMultipleAccessPointAttribute(application);

      }
      return false;
   }

   public static boolean isApplicationProducer(Message message)
   {
      String origin = (String) message.getHeader(CamelConstants.MessageProperty.ORIGIN);

      if (origin != null)
      {
         return origin.equals(CamelConstants.OriginValue.APPLICATION_PRODUCER);
      }

      // has to be old style and therefore application producer
      return true;
   }

   public static boolean isApplicationConsumer(Message message)
   {
      String origin = (String) message.getHeader(CamelConstants.MessageProperty.ORIGIN);

      if (origin != null)
      {
         return origin.equals(CamelConstants.OriginValue.APPLICATION_CONSUMER);
      }

      // has to be old style and therefore application producer
      return false;
   }

   private static Document getDocumentOutDataAccessPoint(Message message, ActivityInstance ai, AccessPoint ap)
   {
      Document document = null;
      String attachmentId = ap.getName();//TODO use AccessPoint ID instead of AccessPoint name
      DataHandler attachment = message.getAttachment(attachmentId);
      if(attachment != null)
      {
         // create Document
         ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
         if(sf == null)
         {
            sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
         }
         DocumentManagementService dms = sf.getDocumentManagementService();
         String fileName="";
         byte[] documentContent = null;

         Object attachmentContent;
         try
         {
            attachmentContent = attachment.getContent();
            if(attachmentContent instanceof DataSource)
            {
               fileName = DataSource.class.cast(attachmentContent).getName();
               documentContent = IOUtils.toByteArray(DataSource.class.cast(attachmentContent).getInputStream());

            } else if(attachmentContent instanceof FileInputStream)
            {
               documentContent = IOUtils.toByteArray(FileInputStream.class.cast(attachmentContent));

            } else if(attachmentContent instanceof byte[])
            {
               documentContent = (byte[])attachmentContent;
            }
         }
         catch (IOException e)
         {
            throw new RuntimeException("Failed retreiving attachment content.", e);
         }

         if(StringUtils.isEmpty(fileName))
         {
            fileName = attachmentId;
         }

         // check if document is already created for PI.
         StringBuilder defaultPath = new StringBuilder(
               DmsUtils.composeDefaultPath(
                     ai.getProcessInstance().getScopeProcessInstanceOID(), ai.getProcessInstance().getStartTime()))
               .append("/")
               .append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER)
               .append("/")
               .append(fileName);

         document = dms.getDocument(defaultPath.toString());
         if(document == null)
         {
            try
            {
               document = CamelDmsUtils.storeDocument(dms, ai.getProcessInstance(), documentContent, fileName, false);
            }
            catch (CreateDocumentException e)
            {
               throw new RuntimeException("Failed creating document.", e);
            }
         }
      }else{
         document=(DmsDocumentBean) message.getHeader(ap.getId());
      }

      return document;
   }

}
