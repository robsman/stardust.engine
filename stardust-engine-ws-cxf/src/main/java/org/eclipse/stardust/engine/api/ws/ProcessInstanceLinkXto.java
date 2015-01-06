
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
 * <p>Java class for ProcessInstanceLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the sourceOid property.
     * 
     */
    public long getSourceOid() {
        return sourceOid;
    }

    /**
     * Sets the value of the sourceOid property.
     * 
     */
    public void setSourceOid(long value) {
        this.sourceOid = value;
    }

    /**
     * Gets the value of the targetOid property.
     * 
     */
    public long getTargetOid() {
        return targetOid;
    }

    /**
     * Sets the value of the targetOid property.
     * 
     */
    public void setTargetOid(long value) {
        this.targetOid = value;
    }

    /**
     * Gets the value of the createTime property.
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
     * Sets the value of the createTime property.
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
     * Gets the value of the creatingUser property.
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
     * Sets the value of the creatingUser property.
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
     * Gets the value of the comment property.
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
     * Sets the value of the comment property.
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
     * Gets the value of the linkType property.
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
     * Sets the value of the linkType property.
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
