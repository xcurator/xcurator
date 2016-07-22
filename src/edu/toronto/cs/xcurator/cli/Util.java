/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.cli;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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

        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<doc>" + xml + "</doc>";
//        System.out.println(xml);
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        StreamSource source = new StreamSource(new StringReader(xml));
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        String prettyxml = result.getWriter().toString();
        return prettyxml;
    }

    public static void main(String[] args) {
        String json = "{\n"
                + "    \"data\": [\n"
                + "        {\n"
                + "            \"service\": false,\n"
                + "            \"event\": \"message\",\n"
                + "            \"id\": \"050000003f1a083f5000000000000000e70cd6c185573390\",\n"
                + "            \"flags\": 256,\n"
                + "            \"to\": {\n"
                + "                \"admins_count\": 0,\n"
                + "                \"peer_id\": 1057495615,\n"
                + "                \"id\": \"$050000003f1a083fe70cd6c185573390\",\n"
                + "                \"peer_type\": \"channel\",\n"
                + "                \"print_name\": \"MyPrintName\",\n"
                + "                \"flags\": 524289,\n"
                + "                \"participants_count\": 0,\n"
                + "                \"title\": \"TheTitle\",\n"
                + "                \"kicked_count\": 0\n"
                + "            },\n"
                + "            \"from\": {\n"
                + "                \"peer_id\": 98267644,\n"
                + "                \"id\": \"$01000000fc71db055506b37488404e27\",\n"
                + "                \"phone\": \"12174175169\",\n"
                + "                \"peer_type\": \"user\",\n"
                + "                \"print_name\": \"MyName\",\n"
                + "                \"flags\": 196609,\n"
                + "                \"first_name\": \"MyFirstName\",\n"
                + "                \"last_name\": \"MyLastName\"\n"
                + "            },\n"
                + "            \"out\": false,\n"
                + "            \"unread\": false,\n"
                + "            \"date\": 1468235231,\n"
                + "            \"text\": \"some text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"service\": false,\n"
                + "            \"event\": \"message\",\n"
                + "            \"id\": \"050000003f1a083f50000sdfd300000e70cd6c185573390\",\n"
                + "            \"flags\": 256,\n"
                + "            \"to\": {\n"
                + "                \"admins_count\": 0,\n"
                + "                \"peer_id\": 13495615,\n"
                + "                \"id\": \"$050sf3f1a083fe70cd6c185573390\",\n"
                + "                \"peer_type\": \"channel\",\n"
                + "                \"print_name\": \"MyPrintName2\",\n"
                + "                \"flags\": 524242,\n"
                + "                \"participants_count\": 0,\n"
                + "                \"title\": \"TheTitle2\",\n"
                + "                \"kicked_count\": 0\n"
                + "            },\n"
                + "            \"from\": {\n"
                + "                \"peer_id\": 9822234,\n"
                + "                \"id\": \"$01000000fc71db055506b37488404e27\",\n"
                + "                \"phone\": \"12174175169\",\n"
                + "                \"peer_type\": \"user\",\n"
                + "                \"print_name\": \"MyName2\",\n"
                + "                \"flags\": 196609,\n"
                + "                \"first_name\": \"MyFirstName2\",\n"
                + "                \"last_name\": \"MyLastName2\"\n"
                + "            },\n"
                + "            \"out\": false,\n"
                + "            \"unread\": false,\n"
                + "            \"date\": 1468985465,\n"
                + "            \"text\": \"some text2\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";
        final String xml = Util.json2xml(json);
        System.out.println(xml);
    }
}
