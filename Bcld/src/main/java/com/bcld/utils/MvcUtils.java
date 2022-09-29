package com.bcld.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.bcld.domain.User;
import com.bcld.domain.logic.RowBounds;

public class MvcUtils {

    public static void setErrors(BindingResult result, Model model) {
        Map<String, String> map = new HashMap<String, String>();

        for (Iterator<FieldError> iterator = result.getFieldErrors().iterator(); iterator.hasNext();) {
            FieldError fieldError = (FieldError) iterator.next();
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        model.addAttribute("errors", map);
    }

    /**
     * @param pageNumber
     * @param userCount
     * @param model
     * @return
     */
    public static RowBounds countPage(String pageNumber, int userCount, Model model) {
        int pageCount = userCount / Globals.PAGESIZE;
        if (userCount % Globals.PAGESIZE != 0 || pageCount == 0) {
            pageCount = pageCount + 1;
        }
        int number = NumberUtils.toInt(pageNumber, 1);
        if(number < 1){
            number = 1;
        }
        number = Math.min(number, pageCount);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("pageNumber", number);
        return new RowBounds(number);
    }

    /**
     * 获取当前用户信息
     * 
     * @return
     */
    public static User getCurrentUser() {
        Subject currentUser = SecurityUtils.getSubject();
        return (User) currentUser.getPrincipal();
    }

    /**
     * 获取当前用户名
     * 
     * @return
     */
    public static String getCurrentUserName() {
        return getCurrentUser().getUserName();
    }

    /**
     * 获取当前会话IP
     * 
     * @return
     */
    public static String getCurrentUserIp() {
        return SecurityUtils.getSubject().getSession().getHost();
    }

    /**
     * 判断当前用户是否有某个权限
     * 
     * @param permission
     * @return
     */
    public static boolean hasPermission(String permission) {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isPermitted(permission);
    }
}
