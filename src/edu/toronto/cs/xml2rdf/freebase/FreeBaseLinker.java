/*
 *    Copyright (c) 2013, University of Toronto.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package edu.toronto.cs.xml2rdf.freebase;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import edu.toronto.cs.xml2rdf.interlink.Interlinker;
import edu.toronto.cs.xml2rdf.string.StringMetric;
import edu.toronto.cs.xml2rdf.string.StringUtils;

public class FreeBaseLinker implements Interlinker {

  boolean debug = true;

  final static String domainName = "www.freebase.com";
  final static String SearchURL = "/api/service/search";
  final static String queryParam = "query";

  final static String freebaseTypePrefix = "http://rdf.freebase.com/rdf/";

  @Override
  public Set<String> findTypesForResource(String str, StringMetric metric, double threshold) {
    try {

      // Create the freebase URL query string
    	// Eric: IMPORTANT: THIS URL IS NO LONGER WORKING AND THE NEW URL REQUIRES AN API KEY. SEE THE FOLLOWING ADDRESS FOR DETAILS!!!
    	// https://developers.google.com/freebase/v1/getting-started#/service/search
      URL url = new URL("http://" + domainName + SearchURL + "?" + queryParam + "=" + str.replaceAll("\\s", "+").replaceAll("%", "").replaceAll("\"", ""));

      // Retrieve the content and make it into a JSON object
      String content = StringUtils.getContent((InputStream) url.getContent());
      JSON obj = JSONSerializer.toJSON(content);
      if (obj.isEmpty()) {
        return null;
      }
      JSONObject jsonObject = (JSONObject) obj;

      // A set that hold all typeID strings that start with
      // the specified prefix
      Set<String> types = new HashSet<String>();

      // Retrieve an array of elements whose key name is "result"
      JSONArray array = jsonObject.getJSONArray("result");

      // Iterate through all elements in the array whose key name is "result"
      for (int i = 0; i < array.size(); i++) {

        // Get the element
        JSONObject resultElement = (JSONObject) array.get(i);

        // The boolean value to check if a element with name
        // similar to the provided text value has been found
        boolean same = false;

        // Get the "name" of the element
        String name = "";
        try{
          name = resultElement.getString("name");
        } catch(Exception e) {
        }

        // Check if the name and the provided text value is similar
        if (metric.getSimilarity(str, name) >= threshold) {
          same = true;
        }
        // If the name of the element is not similar, try to find
        // if a similar alias exists
        else {
          JSONArray aliases = resultElement.getJSONArray("alias");
          for (int j = 0; j < aliases.size(); j++) {
            String alias = aliases.getString(j);
            if (metric.getSimilarity(str, alias) >= threshold) {
              same = true;
              break;
            }
          }
        }

        // If the current element has a name or an alias that is similar
        // to the provided text value
        if (same) {

          // Find the array of types of the element
          JSONArray typeArray = resultElement.getJSONArray("type");

          // Iterate through each type
          for (int j = 0; j < typeArray.size(); j++) {
            // For each type, get its typeID, which looks something like "/music/release"
            String typeId = ((JSONObject)typeArray.get(j)).getString("id");
            // Add the prefix to the typeID, which now looks something like
            // "http://rdf.freebase.com/rdf/music.release"
            typeId = freebaseTypePrefix + typeId.substring(1).replaceAll("/", ".");
            // Skip the current iteration if the typeID ends with "topic"
            if (typeId.endsWith("topic")) {
              continue;
            }
            // Add the typeID to types
            types.add(typeId);
          }
        }
      }

      return types;
    } catch (MalformedURLException e) {
      if (debug)
        e.printStackTrace();
    } catch (IOException e) {
      if (debug)
        e.printStackTrace();
    }

    return null;
  }

  @Override
  public String getLabelForResource(String uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> findSameAsForResource(String str, StringMetric metric,
      double threshold, Set<String> types2) {
    Set<String> ret = new HashSet<String>();

    try {

      URL url = new URL("http://" + domainName + SearchURL + "?" + queryParam + "=" + str.replaceAll("\\s", "+"));
      String content = StringUtils.getContent((InputStream) url.getContent());
      JSON obj = JSONSerializer.toJSON(content);
      if (obj.isEmpty()) {
        return null;
      }

      JSONObject jsonObject = (JSONObject) obj;
      JSONArray array = jsonObject.getJSONArray("result");
      for (int i = 0; i < array.size(); i++) {
        JSONObject resultElement = (JSONObject) array.get(i);

        boolean same = false;
        String name = "";
        try{
          name = resultElement.getString("name");
        } catch(Exception e) {
        }
        if (metric.getSimilarity(str, name) >= threshold) {
          same = true;
        } else {
          JSONArray aliases = resultElement.getJSONArray("alias");
          for (int j = 0; j < aliases.size(); j++) {
            String alias = aliases.getString(j);
            if (metric.getSimilarity(str, alias) >= threshold) {
              same = true;
              break;
            }
          }
        }

        if (same) {
          boolean typeMatches = false;
          JSONArray typeArray = resultElement.getJSONArray("type");
          for (int j = 0; j < typeArray.size(); j++) {
            String typeId = ((JSONObject)typeArray.get(j)).getString("id");
            typeId = freebaseTypePrefix + typeId.substring(1).replaceAll("/", ".");
            if (typeId.endsWith("topic")) {
              continue;
            }
            if (types2.contains(typeId)) {
              typeMatches = true;
              break;
            }
          }

          if (typeMatches) {
            ret.add(freebaseTypePrefix + resultElement.getString("id").substring(1).replace('/', '.'));
          }
        }
      }

    } catch (MalformedURLException e) {
      if (debug)
        e.printStackTrace();
    } catch (IOException e) {
      if (debug)
        e.printStackTrace();
    }

    return ret;
  }

  public static void main(String[] args) {
    System.out.println(new FreeBaseLinker().findTypesForResource("united states", null, 0));
  }

  @Override
  public Map<String, Set<String>> findTypesForResources(List<String> str,
      StringMetric metric, double threshold) {
    // TODO Auto-generated method stub
    return null;
  }

  final private static int NUMBER_OF_CONCURRENT_THREADS = 6;
  @Override
  public Map<String, Set<String>> findSameAsForResources(List<String> str,
      StringMetric metric, double threshold, Set<String> types) {



    Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();

    //    TypeFetcher thread = new TypeFetcher(term, metric, threshold, resultMap );
    //    
    //    thread.
    return resultMap;
  }

  class TypeFetcher extends Thread{
    String term;
    private StringMetric metric;
    private double threshold;
    private Map<String, Set<String>> resultMap;

    public TypeFetcher(String term, StringMetric metric, double threshold, Map<String, Set<String>> resultMap) {
      this.term = term;
      this.threshold = threshold;
      this.metric = metric;
      this.resultMap = resultMap;
    }

    @Override
    public void run() {
      Set<String> types = findTypesForResource(term, metric, threshold);
      resultMap.put(term, types);
    }
  }
}
