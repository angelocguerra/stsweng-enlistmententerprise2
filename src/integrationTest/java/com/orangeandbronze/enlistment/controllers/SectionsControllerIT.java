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
    }

    @Test
    void createSection_save_to_db() throws Exception {
        // Given that the section table in the db is empty and the following records are in subject, room, and admin
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", DEFAULT_ROOM_NAME, 20);
        jdbcTemplate.update("INSERT INTO admin (id, firstname, lastname) VALUES (?, ?, ?)", 17, "firstName", "lastName");

        // When a post request on path /sections is invoked  with the admin in session and with the following parameters
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
        );

        // Then the section table should contain a single record whose fields match the parameters
        final String query = "SELECT COUNT(*) FROM section WHERE section_id = ? AND subject_subject_id = ? AND days = ? AND start_time = ? AND end_time = ? AND room_name = ?";
        int actualCount = jdbcTemplate.queryForObject(
                query,
                Integer.class,
                DEFAULT_SECTION_ID,
                DEFAULT_SUBJECT_ID,
                MTH.ordinal(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                DEFAULT_ROOM_NAME
        );

        assertEquals(1, actualCount);
    }


}
