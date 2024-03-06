package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.annotation.*;
import org.springframework.test.web.servlet.*;
import org.testcontainers.junit.jupiter.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.orangeandbronze.enlistment.controllers.UserAction.*;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest
class EnlistControllerIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;


    @Test
    void enlist_student_in_section() throws Exception {

    }


    @Test
    void cancel_student_in_section() throws Exception {

    }

    private final static int FIRST_STUDENT_ID = 11;
    private final static int NUMBER_OF_STUDENTS = 5;
    private final static int LAST_STUDENT_NUMBER = FIRST_STUDENT_ID + NUMBER_OF_STUDENTS - 1;

    private void insertManyStudents() {
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = FIRST_STUDENT_ID; i <= LAST_STUDENT_NUMBER; i++) {
            batchArgs.add(new Object[]{i, "firstname", "lastname"});
        }
        jdbcTemplate.batchUpdate("INSERT INTO student(student_number, firstname, lastname) VALUES (?, ?, ?)", batchArgs);
    }

    private void insertNewDefaultSectionWithCapacity(int capacity) {
        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, capacity);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        jdbcTemplate.update(
                "INSERT INTO section (section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id, version)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9, 0), LocalTime.of(10, 0), roomName, DEFAULT_SUBJECT_ID, 0);
    }

    private void assertNumberOfStudentsSuccessfullyEnlistedInDefaultSection(int expectedCount) {
        int numStudents = jdbcTemplate.queryForObject(
                "select count(*) from student_sections where sections_section_id = '" +
                        DEFAULT_SECTION_ID + "'", Integer.class);
        assertEquals(expectedCount, numStudents);
    }

    @Test
    void enlist_concurrent_separate_section_instances_representing_same_record_students_beyond_capacity() throws Exception {

    }


    @Test
    void enlist_concurrently_same_section_enough_capacity() throws Exception {

    }



    private void startEnlistmentThreads() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = FIRST_STUDENT_ID; i <= LAST_STUDENT_NUMBER; i++) {
            final int studentNo = i;
            new EnslistmentThread(studentRepository.findById(studentNo).orElseThrow(() ->
                    new NoSuchElementException("No student w/ student num " + studentNo + " found in DB.")),
                    latch, mockMvc).start();
        }
        latch.countDown();
        Thread.sleep(5000); // wait time to allow all the threads to finish
    }

    private static class EnslistmentThread extends Thread {
        private final Student student;
        private final CountDownLatch latch;
        private final MockMvc mockMvc;

        public EnslistmentThread(Student student, CountDownLatch latch, MockMvc mockMvc) {
            this.student = student;
            this.latch = latch;
            this.mockMvc = mockMvc;
        }

        @Override
        public void run() {
            try {
                latch.await(); // The thread keeps waiting till it is informed
                mockMvc.perform(post("/enlist").sessionAttr("student", student)
                        .param("sectionId", DEFAULT_SECTION_ID).param("userAction", ENLIST.name()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
