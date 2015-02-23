
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 			The DeploymentInfo is used to receive information about a
 *  			deployment operation. Deployment operations are all operations which modify models in audit trail
 *  			or their attributes, i.e. deploy, owerwrite, modify or delete.
 * 			
 * 
 * <p>Java-Klasse für DeploymentInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DeploymentInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errors" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="error" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="warnings" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="warning" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="predecessorOID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="deploymentTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="revision" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="disabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentInfo", propOrder = {
    "errors",
    "warnings",
    "modelOid",
    "predecessorOID",
    "validFrom",
    "validTo",
    "deploymentTime",
    "success",
    "id",
    "comment",
    "revision",
    "disabled"
})
public class DeploymentInfoXto {

    protected DeploymentInfoXto.ErrorsXto errors;
    protected DeploymentInfoXto.WarningsXto warnings;
    protected int modelOid;
    protected int predecessorOID;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validTo;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date deploymentTime;
    protected boolean success;
    @XmlElement(required = true)
    protected String id;
    protected String comment;
    protected int revision;
    protected boolean disabled;

    /**
     * Ruft den Wert der errors-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DeploymentInfoXto.ErrorsXto }
     *     
     */
    public DeploymentInfoXto.ErrorsXto getErrors() {
        return errors;
    }

    /**
     * Legt den Wert der errors-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DeploymentInfoXto.ErrorsXto }
     *     
     */
    public void setErrors(DeploymentInfoXto.ErrorsXto value) {
        this.errors = value;
    }

    /**
     * Ruft den Wert der warnings-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DeploymentInfoXto.WarningsXto }
     *     
     */
    public DeploymentInfoXto.WarningsXto getWarnings() {
        return warnings;
    }

    /**
     * Legt den Wert der warnings-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DeploymentInfoXto.WarningsXto }
     *     
     */
    public void setWarnings(DeploymentInfoXto.WarningsXto value) {
        this.warnings = value;
    }

    /**
     * Ruft den Wert der modelOid-Eigenschaft ab.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Legt den Wert der modelOid-Eigenschaft fest.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

    /**
     * Ruft den Wert der predecessorOID-Eigenschaft ab.
     * 
     */
    public int getPredecessorOID() {
        return predecessorOID;
    }

    /**
     * Legt den Wert der predecessorOID-Eigenschaft fest.
     * 
     */
    public void setPredecessorOID(int value) {
        this.predecessorOID = value;
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
     * Ruft den Wert der success-Eigenschaft ab.
     * 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Legt den Wert der success-Eigenschaft fest.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der comment-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Legt den Wert der comment-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
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


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="error" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
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
        "error"
    })
    public static class ErrorsXto {

        protected List<InconsistencyXto> error;

        /**
         * Gets the value of the error property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the error property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getError().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InconsistencyXto }
         * 
         * 
         */
        public List<InconsistencyXto> getError() {
            if (error == null) {
                error = new ArrayList<InconsistencyXto>();
            }
            return this.error;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="warning" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
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
        "warning"
    })
    public static class WarningsXto {

        protected List<InconsistencyXto> warning;

        /**
         * Gets the value of the warning property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the warning property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getWarning().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InconsistencyXto }
         * 
         * 
         */
        public List<InconsistencyXto> getWarning() {
            if (warning == null) {
                warning = new ArrayList<InconsistencyXto>();
            }
            return this.warning;
        }

    }

}
