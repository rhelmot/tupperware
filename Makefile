all:
	javac src/* -d bin

run:
	java -cp lib/ojdbc6.jar:./bin Tupperware

test:
	java -cp lib/ojdbc6.jar:./bin TestTupperware

setup:
	./scripts/sql ./scripts/teardown.sql
	./scripts/sql ./scripts/setup.sql

load:
	java -cp lib/ojdbc6.jar:./bin LoadTest

teardown:
	./scripts/sql ./scripts/teardown.sql
