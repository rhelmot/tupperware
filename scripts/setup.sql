CREATE TABLE Settings (
    current_time TIMESTAMP NOT NULL
);

CREATE TABLE Users (
    hid INTEGER NOT NULL,
    email CHAR(20) UNIQUE NOT NULL,
    name CHAR(20) NOT NULL,
    phone CHAR(10) NOT NULL,
    pass_hash CHAR(32) NOT NULL,
    screenname CHAR(256) NOT NULL,
    is_manager INTEGER NOT NULL,
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
    session_id INTEGER NOT NULL,
    token CHAR(32) NOT NULL,
    hid INTEGER NOT NULL,
    is_managing INTEGER NOT NULL,
    PRIMARY KEY (session_id)
);

CREATE TABLE ChatGroups (
    gid INTEGER NOT NULL,
    group_name CHAR(20) NOT NULL,
    duration INTEGER NOT NULL,
    PRIMARY KEY (gid)
);

CREATE TABLE ChatGroupMemberships (
    gid INTEGER NOT NULL,
    hid INTEGER NOT NULL,
    is_owner INTEGER NOT NULL,
    invitation_accepted INTEGER NOT NULL,
    PRIMARY KEY (gid, hid),
    FOREIGN KEY (gid) REFERENCES ChatGroups(gid),
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE TABLE Posts (
    pid INTEGER NOT NULL,
    author INTEGER NOT NULL,
    text VARCHAR(1400) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_public INTEGER NOT NULL,
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
    deleted_by_sender INTEGER NOT NULL,
    deleted_by_receiver INTEGER NOT NULL,
    PRIMARY KEY (cid),
    FOREIGN KEY (author) REFERENCES Users(hid),
    FOREIGN KEY (gid) REFERENCES ChatGroups(gid)
);

CREATE TABLE PostTags (
    pid INTEGER NOT NULL,
    tag_text CHAR(200) NOT NULL,
    PRIMARY KEY (pid),
    FOREIGN KEY (pid) REFERENCES Posts(pid)
);

CREATE TABLE UserTags (
    hid INTEGER NOT NULL,
    tag_text CHAR(200) NOT NULL,
    PRIMARY KEY (hid),
    FOREIGN KEY (hid) REFERENCES Users(hid)
);

CREATE SEQUENCE SeqHid START WITH 1;
CREATE SEQUENCE SeqPid START WITH 1;
CREATE SEQUENCE SeqCid START WITH 1;
CREATE SEQUENCE SeqGid START WITH 1;
