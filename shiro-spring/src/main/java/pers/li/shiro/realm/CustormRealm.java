package pers.li.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import pers.li.dao.UserDao;
import pers.li.entity.User;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * create by lishengbo on 2018-05-31 09:50
 *
 * 自定义 RealM 实现
 */
@SuppressWarnings("all")
public class CustormRealm extends AuthorizingRealm {


    @Resource
    private UserDao userDao;

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        //1.获取主体传过来的认证信息--用户名
        String userName=(String)principalCollection.getPrimaryPrincipal();
        //2.通过用户名到数据库/缓存中获取角色信息--模拟数据库查询
        Set<String> roles=getRolesBYUserName(userName);
        //3.权限
        Set<String> perss=getPermissionBYUserName(userName);
        //4.创建返回信息
        SimpleAuthorizationInfo simpleAuthorizationInfo=new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.setRoles(roles);
        simpleAuthorizationInfo.setStringPermissions(perss);
        return simpleAuthorizationInfo;
    }

    /**
     * 获取权限信息
     * @param userName
     * @return
     */
    private Set<String> getPermissionBYUserName(String userName) {
        List<String> roles =userDao.getRolesBYUserName(userName);
        List<String> pers =userDao.getPermissionBYUserName(roles.get(0));
        Set<String> strings = new HashSet<String>(pers);
        return strings;
    }

    /**
     *  获取角色信息
     * @param userName
     * @return
     */
    private Set<String> getRolesBYUserName(String userName) {
        System.out.println("从数据库中获取授权数据---");
        List<String> roles =userDao.getRolesBYUserName(userName);
        Set<String> strings = new HashSet<String>(roles);
        return strings;

    }

    /**
     * 认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //1.获取主体传过来的认证信息--用户名
        String userName=(String)authenticationToken.getPrincipal();

        //2.通过用户名到数据库中获取凭证--模拟数据库查询
        String passWord=getPassBYUserName(userName);
        if(passWord==null){
            return null;
        }
        //创建返回信息
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(userName,passWord,"customRealm");

        //盐的设置 TODO
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes("Mark"));

        return authenticationInfo;
    }
    /**
     * 数据库查询--认证信息
     * @param userName
     * @return
     */
    private String getPassBYUserName(String userName) {
        User user=userDao.getPassBYUserName(userName);
        if(user!=null){
            return user.getPassword();
        }

        return null;
    }

    public static void main(String[] args) {

        //md5加密计算--无盐
        System.out.println(new Md5Hash("123456"));
        //md5+盐
        System.out.println(new Md5Hash("123456","Mark"));


    }
}
