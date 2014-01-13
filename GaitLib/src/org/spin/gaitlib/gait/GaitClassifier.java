
package org.spin.gaitlib.gait;

import android.os.Environment;
import android.util.Log;

import org.spin.gaitlib.core.ILoggable;
import org.spin.gaitlib.sensor.SignalListener;
import org.spin.gaitlib.util.ClassifierAttributeXMLReader;
import org.spin.gaitlib.util.ClassifierAttributeXMLReader.ClassifierAttributes;
import org.spin.gaitlib.util.Logger;
import org.xmlpull.v1.XmlPullParserException;

import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * An abstract class for gait classifier. It handles loading model from file, and requires concrete
 * gait classifier objects to define gaits, setup attributes, and classify gait. Raw sensor data is
 * acquired through the signal listener.
 * 
 * @author Mike
 */
public abstract class GaitClassifier implements ILoggable {

    private static final String TAG = "GaitClassifier";

    private static final String MODEL_LOADING_THREAD_NAME = "org.spin.gaitlib.GaitClassifierModelLoading";

    /**
     * List of gaits among which to classify.
     */
    private final List<String> gaits = new ArrayList<String>();

    private SignalListener signalListener;

    private Classifier classifier = null;

    /**
     * List of attributes in the classifier model
     */
    private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();;

    /**
     * Latest result in the gait classification.
     */
    private String currentGait = null;

    private boolean isLoadingModel = false;

    private boolean isModelLoaded = false;

    private final Set<IClassifierModelLoadingListener> modelLoadingListeners = new HashSet<IClassifierModelLoadingListener>();

    private final Logger classificationResultLogger = new Logger(null, ".gcsv",
            "TimeSinceStart(ms), Gait");

    public GaitClassifier(String modelFileLocation, String modelXMLFileLocation) {
        addModelLoadingListener(new IClassifierModelLoadingListener() {

            public void onModelLoaded(boolean success) {
                isModelLoaded = success;
            }

            public void onLoadingStart() {
                isLoadingModel = true;
            }
        });
        loadModel(modelFileLocation, modelXMLFileLocation);
    }

    /**
     * Load the classifier model, define the attributes and the set of gaits. The status of the the
     * process can be monitored with {@link IClassifierModelLoadingListener}.
     * 
     * @param modelFileLocation
     * @param modelXMLFileLocation
     */
    private void loadModel(final String modelFileLocation, final String modelXMLFileLocation) {
        Thread t = new Thread(null, new Runnable() {

            public void run() {
                notifyModelLoadingStart();

                ClassifierAttributes attributes = null;
                Classifier newClassifier = null;

                try {
                    // load classifier model attributes from the XML file
                    ClassifierAttributeXMLReader reader = new ClassifierAttributeXMLReader();
                    BufferedInputStream attrInputStream = new BufferedInputStream(
                            new FileInputStream(
                                    Environment.getExternalStorageDirectory() + "/"
                                            + modelXMLFileLocation));
                    attributes = reader.parse(attrInputStream);
                    attrInputStream.close();

                    // load classifier model from the model file
                    ObjectInputStream modelInputStream = new ObjectInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(
                                            Environment.getExternalStorageDirectory() + "/"
                                                    + modelFileLocation)));
                    newClassifier = (Classifier) modelInputStream.readObject();
                    modelInputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, e.toString());
                } catch (ClassNotFoundException e) {
                    Log.w(TAG, e.toString());
                } catch (XmlPullParserException e) {
                    Log.w(TAG, e.toString());
                } finally {
                    // populate gaits
                    for (String gait : attributes.getGaits()) {
                        addGait(gait);
                    }
                    // populate features/attributes
                    for (String attribute : attributes.getAttributes()) {
                        addAttribute(new Attribute(attribute));
                    }
                    addAttribute(new Attribute(attributes.getGaitAttributeName(), getAllGaits()));

                    if (newClassifier != null) {
                        setClassifier(newClassifier);
                    }
                    notifyModelLoadingFinish(newClassifier != null);
                }
            }
        }, MODEL_LOADING_THREAD_NAME, 256000);
        t.start();
    }

    /**
     * @throws Exception
     */
    public abstract void classifyGait() throws Exception;

    protected void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    protected void removeAttribute(Attribute attribute) {
        attributes.remove(attribute);
    }

    protected ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    protected Classifier getClassifier() {
        return classifier;
    }

    protected void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public String getCurrentGait() {
        return currentGait;
    }

    protected void setCurrentGait(String currentGait) {
        this.currentGait = currentGait;
        classificationResultLogger.print(TimeUnit.MILLISECONDS.convert(
                signalListener.getTimeSinceStart(System.nanoTime(), TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS), currentGait);
    }

    /**
     * @return the list of gaits among which to classify.
     */
    public List<String> getAllGaits() {
        return gaits;
    }

    /**
     * Add the name of a gait to the list of gaits among which to classify.
     * 
     * @param gait
     * @return <code>true</code> if successfully added the gait.
     */
    protected boolean addGait(String gait) {
        return gaits.add(gait);
    }

    protected boolean removeGait(String gait) {
        return gaits.remove(gait);
    }

    public boolean isLoadingModel() {
        return isLoadingModel;
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }

    public void setSignalListener(SignalListener signalListener) {
        this.signalListener = signalListener;
    }

    protected SignalListener getSignalListener() {
        return signalListener;
    }

    /**
     * Register listener to receive updates during the gait classifier model loading process,
     * including start and end, success or fail.
     * 
     * @param listener
     */
    public void addModelLoadingListener(IClassifierModelLoadingListener listener) {
        modelLoadingListeners.add(listener);
        if (isLoadingModel) {
            listener.onLoadingStart();
        }
    }

    public void removeModelLoadingListener(IClassifierModelLoadingListener listener) {
        modelLoadingListeners.remove(listener);
    }

    private void notifyModelLoadingFinish(boolean success) {
        for (IClassifierModelLoadingListener listener : modelLoadingListeners) {
            listener.onModelLoaded(success);
        }
    }

    private void notifyModelLoadingStart() {
        for (IClassifierModelLoadingListener listener : modelLoadingListeners) {
            listener.onLoadingStart();
        }
    }

    public void setLoggingEnabled(boolean enabled) {
        classificationResultLogger.setEnabled(enabled);
    }

    public boolean isLogging() {
        return classificationResultLogger.isEnabled();
    }
}
