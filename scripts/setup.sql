CREATE TABLE Settings (
    currentTime TIMESTAMP,
    currentInterval INTERVAL,
    clockActive INTEGER NOT NULL
);

INSERT INTO SETTINGS (currentTime, currentInterval, clockActive)
    VALUES (NOW(), INTERVAL '0' HOUR, 1);

CREATE TABLE Users (
    hid INTEGER NOT NULL,
    email VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(20) NOT NULL,
    phone CHAR(10) NOT NULL,
    passwordHash CHAR(64) NOT NULL,
    screenname VARCHAR(20) NOT NULL,
    isManager INTEGER NOT NULL,
    PRIMARY KEY (hid)
);

CREATE TABLE Friendships (
    up INTEGER NOT NULL,
    down INTEGER NOT NULL,
    since TIMESTAMP NOT NULL,
    PRIMARY KEY (up, down),
    FOREIGN KEY (up) REFERENCES Users(hid),
    FOREIGN KEY (down) REFERENCES Users(hid)
);

CREATE TABLE FriendRequests (
    requester INTEGER NOT NULL,
    requestee INTEGER NOT NULL,
    PRIMARY KEY (requester, requestee),
    FOREIGN KEY (requester) REFERENCES Users(hid),
    FOREIGN KEY (requestee) REFERENCES Users(hid)
);

CREATE TABLE Sessions (
    sid INTEGER NOT NULL,
    token CHAR(32) NOT NULL UNIQUE,
    hid INTEGER NOT NULL,
    isManaging INTEGER NOT NULL,
    PRIMARY KEY (sid)
);

CREATE TABLE ChatGroups (
    gid INTEGER NOT NULL,
    groupName VARCHAR(20) NOT NULL,
    duration INTEGER NOT NULL,
    PRIMARY KEY (gid)
);

CREATE TABLE ChatGroupMemberships (
    gid INTEGER NOT NULL,
    hid INTEGER NOT NULL,
    isOwner INTEGER NOT NULL,
    invitationAccepted INTEGER NOT NULL,
    PRIMARY KEY (gid, hid),
    FOREIGN KEY (gid) REFERENCES ChatGroups(gid),
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE TABLE Posts (
    pid INTEGER NOT NULL,
    author INTEGER NOT NULL,
    text VARCHAR(1400) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    isPublic INTEGER NOT NULL,
    PRIMARY KEY (pid),
    FOREIGN KEY (author) REFERENCES Users(hid)
);

CREATE TABLE PostVisibilities (
    pid INTEGER NOT NULL,
    hid INTEGER,
    FOREIGN KEY (pid) REFERENCES Posts(pid) ON DELETE CASCADE,
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE TABLE Chats (
    cid INTEGER NOT NULL,
    author INTEGER NOT NULL,
    text VARCHAR(1400) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    hid INTEGER,
    gid INTEGER,
    deletedBySender INTEGER NOT NULL,
    deletedByReceiver INTEGER NOT NULL,
    PRIMARY KEY (cid),
    FOREIGN KEY (author) REFERENCES Users(hid),
    FOREIGN KEY (gid) REFERENCES ChatGroups(gid)
);

CREATE TABLE PostTags (
    pid INTEGER NOT NULL,
    tagText VARCHAR(200) NOT NULL,
    PRIMARY KEY (pid, tagText),
    FOREIGN KEY (pid) REFERENCES Posts(pid) ON DELETE CASCADE
);

CREATE TABLE UserTags (
    hid INTEGER NOT NULL,
    tagText VARCHAR(200) NOT NULL,
    PRIMARY KEY (hid, tagText),
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE TABLE Reads (
    pid INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (pid) REFERENCES Posts(pid) ON DELETE CASCADE
);

CREATE TABLE Reports (
    tid INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    newMessages INTEGER NOT NULL,
    messageReads INTEGER NOT NULL,
    avgMessageReads REAL NOT NULL,
    avgNewMessageReads REAL NOT NULL,
    topPost1 INTEGER,
    topPost2 INTEGER,
    topPost3 INTEGER,
    topUser1 INTEGER,
    topUser2 INTEGER,
    topUser3 INTEGER,
    inactiveUserCount INTEGER NOT NULL,
    PRIMARY KEY (tid),
    FOREIGN KEY (topPost1) REFERENCES Posts(pid) ON DELETE SET NULL,
    FOREIGN KEY (topPost2) REFERENCES Posts(pid) ON DELETE SET NULL,
    FOREIGN KEY (topPost3) REFERENCES Posts(pid) ON DELETE SET NULL,
    FOREIGN KEY (topUser1) REFERENCES Users(hid) ON DELETE SET NULL,
    FOREIGN KEY (topUser2) REFERENCES Users(hid) ON DELETE SET NULL,
    FOREIGN KEY (topUser3) REFERENCES Users(hid) ON DELETE SET NULL
);

CREATE TABLE ReportTagData (
    tid INTEGER NOT NULL,
    tagText VARCHAR(200) NOT NULL,
    pid INTEGER NOT NULL,
    FOREIGN KEY (tid) REFERENCES Reports(tid),
    FOREIGN KEY (pid) REFERENCES Posts(pid) ON DELETE SET NULL
);

CREATE SEQUENCE SeqHid START WITH 1;
CREATE SEQUENCE SeqPid START WITH 1;
CREATE SEQUENCE SeqCid START WITH 1;
CREATE SEQUENCE SeqGid START WITH 1;
CREATE SEQUENCE SeqSid START WITH 1;
CREATE SEQUENCE SeqTid START WITH 1;

CREATE OR REPLACE FUNCTION getTime()
    RETURNS TIMESTAMP
    AS $$
    DECLARE output TIMESTAMP;
    DECLARE currentSettings Settings%ROWTYPE;
    BEGIN
        SELECT * INTO currentSettings
            FROM Settings;
        IF currentSettings.clockActive = 0 THEN
            output := currentSettings.currentTime;
        ELSE
            output := now() + currentSettings.currentInterval;
        END IF;
        RETURN(output);
    END $$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DATE_SUB (t IN TIMESTAMP, i IN INTERVAL)
    RETURNS TIMESTAMP
    AS $$
    DECLARE output TIMESTAMP;
    BEGIN
        output := t - i;
        RETURN(output);
    END $$
    LANGUAGE plpgsql;

SELECT getTime();
SELECT DATE_SUB(getTime(), INTERVAL '2' DAY);
