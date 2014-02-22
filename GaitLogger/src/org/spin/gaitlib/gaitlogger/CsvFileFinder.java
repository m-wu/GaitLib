
package org.spin.gaitlib.gaitlogger;

import java.io.File;

public class CsvFileFinder {

    /**
     * ScanForFiles Scans a directory for files matching a certain description and returns a list of
     * files, from which the full file path can be obtained param dirname: The full path to the
     * directory to be scanned
     * 
     * @param startsWith: a list of strings that the file could start with. if startsWith=[] then no
     *            restrictions
     * @param endsWith: a list of strings that the file could end with. if endsWith=[] then no
     *            restrictions
     * @param has: a list of strings that the file could have in its name. if has=[] then no
     *            restrictions
     * @param recursive: Boolean. If true, recursively scans all subfolders.
     * @throws Exception
     * @returns an array containing the actual files (not just the file names) found
     */
    public static File[] scanForFiles(String dirname, String[] startsWith, String[] endsWith,
            String[] has, boolean recursive) throws Exception
    {
        if (dirname == null)
            return null;
        dirname.trim();
        File file = new File(dirname);
        if (!file.exists())
            throw new Exception("Directory name " + dirname + "does not exist.");

        File[] files = new File[0];

        boolean matchesStartsWith = false;
        boolean matchesEndsWith = false;
        boolean matchesHas = false;
        int numFilesFound = -1; // starts counting from 0

        for (File fileBeingChecked : file.listFiles()) {
            if (fileBeingChecked.isFile()) {
                String nameOfFileBeingChecked = fileBeingChecked.getName();
                // check for beginning patterns
                if (startsWith == null || startsWith.equals(null)) {
                    matchesStartsWith = true;
                }
                else {
                    for (String startWithString : startsWith) {
                        if (nameOfFileBeingChecked.startsWith(startWithString)) {
                            matchesStartsWith = true;
                        }
                    }
                }

                // check for ending patterns
                if (endsWith == null || endsWith.equals(null)) {
                    matchesEndsWith = true;
                }
                else {
                    for (String endsWithString : endsWith) {
                        if (nameOfFileBeingChecked.endsWith(endsWithString))
                            matchesEndsWith = true;
                    }
                }

                // check for "has anywhere" patterns
                if (has == null || has.equals(null)) {
                    matchesHas = true;
                }
                else {
                    for (String hasString : has) {
                        if (nameOfFileBeingChecked.contains(hasString))
                            matchesHas = true;
                    }
                }

                if (matchesStartsWith && matchesEndsWith && matchesHas) {
                    numFilesFound++;
                    if (numFilesFound + 1 > files.length) { // if array is full, add one extra space
                        File[] tempArray = new File[files.length + 1];
                        System.arraycopy(files, 0, tempArray, 0, files.length);
                        files = tempArray;
                    }
                    files[numFilesFound] = fileBeingChecked;
                }
            }
            else if (recursive && fileBeingChecked.isDirectory()) {
                File[] tempFileArray = scanForFiles(fileBeingChecked.getAbsolutePath(), startsWith,
                        endsWith, null, recursive);
                File[] newFileArray = new File[tempFileArray.length + files.length];
                System.arraycopy(files, 0, newFileArray, 0, files.length);
                System.arraycopy(tempFileArray, 0, newFileArray, 0, tempFileArray.length);
                files = newFileArray;
            }

            matchesStartsWith = false; // reset flags
            matchesEndsWith = false;
            matchesHas = false;
        }
        return files;
    }
}
