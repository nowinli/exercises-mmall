package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
	@Autowired
	private UserMapper userMapper;

	@Override
	public ServerResponse<User> login(String username, String password) {
		int resultCount = userMapper.checkUsername(username);
		if (resultCount == 0){
		return ServerResponse.createByErrorMessage("用户名不存在");
	}
		/// TODO: 2018/10/11  /密码登录MD5(加密)
		String md5Password = MD5Util.MD5EncodeUtf8(password);
		User user = userMapper.selectLogin(username, md5Password);
		if (user == null){
			return ServerResponse.createByErrorMessage("密码错误");
		}
		//登录成功则将返回信息中的密码置为空
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess("登录成功",user);
	}

	//注册
	public ServerResponse<String> register(User user){
		ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
		if (!validResponse.isSuccess()){
			return validResponse;
		}
		validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
		if (!validResponse.isSuccess()){
			return validResponse;
		}
		//设置角色为普通用户
		user.setRole(Const.Role.ROLE_CUSTOMER);
		//MD5加密
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		int resultCount = userMapper.insert(user);
		if (resultCount == 0){
			return ServerResponse.createByErrorMessage("注册失败");
		}
		return ServerResponse.createBySuccessMessage("注册成功");
	}

	//防止恶意用户通过接口调用注册接口，每次输入下一个信息框后台都进行检验反馈给前台
	public ServerResponse<String> checkValid(String str,String type){
		//isNotBlank中" "返回flase，isNotEmpty中" "返回true
		if (StringUtils.isNotBlank(type)){
			//开始校验
			if (Const.USERNAME.equals(type)){
				int resultCount = userMapper.checkUsername(str);
				if (resultCount > 0){
					return ServerResponse.createByErrorMessage("用户名已存在");
				}
			}
			if (Const.EMAIL.equals(type)){
				int resultCount = userMapper.checkEmail(str);
				if (resultCount > 0){
					return ServerResponse.createByErrorMessage("email已存在");
				}
			}
		}else {
			return  ServerResponse.createByErrorMessage("参数错误");
		}
		return ServerResponse.createBySuccessMessage("校验成功");
	}

	//忘记密码选择问题
	public ServerResponse selectQuestion(String username){
		ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
		if (validResponse.isSuccess()){
			//用户不存在
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String question = userMapper.selectQuestionByUsername(username);
		if (StringUtils.isNotBlank(question)){
			return ServerResponse.createBySuccess(question);
		}
		return ServerResponse.createByErrorMessage("找回的密码的问题是空的");
	}

	public ServerResponse<String> checkAnswer(String username,String question,String answer){
		int resultCount = userMapper.checkAnswer(username,question,answer);
		if (resultCount>0){
			//说明问题以及问题答案是这个用户打得并且是正确的
			String forgetToken = UUID.randomUUID().toString();
			TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
			return ServerResponse.createBySuccess(forgetToken);
		}
		return ServerResponse.createByErrorMessage("问题答案错误");
	}

	public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
		if (StringUtils.isBlank(forgetToken)){
			return ServerResponse.createByErrorMessage("参数错误,token需要传递");
		}
		ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
		if (validResponse.isSuccess()){
			//用户不存在
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		//防止被恶意调用接口，假如没有taken那么就可以修改任意用户密码。（经典的横向越权）
		String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
		if (StringUtils.isBlank(token)){
			return ServerResponse.createByErrorMessage("token无效或者过期");
		}
		if (StringUtils.equals(forgetToken,token)){
			String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
			int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
			if (rowCount > 0){
				return ServerResponse.createBySuccessMessage("修改密码成功");
			}
		}else {
			return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的TOKEN");
		}
		return ServerResponse.createByErrorMessage("修改密码失败");
	}

	public ServerResponse<String> resetPassword(String passwordOld, String passwordNew,User user){
		//防止横向越权因为有很大可能密码一样所以需要用户ID和密码一起校验
		int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
		if (resultCount == 0){
			return ServerResponse.createByErrorMessage("旧密码错误");
		}

		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int updateCount = userMapper.updateByPrimaryKeySelective(user);
		if (updateCount > 0){
			return ServerResponse.createBySuccessMessage("密码更新成功");
		}
		return ServerResponse.createByErrorMessage("密码更新失败");
	}

	public ServerResponse<User> updateInformation(User user){
		//username是不能被更新的
		//email也要校验，校验新的是否存在，并且存在的email如果相同的话，不能是当前用户的
		int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
		if (resultCount > 0){
			return ServerResponse.createByErrorMessage("email已存在");
		}
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPassword(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());

		int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
		if (updateCount > 0){
			return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
		}
		return ServerResponse.createByErrorMessage("更新个人信息失败");
	}

	public ServerResponse<User> getInformation(Integer userId){
		User user = userMapper.selectByPrimaryKey(userId);
		if (user == null){
			return ServerResponse.createByErrorMessage("找不到当前用户");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess(user);
	}

	//后台
	/**
	 * 校验是否是管理员
	 */
	public ServerResponse checkAdminRole(User user){
		if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
			return ServerResponse.createBySuccess();
		}
		return ServerResponse.createByError();
	}
}
