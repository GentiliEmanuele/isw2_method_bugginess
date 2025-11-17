package org.isw2.git.model;

import java.util.Objects;

public class Author {
    private String name;
    private String authorEmail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(name, author.name) &&
                Objects.equals(authorEmail, author.authorEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, authorEmail);
    }
}
