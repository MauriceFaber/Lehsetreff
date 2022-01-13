CREATE DATABASE meshenger;

create table meshenger.users(ID int NOT NULL AUTO_INCREMENT,
                            passphrase varchar(32) NOT NULL,
                            userName varchar(50) NOT NULL UNIQUE,
                            apiKey CHAR (64) NOT NULL UNIQUE,
                            avatar varchar(255) NOT NULL,
                            primary key(ID)
                            );

Create table meshenger.chatrooms(ID int NOT NULL AUTO_INCREMENT,
                                chatName varchar(50) NOT NULL,
                                chatAvatar varchar(255) NOT NULL,
                                latestMessage timestamp NOT NULL,
                                chatroomType int NOT NULL,
                                primary key(ID)
                                );

Create TABLE meshenger.messages(ID int NOT NULL AUTO_INCREMENT,
                                content VARCHAR(1024),
                                contentType int NOT NULL,
                                dateAndTime timeStamp, 
                                senderID int NOT NULL,
                                chatroomID int NOT NULL,
                                primary key(ID),
                                foreign key(chatroomID) References chatrooms(ID) ON DELETE CASCADE,
                                foreign key(senderID) References users(ID) ON DELETE CASCADE
                                );        

Create table meshenger.chatrooms_users(userID int NOT NULL,
                                       chatroomID int NOT NULL,
                                       latestMessage timestamp NOT NULL,
                                       foreign key(userID) references users(ID) ON DELETE CASCADE,
                                       foreign key(chatroomID) references chatrooms(ID) ON DELETE CASCADE,
                                       CONSTRAINT ID UNIQUE (userID, chatroomID)
                                       );       
									                             
create table meshenger.contacts(contactID int NOT NULL,
                                contactOwnerID int NOT NULL,
                                foreign key(contactID) references users(ID) ON DELETE CASCADE,
                                foreign key(contactOwnerID) references users(ID) ON DELETE CASCADE,
                                CONSTRAINT ID UNIQUE (contactID, contactOwnerID),
                                check (contactID != contactOwnerID)
                                );