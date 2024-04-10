# Gyro
A lightweight, resource-efficient asynchronous rate limiter specialized on client side.

### Introduction
Rate limiting is a critical component for maintaining the stability and reliability of server resources. In the realm of Java applications, well-established libraries such as Guava, Bucket4j, among others, offer robust solutions for managing incoming requests. These libraries excel in scenarios where an application acts as a server, enabling it to efficiently reject excessive requests from clients, typically end-users.

However, a less discussed but equally important challenge arises when the tables are turned - What happens when your application needs to impose limits on its outgoing requests? The pursuit of performance optimization is a common goal, yet it becomes nuanced when interfacing with external services. The conventional libraries mentioned above primarily adopt a synchronous approach. This means that in the event of request limitation, the client's only recourse is to wait—a strategy that may lead to thread wastage through either idle waiting or, in less ideal scenarios, active polling (also known as busy waiting).

Gyro simplifies rate limiting for outbound requests with a focus on conserving system resources. By submitting your tasks to Gyro, it manages them with an asynchronous approach, ensuring your application runs smoothly with optimal memory and CPU usage. Designed to be lightweight, Gyro aims to enhance your application’s functionality without the burden of high resource consumption.
