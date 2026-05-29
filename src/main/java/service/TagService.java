package service;

import model.Tag;
import repository.TagRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for Tag — all business logic and validation lives here.
 *
 * Error handling: throws IllegalArgumentException for validation failures,
 * and lets RuntimeException from the repository bubble up to the UI.
 */
public class TagService {

    private final TagRepository tagRepository;

    public TagService() {
        this.tagRepository = new TagRepository();
    }

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // ── createTag ─────────────────────────────────────────────────────────────

    /**
     * Creates and saves a new tag for the given profile.
     * @throws IllegalArgumentException if name is blank or already exists for this profile
     */
    public Tag createTag(int profileId, String name) {
        String trimmed = validateName(name);

        if (tagRepository.findByNameAndProfile(trimmed, profileId).isPresent()) {
            throw new IllegalArgumentException(
                "A tag named \"" + trimmed + "\" already exists for this profile.");
        }

        return tagRepository.save(new Tag(profileId, trimmed));
    }

    // ── updateTag ─────────────────────────────────────────────────────────────

    /**
     * Renames an existing tag.
     * @throws IllegalArgumentException if name blank, tag not found, wrong profile, or name taken
     */
    public Tag updateTag(int tagId, int profileId, String newName) {
        String trimmed = validateName(newName);

        Tag existing = requireTag(tagId, profileId);

        if (!existing.getName().equalsIgnoreCase(trimmed) &&
            tagRepository.findByNameAndProfile(trimmed, profileId).isPresent()) {
            throw new IllegalArgumentException(
                "A tag named \"" + trimmed + "\" already exists for this profile.");
        }

        existing.setName(trimmed);
        tagRepository.update(existing);
        return existing;
    }

    // ── deleteTag ─────────────────────────────────────────────────────────────

    /**
     * Deletes a tag by id, verifying it belongs to the given profile.
     * @throws IllegalArgumentException if tag not found or wrong profile
     */
    public void deleteTag(int tagId, int profileId) {
        requireTag(tagId, profileId);
        tagRepository.deleteById(tagId);
    }

    // ── assignTag ─────────────────────────────────────────────────────────────

    /**
     * Assigns a tag to a transaction in the same profile. Silently ignores duplicate assignments.
     * @throws IllegalArgumentException if tag or transaction is not found for this profile
     */
    public void assignTag(int transactionId, int tagId, int profileId) {
        requireTag(tagId, profileId);
        requireTransaction(transactionId, profileId);
        tagRepository.assignTagToTransaction(transactionId, tagId, profileId);
    }

    // ── removeTag ─────────────────────────────────────────────────────────────

    /**
     * Removes a tag from a transaction in the same profile.
     */
    public void removeTag(int transactionId, int tagId, int profileId) {
        requireTag(tagId, profileId);
        requireTransaction(transactionId, profileId);
        tagRepository.removeTagFromTransaction(transactionId, tagId);
    }

    // ── getTagsForTransaction ─────────────────────────────────────────────────

    public List<Tag> getTagsForTransaction(int transactionId) {
        return tagRepository.findTagsByTransaction(transactionId);
    }

    // ── getAllTagsForProfile ───────────────────────────────────────────────────

    public List<Tag> getAllTagsForProfile(int profileId) {
        return tagRepository.findAllByProfile(profileId);
    }

    // ── findById ──────────────────────────────────────────────────────────────

    public Optional<Tag> findById(int tagId) {
        return tagRepository.findById(tagId);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty.");
        }
        return name.trim();
    }

    private void requireTransaction(int transactionId, int profileId) {
        if (!tagRepository.transactionBelongsToProfile(transactionId, profileId)) {
            throw new IllegalArgumentException(
                "Transaction with id=" + transactionId + " was not found for this profile.");
        }
    }

    private Tag requireTag(int tagId, int profileId) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag with id=" + tagId + " not found."));
        if (tag.getProfileId() != profileId) {
            throw new IllegalArgumentException("This tag does not belong to your profile.");
        }
        return tag;
    }
}
