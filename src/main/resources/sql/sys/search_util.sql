CREATE OR REPLACE FUNCTION find_search_field_list_by_menu_path(_menu_path text)
RETURNS TEXT AS $$
DECLARE
	_query TEXT;
BEGIN
_query = FORMAT('
	SELECT field.name, field.field as id, field.type
	FROM search_field field
	INNER JOIN search_field_menu field_menu ON field_menu.search_field_id = field.id
	INNER JOIN menu ON menu.id = field_menu.menu_id AND menu.path=%L
	WHERE field.disabled = FALSE AND field.deleted_by IS NULL
	ORDER BY field_menu.counter DESC NULLS LAST
', _menu_path);

RETURN json_query(_query);
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;