package com.example.fusen.dao;

import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.entity.Bookmark;

@Dao
@ConfigAutowireable
public interface BookmarkDao {
  @Select
  Optional<Bookmark> findById(Long id);

  @Select
  Optional<Bookmark> findByUrl(String url);

  @Insert
  @Transactional
  int insert(Bookmark bookmark);

  @Update
  @Transactional
  int update(Bookmark bookmark);

  @Delete
  @Transactional
  int delete(Bookmark bookmark);

  @Select
  boolean existsByUrl(String url);

  @Select
  List<Bookmark> findAll(SelectOptions options);
}
