import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@SpringBootApplication
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}

@Entity
class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private LocalDateTime createdAt;
    private TaskStatus status;

    public void setId(Long id) { this.id = id; }
    public String getDescription() { return this.description; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public TaskStatus getStatus() { return this.status; }

    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStatus(TaskStatus status) { this.status = status; }
}

interface TaskRepository extends JpaRepository<Task, Long> {}

@RestController
class TaskManagerController {
    private final TaskRepository taskRepo;

    @Autowired
    public TaskManagerController(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;
    }

    // Add a new task
    @PostMapping("/add")
    public void addTask(@RequestBody Task task) {
        task.setCreatedAt(LocalDateTime.now());
        this.taskRepo.save(task);
    }

    // Get all tasks
    @GetMapping("/all")
    public List<Task> getAllTasks() {
        return this.taskRepo.findAll();
    }

    // Get all tasks by status
    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable TaskStatus status) {
        return this.taskRepo.findAll().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    // Update task status
    @PutMapping("/{id}")
    public void updateTaskStatus(@PathVariable Long id, @RequestBody Task updatedTask) {
        Optional<Task> optionalTask = this.taskRepo.findById(id);

        if (optionalTask.isPresent()) {  // Check if the task exists
            Task taskToUpdate = optionalTask.get();
            taskToUpdate.setStatus(updatedTask.getStatus());
            this.taskRepo.save(taskToUpdate);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such task exists."); // Throw a 404 error if the task doesn't exist
        }
    }

    // Delete a task by ID
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        Optional<Task> optionalTask = this.taskRepo.findById(id);

        if (optionalTask.isPresent()) {  // Check if the task exists
            Task taskToDelete = optionalTask.get();
            this.taskRepo.delete(taskToDelete);
            // this.taskRepo.save(taskToUpdate);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such task exists.");  // Throw a 404 error if the task doesn't exist
        }
    }
}

enum TaskStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED;
}