create table Guests (
  gid INTEGER NOT NULL,
  name VARCHAR(30) NOT NULL,
  email VARCHAR(50),
  phone VARCHAR(11),
  PRIMARY KEY (gid)
);

create table CasualGuests (
  gid INTEGER NOT NULL,
  PRIMARY KEY (gid),
  FOREIGN KEY (gid) REFERENCES guests
);

create table VIPGuest (
  gid INT NOT NULL,
  TierDiscount INT DEFAULT 0,
  PRIMARY KEY (gid),
  FOREIGN KEY (gid) REFERENCES Guests
);

create table Rooms (
  RoomNum INT NOT NULL,
  RoomType VARCHAR(30),
  Capacity INT,
  PRIMARY KEY (RoomNum)
);

-- Reservations must have a roomNum and gid
create table Reservations (
  ResID INT NOT NULL,
  CheckIn VARCHAR(8),
  CheckOut VARCHAR(8),
  GuestCount INT,
  RoomNum INT NOT NULL,
  gid INT NOT NULL,
  BookDate VARCHAR(8),
  CardNum VARCHAR(16),
  Cost INT,
  PRIMARY KEY (ResID),
  FOREIGN KEY (RoomNum) REFERENCES Rooms,
  FOREIGN KEY (gid) REFERENCES Guests
);

create table Staff (
  SID INT NOT NULL,
  Name VARCHAR(30),
  Birthdate VARCHAR(8),
  PRIMARY KEY (SID)
);

create table Assistants (
  SID INT NOT NULL,
  Tier INT,
  PRIMARY KEY (SID),
  FOREIGN KEY (SID) REFERENCES Staff
);

create table Cleaners (
  SID INT NOT NULL,
  PRIMARY KEY (SID),
  FOREIGN KEY (SID) REFERENCES Staff
);

create table Hosts (
  SID INT NOT NULL,
  ResID INT NOT NULL,
  PRIMARY KEY (SID, ResID),
  FOREIGN KEY (SID) REFERENCES Assistants,
  FOREIGN KEY (ResID) REFERENCES Reservations
);

create table Assigned (
  SID INT NOT NULL,
  GID INT NOT NULL,
  PRIMARY KEY (SID, GID),
  FOREIGN KEY (SID) REFERENCES Assistants,
  FOREIGN KEY (GID) REFERENCES VIPGuest
);

create table Cleans (
  SID INT NOT NULL,
  ResID INT NOT NULL,
  Tips INT DEFAULT 0,
  PRIMARY KEY (SID, ResID),
  FOREIGN KEY (SID) REFERENCES Cleaners,
  FOREIGN KEY (ResID) REFERENCES Reservations
);

-- opentime and closetime are D/M/Y strings, including /
create table Amenities (
  Name VARCHAR(50) NOT NULL,
  OpenTime VARCHAR(8),
  CloseTime VARCHAR(8),
  PRIMARY KEY (Name)
);

create table PaidAmenities (
  Name VARCHAR(50) NOT NULL,
  Cost INT,
  PRIMARY KEY (Name),
  FOREIGN KEY (Name) REFERENCES Amenities
);

create table Charges (
  Name VARCHAR(50) NOT NULL,
  ResID INT NOT NULL,
  CardNum VARCHAR(16) NOT NULL,
  PRIMARY KEY (Name, ResID),
  FOREIGN KEY (Name) REFERENCES PaidAmenities,
  FOREIGN KEY (ResID) REFERENCES Reservations
);
