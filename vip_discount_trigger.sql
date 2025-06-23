CREATE TRIGGER APPLYVIPDISCOUNT
AFTER INSERT ON Reservations
REFERENCING NEW AS NEW_ROW
FOR EACH ROW
BEGIN ATOMIC
    DECLARE v_discount DECIMAL(5,2);

    -- Initialize discount to 0
    SET v_discount = 0;

    -- Check if the guest is already a VIP
    IF EXISTS (SELECT 1 FROM VIPGuest WHERE GID = NEW_ROW.GID) THEN
        -- Retrieve the guest's discount tier
        SET v_discount = (SELECT TierDiscount FROM VIPGuest WHERE GID = NEW_ROW.GID);
    ELSE
        -- Insert the guest as a new VIP with a 5% discount
        INSERT INTO VIPGuest (GID, TierDiscount) VALUES (NEW_ROW.GID, 5);
        SET v_discount = 5;
    END IF;

    -- Apply the discount to the reservation
    UPDATE Reservations
    SET Cost = Cost * (1 - v_discount / 100)
    WHERE ResID = NEW_ROW.ResID;

    -- Log the discount
    INSERT INTO VIPDiscountLog (GID, OriginalCost, DiscountCost)
    VALUES (NEW_ROW.GID, NEW_ROW.Cost, NEW_ROW.Cost * (1 - v_discount / 100));
END@
