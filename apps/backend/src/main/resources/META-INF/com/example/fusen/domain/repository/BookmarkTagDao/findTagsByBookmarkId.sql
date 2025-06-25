SELECT
    t.id,
    t.name,
    t.created_at,
    t.updated_at
FROM
    tags t
    INNER JOIN bookmark_tags bt ON t.id = bt.tag_id
WHERE
    bt.bookmark_id = /* bookmarkId */1