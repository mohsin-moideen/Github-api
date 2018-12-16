1. Execution instructions:
	i. Open a terminal and navigate to blueoptima-github-api folder.
	ii. Execute command 'mvn package' to build the project (assuming mvn is present in classpath).
	iii. Execute command 'java -jar target\blueoptima-github-api-0.0.1-SNAPSHOT.jar' to run the program.
	iv. Open a browser and enter the url 'http://localhost:8080/profiles/'

2. Understanding & approach:
	Github enforces a rate limit on their APIs to avoid any type of D/DOS attacks at the same time to avoid malicious scanning of user data.
	There are 3 types of rate limits:
		i. Core rate limit
		ii. Search rate limit
		iii. graphql rate limit
	If the request is from non authenticated source, core and search rate limit are limited to 60 and 10 respectively.
	On authenticating with key and token, limits are raised to 5000 and 30 respectively.
	In order to avoid account lockout, the remaining requests are checked before every API call.
	To make the rate limit check efficient,local copies of the limits are maintained and deducted every time an API call is made.
	The local copy is refreshed only when it becomes zero. If, after refreshing the limits and they are still zero and exception is thrown and ending the execution 	of the program.

3. Assumptions:
	i. Input will always be in the format "<First name> <Last name> <location>", where location is optional.
	ii. First name and last name are always present.

4. Accuracy improvement:
	If email was provided accuracy would become 100%.
	If Github login id was provided, the search API could be avoided.

5. Enhancements
	i. Caching response of recently used APIs.
	ii. UI to upload input file and view results.


	




	