# FINAL REPORT

I named my application "Tupperware" because I dislike the name "BuzMo" pretty
strongly.

## Running the application

```
$ make lib              # build lanterna, the gui library
$ make                  # build the main body of code
$ make load             # set up the database and load the demo data
$ make run              # run that bad boy
```

## Source code

All source code can be found in the src/ folder.

## Database schema

Database schema can be found in scripts/setup.sql.

## SQL Queries

All queries can be found in src/Database.java.

## Design revisions

- All CHAR fields were changed to VARCHAR to get around what I consider a bug
  in oracle sql (padding all char fields with spaces and then casting prepared
  statement strings to varchar)
- Changed all foreign key references to the Posts table to ON DELETE CASCADE,
  to maintain data integrity on post deletion
- Added a new table Reads to track every time a post was loaded, along with
  its timestamp
- Made {pid,hid} and tagText primary keys of the tags tables to prevent
  duplicate tags on an individual user or post
- Added a new table Reports to track report history, and a new table
  ReportTagData to track the top-post-per-tag report data. These tables
  contain foreign key references to the Posts table, but they are ON DELETE
  SET NULL instead of CASCADE.

The application's design was ultimately pretty much exactly what I described
in my initial report.
