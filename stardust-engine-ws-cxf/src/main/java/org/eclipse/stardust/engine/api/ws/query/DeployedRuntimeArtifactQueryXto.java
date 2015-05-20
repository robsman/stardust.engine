
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *       Query container for building complex queries for runtime artifacts.
 *       
 * 
 * <p>Java-Klasse für DeployedRuntimeArtifactQuery complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DeployedRuntimeArtifactQuery">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}Query">
 *       &lt;sequence>
 *         &lt;element name="includeOnlyActive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeployedRuntimeArtifactQuery", propOrder = {
    "includeOnlyActive"
})
public class DeployedRuntimeArtifactQueryXto
    extends QueryXto
{

    protected Boolean includeOnlyActive;

    /**
     * Ruft den Wert der includeOnlyActive-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeOnlyActive() {
        return includeOnlyActive;
    }

    /**
     * Legt den Wert der includeOnlyActive-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeOnlyActive(Boolean value) {
        this.includeOnlyActive = value;
    }

}
