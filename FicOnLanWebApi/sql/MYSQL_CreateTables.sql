-- Indexes for primary keys have been explicitly created.
 
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS Session;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS EmailTemplate;
DROP TABLE IF EXISTS Event;
SET foreign_key_checks = 1;
 
-- ------------------------------ Session  -------------------------------------

CREATE TABLE Session (
	Session_id                  varchar(100)  NOT NULL  ,
	Session_user_id				bigint UNSIGNED NOT NULL ,
	Session_lastAccess			timestamp ,
	CONSTRAINT Session_PK PRIMARY KEY ( Session_id ) 
) engine=InnoDB;

-- ------------------------------ User -------------------------------------

CREATE TABLE User ( 
	User_id                      bigint UNSIGNED NOT NULL  AUTO_INCREMENT,
	User_permissions             varchar(16) NOT NULL DEFAULT "",
	User_name                    varchar(250)  NOT NULL  ,
	User_login                   varchar(200) NOT NULL  ,
	User_password                varchar(200) NOT NULL  ,
	User_secondPassword          varchar(200) NOT NULL  ,
	User_secondPasswordExpDate   timestamp ,
	User_dni                     varchar(11)  NOT NULL ,
	User_email                   varchar(200)  NOT NULL  ,
	User_telf                    varchar(15) , 
	User_shirtSize               varchar(3) ,
	User_borndate                datetime ,
	User_language                varchar(5)  NOT NULL,
	CONSTRAINT User_PK PRIMARY KEY ( User_id ) ,
	CONSTRAINT User_login_UNIQUE UNIQUE ( User_login )  ,
	CONSTRAINT User_dni_UNIQUE UNIQUE ( User_dni )  ,
	CONSTRAINT User_email_UNIQUE UNIQUE ( User_email )  
 ) engine=InnoDB;

 CREATE INDEX UserIndexByUser_login ON User (User_login);
 CREATE INDEX UserIndexByUser_dni ON User (User_dni); 
 CREATE INDEX UserIndexByUser_email ON User (User_email);

-- ------------------------------ EVENT -------------------------------------

CREATE TABLE Event ( 
	Event_id                           bigint UNSIGNED NOT NULL AUTO_INCREMENT,
	Event_name                         varchar(150) NOT NULL  ,
	Event_description                  TEXT    ,
	Event_num_participants             int DEFAULT 1 ,
	Event_minimunAge                   int DEFAULT 0 ,
	Event_price                        int DEFAULT 0 ,
	Event_reg_date_open                datetime NOT NULL  ,
	Event_reg_date_close               datetime NOT NULL  ,
	Event_setPaidTemplate_id           bigint UNSIGNED ,
	Event_onQueueTemplate_id           bigint UNSIGNED ,
	Event_outstandingTemplate_id       bigint UNSIGNED ,
	Event_outOfDateTemplate_id         bigint UNSIGNED ,
	Event_fromQueueToOutstanding_id    bigint UNSIGNED ,
	Event_rules                        TEXT,
	CONSTRAINT pk_event PRIMARY KEY ( Event_id ) ,
	CONSTRAINT Event_name_UNIQUE UNIQUE ( Event_name )
 ) engine=InnoDB;
 
 CREATE INDEX EventIndexByEvent_Name ON Event (Event_name);
 
-- ------------------------------ EmailTemplate -------------------------------------

CREATE TABLE EmailTemplate (
	EmailTemplate_id             bigint UNSIGNED NOT NULL  AUTO_INCREMENT,
	EmailTemplate_name           varchar(128) NOT NULL UNIQUE, 
	EmailTemplate_file           varchar(128), 
	EmailTemplate_fileName       varchar(128), 
	EmailTemplate_case           varchar(128) NOT NULL,
	EmailTemplate_body           TEXT,
    CONSTRAINT pk_email PRIMARY KEY(EmailTemplate_id)
) engine=InnoDB;

CREATE INDEX EmailTemplateByname ON EmailTemplate (EmailTemplate_name);
 

 
 ALTER TABLE Session ADD CONSTRAINT fk_session_user FOREIGN KEY ( Session_user_id ) REFERENCES User (User_id) ON DELETE CASCADE ON UPDATE CASCADE;
 
 ALTER TABLE Event ADD CONSTRAINT fk_event_setPaidTemplate FOREIGN KEY ( Event_setPaidTemplate_id ) REFERENCES EmailTemplate (EmailTemplate_id) ON DELETE SET NULL ON UPDATE CASCADE;
 ALTER TABLE Event ADD CONSTRAINT fk_event_onQueueTemplate FOREIGN KEY ( Event_onQueueTemplate_id ) REFERENCES EmailTemplate (EmailTemplate_id) ON DELETE SET NULL ON UPDATE CASCADE;
 ALTER TABLE Event ADD CONSTRAINT fk_event_outstandingTemplate FOREIGN KEY (Event_outstandingTemplate_id ) REFERENCES EmailTemplate (EmailTemplate_id) ON DELETE SET NULL ON UPDATE CASCADE;
 ALTER TABLE Event ADD CONSTRAINT fk_event_outOfDateTemplate FOREIGN KEY ( Event_outOfDateTemplate_id ) REFERENCES EmailTemplate (EmailTemplate_id) ON DELETE SET NULL ON UPDATE CASCADE;
 ALTER TABLE Event ADD CONSTRAINT fk_event_fromQueueToOutstanding FOREIGN KEY ( Event_fromQueueToOutstanding_id ) REFERENCES EmailTemplate (EmailTemplate_id) ON DELETE SET NULL ON UPDATE CASCADE;
 