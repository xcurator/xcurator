/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xml2rdf.freebase;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author Amirhossein Aleyasen <aleyase2@illinois.edu>
 * created on Dec 1, 2015, 3:51:10 PM
 */
public class FreebaseUtil {

    final private static String FREEBASE_PROPERTIES_LOC = "resources/freebase/freebase.properties";
    final private static Properties properties = new Properties();

    public static void main(String[] args) {
        final JSONArray json = search("Propofol", "resources/freebase/type_query.json", 5);
        System.out.println(json);
//        Object document = Configuration.defaultConfiguration().getProvider().parse(json.toString());

//        final List<String> types_list = JsonPath.read(document, "$..type[*].name");
//        Set<String> types = new HashSet(types_list);
//        System.out.println(types);
    }

    public static JSONArray search(String query, String mql_query_file, int limit) {
        try {
            properties.load(new FileInputStream(FREEBASE_PROPERTIES_LOC));
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
            url.put("query", query);
//            url.put("filter", "(all type:/music/artist created:\"The Lady Killer\")");
            url.put("limit", limit);
            String mql_query = IOUtils.toString(new FileInputStream(mql_query_file));

            url.put("mql_output", mql_query);
            url.put("key", properties.get("API_KEY"));
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = null;
            try {
                httpResponse = request.execute();
            } catch (Exception e) {

            }
            if (httpResponse == null) {
                return null;
            }
            JSON obj = JSONSerializer.toJSON(httpResponse.parseAsString());
            if (obj.isEmpty()) {
                return null;
            }
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray results = jsonObject.getJSONArray("result");
            System.out.println(results);
            return results;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static JSONArray fetch(String query_template_file, Map<String, String> params) {
        try {
            properties.load(new FileInputStream(FREEBASE_PROPERTIES_LOC));
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            String query = IOUtils.toString(new FileInputStream(query_template_file));
            query = manipulateQuery(query, params);
            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
            url.put("query", query);
            url.put("key", properties.get("API_KEY"));
            System.out.println("URL:" + url);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            JSON obj = JSONSerializer.toJSON(httpResponse.parseAsString());
            if (obj.isEmpty()) {
                return null;
            }
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray results = jsonObject.getJSONArray("result");
            System.out.println(results.toString());
            return results;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String manipulateQuery(String query, Map<String, String> params) {
        String manipulate_query = query;
        for (String key : params.keySet()) {
            manipulate_query = query.replace("%" + key + "%", params.get(key));
        }
        return manipulate_query;
    }
}
