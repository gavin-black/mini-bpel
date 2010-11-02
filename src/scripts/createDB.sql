drop database if exists bpel_engine;
create database bpel_engine;
use bpel_engine;

create table bpel_process( 
		  id INT NOT NULL AUTO_INCREMENT, 
		  PRIMARY KEY(id), 
		  state varchar(10), 
		  currentType varchar(32), 
		  currentName varchar(300),
		  bpel LONGTEXT 
);

create table current_parents( 
		  id INT NOT NULL,
		  type varchar(32), 
		  name varchar(300), 
		  PRIMARY KEY(id, type, name)
);

create table variables(
		  id INT NOT NULL,
		  name varchar(256) NOT NULL, 
		  PRIMARY KEY(id, name), 
		  type varchar(32), 
		  value varchar(40000), 
		  large_value BLOB 
);

create table fault_handlers(
		  id INT NOT NULL,
		  name varchar(256) NOT NULL, 
		  PRIMARY KEY(id, name) 
);

create table log(
		  num INT NOT NULL AUTO_INCREMENT,
		  PRIMARY KEY(num), 
		  id INT NOT NULL,
		  ip varchar(32), 
		  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
		  type varchar(32), 
		  name varchar(300), 
		  description varchar(500) 
); 
