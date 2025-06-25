UPDATE bookmarks
SET
    url = /* bookmark.url */'example',
    title = /* bookmark.title */'title',
    description = /* bookmark.description */'description',
    updated_at = NOW()
WHERE
    id = /* bookmark.id */1