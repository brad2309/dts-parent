package com.dts.b;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserExtDAO {


	@Options(useGeneratedKeys=true,keyProperty="id")
	@Insert("insert into t_user_ext (user_id,nickname,gendar) values (#{userId},#{nickname},#{gendar})")
	int insert(UserExt record);
	
	@Delete("delete from t_user_ext where id=#{id}")
	int delete(Integer id);
	
}
