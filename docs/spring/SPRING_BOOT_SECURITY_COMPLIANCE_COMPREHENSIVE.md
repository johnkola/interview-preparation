# Spring Boot Advanced Security and Compliance - Comprehensive Guide

## Table of Contents
1. [Introduction to Security and Compliance](#introduction-to-security-and-compliance)
2. [Spring Security Fundamentals](#spring-security-fundamentals)
3. [Authentication Mechanisms](#authentication-mechanisms)
4. [Authorization and Access Control](#authorization-and-access-control)
5. [OAuth2 and OpenID Connect](#oauth2-and-openid-connect)
6. [JWT Token Management](#jwt-token-management)
7. [Multi-Factor Authentication](#multi-factor-authentication)
8. [Session Management](#session-management)
9. [CSRF Protection](#csrf-protection)
10. [CORS Configuration](#cors-configuration)
11. [API Security](#api-security)
12. [Microservices Security](#microservices-security)
13. [Data Protection and Encryption](#data-protection-and-encryption)
14. [Audit and Compliance](#audit-and-compliance)
15. [PCI DSS Compliance](#pci-dss-compliance)
16. [GDPR Compliance](#gdpr-compliance)
17. [SOX Compliance](#sox-compliance)
18. [Security Testing](#security-testing)
19. [Monitoring and Incident Response](#monitoring-and-incident-response)
20. [Banking Domain Security Examples](#banking-domain-security-examples)
21. [Best Practices](#best-practices)
22. [Common Vulnerabilities](#common-vulnerabilities)
23. [Interview Questions](#interview-questions)

## Introduction to Security and Compliance

Security and compliance are critical aspects of enterprise applications, especially in banking and financial services. This guide covers comprehensive security implementation using Spring Boot and compliance with major regulations.

### Security Principles

**Defense in Depth**
- Multiple layers of security controls
- Principle of least privilege
- Fail-safe defaults
- Complete mediation

**Key Security Areas**
- Authentication and Authorization
- Data Protection
- Communication Security
- Audit and Monitoring
- Incident Response

### Compliance Requirements

**Financial Regulations**
- PCI DSS (Payment Card Industry Data Security Standard)
- SOX (Sarbanes-Oxley Act)
- GDPR (General Data Protection Regulation)
- PSD2 (Payment Services Directive 2)
- Basel III

## Spring Security Fundamentals

### Core Dependencies

```xml
<dependencies>
    <!-- Spring Boot Security Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- OAuth2 Client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- JWT Support -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>

    <!-- Security Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Basic Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts/**").hasRole("USER")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .and())
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
```

## Authentication Mechanisms

### Database Authentication

```java
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check for brute force attacks
        if (loginAttemptService.isBlocked(username)) {
            throw new AccountLockedException("Account temporarily locked due to multiple failed attempts");
        }

        User user = userRepository.findByUsernameAndActiveTrue(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return SecurityUser.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(mapRolesToAuthorities(user.getRoles()))
            .accountNonExpired(!user.isExpired())
            .accountNonLocked(!user.isLocked())
            .credentialsNonExpired(!user.isPasswordExpired())
            .enabled(user.isActive())
            .lastLoginDate(user.getLastLoginDate())
            .passwordLastChanged(user.getPasswordLastChanged())
            .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
            .flatMap(role -> Stream.concat(
                Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getName())),
                role.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
            ))
            .collect(Collectors.toSet());
    }
}

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) {
                    return 0;
                }
            });
    }

    public void loginSucceeded(String username) {
        attemptsCache.invalidate(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getUnchecked(username);
        attempts++;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            // Log security event
            SecurityEventLogger.logAccountLockout(username, attempts);
        }
    }

    public boolean isBlocked(String username) {
        return attemptsCache.getUnchecked(username) >= MAX_ATTEMPTS;
    }
}
```

### LDAP Authentication

```java
@Configuration
@EnableLdapRepositories
public class LdapSecurityConfig {

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://ldap.company.com:389");
        contextSource.setBase("dc=company,dc=com");
        contextSource.setUserDn("cn=admin,dc=company,dc=com");
        contextSource.setPassword("admin_password");
        return contextSource;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(
            new BindAuthenticator(contextSource()),
            new LdapAuthoritiesPopulator() {
                @Override
                public Collection<? extends GrantedAuthority> getGrantedAuthorities(
                        DirContextOperations userData, String username) {
                    return getLdapAuthorities(userData, username);
                }
            }
        );
        provider.setUserDetailsContextMapper(new LdapUserDetailsMapper());
        return provider;
    }

    private Collection<? extends GrantedAuthority> getLdapAuthorities(
            DirContextOperations userData, String username) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract groups from LDAP
        String[] groups = userData.getStringAttributes("memberOf");
        if (groups != null) {
            for (String group : groups) {
                // Extract group name from DN
                String groupName = extractGroupName(group);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + groupName));
            }
        }

        // Map additional permissions based on user attributes
        String department = userData.getStringAttribute("department");
        if ("IT".equals(department)) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_SYSTEM_ADMIN"));
        }

        return authorities;
    }

    private String extractGroupName(String groupDn) {
        // Extract CN from group DN
        Pattern pattern = Pattern.compile("CN=([^,]+)");
        Matcher matcher = pattern.matcher(groupDn);
        return matcher.find() ? matcher.group(1) : "";
    }

    public static class LdapUserDetailsMapper implements UserDetailsContextMapper {
        @Override
        public UserDetails mapUserFromContext(DirContextOperations ctx,
                String username, Collection<? extends GrantedAuthority> authorities) {

            return SecurityUser.builder()
                .username(username)
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .email(ctx.getStringAttribute("mail"))
                .fullName(ctx.getStringAttribute("displayName"))
                .department(ctx.getStringAttribute("department"))
                .build();
        }

        @Override
        public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
            // Implementation for updating LDAP user details
        }
    }
}
```

## Authorization and Access Control

### Method-Level Security

```java
@Service
@PreAuthorize("hasRole('USER')")
public class AccountSecurityService {

    @PreAuthorize("hasRole('ADMIN') or @accountOwnershipService.isOwner(authentication.name, #accountId)")
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    @PreAuthorize("@accountSecurityService.canAccessAccount(authentication.name, #accountId, 'READ')")
    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @PreAuthorize("@accountSecurityService.canTransfer(authentication.name, #fromAccountId, #toAccountId, #amount)")
    @PostAuthorize("@auditService.logTransfer(authentication.name, returnObject)")
    public TransferResult transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        return transferService.transfer(fromAccountId, toAccountId, amount);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostFilter("@accountSecurityService.filterByAccessLevel(authentication.name, filterObject)")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Secured({"ROLE_ADMIN", "ROLE_AUDITOR"})
    public List<AuditLog> getAuditLogs(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @RolesAllowed({"ADMIN", "COMPLIANCE_OFFICER"})
    public ComplianceReport generateComplianceReport(ComplianceReportRequest request) {
        return complianceService.generateReport(request);
    }
}

@Component
public class AccountOwnershipService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public boolean isOwner(String username, Long accountId) {
        return customerRepository.findByUsername(username)
            .flatMap(customer -> accountRepository.findById(accountId)
                .filter(account -> account.getCustomerId().equals(customer.getId())))
            .isPresent();
    }

    public boolean canAccessAccount(String username, Long accountId, String operation) {
        // Check ownership
        if (isOwner(username, accountId)) {
            return true;
        }

        // Check for additional permissions (e.g., joint accounts, power of attorney)
        return hasAdditionalPermissions(username, accountId, operation);
    }

    public boolean canTransfer(String username, Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // Check ownership of source account
        if (!canAccessAccount(username, fromAccountId, "TRANSFER")) {
            return false;
        }

        // Check transfer limits
        Account fromAccount = accountRepository.findById(fromAccountId).orElse(null);
        if (fromAccount == null) {
            return false;
        }

        // Daily transfer limit check
        BigDecimal dailyTransferLimit = getDailyTransferLimit(username);
        BigDecimal todaysTransfers = getTodaysTransferAmount(fromAccountId);

        return todaysTransfers.add(amount).compareTo(dailyTransferLimit) <= 0;
    }

    private boolean hasAdditionalPermissions(String username, Long accountId, String operation) {
        // Implementation for checking joint accounts, power of attorney, etc.
        return false;
    }

    private BigDecimal getDailyTransferLimit(String username) {
        return customerRepository.findByUsername(username)
            .map(Customer::getDailyTransferLimit)
            .orElse(BigDecimal.valueOf(5000)); // Default limit
    }

    private BigDecimal getTodaysTransferAmount(Long accountId) {
        LocalDate today = LocalDate.now();
        return transactionRepository.findTransferAmountByAccountIdAndDate(accountId, today);
    }
}
```

### Role-Based Access Control (RBAC)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    private boolean active = true;
    private boolean locked = false;
    private boolean expired = false;
    private boolean passwordExpired = false;
    private LocalDateTime lastLoginDate;
    private LocalDateTime passwordLastChanged;

    // Constructors, getters, setters
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    // Constructors, getters, setters
}

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    private String resource;
    private String action;

    // Constructors, getters, setters
}

@Service
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public void assignPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));

        Permission permission = permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + permissionName));

        role.getPermissions().add(permission);
        roleRepository.save(role);

        // Log the assignment for audit
        auditService.logRolePermissionAssignment(roleName, permissionName);
    }

    public void revokePermissionFromRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));

        Permission permission = permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + permissionName));

        role.getPermissions().remove(permission);
        roleRepository.save(role);

        // Log the revocation for audit
        auditService.logRolePermissionRevocation(roleName, permissionName);
    }

    public Set<Permission> getUserPermissions(String username) {
        return userRepository.findByUsername(username)
            .map(user -> user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    public boolean hasPermission(String username, String resource, String action) {
        return getUserPermissions(username).stream()
            .anyMatch(permission ->
                permission.getResource().equals(resource) &&
                permission.getAction().equals(action));
    }
}
```

## OAuth2 and OpenID Connect

### OAuth2 Authorization Server

```java
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()")
            .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
            .withClient("banking-web-app")
                .secret(passwordEncoder.encode("web-app-secret"))
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("read", "write")
                .redirectUris("https://banking-web.company.com/callback")
                .accessTokenValiditySeconds(3600) // 1 hour
                .refreshTokenValiditySeconds(86400) // 24 hours
            .and()
            .withClient("mobile-banking-app")
                .secret(passwordEncoder.encode("mobile-app-secret"))
                .authorizedGrantTypes("authorization_code", "refresh_token", "password")
                .scopes("read", "write", "mobile")
                .accessTokenValiditySeconds(1800) // 30 minutes
                .refreshTokenValiditySeconds(604800) // 1 week
            .and()
            .withClient("third-party-service")
                .secret(passwordEncoder.encode("third-party-secret"))
                .authorizedGrantTypes("client_credentials")
                .scopes("api-access")
                .accessTokenValiditySeconds(7200); // 2 hours
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
            .tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancerChain())
            .accessTokenConverter(jwtAccessTokenConverter())
            .reuseRefreshTokens(false);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("banking-secret-key");
        return converter;
    }

    @Bean
    public TokenEnhancerChain tokenEnhancerChain() {
        TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(Arrays.asList(
            new CustomTokenEnhancer(),
            jwtAccessTokenConverter()
        ));
        return chain;
    }

    public static class CustomTokenEnhancer implements TokenEnhancer {
        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
                OAuth2Authentication authentication) {

            Map<String, Object> additionalInfo = new HashMap<>();

            // Add custom claims
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            additionalInfo.put("username", userDetails.getUsername());
            additionalInfo.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
            additionalInfo.put("issued_at", System.currentTimeMillis());

            // Add banking-specific claims
            if (userDetails instanceof SecurityUser) {
                SecurityUser securityUser = (SecurityUser) userDetails;
                additionalInfo.put("customer_id", securityUser.getCustomerId());
                additionalInfo.put("branch_code", securityUser.getBranchCode());
            }

            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            return accessToken;
        }
    }
}
```

### OAuth2 Resource Server

```java
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
            .resourceId("banking-api")
            .tokenStore(tokenStore())
            .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .requestMatchers()
                .antMatchers("/api/**")
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/v1/accounts/**").access("#oauth2.hasScope('read')")
                .antMatchers(HttpMethod.POST, "/api/v1/accounts/**").access("#oauth2.hasScope('write')")
                .antMatchers("/api/v1/admin/**").access("#oauth2.hasScope('admin')")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec("banking-secret-key".getBytes(), "HmacSHA256")
        ).build();

        decoder.setJwtValidator(jwtValidator());
        return decoder;
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> authorities = jwt.getClaimAsStringList("authorities");
            return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        return converter;
    }

    @Bean
    public OAuth2TokenValidator<Jwt> jwtValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new JwtIssuerValidator("https://auth.company.com"));
        validators.add(new CustomJwtValidator());

        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    public static class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {
        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            // Custom validation logic
            String username = jwt.getClaimAsString("username");
            if (username == null || username.trim().isEmpty()) {
                return OAuth2TokenValidatorResult.failure("Missing username claim");
            }

            // Check if user is still active
            boolean isActive = userService.isUserActive(username);
            if (!isActive) {
                return OAuth2TokenValidatorResult.failure("User account is inactive");
            }

            return OAuth2TokenValidatorResult.success();
        }
    }
}
```

### OpenID Connect Implementation

```java
@Configuration
@EnableOAuth2Client
public class OpenIDConnectConfig {

    @Bean
    public OAuth2RestTemplate openIdConnectRestTemplate() {
        return new OAuth2RestTemplate(openIdConnectResource());
    }

    @Bean
    public OAuth2ProtectedResourceDetails openIdConnectResource() {
        AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();
        resource.setClientId("banking-oidc-client");
        resource.setClientSecret("oidc-client-secret");
        resource.setAccessTokenUri("https://auth.company.com/oauth/token");
        resource.setUserAuthorizationUri("https://auth.company.com/oauth/authorize");
        resource.setScope(Arrays.asList("openid", "profile", "email"));
        return resource;
    }

    @Bean
    public OpenIDConnectFilter openIdConnectFilter() {
        OpenIDConnectFilter filter = new OpenIDConnectFilter("/oidc/callback");
        filter.setRestTemplate(openIdConnectRestTemplate());
        filter.setIssuer("https://auth.company.com");
        filter.setClientId("banking-oidc-client");
        return filter;
    }

    public static class OpenIDConnectFilter extends AbstractAuthenticationProcessingFilter {

        private OAuth2RestOperations restTemplate;
        private String issuer;
        private String clientId;

        public OpenIDConnectFilter(String defaultFilterProcessesUrl) {
            super(defaultFilterProcessesUrl);
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request,
                HttpServletResponse response) throws AuthenticationException {

            String code = request.getParameter("code");
            if (code == null) {
                throw new BadCredentialsException("Authorization code is missing");
            }

            try {
                // Exchange authorization code for tokens
                OAuth2AccessToken accessToken = restTemplate.getAccessToken();

                // Get ID token
                String idToken = (String) accessToken.getAdditionalInformation().get("id_token");
                if (idToken == null) {
                    throw new BadCredentialsException("ID token is missing");
                }

                // Validate and parse ID token
                Jwt jwt = parseAndValidateIdToken(idToken);

                // Extract user information
                String username = jwt.getClaimAsString("sub");
                String email = jwt.getClaimAsString("email");
                String name = jwt.getClaimAsString("name");

                // Create authentication token
                OpenIDConnectAuthenticationToken authToken =
                    new OpenIDConnectAuthenticationToken(username, accessToken, jwt);

                return getAuthenticationManager().authenticate(authToken);

            } catch (Exception e) {
                throw new BadCredentialsException("OpenID Connect authentication failed", e);
            }
        }

        private Jwt parseAndValidateIdToken(String idToken) {
            // Parse and validate ID token
            return jwtDecoder.decode(idToken);
        }
    }
}
```

## JWT Token Management

### JWT Service Implementation

```java
@Service
public class JwtTokenService {

    private static final String SECRET_KEY = "banking-jwt-secret-key-must-be-long-enough";
    private static final int ACCESS_TOKEN_VALIDITY = 3600; // 1 hour
    private static final int REFRESH_TOKEN_VALIDITY = 86400; // 24 hours

    private final Key signingKey;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenService(UserDetailsService userDetailsService,
                          TokenBlacklistService tokenBlacklistService) {
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access_token");
        claims.put("authorities", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));

        if (userDetails instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) userDetails;
            claims.put("customer_id", securityUser.getCustomerId());
            claims.put("branch_code", securityUser.getBranchCode());
        }

        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh_token");
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String username = getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Invalidate old refresh token
        tokenBlacklistService.blacklistToken(refreshToken);

        return generateAccessToken(userDetails);
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);

            // Additional validations
            Claims claims = claimsJws.getBody();
            String username = claims.getSubject();

            // Check if user still exists and is active
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return userDetails.isEnabled() &&
                   userDetails.isAccountNonExpired() &&
                   userDetails.isAccountNonLocked() &&
                   userDetails.isCredentialsNonExpired();

        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private String createToken(Map<String, Object> claims, String subject, int validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity * 1000L);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setIssuer("banking-app")
            .setId(UUID.randomUUID().toString())
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    public void invalidateToken(String token) {
        tokenBlacklistService.blacklistToken(token);
    }

    public void invalidateAllUserTokens(String username) {
        tokenBlacklistService.blacklistAllUserTokens(username);
    }
}

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_KEY_PREFIX = "blacklisted_token:";
    private static final String USER_TOKENS_KEY_PREFIX = "user_tokens:";

    public void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                    BLACKLIST_KEY_PREFIX + token,
                    "blacklisted",
                    Duration.ofMillis(ttl)
                );
            }
        } catch (Exception e) {
            logger.error("Error blacklisting token", e);
        }
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + token);
    }

    public void blacklistAllUserTokens(String username) {
        String userTokensKey = USER_TOKENS_KEY_PREFIX + username;
        Set<String> userTokens = redisTemplate.opsForSet().members(userTokensKey);

        if (userTokens != null) {
            userTokens.forEach(this::blacklistToken);
            redisTemplate.delete(userTokensKey);
        }
    }

    public void trackUserToken(String username, String token) {
        String userTokensKey = USER_TOKENS_KEY_PREFIX + username;
        redisTemplate.opsForSet().add(userTokensKey, token);

        // Set expiration for user tokens tracking
        redisTemplate.expire(userTokensKey, Duration.ofDays(30));
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor("banking-jwt-secret-key-must-be-long-enough".getBytes());
    }
}
```

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && jwtTokenService.validateToken(token)) {
                String username = jwtTokenService.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Log successful authentication
                    auditService.logSuccessfulAuthentication(username, request.getRemoteAddr());
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication error: {}", e.getMessage());
            auditService.logFailedAuthentication(request.getRemoteAddr(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/public/") ||
               path.equals("/actuator/health");
    }
}
```

## Multi-Factor Authentication

### MFA Configuration

```java
@Configuration
public class MfaConfig {

    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        GoogleAuthenticator authenticator = new GoogleAuthenticator();
        authenticator.setWindowSize(3); // Allow 3 intervals for clock skew
        return authenticator;
    }

    @Bean
    public QRGenerator qrGenerator() {
        return new QRGenerator();
    }
}

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator;
    private final QRGenerator qrGenerator;
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final EmailService emailService;

    public MfaSetupResult setupTotp(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isMfaEnabled()) {
            throw new MfaAlreadyEnabledException("MFA is already enabled for this user");
        }

        // Generate secret key
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secretKey = key.getKey();

        // Generate QR code
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            "Banking App",
            username,
            key
        );

        String qrCodeImage = qrGenerator.generateQRCodeImage(qrCodeUrl, 200, 200);

        // Store secret key (encrypted)
        user.setMfaSecretKey(encryptionService.encrypt(secretKey));
        user.setMfaType(MfaType.TOTP);
        user.setMfaEnabled(false); // Will be enabled after verification
        userRepository.save(user);

        return MfaSetupResult.builder()
            .secretKey(secretKey)
            .qrCodeImage(qrCodeImage)
            .backupCodes(generateBackupCodes(user))
            .build();
    }

    public boolean verifyTotp(String username, int totpCode) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        String encryptedSecret = user.getMfaSecretKey();
        if (encryptedSecret == null) {
            return false;
        }

        String secretKey = encryptionService.decrypt(encryptedSecret);
        boolean isValid = googleAuthenticator.authorize(secretKey, totpCode);

        if (isValid && !user.isMfaEnabled()) {
            // First successful verification - enable MFA
            user.setMfaEnabled(true);
            user.setMfaVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            auditService.logMfaEnabled(username);
        }

        return isValid;
    }

    public String sendSmsCode(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new PhoneNumberNotConfiguredException("Phone number not configured");
        }

        // Generate 6-digit code
        String code = generateSmsCode();

        // Store code with expiration (5 minutes)
        String cacheKey = "sms_mfa:" + username;
        redisTemplate.opsForValue().set(cacheKey, code, Duration.ofMinutes(5));

        // Send SMS
        smsService.sendSms(phoneNumber, "Your banking app verification code is: " + code);

        auditService.logMfaSmsSent(username, phoneNumber);

        return "SMS code sent to " + maskPhoneNumber(phoneNumber);
    }

    public boolean verifySmsCode(String username, String code) {
        String cacheKey = "sms_mfa:" + username;
        String storedCode = redisTemplate.opsForValue().get(cacheKey);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(cacheKey);
            auditService.logMfaSmsVerified(username);
            return true;
        }

        auditService.logMfaSmsVerificationFailed(username);
        return false;
    }

    public List<String> generateBackupCodes(User user) {
        List<String> backupCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            backupCodes.add(code);
        }

        // Store encrypted backup codes
        String encryptedCodes = encryptionService.encrypt(String.join(",", backupCodes));
        user.setMfaBackupCodes(encryptedCodes);
        userRepository.save(user);

        return backupCodes;
    }

    public boolean verifyBackupCode(String username, String backupCode) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        String encryptedCodes = user.getMfaBackupCodes();
        if (encryptedCodes == null) {
            return false;
        }

        String decryptedCodes = encryptionService.decrypt(encryptedCodes);
        List<String> codes = Arrays.asList(decryptedCodes.split(","));

        if (codes.contains(backupCode)) {
            // Remove used backup code
            codes.remove(backupCode);
            String updatedCodes = String.join(",", codes);
            user.setMfaBackupCodes(encryptionService.encrypt(updatedCodes));
            userRepository.save(user);

            auditService.logMfaBackupCodeUsed(username);
            return true;
        }

        return false;
    }

    private String generateSmsCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() > 4) {
            return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
        }
        return phoneNumber;
    }
}
```

### MFA Authentication Process

```java
@RestController
@RequestMapping("/api/v1/auth")
public class MfaAuthenticationController {

    private final MfaService mfaService;
    private final AuthenticationService authenticationService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Primary authentication
            Authentication auth = authenticationService.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            if (user.isMfaEnabled()) {
                // Generate temporary token for MFA
                String tempToken = jwtTokenService.generateTemporaryToken(userDetails);

                return ResponseEntity.ok(LoginResponse.builder()
                    .requiresMfa(true)
                    .mfaType(user.getMfaType())
                    .tempToken(tempToken)
                    .message("MFA verification required")
                    .build());
            } else {
                // Direct login without MFA
                String accessToken = jwtTokenService.generateAccessToken(userDetails);
                String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

                return ResponseEntity.ok(LoginResponse.builder()
                    .requiresMfa(false)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600)
                    .build());
            }

        } catch (BadCredentialsException e) {
            auditService.logFailedLogin(request.getUsername(), "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse.builder()
                    .error("Invalid username or password")
                    .build());
        }
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<LoginResponse> verifyMfa(@Valid @RequestBody MfaVerificationRequest request) {
        try {
            // Validate temporary token
            if (!jwtTokenService.validateTemporaryToken(request.getTempToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                        .error("Invalid or expired temporary token")
                        .build());
            }

            String username = jwtTokenService.getUsernameFromToken(request.getTempToken());
            boolean isValid = false;

            switch (request.getMfaType()) {
                case TOTP:
                    isValid = mfaService.verifyTotp(username, Integer.parseInt(request.getCode()));
                    break;
                case SMS:
                    isValid = mfaService.verifySmsCode(username, request.getCode());
                    break;
                case BACKUP_CODE:
                    isValid = mfaService.verifyBackupCode(username, request.getCode());
                    break;
            }

            if (isValid) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                String accessToken = jwtTokenService.generateAccessToken(userDetails);
                String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

                // Invalidate temporary token
                jwtTokenService.invalidateToken(request.getTempToken());

                auditService.logSuccessfulMfaVerification(username, request.getMfaType());

                return ResponseEntity.ok(LoginResponse.builder()
                    .requiresMfa(false)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600)
                    .build());
            } else {
                auditService.logFailedMfaVerification(username, request.getMfaType());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                        .error("Invalid MFA code")
                        .build());
            }

        } catch (Exception e) {
            logger.error("MFA verification error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LoginResponse.builder()
                    .error("MFA verification failed")
                    .build());
        }
    }

    @PostMapping("/mfa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSetupResponse> setupMfa(@RequestParam MfaType mfaType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            switch (mfaType) {
                case TOTP:
                    MfaSetupResult result = mfaService.setupTotp(username);
                    return ResponseEntity.ok(MfaSetupResponse.builder()
                        .secretKey(result.getSecretKey())
                        .qrCodeImage(result.getQrCodeImage())
                        .backupCodes(result.getBackupCodes())
                        .message("Scan QR code with authenticator app")
                        .build());

                case SMS:
                    String smsResult = mfaService.sendSmsCode(username);
                    return ResponseEntity.ok(MfaSetupResponse.builder()
                        .message(smsResult)
                        .build());

                default:
                    return ResponseEntity.badRequest()
                        .body(MfaSetupResponse.builder()
                            .error("Unsupported MFA type")
                            .build());
            }
        } catch (Exception e) {
            logger.error("MFA setup error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MfaSetupResponse.builder()
                    .error("MFA setup failed: " + e.getMessage())
                    .build());
        }
    }
}
```

## Data Protection and Encryption

### Field-Level Encryption

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Encrypted
    @Column(name = "ssn")
    private String socialSecurityNumber;

    @Encrypted
    @Column(name = "phone")
    private String phoneNumber;

    @Encrypted
    @Column(name = "email")
    private String email;

    @Encrypted
    @Column(name = "address")
    private String address;

    // Date of birth - sensitive but may need queries
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Non-sensitive fields
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors, getters, setters
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Encrypted {
}

@Component
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final SecretKey secretKey;

    public EncryptionService(@Value("${app.encryption.key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedWithIv.length - GCM_IV_LENGTH];

            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new DecryptionException("Decryption failed", e);
        }
    }

    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

@Component
public class EncryptionAttributeConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptionService.decrypt(dbData);
    }
}

// Entity listener for automatic encryption
@Component
public class EncryptionEntityListener {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptionEntityListener.encryptionService = encryptionService;
    }

    @PrePersist
    @PreUpdate
    public void encryptFields(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Encrypted.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(entity);
                    if (value != null && !value.isEmpty()) {
                        field.set(entity, encryptionService.encrypt(value));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error encrypting field: " + field.getName(), e);
                }
            }
        }
    }

    @PostLoad
    public void decryptFields(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Encrypted.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(entity);
                    if (value != null && !value.isEmpty()) {
                        field.set(entity, encryptionService.decrypt(value));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error decrypting field: " + field.getName(), e);
                }
            }
        }
    }
}
```

### Database Encryption

```java
@Configuration
public class DatabaseEncryptionConfig {

    @Bean
    @Primary
    public DataSource encryptedDataSource(@Qualifier("actualDataSource") DataSource dataSource) {
        return new EncryptedDataSourceWrapper(dataSource);
    }

    public static class EncryptedDataSourceWrapper implements DataSource {
        private final DataSource delegate;
        private final EncryptionService encryptionService;

        public EncryptedDataSourceWrapper(DataSource delegate) {
            this.delegate = delegate;
            this.encryptionService = ApplicationContextProvider.getBean(EncryptionService.class);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return new EncryptedConnectionWrapper(delegate.getConnection(), encryptionService);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return new EncryptedConnectionWrapper(delegate.getConnection(username, password), encryptionService);
        }

        // Delegate other methods...
    }

    public static class EncryptedConnectionWrapper implements Connection {
        private final Connection delegate;
        private final EncryptionService encryptionService;

        public EncryptedConnectionWrapper(Connection delegate, EncryptionService encryptionService) {
            this.delegate = delegate;
            this.encryptionService = encryptionService;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return new EncryptedPreparedStatementWrapper(
                delegate.prepareStatement(sql), encryptionService);
        }

        // Delegate other methods...
    }
}

@Service
public class TransparentDataEncryptionService {

    private final EncryptionService encryptionService;
    private final Set<String> encryptedColumns;

    public TransparentDataEncryptionService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.encryptedColumns = Set.of("ssn", "phone", "email", "address", "account_number");
    }

    public String encryptIfNeeded(String columnName, String value) {
        if (encryptedColumns.contains(columnName.toLowerCase()) && value != null) {
            return encryptionService.encrypt(value);
        }
        return value;
    }

    public String decryptIfNeeded(String columnName, String value) {
        if (encryptedColumns.contains(columnName.toLowerCase()) && value != null) {
            return encryptionService.decrypt(value);
        }
        return value;
    }
}
```

## Audit and Compliance

### Comprehensive Audit System

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "action")
    private String action;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private AuditSeverity severity;

    @Column(name = "compliance_relevant")
    private boolean complianceRelevant;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    // Constructors, getters, setters
}

@Service
@Async
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final RiskAssessmentService riskAssessmentService;

    public void logUserAction(String eventType, String userId, String action,
                             Object oldValue, Object newValue, HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .eventType(eventType)
                .userId(userId)
                .sessionId(extractSessionId(request))
                .ipAddress(extractIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .action(action)
                .oldValues(serializeObject(oldValue))
                .newValues(serializeObject(newValue))
                .timestamp(LocalDateTime.now())
                .severity(determineSeverity(eventType, action))
                .complianceRelevant(isComplianceRelevant(eventType))
                .riskScore(calculateRiskScore(eventType, userId, request))
                .build();

            auditLogRepository.save(auditLog);

            // Send to real-time monitoring if high risk
            if (auditLog.getRiskScore() > 80) {
                alertingService.sendHighRiskAlert(auditLog);
            }

        } catch (Exception e) {
            logger.error("Failed to log audit event", e);
            // Fallback to file logging
            logToFile(eventType, userId, action, e);
        }
    }

    public void logEntityChange(String entityType, String entityId, String action,
                               Object oldEntity, Object newEntity, String userId) {
        Map<String, Object> changes = calculateChanges(oldEntity, newEntity);

        if (!changes.isEmpty()) {
            AuditLog auditLog = AuditLog.builder()
                .eventType("ENTITY_CHANGE")
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .action(action)
                .oldValues(serializeObject(oldEntity))
                .newValues(serializeObject(newEntity))
                .timestamp(LocalDateTime.now())
                .severity(AuditSeverity.INFO)
                .complianceRelevant(true)
                .additionalData(serializeObject(changes))
                .build();

            auditLogRepository.save(auditLog);
        }
    }

    public void logSecurityEvent(String eventType, String userId, String description,
                                AuditSeverity severity, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
            .eventType("SECURITY_EVENT")
            .userId(userId)
            .sessionId(extractSessionId(request))
            .ipAddress(extractIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .action(eventType)
            .timestamp(LocalDateTime.now())
            .severity(severity)
            .complianceRelevant(true)
            .riskScore(calculateSecurityRiskScore(eventType, userId))
            .additionalData(description)
            .build();

        auditLogRepository.save(auditLog);

        // Immediate alerting for critical security events
        if (severity == AuditSeverity.CRITICAL) {
            alertingService.sendCriticalSecurityAlert(auditLog);
        }
    }

    public void logDataAccess(String entityType, String entityId, String action,
                             String userId, boolean authorized) {
        AuditLog auditLog = AuditLog.builder()
            .eventType("DATA_ACCESS")
            .entityType(entityType)
            .entityId(entityId)
            .userId(userId)
            .action(action)
            .timestamp(LocalDateTime.now())
            .severity(authorized ? AuditSeverity.INFO : AuditSeverity.WARNING)
            .complianceRelevant(true)
            .additionalData(authorized ? "AUTHORIZED" : "UNAUTHORIZED")
            .build();

        auditLogRepository.save(auditLog);

        if (!authorized) {
            alertingService.sendUnauthorizedAccessAlert(auditLog);
        }
    }

    private String extractSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String serializeObject(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.warn("Failed to serialize object for audit", e);
            return object.toString();
        }
    }

    private AuditSeverity determineSeverity(String eventType, String action) {
        if (eventType.contains("SECURITY") || action.contains("DELETE")) {
            return AuditSeverity.HIGH;
        } else if (action.contains("CREATE") || action.contains("UPDATE")) {
            return AuditSeverity.MEDIUM;
        }
        return AuditSeverity.INFO;
    }

    private boolean isComplianceRelevant(String eventType) {
        return Set.of("USER_LOGIN", "DATA_ACCESS", "ENTITY_CHANGE", "SECURITY_EVENT",
                     "TRANSACTION", "ACCOUNT_ACCESS").contains(eventType);
    }

    private Integer calculateRiskScore(String eventType, String userId, HttpServletRequest request) {
        return riskAssessmentService.calculateRiskScore(eventType, userId,
            extractIpAddress(request), LocalTime.now());
    }

    private Integer calculateSecurityRiskScore(String eventType, String userId) {
        // High-risk security events
        Map<String, Integer> securityEventRisks = Map.of(
            "FAILED_LOGIN", 30,
            "ACCOUNT_LOCKED", 70,
            "MFA_FAILED", 50,
            "UNAUTHORIZED_ACCESS", 90,
            "PRIVILEGE_ESCALATION", 95,
            "SUSPICIOUS_ACTIVITY", 85
        );

        return securityEventRisks.getOrDefault(eventType, 10);
    }

    private Map<String, Object> calculateChanges(Object oldEntity, Object newEntity) {
        // Implementation to calculate field-level changes
        return new HashMap<>(); // Placeholder
    }

    private void logToFile(String eventType, String userId, String action, Exception error) {
        // Fallback file logging implementation
    }
}
```

### Audit Annotations and AOP

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String eventType();
    String action();
    boolean complianceRelevant() default true;
    AuditSeverity severity() default AuditSeverity.INFO;
}

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "system";

        // Get HTTP request for additional context
        HttpServletRequest request = getCurrentRequest();

        Object oldValue = null;
        if (auditable.action().contains("UPDATE") || auditable.action().contains("DELETE")) {
            oldValue = extractOldValue(joinPoint);
        }

        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // Log the audit event
            try {
                auditService.logUserAction(
                    auditable.eventType(),
                    userId,
                    auditable.action(),
                    oldValue,
                    result,
                    request
                );

                if (exception != null) {
                    auditService.logSecurityEvent(
                        "METHOD_EXCEPTION",
                        userId,
                        "Exception in " + methodName + ": " + exception.getMessage(),
                        AuditSeverity.ERROR,
                        request
                    );
                }
            } catch (Exception auditException) {
                logger.error("Failed to audit method execution", auditException);
            }
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private Object extractOldValue(ProceedingJoinPoint joinPoint) {
        // Extract entity ID from method arguments and fetch current state
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                // Assume first Long argument is entity ID
                return fetchCurrentEntity(joinPoint.getTarget().getClass(), (Long) arg);
            }
        }
        return null;
    }

    private Object fetchCurrentEntity(Class<?> serviceClass, Long entityId) {
        // Implementation to fetch current entity state
        return null; // Placeholder
    }
}

// Usage example
@Service
public class AccountService {

    @Auditable(eventType = "ACCOUNT_ACCESS", action = "VIEW_ACCOUNT")
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    @Auditable(eventType = "ACCOUNT_MANAGEMENT", action = "CREATE_ACCOUNT", severity = AuditSeverity.MEDIUM)
    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account();
        // Set account properties
        return accountRepository.save(account);
    }

    @Auditable(eventType = "ACCOUNT_MANAGEMENT", action = "UPDATE_ACCOUNT", severity = AuditSeverity.MEDIUM)
    public Account updateAccount(Long accountId, UpdateAccountRequest request) {
        Account account = getAccount(accountId);
        // Update account properties
        return accountRepository.save(account);
    }

    @Auditable(eventType = "ACCOUNT_MANAGEMENT", action = "DELETE_ACCOUNT", severity = AuditSeverity.HIGH)
    public void deleteAccount(Long accountId) {
        Account account = getAccount(accountId);
        accountRepository.delete(account);
    }
}
```

## PCI DSS Compliance

### PCI DSS Requirements Implementation

```java
@Configuration
public class PciDssConfig {

    // Requirement 1 & 2: Install and maintain a firewall configuration
    // Requirement 3: Protect stored cardholder data
    @Bean
    public CardDataEncryptionService cardDataEncryptionService() {
        return new CardDataEncryptionService();
    }

    // Requirement 4: Encrypt transmission of cardholder data
    @Bean
    public SSLContextBuilder sslContextBuilder() {
        return SSLContextBuilder.create()
            .loadTrustMaterial(null, (chain, authType) -> true);
    }

    // Requirement 7: Restrict access to cardholder data by business need-to-know
    @Bean
    public CardDataAccessControlService cardDataAccessControlService() {
        return new CardDataAccessControlService();
    }

    // Requirement 8: Identify and authenticate access to system components
    @Bean
    public PciCompliantAuthenticationService authenticationService() {
        return new PciCompliantAuthenticationService();
    }

    // Requirement 10: Track and monitor all access to network resources and cardholder data
    @Bean
    public PciAuditService pciAuditService() {
        return new PciAuditService();
    }
}

@Service
public class CardDataEncryptionService {

    private static final String CARD_ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private final SecretKey cardDataKey;
    private final TokenizationService tokenizationService;

    public CardDataEncryptionService() {
        this.cardDataKey = generateCardDataKey();
        this.tokenizationService = new TokenizationService();
    }

    // PCI Requirement 3.4: Store PAN securely
    public String encryptCardNumber(String cardNumber) {
        validateCardNumber(cardNumber);

        try {
            // Mask all but last 4 digits for logging
            String maskedCard = maskCardNumber(cardNumber);
            auditService.logCardDataAccess("ENCRYPT", maskedCard);

            Cipher cipher = Cipher.getInstance(CARD_ENCRYPTION_ALGORITHM);
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, cardDataKey, gcmSpec);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[12 + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, 12);
            System.arraycopy(encryptedBytes, 0, encryptedWithIv, 12, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            auditService.logSecurityEvent("CARD_ENCRYPTION_FAILED", getCurrentUser(),
                "Card encryption failed", AuditSeverity.CRITICAL, getCurrentRequest());
            throw new PciComplianceException("Card data encryption failed", e);
        }
    }

    // Tokenization for PCI scope reduction
    public String tokenizeCardNumber(String cardNumber) {
        validateCardNumber(cardNumber);

        String token = tokenizationService.generateToken(cardNumber);

        // Store mapping securely (in separate secure environment)
        tokenizationService.storeTokenMapping(token, encryptCardNumber(cardNumber));

        auditService.logCardDataAccess("TOKENIZE", maskCardNumber(cardNumber));

        return token;
    }

    public String detokenizeCardNumber(String token) {
        if (!tokenizationService.isValidToken(token)) {
            throw new InvalidTokenException("Invalid card token");
        }

        String encryptedCardNumber = tokenizationService.getEncryptedCardNumber(token);
        String cardNumber = decryptCardNumber(encryptedCardNumber);

        auditService.logCardDataAccess("DETOKENIZE", maskCardNumber(cardNumber));

        return cardNumber;
    }

    private String decryptCardNumber(String encryptedCardNumber) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedCardNumber);

            byte[] iv = new byte[12];
            byte[] encryptedBytes = new byte[encryptedWithIv.length - 12];

            System.arraycopy(encryptedWithIv, 0, iv, 0, 12);
            System.arraycopy(encryptedWithIv, 12, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(CARD_ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, cardDataKey, gcmSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            auditService.logSecurityEvent("CARD_DECRYPTION_FAILED", getCurrentUser(),
                "Card decryption failed", AuditSeverity.CRITICAL, getCurrentRequest());
            throw new PciComplianceException("Card data decryption failed", e);
        }
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }

        // Remove any non-digit characters
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");

        // Validate length
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number length");
        }

        // Luhn algorithm validation
        if (!isValidLuhn(cleanCardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit % 10 + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "*".repeat(cardNumber.length() - 4) + last4;
    }

    private SecretKey generateCardDataKey() {
        // In production, this should be retrieved from a secure key management system
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES algorithm not available", e);
        }
    }
}

@Service
public class PciCompliantAuthenticationService {

    // PCI Requirement 8.2: Strong user authentication
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Enforce strong password policy
        validatePasswordPolicy(password);

        // Check for account lockout
        if (isAccountLocked(username)) {
            auditService.logSecurityEvent("LOGIN_ATTEMPT_LOCKED_ACCOUNT", username,
                "Login attempt on locked account", AuditSeverity.HIGH, getCurrentRequest());
            throw new AccountLockedException("Account is locked");
        }

        // Authenticate user
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            handleFailedLogin(username);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Check password expiration (PCI Requirement 8.2.4)
        if (isPasswordExpired(userDetails)) {
            auditService.logSecurityEvent("LOGIN_PASSWORD_EXPIRED", username,
                "Login with expired password", AuditSeverity.MEDIUM, getCurrentRequest());
            throw new CredentialsExpiredException("Password has expired");
        }

        // Successful authentication
        auditService.logUserAction("USER_LOGIN", username, "SUCCESSFUL_LOGIN",
            null, null, getCurrentRequest());

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void validatePasswordPolicy(String password) {
        // PCI Requirement 8.2.3: Password complexity
        if (password.length() < 12) {
            throw new WeakPasswordException("Password must be at least 12 characters long");
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            throw new WeakPasswordException("Password must contain uppercase, lowercase, digit, and special character");
        }

        // Check against common passwords
        if (isCommonPassword(password)) {
            throw new WeakPasswordException("Password is too common");
        }
    }

    private boolean isPasswordExpired(UserDetails userDetails) {
        if (userDetails instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) userDetails;
            LocalDateTime passwordLastChanged = securityUser.getPasswordLastChanged();

            if (passwordLastChanged == null) {
                return true; // Force change if never set
            }

            // PCI Requirement 8.2.4: Change passwords at least once every 90 days
            return passwordLastChanged.isBefore(LocalDateTime.now().minusDays(90));
        }
        return false;
    }

    private void handleFailedLogin(String username) {
        int failedAttempts = loginAttemptService.getFailedAttempts(username);
        loginAttemptService.recordFailedAttempt(username);

        auditService.logSecurityEvent("LOGIN_FAILED", username,
            "Failed login attempt #" + (failedAttempts + 1), AuditSeverity.MEDIUM, getCurrentRequest());

        // PCI Requirement 8.1.6: Limit repeated access attempts
        if (failedAttempts >= 5) {
            lockAccount(username);
            auditService.logSecurityEvent("ACCOUNT_LOCKED", username,
                "Account locked due to repeated failed login attempts", AuditSeverity.HIGH, getCurrentRequest());
        }
    }
}

@Service
public class PciAuditService extends AuditService {

    // PCI Requirement 10.2: Implement automated audit trails
    @Override
    public void logCardDataAccess(String action, String maskedCardNumber) {
        AuditLog auditLog = AuditLog.builder()
            .eventType("CARD_DATA_ACCESS")
            .action(action)
            .entityType("CARD_DATA")
            .entityId(maskedCardNumber)
            .userId(getCurrentUser())
            .sessionId(getCurrentSessionId())
            .ipAddress(getCurrentIpAddress())
            .timestamp(LocalDateTime.now())
            .severity(AuditSeverity.HIGH)
            .complianceRelevant(true)
            .riskScore(85) // High risk for card data access
            .build();

        auditLogRepository.save(auditLog);

        // Real-time monitoring for card data access
        monitoringService.sendCardDataAccessAlert(auditLog);
    }

    // PCI Requirement 10.3: Record audit trail entries
    public void generateDailyAuditReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<AuditLog> dailyAudits = auditLogRepository.findByDateAndComplianceRelevant(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay(), true);

        PciAuditReport report = PciAuditReport.builder()
            .reportDate(yesterday)
            .totalEvents(dailyAudits.size())
            .cardDataAccesses(countCardDataAccesses(dailyAudits))
            .failedLogins(countFailedLogins(dailyAudits))
            .systemAdminActions(countSystemAdminActions(dailyAudits))
            .securityViolations(countSecurityViolations(dailyAudits))
            .build();

        // Store report securely
        pciReportRepository.save(report);

        // Send to compliance team
        complianceNotificationService.sendDailyAuditReport(report);
    }

    // PCI Requirement 10.5: Secure audit trails
    @PostConstruct
    public void configureAuditSecurity() {
        // Ensure audit logs are write-only for most users
        // Implement log integrity checking
        // Configure automated backup of audit logs
    }
}
```

## Banking Domain Security Examples

### Complete Banking Security Implementation

```java
@RestController
@RequestMapping("/api/v1/banking")
@PreAuthorize("hasRole('CUSTOMER')")
public class SecureBankingController {

    private final AccountSecurityService accountSecurityService;
    private final TransactionSecurityService transactionSecurityService;
    private final CardDataEncryptionService cardDataEncryptionService;

    @GetMapping("/accounts")
    @PreAuthorize("@accountSecurityService.canViewAccounts(authentication.name)")
    @Auditable(eventType = "ACCOUNT_ACCESS", action = "LIST_ACCOUNTS")
    public ResponseEntity<List<AccountDto>> getAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<Account> accounts = accountSecurityService.getCustomerAccounts(username);

        List<AccountDto> accountDtos = accounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(accountDtos);
    }

    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("@accountSecurityService.canAccessAccount(authentication.name, #accountId)")
    @Auditable(eventType = "ACCOUNT_ACCESS", action = "VIEW_ACCOUNT_DETAILS")
    public ResponseEntity<AccountDetailDto> getAccountDetails(
            @PathVariable Long accountId,
            Authentication authentication) {

        Account account = accountSecurityService.getAccountWithSecurityCheck(accountId, authentication.getName());
        AccountDetailDto dto = convertToDetailDto(account);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/accounts/{accountId}/transfer")
    @PreAuthorize("@transactionSecurityService.canTransfer(authentication.name, #accountId, #request.amount)")
    @Auditable(eventType = "TRANSACTION", action = "INITIATE_TRANSFER", severity = AuditSeverity.HIGH)
    public ResponseEntity<TransferResponseDto> initiateTransfer(
            @PathVariable Long accountId,
            @Valid @RequestBody TransferRequestDto request,
            Authentication authentication) {

        // Additional security checks
        transactionSecurityService.validateTransferLimits(authentication.getName(), request.getAmount());
        transactionSecurityService.checkFraudRisk(accountId, request);

        TransferResult result = transactionSecurityService.executeSecureTransfer(
            accountId, request.getToAccountId(), request.getAmount(),
            request.getDescription(), authentication.getName());

        return ResponseEntity.ok(convertToTransferResponseDto(result));
    }

    @PostMapping("/cards/{cardId}/transactions")
    @PreAuthorize("@cardSecurityService.canUseCard(authentication.name, #cardId)")
    @Auditable(eventType = "CARD_TRANSACTION", action = "PROCESS_CARD_TRANSACTION", severity = AuditSeverity.HIGH)
    public ResponseEntity<CardTransactionResponseDto> processCardTransaction(
            @PathVariable Long cardId,
            @Valid @RequestBody CardTransactionRequestDto request,
            Authentication authentication) {

        // PCI DSS compliant card processing
        Card card = cardSecurityService.getCardWithSecurityCheck(cardId, authentication.getName());

        // Validate transaction limits
        cardSecurityService.validateTransactionLimits(card, request.getAmount());

        // Process transaction securely
        CardTransactionResult result = cardSecurityService.processSecureTransaction(card, request);

        return ResponseEntity.ok(convertToCardTransactionResponseDto(result));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @PreAuthorize("@accountSecurityService.canAccessAccount(authentication.name, #accountId)")
    @Auditable(eventType = "TRANSACTION_HISTORY", action = "VIEW_TRANSACTION_HISTORY")
    public ResponseEntity<Page<TransactionDto>> getTransactionHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication) {

        // Validate date range (limit to prevent data exposure)
        if (fromDate != null && toDate != null) {
            long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
            if (daysBetween > 365) {
                throw new SecurityException("Date range cannot exceed 365 days");
            }
        }

        Page<Transaction> transactions = transactionSecurityService.getSecureTransactionHistory(
            accountId, authentication.getName(), PageRequest.of(page, size), fromDate, toDate);

        Page<TransactionDto> transactionDtos = transactions.map(this::convertToTransactionDto);

        return ResponseEntity.ok(transactionDtos);
    }

    @PostMapping("/accounts/{accountId}/cards")
    @PreAuthorize("@accountSecurityService.canManageCards(authentication.name, #accountId)")
    @Auditable(eventType = "CARD_MANAGEMENT", action = "REQUEST_NEW_CARD", severity = AuditSeverity.MEDIUM)
    public ResponseEntity<CardRequestResponseDto> requestNewCard(
            @PathVariable Long accountId,
            @Valid @RequestBody CardRequestDto request,
            Authentication authentication) {

        // Validate card request
        cardSecurityService.validateCardRequest(accountId, request, authentication.getName());

        // Process card request securely
        CardRequestResult result = cardSecurityService.processCardRequest(accountId, request);

        return ResponseEntity.ok(convertToCardRequestResponseDto(result));
    }

    @PostMapping("/accounts/{accountId}/lock")
    @PreAuthorize("@accountSecurityService.canLockAccount(authentication.name, #accountId)")
    @Auditable(eventType = "ACCOUNT_SECURITY", action = "LOCK_ACCOUNT", severity = AuditSeverity.HIGH)
    public ResponseEntity<AccountLockResponseDto> lockAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountLockRequestDto request,
            Authentication authentication) {

        AccountLockResult result = accountSecurityService.lockAccount(
            accountId, request.getReason(), authentication.getName());

        return ResponseEntity.ok(convertToAccountLockResponseDto(result));
    }

    @PostMapping("/alerts/suspicious-activity")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Auditable(eventType = "SECURITY_ALERT", action = "REPORT_SUSPICIOUS_ACTIVITY", severity = AuditSeverity.HIGH)
    public ResponseEntity<SuspiciousActivityResponseDto> reportSuspiciousActivity(
            @Valid @RequestBody SuspiciousActivityReportDto request,
            Authentication authentication) {

        SuspiciousActivityResult result = securityAlertService.reportSuspiciousActivity(
            request, authentication.getName());

        return ResponseEntity.ok(convertToSuspiciousActivityResponseDto(result));
    }
}

@Service
public class FraudDetectionService {

    private final MachineLearningFraudModel fraudModel;
    private final RiskScoringService riskScoringService;
    private final TransactionPatternAnalyzer patternAnalyzer;

    public FraudRiskAssessment assessTransactionRisk(TransactionRequest request, String userId) {
        // Real-time fraud detection
        Map<String, Object> features = extractTransactionFeatures(request, userId);
        double fraudScore = fraudModel.predict(features);

        // Pattern analysis
        boolean suspiciousPattern = patternAnalyzer.isSuspiciousPattern(request, userId);

        // Velocity checks
        boolean velocityViolation = checkVelocityRules(request, userId);

        // Geographic analysis
        boolean geographicAnomaly = checkGeographicAnomaly(request, userId);

        FraudRiskLevel riskLevel = calculateRiskLevel(fraudScore, suspiciousPattern,
            velocityViolation, geographicAnomaly);

        FraudRiskAssessment assessment = FraudRiskAssessment.builder()
            .fraudScore(fraudScore)
            .riskLevel(riskLevel)
            .suspiciousPattern(suspiciousPattern)
            .velocityViolation(velocityViolation)
            .geographicAnomaly(geographicAnomaly)
            .recommendedAction(getRecommendedAction(riskLevel))
            .build();

        // Log fraud assessment
        auditService.logFraudAssessment(request, userId, assessment);

        return assessment;
    }

    private Map<String, Object> extractTransactionFeatures(TransactionRequest request, String userId) {
        Map<String, Object> features = new HashMap<>();

        // Transaction features
        features.put("amount", request.getAmount().doubleValue());
        features.put("transaction_time", getTimeOfDayFeature(LocalTime.now()));
        features.put("transaction_day", LocalDate.now().getDayOfWeek().getValue());

        // User features
        UserProfile userProfile = getUserProfile(userId);
        features.put("user_age", userProfile.getAge());
        features.put("account_age_days", userProfile.getAccountAgeDays());
        features.put("avg_transaction_amount", userProfile.getAverageTransactionAmount());

        // Historical features
        features.put("transactions_last_hour", getTransactionCount(userId, Duration.ofHours(1)));
        features.put("transactions_last_day", getTransactionCount(userId, Duration.ofDays(1)));
        features.put("amount_last_day", getTransactionAmount(userId, Duration.ofDays(1)));

        return features;
    }

    private FraudRiskLevel calculateRiskLevel(double fraudScore, boolean suspiciousPattern,
                                            boolean velocityViolation, boolean geographicAnomaly) {
        if (fraudScore > 0.8 || (suspiciousPattern && velocityViolation)) {
            return FraudRiskLevel.HIGH;
        } else if (fraudScore > 0.6 || velocityViolation || geographicAnomaly) {
            return FraudRiskLevel.MEDIUM;
        } else if (fraudScore > 0.3 || suspiciousPattern) {
            return FraudRiskLevel.LOW;
        } else {
            return FraudRiskLevel.MINIMAL;
        }
    }

    private FraudAction getRecommendedAction(FraudRiskLevel riskLevel) {
        switch (riskLevel) {
            case HIGH:
                return FraudAction.BLOCK_TRANSACTION;
            case MEDIUM:
                return FraudAction.REQUIRE_ADDITIONAL_AUTH;
            case LOW:
                return FraudAction.MONITOR_CLOSELY;
            default:
                return FraudAction.ALLOW;
        }
    }
}
```

This comprehensive guide covers advanced security and compliance implementation in Spring Boot applications, with specific focus on banking and financial services requirements. The examples demonstrate real-world security patterns, compliance controls, and best practices for building secure, compliant enterprise applications.

## Interview Questions

### Beginner Level

**Q1: What are the main components of Spring Security?**

A: Main components include:
- **SecurityContext**: Holds security information for current thread
- **Authentication**: Represents authenticated principal
- **GrantedAuthority**: Represents permissions granted to principal
- **UserDetails**: Core user information
- **SecurityFilterChain**: Chain of security filters
- **AuthenticationManager**: Processes authentication requests

**Q2: How does method-level security work in Spring?**

A: Method-level security uses annotations:
- `@PreAuthorize`: Check before method execution
- `@PostAuthorize`: Check after method execution
- `@Secured`: Simple role-based access
- `@RolesAllowed`: JSR-250 standard
- Uses SpEL expressions for complex authorization logic

### Intermediate Level

**Q3: Explain OAuth2 authorization flows.**

A: Main OAuth2 flows:
- **Authorization Code**: For web applications with backend
- **Implicit**: For SPAs (deprecated)
- **Client Credentials**: For service-to-service
- **Resource Owner Password**: For trusted applications
- **Authorization Code with PKCE**: For mobile/SPA apps

**Q4: How do you implement field-level encryption?**

A: Field-level encryption approaches:
- JPA attribute converters
- Entity listeners with encryption
- Custom encryption annotations
- Transparent data encryption at database level

### Advanced Level

**Q5: How would you design a PCI DSS compliant payment system?**

A: Key design elements:
- Card data tokenization
- End-to-end encryption
- Network segmentation
- Access controls and monitoring
- Secure key management
- Comprehensive audit logging
- Regular security testing

**Q6: Explain your approach to fraud detection in real-time payments.**

A: Fraud detection strategy:
- Machine learning models for real-time scoring
- Rule-based velocity checks
- Pattern analysis and anomaly detection
- Geographic and behavioral analysis
- Risk-based authentication
- Real-time monitoring and alerting