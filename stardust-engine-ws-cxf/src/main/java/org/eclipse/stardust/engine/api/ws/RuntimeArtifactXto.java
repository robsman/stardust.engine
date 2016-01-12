
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 *          An artifact which can be deployed and is valid from a specified date at runtime.
 *          
 * 
 * <p>Java class for RuntimeArtifact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuntimeArtifact">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="artifactTypeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="artifactId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="artifactName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuntimeArtifact", propOrder = {
    "artifactTypeId",
    "artifactId",
    "artifactName",
    "validFrom",
    "content"
})
public class RuntimeArtifactXto {

    @XmlElement(required = true)
    protected String artifactTypeId;
    @XmlElement(required = true)
    protected String artifactId;
    protected String artifactName;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;
    @XmlMimeType("*/*")
    protected DataHandler content;

    /**
     * Gets the value of the artifactTypeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArtifactTypeId() {
        return artifactTypeId;
    }

    /**
     * Sets the value of the artifactTypeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArtifactTypeId(String value) {
        this.artifactTypeId = value;
    }

    /**
     * Gets the value of the artifactId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the value of the artifactId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArtifactId(String value) {
        this.artifactId = value;
    }

    /**
     * Gets the value of the artifactName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArtifactName() {
        return artifactName;
    }

    /**
     * Sets the value of the artifactName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArtifactName(String value) {
        this.artifactName = value;
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
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setContent(DataHandler value) {
        this.content = value;
    }

}
