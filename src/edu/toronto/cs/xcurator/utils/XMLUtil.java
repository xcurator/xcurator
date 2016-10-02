/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Amir
 */
public class XMLUtil {
    // Protected Properties

    protected DocumentBuilderFactory domFactory = null;
    protected DocumentBuilder domBuilder = null;

    public XMLUtil() {
        try {
            domFactory = DocumentBuilderFactory.newInstance();
            domBuilder = domFactory.newDocumentBuilder();
        } catch (FactoryConfigurationError exp) {
            System.err.println(exp.toString());
        } catch (ParserConfigurationException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }

    }

    public int convertFile(String csvFileName, String xmlFileName,
            String delimiter) {

        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();
            // Root element
            Element rootElement = newDoc.createElement("XMLCreators");
            newDoc.appendChild(rootElement);
            // Read csv file
            BufferedReader csvReader;
            csvReader = new BufferedReader(new FileReader(csvFileName));

//                int fieldCount = 0;
//                String[] csvFields = null;
//                StringTokenizer stringTokenizer = null;
//
//                // Assumes the first line in CSV file is column/field names
//                // The column names are used to name the elements in the XML file,
//                // avoid the use of Space or other characters not suitable for XML element
//                // naming
//
//                String curLine = csvReader.readLine();
//                if (curLine != null) {
//                    // how about other form of csv files?
//                    stringTokenizer = new StringTokenizer(curLine, delimiter);
//                    fieldCount = stringTokenizer.countTokens();
//                    if (fieldCount > 0) {
//                        csvFields = new String[fieldCount];
//                        int i = 0;
//                        while (stringTokenizer.hasMoreElements()) {
//                            csvFields[i++] = String.valueOf(stringTokenizer.nextElement());
//                        }
//                    }
//                }
//
//                // At this point the coulmns are known, now read data by lines
//                while ((curLine = csvReader.readLine()) != null) {
//                    stringTokenizer = new StringTokenizer(curLine, delimiter);
//                    fieldCount = stringTokenizer.countTokens();
//                    if (fieldCount > 0) {
//                        Element rowElement = newDoc.createElement("row");
//                        int i = 0;
//                        while (stringTokenizer.hasMoreElements()) {
//                            try {
//                                String curValue = String.valueOf(stringTokenizer.nextElement());
//                                Element curElement = newDoc.createElement(csvFields[i++]);
//                                curElement.appendChild(newDoc.createTextNode(curValue));
//                                rowElement.appendChild(curElement);
//                            } catch (Exception exp) {
//                            }
//                        }
//                        rootElement.appendChild(rowElement);
//                        rowsCount++;
//                    }
//                }
//                csvReader.close();
//
//                // Save the document to the disk file
//                TransformerFactory tranFactory = TransformerFactory.newInstance();
//                Transformer aTransformer = tranFactory.newTransformer();
//                Source src = new DOMSource(newDoc);
//                Result result = new StreamResult(new File(xmlFileName));
//                aTransformer.transform(src, result);
//                rowsCount++;
            int line = 0;
            List<String> headers = new ArrayList<String>(5);

            String text = null;
            while ((text = csvReader.readLine()) != null) {

                StringTokenizer st = new StringTokenizer(text, delimiter, false);
                String[] rowValues = new String[st.countTokens()];
                int index = 0;
                while (st.hasMoreTokens()) {

                    String next = st.nextToken();
                    rowValues[index++] = next;

                }

                if (line == 0) { // Header row

                    for (String col : rowValues) {
                        headers.add(col);
                    }

                } else { // Data row

                    rowsCount++;

                    Element rowElement = newDoc.createElement("row");
                    rootElement.appendChild(rowElement);
                    for (int col = 0; col < headers.size(); col++) {

                        String header = headers.get(col);
                        String value = null;

                        if (col < rowValues.length) {

                            value = rowValues[col];

                        } else {
                            // ?? Default value
                            value = "";
                        }

                        Element curElement = newDoc.createElement(header);
                        curElement.appendChild(newDoc.createTextNode(value));
                        rowElement.appendChild(curElement);

                    }

                }
                line++;

            }

            ByteArrayOutputStream baos = null;
            OutputStreamWriter osw = null;

            try {

                baos = new ByteArrayOutputStream();
                osw = new OutputStreamWriter(baos);

                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                Source src = new DOMSource(newDoc);
                Result result = new StreamResult(osw);
                aTransformer.transform(src, result);

                osw.flush();
                System.out.println(new String(baos.toByteArray()));

            } catch (Exception exp) {
                exp.printStackTrace();
            } finally {
                try {
                    osw.close();
                } catch (Exception e) {
                }
                try {
                    baos.close();
                } catch (Exception e) {
                }
            }

            // Output to console for testing
            // Resultt result = new StreamResult(System.out);
        } catch (IOException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }
        return rowsCount;
        // "XLM Document has been created" + rowsCount;
    }
}
