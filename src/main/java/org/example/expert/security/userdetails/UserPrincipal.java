package org.example.expert.security.userdetails;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String role;

    public static UserPrincipal of(Long id, String email, String role) {
        return new UserPrincipal(id, email, role);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {return "";}
    @Override public String getUsername() { return email; }
    @Override
    public boolean isAccountNonExpired() {return true;}
    @Override
    public boolean isAccountNonLocked() {return true;}
    @Override
    public boolean isCredentialsNonExpired() {return true;}
    @Override
    public boolean isEnabled() {return true;}


}
