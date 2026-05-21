package model;

public class Category {
    private int id;
    private int profileId;
    private String name;

    public Category() {
    }

    public Category(int id, int profileId, String name) {
        this.id = id;
        this.profileId = profileId;
        this.name = name;
    }

    public Category(int profileId, String name) {
        this.profileId = profileId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", profileId=" + profileId +
                ", name='" + name + '\'' +
                '}';
    }
}
