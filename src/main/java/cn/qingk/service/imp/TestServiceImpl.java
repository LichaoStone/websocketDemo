package cn.qingk.service.imp;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.qingk.dao.UserMapper;
import cn.qingk.entity.User;
import cn.qingk.service.TestService;

@Service
public class TestServiceImpl implements TestService {
    public static final Logger logger = Logger.getLogger(TestServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Transactional
    public String listUsers() throws Exception {
    	logger.info("testQuery--start");
        List<User> users = userMapper.listUsers();
        logger.info("users--"+users);
//        User u1 = new User("zhangsan", 18);
//        User u2 = new User("lisi", 19);
//        User u3 = new User("wangwu", 12);
//        users.add(u1);
//        users.add(u2);
//        users.add(u3);
//        List<User> users = new ArrayList<User>();
        String res = "";
        if (users != null && users.size() > 0) {
            for (User user : users) {
                res += user.toString() + "|";
            }
        } else {
            res = "Not found.";
        }
        
        logger.info("testQuery--end:"+res);
        return res;
    }
}
