
package org.spin.gaitlib.util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse XML files that contain classifier model information, including attribute names and the list
 * of gait names. <br>
 * 
 * @author Mike, following the tutorial at <a
 *         href="http://developer.android.com/training/basics/network-ops/xml.html"
 *         >http://developer.android.com/training/basics/network-ops/xml.html</a>
 */
public class ClassifierAttributeXMLReader {
    private static final String ATTRIBUTE_TAG = "attribute";
    private static final String ATTRIBUTES_TAG = "attributes";
    private static final String ATTRIBUTE_NAME_TAG = "attrName";
    private static final String CLASSIFIER_TAG = "classifier";
    private static final String GAIT_TAG = "gait";
    private static final String GAITS_TAG = "gaits";
    private static final String ns = null;

    public static class ClassifierAttributes {
        private final List<String> attributes;
        private final List<String> gaits;
        private final String gaitAttrName;

        private ClassifierAttributes(List<String> attributes, List<String> gaits,
                String gaitAttrName) {
            this.attributes = attributes;
            this.gaits = gaits;
            this.gaitAttrName = gaitAttrName;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public List<String> getGaits() {
            return gaits;
        }

        public String getGaitAttributeName() {
            return gaitAttrName;
        }
    }

    public ClassifierAttributes parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readClassifier(parser);
        } finally {
            in.close();
        }
    }

    private ClassifierAttributes readClassifier(XmlPullParser parser)
            throws XmlPullParserException,
            IOException {
        List<String> attributes = null;
        List<String> gaits = null;
        String gaitAttrName = null;

        parser.require(XmlPullParser.START_TAG, ns, CLASSIFIER_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ATTRIBUTES_TAG)) {
                attributes = readAttributes(parser);
            } else if (name.equals(GAITS_TAG)) {
                gaitAttrName = parser.getAttributeValue(null, ATTRIBUTE_NAME_TAG);
                gaits = readGaits(parser);
            } else {
                skip(parser);
            }
        }
        return new ClassifierAttributes(attributes, gaits, gaitAttrName);
    }

    private List<String> readAttributes(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        List<String> attributes = new ArrayList<String>();

        parser.require(XmlPullParser.START_TAG, ns, ATTRIBUTES_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ATTRIBUTE_TAG)) {
                attributes.add(readText(parser));
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, ATTRIBUTES_TAG);
        return attributes;
    }

    private List<String> readGaits(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        List<String> gaits = new ArrayList<String>();
        parser.require(XmlPullParser.START_TAG, ns, GAITS_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(GAIT_TAG)) {
                gaits.add(readText(parser));
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, GAITS_TAG);
        return gaits;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
