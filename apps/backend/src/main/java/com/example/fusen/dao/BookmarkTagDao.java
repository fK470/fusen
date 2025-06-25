package com.example.fusen.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.entity.Tag;

@Dao
@ConfigAutowireable
public interface BookmarkTagDao {
  @Select
  List<Tag> findTagsByBookmarkId(Long bookmarkId);

  @Insert
  @Transactional
  int insertBookmarkTag(Long bookmarkId, Long tagId);

  @Delete
  @Transactional
  int deleteByBookmarkId(Long bookmarkId);
}