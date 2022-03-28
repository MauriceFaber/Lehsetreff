DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_delete1
AFTER DELETE ON meshenger.users FOR EACH ROW
BEGIN
  IF EXISTS(SELECT ID from lehsetreff.users WHERE ID = OLD.ID ) THEN
    DELETE FROM lehsetreff.users WHERE ID = OLD.ID;
  END IF;
END;
$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_delete2
AFTER DELETE ON lehsetreff.users FOR EACH ROW
BEGIN
  IF EXISTS(SELECT ID from meshenger.users WHERE ID = OLD.ID ) THEN
    DELETE FROM meshenger.users WHERE ID = OLD.ID;
  END IF;
END;
$$
DELIMITER ;





DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_update1
AFTER UPDATE ON meshenger.users FOR EACH ROW
BEGIN
 IF NOT EXISTS( SELECT * from lehsetreff.users WHERE ID = NEW.ID and passphrase = NEW.passphrase and userName = NEW.userName and apiKey = NEW.apiKey and avatar = NEW.avatar) THEN
    UPDATE lehsetreff.users 
    set passphrase = NEW.passphrase,
    userName = NEW.userName, 
    apiKey = NEW.apiKey,
    avatar = NEW.avatar
	where ID = NEW.ID;
  END IF;
END;
$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_update2
AFTER UPDATE ON lehsetreff.users FOR EACH ROW
BEGIN
 IF NOT EXISTS( SELECT * from meshenger.users WHERE ID = NEW.ID and passphrase = NEW.passphrase and userName = NEW.userName and apiKey = NEW.apiKey and avatar = NEW.avatar) THEN
    UPDATE meshenger.users 
    set passphrase = NEW.passphrase,
    userName = NEW.userName, 
    apiKey = NEW.apiKey,
    avatar = NEW.avatar
	where ID = NEW.ID;
  END IF;
END;
$$
DELIMITER ;





DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_insert1
AFTER INSERT ON meshenger.users FOR EACH ROW
BEGIN
 IF NOT EXISTS( SELECT ID from lehsetreff.users WHERE ID = NEW.ID ) THEN
    INSERT INTO lehsetreff.users (ID, passphrase, userName, apiKey, avatar) VALUES (NEW.ID, NEW.passphrase, NEW.userName, NEW.apiKey, NEW.avatar);
  END IF;
END;
$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_insert2
AFTER INSERT ON lehsetreff.users FOR EACH ROW
BEGIN
 IF NOT EXISTS( SELECT ID from meshenger.users WHERE ID = NEW.ID ) THEN
    INSERT INTO meshenger.users (ID, passphrase, userName, apiKey, avatar) VALUES (NEW.ID, NEW.passphrase, NEW.userName, NEW.apiKey, NEW.avatar);
  END IF;
END;
$$
DELIMITER ;