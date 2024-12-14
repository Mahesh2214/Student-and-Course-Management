CREATE DATABASE student_course_db;
use student_course_db;
CREATE TABLE Courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_duration INT NOT NULL
);
select * from Courses;
CREATE TABLE Students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) DEFAULT 'Student',
    course_id INT,
    FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE SET NULL
);
select * from Student;
drop table course;
CREATE TABLE Course (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(50) UNIQUE NOT NULL,
    course_duration INT NOT NULL -- in months or weeks
);
drop table Student;
CREATE TABLE Student (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    course_id INT,
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE SET NULL
    
    
);

SHOW CREATE TABLE Course;
SHOW CREATE TABLE Student;
SET GLOBAL general_log = 'ON';
SELECT * FROM Course;
SELECT * FROM Student;


