import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;


public class databaseApp {
     private static final String URL = "jdbc:db2://winter2025-comp421.cs.mcgill.ca:50000/comp421";
    //private static final String user = System.getenv("SOCSUSER");
    //private static final String PASSWORD = System.getenv("SOCSPASSWD");
    private static final String user = "cs421g35";
    private static final String password = "BulldogDOG53#!";  //Remove when submitting
    
    public static void main(String[] args) {
        if (user == null || password == null) {
            System.err.println("Error: Missing credentials.");
            return;
        }
        
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        try (Connection con = DriverManager.getConnection(URL, user, password);
             Scanner scanner = new Scanner(System.in)){
	
	
            while (true) {
                System.out.println("\nHotel Database Menu:");
                System.out.println("1. Look up guest reservations");  //Options available to the users
								      //Call submenu
                System.out.println("2. Make a new reservation");
                System.out.println("3. Cancel a reservation");
                System.out.println("4. Add a new guest");
                System.out.println("5. Upgrade a guest's status");
                System.out.println("6. Quit");
                System.out.print("Select an option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();  
                
                switch (choice) {
                    case 1: guestReservationsSubmenu(con, scanner); break;
                    case 2: makeReservation(con, scanner); break;
                    case 3: cancelReservation(con, scanner); break;
                    case 4: addGuest(con, scanner); break;
                    case 5: increaseStatus(con, scanner); break;
                    case 6: System.out.println("Exiting..."); return; //exit the app
                    default: System.out.println("Invalid option. Try again."); //for invalid number
			    }
	    }
                } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
//guest reservation submenu
private static void guestReservationsSubmenu(Connection con, Scanner scanner) throws SQLException{
while(true){
 System.out.println("\nGuest Reservations Submenu:");
 System.out.println("1. View all reservations");
 System.out.println("2. Lookup reservations.");
 System.out.println("3. Search rooms not reserved");
 System.out.println("4. Search VIP Guest reservations.");
 System.out.println("5. Return to main menu");
 System.out.print("Select an option: ");
  int submenuChoice = scanner.nextInt();
            scanner.nextLine();

            switch (submenuChoice) {
                case 1:
                    //view all reservations
                    viewAllReservations(con, scanner);
                    break;
		case 2:
		    //lookup reservations based on guest name
		     lookupReservations(con, scanner);
             break;
        case 3:
            // search rooms without reservations
            searchRoomsNotReserved(con, scanner);
            break;
		case 4:
		    //search VIP guest reservations
		    searchVIPReservations(con, scanner);
             break;
        case 5:
                    // exit submenu and return to main
            return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
}
}
//view all reservations
private static void viewAllReservations(Connection con, Scanner scanner) throws SQLException {
    String query = "SELECT r.ResID, r.CheckIn, r.CheckOut, g.Name " +
                   "FROM Reservations r JOIN Guests g ON r.GID = g.GID " +
                   "ORDER BY r.ResID";
    try (PreparedStatement stmt = con.prepareStatement(query)) {
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            System.out.println("No reservations found.");
        } else {
            System.out.println("All Reservations:");
            do {
                int resID = rs.getInt("ResID");
                Date checkIn = rs.getDate("CheckIn");
                Date checkOut = rs.getDate("CheckOut");
                String guestName = rs.getString("Name");
                System.out.println("Reservation ID: " + resID + ", Guest: " + guestName +
                                   ", Check-In: " + checkIn + ", Check-Out: " + checkOut);
            } while (rs.next());
        }
   }
}
//search rooms not reserved
private static void searchRoomsNotReserved(Connection con, Scanner scanner) throws SQLException {
    try {
        // Ask for dates
        System.out.print("Enter desired Check-In date (YYYY-MM-DD): ");
        String checkInStr = scanner.nextLine();
        System.out.print("Enter desired Check-Out date (YYYY-MM-DD): ");
        String checkOutStr = scanner.nextLine();
        
        // Convert input to SQL dates
        Date checkInDate = Date.valueOf(checkInStr);
        Date checkOutDate = Date.valueOf(checkOutStr);
        
        // Query: find rooms that are not reserved during any part of the given period.
        // This query assumes that if a room is reserved such that its reservation overlaps the
        // requested period, it is considered unavailable.
        String query = "SELECT RoomNum, RoomType, Capacity FROM Rooms " +
                       "WHERE RoomNum NOT IN ( " +
                       "    SELECT RoomNum FROM Reservations " +
                       "    WHERE (CheckIn < ? AND CheckOut > ?) " +
                       ")";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            // Note: We use the check-out date as the lower bound and the check-in date as the upper bound
            // for overlap detection.
            stmt.setDate(1, checkOutDate);
            stmt.setDate(2, checkInDate);
            
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("No available rooms found for the given dates.");
            } else {
                System.out.println("Available Rooms:");
                do {
                    int roomNum = rs.getInt("RoomNum");
                    String roomType = rs.getString("RoomType");
                    int capacity = rs.getInt("Capacity");
                    System.out.println("Room Number: " + roomNum +
                                       ", Type: " + roomType +
                                       ", Capacity: " + capacity);
                } while (rs.next());
            }
        }
    }catch(SQLException e){

    }
}
//search VIP guest reservations
private static void searchVIPReservations(Connection con, Scanner scanner) throws SQLException {
    System.out.print("Enter VIP guest name: ");
    String name = scanner.nextLine();
    
    String query = "SELECT r.ResID, r.CheckIn, r.CheckOut, g.Name, v.TierDiscount " +
                   "FROM Reservations r " +
                   "JOIN Guests g ON r.GID = g.GID " +
                   "JOIN VIPGuest v ON v.GID = g.GID " +
                   "WHERE LOWER(g.Name) = LOWER(?) " +
                   "ORDER BY r.ResID";
    
    try (PreparedStatement stmt = con.prepareStatement(query)) {
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            System.out.println("No VIP reservations found for guest " + name + ".");
        } else {
            System.out.println("VIP Reservations for guest " + name + ":");
            do {
                int resID = rs.getInt("ResID");
                Date checkIn = rs.getDate("CheckIn");
                Date checkOut = rs.getDate("CheckOut");
                double tierDiscount = rs.getDouble("TierDiscount");
                System.out.println("Reservation ID: " + resID +
                                   ", Check-In: " + checkIn +
                                   ", Check-Out: " + checkOut +
                                   ", Tier Discount: " + tierDiscount);
            } while (rs.next());
        }
    } catch (SQLException e) {
        System.err.println("Database error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
    }
}
//prints reservations associated with a guest name
    private static void lookupReservations(Connection con, Scanner scanner) throws SQLException {
        System.out.print("Enter guest name: ");
        String name = scanner.nextLine();
        //Allowing for the user not using capitalization and still finding the guest with use of LOWER
        String query = "SELECT ResID, CheckIn, CheckOut FROM Reservations,Guests WHERE LOWER(Name) =LOWER(?) AND Reservations.GID=Guests.GID";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            //Enumerating the reservations to their name
            if (!rs.next()) {
                System.out.println("No reservations found for guest " + name+".");
            } else {
                System.out.println("Here are the reservation(s) for guest "+name+":");
                do {
                    System.out.println("Reservation ID: " + rs.getInt("ResID") + ", Check-In: " + rs.getDate("CheckIn") + ", Check-Out: " + rs.getDate("CheckOut"));
                }while (rs.next());
            }
        }
    }

    //Make a reservation for a guest for a specific room and date. If not available then return the next availability for that room
    private static void makeReservation(Connection con, Scanner scanner) throws SQLException {
        try{System.out.print("Enter guest ID: ");
        int gid = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter room number: ");
        int roomNum = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter the desired check in date (YYYYMMDD): ");
        String checkin=scanner.nextLine();
        LocalDate checkinDate = LocalDate.parse(checkin, DateTimeFormatter.ofPattern("yyyyMMdd"));
        Date sqlCheckin = Date.valueOf(checkinDate);

        System.out.println("Enter length of stay: ");
        int stay = scanner.nextInt();
        scanner.nextLine();
        LocalDate checkoutDate = checkinDate.plusDays(stay);
        Date sqlCheckout = Date.valueOf(checkoutDate);

        System.out.println("Enter credit card number: ");
        String cardnum= scanner.nextLine();
        
        System.out.println("Enter cost per night: "); //this is input so the cost can change per season
        int cost= scanner.nextInt();
        scanner.nextLine();
        cost=cost*stay; //cost per night multiplied by stay length
        
        System.out.println("Enter the number of guests that will be staying in the room: "); //this is input so the cost can change per season
        int guestnum= scanner.nextInt();
        scanner.nextLine();

        //Check that number of guest doesnt not exceed capacity of the room
        String capSQL = "SELECT capacity FROM rooms where roomnum=?";
        try(PreparedStatement checkcap = con.prepareStatement(capSQL)){
            checkcap.setInt(1, roomNum);
            ResultSet rs = checkcap.executeQuery();
            if(rs.next()){
                int actualCap = rs.getInt(1);
                if(guestnum>actualCap){
                    System.out.println("Reservation not successful: too many guests for room with capacity "+actualCap);
                    return;
                }}
            else{
                System.out.println("Reservation not successful: room number does not exist.");
                return;
            }
            
        }
        //Check if guest is VIP, if so apply their discount to the cost
        String checkVIPSQL = "SELECT TierDiscount FROM VIPGuest WHERE GID=?";
        try(PreparedStatement stmt1=con.prepareStatement(checkVIPSQL)){
            stmt1.setInt(1, gid);
            ResultSet r =stmt1.executeQuery();

            if(r.next()){ //If they are indeed vip
                int discount=r.getInt(1);
                cost = (int) (cost-(cost*(discount/100.0)));
            }}

        //First check if available
        String availableSQL = "SELECT 1 FROM Reservations WHERE RoomNum=? AND NOT (Checkout<=(?) OR CheckIn >=(?))";

        try(PreparedStatement stmt = con.prepareStatement(availableSQL)){
            stmt.setInt(1, roomNum);
            stmt.setDate(2, sqlCheckin);
            stmt.setDate(3, sqlCheckout);
            ResultSet rs = stmt.executeQuery();

            int resid;
            if(!rs.next()){ //room available, then we insert in table
                //generate a RESID
                String getMaxId = "SELECT COALESCE(MAX(ResID), 0) + 1 FROM Reservations";
                try(PreparedStatement maxid=con.prepareStatement(getMaxId)){
                    ResultSet r=maxid.executeQuery();
                    r.next();
                    resid=r.getInt(1);
                }
                String insertSQL = "INSERT INTO Reservations (ResID,CheckIn, CheckOut, GuestCount, RoomNum, GID, BookDate, CardNum, Cost) VALUES (?,?,?, ?,?,?, CURRENT_DATE,?, ?)";
                try (PreparedStatement stmt2 = con.prepareStatement(insertSQL)) {
                    stmt2.setInt(1,resid);
                    stmt2.setDate(2, sqlCheckin);
                    stmt2.setDate(3, sqlCheckout);
                    stmt2.setInt(4, guestnum);
                    stmt2.setInt(5, roomNum);
                    stmt2.setInt(6, gid);
                    stmt2.setString(7, cardnum);
                    stmt2.setInt(8,cost);
                    stmt2.executeUpdate();
                    System.out.println("Reservation made successfully for "+guestnum+" guest(s) in room "+roomNum+ " with total cost of "+cost+"$.");
                }catch(SQLException e) {
                    System.err.println("Failed to make reservation, check your input information is correct. ");
                }
            }
            else{ //else suggest something else
                System.out.println("Room "+ roomNum+" is not available for your dates. Here is a suggestion.");
                //Select the next available date 
                String alternative = "SELECT checkout FROM Reservations WHERE Roomnum =? AND checkout>=? ORDER BY checkout ASC LIMIT 1";
                try(PreparedStatement stmt3 = con.prepareStatement(alternative)){
                    stmt3.setInt(1, roomNum);
                    stmt3.setDate(2, sqlCheckin);
                    ResultSet rs3=stmt3.executeQuery();
                    
                    if(rs3.next()){
                        Date next=rs3.getDate(1);
                        System.out.println("The next available date for room "+roomNum+" is "+next.toString()+".");
                    }
                }
            }
        }}catch(InputMismatchException e){
            scanner.nextLine();
            System.out.println("Wrong input type.");
        }
    }
    
    //Cancel a reservation (will only remove if in the future) and delete all foreign key dependencies
    private static void cancelReservation(Connection con, Scanner scanner) throws SQLException {
        System.out.print("Enter reservation ID to cancel: ");
        int resID = scanner.nextInt();
        scanner.nextLine();

        //First check that res is in future, not relevant to cancel in the past
        String checkDate= "SELECT ResId FROM Reservations WHERE ResId=? AND Checkin>CURRENT_DATE";
        try(PreparedStatement check = con.prepareStatement(checkDate)){
            check.setInt(1, resID);
            ResultSet rs = check.executeQuery();
            if(!rs.next()){
                System.out.println("No cancellation: Reservation already past or no reservation with this id exist.");
                return;
            }
        }
        //Then have to delete any dependencies
        try (PreparedStatement deleteCharges = con.prepareStatement("DELETE FROM Charges WHERE ResID = ?")) {
            deleteCharges.setInt(1, resID);
            deleteCharges.executeUpdate();
        }
        try (PreparedStatement deleteCleans = con.prepareStatement("DELETE FROM Cleans WHERE ResID = ?")) {
            deleteCleans.setInt(1, resID);
            deleteCleans.executeUpdate();
        }
        try (PreparedStatement deleteHosts = con.prepareStatement("DELETE FROM Hosts WHERE ResID = ?")) {
            deleteHosts.setInt(1, resID);
            deleteHosts.executeUpdate();
        }
        
        String deleteSQL = "DELETE FROM Reservations WHERE ResID = ?";
        try (PreparedStatement stmt = con.prepareStatement(deleteSQL)) {
            stmt.setInt(1, resID);
            stmt.executeUpdate();
            System.out.println("Reservation cancelled successfully.");
        }catch (SQLException e) {
            System.err.println("Failed to cancel reservation: " + e.getMessage());
        }
    }
    
    //Inserts a new guest into Guests table, generates a GID for them
    private static void addGuest(Connection con, Scanner scanner) throws SQLException {
        System.out.print("Enter guest name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();
        
        //Before inserting, we have to assign a new GID
        String maxGID = "SELECT MAX(GID) FROM Guests";
        int newGID=0;//if table is empty

        try(Statement s = con.createStatement(); ResultSet r = s.executeQuery(maxGID)){
            if(r.next() && r.getInt(1)>0){
                newGID=r.getInt(1)+1; //adding one to highest gid we currently have
            }
        }
        String insertSQL = "INSERT INTO Guests (GID,Name, Email, Phone) VALUES (?,?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(insertSQL)) {
            stmt.setInt(1, newGID);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.executeUpdate();
            System.out.println("Guest with Name: "+name+" and ID "+newGID+" added successfully!");
        }
    }
    
    //Updates the status of a guest. If guest is casual, we update to VIP. 
    //If already VIP, we increase the tier discount (If already maximum then unchanged)
    private static void increaseStatus(Connection con, Scanner scanner) throws SQLException {
        System.out.print("Enter guest ID: ");
        try{
        int GID = scanner.nextInt();
        
        //Check if guest is already a VIP
        String checkVIPSQL = "SELECT TierDiscount FROM VIPGuest WHERE GID=?";
        try(PreparedStatement stmt1=con.prepareStatement(checkVIPSQL)){
            stmt1.setInt(1, GID);
            ResultSet r =stmt1.executeQuery();

            if(r.next()){ //If they are indeed vip
                int discount=r.getInt(1);
                if(discount!=15){ //if discount not already max
                    discount=discount+5;
                }
                String updatedis ="UPDATE VIPGuest SET TierDiscount=? WHERE GID=? ";
                try(PreparedStatement stmt2= con.prepareStatement(updatedis)){
                    stmt2.setInt(1, discount);
                    stmt2.setInt(2, GID);
                    stmt2.executeUpdate();
                    System.out.println("Guest with id "+GID+ " now has discount of "+discount+"%.");
                }
            }
            else{ //else its a casual getting upgraded to vip tier 1 (5%discount)
                //first delete them from casual, then insert in vip
                String deleteCasual = "DELETE FROM CasualGuests WHERE GID=?";
                String insertVip = "INSERT INTO VIPGuest(GID,TierDiscount) VALUES(?,5)";
                try(PreparedStatement del = con.prepareStatement(deleteCasual); 
                    PreparedStatement ins = con.prepareStatement(insertVip);){
                        del.setInt(1, GID);
                        del.executeUpdate();

                        ins.setInt(1, GID);
                        ins.executeUpdate();

                        System.out.println("Guest "+GID+" has been upgraded to VIP status with 5% discount.");

                }
            }
        }catch(SQLException | InputMismatchException e){
            System.out.println("Invalid guest ID.");
        }
    }catch(InputMismatchException e){
        scanner.nextLine();
        System.out.println("Invalid guest ID.");
    }
    }
}
