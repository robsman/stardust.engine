
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepositoryInstanceInfos complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryInstanceInfos">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryInstanceInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryInstanceInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryInstanceInfos", propOrder = {
    "repositoryInstanceInfo"
})
public class RepositoryInstanceInfosXto {

    protected List<RepositoryInstanceInfoXto> repositoryInstanceInfo;

    /**
     * Gets the value of the repositoryInstanceInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the repositoryInstanceInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRepositoryInstanceInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RepositoryInstanceInfoXto }
     * 
     * 
     */
    public List<RepositoryInstanceInfoXto> getRepositoryInstanceInfo() {
        if (repositoryInstanceInfo == null) {
            repositoryInstanceInfo = new ArrayList<RepositoryInstanceInfoXto>();
        }
        return this.repositoryInstanceInfo;
    }

}
