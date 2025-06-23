CREATE TRIGGER ASSIGN_TO_VIP
AFTER INSERT ON VIPGuest
REFERENCING NEW AS NEW_VIP
FOR EACH ROW
BEGIN ATOMIC
    -- Attempt to assign the first available assistant with the same TierDiscount
    INSERT INTO Assigned (SID, GID)
    SELECT A.SID, NEW_VIP.GID
    FROM Assistants A
    LEFT JOIN Assigned AS Ass ON A.SID = Ass.SID
    WHERE A.Tier = NEW_VIP.TierDiscount  -- Match tier levels
    FETCH FIRST 1 ROW ONLY;

    -- Check if the assistant assignment was successful
    IF (SELECT COUNT(*) FROM Assigned WHERE GID = NEW_VIP.GID) = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No matching assistant available for this VIP tier. Try later!';
    END IF;
END@
