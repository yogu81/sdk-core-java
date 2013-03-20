This repository contains core files for all Java sdks.

Prerequisites:
---------------
*	Java JDK-1.5 or higher
*	Apache Maven 2 or higher

To build this application:
--------------------------
*	Run 'mvn install' to build jar file.

Core Testing:
------------
*	Run 'mvn test' to run the test cases for all the core classes.
*	Test reports are generated in testReport folder.

Core Logging:
------------
*	For logging - java.util.logging has been used. To change the default configuration, edit the logging.properties file in 'jre/lib' folder under your JDK root.		  

		  
Core Configuration:
------------------
The core uses .properties format configuration file. Sample of this file is at 
 
'src/test/resources/'. You can use the 'sdk_config.properties' configuration file to configure

*	Mode is specified using the parameter name 'mode' with values 'sandbox' or 'live', if specified 'service.EndPoint' parameter is not required and the SDK choses the sandbox or live endpoints automatically.

*	(Multiple) API account credentials, by appending a '.' (dot) character and the service name to 'service.EndPoint' parameter.

*	HTTP connection parameters, if certain connection parameters are not specified, the SDK will assume defaults for them.

*	Service configuration.
