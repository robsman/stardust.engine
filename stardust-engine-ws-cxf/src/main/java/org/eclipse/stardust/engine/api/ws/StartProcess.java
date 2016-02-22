
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parameters" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *         &lt;element name="startSynchronously" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="attachments" type="{http://eclipse.org/stardust/ws/v2012a/api}InputDocuments" minOccurs="0"/>
 *         &lt;element name="benchmarkId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "processId",
    "parameters",
    "startSynchronously",
    "attachments",
    "benchmarkId"
})
@XmlRootElement(name = "startProcess")
public class StartProcess {

    @XmlElement(required = true, nillable = true)
    protected String processId;
    @XmlElement(required = true, nillable = true)
    protected ParametersXto parameters;
    @XmlElement(required = true, type = Boolean.class, nillable = true)
    protected Boolean startSynchronously;
    @XmlElementRef(name = "attachments", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<InputDocumentsXto> attachments;
    @XmlElementRef(name = "benchmarkId", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<String> benchmarkId;

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessId(String value) {
        this.processId = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setParameters(ParametersXto value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the startSynchronously property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStartSynchronously() {
        return startSynchronously;
    }

    /**
     * Sets the value of the startSynchronously property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStartSynchronously(Boolean value) {
        this.startSynchronously = value;
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link InputDocumentsXto }{@code >}
     *     
     */
    public JAXBElement<InputDocumentsXto> getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link InputDocumentsXto }{@code >}
     *     
     */
    public void setAttachments(JAXBElement<InputDocumentsXto> value) {
        this.attachments = ((JAXBElement<InputDocumentsXto> ) value);
    }

    /**
     * Gets the value of the benchmarkId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBenchmarkId() {
        return benchmarkId;
    }

    /**
     * Sets the value of the benchmarkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBenchmarkId(JAXBElement<String> value) {
        this.benchmarkId = ((JAXBElement<String> ) value);
    }

}
