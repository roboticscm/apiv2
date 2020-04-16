package vn.com.sky.sys.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HasuraClaims {
    @JsonProperty("x-hasura-default-role")
    private String xHasuraDefaultRole;

    @JsonProperty("x-hasura-allowed-roles")
    private String[] xHasuraAllowedRoles;

    @JsonProperty("x-hasura-user-name")
    private String xHasuraUsername;

    @JsonProperty("x-hasura-user-id")
    private Long xHasuraUserId;
}
