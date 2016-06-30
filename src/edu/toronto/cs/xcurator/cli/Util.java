/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Amir
 */
public class Util {

    public static List<String> getFiles(String dir) {
        List<String> files = new ArrayList<>();
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                files.add(listOfFiles[i].getAbsolutePath());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return files;
    }

    public static String json2xml(String json) {
        JSONObject jsonObj = new JSONObject(json);
        String xml = XML.toString(jsonObj);
        return xml;
    }
}
