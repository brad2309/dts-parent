package com.dts.c;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserAccountDAO {

	@Options(useGeneratedKeys=true,keyProperty="id")
	@Insert("insert into t_user_account (user_id,total) values (#{userId},#{total})")
	int insert(UserAccount record);
	
	@Delete("delete from t_user_account where id=#{id}")
	int delete(Integer id);
	
}
