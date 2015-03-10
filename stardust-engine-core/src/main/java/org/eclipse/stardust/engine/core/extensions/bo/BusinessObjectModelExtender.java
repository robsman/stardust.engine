package org.eclipse.stardust.engine.core.extensions.bo;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PRIMARY_KEY_ATT;

import java.util.Collection;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.XPathUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.model.beans.DataPathBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class BusinessObjectModelExtender implements IPartitionMonitor
{

   private static final String INPUT_PREFERENCES_DESCRIPTOR_KEY = "descriptor";

   private static final String INPUT_PREFERENCES_DESCRIPTOR_KEY_LABEL = "InputPreferences_label";

   Logger trace = LogManager.getLogger(BusinessObjectModelExtender.class);

   @Override
   public void userRealmCreated(IUserRealm realm)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void userRealmDropped(IUserRealm realm)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void userCreated(IUser user)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void userEnabled(IUser user)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void userDisabled(IUser user)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void modelDeployed(IModel model, boolean isOverwrite)
         throws DeploymentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void modelDeleted(IModel model) throws DeploymentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void modelLoaded(IModel model)
   {

      ModelElementList<IData> dataList = model.getData();

      for (IData data : dataList)
      {
         // find all Data that are Business Objects
         if (((String) data.getAttribute(PRIMARY_KEY_ATT)) != null
               && StructuredTypeRtUtils.isStructuredType(data.getType().getId()))
         {
            findDescriptorsForBusinessObject(data, model);
         }

      }

   }

   private void findDescriptorsForBusinessObject(IData data, IModel model)
   {
      ITypeDeclaration typeDecl = StructuredTypeRtUtils.getTypeDeclaration(data);

      Set<TypedXPath> xpaths = StructuredTypeRtUtils.getAllXPaths(model, typeDecl);

      for (TypedXPath xpath : xpaths)
      {
         XPathAnnotations annotations = xpath.getAnnotations();

         if (annotations != null)
         {

            String descriptorKeyValue = annotations.getElementValue(
                  XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE, new String[] {
                        "storage", INPUT_PREFERENCES_DESCRIPTOR_KEY});

            String descriptorLabelValue = annotations.getElementValue(
                  XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE, new String[] {
                        "ui", INPUT_PREFERENCES_DESCRIPTOR_KEY_LABEL});

            if (descriptorKeyValue != null && Boolean.parseBoolean(descriptorKeyValue))
            {

               if (descriptorLabelValue == null)
               {
                  descriptorLabelValue = StructuredDataXPathUtils.getLastXPathPart(xpath.getXPath());
               }

               ModelElementList<IProcessDefinition> pds = model.getProcessDefinitions();

               // Add descriptor to all process definitions referencing the BO
               for (IProcessDefinition pd : pds)
               {
                  if (pd.getAttribute("stardust:model:businessObject").equals(
                        typeDecl.getId()))
                  {
                     addDescriptorsToProcessDefinition(pd, data, xpath.getXPath(),
                           descriptorLabelValue);
                  }
               }
            }

         }

      }
   }

   private IProcessDefinition addDescriptorsToProcessDefinition(IProcessDefinition pd,
         IData data, String xpath, String label)
   {

      IDataPath dataPath = new DataPathBean(xpath, label, data, xpath, Direction.IN);
      dataPath.setDescriptor(true);

      pd.addToDataPaths(dataPath);

      return pd;
   }

}
