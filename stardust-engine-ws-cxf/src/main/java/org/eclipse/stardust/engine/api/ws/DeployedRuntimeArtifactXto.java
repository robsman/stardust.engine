
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
 *          A deployed runtime artifact is including a unique oid.
 *          It does not contain binary content.
 *          The runtime artifact including the content can be retrieved by oid using other service methods.
 *          
 * 
 * <p>Java class for DeployedRuntimeArtifact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeployedRuntimeArtifact">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="artifactTypeId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="artifactId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="artifactName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeployedRuntimeArtifact", propOrder = {
    "oid",
    "artifactTypeId",
    "artifactId",
    "artifactName",
    "validFrom"
})
public class DeployedRuntimeArtifactXto {

    protected long oid;
    @XmlElement(required = true)
    protected String artifactTypeId;
    @XmlElement(required = true)
    protected String artifactId;
    protected String artifactName;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;

    /**
     * Gets the value of the oid property.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
    }

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

}
