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

@Entity
@Table(name = "customer_group")
class GroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "group_name", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Group name cannot be blank")
    private String groupName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

@Repository
interface GroupRepository extends JpaRepository<GroupModel, Integer> {
    List<GroupModel> findByIsActiveTrue();
    Optional<GroupModel> findByGroupNameIgnoreCase(String groupName);
}

@Controller
class GroupController {
    private final GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<GroupModel> activeGroups = groupRepository.findByIsActiveTrue();
        model.addAttribute("groups", activeGroups);
        model.addAttribute("totalGroups", activeGroups.size());
        return "dashboard";
    }

    @GetMapping("/add")
    public String addGroupForm() {
        return "add_group";
    }

    @PostMapping("/add")
    public String addGroup(@RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name cannot be blank!");
            return "redirect:/add";
        }

        if (groupRepository.findByGroupNameIgnoreCase(trimmedName).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "This group name already exists. Use a unique name!");
            return "redirect:/add";
        }

        GroupModel group = new GroupModel();
        group.setGroupName(trimmedName);
        group.setIsActive(true);
        groupRepository.save(group);

        redirectAttributes.addFlashAttribute("success", "Group added successfully!");
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editGroupForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found!");
            return "redirect:/";
        }
        model.addAttribute("group", groupOpt.get());
        return "edit_group";
    }

    @PostMapping("/edit/{id}")
    public String editGroup(@PathVariable("id") Integer id, @RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name cannot be blank!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> existing = groupRepository.findByGroupNameIgnoreCase(trimmedName);
        if (existing.isPresent() && !existing.get().getGroupId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "This group name is already assigned to another group!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setGroupName(trimmedName);
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group updated successfully!");
        }

        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteGroup(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setIsActive(false);
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group deactivated successfully!");
        }
        return "redirect:/";
    }
}
