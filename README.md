# Database Systems Project
This project simulates a hotel management system with full backend functionality built on relational database principles, supported by advanced SQL logic, trigger automation, and data visualization. 
Designed to model real-world operations, the project emphasizes data-driven decision-making, and process automation.
The project was built in a deliberate step-by-step process, involving a step by step contruction of an ERD and relational schema, which could then be used to construct an SQL written database.
The addition of automation component allowing users of the database to easily access and retrieve data.
Below is an overview of the skills demonstrated in this project. To see the ERD, Relational Schema and various query optimizations for this database see pdf.

**Relational Schema Design & Data Modeling:**
Designed normalized multi-table schema (Guests, Reservations, Staff, Rooms, etc.)
Modeled table inheritance (VIPGuest vs CasualGuest) and time-series consistency via DATE/TIME typing

**Stored Procedure Development for Monthly Financial Reporting:**
Created MonthlyAmenityReport stored procedure to track monthly amenity usage and revenue
Used SQL cursors, aggregates, and dynamic table generation for report creation

**Trigger-Based Process Automation:**
APPLYVIPDISCOUNT: Auto-upgrades guests to VIPs, applies tiered discount, and logs cost differences
ASSIGN_TO_VIP: Automatically assigns staff to VIPs based on discount tier

**Query Optimization with Indexing:**
Created indexes on RoomNum, CheckIn, CheckOut, and GID to speed up availability and JOIN queries

**Data Visualization and Insight Generation:**
SQL and Excel charts created as query output:
RoomType bookings by month
Total monthly spend trends
Extracted seasonality patterns and strategic insights

**Java Application Integration - LI application to:**
Create/cancel reservations
Apply VIP logic
Search guest and room info
Ensured business rules (capacity, discount, availability) were validated via SQL


