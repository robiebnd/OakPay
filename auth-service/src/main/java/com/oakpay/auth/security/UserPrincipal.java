package com.oakpay.auth.security;

import com.oakpay.auth.user.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    private final UUID userId;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final String role;

    private UserPrincipal(UUID userId, String email, String password, boolean enabled, String role) {
        this.userId=userId; this.email=email; this.password=password; this.enabled=enabled; this.role=role==null?"CLIENT":role;
    }
    public static UserPrincipal from(User user){return new UserPrincipal(user.getId(),user.getEmail(),user.getPassword(),user.isEnabled(),user.getRole());}
    public UUID getUserId(){return userId;} public String getRole(){return role;}
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of(new SimpleGrantedAuthority("ROLE_"+role));}
    @Override public String getPassword(){return password;} @Override public String getUsername(){return email;}
    @Override public boolean isAccountNonExpired(){return true;} @Override public boolean isAccountNonLocked(){return true;}
    @Override public boolean isCredentialsNonExpired(){return true;} @Override public boolean isEnabled(){return enabled;}
}