package com.mmall.dao;

import com.mmall.pojo.Cart;
//购物车接口
public interface CartMapper {
    //根据主键删除
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);
}