package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;
import org.apache.camel.Exchange;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;
import org.eclipse.stardust.engine.extensions.camel.component.process.subcommand.*;

/**
 * 
 * @author JanHendrik.Scheufen
 */
public class ProcessProducer extends AbstractIppProducer
{
   static Logger LOG = LogManager.getLogger(ProcessProducer.class);

   private ProcessEndpoint endpoint;

   public ProcessProducer(ProcessEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   private AbstractSubCommand determineSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {

      if (SubCommand.Process.COMMAND_FIND.equals(endpoint.getSubCommand()))
      {
         return new FindProcessSubCommand(endpoint, sf);
      }
      // *** START PROCESS ***
      else if (SubCommand.Process.COMMAND_START.equals(endpoint.getSubCommand()))
      {
         return new StartProcessSubCommand(endpoint, sf);
      }
      // *** ATTACH DOCUMENT ***
      else if (SubCommand.Process.COMMAND_ATTACH.equals(endpoint.getSubCommand()))
      {
         return new AttachDocumentSubCommand(endpoint, sf);
      }// *** CONTINUE ***
      else if (SubCommand.Process.COMMAND_CONTINUE.equals(endpoint.getSubCommand()))
      {
         return new ContinueProcessSubCommand(endpoint, sf);
      }// *** SET PROPERTIES ***
      else if (SubCommand.Process.COMMAND_SET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         return new SetPropertiesSubCommand(endpoint, sf);
      }
      else if (SubCommand.Process.COMMAND_GET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         return new GetPropertiesSubCommand(endpoint, sf);
      }
      else if (SubCommand.Process.COMMAND_SPAWN_SUB_PROCESS.equals(endpoint.getSubCommand()))
      {
         return new SpawnSubProcessSubCommand(endpoint, sf);
      }
      return null;
   }

   /**
    * Processes the message exchange
    * 
    * @param exchange
    *           the message exchange
    * @throws Exception
    *            if an internal processing error has occurred.
    */
   public void process(Exchange exchange) throws Exception
   {
      if (getServiceFactory(this.endpoint, exchange) == null)
      {
         throw new MissingEndpointException("Authentication Endpoint is missing. You have to specify one.");
      }
      ServiceFactory sf = getServiceFactory(this.endpoint, exchange);
      AbstractSubCommand subCommand = determineSubCommand(endpoint, sf);
      subCommand.process(exchange);
   }
   // /**
   // * creates new folder based on the input parameters This function creates a single
   // * folder on the provided path which must be valid
   // *
   // * @param folderPath
   // * @param folderName
   // * @return
   // */
   // public static Folder createFolder(String folderPath, String folderName)
   // {
   // if (null != folderName)
   // {
   // // append
   // folderPath = folderPath + "/" + folderName;
   // }
   // return createFolderIfNotExists(folderPath);
   // }
   // /**
   // * Returns the folder if exist otherwise create new folder
   // *
   // * @param folderPath
   // * @return
   // */
   // public static Folder createFolderIfNotExists(String folderPath)
   // {
   // Folder folder = getDocumentManagementService().getFolder(folderPath,
   // Folder.LOD_NO_MEMBERS);
   //
   // if (null == folder)
   // {
   // // folder does not exist yet, create it
   // String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
   // String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);
   //
   // if (StringUtils.isEmpty(parentPath))
   // {
   // // top-level reached
   // return getDocumentManagementService().createFolder("/",
   // DmsUtils.createFolderInfo(childName));
   // }
   // else
   // {
   // Folder parentFolder = createFolderIfNotExists(parentPath);
   // return getDocumentManagementService().createFolder(parentFolder.getId(),
   // DmsUtils.createFolderInfo(childName));
   // }
   // }
   // else
   // {
   // return folder;
   // }
   // }
   // public static DocumentManagementService getDocumentManagementService()
   // {
   // return ClientEnvironment.getCurrentServiceFactory().getDocumentManagementService();
   // }
   // public static Folder getFolder(String path)
   // {
   // Folder folder = null;
   // String searchString = substringAfterLast(path, "/");
   // searchString = replaceIllegalXpathSearchChars(searchString);
   // List<Folder> newlist =
   // getDocumentManagementService().findFoldersByName(searchString,
   // Folder.LOD_NO_MEMBERS);
   // for (Folder tempFolder : newlist)
   // {
   // if (path.equalsIgnoreCase(tempFolder.getPath()))
   // {
   // folder = tempFolder;
   // break;
   // }
   // }
   //
   // return folder;
   // }
   // /**
   // * @param source
   // * @param separator
   // * @return
   // */
   // public static final String substringAfterLast(String source, String separator)
   // {
   // if (org.eclipse.stardust.common.StringUtils.isEmpty(source))
   // {
   // return source;
   // }
   // if (org.eclipse.stardust.common.StringUtils.isEmpty(separator))
   // {
   // return "";
   // }
   // int pos = source.lastIndexOf(separator);
   // if (pos == -1 || pos == (source.length() - separator.length()))
   // {
   // return "";
   // }
   // return source.substring(pos + separator.length());
   // }
   // public static String replaceIllegalXpathSearchChars(String s)
   // {
   // return s.replaceAll("'", "%");
   // }
}