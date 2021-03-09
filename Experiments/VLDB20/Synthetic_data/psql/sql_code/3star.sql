SET client_min_messages TO WARNING;

DROP TABLE IF EXISTS R1;
DROP TABLE IF EXISTS R2;
DROP TABLE IF EXISTS R3;

CREATE TABLE R1 (
	A1 integer,
	A2 integer,
	Weight1 numeric
);

CREATE TABLE R2 (
	A1 integer,
	A3 integer,
	Weight2 numeric
);

CREATE TABLE R3 (
	A1 integer,
	A4 integer,
	Weight3 numeric
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

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

SELECT A1, A2, A3, A4
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3
ORDER BY Weight1 + Weight2 + Weight3;

COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

SELECT A1, A2, A3, A4
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3
ORDER BY Weight1 + Weight2 + Weight3;

COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

EXPLAIN ANALYZE SELECT A1, A2, A3, A4
FROM R1 NATURAL JOIN R2 NATURAL JOIN R3
ORDER BY Weight1 + Weight2 + Weight3;


COMMIT;

DROP TABLE R1;DROP TABLE R2;DROP TABLE R3;