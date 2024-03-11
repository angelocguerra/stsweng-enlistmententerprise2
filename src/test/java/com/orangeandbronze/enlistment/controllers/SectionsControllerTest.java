package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.servlet.mvc.support.*;

import java.util.*;

import static com.orangeandbronze.enlistment.domain.Days.MTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;

class SectionsControllerTest {

    @Test
    void createSection_save_new_section_to_repository() {
        // Given the controller, repositories & valid parameter arguments for creating a section
        SectionsController controller = new SectionsController();

        SectionRepository sectionRepo = mock(SectionRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);
        RoomRepository roomRepo = mock(RoomRepository.class);
        RedirectAttributes redirectAttrs = mock(RedirectAttributes.class);

        // When the controller receives the arguments

        Section section = newDefaultSection();
        when(sectionRepo.save(section)).thenReturn(section);
        controller.setSectionRepo(sectionRepo);

        when(subjectRepo.findById(DEFAULT_SUBJECT_ID)).thenReturn(Optional.of(DEFAULT_SUBJECT));
        controller.setSubjectRepo(subjectRepo);

        Room room = newDefaultRoom();
        when(roomRepo.findById(DEFAULT_ROOM_NAME)).thenReturn(Optional.of(room));
        controller.setRoomRepo(roomRepo);

        // Then
        // - it should retrieve the entities from the db, create a new section
        String returnVal = controller.createSection(DEFAULT_SECTION_ID, DEFAULT_SUBJECT_ID, MTH, "08:30", "10:00", DEFAULT_ROOM_NAME, redirectAttrs);

        // - save the section in the db
        verify(subjectRepo).findById(DEFAULT_SUBJECT_ID);
        verify(roomRepo).findById(DEFAULT_ROOM_NAME);

        verify(sectionRepo).save(section);

        // - set a flash attribute called "sectionSuccessMessage" with the message "Successfully created new section " + sectionId
        verify(redirectAttrs).addFlashAttribute("sectionSuccessMessage", "Successfully created new section" + DEFAULT_SECTION_ID);

        // - return the string value "redirect:sections" to redirect to the GET method
        assertEquals("redirect:sections", returnVal);

    }
}
