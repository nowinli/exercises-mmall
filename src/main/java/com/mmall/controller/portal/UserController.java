package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
	@Autowired
	private IUserService iUserService;
	//用户登录
	@RequestMapping(value = "login.do",method = RequestMethod.POST)
	//responsebody注解表示返回时自动使用SpringMVC Jackson插件将返回值序列化为json
	//将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML
	//等同于response.getWriter.write(JSONObject.fromObject(user).toString());
	//它的配置在dispatcher-servlet.xml中
	@ResponseBody
	//登录
	public ServerResponse<User> login(String username, String password, HttpSession session){
		ServerResponse<User> response = iUserService.login(username, password);
		if (response.isSuccess()){
			session.setAttribute(Const.CURRENT_USER,response.getData());
		}
		return response;
	}
	//退出
	@RequestMapping(value = "logout.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> logout(HttpSession session){
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccess();
	}
	//注册
	@RequestMapping(value = "register.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user){
		return iUserService.register(user);
	}

	@RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
	@ResponseBody
	//防止恶意用户通过接口调用注册接口，每次输入下一个信息框后台都进行检验反馈给前台
	public ServerResponse<String> checkValid(String str,String type){
		return iUserService.checkValid(str,type);
	}

	@RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user != null){
			return ServerResponse.createBySuccess(user);
		}
		return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
	}

	@RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse forgetGetQuestion(String username){
		return iUserService.selectQuestion(username);
	}

	@RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
		return iUserService.checkAnswer(username,question,answer);
	}

	@RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
		return iUserService.forgetRestPassword(username,passwordNew,forgetToken);
	}

	@RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
	@ResponseBody
	//登录状态充值密码
	public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null){
			return ServerResponse.createByErrorMessage("用户未登录");
		}
		return iUserService.resetPassword(passwordOld,passwordNew,user);
	}

	@RequestMapping(value = "update_information.do",method = RequestMethod.POST)
	@ResponseBody
	//登录状态下更新用户信息
	public ServerResponse<User> update_information(HttpSession session,User user){
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if (currentUser == null){
			return ServerResponse.createByErrorMessage("用户未登录");
		}
		//防止ID变化导致横向越权
		user.setId(currentUser.getId());
		user.setUsername(currentUser.getUsername());
		ServerResponse<User> response = iUserService.updateInformation(user);
		if (response.isSuccess()){
			response.getData().setUsername(currentUser.getUsername());
			session.setAttribute(Const.CURRENT_USER,response.getData());
		}
		return response;
	}

	@RequestMapping(value = "get_information.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> get_information(HttpSession session){
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if (currentUser == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登录status=10");
		}
		return iUserService.getInformation(currentUser.getId());
	}
}
