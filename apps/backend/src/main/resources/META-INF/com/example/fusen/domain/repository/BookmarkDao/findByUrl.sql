SELECT
    id,
    url,
    title,
    description,
    created_at,
    updated_at
FROM
    bookmarks
WHERE
    url = /* url */'example'
