package com.dts.b;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserDAO {

	@Options(useGeneratedKeys=true,keyProperty="id")
	@Insert("insert into t_user (username,password,create_time) values (#{username},#{password},#{createTime})")
	int insert(User record);
	
	@Delete("delete from t_user where id=#{id}")
	int delete(Integer id);
	
}
