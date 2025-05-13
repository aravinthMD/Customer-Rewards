# Customer Rewards Calculation Program
**Requirement**
A retailer offers a rewards program to its customers,awarding points based on each recorder purchase.
A customer receives 2 points for every dollar spent over $100 in each transaction, plus 1 point for every dollar spent between $50 and $100 in each transaction.
(eg.a$120 purchase = 2x$20 + 1x$50 = 90 points)

Given a record of every transaction during three month period, calculate the reward points earned for each customer per month and total.

1) Solve using spring boot
2) create restful endpoint
3) make up a data set to best demonstrate your solution
4) check solution in GitHub

# Features
- Save Customers
- Update Customers
- Save Purchase orders based on Customers
- Calculate Reward Points based on Purchase Orders
- Fetch Customer and Monthly Reward Points Based on Monthly Transactions

# Tech Stack
- Java 17
- Spring Boot 3.4.5
- H2 Database

# IMPLEMENTATIONS

This project uses Spring Boot + Spring Restful API + JPA/H2 DB + Maven + Junit 5 + OpenAPI/Swagger

Swagger UI is at
http://localhost:8080/swagger-ui/index.html

API doc is at
http://localhost:8080/v3/api-docs/

To build project, run command from terminal with
mvn clean install

