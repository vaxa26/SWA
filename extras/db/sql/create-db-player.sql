CREATE ROLE player LOGIN PASSWORD 'p';
CREATE DATABASE player;
GRANT ALL ON DATABASE player TO player;
CREATE TABLESPACE playerspace OWNER player LOCATION '/var/lib/postgresql/tablespace/player';
