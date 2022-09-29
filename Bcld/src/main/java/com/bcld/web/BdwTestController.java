package com.bcld.web;

import java.awt.image.BufferedImage;





import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.bcld.domain.Role;
import com.bcld.domain.User;
import com.bcld.domain.UserRole;
import com.bcld.domain.example.UserExample;
import com.bcld.persistence.UserMapper;
import com.bcld.service.AuthorityCertificationService;
import com.bcld.service.UserService;
import com.bcld.utils.Globals;
import com.bcld.utils.MvcUtils;



@Controller
public class BdwTestController {

    private static Logger log = LoggerFactory.getLogger(BdwTestController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private AuthorityCertificationService authorityCertificationService;
    
    //1 登陆页面
    @RequestMapping("/bdw/login")
    public String login() {
        return "/bdw/index";
    }
    
    //2 首页
    @RequestMapping("/bdw/start")
    public String start() throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        List<User> listUser=userService.selectByExample(user);
        return "/bdw/start";
    }
    
    @RequestMapping("/rbac/right_list")
    public String listRight(Model model) {
        User user = MvcUtils.getCurrentUser();
        String userId = user.getId();
       
        boolean authority = authorityCertificationService.authority("模块名称XXX", userId);
        if (!authority) {
            return "authority";//该userId用户无权访问该URL连接，跳转到的提示页面
        }
        model.addAttribute("rights","xxx");
        return "/common";
    }
    
    
    @RequestMapping("/bdw/list")
    public String listPageUser(User user, String pageNumber, BindingResult result, Model model) {
        List<User> listUser=userService.selectByExample(user);
        model.addAttribute("users", listUser);
        model.addAttribute("test", "balabala");
        System.out.println("123");
        return "/bdw/user_list";
    }
    
    //提供给Android app 调试接口使用，上 传一个对象返回另外一个对象
    @RequestMapping("/bdw/test")
    public String testInsertAsset() throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        List<User> listUser=userService.selectByExample(user);
        return "/bdw/user_list";
    }
    
   
    
    @RequestMapping("/bdw/create")
    public String test1() throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        List<User> listUser=userService.selectByExample(user);
        return "/bdw/user_list";
    }
    @RequestMapping("/bdw/remove")
    public String test2(String userId) throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        user.setId(userId);
        UserExample userExample=new UserExample();
        userExample.createCriteria().andIdEqualTo(userId);
        userService.deleteByExample(userExample);
        return "/bdw/user_list";
    }
    @RequestMapping("/bdw/update")
    public String test3() throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        List<User> listUser=userService.selectByExample(user);
        return "/bdw/user_list";
    }
    @RequestMapping("/bdw/query")
    public String test4() throws JsonGenerationException, JsonMappingException, IOException {
        User user=new User();
        List<User> listUser=userService.selectByExample(user);
        return "/bdw/user_list";
    }

}
