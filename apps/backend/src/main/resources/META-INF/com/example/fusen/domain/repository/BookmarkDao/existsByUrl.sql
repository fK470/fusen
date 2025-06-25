SELECT
    CASE WHEN COUNT(*) > 0 THEN true ELSE false END
FROM
    bookmarks
WHERE
    url = /* url */'example'