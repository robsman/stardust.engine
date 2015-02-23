
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *             Provides deployment information for a workflow model.
 * 			
 * 
 * <p>Java-Klasse für ModelDescription complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelDescription">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="active" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="deploymentComment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="revision" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="deploymentTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="predecessor" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="disabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelDescription", propOrder = {
    "active",
    "validFrom",
    "validTo",
    "deploymentComment",
    "version",
    "revision",
    "deploymentTime",
    "predecessor",
    "disabled"
})
@XmlSeeAlso({
    ModelXto.class
})
public class ModelDescriptionXto
    extends ModelElementXto
{

    protected boolean active;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validTo;
    @XmlElement(required = true)
    protected String deploymentComment;
    @XmlElement(required = true)
    protected String version;
    protected int revision;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date deploymentTime;
    protected int predecessor;
    protected boolean disabled;

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
     * Ruft den Wert der validTo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getValidTo() {
        return validTo;
    }

    /**
     * Legt den Wert der validTo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidTo(Date value) {
        this.validTo = value;
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
     * Ruft den Wert der predecessor-Eigenschaft ab.
     * 
     */
    public int getPredecessor() {
        return predecessor;
    }

    /**
     * Legt den Wert der predecessor-Eigenschaft fest.
     * 
     */
    public void setPredecessor(int value) {
        this.predecessor = value;
    }

    /**
     * Ruft den Wert der disabled-Eigenschaft ab.
     * 
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Legt den Wert der disabled-Eigenschaft fest.
     * 
     */
    public void setDisabled(boolean value) {
        this.disabled = value;
    }

}
