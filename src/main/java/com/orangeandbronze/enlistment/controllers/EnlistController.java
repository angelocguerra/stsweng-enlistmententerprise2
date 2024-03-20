package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.orm.*;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.view.*;

import javax.persistence.*;
import javax.transaction.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.apache.commons.lang3.Validate.notNull;


@Transactional
@Controller
@RequestMapping("enlist")
@SessionAttributes("student")

/**
 * Controller class that is responsible for handling enlistment-related actions.
 */
class EnlistController {

    @Autowired
    private SectionRepository sectionRepo;
    @Autowired
    private StudentRepository studentRepo;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Initializes the student model attribute.
     *
     * @param   model           the model to be initialized
     * @param   studentNumber   the student number
     */
    @ModelAttribute
    public void initStudent(Model model, Integer studentNumber) {
        Student student = (Student) model.getAttribute("student");
        if (studentNumber == null && student == null) {
            throw new LoginException("both studentNumber & student are null");
        }
        if (studentNumber != null && (studentNumber < 1 || studentNumber > 3)) {
            throw new LoginException("studentNumber out of range, was: " + studentNumber);
        }
        if (studentNumber != null) {
            student = studentRepo.findById(studentNumber).orElseThrow(() -> new NoSuchElementException("No student for studentNumber " + studentNumber));
            model.addAttribute(student);
        }
        model.addAttribute("isRetry", false);

    }

    /**
     * Handles LoginException by redirecting to the login page.
     *
     * @return a RedirectView object pointing to the login page
     */
    @ExceptionHandler(LoginException.class)
    public RedirectView home() {
        return new RedirectView("login.html");
    }


    /**
     * Displays the available sections for enlistment.
     *
     * @param model     the model containing enlisted and available sections
     * @param student   the student object
     * @return the view name for enlistment page
     */
    @GetMapping
    public String showSections(Model model, @ModelAttribute Student student) {
        var enlistedSections = student.getSections();
        model.addAttribute("enlistedSections", enlistedSections);
        model.addAttribute("availableSections", sectionRepo.findAll().stream()
                .filter(sec -> !enlistedSections.contains(sec)).collect(Collectors.toList()));
        return "enlist";
    }

    /**
     * Handles enlistment or cancellation of a student for a section.
     *
     * @param student    the student object
     * @param sectionId  the ID of the section to enlist or cancel
     * @param userAction the action to perform (ENLIST or CANCEL)
     * @return the redirect URL to the enlistment page
     */
    @Retryable(ObjectOptimisticLockingFailureException.class)
    @PostMapping
    public String enlistOrCancel(@ModelAttribute Student student, @RequestParam String sectionId, @RequestParam UserAction userAction) {
        // Error Checking

        Section section = sectionRepo.findById(sectionId).orElseThrow(() -> new NoSuchElementException("No section for sectionId " + sectionId));
        section.checkIfFull();
        
        // Connect app and db
        Session session = entityManager.unwrap(Session.class);
        notNull(session);

        // Update the student state
        session.update(student);
        userAction.act(student, section);
        // Save to the db
        studentRepo.save(student);
        sectionRepo.save(section);

        // Redirect to enlistment page
        return "redirect:enlist";
    }


    /**
     * Handles EnlistmentException by redirecting to the enlistment page and displaying the error message.
     *
     * @param redirectAttrs the redirect attributes
     * @param e             the EnlistmentException
     * @return the redirect URL to the enlistment page
     */
    @ExceptionHandler(EnlistmentException.class)
    public String handleException(RedirectAttributes redirectAttrs, EnlistmentException e) {
        redirectAttrs.addFlashAttribute("enlistmentExceptionMessage", e.getMessage());
        return "redirect:enlist";
    }


    /**
     * Setter method for sectionRepo dependency injection.
     *
     * @param sectionRepo the SectionRepository object
     */
    void setSectionRepo(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    /**
     * Setter method for studentRepo dependency injection.
     *
     * @param studentRepo the StudentRepository object
     */
    void setStudentRepo(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    /**
     * Setter method for entityManager dependency injection.
     *
     * @param entityManager the EntityManager object
     */
    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}

/**
 * Enum representing user actions (ENLIST or CANCEL) for enlistment.
 */
enum UserAction {
    ENLIST(Student::enlist),
    CANCEL(Student::cancel);

    private final BiConsumer<Student, Section> action;

    /**
     * Constructor for UserAction enum.
     *
     * @param action the action to be performed
     */
    UserAction(BiConsumer<Student, Section> action) {
        this.action = action;
    }

    /**
     * Performs the action for a given student and section.
     *
     * @param student the student object
     * @param section the section object
     */
    void act(Student student, Section section) {
        action.accept(student, section);
    }

}
