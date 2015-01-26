
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.BusinessObjectQueryXto;


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
 *         &lt;element name="businessObjectQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}BusinessObjectQuery"/>
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
    "businessObjectQuery"
})
@XmlRootElement(name = "getAllBusinessObjects")
public class GetAllBusinessObjects {

    @XmlElement(required = true, nillable = true)
    protected BusinessObjectQueryXto businessObjectQuery;

    /**
     * Gets the value of the businessObjectQuery property.
     * 
     * @return
     *     possible object is
     *     {@link BusinessObjectQueryXto }
     *     
     */
    public BusinessObjectQueryXto getBusinessObjectQuery() {
        return businessObjectQuery;
    }

    /**
     * Sets the value of the businessObjectQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessObjectQueryXto }
     *     
     */
    public void setBusinessObjectQuery(BusinessObjectQueryXto value) {
        this.businessObjectQuery = value;
    }

}
