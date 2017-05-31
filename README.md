# SITA_TEST_TASK

## Task Description

Test task scenario:

There will be a series of files placed into the directory (C:\SITA_TEST_TASK\IN) with a number on each line.  Attached are some example files (invalid and valid).  We would like the application to sum all the numbers in the file and create a new file containing the resulting value in the directory (C:\SITA_TEST_TASK\OUT). The output file should have the same name as the input file with .PROCESSED appended to the end of the filename. When the input file is successfully processed it should be moved to the following directory (C:\SITA_TEST_TASK\PROCESSED). If an error occurs while processing the input file then it should be moved into the following directory (C:\SITA_TEST_TASK\ERROR).

Please follow the below directions when completing this task:

1. Create a maven project called SITA_TEST_TASK and ensure that it will generate a war file that can deployed in Glassfish/Tomcat. 

2. Use Spring Integration to process the input messages accordingly

3. Include a README file to explain how the project is built and any other relevant details

4. Create unit tests for relevant methods 

5. Create a public repository on GitHub and send a link to that repository when you are finished with the task.


## Installation & Running Test

There are two ways to run this test. 

1) Simply Run com.sita.App.java as Spring Boot application 
2) Generate the war from maven and deploy on any application server.

## Overview

1) com.sita.config.FilePathConfiguration

This class read the application.properties files and Setup all the Directories path e.g IN, OUT, ERROR, PROCESSED

2) com.sita.App

This class will create the inbound route that pool's the IN directory and search for matched pattern file (configured in ) and then covert file content into string and forward to channel "inbound-channel"

3) com.sita.writer.FileProcessor

Listen on channel "inbound-channel" and calculate the sum and write the result to OUT directory with original file name along with suffix ".PROCESSED". In case the transaction is successful the original source file will be moved to PROCESSED folder. In case of invalid content in the files then it throws NumberFormatException and source file will be moved to the ERROR folder.  

