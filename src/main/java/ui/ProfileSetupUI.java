package ui;

import model.Currency;
import model.Profile;
import service.ProfileService;

import java.util.List;
import java.util.Scanner;

/**
 Console-based UI for Profile management.
Flow:
ProfileSetupUI → ProfileService → ProfileRepository → Database
 */
public class ProfileSetupUI {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final ProfileService profileService;
    private final Scanner        scanner;

    // Constructor

    public ProfileSetupUI(ProfileService profileService) {
        this.profileService = profileService;
        this.scanner        = new Scanner(System.in);
    }

    // MAIN MENU

    public Profile start() {
        System.out.println("========================================");
        System.out.println("       Welcome to Fintrack Setup        ");
        System.out.println("========================================");
 
        // Tracks the profile the user creates or selects during this session
        Profile activeProfile = null;
 
        boolean running = true;
 
        while (running) {
            printMainMenu();
            int choice = readIntInput("Enter your choice: ");
 
            switch (choice) {
                case 1 -> activeProfile = handleCreateProfile();
                case 2 -> handleViewAllProfiles();
                case 3 -> handleUpdateProfile();
                case 4 -> handleDeleteProfile();
                case 5 -> {
                    System.out.println("Exiting profile setup. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 5.");
            }
        }
 
        return activeProfile;
    }

    // Print main menu options

    private void printMainMenu() {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("             Profile Menu               ");
        System.out.println("----------------------------------------");
        System.out.println("  1. Create new profile");
        System.out.println("  2. View all profiles");
        System.out.println("  3. Update profile");
        System.out.println("  4. Delete profile");
        System.out.println("  5. Exit");
        System.out.println("----------------------------------------");
    }

    // HANDLE CREATE
    
    private Profile handleCreateProfile() {
        System.out.println("\n--- Create New Profile ---");
 
        // Collect name
        String name = readStringInput("Enter profile name: ");
 
        // Collect currency via numbered list
        Currency selectedCurrency = readCurrencyInput();
 
        if (selectedCurrency == null) {
            System.out.println("Invalid currency selection. Profile creation cancelled.");
            return null;
        }
 
        // Delegate to service — service handles all validation
        Profile created = profileService.createProfile(name, selectedCurrency);
 
        if (created != null) {
            System.out.println("\n✔ Profile created successfully!");
            printProfileDetails(created);
        }
        // If null, ProfileService already printed the reason
 
        return created;
    }

    // HANDLE VIEW ALL

    private void handleViewAllProfiles() {
        System.out.println("\n--- All Profiles ---");

        List<Profile> profiles = profileService.getAllProfiles();

        if (profiles.isEmpty()) {
            System.out.println("No profiles found.");
            return;
        }

        System.out.println("Total profiles: " + profiles.size());
        System.out.println();

        for (Profile profile : profiles) {
            printProfileDetails(profile);
            System.out.println();
        }
    }

    // HANDLE UPDATE

    private void handleUpdateProfile() {
        System.out.println("\n--- Update Profile ---");

        int id = readIntInput("Enter the id of the profile to update: ");

        System.out.println("\nWhat would you like to update?");
        System.out.println("  1. Name only");
        System.out.println("  2. Currency only");
        System.out.println("  3. Both name and currency");

        int choice = readIntInput("Enter your choice: ");

        String   newName     = null;
        Currency newCurrency = null;

        switch (choice) {
            case 1 -> {
                newName = readStringInput("Enter new profile name: ");
            }
            case 2 -> {
                newCurrency = readCurrencyInput();
                if (newCurrency == null) {
                    System.out.println("Invalid currency selection. Update cancelled.");
                    return;
                }
            }
            case 3 -> {
                newName     = readStringInput("Enter new profile name: ");
                newCurrency = readCurrencyInput();
                if (newCurrency == null) {
                    System.out.println("Invalid currency selection. Update cancelled.");
                    return;
                }
            }
            default -> {
                System.out.println("Invalid choice. Update cancelled.");
                return;
            }
        }

        // Delegate to service — passing null means "don't change that field"
        boolean success = profileService.updateProfile(id, newName, newCurrency);

        if (success) {
            System.out.println("✔ Profile updated successfully!");
        }
        // If false, ProfileService already printed the reason
    }

    // HANDLE DELETE
    
    private void handleDeleteProfile() {
        System.out.println("\n--- Delete Profile ---");

        int id = readIntInput("Enter the id of the profile to delete: ");

        // Ask for confirmation before deleting — this cannot be undone
        System.out.print("Are you sure you want to delete profile with id " + id + "? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean success = profileService.deleteProfile(id);

        if (success) {
            System.out.println("✔ Profile deleted successfully.");
        }
        // If false, ProfileService already printed the reason
    }

    // PRIVATE HELPERS

    private Currency readCurrencyInput() {
        Currency[] currencies = Currency.values();

        System.out.println("\nSelect a default currency:");
        for (int i = 0; i < currencies.length; i++) {
            System.out.println("  " + (i + 1) + ". " + currencies[i]);
        }

        int choice = readIntInput("Enter your choice: ");

        // Validate the range
        if (choice < 1 || choice > currencies.length) {
            return null;
        }

        // Arrays are 0-indexed, menu is 1-indexed
        return currencies[choice - 1];
    }

    private void printProfileDetails(Profile profile) {
        System.out.println("  ID       : " + profile.getId());
        System.out.println("  Name     : " + profile.getName());
        System.out.println("  Currency : " + profile.getDefaultCurrency());
        System.out.println("  Created  : " + profile.getCreatedAt());
    }

    private String readStringInput(String prompt) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        }
        return input;
    }

    private int readIntInput(String prompt) {
        System.out.print(prompt);
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return -1;
        }
    }
}