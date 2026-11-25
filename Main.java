import java.util.*;
import java.io.*;

class Main {

    // Circular Linked List Node
    static class Node {
        String stop;
        Node next;

        Node(String stop) {
            this.stop = stop;
        }
    }

    // Passenger Ticket
    static class Ticket {
        String id, name, bus, seat;

        Ticket(String id, String name, String bus, String seat) {
            this.id = id;
            this.name = name;
            this.bus = bus;
            this.seat = seat;
        }
    }

    // Bus Details
    static class Bus {
        String id;
        Node head, current;

        boolean[][] seats = new boolean[2][3];

        Queue<String> waiting = new LinkedList<>();

        Bus(String id, String[] stops) {
            this.id = id;

            for (String stop : stops) {
                addStop(stop);
            }
        }

        // Circular Linked List
        void addStop(String stop) {
            Node newNode = new Node(stop);

            if (head == null) {
                head = newNode;
                head.next = head;
            } else {
                Node temp = head;

                while (temp.next != head) {
                    temp = temp.next;
                }

                temp.next = newNode;
                newNode.next = head;
            }

            current = head;
        }

        void showRoute() {
            Node temp = head;

            System.out.print("Route: ");

            do {
                System.out.print(temp.stop + " -> ");
                temp = temp.next;
            } while (temp != head);

            System.out.println("(Back to first stop)");
        }

        void nextStop() {
            current = current.next;
            System.out.println("Next stop: " + current.stop);
        }

        // Find Available Seat
        String getSeat() {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!seats[i][j]) {
                        seats[i][j] = true;
                        return "" + (char)('A' + j) + (i + 1);
                    }
                }
            }
            return null;
        }

        void releaseSeat(String seat) {
            int col = seat.charAt(0) - 'A';
            int row = seat.charAt(1) - '1';

            seats[row][col] = false;
        }

        void showSeats() {
            System.out.println("\nSeat Status:");

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    String seat = "" + (char)('A' + j) + (i + 1);

                    System.out.print(seat + " : " +
                        (seats[i][j] ? "Booked" : "Available") + "   ");
                }
                System.out.println();
            }
        }
    }

    static Scanner sc = new Scanner(System.in);

    static HashMap<String, Bus> buses = new HashMap<>();
    static HashMap<String, Ticket> tickets = new HashMap<>();

    static int ticketCount = 101;

    // Book Ticket
    static void bookTicket() {
        System.out.print("Enter Bus ID: ");
        String busId = sc.nextLine();

        Bus bus = buses.get(busId);

        if (bus == null) {
            System.out.println("Bus not found!");
            return;
        }

        System.out.print("Enter Passenger Name: ");
        String name = sc.nextLine();

        String seat = bus.getSeat();

        if (seat == null) {
            bus.waiting.add(name);
            System.out.println("Bus full. " + name +
                    " added to waiting queue.");
            return;
        }

        String ticketId = "BT" + ticketCount++;

        Ticket ticket = new Ticket(ticketId, name, busId, seat);

        tickets.put(ticketId, ticket);

        System.out.println("\nTicket Booked Successfully!");
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Passenger: " + name);
        System.out.println("Bus: " + busId);
        System.out.println("Seat: " + seat);
    }

    // Search Ticket
    static void searchTicket() {
        System.out.print("Enter Ticket ID: ");
        String id = sc.nextLine();

        Ticket t = tickets.get(id);

        if (t == null) {
            System.out.println("Ticket " + id + " not found.");
        } else {
            System.out.println("Passenger: " + t.name);
            System.out.println("Bus: " + t.bus);
            System.out.println("Seat: " + t.seat);
        }
    }

    // Cancel Ticket
    static void cancelTicket() {
        System.out.print("Enter Ticket ID: ");
        String id = sc.nextLine();

        Ticket t = tickets.remove(id);

        if (t == null) {
            System.out.println("Ticket not found!");
            return;
        }

        Bus bus = buses.get(t.bus);
        bus.releaseSeat(t.seat);

        System.out.println("Ticket cancelled successfully.");

        // Give cancelled seat to waiting passenger
        if (!bus.waiting.isEmpty()) {
            String name = bus.waiting.poll();

            String seat = bus.getSeat();
            String newId = "BT" + ticketCount++;

            Ticket newTicket = new Ticket(newId, name, bus.id, seat);

            tickets.put(newId, newTicket);

            System.out.println("Waiting passenger allocated seat!");
            System.out.println("Name: " + name);
            System.out.println("New Ticket ID: " + newId);
            System.out.println("Seat: " + seat);
        }
    }

    // Save Data in File
    static void saveData() {
        try {
            PrintWriter pw = new PrintWriter("bus_routes.txt");

            for (Bus b : buses.values()) {
                pw.print("BUS|" + b.id + "|");

                Node temp = b.head;
                do {
                    pw.print(temp.stop + ",");
                    temp = temp.next;
                } while (temp != b.head);

                pw.println();
            }

            for (Ticket t : tickets.values()) {
                pw.println("TICKET|" + t.id + "|" +
                    t.name + "|" + t.bus + "|" + t.seat);
            }

            pw.close();
            System.out.println("Data saved successfully.");

        } catch (Exception e) {
            System.out.println("File saving error!");
        }
    }

    // Load Data from File
    static void loadData() {
        File file = new File("bus_routes.txt");

        if (!file.exists()) return;

        try {
            Scanner fs = new Scanner(file);

            while (fs.hasNextLine()) {
                String line = fs.nextLine();
                String[] data = line.split("\\|");

                if (data[0].equals("BUS")) {
                    String[] stops = data[2].split(",");

                    buses.put(data[1], new Bus(data[1], stops));
                }

                else if (data[0].equals("TICKET")) {
                    Ticket t = new Ticket(
                        data[1], data[2], data[3], data[4]);

                    tickets.put(t.id, t);

                    Bus b = buses.get(t.bus);

                    if (b != null) {
                        int row = t.seat.charAt(1) - '1';
                        int col = t.seat.charAt(0) - 'A';

                        b.seats[row][col] = true;
                    }

                    int number = Integer.parseInt(
                        t.id.substring(2));

                    ticketCount = Math.max(ticketCount, number + 1);
                }
            }

            fs.close();

        } catch (Exception e) {
            System.out.println("File loading error!");
        }
    }

    public static void main(String[] args) {

        // Default Bus
        buses.put("B101", new Bus("B101",
            new String[]{"Aman", "Bus Stand",
                         "City Center", "Railway Station"}));

        loadData();

        int choice;

        do {
            System.out.println("\n===== BUS ROUTE & TICKETING SYSTEM =====");
            System.out.println("1. View Bus Route");
            System.out.println("2. Book Ticket");
            System.out.println("3. Search Ticket");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Next Stop");
            System.out.println("6. Show Seats");
            System.out.println("7. Exit");

            System.out.print("Enter Choice: ");
            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {

                case 1:
                    System.out.print("Enter Bus ID: ");
                    String id = sc.nextLine();

                    Bus b = buses.get(id);

                    if (b != null)
                        b.showRoute();
                    else
                        System.out.println("Bus not found!");
                    break;

                case 2:
                    bookTicket();
                    break;

                case 3:
                    searchTicket();
                    break;

                case 4:
                    cancelTicket();
                    break;

                case 5:
                    System.out.print("Enter Bus ID: ");
                    id = sc.nextLine();

                    b = buses.get(id);

                    if (b != null)
                        b.nextStop();
                    else
                        System.out.println("Bus not found!");
                    break;

                case 6:
                    System.out.print("Enter Bus ID: ");
                    id = sc.nextLine();

                    b = buses.get(id);

                    if (b != null)
                        b.showSeats();
                    else
                        System.out.println("Bus not found!");
                    break;

                case 7:
                    saveData();
                    System.out.println("Thank you!");
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }

        } while (choice != 7);
    }
}