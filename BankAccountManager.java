import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * PS 29 - Simple Bank Account Manager
 * Concepts: ArrayList, Searching, Sorting, Methods, File Handling
 * Author  : Jaykishor Vaishnaw (RU-25-10616)
 */

/** One bank account record. */
class Account {
    private final String id;
    private final String name;
    private double balance;

    Account(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    String getId()       { return id; }
    String getName()     { return name; }
    double getBalance()  { return balance; }

    void deposit(double amount)  { balance += amount; }
    void withdraw(double amount) { balance -= amount; }
}

public class BankAccountManager {

    private static final String FILE_NAME = "accounts.txt";
    private static final int FIRST_ID = 101;

    private static final ArrayList<Account> accounts = new ArrayList<>();
    private static int nextId = FIRST_ID;
    private static Scanner sc;

    // ---------------------------------------------------------------- main
    public static void main(String[] args) {
        sc = new Scanner(System.in);
        loadFromFile();

        boolean running = true;
        while (running) {
            showMenu();
            String line = readLine("Enter your choice: ");
            if (line == null) {            // input finished (e.g. piped input)
                break;
            }
            switch (line.trim()) {
                case "1": createAccount();   break;
                case "2": depositMoney();    break;
                case "3": withdrawMoney();   break;
                case "4": searchByName();    break;
                case "5": displaySorted();   break;
                case "6": running = false;   break;
                default:  System.out.println("Invalid choice. Enter a number from 1 to 6.");
            }
        }

        saveToFile();
        System.out.println("Accounts saved to " + FILE_NAME + ". Goodbye!");
    }

    // ---------------------------------------------------------------- menu
    private static void showMenu() {
        System.out.println();
        System.out.println("===== Simple Bank Account Manager =====");
        System.out.println("1. Create Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Search by Name");
        System.out.println("5. Display Sorted by Balance (High to Low)");
        System.out.println("6. Exit");
        System.out.println("---------------------------------------");
    }

    // ------------------------------------------------------ 1. create account
    private static void createAccount() {
        String name = readLine("Name: ");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty() || name.indexOf(',') >= 0) {
            System.out.println("Invalid name. Name cannot be empty or contain a comma.");
            return;
        }

        double opening = readAmount("Opening balance: ");
        if (Double.isNaN(opening)) return;
        if (opening < 0) {
            System.out.println("Opening balance cannot be negative.");
            return;
        }

        Account acc = new Account("A" + nextId, name, opening);
        nextId++;
        accounts.add(acc);
        System.out.println("Account created. ID: " + acc.getId() + " | "
                + acc.getName() + " | Balance: " + money(acc.getBalance()));
    }

    // ---------------------------------------------------------- 2. deposit
    private static void depositMoney() {
        Account acc = askForAccount();
        if (acc == null) return;

        double amount = readAmount("Amount: ");
        if (Double.isNaN(amount)) return;
        if (amount <= 0) {
            System.out.println("Invalid amount. Deposit must be greater than 0.");
            return;
        }
        acc.deposit(amount);
        System.out.println("Deposited " + money(amount) + ". New balance: " + money(acc.getBalance()));
    }

    // --------------------------------------------------------- 3. withdraw
    private static void withdrawMoney() {
        Account acc = askForAccount();
        if (acc == null) return;

        double amount = readAmount("Amount: ");
        if (Double.isNaN(amount)) return;
        if (amount <= 0) {
            System.out.println("Invalid amount. Withdrawal must be greater than 0.");
            return;
        }
        if (amount > acc.getBalance()) {
            System.out.println("Insufficient balance. Current balance: " + money(acc.getBalance()));
            return;
        }
        acc.withdraw(amount);
        System.out.println("Withdrawn " + money(amount) + ". New balance: " + money(acc.getBalance()));
    }

    // ------------------------------------------------------ 4. search by name
    private static void searchByName() {
        String key = readLine("Enter name to search: ");
        if (key == null) return;
        key = key.trim();
        if (key.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }

        ArrayList<Account> found = linearSearchByName(key);
        if (found.isEmpty()) {
            System.out.println("No account found for \"" + key + "\".");
            return;
        }
        System.out.println(found.size() + " account(s) found:");
        for (Account a : found) {
            System.out.println(a.getId() + " | " + a.getName() + " | " + money(a.getBalance()));
        }
    }

    /** Linear search: returns every account whose name contains the key (ignoring case). */
    private static ArrayList<Account> linearSearchByName(String key) {
        ArrayList<Account> result = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            if (containsIgnoreCase(accounts.get(i).getName(), key)) {
                result.add(accounts.get(i));
            }
        }
        return result;
    }

    /** Hand-written substring check (no built-in search methods). */
    private static boolean containsIgnoreCase(String text, String key) {
        String t = text.toLowerCase();
        String k = key.toLowerCase();
        for (int start = 0; start + k.length() <= t.length(); start++) {
            int j = 0;
            while (j < k.length() && t.charAt(start + j) == k.charAt(j)) {
                j++;
            }
            if (j == k.length()) return true;
        }
        return false;
    }

    /** Linear search by account ID (case-insensitive). Returns null when not found. */
    private static Account findById(String id) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId().equalsIgnoreCase(id)) {
                return accounts.get(i);
            }
        }
        return null;
    }

    // ----------------------------------------------- 5. display sorted by balance
    private static void displaySorted() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts to display.");
            return;
        }
        ArrayList<Account> sorted = sortByBalanceDesc(accounts);
        for (int i = 0; i < sorted.size(); i++) {
            Account a = sorted.get(i);
            System.out.println((i + 1) + ". " + a.getId() + " | " + a.getName()
                    + " | " + money(a.getBalance()));
        }
    }

    /**
     * Insertion sort (stable) on a copy of the list, highest balance first.
     * Written by hand - no Collections.sort() or Comparator.
     */
    private static ArrayList<Account> sortByBalanceDesc(ArrayList<Account> source) {
        ArrayList<Account> list = new ArrayList<>(source);
        for (int i = 1; i < list.size(); i++) {
            Account key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).getBalance() < key.getBalance()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
        return list;
    }

    // ------------------------------------------------------------ file handling
    private static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length != 3) continue;           // skip damaged lines
                try {
                    String id = p[0].trim();
                    double bal = Double.parseDouble(p[2].trim());
                    accounts.add(new Account(id, p[1].trim(), bal));
                    int num = Integer.parseInt(id.substring(1));
                    if (num >= nextId) nextId = num + 1;
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // ignore a damaged record and carry on
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }
    }

    private static void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                bw.write(a.getId() + "," + a.getName() + "," + money(a.getBalance()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ input helpers
    /** Asks for an account ID and returns the account, or null (with a message) if invalid. */
    private static Account askForAccount() {
        String id = readLine("Account ID: ");
        if (id == null) return null;
        Account acc = findById(id.trim());
        if (acc == null) {
            System.out.println("Account not found: " + id.trim());
        }
        return acc;
    }

    /** Prints the prompt and returns the typed line, or null when input has ended. */
    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            System.out.println();
            return null;
        }
        return sc.nextLine();
    }

    /** Reads a number. Returns NaN if the text is not a valid number or input has ended. */
    private static double readAmount(String prompt) {
        String s = readLine(prompt);
        if (s == null) return Double.NaN;
        try {
            double v = Double.parseDouble(s.trim());
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
            return Double.NaN;
        }
    }

    /** Whole numbers print without decimals (5000), others with two (5000.50). */
    private static String money(double v) {
        if (v == Math.floor(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.format("%.2f", v);
    }
}