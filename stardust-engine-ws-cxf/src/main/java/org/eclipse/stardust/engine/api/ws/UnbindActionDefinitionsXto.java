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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        Contains all the unbind actions registered on the event handler.
 * 	        
 * 
 * <p>Java class for UnbindActionDefinitions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnbindActionDefinitions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="unbindAction" type="{http://eclipse.org/stardust/ws/v2012a/api}EventActionDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnbindActionDefinitions", propOrder = {
    "unbindAction"
})
public class UnbindActionDefinitionsXto {

    protected List<EventActionDefinitionXto> unbindAction;

    /**
     * Gets the value of the unbindAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unbindAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnbindAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventActionDefinitionXto }
     * 
     * 
     */
    public List<EventActionDefinitionXto> getUnbindAction() {
        if (unbindAction == null) {
            unbindAction = new ArrayList<EventActionDefinitionXto>();
        }
        return this.unbindAction;
    }

}
