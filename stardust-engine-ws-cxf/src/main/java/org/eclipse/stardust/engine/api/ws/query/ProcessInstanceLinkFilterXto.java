
package org.eclipse.stardust.engine.api.ws.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for matching process instances having links from or to other process instances.
 *         
 * 
 * <p>Java-Klasse für ProcessInstanceLinkFilter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceLinkFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="direction" type="{http://eclipse.org/stardust/ws/v2012a/api/query}LinkDirection"/>
 *         &lt;element name="linkTypes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceLinkFilter", propOrder = {
    "processOid",
    "direction",
    "linkTypes"
})
public class ProcessInstanceLinkFilterXto
    extends PredicateBaseXto
{

    protected long processOid;
    @XmlElement(required = true)
    protected LinkDirectionXto direction;
    @XmlElement(required = true)
    protected ProcessInstanceLinkFilterXto.LinkTypesXto linkTypes;

    /**
     * Ruft den Wert der processOid-Eigenschaft ab.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Legt den Wert der processOid-Eigenschaft fest.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Ruft den Wert der direction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LinkDirectionXto }
     *     
     */
    public LinkDirectionXto getDirection() {
        return direction;
    }

    /**
     * Legt den Wert der direction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkDirectionXto }
     *     
     */
    public void setDirection(LinkDirectionXto value) {
        this.direction = value;
    }

    /**
     * Ruft den Wert der linkTypes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceLinkFilterXto.LinkTypesXto }
     *     
     */
    public ProcessInstanceLinkFilterXto.LinkTypesXto getLinkTypes() {
        return linkTypes;
    }

    /**
     * Legt den Wert der linkTypes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceLinkFilterXto.LinkTypesXto }
     *     
     */
    public void setLinkTypes(ProcessInstanceLinkFilterXto.LinkTypesXto value) {
        this.linkTypes = value;
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
     *         &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "typeId"
    })
    public static class LinkTypesXto {

        protected List<String> typeId;

        /**
         * Gets the value of the typeId property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the typeId property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTypeId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTypeId() {
            if (typeId == null) {
                typeId = new ArrayList<String>();
            }
            return this.typeId;
        }

    }

}
