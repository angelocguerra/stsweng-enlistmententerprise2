package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.orangeandbronze.enlistment.controllers.UserAction.ENLIST;
import static com.orangeandbronze.enlistment.domain.Days.MTH;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest
class SectionsControllerIT  {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private final static String TEST = "test";

    @Container
    private final PostgreSQLContainer container = new PostgreSQLContainer("postgres:14").withDatabaseName(TEST).withUsername(TEST).withPassword(TEST);

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:14:///" + TEST);
        registry.add("spring.datasource.username", () -> TEST);
        registry.add("spring.datasource.password", () -> TEST);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Test
    void createSection_save_to_db() throws Exception {

        // Given that the section table in the db is empty and the following records are in subject, room, and admin
        final String sectionId = "sectionId";
        final String subjectId = "subjectId";
        final Days days = MTH;
        final String start = "09:00";
        final String end = "10:00";
        final String roomName = "roomName";

        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", subjectId);
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 20);
        jdbcTemplate.update("INSERT INTO admin (id, firstname, lastname) VALUES (?, ?, ?)", 17, "firstName", "lastName");
        jdbcTemplate.update("INSERT INTO faculty (faculty_number, firstname, lastname )VALUES (?, ?, ?)", DEFAULT_FACULTY_NUMBER, "firstname", "lastname");

        // When a post request on path /sections is invoked  with the admin in session and with the following parameters
        mockMvc.perform(
                post("/sections")
                        .sessionAttr("admin", mock(Admin.class))
                        .param("sectionId", sectionId)
                        .param("subjectId", subjectId)
                        .param("days", days.name())
                        .param("start", start)
                        .param("end", end)
                        .param("roomName", roomName)
                        .param("facultyNumber", String.valueOf(DEFAULT_FACULTY_NUMBER))
        );

        // Then the section table should contain a single record whose fields match the parameters
        Map<String, Object> results = jdbcTemplate.queryForMap("SELECT * FROM section WHERE section_id = ?", sectionId);
        assertAll(
                () -> assertEquals(sectionId, results.get("section_id")),
                () -> assertEquals(subjectId, results.get("subject_subject_id")),
                () -> assertEquals(days.ordinal(), results.get("days")),
                () -> assertEquals(LocalTime.parse(start), LocalTime.parse(results.get("start_time").toString())),
                () -> assertEquals(LocalTime.parse(end), LocalTime.parse(results.get("end_time").toString())),
                () -> assertEquals(roomName, results.get("room_name")),
                () -> assertEquals(DEFAULT_FACULTY_NUMBER, results.get("instructor_faculty_number"))
        );
    }

    private final static int FIRST_ADMIN_ID = 1;
    private final static int NUMBER_OF_ADMIN = 3;
    private final static int LAST_ADMIN_NUMBER = FIRST_ADMIN_ID + NUMBER_OF_ADMIN - 1;

    @Test
    void concurrently_create_overlapping_section() throws Exception {
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", "roomName", 20);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        startCreateSectionThreads(); // start multi threads
        assertNumberOfSectionsCreated(3);    //check if multi threading was allowed by checking the number of sections created
    }

    private void assertNumberOfSectionsCreated(int expectedCount) {
        int numSections = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM room_sections WHERE room_name = 'roomName'", Integer.class);
        assertEquals(expectedCount, numSections);
    }

    private void startCreateSectionThreads() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = FIRST_ADMIN_ID; i <= LAST_ADMIN_NUMBER; i++) {
            final int id = i;
            new CreateSectionThread(
                    adminRepository.findById(id).orElseThrow(() ->
                            new NoSuchElementException("No such admin w/ id: " + id + " found in DB.")),
                    facultyRepository.findById(id).orElseThrow(() ->
                            new NoSuchElementException("No such faculty w/ id: " + id + " found in DB.")),
                    latch, mockMvc,Integer.toString(i)).start();
        }
        latch.countDown();
        Thread.sleep(5000); // wait time to allow all the threads to finish
    }

    private static class CreateSectionThread extends Thread {
        private final Admin admin;
        private final Faculty faculty;
        private final CountDownLatch latch;
        private final MockMvc mockMvc;
        private final String sectionId;

        public CreateSectionThread(Admin admin, Faculty faculty, CountDownLatch latch, MockMvc mockMvc, String sectionId) {
            this.admin = admin;
            this.faculty = faculty;
            this.latch = latch;
            this.mockMvc = mockMvc;
            this.sectionId = sectionId;
        }

        @Override
        public void run() {
            try {
                latch.await(); // The thread keeps waiting till it is informed
                mockMvc.perform(post("/sections").sessionAttr("admin", admin)
                        .param("sectionId", sectionId)
                        .param("subjectId", DEFAULT_SUBJECT_ID)
                        .param("days", "WS")
                        .param("start", "10:00").param("end", "11:30")
                        .param("roomName", "roomName")
                        .param("facultyNumber", String.valueOf(faculty.getFacultyNumber())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}
