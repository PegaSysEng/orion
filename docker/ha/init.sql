CREATE USER orion;

CREATE DATABASE payloaddb;
GRANT ALL PRIVILEGES ON DATABASE payloaddb TO orion;

\connect payloaddb
CREATE TABLE store (
  key char(60),
  value bytea,
  primary key(key)
);
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO orion;


CREATE DATABASE knownnodes;
GRANT ALL PRIVILEGES ON DATABASE knownnodes TO orion;

\connect knownnodes
CREATE TABLE store (
  key char(60),
  value bytea,
  primary key(key)
);
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO orion;