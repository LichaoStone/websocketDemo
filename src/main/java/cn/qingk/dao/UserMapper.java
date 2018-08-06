package cn.qingk.dao;


import java.util.List;
import cn.qingk.entity.User;

public interface UserMapper {
    public List<User> listUsers() throws Exception;
}
