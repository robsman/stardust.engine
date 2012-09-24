/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an ActivityQuery execution.
 * 			
 * 
 * <p>Java class for ActivityQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivityQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="activityInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityQueryResult", propOrder = {
    "activityInstances"
})
public class ActivityQueryResultXto
    extends QueryResultXto
{

    protected ActivityInstancesXto activityInstances;

    /**
     * Gets the value of the activityInstances property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstancesXto }
     *     
     */
    public ActivityInstancesXto getActivityInstances() {
        return activityInstances;
    }

    /**
     * Sets the value of the activityInstances property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstancesXto }
     *     
     */
    public void setActivityInstances(ActivityInstancesXto value) {
        this.activityInstances = value;
    }

}
