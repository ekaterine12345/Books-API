package com.example.books_api.entities;


import com.example.books_api.user.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;
//import org.jspecify.annotations.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "_users")
public class User extends BaseEntity<Long> implements UserDetails {
    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    @SequenceGenerator(name="userSeqId", sequenceName = "USER_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="userSeqId")
    private Long id;

    private String firstname;
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email; //username

    @Column(nullable = false)
    private String password; // Will be BCrypt encoded

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    } // @Nullable

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
        return true;
    }


    @ManyToMany
    @JoinTable(name = "user_purchases", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @JsonIgnore
    @Builder.Default
    private List<Book> purchasedBooks = new ArrayList<>();


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Cart cart;


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
