CREATE INDEX part_notification_idx_access_date ON part_notification(access_date DESC NULLS LAST);



CREATE OR REPLACE FUNCTION find_notifications(_user_id BIGINT, _text_search TEXT)
RETURNS TEXT AS $$
DECLARE 
	ret_val TEXT;
	_query TEXT;
	cond TEXT = '';
BEGIN
IF _text_search IS NOT NULL THEN
	cond = ' AND notify.full_text_search like ''%' || _text_search || '%''';
END IF;

_query = format('
	SELECT notify.id, notify.title, notify.target_id, notify.menu_path, notify.department_id, notify.created_date,
		notify.type, notify.is_finished, notify.is_read, notify.is_cancel,
		from_human_full_name, to_human_full_name,
		department_name
	FROM (
		SELECT 
			ROW_NUMBER() OVER (PARTITION BY notify.type ORDER BY notify.is_read, notify.created_date DESC NULLS LAST) AS row_number,
			notify.*,
			from_human.last_name || '' '' || from_human.first_name AS from_human_full_name,
			to_human.last_name || '' '' || to_human.first_name AS to_human_full_name,
			oo.name as department_name
		FROM part_notification notify
		LEFT JOIN human_or_org from_human ON from_human.id = notify.from_human_id
		LEFT JOIN human_or_org to_human ON to_human.id = notify.to_human_id
		LEFT JOIN owner_org oo ON oo.id = notify.department_id
		WHERE notify.to_human_id = %L
			%s
	) notify
	WHERE notify.row_number < 1000
', _user_id, cond);

EXECUTE 'SELECT COALESCE(JSON_AGG (t), ''[]'') FROM (' || _query || ') t' INTO ret_val;
RETURN ret_val;
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;