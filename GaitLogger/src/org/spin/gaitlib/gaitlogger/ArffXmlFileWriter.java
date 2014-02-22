package org.spin.gaitlib.gaitlogger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Write the header of the ARFF file and the full classifier info XML file.
 * 
 * @author Larissa Leong
 *
 */
public class ArffXmlFileWriter {
    
    public static void write(File outputArffFile, File outputXmlFile, ArrayList<String> allGaitNames) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputArffFile, true));
            out.write("@relation GaitDataSet");             
            
            ArrayList<String> attributeNames = new ArrayList<String>(); //attribute names of all the attributes extracted above
            
            attributeNames.add("minimumx_accel");
            attributeNames.add("maximumx_accel");
            attributeNames.add("meanx_accel");
            attributeNames.add("variancex_accel");
            attributeNames.add("skewnessx_accel");
            attributeNames.add("kurtosisx_accel");
            attributeNames.add("p25x_accel");
            attributeNames.add("medianx_accel");
            attributeNames.add("p75x_accel");
            attributeNames.add("histogram0x_accel");
            attributeNames.add("histogram1x_accel");
            attributeNames.add("histogram2x_accel");
            attributeNames.add("histogram3x_accel");
            attributeNames.add("histogram4x_accel");
            attributeNames.add("histogram5x_accel");
            attributeNames.add("histogram6x_accel");
            attributeNames.add("histogram7x_accel");
            attributeNames.add("histogram8x_accel");
            attributeNames.add("histogram9x_accel");
            attributeNames.add("strongestfreqx_accel");
            attributeNames.add("secondstrongestfreqx_accel");
            attributeNames.add("weakestfreqx_accel");
            attributeNames.add("weighted_avgfreqx_accel");
            attributeNames.add("frequency_variancex_accel");
            attributeNames.add("spectral_entropyx_accel");
            attributeNames.add("spectral_histogram0x_accel");
            attributeNames.add("spectral_histogram1x_accel");
            attributeNames.add("spectral_histogram2x_accel");
            attributeNames.add("spectral_histogram3x_accel");
            attributeNames.add("spectral_histogram4x_accel");
            attributeNames.add("spectral_histogram5x_accel");
            attributeNames.add("spectral_histogram6x_accel");
            attributeNames.add("spectral_histogram7x_accel");
            attributeNames.add("spectral_histogram8x_accel");
            attributeNames.add("spectral_histogram9x_accel");           
            attributeNames.add("minimumy_accel");
            attributeNames.add("maximumy_accel");
            attributeNames.add("meany_accel");
            attributeNames.add("variancey_accel");
            attributeNames.add("skewnessy_accel");
            attributeNames.add("kurtosisy_accel");
            attributeNames.add("p25y_accel");
            attributeNames.add("mediany_accel");
            attributeNames.add("p75y_accel");
            attributeNames.add("histogram0y_accel");
            attributeNames.add("histogram1y_accel");
            attributeNames.add("histogram2y_accel");
            attributeNames.add("histogram3y_accel");
            attributeNames.add("histogram4y_accel");
            attributeNames.add("histogram5y_accel");
            attributeNames.add("histogram6y_accel");
            attributeNames.add("histogram7y_accel");
            attributeNames.add("histogram8y_accel");
            attributeNames.add("histogram9y_accel");
            attributeNames.add("strongestfreqy_accel");
            attributeNames.add("secondstrongestfreqy_accel");
            attributeNames.add("weakestfreqy_accel");
            attributeNames.add("weighted_avgfreqy_accel");
            attributeNames.add("frequency_variancey_accel");
            attributeNames.add("spectral_entropyy_accel");
            attributeNames.add("spectral_histogram0y_accel");
            attributeNames.add("spectral_histogram1y_accel");
            attributeNames.add("spectral_histogram2y_accel");
            attributeNames.add("spectral_histogram3y_accel");
            attributeNames.add("spectral_histogram4y_accel");
            attributeNames.add("spectral_histogram5y_accel");
            attributeNames.add("spectral_histogram6y_accel");
            attributeNames.add("spectral_histogram7y_accel");
            attributeNames.add("spectral_histogram8y_accel");
            attributeNames.add("spectral_histogram9y_accel");
            attributeNames.add("minimumz_accel");
            attributeNames.add("maximumz_accel");
            attributeNames.add("meanz_accel");
            attributeNames.add("variancez_accel");
            attributeNames.add("skewnessz_accel");
            attributeNames.add("kurtosisz_accel");
            attributeNames.add("p25z_accel");
            attributeNames.add("medianz_accel");
            attributeNames.add("p75z_accel");
            attributeNames.add("histogram0z_accel");
            attributeNames.add("histogram1z_accel");
            attributeNames.add("histogram2z_accel");
            attributeNames.add("histogram3z_accel");
            attributeNames.add("histogram4z_accel");
            attributeNames.add("histogram5z_accel");
            attributeNames.add("histogram6z_accel");
            attributeNames.add("histogram7z_accel");
            attributeNames.add("histogram8z_accel");
            attributeNames.add("histogram9z_accel");
            attributeNames.add("strongestfreqz_accel");
            attributeNames.add("secondstrongestfreqz_accel");
            attributeNames.add("weakestfreqz_accel");
            attributeNames.add("weighted_avgfreqz_accel");
            attributeNames.add("frequency_variancez_accel");
            attributeNames.add("spectral_entropyz_accel");
            attributeNames.add("spectral_histogram0z_accel");
            attributeNames.add("spectral_histogram1z_accel");
            attributeNames.add("spectral_histogram2z_accel");
            attributeNames.add("spectral_histogram3z_accel");
            attributeNames.add("spectral_histogram4z_accel");
            attributeNames.add("spectral_histogram5z_accel");
            attributeNames.add("spectral_histogram6z_accel");
            attributeNames.add("spectral_histogram7z_accel");
            attributeNames.add("spectral_histogram8z_accel");
            attributeNames.add("spectral_histogram9z_accel");                       
            attributeNames.add("pearson_xy");
            attributeNames.add("pearson_xz");
            attributeNames.add("pearson_yz");
            attributeNames.add("pearsonP_xy"); 
            attributeNames.add("pearsonP_xz");
            attributeNames.add("pearsonP_yz");
            attributeNames.add("signal_magnitude_area");
                                    
            //Write out attribute names
            for (String attributeName : attributeNames) {
                out.newLine();
                out.write("@attribute " + attributeName + " real");
            } 
            
            //Write out gait attribute
            out.newLine();
            out.write("@attribute gait {");
            out.write(allGaitNames.get(0));
            
            for(int y = 1; y <= allGaitNames.size() -1; y++) {
                out.write(", " + allGaitNames.get(y));                  
            }
            out.write("}");
            out.newLine();
            out.newLine();
            
            //Begin writing out data
            out.write("@data");
            out.newLine();
            out.close();
            
            //***Write out XML file:
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
     
            //root 
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("classifier");
            doc.appendChild(root);
            
            //attributes
            Element attributes = doc.createElement("attributes");
            root.appendChild(attributes);
            
            //attribute
            for(String attributeName : attributeNames)
            {
                Element attribute = doc.createElement("attribute");
                attribute.appendChild(doc.createTextNode(attributeName));
                attributes.appendChild(attribute);
            }
            
            //gaits
            Element gaits = doc.createElement("gaits");
            gaits.setAttribute("attrName", "gaits");
            root.appendChild(gaits);
            
            //gait
            for(String gaitName : allGaitNames)
            {
                Element gait = doc.createElement("gait");
                gait.appendChild(doc.createTextNode(gaitName));
                gaits.appendChild(gait);
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputXmlFile);
     
            transformer.transform(source, result);          
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
