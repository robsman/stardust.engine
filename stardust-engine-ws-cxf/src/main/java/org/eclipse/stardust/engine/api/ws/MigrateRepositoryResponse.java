
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="repositoryMigrationReport" type="{http://eclipse.org/stardust/ws/v2012a/api}RepositoryMigrationReport"/>
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
    "repositoryMigrationReport"
})
@XmlRootElement(name = "migrateRepositoryResponse")
public class MigrateRepositoryResponse {

    @XmlElement(required = true, nillable = true)
    protected RepositoryMigrationReportXto repositoryMigrationReport;

    /**
     * Gets the value of the repositoryMigrationReport property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryMigrationReportXto }
     *     
     */
    public RepositoryMigrationReportXto getRepositoryMigrationReport() {
        return repositoryMigrationReport;
    }

    /**
     * Sets the value of the repositoryMigrationReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryMigrationReportXto }
     *     
     */
    public void setRepositoryMigrationReport(RepositoryMigrationReportXto value) {
        this.repositoryMigrationReport = value;
    }

}
