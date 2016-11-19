CREATE TABLE Settings (
    currentTime TIMESTAMP,
    currentInterval INTERVAL DAY(9) to SECOND(0),
    clockActive INTEGER NOT NULL
);

INSERT INTO SETTINGS (currentTime, currentInterval, clockActive)
    VALUES (SYSTIMESTAMP, INTERVAL '0' SECOND, 1);

CREATE TABLE Users (
    hid INTEGER NOT NULL,
    email CHAR(20) UNIQUE NOT NULL,
    name CHAR(20) NOT NULL,
    phone CHAR(10) NOT NULL,
    passwordHash CHAR(64) NOT NULL,
    screenname CHAR(20) NOT NULL,
    isManager INTEGER NOT NULL,
    PRIMARY KEY (hid)
);

CREATE TABLE Friendships (
    left INTEGER NOT NULL,
    right INTEGER NOT NULL,
    since TIMESTAMP NOT NULL,
    PRIMARY KEY (left, right),
    FOREIGN KEY (left) REFERENCES Users(hid),
    FOREIGN KEY (right) REFERENCES Users(hid)
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
    groupName CHAR(20) NOT NULL,
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
    FOREIGN KEY (pid) REFERENCES Posts(pid),
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
    tagText CHAR(200) NOT NULL,
    FOREIGN KEY (pid) REFERENCES Posts(pid)
);

CREATE TABLE UserTags (
    hid INTEGER NOT NULL,
    tagText CHAR(200) NOT NULL,
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE SEQUENCE SeqHid START WITH 1;
CREATE SEQUENCE SeqPid START WITH 1;
CREATE SEQUENCE SeqCid START WITH 1;
CREATE SEQUENCE SeqGid START WITH 1;
CREATE SEQUENCE SeqSid START WITH 1;

CREATE OR REPLACE FUNCTION getTime
    RETURN TIMESTAMP
    AS
        output TIMESTAMP;
        currentSettings Settings%ROWTYPE;
    BEGIN
        SELECT * INTO currentSettings
            FROM Settings;
        IF currentSettings.clockActive = 0 THEN
            output := currentSettings.currentTime;
        ELSE
            output := SYSTIMESTAMP + currentSettings.currentInterval;
        END IF;
        RETURN(output);
    END;
/

SELECT get_time() from dual;
