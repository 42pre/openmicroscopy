/*
 * org.openmicroscopy.shoola.env.data.model.ChannelData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
 */

package org.openmicroscopy.shoola.env.data.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ChannelData
{
	
	/** Attribute ID in DB. */
	private final long      id;
	
	/** OME index of the channel. */
	private final int      index;

	/** Emission wavelength in nanometer. */
	private final int      nanometer;

	/** Excitation wavelength in nanometer. */
	private int            excitation;

	/** Photometric interpretation. */
	private String         interpretation;
    
   
    private float          ndFilter, auxLightAttenuation, detectorGain, 
                           detectorOffset, lightAttenuation;
    
    private int            auxLightWavelength, pinholeSize, lightWavelength, 
                           SamplesPerPixel;
    
    private String         fluor, auxTechnique, contrastMethod, mode, 
                           illuminationType;
    
    public ChannelData(long id, int index, int nanometer)
    {
        this.id = id;
        this.index = index;  
        this.nanometer = nanometer;
    }
    
	public ChannelData(long id, int index, int nanometer, String interpretation,
						int excitation, String fluor)
	{
		this.id = id;
		this.index = index;                               
		this.nanometer = nanometer;
		this.interpretation = interpretation;
		this.excitation = excitation;
		this.fluor = fluor;
	}
    
	public int getNanometer() { return nanometer; }
	
	public long getID() { return id; }
	
	public int getIndex() { return index; }

	public String getInterpretation() { return interpretation; }

	public void setInterpretation(String string) { interpretation = string; }

	public int getExcitation() {return excitation; }

	public String getFluor() { return fluor; }

	public void setExcitation(int excitation) { this.excitation = excitation; }

	public void setFluor(String fluor) { this.fluor = fluor; }
    
	public float getAuxLightAttenuation() { return auxLightAttenuation; }
    
    public void setAuxLightAttenuation(float auxLightAttenuation)
    {
        this.auxLightAttenuation = auxLightAttenuation;
    }
    
    public int getAuxLightWavelength() { return auxLightWavelength; }
    
    public void setAuxLightWavelength(int auxLightWavelength)
    {
        this.auxLightWavelength = auxLightWavelength;
    }
    
    public String getAuxTechnique() { return auxTechnique; }
    
    public void setAuxTechnique(String auxTechnique)
    {
        this.auxTechnique = auxTechnique;
    }
    
    public String getContrastMethod() { return contrastMethod; }
    
    public void setContrastMethod(String contrastMethod)
    {
        this.contrastMethod = contrastMethod;
    }
    
    public float getDetectorGain() { return detectorGain; }
    
    public void setDetectorGain(float detectorGain)
    {
        this.detectorGain = detectorGain;
    }
    
    public float getDetectorOffset() { return detectorOffset; }
    
    public void setDetectorOffset(float detectorOffset)
    {
        this.detectorOffset = detectorOffset;
    }
    
    public String getIlluminationType() { return illuminationType; }
    
    public void setIlluminationType(String illuminationType)
    {
        this.illuminationType = illuminationType;
    }
    
    public float getLightAttenuation() { return lightAttenuation; }
    
    public void setLightAttenuation(float lightAttenuation)
    {
        this.lightAttenuation = lightAttenuation;
    }
    
    public int getLightWavelength() { return lightWavelength; }
    
    public void setLightWavelength(int lightWavelength)
    {
        this.lightWavelength = lightWavelength;
    }
    
    public String getMode() { return mode; }
    
    public void setMode(String mode) { this.mode = mode; }
    
    public float getNDFilter() { return ndFilter; }
    
    public void setNDFilter(float ndFilter) { this.ndFilter = ndFilter; }
    
    public int getPinholeSize() { return pinholeSize; }
    
    public void setPinholeSize(int pinholeSize)
    { 
        this.pinholeSize = pinholeSize;
    }
    
    public int getSamplesPerPixel() { return SamplesPerPixel; }
    
    public void setSamplesPerPixel(int samplesPerPixel)
    {
        SamplesPerPixel = samplesPerPixel;
    }
    
}
