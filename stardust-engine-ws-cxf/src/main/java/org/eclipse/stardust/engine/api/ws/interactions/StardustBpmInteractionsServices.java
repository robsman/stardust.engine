package org.eclipse.stardust.engine.api.ws.interactions;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2014-11-26T10:53:28.057+01:00
 * Generated source version: 2.6.1
 * 
 */
@WebServiceClient(name = "StardustBpmInteractionsServices", 
                  wsdlLocation = "file:/C:/development/Products/IPP/Versions/b_dev_8_1_x/stardust/engine/stardust-engine-ws-cxf/src/main/resources/META-INF/wsdl/StardustBpmInteractionsService.wsdl",
                  targetNamespace = "http://eclipse.org/stardust/ws/v2012a/interactions") 
public class StardustBpmInteractionsServices extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://eclipse.org/stardust/ws/v2012a/interactions", "StardustBpmInteractionsServices");
    public final static QName BpmInteractionsServiceEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/interactions", "BpmInteractionsServiceEndpoint");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/development/Products/IPP/Versions/b_dev_8_1_x/stardust/engine/stardust-engine-ws-cxf/src/main/resources/META-INF/wsdl/StardustBpmInteractionsService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(StardustBpmInteractionsServices.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/C:/development/Products/IPP/Versions/b_dev_8_1_x/stardust/engine/stardust-engine-ws-cxf/src/main/resources/META-INF/wsdl/StardustBpmInteractionsService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public StardustBpmInteractionsServices(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public StardustBpmInteractionsServices(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public StardustBpmInteractionsServices() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns IBpmInteractionsService
     */
    @WebEndpoint(name = "BpmInteractionsServiceEndpoint")
    public IBpmInteractionsService getBpmInteractionsServiceEndpoint() {
        return super.getPort(BpmInteractionsServiceEndpoint, IBpmInteractionsService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IBpmInteractionsService
     */
    @WebEndpoint(name = "BpmInteractionsServiceEndpoint")
    public IBpmInteractionsService getBpmInteractionsServiceEndpoint(WebServiceFeature... features) {
        return super.getPort(BpmInteractionsServiceEndpoint, IBpmInteractionsService.class, features);
    }

}
