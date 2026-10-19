create database Customer;
use Customer;

#   cd /mnt/c/Users/PC/OneDrive/Documents/MobileDb/transaction-server/customer-frontend
#  cd /mnt/c/Users/PC/OneDrive/Documents/MobileDb/transaction-server/backend   node backend/server.js




select * from customers;
select * from country_codes;

CREATE TABLE customers (
  customer_ID INT NOT NULL AUTO_INCREMENT,
  customer_passwordID VARCHAR(50),
  customer_name VARCHAR(50),
  customer_Phone_No VARCHAR(15),
  customer_email VARCHAR(50),
  customer_primary_country VARCHAR(15),
  customer_secondary_country VARCHAR(15),
  customer_major_transactions VARCHAR(50),
  customer_description VARCHAR(15),
  PRIMARY KEY (customer_ID)
);
ALTER TABLE customers ADD COLUMN theme_color VARCHAR(20);


drop table customers;

select *
from customers;

truncate table customers;

CREATE TABLE country_codes (
  country_name VARCHAR(50) PRIMARY KEY,
  country_code INT NOT NULL
);

INSERT INTO country_codes VALUES
('Kenya', 254),
('Congo', 243),
('Uganda', 256),
('Tanzania', 255),
('Rwanda', 250);
select * from country_codes;

create table major_customers(
customer_name varchar(50),
customer_PhoneNo varchar(15),
total_transactions varchar(50),
foreign key(customer_name) references customers(ccustomer_ID),
foreign key(customer_PhoneNo) references customers(customer_Phone_No)
);

















CREATE TABLE customer_deletes (
    delete_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    customer_primary_country VARCHAR(100),
    customer_secondary_country VARCHAR(100),
    customer_major_transactions TEXT,
    customer_description TEXT,
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_by VARCHAR(100) -- optional: track user who deleted
);

select *
from customers;
ALTER TABLE customers ADD CONSTRAINT unique_email UNIQUE (customer_email);


SELECT customer_passwordID FROM customers WHERE customer_email='TNTHAWER1@GMAIL.COM';
select *
from customer_deletes;
truncate table customer_deletes;
truncate table customers;
SHOW TABLES;





DELIMITER //
CREATE TRIGGER before_customer_delete
BEFORE DELETE ON customers
FOR EACH ROW
BEGIN
    INSERT INTO customer_deletes (
        customer_id,
        customer_name,
        customer_phone,
        customer_email,
        customer_primary_country,
        customer_secondary_country,
        customer_major_transactions,
        customer_description
    ) VALUES (
        OLD.customer_ID,
        OLD.customer_name,
        OLD.customer_Phone_No,
        OLD.customer_email,
        OLD.customer_primary_country,
        OLD.customer_secondary_country,
        OLD.customer_major_transactions,
        OLD.customer_description
    );
END;
//
DELIMITER ;
drop trigger before_customer_delete;


INSERT INTO customers (
    customer_ID,
    customer_name,
    customer_Phone_No,
    customer_email,
    customer_primary_country,
    customer_secondary_country,
    customer_major_transactions,
    customer_description
)
SELECT customer_id,
       customer_name,
       customer_phone,
       customer_email,
       customer_primary_country,
       customer_secondary_country,
       customer_major_transactions,
       customer_description
FROM customer_deletes
WHERE customer_id = 2;

