CREATE DATABASE lehsetreff;


Create table lehsetreff.threadGroups(ID int NOT NULL AUTO_INCREMENT,
                                    caption varchar(50) NOT NULL,
                                    ownerID int NOT NULL,
                                    groupDescription varchar(100) NOT NULL, 
                                    primary key(ID),
                                    foreign key(ownerID) References users(ID)
--                                  CONSTRAINT (caption)
                                    );


Create table lehsetreff.threads(ID int NOT NULL AUTO_INCREMENT,
                                caption varchar(50) NOT NULL,
                                latestMessage timestamp NOT NULL,
                                ownerID int NOT NULL,
                                groupID int NOT NULL,
                                threadDescription varchar(100) NOT NULL,
                                foreign key(ownerID) references users(ID),
                                foreign key(groupID) references threadGroups(ID) ON DELETE CASCADE,
                                primary key(ID)
                                );

-- Create table lehsetreff.threads_users(userID int NOT NULL,
--                                        threadID int NOT NULL,
--                                        latestMessage timestamp NOT NULL,
--                                        foreign key(userID) references meshenger.users(ID) ON DELETE CASCADE,
--                                        foreign key(threadID) references thread(ID) ON DELETE CASCADE,
--                                        CONSTRAINT ID UNIQUE (userID, threadID)
--                                        );


Create table lehsetreff.userRoles(userID int NOT NULL,
                                roleID int NOT NULL,
                                foreign key(userID) references users(ID) ON DELETE CASCADE,
                                primary key(userID)
                                );


Create table lehsetreff.messages(ID int NOT NULL AUTO_INCREMENT,
                                content VARCHAR(1024),
                                contentType int NOT NULL,
                                dateAndTime timeStamp, 
                                senderID int NOT NULL,
                                threadID int NOT NULL,
                                wasModified boolean,
                                primary key(ID),
                                foreign key(threadID) References threads(ID) ON DELETE CASCADE,
                                foreign key(senderID) References users(ID)
                                );  