#xCurator - Semi-Structured Data to Linked Data
========
xCurator transforms semi-structured data to linked data by leveraging information from both structure and data of the input sources. It supports both XML and JSON data sources from file, set of files and URLs. It generates mapping file as well as RDF files stored in [TDB](http://jena.apache.org/documentation/tdb/) format. 


## Getting Start
 Clone the repository (or download the zip file) and run the ```xcurator.sh``` (```xcurator.bat``` in windows) in the ```bin``` diretory.  


### Parameters


| Parameter         | Description                                                               |
|-------------------|---------------------------------------------------------------------------|
| -d,--dir          | Input directory path                                                      |
| -f,--file         | Input file (xml/json) path                                                |
| -h,--domain       | The generated RDFs will have this domain name in their URIs.              |
| -m,--mapping-file | The output mapping file. If none then there will be no mapping file output. |
| -o,--output       | Directory of the output TDB                                               |
| -t,--type         | Type of the input (xml or json). [default: xml]                           |
| -u,--url          | The URL for the source xml                                                |

### Examples
```bash
xcurator.bat -f sample.json -m mapping.xml -h http://xyz.com -t json -o tdb
```
Create mapping.xml and tdb directory for the sample.json input file. 


