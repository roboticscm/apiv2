package vn.com.sky.sys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.com.sky.base.GenericEntity;
import vn.com.sky.security.JwtRole;

@Data
@Table
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class HumanOrOrg extends GenericEntity implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long defaultOwnerOrgId;
    private String code;
    private String lastName;
    private String firstName;

    @Transient
    private String name;

    private String email;
    private String resetPasswordToken;
    private Long resetPasswordTime;
    private String nickName;
    private String username;
    private String password;
    private String fontIcon;
    private Boolean useFontIcon;
    private String iconData;
    private Long passwordExpired;
    private Boolean activated = true;
    private Long lastLogin;
    private Integer typeCustomer;
    private Integer typeSupplier;
    private Integer typeEmployee;
    private Integer typeCompany;

    public HumanOrOrg(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.setDisabled(enabled);
    }

    @JsonIgnore
    @Transient
    public ArrayList<JwtRole> getRoles() {
        var list = new ArrayList<JwtRole>();
        list.add(JwtRole.ROLE_USER);

        return list;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return super.getDisabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var roles = new ArrayList<JwtRole>();
        return roles
            .stream()
            .map(authority -> new SimpleGrantedAuthority(authority.name()))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }
}
