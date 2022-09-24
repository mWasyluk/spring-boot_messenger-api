# Work in progress!

# Messenger API

## About
The Messenger API is a REST API that provides endpoints which are used to perform CRUD operations on chat users and theirs messages.

## Available endpoint
The API provides endpoints that are handful to reach the expected result. Those are:
* [Messages](https://github.com/mWasyluk/spring-boot_messenger-api/blob/main/src/main/java/pl/wasyluva/spring_messengerapi/web/controller/MessageController.java)
  * GET - /messages/ - returns all persisted messages (insecure)
  * POST - /messages/send/{userUUID} - persists new message
  * PATCH - /messages/update/ - updates state of message if provided message UUID matches the persisted one
* [Profiles](https://github.com/mWasyluk/spring-boot_messenger-api/blob/main/src/main/java/pl/wasyluva/spring_messengerapi/web/controller/UserProfileController.java)
  * GET - /profiles/ - returns all persisted profiles (insecure)
  * GET - /profiles/{userUUID} - returns persisted profile with provided UUID
  * PATCH - /profiles/update - updates state of profile if provided profile UUID matches the persisted one

## Contents
Following links will help you to look around interesting parts of the project in a snap:
* [Properties](https://github.com/mWasyluk/spring-boot_messenger-api/blob/main/src/main/resources/application.properties)
* [Source code](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/main/java/pl/wasyluva/spring_messengerapi)
  * [Data](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/main/java/pl/wasyluva/spring_messengerapi/data)
  * [Domains](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/main/java/pl/wasyluva/spring_messengerapi/domain)
  * [Security](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/main/java/pl/wasyluva/spring_messengerapi/security)
  * [Web](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/main/java/pl/wasyluva/spring_messengerapi/web)
* [Tests](https://github.com/mWasyluk/spring-boot_messenger-api/tree/main/src/test/java/pl/wasyluva/spring_messengerapi)

## Dependencies
The dependencies used to build and run the project are:
* [Spring](https://spring.io/projects)*
  * [Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/)*
  * [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/index.html)*
  * [Spring Security](https://docs.spring.io/spring-security/reference/index.html)*
  * [Spring Data JPA (_Hibernate_)](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)*
* Tests
  * [JUnit](https://junit.org/junit5/docs/current/user-guide/)*
  * [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core.html)*
  * [Mockito](https://site.mockito.org/)*
* [Lombok](https://projectlombok.org/features/)*
* [PostgreSQL](https://jdbc.postgresql.org/documentation/use/)*

\* -<em> link to external website</em>
