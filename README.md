# Club_Membership_Management_System_

## Project Overview
The **Club Membership Management System** is a Java-based application developed to manage college clubs and student memberships. It helps colleges create and manage clubs, maintain student records, and track students joining different clubs.

The application uses Java Swing to provide a simple graphical user interface (GUI).

## Problem Statement
A college wants to maintain a list of student clubs and manage students joining those clubs. The system should allow administrators to create and manage club records, maintain student details, control club membership capacity, and identify clubs with excess members.

## Technologies Used
- **Programming Language:** Java
- **GUI:** Java Swing
- **Data Storage:** File Handling
- **Collections:** ArrayList

## Features

### 1. Club Management
- Create new clubs.
- View all club details.
- Edit existing club records.
- Delete club records.

### 2. Student Management
- Add new student records.
- Store student ID and name.
- View student details and their club membership status.

### 3. Membership Management
- Allow students to join clubs.
- Display members of each club.
- A student can join only one club at a time.

### 4. Club Capacity Management
- Each club has a maximum capacity of 30 members.
- Reject new registrations when a club reaches its maximum capacity.
- Display the membership status as Available or Full.

### 5. Existing Excess Members
- Identify clubs that already have more than 30 members.
- Display an alert for clubs with excess members.
- Existing members are not removed from the club.

### 6. Additional Features
- Search clubs by ID, name, or description.
- Display club membership counts.
- Import student details from TXT and CSV files.
- Save and load records using file handling.

## Application Modules
1. Clubs
2. Students
3. Membership
4. Search
5. Upload Folder

## How to Run
1. Install Java JDK.
2. Download or clone this repository.
3. Open the project in any Java IDE or terminal.
4. Compile the program:

   ```bash
   javac ClubManagementGUI.java
   ```

5. Run the program:

   ```bash
   java ClubManagementGUI
   ```

## Conclusion
The Club Membership Management System provides a simple way to manage college clubs and student memberships. It helps maintain student records, control club capacity, and identify clubs with excess members using Java and basic programming concepts.
