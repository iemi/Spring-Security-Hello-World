#概述
Web系统中的认证和权限服务是现在互联网服务中不可或缺的一部分，目前主流的安全框架无非就是Apache Shiro和Spring Security。Apache Shiro简单易用是一大优势，而Spring Security则功能更为强大且能够配合Spring使用。
***
#Spring Security的hello world
先声明本文意在阐明Spring Security的基本原理，所以代码没有持久层部分（数据库操作）。
#####Web项目搭建
先利用SpringBoot搭建一个简单的Web服务并且加入Spring Security依赖
```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
写一个简单的controller
```
@RestController
public class HelloController {
    @RequestMapping("hello")
    public String hello(){
        return "hello spring security";
    }
}
```
咋们尝试访问这个接口localhost:8080/hello，不能直接访问了，而需要表单登录
![登录页面](https://upload-images.jianshu.io/upload_images/19628259-db31a72bbf93e8ac.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
在加入Spring Security后就有一个默认的安全配置，所有服务器资源的访问都需要经过表单登录认证。并且会生成一个用户名user的凭证，密码是程序启动时控制台输出的一串字符
![控制台输出](https://upload-images.jianshu.io/upload_images/19628259-3debec4bdc8bc519.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#####加入自定义安全配置
自定义安全配置类，继承WebSecurityConfigurerAdapter
```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter{
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .formLogin()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

}
```
http.formLogin().and().authorizeRequests().anyRequest().authenticated()，指定了默认的验证方式是表单登录并且任何请求都需要经过认证。Spring Security早期的版本默认指定的认证方式是HttpBasic的认证方式，现在的默认就是表单登录验证。所以如果使用了最新版本的Spring Security，根据上面的配置和之前不加入配置并没有变化
#####Spring Security工作原理
Spring Security的核心就是过滤器链，在访问服务器资源时请求需要经过过滤器链才能获取资源
![Spring Security过滤器链](https://upload-images.jianshu.io/upload_images/19628259-df0618068153b6f3.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
图中绿色的过滤器是负责认证的过滤器，比如HttpBasic和表单认证；橙色过滤器过滤器链的最后一关，它判断请求用户是否认证和拥有权限，没通过会抛出异常；蓝色是异常处理过滤器，它对橙色过滤器抛出的异常进行处理。其中绿色的过滤器可以根据配置决定是否生效和顺序，蓝色和橙色是固定顺序和一定生效的
***
#Spring Security自定义认证逻辑
#####处理用户信息获取逻辑和密码加密解密处理
上面提到Spring Security默认会生成一个用户名user的凭证并且提供密码，但是我们的实际需求肯定不是这样的，所以我们自定义处理用户信息获取逻辑。我们通过自定义用户信息获取类，它实现UserDetailsService接口
```
@Component
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        /**
         * 这里实际情况应该是根据参数s查询数据库用户数据
         */
        return new User("admin",bCryptPasswordEncoder.encode("123"), AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
    }
}
```
上面并没有对数据库的真实查询操作，只是模拟而已。loadUserByUsername方法返回一个Spring Security提供的User类（这个类可以是一个自定义的User类，后面会说）。然后Spring Security配置类中重写另一个configure方法对userDetailsService进行配置，配置成我们自定义的用户信息获取类（auth.userDetailsService(myUserDetailService)指定了自定义处理用户信息获取的类），并且初始化一个BCryptPasswordEncoder的编码器
```
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private MyUserDetailService myUserDetailService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .formLogin()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailService);
        super.configure(auth);
    }
}
```
重新启动服务尝试访问http://localhost:8080/hello，输入用户名admin，密码123
![访问成功](https://upload-images.jianshu.io/upload_images/19628259-7ed37707d2ddbdeb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#####处理用户检验逻辑
在实际需求中，我们可能需要判断用户过期、用户锁定、凭证过期、用户可用等问题。Spring Security提供的User类可能并不能解决我们的需求，自定义一个User实现UserDetails
```
public class User implements UserDetails{
    private Integer id;
    private String username;
    private String password;
    private Integer age;
    private Boolean isAccountNonExpired;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getAccountNonExpired() {
        return isAccountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        isAccountNonExpired = accountNonExpired;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.username;
    }

    @Override
    public String getUsername() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
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
        return false;
    }
}
```
其中包括一个用户的一些基本字段，通过重写isAccountNonExpired、isAccountNonLocked、isCredentialsNonExpired、isEnabled自定义用户校验逻辑。比如重写isAccountNonExpired根据isAccountNonExpired字段的布尔值判断是否用户过期。修改MyUserDetailService的loadUserByUsername
```
@Component
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        /**
         * 这里实际情况应该是根据参数s查询数据库用户数据
         */
        User user = new User();
        user.setId(1);
        user.setUsername("admin");
        user.setPassword("123");
        user.setAge(21);
        user.setAccountNonExpired(false);
        return user;
        //return new User("admin",bCryptPasswordEncoder.encode("123"), AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
    }
}
```
重新启动服务，尝试访问http://localhost:8080/hello，输入admin、123，校验结果是用户被锁定
![用户锁定](https://upload-images.jianshu.io/upload_images/19628259-eceb97d9d8c7fc3b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
***
#总结
* Spring Security的Hello World
* Spring Security的工作原理
* Spring Security自定义认证逻辑


Github仓库地址



