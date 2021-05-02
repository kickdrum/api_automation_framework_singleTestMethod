# API Automation Framework
API Automation framework using RestAssured.
This method makes use of a single Test method takes gets its list of input request json file names from a Data Provider method.
Each request file in the received list undergoes through the API call and verification steps.

The actual output from the API call is compared against the corresponding expected json (stored in the same location where request jsons are present, suffixed as '-response.json').

This framework can be used to process for as many sets (request & expected response json files) as stored in the 'jsonFiles' folder.


