package com.dts.c;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserLogDAO {
	

	@Options(useGeneratedKeys=true,keyProperty="id")
	@Insert("insert into t_user_log (user_id,log) values (#{userId},#{log})")
	int insert(UserLog record);
	
	@Delete("delete from t_user_log where id=#{id}")
	int delete(Integer id);

}
