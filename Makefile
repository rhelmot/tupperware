.PHONY: all lib run test setup load teardown clean

all:
	#javac -cp lib/lanterna-3.0.0-beta3.jar src/*.java -d bin
	javac -g -cp lib/ src/*.java -d bin

lib:
	find lib/com -name '*.java' | javac -g -d bin @/dev/stdin

run:
	#java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin Tupperware
	java -cp lib/ojdbc6.jar:./bin Tupperware 2> .stderr

test:
	#java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin TestTupperware
	java -cp lib/ojdbc6.jar:./bin TestTupperware

setup:
	./scripts/sql ./scripts/teardown.sql
	./scripts/sql ./scripts/setup.sql

load:
	./scripts/sql ./scripts/teardown.sql
	./scripts/sql ./scripts/setup.sql
	#java -cp lib/ojdbc6.jar:lib/lanterna-3.0.0-beta3.jar:./bin LoadTest
	java -cp lib/ojdbc6.jar:./bin LoadTest

teardown:
	./scripts/sql ./scripts/teardown.sql

clean:
	rm -rf bin/*.class bin/com
