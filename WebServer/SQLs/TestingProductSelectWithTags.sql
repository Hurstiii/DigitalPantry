-- SQLite
SELECT tags.colour,
  tags.tag
FROM products
  INNER JOIN (
    SELECT *
    FROM prod_tags
      INNER JOIN tags ON prod_tags.tag_id = tags.id
  ) as tags ON products.barcode = tags.barcode
  AND tags.barcode = "0910823123"
ORDER BY name ASC