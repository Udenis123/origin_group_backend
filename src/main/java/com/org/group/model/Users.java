package com.org.group.model;

import com.org.group.model.project.Bookmark;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.LaunchProject;
import com.org.group.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "specify your name")
    public String name;

    @NotBlank(message = "specify ID")
    @Column(unique = true, nullable = false)
    private String nationalId;

    @NotBlank(message = "specify gender")
    @Column(nullable = false)
    private String gender;

    @NotBlank(message = "specify nationality")
    @Column(nullable = false)
    private String nationality;

    @NotBlank(message = "specify professional")
    @Column(nullable = false)
    private String professional;

    @Column(unique = true, nullable = false)
    @Email(message = "please put valid email")
    private String email;

    @Size(min = 10, max = 15, message = "phone number must be 10 digit")
    @Column(nullable = false, unique = true)
    private String phone;

    private String password;

    private boolean enabled;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "code_expiry_at")
    private LocalDateTime codeExpiryAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Column(nullable = false)
    private Boolean subscribed;

    @Column(nullable = false)
    private boolean isActive;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "temp_email", nullable = true,updatable = true)
    private String tempEmail;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private Set<UserSubscription> subscriptions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LaunchProject> launchProjects;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommunityProject> communityProjects;

    @OneToOne(mappedBy = "users")
    private UserRatting userRatting;

    public Users() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}