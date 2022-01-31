DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_update
AFTER UPDATE ON meshenger.users FOR EACH ROW
BEGIN
    UPDATE lehsetreff.users 
    set passphrase = NEW.passphrase,
    userName = NEW.userName, 
    apiKey = NEW.apiKey,
    avatar = NEW.avatar
	where ID = NEW.ID;
END;
$$
DELIMITER ;