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
package edu.toronto.cs.xcurator.common;

import org.w3c.dom.Document;

public class DataDocument {

    public final Document Data;

    // The URI of the generated resource maybe dependent on document-level
    // attributes, so set it here.
    // Not setting it will just use the default URI base defined in the 
    // ElementIdGenerator
    public final String resourceUriPattern;

    public DataDocument(Document data) {
        this.Data = data;
        this.resourceUriPattern = null;
    }

    public DataDocument(Document data, String resourceUriBase) {
        this.Data = data;
        this.resourceUriPattern = (resourceUriBase.endsWith("/") ? resourceUriBase
                : resourceUriBase + "/") + "${UUID}";
    }

}
