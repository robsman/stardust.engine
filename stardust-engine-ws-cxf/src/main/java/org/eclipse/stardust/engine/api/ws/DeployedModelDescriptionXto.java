
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *             The DeployedModelDescription class provides deployment information for a workflow model.
 * 			
 * 
 * <p>Java-Klasse für DeployedModelDescription complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DeployedModelDescription">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="consumerModels" type="{http://eclipse.org/stardust/ws/v2012a/api}OidList"/>
 *         &lt;element name="deploymentComment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deploymentTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="implementationProcesses" type="{http://eclipse.org/stardust/ws/v2012a/api}ImplementationProcessesMap"/>
 *         &lt;element name="providersModel" type="{http://eclipse.org/stardust/ws/v2012a/api}OidList"/>
 *         &lt;element name="revision" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="active" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeployedModelDescription", propOrder = {
    "consumerModels",
    "deploymentComment",
    "deploymentTime",
    "implementationProcesses",
    "providersModel",
    "revision",
    "validFrom",
    "version",
    "active"
})
public class DeployedModelDescriptionXto
    extends ModelElementXto
{

    @XmlElement(required = true)
    protected OidListXto consumerModels;
    @XmlElement(required = true)
    protected String deploymentComment;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date deploymentTime;
    @XmlElement(required = true)
    protected ImplementationProcessesMapXto implementationProcesses;
    @XmlElement(required = true)
    protected OidListXto providersModel;
    protected int revision;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;
    @XmlElement(required = true)
    protected String version;
    protected boolean active;

    /**
     * Ruft den Wert der consumerModels-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OidListXto }
     *     
     */
    public OidListXto getConsumerModels() {
        return consumerModels;
    }

    /**
     * Legt den Wert der consumerModels-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OidListXto }
     *     
     */
    public void setConsumerModels(OidListXto value) {
        this.consumerModels = value;
    }

    /**
     * Ruft den Wert der deploymentComment-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeploymentComment() {
        return deploymentComment;
    }

    /**
     * Legt den Wert der deploymentComment-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeploymentComment(String value) {
        this.deploymentComment = value;
    }

    /**
     * Ruft den Wert der deploymentTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDeploymentTime() {
        return deploymentTime;
    }

    /**
     * Legt den Wert der deploymentTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeploymentTime(Date value) {
        this.deploymentTime = value;
    }

    /**
     * Ruft den Wert der implementationProcesses-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ImplementationProcessesMapXto }
     *     
     */
    public ImplementationProcessesMapXto getImplementationProcesses() {
        return implementationProcesses;
    }

    /**
     * Legt den Wert der implementationProcesses-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ImplementationProcessesMapXto }
     *     
     */
    public void setImplementationProcesses(ImplementationProcessesMapXto value) {
        this.implementationProcesses = value;
    }

    /**
     * Ruft den Wert der providersModel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OidListXto }
     *     
     */
    public OidListXto getProvidersModel() {
        return providersModel;
    }

    /**
     * Legt den Wert der providersModel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OidListXto }
     *     
     */
    public void setProvidersModel(OidListXto value) {
        this.providersModel = value;
    }

    /**
     * Ruft den Wert der revision-Eigenschaft ab.
     * 
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Legt den Wert der revision-Eigenschaft fest.
     * 
     */
    public void setRevision(int value) {
        this.revision = value;
    }

    /**
     * Ruft den Wert der validFrom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Legt den Wert der validFrom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidFrom(Date value) {
        this.validFrom = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der active-Eigenschaft ab.
     * 
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Legt den Wert der active-Eigenschaft fest.
     * 
     */
    public void setActive(boolean value) {
        this.active = value;
    }

}
