
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 *             The DeployedModelDescription class provides deployment information for a workflow model.
 * 			
 * 
 * <p>Java class for DeployedModelDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the consumerModels property.
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
     * Sets the value of the consumerModels property.
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
     * Gets the value of the deploymentComment property.
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
     * Sets the value of the deploymentComment property.
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
     * Gets the value of the deploymentTime property.
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
     * Sets the value of the deploymentTime property.
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
     * Gets the value of the implementationProcesses property.
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
     * Sets the value of the implementationProcesses property.
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
     * Gets the value of the providersModel property.
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
     * Sets the value of the providersModel property.
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
     * Gets the value of the revision property.
     * 
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Sets the value of the revision property.
     * 
     */
    public void setRevision(int value) {
        this.revision = value;
    }

    /**
     * Gets the value of the validFrom property.
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
     * Sets the value of the validFrom property.
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
     * Gets the value of the version property.
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
     * Sets the value of the version property.
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
     * Gets the value of the active property.
     * 
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     */
    public void setActive(boolean value) {
        this.active = value;
    }

}
