#xCurator - Semi-Structured Data to Linked Data

[![Build Status](https://travis-ci.org/xcurator/xcurator.svg?branch=ver2)](https://travis-ci.org/xcurator/xcurator)

------------------
xCurator transforms semi-structured data to linked data by leveraging information from both structure and data of the input sources. It supports both XML and JSON data sources from file, set of files and URLs. It generates mapping file as well as RDF files stored in [TDB](http://jena.apache.org/documentation/tdb/) format. 

## Requirements
 * Java 1.7 (or newer)
 

## Getting Started
 1- Clone the repository (or download the zip file)  
```bash 
git clone --recursive https://github.com/Aleyasen/xcurator.git
 ```
If you don't need to download datasets please clone without ```--recursive``` parameter.

 2- Run the ```xcurator.sh``` (```xcurator.bat``` in windows) in the ```bin``` diretory.  


### Parameters


| Parameter         | Description                                                               |
|-------------------|---------------------------------------------------------------------------|
| -d,--dir          | Input directory path                                                      |
| -f,--file         | Input file (xml/json) path                                                |
| -h,--domain       | The generated RDFs will have this domain name in their URIs.              |
| -m,--mapping-file | The output mapping file. If none then there will be no mapping file output. |
| -o,--output       | Directory of the output TDB                                               |
| -t,--type         | Type of the input (xml or json). (default: xml)                           |
| -u,--url          | The URL for the source xml                                                |
| -s,--steps        | The curation steps (default: DIOFK)                                               |


### Curation Steps
The curation steps identifier is a string that specify the steps will run on the input data in order. For example, DOF will perform 
Duplicate Removal, Intra Linking and Schema Flatting in order. 

| Step ID           | Description                                                               |
|-------------------|---------------------------------------------------------------------------|
| D                 | Duplicate Removal                                                      |
| I                 | Inter Linking                                                |
| O                 | Intra Linking             |
| F                 | Schema Flatting |
| K                 | Find Keys                                               |

### Examples
```bash
xcurator.bat -d data/dir -m mapping.xml -h http://xyz.com
```
Generate ```mapping.xml``` for the set of XML files in the ```data/dir``` directory.


```bash
xcurator.bat -f sample.json -m mapping.xml -h http://xyz.com -t json -o tdb
```
Generate ```mapping.xml``` and ```tdb``` directory for the ```sample.json``` input file. 

```bash
xcurator.bat -f sample.json -m mapping.xml -h http://xyz.com -s DF
```
Generate ```mapping.xml``` for the ```sample.json``` input file by running only Duplicat Removal and Schema Flatting steps. 

