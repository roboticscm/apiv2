CREATE OR REPLACE FUNCTION tsk_find_tasks(_user_id BIGINT, _menu_path TEXT, _dep_id BIGINT, _page BIGINT, _page_size BIGINT)
RETURNS TEXT AS $$
DECLARE 
	full_query TEXT;
	ret_val TEXT;
	data_level_cond TEXT;
	list_staff TEXT;
	full_count_query TEXT;
	payload_query TEXT;
	order_by TEXT;
BEGIN
list_staff = get_list_staff_ids(_user_id, _menu_path, _dep_id);
IF list_staff IS NULL THEN -- full control
	data_level_cond = '';
ELSEIF list_staff = '' THEN -- prevent access
	data_level_cond = ' AND FALSE ';
ELSE
	data_level_cond = ' and t.created_by in (' || list_staff || ')';
END IF;

full_count_query = format('
	SELECT COUNT(*)
	FROM tsk_task t
	WHERE t.deleted_by IS NULL
');

payload_query = FORMAT('
	WITH tmp_payload AS (
	SELECT t.*
	FROM tsk_task t
	WHERE t.deleted_by IS NULL
	OFFSET %s
	LIMIT %s
	)	
	SELECT t.id, t.name, project.name as "projectName", t.start_time, t.deadline,
		COALESCE(t.updated_date, t.created_date) AS updated_or_created_date,
		array_agg(st.name ORDER BY std.start_time DESC) AS "lastStatusName",
		array_agg(DISTINCT h_assigner.last_name || '' '' || h_assigner.first_name) AS "assigners",
		array_agg(DISTINCT h_assignee.last_name || '' '' || h_assignee.first_name) AS "assignees",
		array_agg(DISTINCT h_evaluator.last_name || '' '' || h_evaluator.first_name) AS "evaluators"
	FROM tmp_payload t

	LEFT JOIN tsk_project project ON project.id = t.project_id

	LEFT JOIN tsk_status_detail std ON std.task_id = t.id
	LEFT JOIN tsk_status st ON st.id = std.status_id

	LEFT JOIN tsk_assign_human_or_org a_asigner ON a_asigner.task_id = t.id AND a_asigner.assign_position=%L
	LEFT JOIN human_or_org h_assigner ON h_assigner.id = a_asigner.human_or_org_id

	LEFT JOIN tsk_assign_human_or_org a_asignee ON a_asignee.task_id = t.id AND a_asignee.assign_position=%L
	LEFT JOIN human_or_org h_assignee ON h_assignee.id = a_asignee.human_or_org_id

	LEFT JOIN tsk_assign_human_or_org a_evaluator ON a_evaluator.task_id = t.id AND a_evaluator.assign_position=%L
	LEFT JOIN human_or_org h_evaluator ON h_evaluator.id = a_evaluator.human_or_org_id

	GROUP BY t.id, t.name, project.name, t.start_time, t.deadline, t.updated_date, t.created_date
					  
', (_page - 1)*_page_size, _page_size, 'ASSIGNER', 'ASSIGNEE', 'EVALUATOR');

order_by = 'ORDER BY updated_or_created_date DESC';

RETURN  paginate(full_count_query, payload_query, order_by);
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;





CREATE OR REPLACE FUNCTION tsk_get_task_by_id(_id BIGINT) RETURNS TEXT AS $$
DECLARE
	_query TEXT;
	ret_val TEXT;
BEGIN
_query = FORMAT('SELECT COALESCE(json_agg(t), ''[]'')::TEXT 
FROM(
	SELECT t.*, 
		array_agg(distinct taf.file_name) AS "taskAttachFiles",
		array_agg(distinct jsonb_build_object(''id'', assigner.human_or_org_id, ''name'', h_assigner.last_name || '' '' || h_assigner.first_name)) AS "assigners",
		array_agg(distinct jsonb_build_object(''id'', assignee.human_or_org_id, ''name'', h_assignee.last_name || '' '' || h_assignee.first_name)) AS "assignees",
		array_agg(distinct jsonb_build_object(''id'', evaluator.human_or_org_id, ''name'', h_evaluator.last_name || '' '' || h_evaluator.first_name)) AS "evaluators",
		array_agg(distinct jsonb_build_object(''id'', char.owner_org_id, ''name'', o_char.name)) as "chars",
		array_agg(distinct jsonb_build_object(''id'', target_person.human_or_org_id, ''name'', h_target_person.last_name || '' '' || h_target_person.first_name)) AS "targetPersons",
		array_agg(distinct jsonb_build_object(''id'', target_team.owner_org_id, ''name'', o_target_team.name)) as "targetTeams",
		array_agg(jsonb_build_object(''index'', assigner_status_detail.id, ''id'', assigner_status_detail.id, ''taskId'', assigner_status_detail.task_id, ''statusId'', assigner_status.id, ''status'', assigner_status.name, ''startTime'', assigner_status_detail.start_time, ''endTime'', assigner_status_detail.end_time, ''note'', assigner_status_detail.note,
				''attachFiles'', (SELECT array_agg(file_name) FROM tsk_status_attach_file WHERE status_detail_id=assigner_status_detail.id)
				
		) ORDER BY assigner_status_detail.start_time) AS "assignerStatusDetails"
	FROM tsk_task t
				
	LEFT JOIN tsk_task_attach_file taf ON taf.task_id = t.id
				
	LEFT JOIN tsk_assign_human_or_org assigner ON assigner.task_id = t.id AND assigner.assign_position=%L
	LEFT JOIN human_or_org h_assigner ON h_assigner.id = assigner.human_or_org_id
			
	LEFT JOIN tsk_assign_human_or_org assignee ON assignee.task_id = t.id AND assignee.assign_position=%L
	LEFT JOIN human_or_org h_assignee ON h_assignee.id = assignee.human_or_org_id
				
	LEFT JOIN tsk_assign_human_or_org evaluator ON evaluator.task_id = t.id AND evaluator.assign_position=%L
	LEFT JOIN human_or_org h_evaluator ON h_evaluator.id = evaluator.human_or_org_id
				
	LEFT JOIN tsk_assign_owner_org char ON char.task_id = t.id AND char.assign_position=%L
	LEFT JOIN owner_org o_char ON o_char.id = char.owner_org_id
				
	LEFT JOIN tsk_assign_human_or_org target_person ON target_person.task_id = t.id AND target_person.assign_position=%L
	LEFT JOIN human_or_org h_target_person ON h_target_person.id = target_person.human_or_org_id
	
	LEFT JOIN tsk_assign_owner_org target_team ON target_team.task_id = t.id AND target_team.assign_position=%L
	LEFT JOIN owner_org o_target_team ON o_target_team.id = target_team.owner_org_id
				
	LEFT JOIN tsk_status_detail assigner_status_detail ON assigner_status_detail.task_id = t.id AND assigner_status_detail.assign_position=%L
	LEFT JOIN tsk_status assigner_status ON assigner_status.id = assigner_status_detail.status_id
				
	WHERE t.id = %L
	GROUP BY t.id
) as t', 'ASSIGNER', 'ASSIGNEE', 'EVALUATOR', 'CHAR', 'TARGET_PERSON', 'TARGET_TEAM', 'ASSIGNER', _id );

EXECUTE _query INTO ret_val;
RETURN  ret_val;
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;
