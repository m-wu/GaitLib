
package org.spin.gaitlib.gait;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.spin.gaitlib.sensor.ThreeAxisSensorReading;
import org.spin.gaitlib.util.SpectralAnalyses;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

/**
 * An implementation of {@link GaitClassifier}, using the algorithm by Oliver Schneider.
 * 
 * @author Oliver
 * @author Mike
 */
public class DefaultGaitClassifier extends GaitClassifier {

    private static final String DEFAULT_MODEL_LOCATION = "Android/data/org.spin.gaitlib/files/classification_models/personalized_model.model";
    private static final String DEFAULT_MODEL_XML_LOCATION = "Android/data/org.spin.gaitlib/files/classification_models/personalized.xml";

    private static final int NUMBER_OF_ATTRIBUTES = 112;

    private static final String[] axis = {
            "x_accel", "y_accel", "z_accel"
    };

    /**
     * A default gait classifier with the default model file - Random Forest with 100 trees.
     * 
     * @param signalListener
     */
    public DefaultGaitClassifier() {
        this(DEFAULT_MODEL_LOCATION, DEFAULT_MODEL_XML_LOCATION);
    }

    /**
     * A default gait classifier with a custom defined classification model.
     * 
     * @param signalListener
     * @param modelFileLocation the path to the model file. E.g., for root/gaitlib/classifier.model,
     *            use <code>gaitlib/classifier.model</code>
     */
    public DefaultGaitClassifier(String modelFileLocation, String modelXMLFileLocation) {
        super(modelFileLocation, modelXMLFileLocation);
    }

    /**
     * @exception ModelNotLoadedException if the model for the classifier is not loaded.
     * @exception Exception if an error occurred during the prediction.
     */
    @Override
    public void classifyGait() throws Exception {
        if (getClassifier() == null) {
            throw new ModelNotLoadedException();
        }

        if (getAttributes().size() != NUMBER_OF_ATTRIBUTES) {
            throw new Exception(
                    "Error: list of attributes does not match between model file and xml file.");
        }

        float[] signalX_float, signalY_float, signalZ_float, signalTime_float;

        ThreeAxisSensorReading[] accelArr = getSignalListener().getAccelReadingsArray();

        signalX_float = new float[accelArr.length];
        signalY_float = new float[accelArr.length];
        signalZ_float = new float[accelArr.length];
        signalTime_float = new float[accelArr.length];

        float[][] signal_float = {
                signalX_float, signalY_float, signalZ_float
        };
        for (int i = 0; i < accelArr.length; i++) {
            signalX_float[i] = accelArr[i].getX();
            signalY_float[i] = accelArr[i].getY();
            signalZ_float[i] = accelArr[i].getZ();
            signalTime_float[i] = accelArr[i].getTimeSinceStartInS();
        }

        double[] features = extractFeatures(signal_float, signalTime_float);

        Instances instances = new Instances("GaitDataSet", getAttributes(), 1);
        instances.setClassIndex(instances.numAttributes() - 1);
        Instance thisInstance = new DenseInstance(1.0, features);
        instances.add(thisInstance);
        thisInstance.setDataset(instances);

        int index = (int) getClassifier().classifyInstance(thisInstance);
        setCurrentGait(instances.classAttribute().value(index));
    }

    /**
     * @param signal_float a 2D array containing the accelerometer readings in x, y and z
     *            coordinate.
     * @param signalTime_float an array containing the timestamp, with unit of seconds, for each
     *            accelerometer reading
     * @return an array of features
     */
    public static double[] extractFeatures(float[][] signal_float, float[] signalTime_float) {

        int signalLength = signal_float[0].length;

        double[] signalX_double = new double[signalLength];
        double[] signalY_double = new double[signalLength];
        double[] signalZ_double = new double[signalLength];
        double[] signalTime_double = new double[signalLength];
        double[][] signal_double = {
                signalX_double, signalY_double, signalZ_double
        };
        float[] zeroes_float = new float[signalLength];
        double signal_magnitude_area = 0;

        for (int i = 0; i < signalLength; i++) {
            signalX_double[i] = signal_float[0][i];
            signalY_double[i] = signal_float[1][i];
            signalZ_double[i] = signal_float[2][i];
            signalTime_double[i] = signalTime_float[i];

            zeroes_float[i] = 0.0f;

            signal_magnitude_area += Math.abs(signalX_double[i]) + Math.abs(signalY_double[i])
                    + Math.abs(signalZ_double[i]);
        }

        float hifac = (float) 0.25;
        float ofac = 4;

        double[] features = new double[NUMBER_OF_ATTRIBUTES + 1];
        int feature_i = 0;

        for (int i = 0; i < axis.length; i++) {
            double[] curSignal = signal_double[i];
            float[] curSignal_float = signal_float[i];
            DescriptiveStatistics curSignalStats = new DescriptiveStatistics(curSignal);
            features[feature_i++] = curSignalStats.getMin();
            features[feature_i++] = curSignalStats.getMax();
            features[feature_i++] = curSignalStats.getMean();
            features[feature_i++] = curSignalStats.getVariance();
            features[feature_i++] = curSignalStats.getSkewness();
            features[feature_i++] = curSignalStats.getKurtosis();
            features[feature_i++] = curSignalStats.getPercentile(25);
            features[feature_i++] = curSignalStats.getPercentile(50);
            features[feature_i++] = curSignalStats.getPercentile(75);

            // histogram
            EmpiricalDistribution curSignalDistribution = new EmpiricalDistribution(10);
            curSignalDistribution.load(curSignal);
            List<SummaryStatistics> curSignalBinStats = curSignalDistribution.getBinStats();
            double curSignalN = curSignalStats.getN();
            for (SummaryStatistics binStat : curSignalBinStats) {
                features[feature_i++] = binStat.getN() / curSignalN;
            }

            float[][] fasper_results = SpectralAnalyses.fasperArray(hifac, ofac, curSignal_float,
                    zeroes_float, zeroes_float, signalTime_float, signalX_double.length);

            // strongest, second strongest and weakest frequencies
            float[] minMaxFreq = SpectralAnalyses.fasperResultsMaxMinFreq(fasper_results);
            features[feature_i++] = minMaxFreq[0];
            features[feature_i++] = minMaxFreq[1];
            features[feature_i++] = minMaxFreq[2];

            // weighted average frequency
            features[feature_i++] = SpectralAnalyses
                    .fasperResultsWeightedAverageFreq(fasper_results);

            // frequency variance
            double[] fasperResultsFrequencies = SpectralAnalyses
                    .fasperResultsFrequenciesAsDoubles(fasper_results);
            features[feature_i++] = (new DescriptiveStatistics(fasperResultsFrequencies))
                    .getVariance();

            double[] fasperResultsPowers = SpectralAnalyses
                    .fasperResultsPowersAsDoubles(fasper_results);

            DescriptiveStatistics fasperPowersStats = new
                    DescriptiveStatistics(fasperResultsPowers);
            double fasperPowersSum = fasperPowersStats.getSum();

            // spectral entropy
            double entropy = 0;
            double log2 = Math.log(2);

            for (int j = 0; j < fasperResultsPowers.length; j++) {
                double probability = fasperResultsPowers[j] / fasperPowersSum;
                entropy = entropy + probability * (Math.log(probability) / log2);
            }
            entropy = -entropy;
            features[feature_i++] = entropy;

            // spectral histogram
            EmpiricalDistribution fasperPowersDistribution = new EmpiricalDistribution(10);
            fasperPowersDistribution.load(fasperResultsPowers);
            List<SummaryStatistics> fasperPowersBinStats = curSignalDistribution.getBinStats();
            double fasperPowersN = fasperPowersStats.getN();
            for (SummaryStatistics binStat : fasperPowersBinStats) {
                features[feature_i++] = binStat.getN() / fasperPowersN;
            }
        }

        // Pearson correlation values
        RealMatrix data_for_correlation = new BlockRealMatrix(signal_double).transpose();
        PearsonsCorrelation pearsonCorr = new PearsonsCorrelation(data_for_correlation);
        // this may be inefficient, I may have already calculated this above
        features[feature_i++] = pearsonCorr.correlation(signalX_double, signalY_double);
        features[feature_i++] = pearsonCorr.correlation(signalX_double, signalZ_double);
        features[feature_i++] = pearsonCorr.correlation(signalY_double, signalZ_double);

        // pearson P values
        RealMatrix pvalues = pearsonCorr.getCorrelationPValues();
        features[feature_i++] = pvalues.getEntry(0, 1);
        features[feature_i++] = pvalues.getEntry(0, 2);
        features[feature_i++] = pvalues.getEntry(1, 2);

        // signal_magnitude_area
        features[feature_i++] = signal_magnitude_area;

        features[feature_i++] = 0; // class attribute
        return features;
    }
}
