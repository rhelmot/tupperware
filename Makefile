all:
	javac src/* -d bin

run:
	java -cp lib/ojdbc6.jar:./bin Tupperware

setup:
	./scripts/sql ./scripts/setup.sql

teardown:
	./scripts/sql ./scripts/teardown.sql
