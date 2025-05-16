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

# API Details
- **To save Customer** -'http://localhost:8080/api/saveCustomer'
- **Method**: POST
  --data
```json
{
  "firstName" : "Aravinth",
  "lastName" : "MD",
  "email" : "aravinth.md@gmail.com",
  "phone" : "8754809950",
  "address" : "123, Park Street"
}
```

- **Response**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "firstName": "Aravinth",
    "lastName": "MD",
    "email": "aravinth.md@gmail.com",
    "phone": "8754809950",
    "address": "123, Park Street"
  },
  "message": "Customer created successfully.",
  "timestamp": 1747121901708
}
```
- **To Update Customer** -'http://localhost:8080/api/{customerId}/updateCustomer'
- **Method**: PUT\
  --data
```json
{

  "firstName" : "Aravinth",
  "lastName" : "MD",
  "email" : "aravinth.md@gmail.com",
  "phone" : "8754809950",
  "address" : "123, Northern Park Street"

}
  ```
- **Response**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "firstName": "Aravinth",
    "lastName": "MD",
    "email": "aravinth.md@gmail.com",
    "phone": "8754809950",
    "address": "123, Northern Park Street"
  },
  "message": "Customer Updated successfully.",
  "timestamp": 1747121928560
}
```


- **To save Purchase Order** -'http://localhost:8080/api/{customerId}/saveReward'
- **Method**: POST

  --data
```json
{
  "purchaseAmount": 120,
  "purchaseDate": "2025-04-15T02:55:19.963Z"
}
```
- **Response**:
```json
 {
  "status": "success",
  "data": {
    "purchaseAmount": 120,
    "rewardPoints": 90.0,
    "customerDetail": {
      "id": 1,
      "firstName": "Aravinth",
      "lastName": "MD",
      "email": "aravinth.md@gmail.com",
      "phone": "8754809950",
      "address": "ddd"
    },
    "purchaseDate": "2025-04-15"
  },
  "message": "Transactions Saved successfully.",
  "timestamp": 1747121981689
}
```
- **To get Customer Monthy Transactional Reward Points** --location 'http://localhost:8080/api/{customerId}/monthly-transactions' \
    - **Method**: GET
        - To populate past three months data 
        - *Response*
```json
{
  "customerId": 1,
  "firstName": "Aravinth",
  "lastName": "MD",
  "email": "aravinth.md@gmail.com",
  "phone": "8754089950",
  "address": "123, Park Street",
  "monthlyRecords": [
    {
      "rewardPoints": 30.0,
      "date": "May 12, 2025"
    },
    {
      "rewardPoints": 270.0,
      "date": "June 25, 2025"
    }
  ],
  "totalRewards": 300.0
}
```
