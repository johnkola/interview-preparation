# Security Deep Dive - OAuth, LDAP, SSO, MFA Interview Questions

## 🔐 OAuth 2.0 Questions & Answers

### Q1: Explain OAuth 2.0 and its grant types

**Answer:**
OAuth 2.0 is an authorization framework that enables applications to obtain limited access to user accounts. It works by delegating user authentication to the service that hosts the user account.

**Key Components:**
- **Resource Owner**: The user who owns the data
- **Client**: Application requesting access
- **Authorization Server**: Issues tokens after authentication
- **Resource Server**: Hosts protected resources

**Grant Types:**

1. **Authorization Code Grant** (Most secure for web apps):
```java
@RestController
public class OAuthController {

    @GetMapping("/authorize")
    public String authorize() {
        String authorizationUrl = "https://auth-server.com/oauth/authorize" +
            "?response_type=code" +
            "&client_id=" + clientId +
            "&redirect_uri=" + redirectUri +
            "&scope=read write" +
            "&state=" + generateState();

        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code, @RequestParam String state) {
        // Exchange authorization code for access token
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        TokenResponse token = restTemplate.postForObject(
            "https://auth-server.com/oauth/token",
            params,
            TokenResponse.class
        );

        // Store and use access token
        return "success";
    }
}
```

2. **Implicit Grant** (Deprecated, was for SPAs):
```javascript
// Not recommended anymore, use Authorization Code with PKCE instead
```

3. **Resource Owner Password Credentials** (Only for trusted apps):
```java
public String getTokenWithPassword(String username, String password) {
    RestTemplate restTemplate = new RestTemplate();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("username", username);
    params.add("password", password);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);

    TokenResponse token = restTemplate.postForObject(
        tokenEndpoint,
        params,
        TokenResponse.class
    );

    return token.getAccessToken();
}
```

4. **Client Credentials** (For machine-to-machine):
```java
@Component
public class M2MAuthService {

    public String getServiceToken() {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "service.read service.write");

        TokenResponse token = restTemplate.postForObject(
            tokenEndpoint,
            params,
            TokenResponse.class
        );

        return token.getAccessToken();
    }
}
```

5. **Refresh Token Grant**:
```java
public String refreshAccessToken(String refreshToken) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("refresh_token", refreshToken);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);

    TokenResponse newToken = restTemplate.postForObject(
        tokenEndpoint,
        params,
        TokenResponse.class
    );

    return newToken.getAccessToken();
}
```

### Q2: How do you implement OAuth 2.0 in Spring Boot?

**Answer:**

**Authorization Server Configuration:**
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
            .withClient("client-id")
            .secret(passwordEncoder.encode("client-secret"))
            .authorizedGrantTypes("authorization_code", "refresh_token", "client_credentials")
            .scopes("read", "write")
            .redirectUris("http://localhost:8080/callback")
            .accessTokenValiditySeconds(3600)
            .refreshTokenValiditySeconds(86400);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
            .accessTokenConverter(jwtAccessTokenConverter());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("secret-key");
        return converter;
    }
}
```

**Resource Server Configuration:**
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/**").authenticated();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("api");
    }
}
```

### Q3: What's the difference between ID Token and Access Token?

**Answer:**
- **Access Token**: Used to access protected resources, contains scopes/permissions
- **ID Token**: Contains user identity information, always a JWT in OpenID Connect

```java
// ID Token (JWT) contains user info
{
  "iss": "https://auth-server.com",
  "sub": "user123",
  "email": "user@example.com",
  "name": "John Doe",
  "iat": 1615902000,
  "exp": 1615905600
}

// Access Token may be opaque or JWT
{
  "scope": "read write",
  "client_id": "app123",
  "jti": "unique-token-id"
}
```

---

## 🏢 LDAP Questions & Answers

### Q4: What is LDAP and how do you integrate it with Spring Security?

**Answer:**
LDAP (Lightweight Directory Access Protocol) is a protocol for accessing and maintaining directory information services. It's commonly used for centralized authentication.

**Spring Security LDAP Configuration:**
```java
@Configuration
@EnableWebSecurity
public class LdapSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
            .userDnPatterns("uid={0},ou=people")
            .groupSearchBase("ou=groups")
            .contextSource()
                .url("ldap://ldap.company.com:389/dc=company,dc=com")
                .managerDn("cn=admin,dc=company,dc=com")
                .managerPassword("admin-password")
            .and()
            .passwordCompare()
                .passwordEncoder(new BCryptPasswordEncoder())
                .passwordAttribute("userPassword");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin();
    }
}
```

### Q5: How do you perform LDAP queries and user synchronization?

**Answer:**

**LDAP Template for Queries:**
```java
@Service
public class LdapUserService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<String> getAllUserNames() {
        return ldapTemplate.search(
            query().where("objectclass").is("person"),
            new AttributesMapper<String>() {
                public String mapFromAttributes(Attributes attrs) throws NamingException {
                    return attrs.get("cn").get().toString();
                }
            }
        );
    }

    public LdapUser findUser(String username) {
        return ldapTemplate.searchForObject(
            query().where("uid").is(username),
            new LdapUserAttributesMapper()
        );
    }

    public void updateUserPhone(String username, String newPhone) {
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(
            DirContext.REPLACE_ATTRIBUTE,
            new BasicAttribute("telephoneNumber", newPhone)
        );

        ldapTemplate.modifyAttributes(
            "uid=" + username + ",ou=people",
            mods
        );
    }

    // User synchronization
    @Scheduled(fixedDelay = 3600000) // Every hour
    public void syncLdapUsers() {
        List<LdapUser> ldapUsers = getAllLdapUsers();

        for (LdapUser ldapUser : ldapUsers) {
            User dbUser = userRepository.findByUsername(ldapUser.getUid());

            if (dbUser == null) {
                // Create new user
                dbUser = new User();
                dbUser.setUsername(ldapUser.getUid());
            }

            // Update user details
            dbUser.setEmail(ldapUser.getMail());
            dbUser.setFullName(ldapUser.getCn());
            dbUser.setDepartment(ldapUser.getDepartment());

            userRepository.save(dbUser);
        }
    }
}
```

### Q6: Explain LDAP directory structure and DN (Distinguished Name)

**Answer:**
LDAP uses a hierarchical tree structure. Each entry is uniquely identified by its DN.

```
dc=company,dc=com                          (Domain Component)
    |
    ├── ou=people                           (Organizational Unit)
    |   ├── uid=john.doe                    (User Entry)
    |   |   cn=John Doe
    |   |   mail=john.doe@company.com
    |   |   userPassword={SSHA}...
    |   └── uid=jane.smith
    |
    └── ou=groups
        ├── cn=developers
        |   member=uid=john.doe,ou=people,dc=company,dc=com
        └── cn=managers
```

**DN Examples:**
- User DN: `uid=john.doe,ou=people,dc=company,dc=com`
- Group DN: `cn=developers,ou=groups,dc=company,dc=com`

---

## 🔑 SSO (Single Sign-On) Questions & Answers

### Q7: Explain SSO and how to implement it

**Answer:**
SSO allows users to authenticate once and access multiple applications without re-authentication.

**SAML-based SSO Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SamlSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider provider = new SAMLAuthenticationProvider();
        provider.setUserDetails(new SAMLUserDetailsService());
        provider.setForcePrincipalAsString(false);
        return provider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/saml/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .apply(saml())
                .userDetailsService(samlUserDetailsService());
    }

    @Bean
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator generator = new MetadataGenerator();
        generator.setEntityId("http://localhost:8080/saml/metadata");
        generator.setEntityBaseURL("http://localhost:8080");
        generator.setRequestSigned(true);
        generator.setWantAssertionSigned(true);
        return generator;
    }
}
```

**OAuth/OIDC-based SSO:**
```java
@Configuration
@EnableWebSecurity
public class OidcSsoConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
            .oauth2Login()
                .authorizationEndpoint()
                    .baseUri("/oauth2/authorize")
                .and()
                .redirectionEndpoint()
                    .baseUri("/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                    .userService(customOAuth2UserService());
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return userRequest -> {
            OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

            // Custom logic to map OAuth2 user to application user
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(oauth2User.getAttribute("name"));
                userRepository.save(user);
            }

            return new CustomOAuth2User(oauth2User, user);
        };
    }
}
```

### Q8: What's the difference between SAML and OAuth/OIDC for SSO?

**Answer:**

| Aspect | SAML 2.0 | OAuth 2.0/OIDC |
|--------|----------|----------------|
| **Protocol** | XML-based | JSON/JWT-based |
| **Primary Use** | Enterprise SSO | API authorization + SSO (OIDC) |
| **Token Format** | XML assertions | JWT (usually) |
| **Mobile Support** | Limited | Excellent |
| **Complexity** | More complex | Simpler |

```java
// SAML Assertion Example (XML)
<saml:Assertion>
    <saml:Subject>
        <saml:NameID Format="email">user@company.com</saml:NameID>
    </saml:Subject>
    <saml:AttributeStatement>
        <saml:Attribute Name="firstName">
            <saml:AttributeValue>John</saml:AttributeValue>
        </saml:Attribute>
    </saml:AttributeStatement>
</saml:Assertion>

// OIDC ID Token Example (JWT)
{
  "iss": "https://idp.company.com",
  "sub": "user123",
  "email": "user@company.com",
  "given_name": "John",
  "family_name": "Doe"
}
```

---

## 🔒 MFA (Multi-Factor Authentication) Questions & Answers

### Q9: How do you implement MFA/2FA in a Spring application?

**Answer:**

**TOTP (Time-based One-Time Password) Implementation:**
```java
@Service
public class TotpService {

    private static final String ISSUER = "MyApp";
    private static final int SECRET_SIZE = 32;

    // Generate secret for user
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }

    // Generate QR code URL for Google Authenticator
    public String generateQRUrl(String username, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            ISSUER,
            username,
            secret,
            ISSUER
        );
    }

    // Verify TOTP code
    public boolean verifyCode(String secret, String code) {
        try {
            // Allow for time skew (30 seconds before/after)
            long timeWindow = System.currentTimeMillis() / 30000;

            for (int i = -1; i <= 1; i++) {
                String hash = generateTOTP(secret, timeWindow + i);
                if (hash.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateTOTP(String secret, long time)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] secretBytes = new Base32().decode(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(time).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
}
```

**MFA Authentication Flow:**
```java
@RestController
@RequestMapping("/auth")
public class MfaController {

    @Autowired
    private TotpService totpService;

    @Autowired
    private UserService userService;

    // Step 1: Username/Password login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = userService.findByUsername(request.getUsername());

        if (user.isMfaEnabled()) {
            // Generate temporary token for MFA verification
            String tempToken = generateTempToken(user);
            return ResponseEntity.ok(new MfaRequiredResponse(tempToken));
        } else {
            // No MFA, return full access token
            String accessToken = generateAccessToken(user);
            return ResponseEntity.ok(new TokenResponse(accessToken));
        }
    }

    // Step 2: MFA verification
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerificationRequest request) {
        String username = validateTempToken(request.getTempToken());
        User user = userService.findByUsername(username);

        // Verify TOTP code
        if (totpService.verifyCode(user.getMfaSecret(), request.getCode())) {
            String accessToken = generateAccessToken(user);
            return ResponseEntity.ok(new TokenResponse(accessToken));
        } else {
            return ResponseEntity.status(401).body("Invalid MFA code");
        }
    }

    // Enable MFA for user
    @PostMapping("/enable-mfa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enableMfa(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        String secret = totpService.generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userService.save(user);

        String qrUrl = totpService.generateQRUrl(user.getUsername(), secret);

        return ResponseEntity.ok(Map.of(
            "secret", secret,
            "qrCode", generateQRCode(qrUrl)
        ));
    }
}
```

### Q10: What are different types of MFA factors?

**Answer:**

1. **Something you know** (Knowledge):
```java
// Password, PIN, Security questions
public boolean verifySecurityQuestion(User user, String question, String answer) {
    return user.getSecurityAnswers()
        .get(question)
        .equals(hashAnswer(answer));
}
```

2. **Something you have** (Possession):
```java
// SMS OTP
@Service
public class SmsOtpService {
    public void sendOtp(String phoneNumber) {
        String otp = generateRandomOtp();
        otpCache.put(phoneNumber, otp, 5, TimeUnit.MINUTES);
        smsProvider.send(phoneNumber, "Your OTP is: " + otp);
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        String cached = otpCache.get(phoneNumber);
        return cached != null && cached.equals(otp);
    }
}

// Hardware token (FIDO2/WebAuthn)
@PostMapping("/webauthn/register")
public ResponseEntity<?> registerWebAuthn(@RequestBody WebAuthnRegistration registration) {
    // Verify attestation
    AttestationObject attestation = cbor.decode(registration.getAttestationObject());
    // Store public key for user
    user.addWebAuthnCredential(attestation.getCredentialId(), attestation.getPublicKey());
}
```

3. **Something you are** (Inherence):
```java
// Biometric (fingerprint, face recognition)
// Usually handled by device/OS, app receives verified token
@PostMapping("/biometric/verify")
public ResponseEntity<?> verifyBiometric(@RequestBody BiometricToken token) {
    // Verify token signature from trusted biometric system
    if (biometricService.verifyToken(token)) {
        return ResponseEntity.ok(generateAccessToken());
    }
    return ResponseEntity.status(401).build();
}
```

---

## 🔐 PingFederate Questions & Answers

### Q11: What is PingFederate and how do you integrate it?

**Answer:**
PingFederate is an enterprise federation server that enables SSO and secure API access management.

**Integration with Spring Boot:**
```java
@Configuration
public class PingFederateConfig {

    @Value("${pingfederate.base-url}")
    private String pingFedBaseUrl;

    @Value("${pingfederate.client-id}")
    private String clientId;

    @Value("${pingfederate.client-secret}")
    private String clientSecret;

    @Bean
    public OAuth2RestTemplate pingFederateRestTemplate() {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setAccessTokenUri(pingFedBaseUrl + "/as/token.oauth2");
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setScope(Arrays.asList("openid", "profile", "email"));

        OAuth2RestTemplate template = new OAuth2RestTemplate(details);
        template.setAccessTokenProvider(new ClientCredentialsAccessTokenProvider());

        return template;
    }

    @Bean
    public JwtDecoder pingFederateJwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(
            pingFedBaseUrl + "/.well-known/jwks.json"
        ).build();
    }
}

@RestController
public class PingFedController {

    @Autowired
    private OAuth2RestTemplate pingFedTemplate;

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        String userInfoEndpoint = pingFedBaseUrl + "/idp/userinfo.openid";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getTokenValue());

        ResponseEntity<Map> response = pingFedTemplate.exchange(
            userInfoEndpoint,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        return response.getBody();
    }
}
```

### Q12: How do you handle token validation with PingFederate?

**Answer:**

**Token Introspection:**
```java
@Service
public class PingFedTokenValidator {

    @Value("${pingfederate.introspection-uri}")
    private String introspectionUri;

    public TokenValidationResult validateToken(String token) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", token);
        params.add("token_type_hint", "access_token");

        HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(params, headers);

        try {
            ResponseEntity<IntrospectionResponse> response = restTemplate.exchange(
                introspectionUri,
                HttpMethod.POST,
                request,
                IntrospectionResponse.class
            );

            IntrospectionResponse body = response.getBody();

            if (body.isActive()) {
                return TokenValidationResult.valid(
                    body.getUsername(),
                    body.getScope(),
                    body.getExp()
                );
            } else {
                return TokenValidationResult.invalid("Token is not active");
            }
        } catch (Exception e) {
            return TokenValidationResult.invalid("Token validation failed: " + e.getMessage());
        }
    }
}

@Component
public class PingFedAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private PingFedTokenValidator tokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            TokenValidationResult result = tokenValidator.validateToken(token);

            if (result.isValid()) {
                Authentication auth = new PingFedAuthentication(
                    result.getUsername(),
                    result.getScopes()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---

## 🎯 Security Best Practices & Common Scenarios

### Q13: How do you handle token storage and refresh in a microservices architecture?

**Answer:**

```java
@Service
public class TokenManagementService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final String REFRESH_PREFIX = "refresh:";

    public void storeTokens(String userId, TokenResponse tokens) {
        // Store access token with expiry
        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + userId,
            tokens.getAccessToken(),
            tokens.getExpiresIn(),
            TimeUnit.SECONDS
        );

        // Store refresh token with longer expiry
        redisTemplate.opsForValue().set(
            REFRESH_PREFIX + userId,
            tokens.getRefreshToken(),
            30,
            TimeUnit.DAYS
        );
    }

    public String getAccessToken(String userId) {
        String token = redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);

        if (token == null) {
            // Try to refresh
            String refreshToken = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
            if (refreshToken != null) {
                token = refreshAccessToken(userId, refreshToken);
            }
        }

        return token;
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void refreshExpiringTokens() {
        Set<String> keys = redisTemplate.keys(TOKEN_PREFIX + "*");

        for (String key : keys) {
            Long ttl = redisTemplate.getExpire(key);

            // Refresh if expiring in less than 10 minutes
            if (ttl != null && ttl < 600) {
                String userId = key.replace(TOKEN_PREFIX, "");
                String refreshToken = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);

                if (refreshToken != null) {
                    refreshAccessToken(userId, refreshToken);
                }
            }
        }
    }
}
```

### Q14: How do you implement API Gateway security with OAuth?

**Answer:**

```java
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange()
                .pathMatchers("/api/public/**").permitAll()
                .pathMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
                .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt()
                .jwtDecoder(jwtDecoder())
            .and()
            .and()
            .csrf().disable()
            .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Component
    public class AuthorizationFilter implements GlobalFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(jwt -> {
                    // Add user context headers for downstream services
                    ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", jwt.getSubject())
                        .header("X-User-Roles", String.join(",", extractRoles(jwt)))
                        .header("X-User-Email", jwt.getClaimAsString("email"))
                        .build();

                    return exchange.mutate().request(request).build();
                })
                .flatMap(chain::filter);
        }

        private List<String> extractRoles(Jwt jwt) {
            Collection<String> authorities = jwt.getClaimAsStringList("authorities");
            return authorities != null ? new ArrayList<>(authorities) : Collections.emptyList();
        }
    }
}
```

### Q15: How do you prevent common security vulnerabilities in authentication?

**Answer:**

**1. Prevent Brute Force Attacks:**
```java
@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 5;
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, Integer>() {
                public Integer load(String key) {
                    return 0;
                }
            });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPTS;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
```

**2. Secure Password Reset:**
```java
@Service
public class PasswordResetService {

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // Don't reveal if email exists
            return;
        }

        String token = generateSecureToken();
        String hashedToken = hashToken(token);

        // Store hashed token with expiry
        user.setResetToken(hashedToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send email with unhashed token
        emailService.sendPasswordResetEmail(email, token);
    }

    public void resetPassword(String token, String newPassword) {
        String hashedToken = hashToken(token);
        User user = userRepository.findByResetToken(hashedToken);

        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        // Validate password strength
        validatePasswordStrength(newPassword);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        // Invalidate all existing sessions
        sessionRegistry.removeSessionInformation(user.getUsername());
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
```

**3. Session Security:**
```java
@Configuration
public class SessionConfig {

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ConcurrentSessionControlAuthenticationStrategy sessionControlStrategy() {
        ConcurrentSessionControlAuthenticationStrategy strategy =
            new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        strategy.setMaximumSessions(1); // Only one session per user
        strategy.setExceptionIfMaximumExceeded(false); // Invalidate oldest session
        return strategy;
    }
}
```

---

## 📚 Additional Security Topics

### CORS Configuration for OAuth
```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://app.company.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### Security Headers
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers()
        .frameOptions().deny()
        .xssProtection().and()
        .contentSecurityPolicy("default-src 'self'")
        .and()
        .httpStrictTransportSecurity()
            .maxAgeInSeconds(31536000)
            .includeSubDomains(true);

    return http.build();
}
```

This comprehensive guide covers the major security topics you'll likely encounter in the TD Bank interview. Each section includes practical code examples that demonstrate real-world implementations.