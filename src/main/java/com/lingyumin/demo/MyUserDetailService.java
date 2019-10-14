package com.lingyumin.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * [简要描述]:
 * [详细描述]:
 *
 * @author:
 * @date: 3:44 PM 2019/10/14
 * @since: JDK 1.8
 */
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
