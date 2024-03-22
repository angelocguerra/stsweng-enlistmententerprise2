package com.orangeandbronze.enlistment.domain;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.*;
import java.util.concurrent.locks.*;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.Validate.*;

/**
 * Represents a Section in the system.
 */
@Entity
public class Section {
    @Id
    private final String sectionId;
    @ManyToOne
    private final Subject subject;
    @Embedded
    private final Schedule schedule;
    @ManyToOne
    private final Room room;

    @ManyToOne
    private final Faculty instructor;

    private int numberOfStudents = 0;

    @Version
    @ColumnDefault("0")
    private int version = 0;

    @Transient
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a Section object with specified attributes.
     *
     * @param sectionId the section ID
     * @param subject   the subject associated with the section
     * @param schedule  the schedule of the section
     * @param room      the room where the section takes place
     */
    public Section(String sectionId, Subject subject, Schedule schedule, Room room, Faculty instructor) {
        notBlank(sectionId,
                "sectionId can't be null, empty or whitespace ");
        notNull(subject);
        isTrue(isAlphanumeric(sectionId),
                "sectionId must be alphanumeric, was: "
                        + sectionId);
        notNull(room);
        notNull(instructor);
        this.sectionId = sectionId;
        this.subject = subject;
        this.schedule = schedule;
        room.addSection(this);
        this.room = room;
        this.instructor = instructor;
    }

    /**
     * Constructs a Section object with specified attributes.
     *
     * @param sectionId       the section ID
     * @param subject         the subject associated with the section
     * @param schedule        the schedule of the section
     * @param room            the room where the section takes place
     * @param numberOfStudents the number of students enrolled in the section
     */
    Section(String sectionId, Subject subject, Schedule schedule, Room room, Faculty instructor, int numberOfStudents) {
        this(sectionId, subject, schedule, room, instructor);
        isTrue(numberOfStudents >= 0,
                "numberOfStudents must be non-negative, was: " + numberOfStudents);
        this.numberOfStudents = numberOfStudents;
    }

    void checkSameSubject(Section other) {
        if (this.subject.equals(other.subject)) {
            throw new SameSubjectException("This section " + this + " & other section " + other +
                    " have same subject of " + subject);
        }
    }

    void checkForScheduleConflict(Section other) {
        this.schedule.checkOverlap(other.schedule);
    }


    int getNumberOfStudents() {
        return numberOfStudents;
    }


    void incrementNumberOfStudents() {
        room.checkIfAtOrOverCapacity(numberOfStudents);
        numberOfStudents++;
    }

    void decrementNumberOfStudents() {
        numberOfStudents--;
    }

    void checkPrereqs(Collection<Subject> subjectsTaken) {
        notNull(subjectsTaken);
        Collection<Subject> copy = new HashSet<>(subjectsTaken); // sets are quicker to search through
        subject.checkPrereqs(copy);
    }

    public void checkIfFull() {
        room.checkIfAtOrOverCapacity(numberOfStudents);
    }

    /** Locks this object's ReentrantLock **/
    void lock() {
        lock.lock();
    }

    /** Unlock this object's ReentrantLock **/
    void unlock() {
        lock.unlock();
    }

    public String getSectionId() {
        return sectionId;
    }

    public Subject getSubject() {
        return subject;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Room getRoom() {
        return room;
    }

    public Faculty getInstructor() {
        return instructor;
    }

    @Override
    public String toString() {
        return sectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Section section = (Section) o;

        return sectionId != null ? sectionId.equals(section.sectionId) : section.sectionId == null;
    }

    @Override
    public int hashCode() {
        return sectionId != null ? sectionId.hashCode() : 0;
    }

    // For JPA only. Do not call!
    private Section() {
        sectionId = null;
        subject = null;
        schedule = null;
        room = null;
        instructor = null;
    }
}
