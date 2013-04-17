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
package edu.toronto.cs.xcurator.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
* @author Eric Yao <jiaxian.yao@mail.utoronto.ca>
*/
public class PatentParser implements Parser {

	@Override
	public void parse(String rawDir, String parsedDir, String fileName) {
		
		// Get the current directory
		String cwd = System.getProperty("user.dir");
		
		try {
			
			String line;
			int count = 0;
			
			FileInputStream is = new FileInputStream(cwd + "\\resources" + "\\" + rawDir + "\\" + fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			FileOutputStream os = new FileOutputStream(cwd + "\\resources" + "\\" + parsedDir + "\\" + fileName);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			bw.newLine();
			bw.write("<us-patent-grants>");
			bw.newLine();

			while ((line = br.readLine()) != null && count <= 300) {
				if (line.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
					count++;
				} else if (line.contains("!DOCTYPE")) {
					continue;
				} else {
					bw.write(line);
					bw.newLine();
				}
			}
			
			bw.write("</us-patent-grants>");
			bw.close();
			os.close();
			
			br.close();
			is.close();
			
			System.out.println((count - 1) + " Patents Parsed.");
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		} catch (IOException e) {
			System.out.println("IOException while reading lines.");
		}
		
	}

}
