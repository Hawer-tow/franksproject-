use kisenyi;
select * from open_and_close ;
select * from buying;
select * from selling;
select * from users; 
select * from expenses;

use customer;
select * from customers;
select * from country_codes;
select * from Ugx2kenyarates;
select * from kenya2Ugxrates;
select * from Ugx2congorates;
select * from congo2Ugxrates;
select * from Ugx2tanzaniarates;
select * from tanzania2Ugxrates;
select * from Ugx2rwandarates;
select * from rwanda2Ugxrates;



CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(50) DEFAULT 'user',
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    access_status TINYINT(1) DEFAULT 0,
    biometric_enabled BOOLEAN DEFAULT FALSE,
    biometric_key_alias VARCHAR(255),
	device_id VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE
);



INSERT INTO users (username, password_hash, email, role)
VALUES ('john', '', 'john@example.com', 'USER');
#   $2a$10$9S8tObaiJBN0Hyxrg0HJweNie0Ode9vkaD0Tzwq2xn2XOW2789OQG
#	$2a$10$gHWta4Ar/d1lzoWbX41U0ucUijsvrAYKyrX8IHkn3rihkiN.YxPR2

select * from users;

truncate table users;
SELECT id, username, password_hash FROM users WHERE password_hash='$2a$10$G7Jd86VN2QcENQs2D3TJeO74.Mf/TsKyxWzGtSxSKw8MZjd6yHA1O';
SELECT id, username, password_hash FROM users WHERE password_hash='$2a$10$xHWFT2al7ewyesO8gryaludGDF4GWJxf0BcOyGB7DZvXVc1RNA9hu';

-- Admin user
INSERT INTO users (id, username, password_hash, email, role)VALUES
(1, 'Winter', '$2a$10$7Q0kYQhYlZkzFZkzFZkzF.ZkzFZkzFZkzFZkzFZkzFZkzFZkzFZkz', 'admin@example.com', 'ADMIN'),
(2, 'John', '$2a$10$7Q0kYQhYlZkzFZkzFZkzF.ZkzFZkzFZkzFZkzFZkzFZkzFZkzFZkz', 'john@example.com', 'USER'),
(3, 'Mary', '$2a$10$7Q0kYQhYlZkzFZkzFZkzF.ZkzFZkzFZkzFZkzFZkzFZkzFZkzFZkz', 'mary@example.com', 'USER'),
(4, 'Winnie', '4', 'mary@example.com', 'USER');

/*(5, 'Brown', '5', 'mary@example.com', 'USER'),
(6, 'maze', '6', 'mary@example.com', 'USER'),
(7, 'Hawer', '7', 'mary@example.com', 'USER'),
(8, 'Mamu', '8', 'mary@example.com', 'USER'); */

create database Customer;
use Customer;





create table Ugx2kenyarates(
Ugx2kenyaratedate date,
Ugx2kenyarate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (Ugx2kenyaratedate)
);
create table kenya2Ugxrates(
kenya2Ugxratedate date,
kenya2Ugxrate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (kenya2Ugxratedate)
);



create table Ugx2congorates(
Ugx2congoratedate date,
Ugx2congorate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (Ugx2congoratedate)
);
create table congo2Ugxrates(
congo2Ugxratedate date,
congo2Ugxrate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (congo2Ugxratedate)
);



create table Ugx2tanzaniarates(
Ugx2tanzaniaratedate date,
Ugx2tanzaniarate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (Ugx2tanzaniaratedate)
);
create table tanzania2Ugxrates(
tanzania2Ugxratedate date,
tanzania2Ugxrate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (tanzania2Ugxratedate)
);



create table Ugx2rwandarates(
Ugx2rwandaratedate date,
Ugx2rwandarate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (Ugx2rwandaratedate)
);
create table rwanda2Ugxrates(
rwanda2Ugxratedate date,
rwanda2Ugxrate  decimal (5,2) not null,
min_transactionvalue  decimal (12,2) not null,
max_transactionValue  decimal (12,2) not null,
primary key (rwanda2Ugxratedate)
);

select *
from Ugx2kenyarates;
select *
from kenya2Ugxrates;
select *
from Ugx2congorates;
select*
from congo2Ugxrates;
select *
from Ugx2tanzaniarates;
select *
from tanzania2Ugxrates;
select *
from Ugx2rwandarates;
select *
from rwanda2Ugxrates;
/*-----------------------------------------------------------------------------------------------------------------------------------------------------*/





create database Kisenyi;
use kisenyi;

SHOW VARIABLES WHERE Variable_name = 'hostname';
show variables where variable_name='localhost';
show variables where variable_name='ip_address';
select user();
select @@hostname;
-- server;

SHOW VARIABLES LIKE 'have_ssl';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'StrongPass123!';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '<your-root-password>';

CREATE TABLE selling (
    Selling_ID INT NOT NULL AUTO_INCREMENT,
    Selling_date DATE NOT NULL,
    Selling_amount DECIMAL(12,2),
    Selling_rate DECIMAL(5,2) NOT NULL,
    Receiving_Country VARCHAR(50),
    Phone_No VARCHAR(15),
    Recipient_name VARCHAR(70),
    Sell_Description VARCHAR(40),

    -- Bank-related fields
    Sender_Account VARCHAR(30),
    Receiver_Account VARCHAR(30),
    Bank_Code VARCHAR(20),
    Transaction_ID VARCHAR(50),
    Currency VARCHAR(10),
    
    -- safaricom related fields
	provider_http_status VARCHAR(255),
	provider_response TEXT,


    PRIMARY KEY (Selling_ID)
);


drop table selling;
CREATE TABLE selling (
    Selling_ID INT NOT NULL AUTO_INCREMENT,
    Selling_date DATETIME NOT NULL,
    Selling_amount DECIMAL(12,2),
    Selling_rate DECIMAL(5,2) NOT NULL,
    Receiving_Country VARCHAR(50),
    Phone_No VARCHAR(15),
    Recipient_name VARCHAR(70),
    Sell_Description VARCHAR(255),

    -- Bank-related fields
    Sender_Account VARCHAR(30),
    Receiver_Account VARCHAR(30),
    Bank_Code VARCHAR(20),
    Currency VARCHAR(10),

    -- Safaricom related fields
    Transaction_ID VARCHAR(50),
    OriginatorConversationID VARCHAR(100),
    ConversationID VARCHAR(100),
    Status VARCHAR(255),
    provider_http_status VARCHAR(255),
    provider_response TEXT,
    originator_conversation_id VARCHAR(255),

    PRIMARY KEY (Selling_ID)
);

select * from selling;
select * from selling_deleted; 

ALTER TABLE selling MODIFY Status VARCHAR(255);
ALTER TABLE selling
  ADD COLUMN originator_conversation_id VARCHAR(255);
ALTER TABLE selling ADD COLUMN SaleType VARCHAR(30) AFTER Status;

describe selling;





truncate table selling;
truncate table selling_deleted;

CREATE TABLE selling_deleted (
    Selling_ID INT,
    Selling_date DATETIME,
    Selling_amount DECIMAL(12,2),
    Selling_rate DECIMAL(5,2),
    Receiving_Country VARCHAR(50),
    Phone_No VARCHAR(15),
    Recipient_name VARCHAR(70),
    Sell_Description VARCHAR(40),
    Sender_Account VARCHAR(30),
    Receiver_Account VARCHAR(30),
    Bank_Code VARCHAR(20),
    Currency VARCHAR(10),
    Transaction_ID VARCHAR(50),
    OriginatorConversationID VARCHAR(100),
    ConversationID VARCHAR(100),
    Status VARCHAR(255),
    provider_http_status VARCHAR(255),
    provider_response TEXT,
    originator_conversation_id VARCHAR(255),
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


DELIMITER $$

CREATE TRIGGER before_selling_delete
BEFORE DELETE ON selling
FOR EACH ROW
BEGIN
    INSERT INTO selling_deleted (
        Selling_ID, Selling_date, Selling_amount, Selling_rate,
        Receiving_Country, Phone_No, Recipient_name, Sell_Description,
        Sender_Account, Receiver_Account, Bank_Code, Currency,
        Transaction_ID, OriginatorConversationID, ConversationID,
        Status, provider_http_status, provider_response, originator_conversation_id
    )
    VALUES (
        OLD.Selling_ID, OLD.Selling_date, OLD.Selling_amount, OLD.Selling_rate,
        OLD.Receiving_Country, OLD.Phone_No, OLD.Recipient_name, OLD.Sell_Description,
        OLD.Sender_Account, OLD.Receiver_Account, OLD.Bank_Code, OLD.Currency,
        OLD.Transaction_ID, OLD.OriginatorConversationID, OLD.ConversationID,
        OLD.Status, OLD.provider_http_status, OLD.provider_response, OLD.originator_conversation_id
    );
END$$

DELIMITER ;









create table buying (

Buying_ID int not null auto_increment,
Buying_date date not null,
Buying_amount decimal (12,2),
Buying_rate decimal (5,2) not null,
Transacting_Country varchar (50),
Buyer_name varchar (70),
Buy_Description varchar(255),
Checkout_Request_ID VARCHAR(255),

primary key(Buying_ID)

);



select * from selling;
select * from buying;

ALTER TABLE selling MODIFY Sell_Description VARCHAR(255);

describe selling;
describe buying;
create table open_and_close (

date date not null,
uganda_opening decimal (12,2),
kenya_opening decimal (12,2),
uganda_cash_in decimal (12,2),
kenya_cash_in decimal (12,2),
uganda_closing decimal (12,2),
kenya_closing decimal (12,2),
description varchar(40),

primary key(date)
);

CREATE TABLE expenses (
    expences_ID INT NOT NULL AUTO_INCREMENT,
    expences_date DATE NOT NULL,
    expense_amount DECIMAL(12,2),
    expense_Description VARCHAR(40),
    PRIMARY KEY (expences_ID)
);

select * from open_and_close; 

/*Town Branch ------------------------------------------------------------------------*/

CREATE table withdraw  (
Tid decimal(10,1)  ,
Tdate date not null,
magent decimal(12,3),
mpersonal decimal(12,3),
aagent decimal(12,3),
apersonal decimal(12,3),
EU decimal(12,3),
EUrate decimal(7,3),
EA decimal(12,3),
EArate decimal(7,3),
JA decimal(12,3),
JArate decimal(7,3),
MU decimal(12,3),
MUrate decimal(7,3),
MA decimal(12,3),
MArate decimal(7,3),
AO decimal(12,3),
AOrate decimal(7,3),
AA decimal(12,3),
AArate decimal(7,3),
TZ decimal(12,3),
TZrate decimal(7,3),
Rwanda decimal(12,3),
RR decimal(7,3),
primary key (Tid)
);

create table deposit(
Tid decimal(10,1) ,
Tdate date not null,
magent decimal(12,3),
mpersonal decimal(12,3),
aagent decimal(12,3),
apersonal decimal(12,3),
EU decimal(12,3),
EUrate decimal(7,3),
EA decimal(12,3),
EArate decimal(7,3),
JA decimal(12,3),
JArate decimal(7,3),
MU decimal(12,3),
MUrate decimal(7,3),
MA decimal(12,3),
MArate decimal(7,3),
AO decimal(12,3),
AOrate decimal(7,3),
AA decimal(12,3),
AArate decimal(7,3),
TZ decimal(12,3),
TZrate decimal(7,3),
Rwanda decimal(12,3),
RR decimal(7,3),
primary key (Tid)
);

create table commission(
Cid decimal(10,1),
CD date,
CI decimal(15,5),
CO decimal(15,5),

primary key(Cid) 
);


insert into deposit values(0,'2025-7-8',200,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into deposit values(1,'2025-7-8',300,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into deposit values(2,'2025-7-8',400,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into deposit values(3,'2025-7-8',500,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);

insert into withdraw values(0,'2025-7-8',200,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into withdraw values(1,'2025-7-8',300,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into withdraw values(2,'2025-7-8',400,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
insert into withdraw values(3,'2025-7-8',500,300,120,300,3000,30,2000,12,4000,12,3000,12,5300,23,3499,12,3900,23,3400,12,35200,12);
select 
Tid as Withdraw_Transaction_ID ,
Tdate as Transaction_Date,
magent as MTN_Agent,
mpersonal as MTN_Personal,
aagent as Airtel_Agent,
apersonal Airtel_Personal,
EU as Equity_Uncle,
EUrate as Equity_Uncle_Rate,
EA as Equity_Aunt,
EArate as Equity_Aunt_Rate,
JA as Joint_Account,
JArate as Joint_Account_Rate,
MU as Mpesa_Uncle,
MUrate as Mpesa_Uncle_Rate,
MA as Mpesa_Aunt ,
MArate as Mpesa_Aunt_Rate,
AO as Agent_Order,
AOrate as Agent_Order_Rate ,
AA as Agent_Airtel,
AArate as Agent_Airtel_Rate,
TZ as Tanzania,
TZrate as Tanzania_Rate,
Rwanda as Rwanda,
RR as Rwanda_Rate
from withdraw;
