/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.json2xml;

import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Amir
 */
public class TestJSON2XML {

    public static void main(String[] args) {
        String str = "{\"menu\": {\n"
                + "  \"id\": \"file\",\n"
                + "  \"value\": \"File\",\n"
                + "  \"popup\": {\n"
                + "    \"menuitem\": [\n"
                + "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n"
                + "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n"
                + "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n"
                + "    ]\n"
                + "  }\n"
                + "}}";
        JSONObject json = new JSONObject(str);
        String xml = XML.toString(json);
        System.out.println(xml);
    }
}
