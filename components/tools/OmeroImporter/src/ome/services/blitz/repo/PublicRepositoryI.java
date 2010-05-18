/*
 * ome.services.blitz.repo.PublicRepositoryI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 * 
 */
package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;

import javax.activation.MimetypesFileTypeMap;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import ome.formats.importer.ImportContainer;
import ome.services.blitz.util.RegisterServantMessage;
import ome.services.db.PgArrayHelper;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.xml.r201004.primitives.PositiveInteger;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawFileStorePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.FileSet;
import omero.grid.RepositoryListConfig;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.model.DimensionOrder;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.util.IceMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import Ice.Current;

/**
 * An implementation of he PublicRepository interface
 * 
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PublicRepositoryI extends _RepositoryDisp {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    private final static String OMERO_PATH = ".omero";

    private final static String THUMB_PATH = "thumbnails";

    private final long id;

    private final File root;

    private final PgArrayHelper helper;

    private final Executor executor;

    private final SimpleJdbcOperations jdbc;

    private final Principal principal;
    
    private Map<String,DimensionOrder> dimensionOrderMap;
    
    private String repoUuid;

    public PublicRepositoryI(File root, long repoObjectId, Executor executor,
            SimpleJdbcOperations jdbc, Principal principal, PgArrayHelper helper) throws Exception {
        this.id = repoObjectId;
        this.executor = executor;
        this.jdbc = jdbc;
        this.principal = principal;
        this.helper = helper;

        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.root = root.getAbsoluteFile();
        this.repoUuid = null;
        this.dimensionOrderMap = null;
        
    }

    public OriginalFile root(Current __current) throws ServerError {
        return new OriginalFileI(this.id, false); // SHOULD BE LOADED.
    }

    /**
     * Register an OriginalFile using its path
     *
     * @param path
     *            Absolute path of the file to be registered.
     * @param mimetype
     *            Mimetype as an RString
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set (unloaded)
     *
     */
    public OriginalFile register(String path, omero.RString mimetype, Current __current)
            throws ServerError {

        File file = new File(path).getAbsoluteFile();
        OriginalFile omeroFile = new OriginalFileI();
        omeroFile = createOriginalFile(file, mimetype);

        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(omeroFile);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register", path) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeFile).getId();
            }
        });

        omeroFile.setId(rlong(id));
        omeroFile.unload();
        return omeroFile;

    }
    
    /**
     * Register an IObject object
     * 
     * @param obj
     *            IObject object.
     * @param params
     *            Map<String, String>
     * @param __current
     *            ice context.
     * @return The IObject with id set
     *
     */
    public IObject registerObject(IObject obj, Map<String, String> params, Current __current)
            throws ServerError {

        if (obj == null) {
            throw new ValidationException(null, null,
                    "obj is required argument");
        }
        if (!(obj instanceof OriginalFile)) {
            throw new ValidationException(null, null,
                    "obj must be OriginalFile (Image objects can no longer be registered using this method)");
        }

        IceMapper mapper = new IceMapper();
        final ome.model.IObject omeObj = (ome.model.IObject) mapper.reverse(obj);
                
        final String repoId = getRepoUuid();
        final Map<String, String> paramMap = params;

        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "registerObject", repoId) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                long id = sf.getUpdateService().saveAndReturnObject(omeObj).getId();
                if (omeObj instanceof ome.model.core.OriginalFile) {
                    jdbc.update("update originalfile set repo = ? where id = ?", repoId, id);
                    helper.setFileParams(id, paramMap);
                } else { // must be an Image object here.
                    ome.model.IObject result = sf.getQueryService().findByQuery("select p from Pixels p where p.image = " + id, null);
                    long pixId = result.getId();                  
                    jdbc.update("update pixels set repo = ? where id = ?", repoId, pixId);
                    helper.setPixelsParams(pixId, paramMap);
                    return id;

                }
                return id;
            }
        });
        
        obj.setId(rlong(id));
        return obj;
    }

    /**
     * Register the Images in a FileSet
     * 
     * @param set
     *            FileSet object.
     * @param params
     *            Map<String, String>
     * @param __current
     *            ice context.
     * @return The FileSet with Image ids set (unloaded)
     *
     */
    public FileSet registerFileSet(FileSet set, Map<String, String> params, Current __current) 
            throws ServerError {
        
        if (set == null) {
            throw new ValidationException(null, null,
                    "fileSet is a required argument");
        }
        if (!set.importableImage) {
            throw new ValidationException(null, null,
                    "fileSet is not importable");
        }
        
        File f = new File(set.fileName);
        final String path = getRelativePath(f);
        final String name = f.getName(); 
        final String repoId = getRepoUuid();
        
        long imageCount = 0;
        for (IObject obj : set.imageList) {
            
            params.put("image_no", Long.toString(imageCount));
            
            IceMapper mapper = new IceMapper();
            final ome.model.IObject omeObj = (ome.model.IObject) mapper.reverse(obj);
                    
            final Map<String, String> paramMap = params;
            
            Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                    this, "registerFileSet", repoId) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    long id = sf.getUpdateService().saveAndReturnObject(omeObj).getId();
                    ome.model.IObject result = sf.getQueryService().findByQuery("select p from Pixels p where p.image = " + id, null);
                    long pixId = result.getId();                  
                    jdbc.update("update pixels set name = ? where id = ?", name, pixId);
                    jdbc.update("update pixels set path = ? where id = ?", path, pixId);
                    jdbc.update("update pixels set repo = ? where id = ?", repoId, pixId);
                    helper.setPixelsParams(pixId, paramMap);
                    return id;
                }
            });
            obj.setId(rlong(id));
            obj.unload();
            imageCount++;
        }
        return set;
    }



    /**
     * Register the Images in a list of Images
     * 
     * @param filename
     *            The absolute path of the parent file.
     * @param imageList
     *            A list of Image objects.
     * @param params
     *            Map<String, String>
     * @param __current
     *            ice context.
     * @return A List of Images with ids set
     *
     */
    public List<Image> registerImageList(String filename, List<Image> imageList, Map<String, String> params, Current __current) 
            throws ServerError {
        
        if (imageList == null || imageList.size() == 0) {
            throw new ValidationException(null, null,
                    "imageList is a required argument and cannot be empty");
        }
        File f = checkPath(filename);
        final String path = getRelativePath(f);
        final String name = f.getName(); 
        final String repoId = getRepoUuid();
        
        long imageCount = 0;
        for (IObject obj : imageList) {
            
            params.put("image_no", Long.toString(imageCount));
            
            IceMapper mapper = new IceMapper();
            final ome.model.IObject omeObj = (ome.model.IObject) mapper.reverse(obj);
                    
            final Map<String, String> paramMap = params;
            
            Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                    this, "registerImageList", repoId) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    long id = sf.getUpdateService().saveAndReturnObject(omeObj).getId();
                    ome.model.IObject result = sf.getQueryService().findByQuery("select p from Pixels p where p.image = " + id, null);
                    long pixId = result.getId();                  
                    jdbc.update("update pixels set name = ? where id = ?", name, pixId);
                    jdbc.update("update pixels set path = ? where id = ?", path, pixId);
                    jdbc.update("update pixels set repo = ? where id = ?", repoId, pixId);
                    helper.setPixelsParams(pixId, paramMap);
                    return id;
                }
            });
            obj.setId(rlong(id));
            imageCount++;
        }
        return imageList;
    }

    public void delete(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        FileUtils.deleteQuietly(file);
    }

    @SuppressWarnings("unchecked")
    
    /**
     * Get a list of all files and directories at path.
     * 
     * @param path
     *            A path on a repository.
     * @param config
     *            A RepositoryListConfig defining the listing config.
     * @param __current
     *            ice context.
     * @return List of OriginalFile objects at path
     *
     */
    public List<OriginalFile> listFiles(String path, RepositoryListConfig config, Current __current) throws ServerError {
        File file = checkPath(path);
        List<File> files;
        List<OriginalFile> oFiles;
        RepositoryListConfig conf;
        
        if(config == null) {
            conf = new RepositoryListConfig(1, true, true, false, true);
        } else {
            conf = config;
        }
        files = filteredFiles(file, conf);
        oFiles = filesToOriginalFiles(files);
        if (conf.registered) {
            oFiles = knownOriginalFiles(oFiles);
        }
        return oFiles;
    }

    /**
     * Get a list of those files as importable and non-importable list.
     * 
     * @param path
     *            A path on a repository    
     * @param config
     *            A RepositoryListConfig defining the listing config.
     * @param __current
     *            ice context.
     * @return A List of FileSet objects.
     * 
     * The map uses the object name as key. This is the file name but should be something
     * guaranteed to be unique. 
     *
     */
     public List<FileSet> listFileSets(String path, RepositoryListConfig config, Current __current)
            throws ServerError {
        File file = checkPath(path);
        RepositoryListConfig conf;
        
        if(config == null) {
            conf = new RepositoryListConfig(1, true, true, false, true);
        } else {
            conf = config;
        }
        List<File> files = filteredFiles(file, conf);
        List<String> names = filesToPaths(files);
        List<ImportContainer> containers = importableImageFiles(path, conf.depth);
        List<FileSet> rv = processImportContainers(containers, names);
        
        return rv;
    }
    
    
    /**
     * Get the format object for a file.
     * 
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return Format object
     *
     */
    public Format format(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        throw new omero.InternalException(null, null, "ticket:2211 - For Colin");
    }

    /**
     * Get (the path of) the thumbnail image for an image file on the repository.
     * 
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return The path of the thumbnail
     *
     */
    public String getThumbnail(String path, Current __current)  throws ServerError {
        File file = checkPath(path);
        String tnPath;
        try {
            tnPath = createThumbnail(file);   
        } catch (ServerError exc) {
            throw exc;
        }
        return tnPath;
    }

    /**
     * Get (the path of) the thumbnail image for an image file on the repository.
     * 
     * @param path
     *            A path on a repository.
     * @param imageIndex
     *            The index of an image in a multi-image file set.
     * @param __current
     *            ice context.
     * @return The path of the thumbnail
     *
     */
    public String getThumbnailByIndex(String path, int imageIndex, Current __current)  throws ServerError {
        File file = checkPath(path);
        String tnPath;
        try {
            tnPath = createThumbnail(file, imageIndex);   
        } catch (ServerError exc) {
            throw exc;
        }
        return tnPath;
    }




    /**
     *
     * Interface methods yet TODO
     *
     */
    public OriginalFile load(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawPixelsStorePrx pixels(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawFileStorePrx file(long fileId, Current __current) throws ServerError {
        File file = getFile(fileId);
        if (file == null) {
            return null;
        }

        // WORKAROUND: See the comment in RawFileStoreI.
        // The most likely correction of this
        // is to have PublicRepositories not be global objects, but be created
        // on demand for each session via SharedResourcesI
        Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.operation = __current.operation;
        String sessionUuid = __current.ctx.get("omero.session");
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);

        // TODO: Refactor all this into a single helper method.
        // If there is no listener available who will take responsibility
        // for this servant, then we bail.
        RepoRawFileStoreI rfs = new RepoRawFileStoreI(fileId, file);
        RegisterServantMessage msg = new RegisterServantMessage(this, rfs, adjustedCurr);
        try {
            this.executor.getContext().publishMessage(msg);
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }
        Ice.ObjectPrx prx = msg.getProxy();
        if (prx == null) {
            throw new omero.InternalException(null, null, "No ServantHolder for proxy.");
        }
        return RawFileStorePrxHelper.uncheckedCast(prx);
    }

    public RawFileStorePrx read(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void rename(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public RenderingEnginePrx render(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx thumbs(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void transfer(String srcPath, RepositoryPrx target,
            String targetPath, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public RawFileStorePrx write(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }



    /**
     *
     * Utility methods
     *
     */

    /**
     * Get the file object at a path.
     * 
     * @param path
     *            A path on a repository.
     * @return File object
     *
     */
    private File checkPath(String path) throws ValidationException {

        if (path == null || path.length() == 0) {
            throw new ValidationException(null, null, "Path is empty");
        }

        boolean found = false;
        File file = new File(path).getAbsoluteFile();
        while (true) {
            if (file.equals(root)) {
                found = true;
                break;
            }
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }

        if (!found) {
            throw new ValidationException(null, null, path + " is not within "
                    + root.getAbsolutePath());
        }

        return new File(path).getAbsoluteFile();
    }

   /**
     * Get a filtered file listing based on the config options.
     * 
     * @param file
     *            A File object representing the directory to be listed.
     * @param config 
     *            A RepositoryListConfig object holding the filter options.
     * @return A list of File objects
     *
     */
    private List<File> filteredFiles(File file, RepositoryListConfig config) throws ServerError {
        List<File> files;
        IOFileFilter filter;
        
        // If hidden is true list all files otherwise only those files not starting with "."
        if (config.hidden) {
            filter = FileFilterUtils.trueFileFilter();
        } else {
            filter = FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("."));
        }
        
        // Now decorate the filter to restrict to files or directories,
        // the else case is for a bizarre config of wanting nothing returned!
        if (!(config.dirs && config.files)) {
            if (config.dirs) {
                filter = FileFilterUtils.makeDirectoryOnly(filter);
            } else if (config.files) {
                filter = FileFilterUtils.makeFileOnly(filter);
            } else {
                filter = FileFilterUtils.falseFileFilter();
            }
        }
        
        files = Arrays.asList(file.listFiles((FileFilter)filter));
        
        return files;
    }
    
    /**
     * Get the mimetype for a file.
     * 
     * @param file
     *            A File in a repository.
     * @return A String representing the mimetype.
     *
     * TODO Return the correct Format object in place of a dummy one
     */
    private String getMimetype(File file) {

        final String contentType = new MimetypesFileTypeMap().getContentType(file);
        return contentType;

    }

    /**
     * Get the DimensionOrder
     * 
     * @param String
     *            A string representing the dimension order
     * @return A DimensionOrder object
     *
     * The HashMap is built on the first call.
     * TODO: Move that build to constructor?
     */
    private DimensionOrder getDimensionOrder(String dimensionOrder) {
        if (dimensionOrderMap == null) {
            dimensionOrderMap = buildDimensionOrderMap();
        }
        return dimensionOrderMap.get(dimensionOrder);
    }

    /**
     * Get OriginalFile objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of new OriginalFile objects
     *
     */
    private List<OriginalFile> filesToOriginalFiles(Collection<File> files) {
        List<OriginalFile> rv = new ArrayList<OriginalFile>();
        for (File f : files) {
            rv.add(createOriginalFile(f));
        }
        return rv;
    }

    /**
     * Get file paths corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of path Strings
     *
     */
    private List<String> filesToPaths(Collection<File> files) {
        List<String> rv = new ArrayList<String>();
        for (File f : files) {
            rv.add(f.getAbsolutePath());
        }
        return rv;
    }

    /**
     * Get registered OriginalFile objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of OriginalFile objects.
     * @return A list of registered OriginalFile objects. 
     *
     */
    private List<OriginalFile> knownOriginalFiles(Collection<OriginalFile> files)  {
        List<OriginalFile> rv = new ArrayList<OriginalFile>();
        for (OriginalFile f : files) {
            OriginalFile oFile = getOriginalFile(f.getPath().getValue(), f.getName().getValue());
            if (oFile != null) {
                rv.add(oFile);
            } else {
                rv.add(f);
            }
        }
        return rv;
    }

    
    private  List<ImportContainer> importableImageFiles(String path, int depth) {
        String paths [] = {path};
        ImportableFiles imp = new ImportableFiles(paths, depth);
        List<ImportContainer> containers = imp.getContainers();
        return containers;
    }

    private List<FileSet> processImportContainers(List<ImportContainer> containers, List<String> names) {
        List<FileSet> rv = new ArrayList<FileSet>();

        for (ImportContainer ic : containers) {
            FileSet set = new FileSet();
            OriginalFile oFile;
            
            set.importableImage = true;
            set.fileName = ic.getFile().getAbsolutePath();
            set.hidden = ic.getFile().isHidden();
            set.dir = ic.getFile().isDirectory();
            set.reader = ic.getReader();
            set.imageCount = ic.getBfImageCount();
                        
            set.usedFiles = new ArrayList<IObject>();
            List<String> iFileList = Arrays.asList(ic.getUsedFiles());
            for (String iFile : iFileList)  {
                File f = new File(iFile);
                removeNameFromFileList(iFile, names);
                oFile = getOriginalFile(getRelativePath(f),f.getName());
                if (oFile != null) {
                    set.usedFiles.add(oFile);
                } else {
                    set.usedFiles.add(createOriginalFile(f));   
                }
            }
            
            int i = 0;
            set.imageList = new ArrayList<Image>();
            List<String> iNames = ic.getBfImageNames();
            for (Pixels pix : ic.getBfPixels())  {
                Image image;
                String imageName;
                pix = createPixels(pix);
                imageName = iNames.get(i);
                if (imageName == null || imageName == "") {
                    imageName = "UNKNOWN";
                }
                image = getImage(set.fileName, i);
                if (image == null) {
                    image = createImage(imageName, pix);   
                }
                set.imageList.add(image);
                i++;
            }
            rv.add(set);
        }
        
        // Add the left over files in the directory as OrignalFile objects
        if (names.size() > 0) {
            for (String iFile : names) {
                File f = new File(iFile);
                FileSet set = new FileSet();
                OriginalFile oFile;
            
                set.importableImage = false;
                set.fileName = iFile;
                set.hidden = f.isHidden();
                set.dir = f.isDirectory();
                set.imageCount = 0;
                        
                set.usedFiles = new ArrayList<IObject>();
                oFile = getOriginalFile(getRelativePath(f),f.getName());
                if (oFile != null) {
                    set.usedFiles.add(oFile);
                } else {
                    set.usedFiles.add(createOriginalFile(f));   
                }
                rv.add(set);
            }
        }
        
        return rv;
    }

    /**
     * Create an OriginalFile object corresponding to a File object 
     * 
     * @param f
     *            A File object.
     * @return An OriginalFile object
     *
     */
    private OriginalFile createOriginalFile(File f) {
        String mimetype = getMimetype(f);
        return createOriginalFile(f, rstring(mimetype));
    }
    
    /**
     * Create an OriginalFile object corresponding to a File object 
     * using the user supplied mimetype string
     * 
     * @param f
     *            A File object.
     * @param mimetype
     *            Mimetype as an RString
     * @return An OriginalFile object
     *
     * TODO populate more attribute fields than the few set here?
     */
    private OriginalFile createOriginalFile(File f, omero.RString mimetype) {
        OriginalFile file = new OriginalFileI();
        file.setName(rstring(f.getName()));
        // This first case deals with registerng the repos themselves.
        if (f.getAbsolutePath().equals(root.getAbsolutePath())) {
            file.setPath(rstring(f.getParent()));
        } else { // Path should be relative to root?
            file.setPath(rstring(getRelativePath(f)));
        }
        file.setSha1(rstring("UNKNOWN"));
        file.setMimetype(mimetype);
        file.setMtime(rtime(f.lastModified()));
        file.setSize(rlong(f.length()));
        // Any other fields?
        
        return file;
    }
    
    /**
     * Create an Image object corresponding to an imagename and pixels object.
     * 
     * @param imageName
     *            A String.
     * @param pix
     *            A Pixels object.
     * @return An Image object
     *
     */
    private Image createImage(String imageName, Pixels pix) {
        Image image = new ImageI();
        image.setName(rstring(imageName));        
        image.setAcquisitionDate(rtime(java.lang.System.currentTimeMillis()));
        image.addPixels(pix);
        return image;
    }

    /**
     * Create a fuller Pixels object from a Pixels object.
     * 
     * @param pix
     *            A Pixels object.
     * @return An Pixels object
     *
     */
    private Pixels createPixels(Pixels pix) {
        // Use the same for all Pixels for now.
        DimensionOrder dimOrder = getDimensionOrder("XYZCT");
        pix.setDimensionOrder(dimOrder);
        pix.setSha1(rstring("UNKNOWN"));
        return pix;
    }

    
    /**
     * Get an {@link OriginalFile} object at the given path and name. Returns null if
     * the OriginalFile does not exist or does not belong to this repo.
     * 
     * @param path
     *            A path to a file.
     * @return OriginalFile object.
     *
     */
    private OriginalFile getOriginalFile(final String path, final String name)  {
        OriginalFile rv;
        final String uuid = getRepoUuid();
        ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile) executor
                .execute(principal, new Executor.SimpleWork(this, "getOriginalFile", uuid, path, name) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        BigInteger id = (BigInteger) session.createSQLQuery(
                                "select id from OriginalFile " +
                                "where path = ? and name = ? and repo = ?")
                                .setParameter(0, path)
                                .setParameter(1, name)
                                .setParameter(2, uuid)
                                .uniqueResult();
                        if (id == null) {
                            return null;
                        } 
                        return sf.getQueryService().find(ome.model.core.OriginalFile.class, id.longValue());
                    }
                });
        if (oFile == null)
        {
            return null;
        }
        IceMapper mapper = new IceMapper();
        rv = (OriginalFile) mapper.map(oFile);
        return rv;
    }

    /**
     * Get an {@link OriginalFile} object based on its id. Returns null if
     * the file does not exist or does not belong to this repo.
     *
     * @param id
     *            long, db id of original file.
     * @return OriginalFile object.
     *
     */
    private File getFile(final long id) {
        final String uuid = getRepoUuid();
        return (File) executor.execute(principal, new Executor.SimpleWork(this, "getFile", id) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                            String path = (String) session.createSQLQuery(
                                    "select path || name from OriginalFile " +
                                    "where id = ? and repo = ?")
                                    .setParameter(0, id)
                                    .setParameter(1, uuid)
                                    .uniqueResult();

                            if (path == null) {
                                return null;
                            }

                            return new File(root, path);
                    }
                });
    }

    /**
     * Get an Image with path corresponding to the paramater path.
     * 
     * @param path
     *            A path to a file.
     * @return List of Image objects, empty if the query returned no values.
     *
     * TODO Broken at present, params is not checked.
     */
    private Image getImage(String fullPath, final long count)  {

        File f = new File(fullPath);
        final String uuid = getRepoUuid();
        final String path = getRelativePath(f);
        final String name = f.getName();
        
        ome.model.core.Image image = (ome.model.core.Image) executor
                .execute(principal, new Executor.SimpleWork(this, "getImage") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        List<BigInteger> pixIds = session.createSQLQuery(
                                "select id from Pixels " +
                                "where path = ? and name = ? and repo = ?")
                                .setParameter(0, path)
                                .setParameter(1, name)
                                .setParameter(2, uuid)
                                .list();
                        if (pixIds == null || pixIds.size() == 0) {
                            return null;
                        }
                        
                        //Map<String, String> params = helper.getPixelsParams(pixIds.get(0).longValue());
                        BigInteger imageId = (BigInteger) session.createSQLQuery(
                                "select image from Pixels " +
                                "where id = ?")
                                .setParameter(0, pixIds.get((int)count))
                                .uniqueResult();

                        return sf.getQueryService().find(ome.model.core.Image.class, imageId.longValue());
                    }
                });
            
        if (image == null)
        {
            return null;
        }
        IceMapper mapper = new IceMapper();
        Image rv = (Image) mapper.map(image);
        return rv;
    }

    /**
     * Create a jpeg thumbnail from an image file using the zeroth image
     * 
     * @param path
     *            A path to a file.
     * @return The path of the thumbnail
     *
     */
    private String createThumbnail(File file) throws ServerError {
        return createThumbnail(file, 0);
    }

    /**
     * Create a jpeg thumbnail from an image file using the nth image
     * 
     * @param path
     *            A path to a file.
     * @param imageIndex
     *            the image index in a multi-image file.
     * @return The path of the thumbnail
     *
     * TODO Weak at present, no caching
     */
    private String createThumbnail(File file, int imageIndex) throws ServerError {
        // Build a path to the thumbnail
        File parent = file.getParentFile();
        File tnParent = new File(new File(parent, OMERO_PATH), THUMB_PATH);
        tnParent.mkdirs(); // Need to check if this is created?
        File tnFile = new File(tnParent, file.getName() + "_" + Integer.toString(imageIndex) + "_tn.jpg");
        // Very basic caching... if a file exists return it.
        if (tnFile.exists()) {
            return tnFile.getAbsolutePath();
        }
        
        // First get the thumb bytes from the image file  
        IFormatReader reader = new ImageReader();
        byte[] thumb;
        reader.setNormalized(true);
        try {
            reader.setId(file.getAbsolutePath());
            reader.setSeries(imageIndex);
            // open middle image thumbnail
            int z = reader.getSizeZ() / 2;
            int t = reader.getSizeT() / 2;
            int ndx = reader.getIndex(z, 0, t);
            thumb = reader.openThumbBytes(ndx); 
        } catch (FormatException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, read failed."); 
        } catch (IOException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, read failed."); 
        }
        
        // Next create the metadata for the thumbnail image file.
        // How much of this is needed for a jpeg? 
        // At present provides monochrome images for some formats, need to provide colour?
        IMetadata meta = null;
        try {
            // Fully qualified to avoid collisions with OMERO service factory
            loci.common.services.ServiceFactory sf =
                new loci.common.services.ServiceFactory();
            meta = sf.getInstance(OMEXMLService.class).createOMEXMLMetadata();
        } catch (DependencyException e) {
            throw new ServerError(null, stackTraceAsString(e),
                    "Thumbnail error, could not create OME-XML service.");
        } catch (ServiceException e) {
            throw new ServerError(null, stackTraceAsString(e),
                    "Thumbnail error, could not create OME-XML metadata.");
        }
        int thumbSizeX = reader.getThumbSizeX();
        int thumbSizeY = reader.getThumbSizeY();  
        meta.createRoot();
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder(ome.xml.r201004.enums.DimensionOrder.XYZCT, 0);
        meta.setPixelsType(ome.xml.r201004.enums.PixelType.UINT8, 0);
        meta.setPixelsSizeX(new PositiveInteger(thumbSizeX), 0);
        meta.setPixelsSizeY(new PositiveInteger(thumbSizeY), 0);
        meta.setPixelsSizeZ(new PositiveInteger(1), 0);
        meta.setPixelsSizeC(new PositiveInteger(1), 0);
        meta.setPixelsSizeT(new PositiveInteger(1), 0);
        meta.setChannelSamplesPerPixel(1, 0, 0);
        
        // Finally try to create the jpeg file abd return the path.  
        IFormatWriter writer = new ImageWriter();
        writer.setMetadataRetrieve(meta);
        try {
            writer.setId(tnFile.getAbsolutePath());
            writer.saveBytes(thumb, true);
            writer.close();  
        } catch (FormatException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        } catch (IOException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        }
        
        return tnFile.getAbsolutePath();
	}

    /**
     * A getter for the repoUuid. 
     * This is run once by getRepoUuid() when first needed, 
     * thereafter lookups are local.
     *
     * TODO: this should probably be done in the constructor?
     */
	private String getRepoUuid() {
	    if (this.repoUuid == null) {
            final long repoId = this.id;
            ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
                .execute(principal, new Executor.SimpleWork(this, "getRepoUuid") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().find(ome.model.core.OriginalFile.class, repoId);
                    }
                });
            OriginalFileI file = (OriginalFileI) new IceMapper().map(oFile);
            this.repoUuid = file.getSha1().getValue(); 
        }
        return this.repoUuid; 
    }

    /**
     * Utility to a build map of DimensionOrder objects keyed by value.
     * This is run once by getDimensionOrder() when first needed, 
     * thereafter lookups are local.
     *
     * TODO: this should probably be done in the constructor?
     */
    private Map<String, DimensionOrder> buildDimensionOrderMap() {
        List <DimensionOrder> dimensionOrderList;
        List<ome.model.enums.DimensionOrder> dimOrderList = (List<ome.model.enums.DimensionOrder>) executor
                .execute(principal, new Executor.SimpleWork(this, "buildDimensionOrderMap") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery("from DimensionOrder as d",
                                null);
                    }
                });
            
        IceMapper mapper = new IceMapper();
        dimensionOrderList = (List<DimensionOrder>) mapper.map(dimOrderList);

        Map<String, DimensionOrder> dimensionOrderMap = new HashMap<String, DimensionOrder>();
        for (DimensionOrder dimensionOrder : dimensionOrderList) {
            dimensionOrderMap.put(dimensionOrder.getValue().getValue(), dimensionOrder);
        }
        return dimensionOrderMap;
    }
    
    private String getRelativePath(File f) {
        String path = f.getParent()
                .substring(root.getAbsolutePath().length(), f.getParent().length());
        // The parent doesn't contain a trailing slash.
        path = path + "/";
        return path;
    }

    /** 
     * Utility to remove a string from a list of strings if it exists.
     * 
     */
    private void removeNameFromFileList(String sText, List<String> sList) {
        int index;
        for(index = 0; index < sList.size(); index ++) {
            if (sText.equals(sList.get(index))) break;
        }
        if (index < sList.size()) sList.remove(index);
    }
    
    // Utility function for passing stack traces back in exceptions.
    private String stackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
   
}
