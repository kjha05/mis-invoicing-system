package com.codeb.ims;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
    import jakarta.persistence.Column;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
    import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.server.ResponseStatusException;
    import org.springframework.http.HttpStatus;
    import org.springframework.mail.SimpleMailMessage;
    import org.springframework.mail.MailException;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
    import java.util.Locale;
    import java.util.Optional;
    import java.security.MessageDigest;
    import java.security.NoSuchAlgorithmException;
    import java.security.SecureRandom;
    import java.nio.charset.StandardCharsets;
    import java.util.Base64;
    import java.util.HexFormat;

@SpringBootApplication
public class ImsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImsApplication.class, args);
    }
}

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
        @Column(unique = true, nullable = false)
    private String username;
        private String email;
    private String password;
    private String role;
        private boolean enabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

@Entity
@Table(name = "clients")
class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}

@Entity
@Table(name = "hierarchy")
class Hierarchy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String groupName;
    private String chainName;
    private String brandName;
    private String subZone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getChainName() { return chainName; }
    public void setChainName(String chainName) { this.chainName = chainName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getSubZone() { return subZone; }
    public void setSubZone(String subZone) { this.subZone = subZone; }
}

@Entity
@Table(name = "customer_group")
class CustomerGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, unique = true, length = 255)
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
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

@Entity
@Table(name = "invoices")
class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientName;
    private double amount;
        private double gstRate;
        private double gstAmount;
        private double totalAmount;
            private String gstType;
    private String status;
        private String paymentMethod;
        private String paymentReference;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
        public double getGstRate() { return gstRate; }
        public void setGstRate(double gstRate) { this.gstRate = gstRate; }
        public double getGstAmount() { return gstAmount; }
        public void setGstAmount(double gstAmount) { this.gstAmount = gstAmount; }
        public double getTotalAmount() { return totalAmount == 0 ? amount + gstAmount : totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
            public String getGstType() { return gstType; }
            public void setGstType(String gstType) { this.gstType = gstType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}

@Entity
@Table(name = "estimates")
class Estimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientName;
    private double amount;
        private double gstRate;
        private double gstAmount;
        private double totalAmount;
        private String gstType;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
        public double getGstRate() { return gstRate; }
        public void setGstRate(double gstRate) { this.gstRate = gstRate; }
        public double getGstAmount() { return gstAmount; }
        public void setGstAmount(double gstAmount) { this.gstAmount = gstAmount; }
        public double getTotalAmount() { return totalAmount == 0 ? amount + gstAmount : totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public String getGstType() { return gstType; }
        public void setGstType(String gstType) { this.gstType = gstType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String tokenHash;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private long expiresAt;

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}

interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
	    User findByEmailIgnoreCase(String email);
	    boolean existsByEmailIgnoreCase(String email);
        long countByEnabledTrue();
}

interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
	    void deleteByEmailIgnoreCase(String email);
}

interface ClientRepository extends JpaRepository<Client, Long> {}

interface HierarchyRepository extends JpaRepository<Hierarchy, Long> {}

interface GroupRepository extends JpaRepository<CustomerGroup, Long> {
    List<CustomerGroup> findByIsActiveTrue();
    Optional<CustomerGroup> findByGroupNameIgnoreCase(String groupName);
}

interface InvoiceRepository extends JpaRepository<Invoice, Long> {}

interface EstimateRepository extends JpaRepository<Estimate, Long> {}

@Service
class ImsService {
    private final ClientRepository clientRepository;
    private final HierarchyRepository hierarchyRepository;
    private final GroupRepository groupRepository;
    private final InvoiceRepository invoiceRepository;
    private final EstimateRepository estimateRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

    public ImsService(ClientRepository clientRepository,
                      HierarchyRepository hierarchyRepository,
                      GroupRepository groupRepository,
                      InvoiceRepository invoiceRepository,
                          EstimateRepository estimateRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.hierarchyRepository = hierarchyRepository;
        this.groupRepository = groupRepository;
        this.invoiceRepository = invoiceRepository;
        this.estimateRepository = estimateRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
    }

    public List<Client> getAllClients() { return clientRepository.findAll(); }
    public void saveClient(Client client) { clientRepository.save(client); }
        public void updateClient(Long id, Client updated) {
            Client client = clientRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            client.setName(updated.getName());
            client.setEmail(updated.getEmail());
            client.setPhone(updated.getPhone());
            client.setCompany(updated.getCompany());
            clientRepository.save(client);
        }

    public List<Hierarchy> getAllHierarchy() { return hierarchyRepository.findAll(); }
    public void saveHierarchy(Hierarchy hierarchy) { hierarchyRepository.save(hierarchy); }

    public List<CustomerGroup> getAllGroups() { return groupRepository.findByIsActiveTrue(); }
    public void saveGroup(CustomerGroup group) { groupRepository.save(group); }
    public void updateGroup(Long id, String name) {
        CustomerGroup group = groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        group.setGroupName(name.trim());
        groupRepository.save(group);
    }
    public void deleteGroup(Long id) {
        CustomerGroup group = groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        group.setIsActive(false);
        groupRepository.save(group);
    }

    public List<Invoice> getAllInvoices() { return invoiceRepository.findAll(); }
    public void saveInvoice(Invoice invoice) { invoiceRepository.save(invoice); }

    public List<Estimate> getAllEstimates() { return estimateRepository.findAll(); }
    public void saveEstimate(Estimate estimate) { estimateRepository.save(estimate); }

        public List<User> getAllUsers() { return userRepository.findAll(); }
        public long getActiveUserCount() { return userRepository.countByEnabledTrue(); }
	    public void createUser(String username, String email, String password, String role) {
                if (userRepository.findByUsername(username) != null || userRepository.existsByEmailIgnoreCase(email)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
            }
            User user = new User();
            user.setUsername(username);
                user.setEmail(email.trim().toLowerCase(Locale.ROOT));
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(normalizeRole(role));
            user.setEnabled(true);
            userRepository.save(user);
        }

        private static String normalizeRole(String role) {
            if (role == null || role.isBlank()) return "EMPLOYEE";
            String normalized = role.trim();
            if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
            normalized = normalized.toUpperCase(Locale.ROOT);
            return normalized.equals("ADMIN") || normalized.equals("EMPLOYEE") ? normalized : "EMPLOYEE";
        }
        public void toggleUser(Long id) {
            User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        }
        public void updateInvoicePayment(Long id, String status, String method, String reference) {
            Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            invoice.setStatus(status);
            invoice.setPaymentMethod(method);
            invoice.setPaymentReference(reference);
            invoiceRepository.save(invoice);
        }
}

@Service
class AccountService {
    private static final System.Logger LOGGER = System.getLogger(AccountService.class.getName());
    private static final long RESET_TOKEN_LIFETIME_MILLIS = 30 * 60 * 1000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String appUrl;
    private final String fromAddress;

    AccountService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                   PasswordEncoder passwordEncoder, JavaMailSender mailSender,
                   @Value("${ims.app-url:http://localhost:8081}") String appUrl,
                   @Value("${ims.mail.from:noreply@localhost}") String fromAddress) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.appUrl = appUrl;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public void register(String username, String email, String password) {
        if (userRepository.findByUsername(username) != null || userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("EMPLOYEE");
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String submittedEmail) {
        String email = submittedEmail.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null || !user.isEnabled()) return;

        tokenRepository.deleteByEmailIgnoreCase(email);
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setTokenHash(hashToken(token));
        resetToken.setExpiresAt(System.currentTimeMillis() + RESET_TOKEN_LIFETIME_MILLIS);
        tokenRepository.save(resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Reset your Code-B IMS password");
        message.setText("Use this one-time link within 30 minutes to reset your password:\n\n"
                + appUrl + "/reset-password?token=" + token + "\n\nIf you did not request this, ignore this email.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            tokenRepository.deleteByEmailIgnoreCase(email);
            LOGGER.log(System.Logger.Level.ERROR, "Password reset email could not be sent", exception);
        }
    }

    public boolean isResetTokenValid(String token) {
        return findValidToken(token).isPresent();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> match = findValidToken(token);
        if (match.isEmpty()) return false;
        String email = match.get().getEmail();
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null || !user.isEnabled()) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByEmailIgnoreCase(email);
        return true;
    }

    private Optional<PasswordResetToken> findValidToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return tokenRepository.findByTokenHash(hashToken(token))
                .filter(resetToken -> resetToken.getExpiresAt() > System.currentTimeMillis());
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}

    @Configuration
    @EnableWebSecurity
    class SecurityConfiguration {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login", "/error").permitAll()
                                                    .requestMatchers("/register", "/forgot-password", "/reset-password").permitAll()
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated())
                    .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                    .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                    .build();
        }

        @Bean
        UserDetailsService userDetailsService(UserRepository repository) {
            return username -> {
                User account = repository.findByUsername(username);
                    if (account == null) account = repository.findByEmailIgnoreCase(username);
                if (account == null) {
                    throw new UsernameNotFoundException("User not found");
                }
                String role = normalizeRole(account.getRole());
                if (account.getRole() == null || account.getRole().isBlank()) {
                    account.setRole(role);
                    repository.save(account);
                }
                return org.springframework.security.core.userdetails.User.withUsername(account.getUsername())
                        .password(account.getPassword())
                        .roles(role)
                        .disabled(!account.isEnabled())
                        .build();
            };
        }

        private static String normalizeRole(String role) {
            if (role == null || role.isBlank()) return "EMPLOYEE";
            String normalized = role.trim();
            if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
            normalized = normalized.toUpperCase(Locale.ROOT);
            return normalized.equals("ADMIN") || normalized.equals("EMPLOYEE") ? normalized : "EMPLOYEE";
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        CommandLineRunner createInitialAdmin(UserRepository repository, PasswordEncoder encoder,
                                             @Value("${ims.admin.username:admin}") String username,
                                                 @Value("${ims.admin.password:ChangeMe123!}") String password,
                                                 @Value("${ims.admin.email:}") String adminEmail) {
            return args -> {
                if (repository.count() == 0) {
                    User admin = new User();
                    admin.setUsername(username);
                    admin.setEmail(adminEmail.isBlank() ? null : adminEmail.trim().toLowerCase(Locale.ROOT));
                    admin.setPassword(encoder.encode(password));
                    admin.setRole("ADMIN");
                    admin.setEnabled(true);
                    repository.save(admin);
                } else if (!adminEmail.isBlank()) {
                    User admin = repository.findByUsername(username);
                    if (admin != null && (admin.getEmail() == null || admin.getEmail().isBlank())) {
                        admin.setEmail(adminEmail.trim().toLowerCase(Locale.ROOT));
                        repository.save(admin);
                    }
                }
            };
        }
    }

@Controller
class ImsController {
    private final ImsService imsService;
        private final AccountService accountService;

        public ImsController(ImsService imsService, AccountService accountService) {
        this.imsService = imsService;
            this.accountService = accountService;
    }

    @GetMapping({"/", "/personal"})
        @ResponseBody
        public String renderDashboard(Authentication authentication, CsrfToken csrf) {
            List<Client> clients = imsService.getAllClients();
            List<Hierarchy> hierarchy = imsService.getAllHierarchy();
            List<Invoice> invoices = imsService.getAllInvoices();
            List<Estimate> estimates = imsService.getAllEstimates();
            boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                double billing = 0;
                for (Invoice invoice : invoices) billing += invoice.getTotalAmount();
            StringBuilder html = new StringBuilder("""
                    <!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                    <title>Code-B IMS</title><style>
                    :root{color-scheme:light;--ink:#17211d;--muted:#65736c;--line:#dce4de;--paper:#f3f6f2;--green:#176b4b;--lime:#d8f36a;--white:#fff}
                    *{box-sizing:border-box}body{margin:0;background:var(--paper);color:var(--ink);font:15px/1.5 "Segoe UI",sans-serif}
                    header{background:var(--ink);color:white;padding:20px max(24px,calc((100% - 1240px)/2));display:flex;justify-content:space-between;align-items:center;gap:18px}
                    header h1{font-size:21px;margin:0}header small{color:#b9c8bf}header form{margin:0}
                    main{max-width:1240px;margin:28px auto;padding:0 22px}.stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:28px}
                    .stat{background:var(--white);border:1px solid var(--line);padding:16px 18px}.stat label{display:block;color:var(--muted);font-size:13px}.stat strong{font-size:24px}
                    section{margin:30px 0}h2{font-size:18px;margin:0 0 12px}form.entry{display:flex;flex-wrap:wrap;gap:8px;margin:0 0 12px}
                    input,select,button{font:inherit;padding:9px 11px;border:1px solid #c7d2ca;border-radius:4px;background:white;color:var(--ink)}input,select{min-width:135px;flex:1}
                    button{background:var(--green);border-color:var(--green);color:white;cursor:pointer;font-weight:600}button:hover{filter:brightness(1.1)}button.secondary{background:#fff;color:var(--ink);border-color:var(--line)}
                    .table-wrap{overflow:auto;background:white;border:1px solid var(--line)}table{width:100%;border-collapse:collapse;min-width:700px}th,td{text-align:left;padding:10px 12px;border-bottom:1px solid var(--line);vertical-align:top}th{font-size:12px;text-transform:uppercase;color:var(--muted);background:#f8faf8}td form{margin:0}td input,td select{min-width:100px;padding:6px}.muted{color:var(--muted)}.admin{border-top:3px solid var(--lime);padding-top:20px}
                    @media(max-width:680px){header{align-items:flex-start;padding:18px;flex-direction:column}main{margin:18px auto;padding:0 14px}.stats{grid-template-columns:1fr}.stat{padding:12px 15px}}
                    </style></head><body>
                    """);
            html.append("<header><div><h1>Code-B Internal Management System</h1><small>Signed in as ")
                    .append(escape(authentication.getName())).append(admin ? " · Admin" : " · Employee")
                    .append("</small></div><form method='post' action='/logout'>").append(csrfField(csrf))
                    .append("<button class='secondary' type='submit'>Sign out</button></form></header><main>")
                    .append("<div class='stats'><div class='stat'><label>Total clients</label><strong>").append(clients.size())
                    .append("</strong></div><div class='stat'><label>Active users</label><strong>").append(imsService.getActiveUserCount())
                    .append("</strong></div><div class='stat'><label>Total billing incl. GST</label><strong>INR ").append(money(billing)).append("</strong></div></div>");

            html.append("<section><h2>Clients</h2><form class='entry' action='/add-client' method='post'>").append(csrfField(csrf))
                    .append("<input name='name' placeholder='Client name' required><input type='email' name='email' placeholder='Email' required><input name='phone' placeholder='Phone'><input name='company' placeholder='Company'><button>Add client</button></form>")
                        ;
                for (Client client : clients) {
                    String formId = "client-form-" + client.getId();
                    html.append("<form id='").append(formId).append("' method='post' action='/clients/").append(client.getId()).append("'>").append(csrfField(csrf)).append("</form>");
                }
                html.append("<div class='table-wrap'><table><tr><th>Name</th><th>Email</th><th>Phone</th><th>Company</th><th>Update</th></tr>");
            for (Client client : clients) {
                String formId = "client-form-" + client.getId();
                    html.append("<tr><td><input form='").append(formId).append("' name='name' value='").append(escape(client.getName())).append("' required></td><td><input form='").append(formId).append("' type='email' name='email' value='")
                        .append(escape(client.getEmail())).append("' required></td><td><input form='").append(formId).append("' name='phone' value='").append(escape(client.getPhone()))
                        .append("'></td><td><input form='").append(formId).append("' name='company' value='").append(escape(client.getCompany())).append("'></td><td><button form='").append(formId).append("' type='submit'>Save</button></td></tr>");
            }
            html.append("</table></div></section><section><h2>Business structure</h2><form class='entry' action='/add-hierarchy' method='post'>").append(csrfField(csrf))
                    .append("<input name='groupName' placeholder='Group' required><input name='chainName' placeholder='Chain'><input name='brandName' placeholder='Brand'><input name='subZone' placeholder='Subzone'><button>Add structure</button></form>")
                    .append("<div class='table-wrap'><table><tr><th>Group</th><th>Chain</th><th>Brand</th><th>Subzone</th></tr>");
            for (Hierarchy item : hierarchy) {
                html.append("<tr><td>").append(escape(item.getGroupName())).append("</td><td>").append(escape(item.getChainName()))
                        .append("</td><td>").append(escape(item.getBrandName())).append("</td><td>").append(escape(item.getSubZone())).append("</td></tr>");
            }
            html.append("</table></div></section>");
            html.append("<section><h2>Invoices</h2><form class='entry' action='/add-invoice' method='post'>").append(csrfField(csrf))
                    .append("<input name='clientName' placeholder='Client name' required><input type='number' min='0.01' step='0.01' name='amount' placeholder='Taxable amount (INR)' required>")
                    .append(gstOptions()).append("<select name='status'><option>Pending</option><option>Paid</option><option>Partial</option></select><input name='paymentMethod' placeholder='Payment method'><input name='paymentReference' placeholder='Payment reference'><button>Create invoice</button></form>")
                    .append("<div class='table-wrap'><table><tr><th>Client</th><th>Taxable</th><th>GST</th><th>Total</th><th>Payment</th>");
            if (admin) html.append("<th>Update payment</th>");
            html.append("</tr>");
            for (Invoice invoice : invoices) {
                html.append("<tr><td>").append(escape(invoice.getClientName())).append("</td><td>INR ").append(money(invoice.getAmount()))
                        .append("</td><td>").append(taxDisplay(invoice.getGstRate(), invoice.getGstAmount(), invoice.getGstType()))
                        .append("</td><td>INR ").append(money(invoice.getTotalAmount())).append("</td><td>").append(escape(invoice.getStatus()))
                        .append("<br><span class='muted'>").append(escape(invoice.getPaymentMethod())).append(" ").append(escape(invoice.getPaymentReference())).append("</span></td>");
                if (admin) html.append("<td><form method='post' action='/admin/invoices/").append(invoice.getId()).append("/payment'>").append(csrfField(csrf))
                        .append("<select name='status'><option").append("Paid".equals(invoice.getStatus()) ? " selected" : "").append(">Paid</option><option")
                        .append("Pending".equals(invoice.getStatus()) ? " selected" : "").append(">Pending</option><option")
                        .append("Partial".equals(invoice.getStatus()) ? " selected" : "").append(">Partial</option></select><input name='paymentMethod' placeholder='Method' value='")
                        .append(escape(invoice.getPaymentMethod())).append("'><input name='paymentReference' placeholder='Reference' value='").append(escape(invoice.getPaymentReference()))
                        .append("'><button>Save</button></form></td>");
                html.append("</tr>");
            }
            html.append("</table></div></section><section><h2>Estimates</h2><form class='entry' action='/add-estimate' method='post'>").append(csrfField(csrf))
                    .append("<input name='clientName' placeholder='Client name' required><input type='number' min='0.01' step='0.01' name='amount' placeholder='Taxable amount (INR)' required>")
                    .append(gstOptions()).append("<select name='status'><option>Draft</option><option>Approved</option><option>Rejected</option></select><button>Create estimate</button></form>")
                    .append("<div class='table-wrap'><table><tr><th>Client</th><th>Taxable</th><th>GST</th><th>Total</th><th>Status</th></tr>");
            for (Estimate estimate : estimates) {
                html.append("<tr><td>").append(escape(estimate.getClientName())).append("</td><td>INR ").append(money(estimate.getAmount()))
                        .append("</td><td>").append(taxDisplay(estimate.getGstRate(), estimate.getGstAmount(), estimate.getGstType()))
                        .append("</td><td>INR ").append(money(estimate.getTotalAmount())).append("</td><td>").append(escape(estimate.getStatus())).append("</td></tr>");
            }
            html.append("</table></div></section>");
            if (admin) appendAdminUsers(html, csrf, imsService.getAllUsers());
            return html.append("</main></body></html>").toString();
        }

        @GetMapping("/groups")
        @ResponseBody
        public String groupsPage(Authentication authentication, CsrfToken csrf) {
            List<CustomerGroup> groups = imsService.getAllGroups();
            StringBuilder html = new StringBuilder("<!doctype html><html lang='en'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Groups · Code-B IMS</title><style>body{margin:0;background:#f3f6f2;color:#17211d;font:15px 'Segoe UI',sans-serif}main{max-width:900px;margin:40px auto;padding:0 20px}h1{margin:0 0 20px}form{display:flex;flex-wrap:wrap;gap:8px;margin:0 0 18px}input,button{font:inherit;padding:10px 12px;border:1px solid #c7d2ca;border-radius:4px}input{flex:1;min-width:180px}button{background:#176b4b;border-color:#176b4b;color:#fff;cursor:pointer}a{color:#176b4b;text-decoration:none}table{width:100%;border-collapse:collapse;background:white;border:1px solid #dce4de}th,td{text-align:left;padding:10px 12px;border-bottom:1px solid #dce4de}button.secondary{background:white;color:#17211d}.alert{padding:10px 12px;margin:10px 0;border-radius:4px} .success{background:#dff0d8;color:#3c763d}.error{background:#f2dede;color:#a94442}</style></head><body><main>");
            html.append("<h1>Customer groups</h1>");
            html.append("<p><a href='/'>Back to dashboard</a></p>")
                    .append("<form action='/groups/add' method='post'>").append(csrfField(csrf))
                    .append("<input name='groupName' placeholder='Enter group name' required>")
                    .append("<button type='submit'>Add group</button></form>");
            for (CustomerGroup group : groups) {
                html.append("<form action='/groups/").append(group.getGroupId()).append("/edit' method='post'>").append(csrfField(csrf))
                        .append("<input name='groupName' value='").append(escape(group.getGroupName())).append("' required>")
                        .append("<button type='submit'>Save</button>")
                        .append("</form>");
            }
            html.append("<table><tr><th>ID</th><th>Name</th><th>Created</th><th>Updated</th><th>Action</th></tr>");
            for (CustomerGroup group : groups) {
                html.append("<tr><td>").append(group.getGroupId()).append("</td><td>").append(escape(group.getGroupName())).append("</td><td>")
                        .append(group.getCreatedAt() == null ? "-" : group.getCreatedAt().toString())
                        .append("</td><td>").append(group.getUpdatedAt() == null ? "-" : group.getUpdatedAt().toString())
                        .append("</td><td><form action='/groups/").append(group.getGroupId()).append("/delete' method='post'>").append(csrfField(csrf))
                        .append("<button class='secondary' type='submit'>Deactivate</button></form></td></tr>");
            }
            html.append("</table></main></body></html>");
            return html.toString();
        }

        @PostMapping("/groups/add")
        public String addGroup(@RequestParam String groupName, Authentication authentication) {
            String trimmedName = groupName == null ? "" : groupName.trim();
            if (trimmedName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be blank");
            }
            if (imsService.getAllGroups().stream().anyMatch(g -> g.getGroupName().equalsIgnoreCase(trimmedName))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Group name already exists");
            }
            CustomerGroup group = new CustomerGroup();
            group.setGroupName(trimmedName);
            group.setIsActive(true);
            imsService.saveGroup(group);
            return "redirect:/groups";
        }

        @PostMapping("/groups/{id}/edit")
        public String editGroup(@PathVariable Long id, @RequestParam String groupName) {
            String trimmedName = groupName == null ? "" : groupName.trim();
            if (trimmedName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be blank");
            }
            imsService.updateGroup(id, trimmedName);
            return "redirect:/groups";
        }

        @PostMapping("/groups/{id}/delete")
        public String deleteGroup(@PathVariable Long id) {
            imsService.deleteGroup(id);
            return "redirect:/groups";
        }

        @GetMapping("/login")
        @ResponseBody
        public String login(CsrfToken csrf, @RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                @RequestParam(required = false) String registered) {
            return "<!doctype html><html lang='en'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Sign in · Code-B IMS</title>"
                    + "<style>body{margin:0;min-height:100vh;display:grid;place-items:center;background:#edf3ed;color:#17211d;font:16px 'Segoe UI',sans-serif}.panel{width:min(420px,calc(100% - 32px));background:white;border:1px solid #dce4de;padding:30px}h1{font-size:24px;margin-top:0}label{display:block;margin:14px 0 5px}input,button{box-sizing:border-box;width:100%;padding:11px;border:1px solid #c7d2ca;border-radius:4px;font:inherit}button{margin-top:18px;background:#176b4b;border-color:#176b4b;color:white;cursor:pointer}.notice{color:#a33}</style></head><body><main class='panel'><h1>Code-B IMS</h1><p>Sign in to continue.</p>"
                    + (error == null ? "" : "<p class='notice'>Username or password is incorrect, or the account is disabled.</p>")
                    + (logout == null ? "" : "<p>Signed out successfully.</p>")
                        + (registered == null ? "" : "<p class='notice'>Account created. You can sign in now.</p>")
                    + "<form method='post' action='/login'>" + csrfField(csrf)
                        + "<label for='username'>Username or email</label><input id='username' name='username' autocomplete='username' required><label for='password'>Password</label><input id='password' type='password' name='password' autocomplete='current-password' required><button>Sign in</button></form><p><a href='/register'>Create account</a> · <a href='/forgot-password'>Forgot password?</a></p></main></body></html>";
        }

        @GetMapping("/register")
        @ResponseBody
        public String registrationPage(CsrfToken csrf, @RequestParam(required = false) String error) {
            return accountShell("Create account", "Create an employee account to access the IMS.",
                    (error == null ? "" : "<p class='notice'>Account could not be created. Check the details or contact your administrator.</p>")
                            + "<form method='post' action='/register'>" + csrfField(csrf)
                            + "<label>Username</label><input name='username' autocomplete='username' required><label>Email</label><input type='email' name='email' autocomplete='email' required>"
                            + "<label>Password</label><input type='password' name='password' minlength='12' autocomplete='new-password' required><label>Confirm password</label><input type='password' name='confirmPassword' minlength='12' autocomplete='new-password' required>"
                            + "<button>Create account</button></form><p><a href='/login'>Back to sign in</a></p>");
        }

        @PostMapping("/register")
        public String register(@RequestParam String username, @RequestParam String email,
                               @RequestParam String password, @RequestParam String confirmPassword, CsrfToken csrf) {
            if (username.isBlank() || !isValidEmail(email) || password.length() < 12 || !password.equals(confirmPassword)) {
                return registrationPage(csrf, "invalid");
            }
            try {
                accountService.register(username.trim(), email.trim(), password);
                return "redirect:/login?registered";
            } catch (ResponseStatusException exception) {
                return registrationPage(csrf, "invalid");
            }
        }

        @GetMapping("/forgot-password")
        @ResponseBody
        public String forgotPasswordPage(CsrfToken csrf) {
            return forgotPasswordForm(csrf, false);
        }

        @PostMapping("/forgot-password")
        @ResponseBody
        public String requestPasswordReset(@RequestParam String email, CsrfToken csrf) {
            if (isValidEmail(email)) accountService.requestPasswordReset(email);
            return forgotPasswordForm(csrf, true);
        }

        @GetMapping("/reset-password")
        @ResponseBody
        public String resetPasswordPage(@RequestParam(required = false) String token, CsrfToken csrf) {
            if (!accountService.isResetTokenValid(token)) {
                return accountShell("Reset password", "This reset link is invalid, expired, or already used.", "<p><a href='/forgot-password'>Request another link</a></p>");
            }
            return resetPasswordForm(token, csrf, null);
        }

        @PostMapping("/reset-password")
        @ResponseBody
        public String resetPassword(@RequestParam String token, @RequestParam String password,
                                    @RequestParam String confirmPassword, CsrfToken csrf) {
            if (password.length() < 12 || !password.equals(confirmPassword)) {
                return resetPasswordForm(token, csrf, "Use matching passwords with at least 12 characters.");
            }
            if (!accountService.resetPassword(token, password)) {
                return accountShell("Reset password", "This reset link is invalid, expired, or already used.", "<p><a href='/forgot-password'>Request another link</a></p>");
            }
            return accountShell("Password updated", "Your password has been changed.", "<p><a href='/login'>Return to sign in</a></p>");
        }

        private static String forgotPasswordForm(CsrfToken csrf, boolean submitted) {
            String message = submitted ? "<p>If an account uses that email, a reset link has been sent.</p>" : "<p>Enter the email address on your account.</p>";
            return accountShell("Forgot password", message + "<form method='post' action='/forgot-password'>" + csrfField(csrf)
                    + "<label>Email</label><input type='email' name='email' autocomplete='email' required><button>Send reset link</button></form><p><a href='/login'>Back to sign in</a></p>");
        }

        private static String resetPasswordForm(String token, CsrfToken csrf, String error) {
            return accountShell("Choose a new password", (error == null ? "" : "<p class='notice'>" + error + "</p>")
                    + "<form method='post' action='/reset-password'>" + csrfField(csrf) + "<input type='hidden' name='token' value='" + escape(token) + "'>"
                    + "<label>New password</label><input type='password' name='password' minlength='12' autocomplete='new-password' required><label>Confirm password</label><input type='password' name='confirmPassword' minlength='12' autocomplete='new-password' required><button>Update password</button></form>");
        }

        private static String accountShell(String title, String content) {
            return "<!doctype html><html lang='en'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>"
                    + escape(title) + " · Code-B IMS</title><style>body{margin:0;min-height:100vh;display:grid;place-items:center;background:#edf3ed;color:#17211d;font:16px 'Segoe UI',sans-serif}.panel{width:min(460px,calc(100% - 32px));background:white;border:1px solid #dce4de;padding:30px}h1{font-size:24px;margin-top:0}label{display:block;margin:14px 0 5px}input,button{box-sizing:border-box;width:100%;padding:11px;border:1px solid #c7d2ca;border-radius:4px;font:inherit}button{margin-top:18px;background:#176b4b;border-color:#176b4b;color:white;cursor:pointer}.notice{color:#a33}</style></head><body><main class='panel'><h1>"
                    + escape(title) + "</h1>" + content + "</main></body></html>";
        }

        private static String accountShell(String title, String description, String content) {
            return accountShell(title, "<p>" + escape(description) + "</p>" + content);
        }

        private static boolean isValidEmail(String email) {
            return email != null && email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        }

    @PostMapping("/add-client")
    public String addClient(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam String company) {
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        client.setCompany(company);
        imsService.saveClient(client);
        return "redirect:/";
    }

        @PostMapping("/clients/{id}")
        public String updateClient(@PathVariable Long id, @RequestParam String name, @RequestParam String email,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String company) {
            Client client = new Client();
            client.setName(name);
            client.setEmail(email);
            client.setPhone(phone);
            client.setCompany(company);
            imsService.updateClient(id, client);
            return "redirect:/";
        }

    @PostMapping("/add-hierarchy")
    public String addHierarchy(@RequestParam String groupName,
                              @RequestParam(required = false) String chainName,
                              @RequestParam(required = false) String brandName,
                              @RequestParam(required = false) String subZone) {
        Hierarchy hierarchy = new Hierarchy();
        hierarchy.setGroupName(groupName);
        hierarchy.setChainName(chainName);
        hierarchy.setBrandName(brandName);
        hierarchy.setSubZone(subZone);
        imsService.saveHierarchy(hierarchy);
        return "redirect:/";
    }

    @PostMapping("/add-invoice")
        public String addInvoice(@RequestParam String clientName, @RequestParam double amount,
                                    @RequestParam double gstRate, @RequestParam String gstType, @RequestParam String status,
                                @RequestParam(required = false) String paymentMethod,
                                    @RequestParam(required = false) String paymentReference,
                                    Authentication authentication) {
            validateBilling(amount, gstRate);
                status = canonicalChoice(status, "Pending", "Paid", "Partial");
                requireChoice(gstType, "INTRA_STATE", "INTER_STATE");
                    gstType = gstType.toUpperCase(Locale.ROOT);
                                status = canonicalChoice(status, "Pending", "Paid", "Partial");
                if (!status.equals("Pending") && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can record invoice payments");
                }
        Invoice invoice = new Invoice();
        invoice.setClientName(clientName);
            invoice.setAmount(roundMoney(amount));
            invoice.setGstRate(gstRate);
                invoice.setGstType(gstType);
                invoice.setGstAmount(roundMoney(invoice.getAmount() * gstRate / 100));
                invoice.setTotalAmount(roundMoney(invoice.getAmount() + invoice.getGstAmount()));
        invoice.setStatus(status);
            invoice.setPaymentMethod(paymentMethod);
            invoice.setPaymentReference(paymentReference);
        imsService.saveInvoice(invoice);
        return "redirect:/";
    }

        @PostMapping("/admin/invoices/{id}/payment")
        public String updatePayment(@PathVariable Long id, @RequestParam String status,
                                    @RequestParam(required = false) String paymentMethod,
                                    @RequestParam(required = false) String paymentReference) {
            requireChoice(status, "Pending", "Paid", "Partial");
            imsService.updateInvoicePayment(id, status, paymentMethod, paymentReference);
            return "redirect:/";
        }

    @PostMapping("/add-estimate")
        public String addEstimate(@RequestParam String clientName, @RequestParam double amount,
                                     @RequestParam double gstRate, @RequestParam String gstType, @RequestParam String status) {
            validateBilling(amount, gstRate);
            requireChoice(status, "Draft", "Approved", "Rejected");
                status = canonicalChoice(status, "Draft", "Approved", "Rejected");
                requireChoice(gstType, "INTRA_STATE", "INTER_STATE");
                    gstType = gstType.toUpperCase(Locale.ROOT);
        Estimate estimate = new Estimate();
        estimate.setClientName(clientName);
            estimate.setAmount(roundMoney(amount));
            estimate.setGstRate(gstRate);
                estimate.setGstType(gstType);
                estimate.setGstAmount(roundMoney(estimate.getAmount() * gstRate / 100));
                estimate.setTotalAmount(roundMoney(estimate.getAmount() + estimate.getGstAmount()));
        estimate.setStatus(status);
        imsService.saveEstimate(estimate);
        return "redirect:/";
    }

        @PostMapping("/admin/users")
            public String createUser(@RequestParam String username, @RequestParam String email,
                                     @RequestParam String password, @RequestParam String role) {
                if (username.isBlank() || !isValidEmail(email) || password.length() < 12) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email and a password of at least 12 characters");
            }
            requireChoice(role.toUpperCase(Locale.ROOT), "ADMIN", "EMPLOYEE");
                imsService.createUser(username.trim(), email, password, role.toUpperCase(Locale.ROOT));
            return "redirect:/";
        }

        @PostMapping("/admin/users/{id}/toggle")
        public String toggleUser(@PathVariable Long id) {
            imsService.toggleUser(id);
            return "redirect:/";
        }

        private static void appendAdminUsers(StringBuilder html, CsrfToken csrf, List<User> users) {
            html.append("<section class='admin'><h2>User access</h2><form class='entry' method='post' action='/admin/users'>").append(csrfField(csrf))
                        .append("<input name='username' placeholder='Username' required><input type='email' name='email' placeholder='Email address' required><input type='password' name='password' minlength='12' placeholder='Password (12+ characters)' required><select name='role'><option>EMPLOYEE</option><option>ADMIN</option></select><button>Create account</button></form>")
                        .append("<div class='table-wrap'><table><tr><th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Access</th></tr>");
            for (User user : users) {
                    html.append("<tr><td>").append(escape(user.getUsername())).append("</td><td>").append(escape(user.getEmail())).append("</td><td>").append(escape(user.getRole()))
                        .append("</td><td>").append(user.isEnabled() ? "Active" : "Disabled").append("</td><td><form method='post' action='/admin/users/")
                        .append(user.getId()).append("/toggle'>").append(csrfField(csrf)).append("<button class='secondary'>")
                        .append(user.isEnabled() ? "Disable" : "Enable").append("</button></form></td></tr>");
            }
            html.append("</table></div></section>");
        }

        private static String csrfField(CsrfToken csrf) {
            return "<input type='hidden' name='" + escape(csrf.getParameterName()) + "' value='" + escape(csrf.getToken()) + "'>";
        }

        private static String gstOptions() {
            return "<select name='gstRate' aria-label='GST rate'><option value='0'>GST 0%</option><option value='5'>GST 5%</option><option value='12'>GST 12%</option><option value='18' selected>GST 18%</option><option value='28'>GST 28%</option></select><select name='gstType' aria-label='Supply state'><option value='INTRA_STATE'>Intra-state (CGST + SGST)</option><option value='INTER_STATE'>Inter-state (IGST)</option></select>";
        }

        private static String taxDisplay(double rate, double gstAmount, String gstType) {
            if ("INTER_STATE".equals(gstType)) return "IGST " + money(rate) + "% / INR " + money(gstAmount);
            double sgst = roundMoney(gstAmount / 2);
            double cgst = roundMoney(gstAmount - sgst);
            return "CGST " + money(rate / 2) + "% INR " + money(cgst) + " + SGST " + money(rate / 2) + "% INR " + money(sgst);
        }

        private static String money(double amount) { return String.format(Locale.ROOT, "%,.2f", amount); }
        private static double roundMoney(double amount) { return Math.round(amount * 100.0) / 100.0; }
        private static void validateBilling(double amount, double gstRate) {
            if (!Double.isFinite(amount) || amount <= 0 || amount != roundMoney(amount)
                    || !List.of(0.0, 5.0, 12.0, 18.0, 28.0).contains(gstRate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive with two decimals and GST must use a valid rate slab");
            }
        }
        private static void requireChoice(String value, String... allowed) {
            if (java.util.Arrays.stream(allowed).noneMatch(option -> option.equalsIgnoreCase(value))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid option");
            }
        }
            private static String canonicalChoice(String value, String... allowed) {
                requireChoice(value, allowed);
                return java.util.Arrays.stream(allowed).filter(option -> option.equalsIgnoreCase(value)).findFirst().orElseThrow();
            }
        private static String escape(String value) {
            if (value == null) return "";
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
        }
}