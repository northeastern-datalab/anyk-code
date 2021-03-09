SET client_min_messages TO WARNING;

DROP TABLE IF EXISTS R1;
DROP TABLE IF EXISTS R2;
DROP TABLE IF EXISTS R3;
DROP TABLE IF EXISTS R4;

CREATE TABLE R1 (
	A1 integer,
	A2 integer,
	Weight1 numeric
);

CREATE TABLE R2 (
	A2 integer,
	A3 integer,
	Weight2 numeric
);

CREATE TABLE R3 (
	A3 integer,
	A4 integer,
	Weight3 numeric
);

CREATE TABLE R4 (
	A4 integer,
	A5 integer,
	Weight4 numeric
);

COPY R1
FROM '/home/nikos/any-k-code/Experiments/VLDB20/Synthetic_data/psql/inputs/R1.csv'
DELIMITER ' ' CSV HEADER;

COPY R2
FROM '/home/nikos/any-k-code/Experiments/VLDB20/Synthetic_data/psql/inputs/R2.csv'
DELIMITER ' ' CSV HEADER;

COPY R3
FROM '/home/nikos/any-k-code/Experiments/VLDB20/Synthetic_data/psql/inputs/R3.csv'
DELIMITER ' ' CSV HEADER;

COPY R4
FROM '/home/nikos/any-k-code/Experiments/VLDB20/Synthetic_data/psql/inputs/R4.csv'
DELIMITER ' ' CSV HEADER;

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

SELECT A1, A2, A3, A4, A5
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3 NATURAL JOIN R4
ORDER BY Weight1 + Weight2 + Weight3 + Weight4;

COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

SELECT A1, A2, A3, A4, A5
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3 NATURAL JOIN R4
ORDER BY Weight1 + Weight2 + Weight3 + Weight4;

COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

EXPLAIN ANALYZE SELECT A1, A2, A3, A4, A5
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3 NATURAL JOIN R4
ORDER BY Weight1 + Weight2 + Weight3 + Weight4;


COMMIT;

DROP TABLE R1;DROP TABLE R2;DROP TABLE R3;DROP TABLE R4;