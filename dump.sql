-- MySQL dump
-- Dump completed on 2025-04-13 20:24:10

CREATE DATABASE IF NOT EXISTS gradebook_db;
USE gradebook_db;

-- Insert Students
INSERT INTO Student (studentID, username, name) VALUES
('S001', 'jdoe', 'John Doe'),
('S002', 'asmith', 'Alice Smith'),
('S003', 'bwayne', 'Bruce Wayne');

-- Insert Classes
INSERT INTO Class (classID, courseNumber, term, sectionNumber, description) VALUES
('CL001', 'CS410', 'Sp20', '1', 'Databases'),
('CL002', 'CS420', 'Fa20', '1', 'Operating Systems');

-- Insert Enrollment
INSERT INTO Enrollment (studentID, classID) VALUES
('S001', 'CL001'),
('S002', 'CL001'),
('S003', 'CL001');

-- Insert Categories
INSERT INTO Category (categoryID, name, weight, classID) VALUES
(1, 'Homework', 40.0, 'CL001'),
(2, 'Exams', 60.0, 'CL001');

-- Insert Assignments
INSERT INTO Assignment (assignmentID, name, description, pointValue, categoryID, classID) VALUES
(1, 'HW1', 'Homework 1 description', 100.0, 1, 'CL001'),
(2, 'HW2', 'Homework 2 description', 100.0, 1, 'CL001'),
(3, 'Midterm', 'Midterm Exam', 200.0, 2, 'CL001'),
(4, 'Final', 'Final Exam', 200.0, 2, 'CL001');

-- Insert Grades
INSERT INTO Grade (studentID, assignmentID, score) VALUES
('S001', 1, 95.0),
('S001', 2, 90.0),
('S001', 3, 180.0),
('S001', 4, 190.0),

('S002', 1, 88.0),
('S002', 2, 92.0),
('S002', 3, 170.0),
('S002', 4, 185.0),

('S003', 1, 100.0),
('S003', 2, 100.0),
('S003', 3, 200.0),
('S003', 4, 200.0);
