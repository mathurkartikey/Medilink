DROP DATABASE IF EXISTS medilink_schema;

CREATE DATABASE medilink_schema;
USE medilink_schema;

CREATE TABLE doctors (
    id INT,
    name VARCHAR(100),
    specialization VARCHAR(100)
);

CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    gender VARCHAR(20)
);

DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  patient_id VARCHAR(100),
  doctor_id VARCHAR(100),
  date DATE
);


CREATE TABLE prescriptions (
    id INT,
    appointment_id INT,
    details TEXT
);

INSERT INTO doctors VALUES (1, 'Dr. Sharma', 'Dermatologist');
INSERT INTO doctors VALUES (2, 'Dr. Gupta', 'Cardiologist');

