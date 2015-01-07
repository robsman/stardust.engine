
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RuntimePermissionsMap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuntimePermissionsMap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RuntimePermissionsEntry" type="{http://eclipse.org/stardust/ws/v2012a/api}RuntimePermissionsEntry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuntimePermissionsMap", propOrder = {
    "runtimePermissionsEntry"
})
public class RuntimePermissionsMapXto {

    @XmlElement(name = "RuntimePermissionsEntry")
    protected List<RuntimePermissionsEntryXto> runtimePermissionsEntry;

    /**
     * Gets the value of the runtimePermissionsEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the runtimePermissionsEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRuntimePermissionsEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RuntimePermissionsEntryXto }
     * 
     * 
     */
    public List<RuntimePermissionsEntryXto> getRuntimePermissionsEntry() {
        if (runtimePermissionsEntry == null) {
            runtimePermissionsEntry = new ArrayList<RuntimePermissionsEntryXto>();
        }
        return this.runtimePermissionsEntry;
    }

}
