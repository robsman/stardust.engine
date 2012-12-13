package org.eclipse.stardust.engine.api.query;

/**
 * Allows to limit a {@link DocumentQuery} to a specified subfolder.
 * 
 * @author roland.stamm
 * 
 */
public class SubFolderPolicy implements EvaluationPolicy
{

   private static final long serialVersionUID = 1664905641955289062L;
   
   private final String limitSubFolder;

   private final boolean recursive;

   /**
    * Limits the search to the specified subfolder and its subfolders.
    * <p>
    * <b>Examples:</b> "/subfolder1", "/subfolder1/subfolder2", "/subfolder1//subfolder5"
    * 
    * @param limitSubFolder
    *           The folder name or path that the query should be limited to.
    */
   public SubFolderPolicy(String limitSubFolder)
   {
      this.limitSubFolder = limitSubFolder;
      this.recursive = true;
   }

   /**
    * Limits the search to the specified subfolder and optionally its subfolders.
    * <p>
    * With <code>recursive</code> set to <code>false</code> the search includes only
    * documents in the specified subfolder.
    * <p>
    * <b>Examples:</b> "/subfolder1", "/subfolder1/subfolder2", "/subfolder1//subfolder5"
    * 
    * @param limitSubFolder
    *           The folder name or path that the query should be limited to.
    * @param recursive
    *           Specifies if folders below the limited subfolder should be included in the
    *           search.
    */
   public SubFolderPolicy(String limitSubFolder, boolean recursive)
   {
      this.limitSubFolder = limitSubFolder;
      this.recursive = recursive;
   }

   /**
    * @return The folder name or path that the query should be limited to.
    */
   public String getLimitSubFolder()
   {
      return limitSubFolder;
   }

   public boolean isRecursive()
   {
      return recursive;
   }

}
