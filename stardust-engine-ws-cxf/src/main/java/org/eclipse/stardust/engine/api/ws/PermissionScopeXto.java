
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			The scope of a permission can be either model, process or activity.
 * 			
 * 
 * <p>Java class for PermissionScope complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermissionScope">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scopeType" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionScopeType"/>
 *         &lt;element name="parentScope" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionScope" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermissionScope", propOrder = {
    "modelOid",
    "id",
    "scopeType",
    "parentScope"
})
public class PermissionScopeXto {

    protected Long modelOid;
    protected String id;
    @XmlElement(required = true)
    protected PermissionScopeTypeXto scopeType;
    protected PermissionScopeXto parentScope;

    /**
     * Gets the value of the modelOid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setModelOid(Long value) {
        this.modelOid = value;
    }

    /**
     * Gets the value of the id property.
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
     * Sets the value of the id property.
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
     * Gets the value of the scopeType property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionScopeTypeXto }
     *     
     */
    public PermissionScopeTypeXto getScopeType() {
        return scopeType;
    }

    /**
     * Sets the value of the scopeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionScopeTypeXto }
     *     
     */
    public void setScopeType(PermissionScopeTypeXto value) {
        this.scopeType = value;
    }

    /**
     * Gets the value of the parentScope property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionScopeXto }
     *     
     */
    public PermissionScopeXto getParentScope() {
        return parentScope;
    }

    /**
     * Sets the value of the parentScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionScopeXto }
     *     
     */
    public void setParentScope(PermissionScopeXto value) {
        this.parentScope = value;
    }

}
