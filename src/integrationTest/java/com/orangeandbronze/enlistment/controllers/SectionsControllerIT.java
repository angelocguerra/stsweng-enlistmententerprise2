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

        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", DEFAULT_ROOM_NAME, 20);
        jdbcTemplate.update("INSERT INTO admin (id, firstname, lastname) VALUES (?, ?, ?)", 17, "firstName", "lastName");
        jdbcTemplate.update("INSERT INTO faculty (faculty_number) VALUES (?)", DEFAULT_FACULTY_NUMBER);

        // When a post request on path /sections is invoked  with the admin in session and with the following parameters

        //params
        Admin admin = adminRepository.findById(1).orElseThrow(() -> new RuntimeException(""));
        mockMvc.perform(
                post("/sections")
                        .sessionAttr("admin", admin)
                        .param("sectionId", DEFAULT_SECTION_ID)
                        .param("subjectId", DEFAULT_SUBJECT_ID)
                        .param("days", MTH.name())
                        .param("start", LocalTime.of(9, 0).toString())
                        .param("end", LocalTime.of(10, 0).toString())
                        .param("roomName", DEFAULT_ROOM_NAME)
                        .param("facultyNumber", String.valueOf(DEFAULT_FACULTY_NUMBER))
        );

        // Then the section table should contain a single record whose fields match the parameters
        final String query = "SELECT COUNT(*) FROM section WHERE section_id = ? AND subject_subject_id = ? AND days = ? AND start_time = ? AND end_time = ? AND room_name = ? AND instructor_faculty_number = ?";
        int actualCount = jdbcTemplate.queryForObject(
                query,
                Integer.class,
                DEFAULT_SECTION_ID,
                DEFAULT_SUBJECT_ID,
                MTH.ordinal(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                DEFAULT_ROOM_NAME,
                DEFAULT_FACULTY_NUMBER
        );

//        Map<String, Object> results = jdbcTemplate.queryForMap("SELECT * FROM section WHERE section_id = ?", DEFAULT_SECTION_ID);
//        System.out.println(results);
//        assertAll(
//                () -> assertEquals(DEFAULT_SECTION_ID, results.get("section_id")),
//                () -> assertEquals(DEFAULT_SUBJECT_ID, results.get("subject_subject_id")),
//                () -> assertEquals(MTH.ordinal(), results.get("days")),
//                () -> assertEquals(LocalTime.of(9, 0), LocalTime.parse(results.get("start_time").toString())),
//                () -> assertEquals(LocalTime.of(10, 0), LocalTime.parse(results.get("end_time").toString())),
//                () -> assertEquals(DEFAULT_ROOM_NAME, results.get("room_name")),
//                () -> assertEquals(String.valueOf(DEFAULT_FACULTY_NUMBER), results.get("instructor_faculty_number"))
//        );

        assertEquals(1, actualCount);
    }

    private final static int FIRST_ADMIN_ID = 5;
    private final static int NUMBER_OF_ADMIN = 5;
    private final static int LAST_ADMIN_NUMBER = FIRST_ADMIN_ID + NUMBER_OF_ADMIN - 1;


    private void insertManyAdmins() {
        List<Object[]> admins = new ArrayList<>();
        for (int i = FIRST_ADMIN_ID; i <= LAST_ADMIN_NUMBER; i++) {
            admins.add(new Object[]{i, "firstname", "lastname"});
        }
        jdbcTemplate.batchUpdate("INSERT INTO admin(id, firstname, lastname) VALUES (?, ?, ?)", admins);
    }

    private void insertNewDefaultSection() {
        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 20);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        jdbcTemplate.update(
                "INSERT INTO section (section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id, version)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9, 0), LocalTime.of(10, 0), roomName, DEFAULT_SUBJECT_ID, 0);
    }

    private void assertNumberOfSectionsCreatedByAdmin(int expectedCount, String sectionId) {
        int numSections = jdbcTemplate.queryForObject(
                "select count(*) from section where section_id = '" +
                        sectionId + "'", Integer.class);
        assertEquals(expectedCount, numSections);
    }

    @Test
    void admins_create_existing_section_concurrently() throws Exception {
        // Given 5 admins and a section
        insertManyAdmins();
        insertNewDefaultSection();
        // When the admins create existing section concurrently
        startCreateSectionThreads(DEFAULT_SECTION_ID);
        // Then no extra sections will be created
        assertNumberOfSectionsCreatedByAdmin(1, DEFAULT_SECTION_ID);
    }


//    @Test
//    void admins_create_new_section_concurrently() throws Exception {
//        // Given 5 admins and a section
//        final String sectionId = "SEC";
//        insertManyAdmins();
//        insertNewDefaultSection();
//        // When the admins create existing section concurrently
//        startCreateSectionThreads(sectionId);
//        // Then only one section will be created
//        assertNumberOfSectionsCreatedByAdmin(1, sectionId);
//    }

    private void startCreateSectionThreads(String sectionId) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = FIRST_ADMIN_ID; i <= LAST_ADMIN_NUMBER; i++) {
            final int adminId = i;
            new CreateSectionThread(adminRepository.findById(adminId).orElseThrow(() ->
                    new NoSuchElementException("No admin w/ admin id " + adminId + " found in DB.")),
                    latch, mockMvc, sectionId).start();
        }
        latch.countDown();
        Thread.sleep(5000); // wait time to allow all the threads to finish
    }

    private static class CreateSectionThread extends Thread {
        private final Admin admin;
        private final CountDownLatch latch;
        private final MockMvc mockMvc;
        private final String sectionId;

        public CreateSectionThread(Admin admin, CountDownLatch latch, MockMvc mockMvc, String sectionId) {
            this.admin = admin;
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
                        .param("facultyNumber", String.valueOf(DEFAULT_FACULTY_NUMBER)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}
