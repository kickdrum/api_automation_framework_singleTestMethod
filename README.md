# API Automation Framework
API Automation framework using RestAssured.

Features:
1) This approach takes in a set of JSON files (Request and Expected Response JSONs). Ensure to name the Request JSON file with suffix as '-request.json' and the ExpectedResponse JSON file with suffix as '-response.json'.
2) Performs API calls for each Request JSON file, and collects the Response.
3) Compares the Actual Response JSON with Expected Response JSON, and concludes on the test.
4) Same Test method gets reused for the number of Request JSON files stored in the 'jsonFiles' folder.
5) Supports different structures of Request and ExpectedResponse JSON files, for the same API endpoint.

