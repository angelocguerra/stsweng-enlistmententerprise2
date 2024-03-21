package com.orangeandbronze.enlistment.domain;

import org.junit.jupiter.api.*;

import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void newSection_same_room_diff_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room, new Faculty(1000, "firstname", "lastname"));
        assertDoesNotThrow(() -> new Section("B", DEFAULT_SUBJECT, TF10to1130, room, new Faculty(1001, "firstname", "lastname")));
    }

    @Test
    void newSection_same_room_same_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room, new Faculty(1000, "firstname", "lastname"));
        assertThrows(ScheduleConflictException.class, () -> new Section("B", DEFAULT_SUBJECT, MTH830to10, room, new Faculty(1001, "firstname", "lastname")));
    }

}
