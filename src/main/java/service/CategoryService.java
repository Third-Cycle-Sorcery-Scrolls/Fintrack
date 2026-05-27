package service;

import model.Category;
import repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for Category.
 *
 * Rule: Service = logic.
 * All validation and business rules live here.
 * The UI calls the service. The service calls the repository.
 * The service never talks to the DB directly.
 *
 * Responsibilities (from the task sheet):
 *   ✔ add category
 *   ✔ delete category
 *   ✔ list categories
 *   ✔ unique category name validation per profile
 */
public class CategoryService {

    // The service OWNS its repository — UI never touches the repository directly
    private final CategoryRepository categoryRepository;

    // ── Constructor ───────────────────────────────────────────────────────────

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    // Allow injection (useful for testing or if Integration wires things up)
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ── Add category ──────────────────────────────────────────────────────────

    /**
     * Creates and saves a new category for the given profile.
     *
     * Validation rules:
     *   1. Name must not be blank
     *   2. Name must be unique within the profile (case-insensitive)
     *
     * Returns the saved Category on success, or null on failure.
     * Prints a user-friendly message explaining why it failed.
     */
    public Category addCategory(int profileId, String name) {

        // Rule 1: name must not be blank
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Category name cannot be empty.");
            return null;
        }

        String trimmedName = name.trim();

        // Rule 2: name must be unique per profile
        if (!isCategoryNameAvailable(profileId, trimmedName)) {
            System.out.println("Error: A category named \"" + trimmedName +
                               "\" already exists for this profile.");
            return null;
        }

        // All good — create the object and save it
        Category newCategory = new Category(profileId, trimmedName);
        Category saved = categoryRepository.save(newCategory);

        if (saved == null) {
            System.out.println("Error: Could not save category. Please try again.");
            return null;
        }

        System.out.println("Category \"" + saved.getName() + "\" added successfully (id=" + saved.getId() + ").");
        return saved;
    }

    // ── Delete category ───────────────────────────────────────────────────────

    /**
     * Deletes a category by its id.
     *
     * Validation rules:
     *   1. Category must exist
     *   2. Category must belong to the given profile (security: can't delete another profile's data)
     *
     * Returns true on success, false on failure.
     */
    public boolean deleteCategory(int categoryId, int profileId) {

        // Rule 1: does it exist?
        Optional<Category> result = categoryRepository.findById(categoryId);
        if (result.isEmpty()) {
            System.out.println("Error: Category with id=" + categoryId + " not found.");
            return false;
        }

        Category existing = result.get();

        // Rule 2: does it belong to this profile?
        if (existing.getProfileId() != profileId) {
            System.out.println("Error: This category does not belong to your profile.");
            return false;
        }

        // deleteById returns void — we just call it and trust it worked
        // If the DB blocks it (foreign key), the repository prints the error
        categoryRepository.deleteById(categoryId);
        System.out.println("Category \"" + existing.getName() + "\" deleted successfully.");
        return true;
    }

    // ── List categories ───────────────────────────────────────────────────────

    /**
     * Returns all categories for the given profile.
     * Returns an empty list (never null) if there are none.
     */
    public List<Category> getCategoriesForProfile(int profileId) {
        return categoryRepository.findAllByProfileId(profileId);
    }

    // ── Get by id ─────────────────────────────────────────────────────────────

    /**
     * Fetches a single category by id.
     * Returns null if not found.
     * Used by other services (e.g. TransactionService) to validate a categoryId.
     */
    public Category getCategoryById(int categoryId) {
        // findById returns Optional — unwrap it, return null if not found
        return categoryRepository.findById(categoryId).orElse(null);
    }

    // ── Update / rename category ──────────────────────────────────────────────

    /**
     * Renames an existing category.
     *
     * Steps (follow project rule: findById first, then modify, then update):
     *   1. Find the category
     *   2. Validate ownership
     *   3. Check new name is unique
     *   4. Modify and save
     *
     * Returns the updated Category on success, null on failure.
     */
    public Category renameCategory(int categoryId, int profileId, String newName) {

        if (newName == null || newName.trim().isEmpty()) {
            System.out.println("Error: New category name cannot be empty.");
            return null;
        }

        String trimmedName = newName.trim();

        // Step 1 — findById returns Optional, unwrap it
        Optional<Category> result = categoryRepository.findById(categoryId);
        if (result.isEmpty()) {
            System.out.println("Error: Category with id=" + categoryId + " not found.");
            return null;
        }

        Category existing = result.get();

        // Step 2 — ownership check
        if (existing.getProfileId() != profileId) {
            System.out.println("Error: This category does not belong to your profile.");
            return null;
        }

        // Step 3 — unique name check (skip if name hasn't actually changed)
        if (!existing.getName().equalsIgnoreCase(trimmedName) &&
            !isCategoryNameAvailable(profileId, trimmedName)) {
            System.out.println("Error: A category named \"" + trimmedName +
                               "\" already exists for this profile.");
            return null;
        }

        // Step 4 — modify then update (update returns void now)
        existing.setName(trimmedName);
        categoryRepository.update(existing);
        System.out.println("Category renamed to \"" + trimmedName + "\" successfully.");
        return existing;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns true if the name is NOT already taken for this profile.
     * (True = the name is available to use.)
     */
    private boolean isCategoryNameAvailable(int profileId, String name) {
        // findByNameAndProfileId returns Category directly (it's our own extra method, not from interface)
        Category found = categoryRepository.findByNameAndProfileId(name, profileId);
        return found == null;
    }
}
