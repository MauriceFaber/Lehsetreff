DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_insert
AFTER INSERT ON meshenger.users FOR EACH ROW
BEGIN
    INSERT INTO lehsetreff.users (ID, passphrase, userName, apiKey, avatar) VALUES (NEW.ID, NEW.passphrase, NEW.userName, NEW.apiKey, NEW.avatar);
END;
$$
DELIMITER ;