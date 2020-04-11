##Orders Parser
Goal of this application is to parse orders from 
**json** and **csv** files of given format.

Example structure can be found in test resource folder

#### To run this application:
* build this application with

    `mvn clean install`
* switch to `target` directory
* create folder with name from `source.directory` property (default is `from-here`)
* run jar with arguments matching contents of target directory 

For example:

`java -jar orders_parser.jar orders1.json orders666.csv orders2.json`