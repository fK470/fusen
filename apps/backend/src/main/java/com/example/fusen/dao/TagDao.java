package com.example.fusen.dao;

import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.entity.Tag;

@Dao
@ConfigAutowireable
public interface TagDao {
  @Select
  Optional<Tag> findByName(String name);

  @Insert
  @Transactional
  int insert(Tag tag);
}
