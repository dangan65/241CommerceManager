E-Commerce Order and Inventory Manager
A Java Swing clothing store management system with inventory control, order processing, and analytics.
Authors: Dan Gannon, Liam Cahill
Course: CSC 241-03, Fall 2025

Features

User authentication with role-based access (Admin/Customer/Guest)
Shopping cart with real-time stock validation
State-based tax calculation (all 50 US states)
Queue-based order processing workflow
Admin inventory management with undo functionality
Sales reports and analytics
Data persistence to text files

Data Structures

HashMap - User auth, order lookup (O(1))
ArrayList - Product lists, cart items
Stack - Undo operations, recently viewed orders
Queue - FIFO order processing
PriorityQueue - Min-heap price sorting

Quick Start
bashjavac *.java
java Main
Test Accounts
Admin: admin / admin123
User: Dan / dan
Guest: Click "Continue as Guest"
Core Files

Main.java - Entry point
LoginWindow.java - Authentication
ProductStoreFrame.java - Main window
ShopPanel.java - Shopping interface
AdminPanel.java - Inventory management
OrdersHistory.java - Queue/Stack implementation
DataPersistence.java - File I/O

Requirements

Java 11+
No external libraries

Data Files
Auto-generated on first run:

users.txt - User credentials
products.txt - Inventory
orders.txt - Order history
