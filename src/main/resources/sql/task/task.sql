CREATE EXTENSION pg_trgm;

CREATE OR REPLACE FUNCTION f_unaccent(text)
  RETURNS text AS
$func$
SELECT unaccent('unaccent', $1)
$func$  LANGUAGE sql IMMUTABLE SET search_path = public, pg_temp;



DROP INDEX tsk_task_idx_01;
CREATE INDEX tsk_task_idx_01 ON tsk_task (access_date DESC NULLS LAST);
   

ALTER TABLE tsk_task
ADD COLUMN document TSVECTOR
GENERATED ALWAYS AS (TO_TSVECTOR('ENGLISH', LOWER(F_UNACCENT(COALESCE(name, ''))) || ' ' || LOWER(F_UNACCENT(COALESCE(description, ''))) || ' ' || LOWER(F_UNACCENT(COALESCE(evaluate_comment, ''))))) STORED;

DROP INDEX tsk_task_idx_02;
CREATE INDEX tsk_task_idx_02 ON tsk_task USING GIN(document);


DROP INDEX tsk_task_idx_03;
CREATE INDEX tsk_task_idx_03 ON tsk_task (created_by);


CREATE INDEX tsk_task_idx_04 ON tsk_task USING GIN(F_UNACCENT(name) gin_trgm_ops);


CREATE OR REPLACE FUNCTION tsk_find_tasks(_user_id BIGINT, _menu_path TEXT, _dep_id BIGINT, _page BIGINT, _page_size BIGINT,
  _text_search TEXT,
  _task_name TEXT,
  _project_name TEXT,
  _assignee_name TEXT,
  _assigner_name TEXT,
  _evaluator_name TEXT,
  _is_completed boolean,
  _is_delay_deadline boolean,
  _created_date_from bigint,
  _created_date_to bigint)
RETURNS TEXT AS $$
DECLARE 
	ret_val TEXT;

	data_level_cond1 TEXT = '';
	data_level_cond2 TEXT = '';
	quick_cond TEXT = '';
	adv_cond TEXT = '';
	
	list_staff TEXT;
	order_by TEXT = '';
	final_order_by TEXT = '';
	
	tmp_query TEXT;
	full_count_query TEXT;
	payload_query TEXT;
	full_query TEXT;
	
	_offset bigint = (_page - 1)*_page_size;
	_limit bigint = _page_size;
	
	_now BIGINT = extract(epoch from now())::bigint;
BEGIN

-- data level
list_staff = get_list_staff_ids(_user_id, _menu_path, _dep_id);

IF list_staff = '' THEN -- prevent access
	data_level_cond1 = ' AND FALSE ';
	data_level_cond2 = ' AND FALSE ';
ELSEIF list_staff IS NOT NULL THEN
 	data_level_cond1 = FORMAT('
 		AND task.created_by IN (%s)				 
	', list_staff);
	
	data_level_cond2 = FORMAT('
 		AND task.created_by NOT IN (%s)
		AND EXISTS (
			SELECT FROM tsk_assign_human_or_org h 
			WHERE task_id = task.id 
				AND human_or_org_id IN (%s) 
				AND (assign_position IN(%L, %L)
					OR (assign_position = %L AND task.submit_status = 1	)		 
				)				  
 			)					 
	', list_staff, list_staff, 'ASSIGNER', 'EVALUATOR', 'ASSIGNEE');
END IF;

-- quick search
IF _text_search IS NOT NULL THEN
	quick_cond = FORMAT('
		AND document @@ TO_TSQUERY(%L)
	', LOWER(F_UNACCENT(_text_search)));
END IF;


-- adv-search
IF _task_name IS NOT NULL THEN
	adv_cond = FORMAT(' AND F_UNACCENT(task.name) ~* %L', LOWER(F_UNACCENT(_task_name)) );
END IF;

IF _project_name IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT('
		AND EXISTS(
			SELECT FROM tsk_project WHERE deleted_by IS NULL AND id = task.project_id AND F_UNACCENT(name) ~* %L
		)
', LOWER(F_UNACCENT(_project_name)) );
END IF;


IF _assignee_name IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT('
		AND EXISTS(
			SELECT FROM tsk_assign_human_or_org assignee WHERE assignee.assign_position=%L AND assignee.task_id = task.id AND EXISTS(
				SELECT FROM human_or_org
				WHERE deleted_by IS NULL AND id = assignee.human_or_org_id AND F_UNACCENT(last_name || '' '' || first_name)  ~* %L
			) 
		)
', 'ASSIGNEE', LOWER(F_UNACCENT(_assignee_name)) );
END IF;


IF _assigner_name IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT('
		AND EXISTS(
			SELECT FROM tsk_assign_human_or_org assigner WHERE assigner.assign_position=%L AND assigner.task_id = task.id AND EXISTS(
				SELECT FROM human_or_org
				WHERE deleted_by IS NULL AND id = assigner.human_or_org_id AND F_UNACCENT(last_name || '' '' || first_name)  ~* %L
			) 
		)
', 'ASSIGNER', LOWER(F_UNACCENT(_assigner_name)) );
END IF;


IF _evaluator_name IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT('
		AND EXISTS(
			SELECT FROM tsk_assign_human_or_org evaluator WHERE evaluator.assign_position=%L AND evaluator.task_id = task.id AND EXISTS(
				SELECT FROM human_or_org
				WHERE deleted_by IS NULL AND id = evaluator.human_or_org_id AND F_UNACCENT(last_name || '' '' || first_name)  ~* %L
			) 
		)
', 'EVALUATOR', LOWER(F_UNACCENT(_evaluator_name)) );
END IF;


IF _is_completed IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT(' AND task.evaluate_complete = %L ', _is_completed);
END IF;


IF _is_delay_deadline = true THEN
	adv_cond = adv_cond || FORMAT(' AND (task.deadline < task.assignee_end_time OR (task.assignee_end_time IS NULL AND task.deadline <= %L) )', _now);
END IF;

IF _created_date_from IS NOT NULL AND _created_date_to IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT(' AND task.created_date BETWEEN %L AND %L ', _created_date_from, _created_date_to);
ELSEIF _created_date_from IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT(' AND task.created_date >= %L', _created_date_from);
ELSEIF _created_date_to IS NOT NULL THEN
	adv_cond = adv_cond || FORMAT(' AND task.created_date <= %L', _created_date_to);
END IF;

-- tmp table
IF data_level_cond2 != '' THEN
	tmp_query = FORMAT('
	WITH tmp AS (
		SELECT task.*
		FROM tsk_task task
		WHERE task.deleted_by IS NULL
			%s
			%s
			%s

		UNION ALL

		SELECT task.*
		FROM tsk_task task
		WHERE task.deleted_by IS NULL
			%s
			%s
			%s
	)', data_level_cond1, quick_cond, adv_cond, data_level_cond2, quick_cond, adv_cond);
ELSE 
	tmp_query = FORMAT('
	WITH tmp AS (
		SELECT task.*
		FROM tsk_task task
		WHERE task.deleted_by IS NULL
			%s
			%s
			%s
	)', data_level_cond1, quick_cond, adv_cond);
END IF;

full_count_query = format('
	SELECT COUNT(*)
	FROM tmp
');

order_by = 'ORDER BY access_date DESC NULLS LAST';
final_order_by = 'ORDER BY t.access_date DESC NULLS LAST';

payload_query = FORMAT('
	SELECT 
		t.id, t.name, project.name as project_name, t.start_time, t.deadline, priority.name as priority_name,
		t.assignee_start_time, t.assignee_end_time, t.evaluate_time,
		array_agg(verity.name ORDER BY std.start_time DESC NULLS LAST) AS "lastStatusName",
		array_agg(DISTINCT h_assigner.last_name || '' '' || h_assigner.first_name) AS "assigners",
		array_agg(DISTINCT h_assignee.last_name || '' '' || h_assignee.first_name) AS "assignees",
		array_agg(DISTINCT h_evaluator.last_name || '' '' || h_evaluator.first_name) AS "evaluators"
	FROM (
		SELECT *
		FROM tmp 
		%s 
		OFFSET %s
		LIMIT %s
	) t
		LEFT JOIN tsk_project project ON project.id = t.project_id
					   
		LEFT JOIN tsk_priority priority ON priority.id = t.priority_id

		LEFT JOIN tsk_status_detail std ON std.task_id = t.id AND std.submit_status = 1 AND std.assign_position=%L
		LEFT JOIN tsk_task_verification verity ON verity.id = std.verification_id

		LEFT JOIN tsk_assign_human_or_org a_asigner ON a_asigner.task_id = t.id AND a_asigner.assign_position=%L
		LEFT JOIN human_or_org h_assigner ON h_assigner.id = a_asigner.human_or_org_id

		LEFT JOIN tsk_assign_human_or_org a_asignee ON a_asignee.task_id = t.id AND a_asignee.assign_position=%L
		LEFT JOIN human_or_org h_assignee ON h_assignee.id = a_asignee.human_or_org_id

		LEFT JOIN tsk_assign_human_or_org a_evaluator ON a_evaluator.task_id = t.id AND a_evaluator.assign_position=%L
		LEFT JOIN human_or_org h_evaluator ON h_evaluator.id = a_evaluator.human_or_org_id
		GROUP BY t.id, t.name, project.name, t.start_time, t.deadline, t.access_date, priority.name,
			t.assignee_start_time, t.assignee_end_time, t.evaluate_time	
		%s
', order_by, _offset, _limit, 'ASSIGNEE', 'ASSIGNER', 'ASSIGNEE', 'EVALUATOR', final_order_by);
raise notice '%', tmp_query;

RETURN  sys_build_json(tmp_query, full_count_query, payload_query);
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;






CREATE OR REPLACE FUNCTION tsk_get_task_by_id(_id BIGINT) RETURNS TEXT AS $$
DECLARE
	_query TEXT;
	ret_val TEXT;
BEGIN
_query = FORMAT('SELECT COALESCE(json_agg(t), ''[]'')::TEXT 
FROM(
	SELECT t.*, creator.last_name || '' '' || creator.first_name as "creatorFullName",
		array_agg(distinct taf.file_name) AS "taskAttachFiles",
		array_agg(distinct jsonb_build_object(''id'', assigner.human_or_org_id, ''name'', h_assigner.last_name || '' '' || h_assigner.first_name)) AS "assigners",
		array_agg(distinct jsonb_build_object(''id'', assignee.human_or_org_id, ''name'', h_assignee.last_name || '' '' || h_assignee.first_name)) AS "assignees",
		array_agg(distinct jsonb_build_object(''id'', evaluator.human_or_org_id, ''name'', h_evaluator.last_name || '' '' || h_evaluator.first_name)) AS "evaluators",
		array_agg(distinct jsonb_build_object(''id'', char.owner_org_id, ''name'', o_char.name)) as "chars",
		array_agg(distinct jsonb_build_object(''id'', target_person.human_or_org_id, ''name'', h_target_person.last_name || '' '' || h_target_person.first_name)) AS "targetPersons",
		array_agg(distinct jsonb_build_object(''id'', target_team.owner_org_id, ''name'', o_target_team.name)) as "targetTeams",
				
		array_agg(jsonb_build_object(''index'', assignee_status_detail.id, ''id'', assignee_status_detail.id, ''taskId'', assignee_status_detail.task_id, ''verificationId'', assignee_verify.id, ''status'', assignee_verify.name, ''percent'', assignee_verify.percent, ''startTime'', assignee_status_detail.start_time, ''endTime'', assignee_status_detail.end_time, ''note'', assignee_status_detail.note, ''submitStatus'', assignee_status_detail.submit_status,
				''attachFiles'', (SELECT array_agg(file_name) FROM tsk_status_attach_file WHERE status_detail_id=assignee_status_detail.id)
		) ORDER BY assignee_status_detail.start_time) AS "assigneeStatusDetails",
				
		array_agg(jsonb_build_object(''index'', assigner_status_detail.id, ''id'', assigner_status_detail.id, ''taskId'', assigner_status_detail.task_id, ''statusId'', assigner_status.id, ''status'', assigner_status.name, ''startTime'', assigner_status_detail.start_time, ''endTime'', assigner_status_detail.end_time, ''note'', assigner_status_detail.note, ''submitStatus'', assigner_status_detail.submit_status,
				''attachFiles'', (SELECT array_agg(file_name) FROM tsk_status_attach_file WHERE status_detail_id=assigner_status_detail.id)
		) ORDER BY assigner_status_detail.start_time) AS "assignerStatusDetails"
				
	FROM tsk_task t
				
	LEFT JOIN human_or_org creator ON creator.id = t.created_by
				
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
				
	LEFT JOIN tsk_status_detail assignee_status_detail ON assignee_status_detail.task_id = t.id AND assignee_status_detail.assign_position=%L
	LEFT JOIN tsk_task_verification assignee_verify ON assignee_verify.id = assignee_status_detail.verification_id
				
	LEFT JOIN tsk_status_detail assigner_status_detail ON assigner_status_detail.task_id = t.id AND assigner_status_detail.assign_position=%L
	LEFT JOIN tsk_status assigner_status ON assigner_status.id = assigner_status_detail.status_id
				
	WHERE t.id = %L
	GROUP BY t.id, "creatorFullName"
) as t', 'ASSIGNER', 'ASSIGNEE', 'EVALUATOR', 'CHAR', 'TARGET_PERSON', 'TARGET_TEAM', 'ASSIGNEE', 'ASSIGNER', _id );

EXECUTE _query INTO ret_val;
RETURN  ret_val;
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;
