/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Amir
 */
public class IOUtils {

    public static List<String> readFileLineByLine(String file) {
        String content = null;
        try {
            content = FileUtils.readFileToString(new File(file));
        } catch (IOException ex) {
            Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> lines = Arrays.asList(content.split("\\r\\n|\\n|\\r"));
        return lines;
    }
}
