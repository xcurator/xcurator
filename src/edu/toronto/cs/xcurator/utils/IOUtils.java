/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.utils;

import edu.toronto.cs.xcurator.mapping.Schema;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static <T> String printMapAsJson(Map<String, T> map) {
        StringBuilder sb = new StringBuilder();
        if (map.isEmpty()) {
            return "{}";
        }
        sb.append("{");
        for (String key : map.keySet()) {
            final Object val = (Object) map.get(key);
            sb.append("\"").append(key).append("\":");
            if (val.getClass().isPrimitive()) {
                sb.append("\"");
            }
            sb.append(val.toString());
            if (val.getClass().isPrimitive()) {
                sb.append("\"");
            }
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("hello", "345");
        map.put("bye", "byebye");
        System.out.println(printMapAsJson(map));
    }
}
