/*
 * ome.formats.testclient.ImportLibrary
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import loci.formats.FormatException;
import loci.common.DataTools;
import ome.formats.OMEROMetadataStoreClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.importer.util.Actions;


import omero.ServerError;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;

/**
 * support class for the proper usage of {@link OMEROMetadataStoreClient} and
 * {@link FormatReader} instances. This library was factored out of
 * {@link ImportHandler} to support {@link ImportFixture} The general workflow
 * for this class (as seen in {@link ImportFixture} is: <code>
 *   ImportLibrary library = new ImportLibrary(store,reader,files);
 *   for (File file : files) {
 *     String fileName = file.getAbsolutePath();
 *     library.open(fileName);
 *     int count = library.calculateImageCount(fileName);
 *     long pixId = library.importMetadata();
 *     library.importData(pixId, fileName, new ImportLibrary.Step(){
 *       public void step(int i) {}});
 *   }
 * </code>
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see FormatReader
 * @see OMEROMetadataStoreClient
 * @see ImportHandler
 * @see ImportFixture
 * @since 3.0-M3
 */
public class ImportLibrary implements IObservable
{
   
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    /**
     * simple action class to be used during
     * {@link ImportLibrary#importData(long, String, ome.formats.testclient.ImportLibrary.Step)}
     */
    public abstract static class Step
    {

        public abstract void step(int series, int step);
    }

    private boolean dumpPixels = false;
    
    private static Log         log = LogFactory.getLog(ImportLibrary.class);

    private IObject            target;

    private OMEROMetadataStoreClient store;

    private OMEROWrapper       reader;

    private ImportContainer[]  fads;

    private int                sizeZ;

    private int                sizeT;

    private int                sizeC;
    
    private int                sizeX;
    
    private int                sizeY;

    private int                zSize;

    private int                tSize;

    private int                wSize;

    /**
     * @param store not null
     * @param reader not null
     */
    public ImportLibrary(OMEROMetadataStoreClient store, OMEROWrapper reader)
    {
        if (store == null || reader == null)
        {
            throw new NullPointerException(
                    "All arguments to ImportLibrary() must be non-null.");
        }
        
        this.store = store;
        this.reader = reader;
    }

    /**
     * Sets the target to which images will be imported. Must be called before
     * {@link #importMetadata()}.
     * 
     * @param target Target object to be linked to.
     */
    public void setTarget(IObject target)
    {
        this.target = target;
    }
    
    public String[] getUsedFiles()
    {
        return reader.getUsedFiles();
    }

    
    // ~ Getters
    // =========================================================================

    /**
     * Returns the current target to be linked to imported images.
     * @return See above.
     */
    public IObject getTarget()
    {
        return target;
    }

    /** simpler getter for {@link #files} */
    public ImportContainer[] getFilesAndDatasets()
    {
        return fads;
    }

    /** gets {@link Image} instance from {@link OMEROMetadataStoreClient} */
    @SuppressWarnings("unchecked")
	public List<Pixels> getRoot()
    {
        return (List<Pixels>) store.getRoot();
    }

    // ~ Actions
    // =========================================================================


    /** opens the file using the {@link FormatReader} instance */
    public void open(String fileName) throws IOException, FormatException
    {
        /* test code ------
        Object[] args;
        
        args = new Object[1];
        args[0] = fileName;
        
        try {
            reader.setId(fileName);
            //reset series count
            log.debug("Image Count: " + reader.getImageCount());
        } catch (java.io.IOException e) {
            IOException(fileName);
        }*/
        
        reader.close();
        reader.setMetadataStore(store);
        reader.setMinMaxStore(store);
        reader.setId(fileName);
        store.setReader(reader.getImageReader());
        //reset series count
        log.debug("Image Count: " + reader.getImageCount());
    }

    /**
     * Calculates and returns the number of planes in this pixels set. Also 
     * sets the offset info.
     * 
     * @param fileName filename for use in {@link #setOffsetInfo(String)}
     * @param pixels Pixels set for which to calculate the plane count.
     * @return the number of planes in this image (z * c * t)
     */
    public int calculateImageCount(String fileName, Pixels pixels)
    {
        this.sizeZ = pixels.getSizeZ().getValue();
        this.sizeC = pixels.getSizeC().getValue();
        this.sizeT = pixels.getSizeT().getValue();
        this.sizeX = pixels.getSizeX().getValue();
        this.sizeY = pixels.getSizeY().getValue();
        int imageCount = sizeZ * sizeC * sizeT;
        setOffsetInfo(fileName);
        return imageCount;
    }

    /**
     * Uses the {@link OMEROMetadataStoreClient} to save the current all
     * image metadata provided.
     * @param imageName A user specified image name.
     * @param imageDescription A user specified description.
     * @param archive Whether or not the user requested the original files to
     * be archived.
     * @return the newly created {@link Pixels} id.
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
     */
	private List<Pixels> importMetadata(String imageName,
			                            String imageDescription,
			                            boolean archive,
			                            Double[] userPixels)
    	throws FormatException, IOException
    {
    	// 1st we post-process the metadata that we've been given.
    	log.debug("Post-processing metadata.");

    	store.setArchive(archive);
    	if (reader.getUsedFiles(true) != null && archive != true)
    	{
            store.setCompanionFiles();
    	}
    	store.setUserSpecifiedImageName(imageName);
    	store.setUserSpecifiedImageDescription(imageDescription);
    	if (userPixels != null)
    	    store.setUserSpecifiedPhysicalPixelSizes(userPixels[0], userPixels[1], userPixels[2]);
    	store.setUserSpecifiedTarget(target);
        store.postProcess();

        log.debug("Saving pixels to DB.");
        List<Pixels> pixelsList = store.saveToDB();
        return pixelsList;
    }

    /**
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private int getBytesPerPixel(int type) {
      switch(type) {
      case 0:
      case 1:
        return 1;  // INT8 or UINT8
      case 2:
      case 3:
        return 2;  // INT16 or UINT16
      case 4:
      case 5:
      case 6:
        return 4;  // INT32, UINT32 or FLOAT
      case 7:
        return 8;  // DOUBLE
      }
      throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    /**
     * Perform an image import.
     * @param file Target file to import.
     * @param index Index of the import in a set. <code>0</code> is safe if 
     * this is a singular import.
     * @param numDone Number of imports completed in a set. <code>0</code> is 
     * safe if this is a singular import.
     * @param total Total number of imports in a set. <code>1</code> is safe
     * if this is a singular import.
     * @param imageName Name to use for all images that are imported from the
     * target file <code>file</code>.
     * @param imageDescription Description to use for all images that are
     * imported from target file <code>file</code>
     * @param archive Whether or not to archive target file <code>file</code>
     * and all sub files.
     * @return List of Pixels that have been imported.
     * @throws FormatException If there is a Bio-Formats image file format
     * error during import.
     * @throws IOException If there is an I/O error.
     * @throws ServerError If there is an error communicating with the OMERO
     * server we're importing into.
     * 
     * TODO: Add observer messaging for any agnostic viewer class to use
     */
    public List<Pixels> importImage(File file, int index, int numDone,
    		                        int total, String imageName, 
    		                        String imageDescription, boolean archive,
    		                        Double[] userPixels)
    	throws FormatException, IOException, ServerError
    {        
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();
        Object[] args;
        
        args = new Object[9];
        args[0] = shortName;
        args[1] = index;
        args[2] = numDone;
        args[3] = total;

        notifyObservers(Actions.LOADING_IMAGE, args);

        open(file.getAbsolutePath());
        
        notifyObservers(Actions.LOADED_IMAGE, args);
        
        String formatString = reader.getImageReader().getReader().getClass().toString();
        formatString = formatString.replace("class loci.formats.in.", "");
        formatString = formatString.replace("Reader", "");
        
        try {
            if (formatString.equals("Micromanager"))
            {
                imageName = new File(file.getParent()).getName();
                shortName = imageName;
            } 
        } catch (Exception e) {}
        
        // Save metadata and prepare the RawPixelsStore for our arrival.
        List<Pixels> pixList = 
        	importMetadata(imageName, imageDescription, archive, userPixels);
    	List<Long> plateIds = new ArrayList<Long>();
    	Image image = pixList.get(0).getImage();
    	if (image.sizeOfWellSamples() > 0)
    	{
    		Plate plate = image.copyWellSamples().get(0).getWell().getPlate();
    		plateIds.add(plate.getId().getValue());
    	}
        List<Long> pixelsIds = new ArrayList<Long>(pixList.size());
        for (Pixels pixels : pixList)
        {
        	pixelsIds.add(pixels.getId().getValue());
        }
        store.preparePixelsStore(pixelsIds);

        int seriesCount = reader.getSeriesCount();
        boolean saveSha1 = false;
        for (int series = 0; series < seriesCount; series++)
        {
            int count = calculateImageCount(fileName, pixList.get(series));
            Pixels pixels = pixList.get(series); 
            long pixId = pixels.getId().getValue(); 
            
            args[4] = getTarget();
            args[5] = pixId;
            args[6] = count;
            args[7] = series;
            
            notifyObservers(Actions.DATASET_STORED, args);

            MessageDigest md = importData(pixId, fileName, series, new ImportLibrary.Step()
            {
                @Override
                public void step(int series, int step)
                {
                    Object args2[] = {series, step, reader.getSeriesCount()};
                    notifyObservers(Actions.IMPORT_STEP, args2);
                }
            });
            if (md != null)
            {
            	String s = OMEROMetadataStoreClient.byteArrayToHexString(md.digest());
            	pixels.setSha1(store.toRType(s));
            	saveSha1 = true;
            }
            
            notifyObservers(Actions.DATA_STORED, args);  
        }
        if (archive)
        {
        	String[] fileNameList = reader.getUsedFiles();
            notifyObservers(Actions.IMPORT_ARCHIVING, args);
        	File[] files = new File[fileNameList.length];
        	for (int i = 0; i < fileNameList.length; i++) 
        	{
        		files[i] = new File(fileNameList[i]); 
        	}
        	store.writeFilesToFileStore(files, pixList.get(0));   
        } 
        else
        {
            String[] fileNameList = store.getFilteredCompanionFiles();
            if (fileNameList != null)
            {
                notifyObservers(Actions.IMPORT_ARCHIVING, args);
                File[] files = new File[fileNameList.length];
                for (int i = 0; i < fileNameList.length; i++)
                {
                    files[i] = new File(fileNameList[i]);
                }
                store.writeFilesToFileStore(files, pixList.get(0));
            }
        }
        
        if (saveSha1)
        {
        	store.updatePixels(pixList);
        }
        
        if (reader.isMinMaxSet() == false)
        {
            store.populateMinMax();
        }
        notifyObservers(Actions.IMPORT_THUMBNAILING, args);
        store.resetDefaultsAndGenerateThumbnails(plateIds, pixelsIds);
        
        notifyObservers(Actions.IMPORT_DONE, args);
        
        return pixList;
    }
    
    /**
     * saves the binary data to the server. After each successful save,
     * {@link Step#step(int)} is called with the number of the iteration just
     * completed.
     * @param series 
     * @return The SHA1 message digest for the Pixels saved.
     */
    public MessageDigest importData(Long pixId, String fileName, int series, Step step)
    throws FormatException, IOException, ServerError
    {
        int i = 1;
        int bytesPerPixel = getBytesPerPixel(reader.getPixelType());
        byte[] arrayBuf = new byte[sizeX * sizeY * bytesPerPixel];

        reader.setSeries(series);
        MessageDigest md;
        
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        
        FileChannel wChannel = null;
        File f;
        
        if (dumpPixels)
        {
            f = new File("pixeldump");
            boolean append = true;
            wChannel = new FileOutputStream(f, append).getChannel();   
        }
        
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    int planeNumber = reader.getIndex(z, c, t);
                    //int planeNumber = getTotalOffset(z, c, t);
                    ByteBuffer buf =
                        reader.openPlane2D(fileName, planeNumber, arrayBuf).getData();
                    arrayBuf = swapIfRequired(buf, fileName);
                    try {
                        md.update(arrayBuf);
                    } catch (Exception e) {
                        // This better not happen. :)
                        throw new RuntimeException(e);
                    }
                    step.step(series, i);
                    store.setPlane(pixId, arrayBuf, z, c, t);
                    if (dumpPixels)
                        wChannel.write(buf);
                    i++;
                }
            }
        }
        
        if (dumpPixels)
            wChannel.close();
        return md;
    }
    
    // ~ Helpers
    // =========================================================================

    private void setOffsetInfo(String fileName)
    {
        int order = 0;
        order = getSequenceNumber(reader.getDimensionOrder());
        setOffsetInfo(order, sizeZ, sizeC, sizeT);
    }

    /**
     * This method calculates the size of a w, t, z section depending on which
     * sequence is being used (either ZTW, WZT, or ZWT)
     * 
     * @param imgSequence
     * @param numZSections
     * @param numWaves
     * @param numTimes
     */
    private void setOffsetInfo(int imgSequence, int numZSections, int numWaves,
            int numTimes)
    {
        int smallOffset = 1;
        switch (imgSequence)
        {
            // ZTW sequence
            case 0:
                zSize = smallOffset;
                tSize = zSize * numZSections;
                wSize = tSize * numTimes;
                break;
            // WZT sequence
            case 1:
                wSize = smallOffset;
                zSize = wSize * numWaves;
                tSize = zSize * numZSections;
                break;
            // ZWT sequence
            case 2:
                zSize = smallOffset;
                wSize = zSize * numZSections;
                tSize = wSize * numWaves;
                break;
            // TWZ sequence
            case 3:
                tSize = smallOffset;
                wSize = tSize * numTimes;
                zSize = wSize * numWaves;
                break;
            // WTZ sequence
            case 4:
                wSize = smallOffset;
                tSize = wSize * numWaves;
                zSize = tSize * numTimes;
                break;
            //TZW
            case 5:
                tSize = smallOffset;
                zSize = wSize * numTimes;
                wSize = tSize * numZSections;
                
        }
    }

    private int getSequenceNumber(String dimOrder)
    {
        if (dimOrder.equals("XYZTC")) return 0;
        if (dimOrder.equals("XYCZT")) return 1;
        if (dimOrder.equals("XYZCT")) return 2;
        if (dimOrder.equals("XYTCZ")) return 3;
        if (dimOrder.equals("XYCTZ")) return 4;
        if (dimOrder.equals("XYTZC")) return 5;
        throw new RuntimeException(dimOrder + " not represented in " +
                "getSequenceNumber");
    }
    
    /** Return true if the data is in little-endian format. 
     * @throws IOException 
     * @throws FormatException */
    public boolean isLittleEndian(String fileName) throws FormatException, IOException {
      return reader.isLittleEndian();
    }
    
    /**
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param byteArray The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    private byte[] swapIfRequired(ByteBuffer buffer, String fileName)
    throws FormatException, IOException
  {
    int pixelType = reader.getPixelType();
    int bytesPerPixel = getBytesPerPixel(pixelType);
    
    // We've got nothing to do if the samples are only 8-bits wide.
    if (bytesPerPixel == 1) 
        return buffer.array();

    int length;
    
    if (isLittleEndian(fileName)) {
      if (bytesPerPixel == 2) { // short
        ShortBuffer buf = buffer.asShortBuffer();
        length = buffer.capacity() / 2;
        short x;
        for (int i = 0; i < length; i++) {
          x = buf.get(i);
          buf.put(i, (short) ((x << 8) | ((x >> 8) & 0xFF)));
        }
      } else if (bytesPerPixel == 4) { // int/uint/float
          IntBuffer buf = buffer.asIntBuffer();
          length = buffer.capacity() / 4;
          for (int i = 0; i < length; i++) {
            buf.put(i, DataTools.swap(buf.get(i)));
          }
      } else if (bytesPerPixel == 8) // double
      {
          LongBuffer buf = buffer.asLongBuffer();
          length = buffer.capacity() / 8;
          for (int i = 0; i < length ; i++) {
            buf.put(i, DataTools.swap(buf.get(i)));
          }
      } else {
        throw new FormatException(
          "Unsupported sample bit width: '" + bytesPerPixel + "'");
      }
    }
    // We've got a big-endian file with a big-endian byte array.
    return buffer.array();
  }
    
    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }
}
