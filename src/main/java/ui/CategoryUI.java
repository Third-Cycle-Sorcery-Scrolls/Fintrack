package ui;

import model.Category;
import service.CategoryService;

import java.util.List;
import java.util.Scanner;

/**
 * Terminal UI for Category management.
 *
 * Rules:
 *   - ONLY calls CategoryService — never the repository directly
 *   - Handles all user input/output for category features
 *   - Needs a profileId (passed in from DashboardUI after login)
 *
 * Menu options:
 *   1. List categories
 *   2. Add category
 *   3. Rename category
 *   4. Delete category
 *   0. Back
 */
public class CategoryUI {

    private final CategoryService categoryService;
    private final Scanner scanner;

    // ── Constructor ───────────────────────────────────────────────────────────

    public CategoryUI() {
        this.categoryService = new CategoryService();
        this.scanner         = new Scanner(System.in);
    }

    // Allow injection (Integration teammate can pass these in from DashboardUI)
    public CategoryUI(CategoryService categoryService, Scanner scanner) {
        this.categoryService = categoryService;
        this.scanner         = scanner;
    }

    // ── Main entry point ──────────────────────────────────────────────────────

    /**
     * Launch the category menu for the given profile.
     * Call this from DashboardUI passing in the logged-in profile's id.
     *
     * Example from DashboardUI:
     *   CategoryUI categoryUI = new CategoryUI();
     *   categoryUI.show(currentProfile.getId());
     */
    public void show(int profileId) {
        boolean running = true;

        while (running) {
            printMenu();
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> listCategories(profileId);
                case "2" -> addCategory(profileId);
                case "3" -> renameCategory(profileId);
                case "4" -> deleteCategory(profileId);
                case "0" -> {
                    System.out.println("Returning to main menu...");
                    running = false;
                }
                default  -> System.out.println("Invalid option. Please enter 0-4.");
            }
        }
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private void printMenu() {
        System.out.println();
        System.out.println("════════════════════════════");
        System.out.println("       CATEGORY MENU        ");
        System.out.println("════════════════════════════");
        System.out.println("  1. List categories");
        System.out.println("  2. Add category");
        System.out.println("  3. Rename category");
        System.out.println("  4. Delete category");
        System.out.println("  0. Back");
        System.out.println("════════════════════════════");
        System.out.print("Choose an option: ");
    }

    // ── List ──────────────────────────────────────────────────────────────────

    private void listCategories(int profileId) {
        List<Category> categories = categoryService.getCategoriesForProfile(profileId);

        System.out.println();
        if (categories.isEmpty()) {
            System.out.println("No categories found. Add one first!");
            return;
        }

        System.out.println("Your categories:");
        System.out.println("──────────────────────────────");
        for (Category c : categories) {
            System.out.printf("  [%d] %s%n", c.getId(), c.getName());
        }
        System.out.println("──────────────────────────────");
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    private void addCategory(int profileId) {
        System.out.println();
        System.out.print("Enter category name: ");
        String name = scanner.nextLine().trim();

        // Hand off to service — it prints success/error messages
        categoryService.addCategory(profileId, name);
    }

    // ── Rename ────────────────────────────────────────────────────────────────

    private void renameCategory(int profileId) {
        // Show the list first so the user knows which id to pick
        listCategories(profileId);

        List<Category> categories = categoryService.getCategoriesForProfile(profileId);
        if (categories.isEmpty()) return; // nothing to rename

        System.out.print("Enter the id of the category to rename: ");
        int id = readInt();
        if (id == -1) return; // user entered something invalid

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();

        categoryService.renameCategory(id, profileId, newName);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    private void deleteCategory(int profileId) {
        // Show the list first so the user knows which id to pick
        listCategories(profileId);

        List<Category> categories = categoryService.getCategoriesForProfile(profileId);
        if (categories.isEmpty()) return; // nothing to delete

        System.out.print("Enter the id of the category to delete: ");
        int id = readInt();
        if (id == -1) return;

        // Confirm before deleting
        System.out.print("Are you sure you want to delete this category? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        categoryService.deleteCategory(id, profileId);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Safely reads an integer from the user.
     * Returns -1 if the input is not a valid number.
     */
    private int readInt() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }
}
