package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;

import javax.persistence.*;
import java.util.*;

import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class EnlistControllerTest {

    @Test
    void enlistOrCancel_enlist_student_in_section() {
        // Given the Controller w/ a student in session, argument is the sectionId that the student wants to enlist in, and UserAction "ENLIST"
        Student student = mock(Student.class);

        // When enlist (post) method is called
        SectionRepository sectionRepo = mock(SectionRepository.class);
        StudentRepository studentRepo = mock(StudentRepository.class);

        Section section = newDefaultSection();
        when(sectionRepo.findById(DEFAULT_SECTION_ID)).thenReturn(Optional.of(section));

        EnlistController controller = new EnlistController();
        controller.setSectionRepo(sectionRepo);
        controller.setStudentRepo(studentRepo);

        EntityManager entityManager = mock(EntityManager.class);
        Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        controller.setEntityManager(entityManager);

        // When the enlistOrCancel method is called
        String returnVal = controller.enlistOrCancel(student, DEFAULT_SECTION_ID, UserAction.ENLIST);
        // Then...
        assertAll(
                // sectionRepo will fetch the appropriate data associated with the sectionId and instantiate a section object
                () -> verify(sectionRepo).findById(DEFAULT_SECTION_ID),
                // reattach student object to Hibernate session
                () -> verify(session).update(student),
                // we call the enlist method of the student object and pass in the section
                () -> verify(student).enlist(section),
                // studentRepo (DB) will save the student info
                () -> verify(studentRepo).save(student),
                // sectionRepo (DB) will save the section info
                () -> verify(sectionRepo).save(section),
                // PRG pattern is implemented at the end
                () -> assertEquals("redirect:enlist", returnVal)
        );
    }


}
