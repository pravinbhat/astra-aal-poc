# astra-aal-poc
Astra sample app with CRUD operations using Spring-Data

This project is a reference implementation of Java Spring-Boot application that uses AstraDB (a Cassandra based SaaS DB Service) as its backend and auto-detects/respond to any possible AstraDB health issues. 

# Objectives
1. Reference implementation of DB connectivity using Java/Spring-Boot and AstraDB
2. Auto-detect and respond to any rare but possible AstraDB health issues (e.g. Cloud/IaaS provider issues, region specific issues etc.) that could make the DB unavailable.
3. Reference data-micro-service REST API implementation based on Cassandra/AstraDB
4. Reference implementation of a circuit-breaker pattern to avoid overwhelming downstream dependencies in event of an ongoing health issue
5. Auto connect to AstraDB OR local Cassandra based on Profile (Local or Astra)

### Tech Stack
- Java 11
- Spring Boot 2.7.x (Spring Framework + Spring Boot + Spring Data + Spring REST + Spring Profiles)
- AstraDB (Cassandra)
- Resilience4j (Circuit Breaker)
- Maven (Build tool)

### Building and running the application
Built the application using command: `mvn clean package`

Run the application using command: `mvn spring-boot:run`

To test the application using 'local' profile (connect to local Cassandra), use command: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
> Project dependencies could be found in the [POM file](https://github.com/pravinbhat/astra-aal-poc/blob/main/pom.xml)

### Application details
[AstraAALPocApplication](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/AstraAALPocApplication.java) class bootstraps the application and also enables the AstraDB profile (default - using AstraConfig) when the local-profile is not activated. 

[application.yml](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/resources/application.yml) is used to configure Astra connection details (using Astra secure-connect-bundle).
- Add your Astra DB specific environment configuration here

[application-local.yml](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/resources/application-local.yml) is used to configure local Cassandra connection details for local testing (application.yml file will be skipped).
- Add your Local Cassandra environment configuration here
- When local profile is activated (using Spring Profile 'local'), the defailt Astra profile will not be bootstraped. 

[logback.xml](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/resources/logback.xml) is used as the logging implementation along with the SLF4J APIs.

[Flight](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/flight/Flight.java) object is used as the persistent entity, you can update it as needed for your use-case.

[FlightRepo](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/flight/FlightRepo.java) provides the API for spring-data based Repository pattern.

[FlightController](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/flight/FlightController.java) provides the core functionality for this data-micro-service, below are some details about the same
- REST APIs are implemented using Spring Web Controller pattern unders the 'flights' endpoint
- Triggers Resilience4j based @CircuitBreaker on any exceptions
    - Implements this using the 'fallbackMethod'. The fallback method then checks if the exceptions was caused due to a health-issue.
    - Resilience4j/CircuitBreaker config are provided in the application.yml and application-local.yml files under 'resilience4j:'
- [HealthCheck](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/health/HealthCheck.java) is used to implement auto-detection of health-issue
- [FlightRepo](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/flight/FlightRepo.java) is used to implement persistence
- [FlightResponse](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/flight/FlightResponse.java) is used to encapsulate the health status along with the 'Flight' entity data.
> Note: The APIs reports back exceptions caused due to a health issues specifically using `HTTP error code 503 ('SERVICE_UNAVAILABLE')`. All other functional errors are reported as HTTP 500 ('INTERNAL_SERVER_ERROR').
> Any additional application specific errors (e.g. HTTP 4xx errors) will need to be implemented and handled accordingly by the application teams.

[HealthCheck](https://github.com/pravinbhat/astra-aal-poc/blob/main/src/main/java/com/bhatman/poc/astra/health/HealthCheck.java) object performs the below two step verification of Astra env health. If any of these checks fail, it reports back the instance as 'Not Healthly' (i.e. HTTP error 503 'SERVICE_UNAVAILABLE').
1. Check if the Astra dashboard reports your specific database instance (using the value for property astra.db.id) as 'Online'.
2. If the above suceeds, it queries the schema for your specific keyspace (using the value for property astra.db.keyspace) with a preset timeout (set using the 'astra.db.timeout' property in the application.yml and application-local.yml files).

