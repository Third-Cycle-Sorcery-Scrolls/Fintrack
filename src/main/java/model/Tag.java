package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Tag {
    private int id;
    private int profileId;
    private String name;
    private LocalDateTime createdAt;

    public Tag() {}

    public Tag(int profileId, String name) {
        this.profileId = profileId;
        this.name = name;
    }

    public Tag(int id, int profileId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id == tag.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name; }
}
