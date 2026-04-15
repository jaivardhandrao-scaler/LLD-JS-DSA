# Decorator Pattern for API Enhancement

## Problem Statement

You are developing a RESTful API for an e-commerce platform. You want to add additional functionality to the API endpoints without cluttering the main API codebase with repetitive logic. The additional functionality includes logging incoming requests and responses, capturing metrics for each request, and authenticating requests before processing.

You aim to implement this additional functionality in a modular and reusable manner to ensure that it can be easily extended or modified in the future without impacting the core API logic.

## Assignment

Your task is to implement the Decorator pattern to enhance the RESTful API endpoints with additional functionality. The Decorator pattern allows behavior to be added to individual objects dynamically, without affecting the behavior of other objects of the same class. This pattern is particularly useful when you need to add or modify the behavior of objects at runtime.

### Build the decorator interface

You have been provided with a base class named `Api`, which represents the main API functionality. Your task is to implement the Decorator pattern by creating decorator classes that extend the `Api` class and add specific functionalities to the API endpoints.

1. Modify the `BaseApiDecorator` class to extend the `Api` class and act as the base class for all decorator classes.
2. The `BaseApiDecorator` class should have a constructor that takes an `Api` object as a parameter and sets it as an instance variable `api`.
3. You need to add the method `executeRequest()` to the `BaseApiDecorator` class to delegate the request execution to the wrapped `Api` object.

### Task 2 - Implement the decorator classes

You need to implement two decorator classes: `LoggingDecorator`, and `RateLimitDecorator`. Each decorator class should extend the `BaseApiDecorator` class and add a specific functionality to the API endpoints. The decorators should be responsible for logging incoming requests and rate-limiting the API endpoints, respectively.

In each decorator class, you need to call the relevant utility methods to add the desired functionality to the API endpoints. The utility methods are provided in the `ApiUtils` class and should be called from the `executeRequest()` method of the decorator classes.

### Instructions

1. In the `BaseApiDecorator` class, inherit from the `Api` class and implement the method `executeRequest()` to delegate the API endpoint operations to the wrapped `API` object. Remember to set the `api` instance variable in the constructor.
2. Implement the `LoggingDecorator` and `RateLimitDecorator` classes by extending the `BaseApiDecorator` class and adding the desired functionalities to the API endpoints.
3. For each decorator class, implement the necessary logic to add the desired functionality, such as logging incoming requests or rate-limiting the API endpoints. You can use the utility methods provided in the `ApiUtils` class for this purpose.
4. Run the provided test cases to verify the correctness of your implementation. You are not required to edit the test cases themselves.
