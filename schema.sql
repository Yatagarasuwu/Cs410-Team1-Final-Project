CREATE TABLE Student (
	studentID VARCHAR(20) PRIMARY KEY,
	username VARCHAR(50) NOT NULL,
	name VARCHAR(100) NOT NULL
);

CREATE TABLE Class (
	classID VARCHAR(20) PRIMARY KEY,  
	courseNumber VARCHAR(10) NOT NULL,
	term VARCHAR(10) NOT NULL,
	sectionNumber VARCHAR(10),
	description TEXT
);

CREATE TABLE Category (
	categoryID INT PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(50) NOT NULL,
	weight DECIMAL(5,2) NOT NULL,  
	classID VARCHAR(20) NOT NULL,
	FOREIGN KEY (classID) REFERENCES Class(classID)
);

CREATE TABLE Assignment (
	assignmentID INT PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
	description TEXT,
	pointValue DECIMAL(6,2) NOT NULL,
	categoryID INT NOT NULL,
	classID VARCHAR(20) NOT NULL,
	FOREIGN KEY (categoryID) REFERENCES Category(categoryID),
	FOREIGN KEY (classID) REFERENCES Class(classID),
	CONSTRAINT uniq_assignment_name_per_class UNIQUE (classID, name)
);

CREATE TABLE Enrollment (
	studentID VARCHAR(20),
	classID VARCHAR(20),
	PRIMARY KEY (studentID, classID),
	FOREIGN KEY (studentID) REFERENCES Student(studentID),
	FOREIGN KEY (classID) REFERENCES Class(classID)
);

CREATE TABLE Grade (
	studentID VARCHAR(20),
	assignmentID INT,
	score DECIMAL(6,2) NOT NULL,
	PRIMARY KEY (studentID, assignmentID),
	FOREIGN KEY (studentID) REFERENCES Student(studentID),
	FOREIGN KEY (assignmentID) REFERENCES Assignment(assignmentID)
);

CREATE INDEX idx_category_class ON Category(classID);
CREATE INDEX idx_assignment_class ON Assignment(classID);
CREATE INDEX idx_assignment_category ON Assignment(categoryID);
CREATE INDEX idx_grade_assignment ON Grade(assignmentID);
CREATE INDEX idx_enrollment_student ON Enrollment(studentID);
CREATE INDEX idx_enrollment_class ON Enrollment(classID);


