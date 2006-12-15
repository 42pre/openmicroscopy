/*
 * pojos.ProjectData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

// Java imports
import java.util.HashSet;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.util.CBlock;

/**
 * The data that makes up an <i>OME</i> Project along with links to its
 * contained Datasets and the Experimenter that owns this Project.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OME2.2
 */
public class ProjectData extends DataObject {

    /** Identifies the {@link Project#NAME} field. */
    public final static String NAME = Project.NAME;

    /** Identifies the {@link Project#DESCRIPTION} field. */
    public final static String DESCRIPTION = Project.DESCRIPTION;

    /** Identifies the {@link Project#DATASETLINKS} field. */
    public final static String DATASET_LINKS = Project.DATASETLINKS;

    /**
     * All the Datasets that contain this Image. The elements of this set are
     * {@link DatasetData} objects. If this Image is not contained in any
     * Dataset, then this set will be empty &#151; but never <code>null</code>.
     */
    private Set datasets;

    /** Creates a new instance. */
    public ProjectData() {
        setDirty(true);
        setValue(new Project());
    }

    /**
     * Creates a new instance.
     * 
     * @param project
     *            Back pointer to the {@link Project} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public ProjectData(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(project);
    }

    /**
     * Sets the name of the project.
     * 
     * @param name
     *            The name of the project. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asProject().setName(name);
    }

    /**
     * Returns the name of the project.
     * 
     * @return See above.
     */
    public String getName() {
        return asProject().getName();
    }

    /**
     * Sets the description of the project.
     * 
     * @param description
     *            The description of the project.
     */
    public void setDescription(String description) {
        setDirty(true);
        asProject().setDescription(description);
    }

    /**
     * Returns the description of the project.
     * 
     * @return See above.
     */
    public String getDescription() {
        return asProject().getDescription();
    }

    // Lazy loaded Links
    /**
     * Returns the datasets contained in this project.
     * 
     * @return See above.
     */
    public Set getDatasets() {
        if (datasets == null && asProject().sizeOfDatasetLinks() >= 0) {
            datasets = new HashSet(asProject().eachLinkedDataset(new CBlock() {
                public Object call(IObject object) {
                    return new DatasetData((Dataset) object);
                }
            }));
        }
        return datasets == null ? null : new HashSet(datasets);
    }

    // Link mutations

    /**
     * Sets the datasets contained in this project.
     * 
     * @param newValue
     *            The set of datasets.
     */
    public void setDatasets(Set newValue) {
        Set currentValue = getDatasets();
        SetMutator m = new SetMutator(currentValue, newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asProject().unlinkDataset(m.nextDeletion().asDataset());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asProject().linkDataset(m.nextAddition().asDataset());
        }
        datasets = m.result();
    }

}
