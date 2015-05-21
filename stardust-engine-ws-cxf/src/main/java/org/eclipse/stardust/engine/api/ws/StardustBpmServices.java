package org.eclipse.stardust.engine.api.ws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2015-05-20T10:08:50.690+02:00
 * Generated source version: 2.6.1
 * 
 */
@WebServiceClient(name = "StardustBpmServices", 
                  wsdlLocation = "file:/C:/development/checkouts/git1/stardust/engine/stardust-engine-ws-cxf/target/codegen/wsdl/StardustBpmService.wsdl",
                  targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api") 
public class StardustBpmServices extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://eclipse.org/stardust/ws/v2012a/api", "StardustBpmServices");
    public final static QName DocumentManagementServiceHttpBasicAuthEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "DocumentManagementServiceHttpBasicAuthEndpoint");
    public final static QName DocumentManagementServiceHttpBasicAuthSslEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "DocumentManagementServiceHttpBasicAuthSslEndpoint");
    public final static QName DocumentManagementServiceWssUsernameTokenEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "DocumentManagementServiceWssUsernameTokenEndpoint");
    public final static QName AdministrationServiceWssUsernameTokenEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "AdministrationServiceWssUsernameTokenEndpoint");
    public final static QName AdministrationServiceHttpBasicAuthEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "AdministrationServiceHttpBasicAuthEndpoint");
    public final static QName AdministrationServiceHttpBasicAuthSslEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "AdministrationServiceHttpBasicAuthSslEndpoint");
    public final static QName UserServiceWssUsernameTokenEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "UserServiceWssUsernameTokenEndpoint");
    public final static QName UserServiceHttpBasicAuthEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "UserServiceHttpBasicAuthEndpoint");
    public final static QName UserServiceHttpBasicAuthSslEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "UserServiceHttpBasicAuthSslEndpoint");
    public final static QName WorkflowServiceHttpBasicAuthEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "WorkflowServiceHttpBasicAuthEndpoint");
    public final static QName WorkflowServiceHttpBasicAuthSslEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "WorkflowServiceHttpBasicAuthSslEndpoint");
    public final static QName WorkflowServiceWssUsernameTokenEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "WorkflowServiceWssUsernameTokenEndpoint");
    public final static QName QueryServiceWssUsernameTokenEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "QueryServiceWssUsernameTokenEndpoint");
    public final static QName QueryServiceHttpBasicAuthSslEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "QueryServiceHttpBasicAuthSslEndpoint");
    public final static QName QueryServiceHttpBasicAuthEndpoint = new QName("http://eclipse.org/stardust/ws/v2012a/api", "QueryServiceHttpBasicAuthEndpoint");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/development/checkouts/git1/stardust/engine/stardust-engine-ws-cxf/target/codegen/wsdl/StardustBpmService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(StardustBpmServices.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/C:/development/checkouts/git1/stardust/engine/stardust-engine-ws-cxf/target/codegen/wsdl/StardustBpmService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public StardustBpmServices(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public StardustBpmServices(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public StardustBpmServices() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceHttpBasicAuthEndpoint")
    public IDocumentManagementService getDocumentManagementServiceHttpBasicAuthEndpoint() {
        return super.getPort(DocumentManagementServiceHttpBasicAuthEndpoint, IDocumentManagementService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceHttpBasicAuthEndpoint")
    public IDocumentManagementService getDocumentManagementServiceHttpBasicAuthEndpoint(WebServiceFeature... features) {
        return super.getPort(DocumentManagementServiceHttpBasicAuthEndpoint, IDocumentManagementService.class, features);
    }
    /**
     *
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceHttpBasicAuthSslEndpoint")
    public IDocumentManagementService getDocumentManagementServiceHttpBasicAuthSslEndpoint() {
        return super.getPort(DocumentManagementServiceHttpBasicAuthSslEndpoint, IDocumentManagementService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceHttpBasicAuthSslEndpoint")
    public IDocumentManagementService getDocumentManagementServiceHttpBasicAuthSslEndpoint(WebServiceFeature... features) {
        return super.getPort(DocumentManagementServiceHttpBasicAuthSslEndpoint, IDocumentManagementService.class, features);
    }
    /**
     *
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceWssUsernameTokenEndpoint")
    public IDocumentManagementService getDocumentManagementServiceWssUsernameTokenEndpoint() {
        return super.getPort(DocumentManagementServiceWssUsernameTokenEndpoint, IDocumentManagementService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IDocumentManagementService
     */
    @WebEndpoint(name = "DocumentManagementServiceWssUsernameTokenEndpoint")
    public IDocumentManagementService getDocumentManagementServiceWssUsernameTokenEndpoint(WebServiceFeature... features) {
        return super.getPort(DocumentManagementServiceWssUsernameTokenEndpoint, IDocumentManagementService.class, features);
    }
    /**
     * Administration Service (WS-Security UsernameToken)
     * 
     *
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceWssUsernameTokenEndpoint")
    public IAdministrationService getAdministrationServiceWssUsernameTokenEndpoint() {
        return super.getPort(AdministrationServiceWssUsernameTokenEndpoint, IAdministrationService.class);
    }

    /**
     * Administration Service (WS-Security UsernameToken)
     * 
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceWssUsernameTokenEndpoint")
    public IAdministrationService getAdministrationServiceWssUsernameTokenEndpoint(WebServiceFeature... features) {
        return super.getPort(AdministrationServiceWssUsernameTokenEndpoint, IAdministrationService.class, features);
    }
    /**
     *
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceHttpBasicAuthEndpoint")
    public IAdministrationService getAdministrationServiceHttpBasicAuthEndpoint() {
        return super.getPort(AdministrationServiceHttpBasicAuthEndpoint, IAdministrationService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceHttpBasicAuthEndpoint")
    public IAdministrationService getAdministrationServiceHttpBasicAuthEndpoint(WebServiceFeature... features) {
        return super.getPort(AdministrationServiceHttpBasicAuthEndpoint, IAdministrationService.class, features);
    }
    /**
     *
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceHttpBasicAuthSslEndpoint")
    public IAdministrationService getAdministrationServiceHttpBasicAuthSslEndpoint() {
        return super.getPort(AdministrationServiceHttpBasicAuthSslEndpoint, IAdministrationService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IAdministrationService
     */
    @WebEndpoint(name = "AdministrationServiceHttpBasicAuthSslEndpoint")
    public IAdministrationService getAdministrationServiceHttpBasicAuthSslEndpoint(WebServiceFeature... features) {
        return super.getPort(AdministrationServiceHttpBasicAuthSslEndpoint, IAdministrationService.class, features);
    }
    /**
     *
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceWssUsernameTokenEndpoint")
    public IUserService getUserServiceWssUsernameTokenEndpoint() {
        return super.getPort(UserServiceWssUsernameTokenEndpoint, IUserService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceWssUsernameTokenEndpoint")
    public IUserService getUserServiceWssUsernameTokenEndpoint(WebServiceFeature... features) {
        return super.getPort(UserServiceWssUsernameTokenEndpoint, IUserService.class, features);
    }
    /**
     *
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceHttpBasicAuthEndpoint")
    public IUserService getUserServiceHttpBasicAuthEndpoint() {
        return super.getPort(UserServiceHttpBasicAuthEndpoint, IUserService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceHttpBasicAuthEndpoint")
    public IUserService getUserServiceHttpBasicAuthEndpoint(WebServiceFeature... features) {
        return super.getPort(UserServiceHttpBasicAuthEndpoint, IUserService.class, features);
    }
    /**
     *
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceHttpBasicAuthSslEndpoint")
    public IUserService getUserServiceHttpBasicAuthSslEndpoint() {
        return super.getPort(UserServiceHttpBasicAuthSslEndpoint, IUserService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IUserService
     */
    @WebEndpoint(name = "UserServiceHttpBasicAuthSslEndpoint")
    public IUserService getUserServiceHttpBasicAuthSslEndpoint(WebServiceFeature... features) {
        return super.getPort(UserServiceHttpBasicAuthSslEndpoint, IUserService.class, features);
    }
    /**
     *
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceHttpBasicAuthEndpoint")
    public IWorkflowService getWorkflowServiceHttpBasicAuthEndpoint() {
        return super.getPort(WorkflowServiceHttpBasicAuthEndpoint, IWorkflowService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceHttpBasicAuthEndpoint")
    public IWorkflowService getWorkflowServiceHttpBasicAuthEndpoint(WebServiceFeature... features) {
        return super.getPort(WorkflowServiceHttpBasicAuthEndpoint, IWorkflowService.class, features);
    }
    /**
     *
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceHttpBasicAuthSslEndpoint")
    public IWorkflowService getWorkflowServiceHttpBasicAuthSslEndpoint() {
        return super.getPort(WorkflowServiceHttpBasicAuthSslEndpoint, IWorkflowService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceHttpBasicAuthSslEndpoint")
    public IWorkflowService getWorkflowServiceHttpBasicAuthSslEndpoint(WebServiceFeature... features) {
        return super.getPort(WorkflowServiceHttpBasicAuthSslEndpoint, IWorkflowService.class, features);
    }
    /**
     *
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceWssUsernameTokenEndpoint")
    public IWorkflowService getWorkflowServiceWssUsernameTokenEndpoint() {
        return super.getPort(WorkflowServiceWssUsernameTokenEndpoint, IWorkflowService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IWorkflowService
     */
    @WebEndpoint(name = "WorkflowServiceWssUsernameTokenEndpoint")
    public IWorkflowService getWorkflowServiceWssUsernameTokenEndpoint(WebServiceFeature... features) {
        return super.getPort(WorkflowServiceWssUsernameTokenEndpoint, IWorkflowService.class, features);
    }
    /**
     *
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceWssUsernameTokenEndpoint")
    public IQueryService getQueryServiceWssUsernameTokenEndpoint() {
        return super.getPort(QueryServiceWssUsernameTokenEndpoint, IQueryService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceWssUsernameTokenEndpoint")
    public IQueryService getQueryServiceWssUsernameTokenEndpoint(WebServiceFeature... features) {
        return super.getPort(QueryServiceWssUsernameTokenEndpoint, IQueryService.class, features);
    }
    /**
     *
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceHttpBasicAuthSslEndpoint")
    public IQueryService getQueryServiceHttpBasicAuthSslEndpoint() {
        return super.getPort(QueryServiceHttpBasicAuthSslEndpoint, IQueryService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceHttpBasicAuthSslEndpoint")
    public IQueryService getQueryServiceHttpBasicAuthSslEndpoint(WebServiceFeature... features) {
        return super.getPort(QueryServiceHttpBasicAuthSslEndpoint, IQueryService.class, features);
    }
    /**
     *
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceHttpBasicAuthEndpoint")
    public IQueryService getQueryServiceHttpBasicAuthEndpoint() {
        return super.getPort(QueryServiceHttpBasicAuthEndpoint, IQueryService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IQueryService
     */
    @WebEndpoint(name = "QueryServiceHttpBasicAuthEndpoint")
    public IQueryService getQueryServiceHttpBasicAuthEndpoint(WebServiceFeature... features) {
        return super.getPort(QueryServiceHttpBasicAuthEndpoint, IQueryService.class, features);
    }

}
