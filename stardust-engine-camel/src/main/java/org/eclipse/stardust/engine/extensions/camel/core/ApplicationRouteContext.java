package org.eclipse.stardust.engine.extensions.camel.core;

import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.APPLICATION_INTEGRATION_OVERLAY_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.GENERIC_ENDPOINT_OVERLAY;;
public abstract class ApplicationRouteContext extends RouteContext
{
   protected IApplication application;
   
   public String getApplicationId()
   {
      return application.getId();
   }
   /**
    * mark route as transacted by default
    *
    * @return
    */
   public boolean markTransacted()
   {
      Object value = application.getAttribute(CamelConstants.TRANSACTED_ROUTE_EXT_ATT);

      if (value != null && value instanceof Boolean)
         return (Boolean) value;

      if (value != null)
         return Boolean.parseBoolean((String) value);
      return true;

   }
   public String getModelId(){
      return application.getModel().getId();
   }

   public String getId(){
      return application.getId();
   }
   
   @SuppressWarnings("unchecked")
   public boolean containsOutputBodyAccessPointOfDocumentType()
   {
      if(application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT) != null
            && application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT).equals(GENERIC_ENDPOINT_OVERLAY))
      {
         String outBodyAccessPointId = application.getAttribute(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
         Iterator<AccessPointBean> accessPoints = application.getAllOutAccessPoints();
         while (accessPoints.hasNext())
         {
            AccessPointBean ap = accessPoints.next();
            if(ap.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_DOCUMENT)
                  && ap.getId().equals(outBodyAccessPointId))
            {
               return true;
            }
         }
      }
      return false;
   }
   
   @SuppressWarnings("unchecked")
   public boolean containsInputtAccessPointOfDocumentType()
   {
      if(application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT) != null
            && application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT).equals(GENERIC_ENDPOINT_OVERLAY))
      {
         Iterator<AccessPointBean> accessPoints = application.getAllInAccessPoints();
         while (accessPoints.hasNext())
         {
            AccessPointBean ap = accessPoints.next();
            if(ap.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_DOCUMENT))
            {
               return true;
            }
         }
      }
      return false;
   }
}
