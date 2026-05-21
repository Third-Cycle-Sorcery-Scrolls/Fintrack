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

    private final ProfileRepository profileRepository;
    private Profile activeProfile;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile save(Profile profile) {
        Profile savedProfile = createProfile(profile.getName(), profile.getDefaultCurrency());
        if (savedProfile == null) {
            throw new IllegalArgumentException("Profile could not be saved.");
        }
        return savedProfile;
    }

    public Profile createProfile(String name, Currency defaultCurrency) {
        String trimmedName = validateName(name);
        if (defaultCurrency == null) {
            throw new IllegalArgumentException("Default currency must be selected.");
        }
        if (profileExists(trimmedName)) {
            throw new IllegalArgumentException("A profile with this name already exists.");
        }

        Profile savedProfile = profileRepository.save(new Profile(trimmedName, defaultCurrency, LocalDateTime.now()));
        activeProfile = savedProfile;
        return savedProfile;
    }

    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    public List<Profile> getAllProfiles() {
        return findAll();
    }

    public Optional<Profile> findById(Integer id) {
        return profileRepository.findById(id);
    }

    public Profile getProfileById(Integer id) {
        return findById(id).orElse(null);
    }

    public Optional<Profile> getActiveProfile() {
        return Optional.ofNullable(activeProfile);
    }

    public Profile requireActiveProfile() {
        return getActiveProfile()
                .orElseThrow(() -> new IllegalStateException("Create or select a profile before continuing."));
    }

    public void setActiveProfile(Profile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile is required.");
        }
        activeProfile = profile;
    }

    public void update(Profile profile) {
        updateProfile(profile.getId(), profile.getName(), profile.getDefaultCurrency());
    }

    public boolean updateProfile(Integer id, String newName, Currency newCurrency) {
        if (id == null) {
            throw new IllegalArgumentException("Profile id cannot be null.");
        }

        Profile existingProfile = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No profile found with id: " + id));

        if (newName != null) {
            String trimmedName = validateName(newName);
            if (!trimmedName.equalsIgnoreCase(existingProfile.getName()) && profileExists(trimmedName)) {
                throw new IllegalArgumentException("A profile with this name already exists.");
            }
            existingProfile.setName(trimmedName);
        }
        if (newCurrency != null) {
            existingProfile.setDefaultCurrency(newCurrency);
        }

        profileRepository.update(existingProfile);
        activeProfile = existingProfile;
        return true;
    }

    public void deleteById(Integer id) {
        deleteProfile(id);
    }

    public boolean deleteProfile(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Profile id cannot be null.");
        }
        profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No profile found with id: " + id));
        profileRepository.deleteById(id);
        if (activeProfile != null && id.equals(activeProfile.getId())) {
            activeProfile = null;
        }
        return true;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty.");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException("Profile name must be at least " + MIN_NAME_LENGTH + " characters.");
        }
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Profile name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        return trimmedName;
    }

    private boolean profileExists(String name) {
        return profileRepository.findByName(name).isPresent();
    }
}