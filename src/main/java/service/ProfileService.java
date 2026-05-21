package service;

import model.Currency;
import model.Profile;
import repository.ProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class ProfileService {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    // Dependency — ProfileRepository

    private final ProfileRepository profileRepository;

    // Constructor

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // CREATE
  
    public Profile createProfile(String name, Currency defaultCurrency) {

        // --- Validate name ---

        // Check for null or blank (handles "", "   ", and null all at once)
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Profile name cannot be empty.");
            return null;
        }

        // Normalize — remove leading/trailing spaces before further checks
        String trimmedName = name.trim();

        // Check minimum length
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            System.out.println("Profile name must be at least " + MIN_NAME_LENGTH + " characters.");
            return null;
        }

        // Check maximum length
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            System.out.println("Profile name must be at most " + MAX_NAME_LENGTH + " characters.");
            return null;
        }

        // Check for duplicate — ask the repository if a profile with this name exists
        // We compare in lowercase so "Alex" and "alex" are treated as the same name
        if (profileExists(trimmedName)) {
            System.out.println("A profile with the name '" + trimmedName + "' already exists.");
            return null;
        }

        // --- Validate currency ---

        if (defaultCurrency == null) {
            System.out.println("Default currency must be selected.");
            return null;
        }

        // --- All validations passed — build and save the profile ---

        // createdAt is always set here in the service, never supplied by the user
        Profile newProfile = new Profile(trimmedName, defaultCurrency, LocalDateTime.now());

        Profile savedProfile = profileRepository.save(newProfile);

        if (savedProfile == null) {
            System.out.println("Failed to save profile. Please try again.");
            return null;
        }

        System.out.println("Profile '" + trimmedName + "' created successfully.");
        return savedProfile;
    }

    // READ — single profile

    public Profile getProfileById(Integer id) {

        if (id == null) {
            System.out.println("Profile id cannot be null.");
            return null;
        }

        Optional<Profile> result = profileRepository.findById(id);

        if (result.isEmpty()) {
            System.out.println("No profile found with id: " + id);
            return null;
        }

        return result.get();
    }

    // READ — all profiles
    
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    // UPDATE
    
    public boolean updateProfile(Integer id, String newName, Currency newCurrency) {

        // Step 1 — fetch the existing profile first
        // Never call update() without doing this — the repository assumes the
        // entity already exists and will silently do nothing if it doesn't.
        Optional<Profile> result = profileRepository.findById(id);

        if (result.isEmpty()) {
            System.out.println("Cannot update — no profile found with id: " + id);
            return false;
        }

        // Get the current profile object with all its existing field values
        Profile existingProfile = result.get();

        // Step 2 & 3 — validate and apply new name if provided
        if (newName != null) {

            String trimmedName = newName.trim();

            if (trimmedName.isEmpty()) {
                System.out.println("Profile name cannot be empty.");
                return false;
            }

            if (trimmedName.length() < MIN_NAME_LENGTH) {
                System.out.println("Profile name must be at least " + MIN_NAME_LENGTH + " characters.");
                return false;
            }

            if (trimmedName.length() > MAX_NAME_LENGTH) {
                System.out.println("Profile name must be at most " + MAX_NAME_LENGTH + " characters.");
                return false;
            }

            // Check duplicate only if the name is actually changing
            if (!trimmedName.equalsIgnoreCase(existingProfile.getName()) && profileExists(trimmedName)) {
                System.out.println("A profile with the name '" + trimmedName + "' already exists.");
                return false;
            }

            existingProfile.setName(trimmedName);
        }

        // Apply new currency if provided
        if (newCurrency != null) {
            existingProfile.setDefaultCurrency(newCurrency);
        }

        // Step 4 — pass the modified object to the repository
        // createdAt is intentionally not touched — it never changes after creation
        profileRepository.update(existingProfile);

        System.out.println("Profile updated successfully.");
        return true;
    }

    // DELETE

    public boolean deleteProfile(Integer id) {

        if (id == null) {
            System.out.println("Profile id cannot be null.");
            return false;
        }

        // Verify the profile exists before attempting to delete
        Optional<Profile> result = profileRepository.findById(id);

        if (result.isEmpty()) {
            System.out.println("Cannot delete — no profile found with id: " + id);
            return false;
        }

        profileRepository.deleteById(id);

        System.out.println("Profile deleted successfully.");
        return true;
    }

    // PRIVATE HELPERS

    private boolean profileExists(String name) {
        Optional<Profile> result = profileRepository.findByName(name.trim().toLowerCase());
        return result.isPresent();
    }
}