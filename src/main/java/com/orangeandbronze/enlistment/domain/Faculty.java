package com.orangeandbronze.enlistment.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import static org.apache.commons.lang3.Validate.*;

@Entity
public class Faculty {
    @Id
    private final int facultyNumber;
    private final String firstname;
    private final String lastname;
    @OneToMany
    private final Collection<Section> sections = new HashSet<>();

    Faculty(int facultyNumber, String firstname, String lastname) {
        isTrue (facultyNumber >= 0,
                "facultyNumber can't be negative, was: " + facultyNumber);
        notBlank(firstname);
        notBlank(lastname);

        this.facultyNumber = facultyNumber;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public void addSection(Section section) {
        notNull(section);
        checkForScheduleConflict(section);
        sections.add(section);
    }

    private void checkForScheduleConflict (Section section) {
        for (Section s : sections) {
            s.checkForScheduleConflict(section);
        }
    }


    public int getFacultyNumber() {
        return facultyNumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    @Override
    public String toString() {
        return lastname + ", " + firstname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faculty faculty = (Faculty) o;
        return facultyNumber == faculty.facultyNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(facultyNumber, firstname, lastname);
    }

    // For JPA/Hibernate, do not delete
    private Faculty() {
        facultyNumber = -1;
        this.lastname = null;
        this.firstname = null;
    }
}
