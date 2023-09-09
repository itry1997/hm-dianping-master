package com.hmdp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hmdp.utils.RedisConstants.LOGIN_CODE_KEY;

/**
 * @author cui
 * @create 2023--05--06--10:12
 */
@Slf4j
@SpringBootTest
public class TokenTest {
    @Resource
    IUserService userService;
    @Resource
    HttpSession session;
    @Resource
    UserMapper userMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    LoginFormDTO loginFormDTO=new LoginFormDTO();

    @Test
    public void getToken(){
        QueryWrapper qw = new QueryWrapper();
        List<User> list = userMapper.selectList(qw);
        List<String> phoneList = new ArrayList<>();
        list.forEach(user->phoneList.add(user.getPhone()));

        for (String phone : phoneList){
            userService.sendCode(phone,session);
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            loginFormDTO.setCode(cacheCode);
            loginFormDTO.setPhone(phone);
            userService.login(loginFormDTO,session);
        }
    }

    @Test
    public void getKey(){
        List<String> list = new ArrayList<>();
        Set<String> keys = stringRedisTemplate.keys("login:token:*");
        for(String key:keys){
            String keyOne = key.substring(12);
            System.out.println(keyOne);
        }
    }
}
