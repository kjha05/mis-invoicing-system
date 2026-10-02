package com.example.groupmanagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class GroupManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupManagementApplication.class, args);
    }
}

// ==================== 1. DATABASE ENTITY MODEL ====================
@Entity
@Table(name = "customer_group")
class GroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId; // Stored unique id of the group

    @Column(name = "group_name", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Group name cannot be blank")
    private String groupName; // Stores unique group name

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // True if active, False if inactive (Soft delete)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Date and time when created

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // Date and time when recently updated[cite: 1]

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

// ==================== 2. REPOSITORY INTERFACE ====================
@Repository
interface GroupRepository extends JpaRepository<GroupModel, Integer> {
    List<GroupModel> findByIsActiveTrue(); // Sirf active groups fetch karne ke liye (Soft delete support)[cite: 1]
    Optional<GroupModel> findByGroupNameIgnoreCase(String groupName); // Duplicate check ke liye[cite: 1]
}

// ==================== 3. CONTROLLER & UI LOGIC ====================
@Controller
class GroupController {

    private final GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    // Dashboard View[cite: 1]
    @GetMapping("/")
    public String dashboard(Model model) {
        List<GroupModel> activeGroups = groupRepository.findByIsActiveTrue();
        model.addAttribute("groups", activeGroups);
        model.addAttribute("totalGroups", activeGroups.size());
        return "dashboard";
    }

    // Add Group Form[cite: 1]
    @GetMapping("/add")
    public String addGroupForm() {
        return "add_group";
    }

    // Add Group Action with Validations[cite: 1]
    @PostMapping("/add")
    public String addGroup(@RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name khali nahi ho sakta!");
            return "redirect:/add";
        }

        if (groupRepository.findByGroupNameIgnoreCase(trimmedName).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Yeh group name pehle se maujood hai. Unique naam dalein!");
            return "redirect:/add";
        }

        GroupModel group = new GroupModel();
        group.setGroupName(trimmedName);
        group.setIsActive(true);
        groupRepository.save(group);

        redirectAttributes.addFlashAttribute("success", "Group safalpurvak add kar diya gaya hai!");
        return "redirect:/";
    }

    // Edit Group Form[cite: 1]
    @GetMapping("/edit/{id}")
    public String editGroupForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group nahi mila!");
            return "redirect:/";
        }
        model.addAttribute("group", groupOpt.get());
        return "edit_group";
    }

    // Edit Group Action[cite: 1]
    @PostMapping("/edit/{id}")
    public String editGroup(@PathVariable("id") Integer id, @RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name khali nahi ho sakta!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> existing = groupRepository.findByGroupNameIgnoreCase(trimmedName);
        if (existing.isPresent() && !existing.get().getGroupId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Yeh group name kisi aur group ke paas pehle se hai!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setGroupName(trimmedName);
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group safalpurvak update ho gaya hai!");
        }

        return "redirect:/";
    }

    // Soft Delete Action (is_active ko false karna)[cite: 1]
    @PostMapping("/delete/{id}")
    public String deleteGroup(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setIsActive(false); // Soft delete implementation[cite: 1]
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group ko deactivate (soft delete) kar diya gaya hai!");
        }
        return "redirect:/";
    }
}