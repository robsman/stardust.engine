
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für ProcessInstanceLink complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="targetOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="createTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="creatingUser" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="linkType" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceLinkType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceLink", propOrder = {
    "sourceOid",
    "targetOid",
    "createTime",
    "creatingUser",
    "comment",
    "linkType"
})
public class ProcessInstanceLinkXto {

    protected long sourceOid;
    protected long targetOid;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date createTime;
    @XmlElement(required = true)
    protected UserXto creatingUser;
    @XmlElement(required = true)
    protected String comment;
    @XmlElement(required = true)
    protected ProcessInstanceLinkTypeXto linkType;

    /**
     * Ruft den Wert der sourceOid-Eigenschaft ab.
     * 
     */
    public long getSourceOid() {
        return sourceOid;
    }

    /**
     * Legt den Wert der sourceOid-Eigenschaft fest.
     * 
     */
    public void setSourceOid(long value) {
        this.sourceOid = value;
    }

    /**
     * Ruft den Wert der targetOid-Eigenschaft ab.
     * 
     */
    public long getTargetOid() {
        return targetOid;
    }

    /**
     * Legt den Wert der targetOid-Eigenschaft fest.
     * 
     */
    public void setTargetOid(long value) {
        this.targetOid = value;
    }

    /**
     * Ruft den Wert der createTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Legt den Wert der createTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateTime(Date value) {
        this.createTime = value;
    }

    /**
     * Ruft den Wert der creatingUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getCreatingUser() {
        return creatingUser;
    }

    /**
     * Legt den Wert der creatingUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setCreatingUser(UserXto value) {
        this.creatingUser = value;
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
     * Ruft den Wert der linkType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceLinkTypeXto }
     *     
     */
    public ProcessInstanceLinkTypeXto getLinkType() {
        return linkType;
    }

    /**
     * Legt den Wert der linkType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceLinkTypeXto }
     *     
     */
    public void setLinkType(ProcessInstanceLinkTypeXto value) {
        this.linkType = value;
    }

}
