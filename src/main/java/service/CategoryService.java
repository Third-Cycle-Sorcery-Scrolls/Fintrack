package service;

import model.Category;
import repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for Category — all business logic and validation lives here.
 *
 * Error handling: throws IllegalArgumentException for validation failures,
 * and lets RuntimeException from the repository bubble up to the UI.
 * The UI catches everything and shows it in the TextArea.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ── Add category ──────────────────────────────────────────────────────────

    /**
     * Creates and saves a new category.
     * @throws IllegalArgumentException if name is blank or already exists
     * @throws RuntimeException if database operation fails
     */
    public Category addCategory(int profileId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        String trimmedName = name.trim();

        if (!isCategoryNameAvailable(profileId, trimmedName)) {
            throw new IllegalArgumentException(
                "A category named \"" + trimmedName + "\" already exists for this profile."
            );
        }

        Category newCategory = new Category(profileId, trimmedName);
        return categoryRepository.save(newCategory);
    }

    // ── Delete category ───────────────────────────────────────────────────────

    /**
     * Deletes a category by id.
     * @throws IllegalArgumentException if category not found or wrong profile
     * @throws RuntimeException         if database operation fails
     */
    public boolean deleteCategory(int categoryId, int profileId) {
        Optional<Category> result = categoryRepository.findById(categoryId);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Category with id=" + categoryId + " not found.");
        }

        Category existing = result.get();

        if (existing.getProfileId() != profileId) {
            throw new IllegalArgumentException("This category does not belong to your profile.");
        }

        categoryRepository.deleteById(categoryId);

        return true;
    }

    // ── List categories ───────────────────────────────────────────────────────

    /**
     * Returns all categories for the given profile.
     * @throws RuntimeException if database operation fails
     */
    public List<Category> getCategoriesForProfile(int profileId) {
        return categoryRepository.findAllByProfileId(profileId);
    }
    
    public List<Category> findAll() {
         return categoryRepository.findAll();
    }
    // ── Get by id ─────────────────────────────────────────────────────────────

    /**
     * Fetches a single category by id.
     * @throws RuntimeException if database operation fails
     */
    public Category getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId).orElse(null);
    }

    // ── Rename category ───────────────────────────────────────────────────────

    /**
     * Renames an existing category.
     * @throws IllegalArgumentException if name blank, category not found, wrong profile, or name taken
     * @throws RuntimeException if database operation fails
     */
    public Category renameCategory(int categoryId, int profileId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New category name cannot be empty.");
        }

        String trimmedName = newName.trim();

        // findById first — required by project rules before calling update
        Optional<Category> result = categoryRepository.findById(categoryId);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Category with id=" + categoryId + " not found.");
        }

        Category existing = result.get();

        if (existing.getProfileId() != profileId) {
            throw new IllegalArgumentException("This category does not belong to your profile.");
        }

        if (!existing.getName().equalsIgnoreCase(trimmedName) &&
            !isCategoryNameAvailable(profileId, trimmedName)) {
            throw new IllegalArgumentException(
                "A category named \"" + trimmedName + "\" already exists for this profile."
            );
        }

        existing.setName(trimmedName);
        categoryRepository.update(existing);
        return existing;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isCategoryNameAvailable(int profileId, String name) {
        Category found = categoryRepository.findByNameAndProfileId(name, profileId);
        return found == null;
    }
}
