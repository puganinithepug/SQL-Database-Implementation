CREATE OR REPLACE PROCEDURE createVIPDiscountLog()
BEGIN
    DECLARE v_count INT DEFAULT 0;

    -- Check if table exists
    SELECT COUNT(*) INTO v_count FROM SYSCAT.TABLES WHERE TABNAME = 'VIPDISCOUNTLOG';

    -- If table doesn't exist, create it
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
            CREATE TABLE VIPDiscountLog (
                LogID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                GID INT NOT NULL,
                OriginalCost DECIMAL(10,2) NOT NULL,
                DiscountCost DECIMAL(10,2) NOT NULL,
                FOREIGN KEY (GID) REFERENCES VIPGuest(GID)
            )';
    END IF;
END@
