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
package edu.toronto.cs.xml2rdf.string;

public class NoWSCaseInsensitiveStringMetric implements StringMetric {

	@Override
	public double getSimilarity(String str1, String str2) {
		str1 = StringUtils.deAccent(str1);
		str2 = StringUtils.deAccent(str2);
		if (str1.replaceAll("\\s", "").equalsIgnoreCase(str2.replaceAll("\\s", ""))) {
			return 1;
		}

		return 0;
	}

}
