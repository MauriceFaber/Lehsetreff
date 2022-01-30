DELIMITER $$
CREATE TRIGGER sync_lehsetreff_users_delete
AFTER DELETE ON meshenger.users FOR EACH ROW
BEGIN
    DELETE FROM lehsetreff.users WHERE id = OLD.id;
END;
$$
DELIMITER ;