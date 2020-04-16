package vn.com.sky.sys.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthTokenReq {
    private Long id;
    private String token;
    private Long userId;
    private String lastLocaleLanguage;
    private Long companyId;
}
