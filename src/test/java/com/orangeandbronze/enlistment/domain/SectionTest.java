package com.orangeandbronze.enlistment.domain;

import org.junit.jupiter.api.*;

import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void newSection_same_room_diff_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room, newFaculty(1001));
        assertDoesNotThrow(() -> new Section("B", DEFAULT_SUBJECT, TF10to1130, room, newFaculty(1002)));
    }

    @Test
    void newSection_same_room_same_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room, newFaculty(1001));
        assertThrows(ScheduleConflictException.class, () -> new Section("B", DEFAULT_SUBJECT, MTH830to10, room, newFaculty(1002)));
    }

    @Test
    void newSection_same_room_overlap_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room, newFaculty(1001));
        assertThrows(ScheduleConflictException.class, () -> new Section("B", DEFAULT_SUBJECT, MTH9to1030, room, newFaculty(1002)));
    }

}
