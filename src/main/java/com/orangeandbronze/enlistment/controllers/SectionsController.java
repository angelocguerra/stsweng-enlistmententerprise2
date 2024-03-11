package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import com.orangeandbronze.enlistment.domain.Period;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import static org.apache.commons.lang3.Validate.notNull;


import java.time.*;
import java.util.*;

@Transactional
@Controller
@RequestMapping("sections")
@SessionAttributes("admin")
class SectionsController {

    @Autowired
    private SubjectRepository subjectRepo;
    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private RoomRepository roomRepo;
    @Autowired
    private SectionRepository sectionRepo;


    @ModelAttribute("admin")
    public Admin admin(Integer id) {
        return adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
    }

    @GetMapping
    public String showPage(Model model, Integer id) {
        Admin admin = id == null ? (Admin) model.getAttribute("admin") :
                adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
        model.addAttribute("admin", admin);
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("rooms", roomRepo.findAll());
        model.addAttribute("sections", sectionRepo.findAll());
        return "sections";
    }

    @PostMapping
    public String createSection(@RequestParam String sectionId, @RequestParam String subjectId, @RequestParam Days days,
                                @RequestParam String start, @RequestParam String end, @RequestParam String roomName,
                                RedirectAttributes redirectAttrs) {
        notNull(sectionId);
        notNull(subjectId);
        notNull(days);
        notNull(start);
        notNull(end);
        notNull(roomName);

        Period period = new Period(LocalTime.parse(start), LocalTime.parse(end));
        Schedule schedule = new Schedule(days, period);

        sectionRepo.findById(sectionId).ifPresent(section -> {
            throw new IllegalArgumentException("Section ID " + sectionId + " already exists");
        });
        Room room = roomRepo.findById(roomName).orElseThrow(() -> new NoSuchElementException("RoomName " + roomName + " not found"));
        Subject subject = subjectRepo.findById(subjectId).orElseThrow(() -> new NoSuchElementException("SubjectId " + subjectId + " not found"));

        Section section = new Section(sectionId, subject, schedule, room);
        sectionRepo.save(section);

        redirectAttrs.addFlashAttribute("sectionSuccessMessage", "Successfully created new section" + sectionId);

        return "redirect:sections";
    }

    @ExceptionHandler(EnlistmentException.class)
    public String handleException(RedirectAttributes redirectAttrs, EnlistmentException e) {
        redirectAttrs.addFlashAttribute("sectionExceptionMessage", e.getMessage());
        return "redirect:sections";
    }

    void setSubjectRepo(SubjectRepository subjectRepo) {
        this.subjectRepo = subjectRepo;
    }

    void setSectionRepo(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    void setRoomRepo(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    void setAdminRepo(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }



}
