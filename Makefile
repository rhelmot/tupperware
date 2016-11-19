all:
	javac -cp lib/lanterna-3.0.0-beta3.jar src/* -d bin

run:
	java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin Tupperware

test:
	java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin TestTupperware

setup:
	./scripts/sql ./scripts/teardown.sql
	./scripts/sql ./scripts/setup.sql

load:
	./scripts/sql ./scripts/teardown.sql
	./scripts/sql ./scripts/setup.sql
	java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin LoadTest

teardown:
	./scripts/sql ./scripts/teardown.sql

clean:
	rm -f bin/*.class
