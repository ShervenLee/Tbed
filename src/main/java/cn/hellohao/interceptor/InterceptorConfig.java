package cn.hellohao.interceptor;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.hellohao.pojo.User;
import cn.hellohao.service.impl.NOSImageupload;
import cn.hellohao.service.impl.UserServiceImpl;
import cn.hellohao.utils.Base64Encryption;
import cn.hellohao.utils.Print;
import cn.hellohao.utils.SpringContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
public class InterceptorConfig implements HandlerInterceptor {
    //private static final Logger log = LoggerFactory.getLogger(InterceptorConfig.class);

    /**
     * 进入controller层之前拦截请求
     */
    //这个方法是在访问接口之前执行的，我们只需要在这里写验证登陆状态的业务逻辑，就可以在用户调用指定接口之前验证登陆状态了
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserServiceImpl userService = SpringContextHolder.getBean(UserServiceImpl.class);
            HttpSession session = request.getSession();
        //这里的User是登陆时放入session的
        User user = (User) session.getAttribute("user");
        if(user==null){
            Cookie[] cookies = request.getCookies();
            String Hellohao_UniqueUserKey = "";
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("Hellohao_UniqueUserKey") && Hellohao_UniqueUserKey.equals("")){
                    Hellohao_UniqueUserKey = URLDecoder.decode(cookie.getValue(), "GBK");
                }
//                if(cookie.getName().equals("pass_hellohaobycookie") && pass.equals("")){
//                    pass =URLDecoder.decode(cookie.getValue(), "GBK");
//                }
            }

            if(Hellohao_UniqueUserKey!=null && !Hellohao_UniqueUserKey.equals("")){
                //String basepass = Base64Encryption.encryptBASE64(pass.getBytes());
                Integer ret = userService.login(null, null,Hellohao_UniqueUserKey);
                if (ret > 0) {
                    User u = userService.getUsersMail(Hellohao_UniqueUserKey);
                    if (u.getIsok() == 1) {
                        session.setAttribute("user", u);
                        session.setAttribute("email", u.getEmail());
                        //request.getRequestDispatcher("/admin/goadmin").forward(request, response);
                    }
                }
            }

        }
        User suser = (User) session.getAttribute("user");
            String email = null;
            Integer level = 0;
            if (suser != null) {
                email = suser.getEmail();
                level = suser.getLevel();
            }
            if (email == null) {
                //这个方法返回false表示忽略当前请求，如果一个用户调用了需要登陆才能使用的接口，如果他没有登陆这里会直接忽略掉
                //当然你可以利用response给用户返回一些提示信息，告诉他没登陆
                System.out.println("没有登录权限");
                request.getRequestDispatcher("/err").forward(request, response);
                return false;
            } else {
                //System.out.println("进入成功");
                return true;    //如果session里有user，表示该用户已经登陆，放行，用户即可继续调用自己需要的接口
            }
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }

}
